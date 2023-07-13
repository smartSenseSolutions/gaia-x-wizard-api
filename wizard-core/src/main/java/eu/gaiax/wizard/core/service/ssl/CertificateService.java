/*
 * Copyright (c) 2023 | smartSense
 */
package eu.gaiax.wizard.core.service.ssl;

import eu.gaiax.wizard.api.models.RegistrationStatus;
import eu.gaiax.wizard.api.models.StringPool;
import eu.gaiax.wizard.api.utils.CommonUtils;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.core.service.domain.DomainService;
import eu.gaiax.wizard.core.service.job.ScheduleService;
import eu.gaiax.wizard.dao.entity.Enterprise;
import eu.gaiax.wizard.dao.entity.EnterpriseCertificate;
import eu.gaiax.wizard.dao.repository.EnterpriseCertificateRepository;
import eu.gaiax.wizard.dao.repository.EnterpriseRepository;
import org.quartz.JobKey;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.toolbox.AcmeUtils;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * A simple client test tool.
 * <p>
 * Pass the names of the domains as parameters.
 */
@Service
public class CertificateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateService.class);
    private static final File USER_KEY_FILE = new File("user.key");

    //Challenge type to be used
    private static final ChallengeType CHALLENGE_TYPE = ChallengeType.DNS;

    // RSA key size of generated key pairs
    private static final int KEY_SIZE = 2048;

    private static final Logger LOG = LoggerFactory.getLogger(CertificateService.class);
    /**
     * The constant CAN_NOT_CONVERT_FILE_IN_PCCS_8_FORMATE_FOR_ENTERPRISE.
     */
    public static final String CAN_NOT_CONVERT_FILE_IN_PCCS_8_FORMATE_FOR_ENTERPRISE = "Can not convert file in pccs8 formate for enterprise->{}";

    /**
     * Instantiates a new Certificate service.
     *
     * @param domainService                   the domain service
     * @param enterpriseRepository            the enterprise repository
     * @param s3Utils                         the s 3 utils
     * @param enterpriseCertificateRepository the enterprise certificate repository
     * @param scheduleService                 the schedule service
     */
    public CertificateService(DomainService domainService, EnterpriseRepository enterpriseRepository, S3Utils s3Utils, EnterpriseCertificateRepository enterpriseCertificateRepository, ScheduleService scheduleService) {
        this.domainService = domainService;
        this.enterpriseRepository = enterpriseRepository;
        this.s3Utils = s3Utils;
        this.enterpriseCertificateRepository = enterpriseCertificateRepository;
        this.scheduleService = scheduleService;
    }

    private enum ChallengeType {
        /**
         * Dns challenge type.
         */
        DNS
    }

    private final DomainService domainService;

    private final EnterpriseRepository enterpriseRepository;

    private final S3Utils s3Utils;

    private final EnterpriseCertificateRepository enterpriseCertificateRepository;

    private final ScheduleService scheduleService;

    /**
     * Create ssl certificate.
     *
     * @param enterpriseId the enterprise id
     * @param jobKey       the job key
     */
    public void createSSLCertificate(long enterpriseId, JobKey jobKey) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId).orElse(null);
        if (enterprise == null) {
            LOGGER.error("Invalid enterprise id");
            return;
        }
        String domain = enterprise.getSubDomainName();
        File domainChainFile = new File("/tmp/" + domain + "_chain.crt");
        File csrFile = new File("/tmp/" + domain + ".csr");
        File keyfile = new File("/tmp/" + domain + ".key");
        File pkcs8File = new File("/tmp/pkcs8_" + domain + ".key");

        try {

            // Load the user key file. If there is no key file, create a new one.
            KeyPair userKeyPair = loadOrCreateUserKeyPair();

            // Create a session for Let's Encrypt.
            // Use "acme://letsencrypt.org" for production server
            Session session = new Session("acme://letsencrypt.org");

            // Get the Account.
            // If there is no account yet, create a new one.
            Account acct = findOrRegisterAccount(session, userKeyPair);

            // Load or create a key pair for the domains. This should not be the userKeyPair!
            KeyPair domainKeyPair = loadOrCreateDomainKeyPair(keyfile);

            // Order the certificate
            Order order = acct.newOrder().domain(domain).create();

            // Perform all required authorizations
            for (Authorization auth : order.getAuthorizations()) {
                authorize(auth);
            }

            // Generate a CSR for all of the domains, and sign it with the domain key pair.
            CSRBuilder csrb = new CSRBuilder();
            csrb.addDomain(domain);
            csrb.sign(domainKeyPair);


            // Write the CSR to a file, for later use.
            try (Writer out = new FileWriter(csrFile)) {
                csrb.write(out);
            }

            // Order the certificate
            order.execute(csrb.getEncoded());

            // Wait for the order to complete
            checkOrderStatus(order);

            // Get the certificate
            Certificate certificate = order.getCertificate();

            List<X509Certificate> certificateChain1 = certificate.getCertificateChain();
            X509Certificate cert1 = certificateChain1.get(0);
            X509Certificate cert2 = certificateChain1.get(1);
            List<X509Certificate> fileCertificates = List.of(cert1, cert2);
            try (FileWriter fw = new FileWriter(domainChainFile)) {
                for (X509Certificate cert : fileCertificates) {
                    AcmeUtils.writeToPem(cert.getEncoded(), AcmeUtils.PemLabel.CERTIFICATE, fw);
                }
                //TODO this flow can be improved
                //write root certificate
                fw.append("""
                        -----BEGIN CERTIFICATE-----
                        MIIFazCCA1OgAwIBAgIRAIIQz7DSQONZRGPgu2OCiwAwDQYJKoZIhvcNAQELBQAw
                        TzELMAkGA1UEBhMCVVMxKTAnBgNVBAoTIEludGVybmV0IFNlY3VyaXR5IFJlc2Vh
                        cmNoIEdyb3VwMRUwEwYDVQQDEwxJU1JHIFJvb3QgWDEwHhcNMTUwNjA0MTEwNDM4
                        WhcNMzUwNjA0MTEwNDM4WjBPMQswCQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJu
                        ZXQgU2VjdXJpdHkgUmVzZWFyY2ggR3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBY
                        MTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAK3oJHP0FDfzm54rVygc
                        h77ct984kIxuPOZXoHj3dcKi/vVqbvYATyjb3miGbESTtrFj/RQSa78f0uoxmyF+
                        0TM8ukj13Xnfs7j/EvEhmkvBioZxaUpmZmyPfjxwv60pIgbz5MDmgK7iS4+3mX6U
                        A5/TR5d8mUgjU+g4rk8Kb4Mu0UlXjIB0ttov0DiNewNwIRt18jA8+o+u3dpjq+sW
                        T8KOEUt+zwvo/7V3LvSye0rgTBIlDHCNAymg4VMk7BPZ7hm/ELNKjD+Jo2FR3qyH
                        B5T0Y3HsLuJvW5iB4YlcNHlsdu87kGJ55tukmi8mxdAQ4Q7e2RCOFvu396j3x+UC
                        B5iPNgiV5+I3lg02dZ77DnKxHZu8A/lJBdiB3QW0KtZB6awBdpUKD9jf1b0SHzUv
                        KBds0pjBqAlkd25HN7rOrFleaJ1/ctaJxQZBKT5ZPt0m9STJEadao0xAH0ahmbWn
                        OlFuhjuefXKnEgV4We0+UXgVCwOPjdAvBbI+e0ocS3MFEvzG6uBQE3xDk3SzynTn
                        jh8BCNAw1FtxNrQHusEwMFxIt4I7mKZ9YIqioymCzLq9gwQbooMDQaHWBfEbwrbw
                        qHyGO0aoSCqI3Haadr8faqU9GY/rOPNk3sgrDQoo//fb4hVC1CLQJ13hef4Y53CI
                        rU7m2Ys6xt0nUW7/vGT1M0NPAgMBAAGjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNV
                        HRMBAf8EBTADAQH/MB0GA1UdDgQWBBR5tFnme7bl5AFzgAiIyBpY9umbbjANBgkq
                        hkiG9w0BAQsFAAOCAgEAVR9YqbyyqFDQDLHYGmkgJykIrGF1XIpu+ILlaS/V9lZL
                        ubhzEFnTIZd+50xx+7LSYK05qAvqFyFWhfFQDlnrzuBZ6brJFe+GnY+EgPbk6ZGQ
                        3BebYhtF8GaV0nxvwuo77x/Py9auJ/GpsMiu/X1+mvoiBOv/2X/qkSsisRcOj/KK
                        NFtY2PwByVS5uCbMiogziUwthDyC3+6WVwW6LLv3xLfHTjuCvjHIInNzktHCgKQ5
                        ORAzI4JMPJ+GslWYHb4phowim57iaztXOoJwTdwJx4nLCgdNbOhdjsnvzqvHu7Ur
                        TkXWStAmzOVyyghqpZXjFaH3pO3JLF+l+/+sKAIuvtd7u+Nxe5AW0wdeRlN8NwdC
                        jNPElpzVmbUq4JUagEiuTDkHzsxHpFKVK7q4+63SM1N95R1NbdWhscdCb+ZAJzVc
                        oyi3B43njTOQ5yOf+1CceWxG1bQVs5ZufpsMljq4Ui0/1lvh+wjChP4kqKOJ2qxq
                        4RgqsahDYVvTH9w7jXbyLeiNdd8XM2w9U/t7y0Ff/9yi0GE44Za4rF2LN9d11TPA
                        mRGunUHBcnWEvgJBQl9nJEiU0Zsnvgc/ubhPgXRR4Xq37Z0j4r7g1SgEEzwxA57d
                        emyPxgcYxn/eR44/KJ4EBs+lVDR3veyJm+kXQ99b21/+jh5Xos1AnX5iItreGCc=
                        -----END CERTIFICATE-----
                        """);
            }


            LOG.info("Success! The certificate for domains {} has been generated!", domain);
            LOG.info("Certificate URL: {}", certificate.getLocation());

            String certificateChainS3Key = enterpriseId + "/x509CertificateChain.pem";
            String csrS3Key = enterpriseId + "/" + csrFile.getName();
            String keyS3Key = enterpriseId + "/" + keyfile.getName();
            String pkcs8FileS3Key = enterpriseId + "/pkcs8_" + keyfile.getName();


            //convert private key in pkcs8 format
            convertKeyFileInPKCS8(keyfile.getAbsolutePath(), pkcs8File.getAbsolutePath(), enterpriseId);

            //save files in s3
            s3Utils.uploadFile(certificateChainS3Key, domainChainFile);
            s3Utils.uploadFile(csrS3Key, csrFile);
            s3Utils.uploadFile(keyS3Key, keyfile);
            s3Utils.uploadFile(pkcs8FileS3Key, pkcs8File);


            enterprise.setStatus(RegistrationStatus.CERTIFICATE_CREATED.getStatus());

            EnterpriseCertificate enterpriseCertificate = enterpriseCertificateRepository.getByEnterpriseId(enterpriseId);
            if (enterpriseCertificate == null) {
                enterpriseCertificate = EnterpriseCertificate.builder()
                        .certificateChain(certificateChainS3Key)
                        .enterpriseId(enterpriseId)
                        .csr(csrS3Key)
                        .privateKey(keyS3Key)
                        .build();
            } else {
                enterpriseCertificate.setCertificateChain(certificateChainS3Key);
                enterpriseCertificate.setCsr(csrS3Key);
                enterpriseCertificate.setPrivateKey(pkcs8FileS3Key);
            }
            //save certificate location
            enterpriseCertificateRepository.save(enterpriseCertificate);

            //create Job tp create ingress and tls secret
            scheduleService.createJob(enterpriseId, StringPool.JOB_TYPE_CREATE_INGRESS, 0);
            if (jobKey != null) {
                //delete job
                scheduleService.deleteJob(jobKey);
            }

        } catch (Exception e) {
            LOGGER.error("Can not create certificate for enterprise ->{}, domain ->{}", enterpriseId, enterprise.getSubDomainName(), e);
            enterprise.setStatus(RegistrationStatus.CERTIFICATE_CREATION_FAILED.getStatus());
        } finally {
            enterpriseRepository.save(enterprise);
            //delete files
            CommonUtils.deleteFile(domainChainFile, csrFile, keyfile, pkcs8File);
        }
    }

    private static void checkOrderStatus(Order order) throws AcmeException {
        try {
            int attempts = 10;
            while (order.getStatus() != Status.VALID && attempts-- > 0) {
                LOGGER.debug("Waiting for order confirmation attempts->{}", attempts);
                // Did the order fail?
                if (order.getStatus() == Status.INVALID) {
                    LOG.error("Order has failed, reason: {}", order.getError());
                    throw new AcmeException("Order failed... Giving up.");
                }

                // Wait for a few seconds
                Thread.sleep(6000L);

                // Then update the status
                order.update();
            }
        } catch (InterruptedException ex) {
            LOG.error("interrupted", ex);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Loads a user key pair from {@link #USER_KEY_FILE}. If the file does not exist, a
     * new key pair is generated and saved.
     * <p>
     * Keep this key pair in a safe place! In a production environment, you will not be
     * able to access your account again if you should lose the key pair.
     *
     * @return User's {@link KeyPair}.
     */
    private KeyPair loadOrCreateUserKeyPair() throws IOException {
        if (USER_KEY_FILE.exists()) {
            // If there is a key file, read it
            try (FileReader fr = new FileReader(USER_KEY_FILE)) {
                return KeyPairUtils.readKeyPair(fr);
            }

        } else {
            // If there is none, create a new key pair and save it
            KeyPair userKeyPair = KeyPairUtils.createKeyPair(KEY_SIZE);
            try (FileWriter fw = new FileWriter(USER_KEY_FILE)) {
                KeyPairUtils.writeKeyPair(userKeyPair, fw);
            }
            return userKeyPair;
        }
    }

    private KeyPair loadOrCreateDomainKeyPair(File domainChainFile) throws IOException {
        if (domainChainFile.exists()) {
            try (FileReader fr = new FileReader(domainChainFile)) {
                return KeyPairUtils.readKeyPair(fr);
            }
        } else {
            KeyPair domainKeyPair = KeyPairUtils.createKeyPair(KEY_SIZE);
            try (FileWriter fw = new FileWriter(domainChainFile)) {
                KeyPairUtils.writeKeyPair(domainKeyPair, fw);
            }
            return domainKeyPair;
        }
    }

    /**
     * Finds your {@link Account} at the ACME server. It will be found by your user's
     * public key. If your key is not known to the server yet, a new account will be
     * created.
     * <p>
     * This is a simple way of finding your {@link Account}. A better way is to get the
     * URL of your new account with {@link Account#getLocation()} and store it somewhere.
     * If you need to get access to your account later, reconnect to it via {@link
     * Session#login(URL, KeyPair)} by using the stored location.
     *
     * @param session {@link Session} to bind with
     * @return {@link Account}
     */
    private Account findOrRegisterAccount(Session session, KeyPair accountKey) throws AcmeException {

        Account account = new AccountBuilder()
                .agreeToTermsOfService()
                .useKeyPair(accountKey)
                .create(session);
        LOG.info("Registered a new user, URL: {}", account.getLocation());

        return account;
    }

    /**
     * Authorize a domain. It will be associated with your account, so you will be able to
     * retrieve a signed certificate for the domain later.
     *
     * @param auth {@link Authorization} to perform
     */
    private void authorize(Authorization auth) throws AcmeException {
        LOG.info("Authorization for domain {}", auth.getIdentifier().getDomain());
        Dns01Challenge dnsChallenge = auth.findChallenge(Dns01Challenge.TYPE);
        String valuesToBeAdded = dnsChallenge.getDigest();
        String domain = Dns01Challenge.toRRName(auth.getIdentifier());

        try {
            // The authorization is already valid. No need to process a challenge.
            if (auth.getStatus() == Status.VALID) {
                return;
            }

            // Find the desired challenge and prepare it.
            Challenge challenge = null;
            if (CHALLENGE_TYPE == ChallengeType.DNS) {
                challenge = dnsChallenge(auth);
            }

            if (challenge == null) {
                throw new AcmeException("No challenge found");
            }

            // If the challenge is already verified, there's no need to execute it again.
            if (challenge.getStatus() == Status.VALID) {
                return;
            }

            // Now trigger the challenge.
            challenge.trigger();

            // Poll for the challenge to complete.
            //TODO known issue, sometime certificate issuing not working
            try {
                int attempts = 3;
                while (challenge.getStatus() != Status.VALID && attempts-- > 0) {
                    LOGGER.debug("Waiting for 30 sec before check of DNS record attempts -> {}", attempts);
                    // Wait for a few seconds
                    Thread.sleep(30000L);

                    // Then update the status
                    challenge.update();
                }
                // Did the authorization fail?
                if (challenge.getStatus() == Status.INVALID) {
                    LOG.error("Challenge has failed, reason: {}", challenge.getError());
                    throw new AcmeException("Challenge failed... Giving up.");
                }
            } catch (InterruptedException ex) {
                LOG.error("interrupted", ex);
                Thread.currentThread().interrupt();
            }

            // All reattempts are used up and there is still no valid authorization?
            if (challenge.getStatus() != Status.VALID) {
                throw new AcmeException("Failed to pass the challenge for domain "
                        + auth.getIdentifier().getDomain() + ", ... Giving up.");
            }

            LOG.info("Challenge has been completed. Remember to remove the validation resource.");

        } finally {
            domainService.deleteTxtRecordForSSLCertificate(domain, valuesToBeAdded);
        }
    }


    /**
     * Prepares a DNS challenge.
     * <p>
     * The verification of this challenge expects a TXT record with a certain content.
     * <p>
     * This example outputs instructions that need to be executed manually. In a
     * production environment, you would rather configure your DNS automatically.
     *
     * @param auth {@link Authorization} to find the challenge in
     * @return {@link Challenge} to verify
     */
    private Challenge dnsChallenge(Authorization auth) throws AcmeException {
        // Find a single dns-01 challenge
        Dns01Challenge challenge = auth.findChallenge(Dns01Challenge.TYPE);
        if (challenge == null) {
            throw new AcmeException("Found no " + Dns01Challenge.TYPE + " challenge, don't know what to do...");
        }

        String valuesToBeAdded = challenge.getDigest();
        String domain = Dns01Challenge.toRRName(auth.getIdentifier());

        //Create TXT records
        domainService.createTxtRecordForSSLCertificate(domain, valuesToBeAdded);

        return challenge;
    }


    private void convertKeyFileInPKCS8(String file, String outputFile, long enterpriseId) {
        try {
            ProcessBuilder pb = new ProcessBuilder("openssl", "pkcs8", "-topk8", "-in", file, "-nocrypt", "-out", outputFile);
            Process p = pb.start();
            int exitCode = p.waitFor();
            if (exitCode == 0) {
                LOGGER.debug("key file converted in pkcs8 format foe enterprise id->{}", enterpriseId);
            } else {
                LOGGER.error(CAN_NOT_CONVERT_FILE_IN_PCCS_8_FORMATE_FOR_ENTERPRISE, enterpriseId);
            }
        } catch (InterruptedException e) {
            LOGGER.error(CAN_NOT_CONVERT_FILE_IN_PCCS_8_FORMATE_FOR_ENTERPRISE, enterpriseId, e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            LOGGER.error(CAN_NOT_CONVERT_FILE_IN_PCCS_8_FORMATE_FOR_ENTERPRISE, enterpriseId, e);
        }

    }
}