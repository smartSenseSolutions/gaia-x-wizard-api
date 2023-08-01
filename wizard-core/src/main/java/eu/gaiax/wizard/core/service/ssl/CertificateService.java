package eu.gaiax.wizard.core.service.ssl;

import eu.gaiax.wizard.api.model.RegistrationStatus;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.utils.CommonUtils;
import eu.gaiax.wizard.core.service.domain.DomainService;
import eu.gaiax.wizard.core.service.job.ScheduleService;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.vault.Vault;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.toolbox.AcmeUtils;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private static final File USER_KEY_FILE = new File("user.key");

    //Challenge type to be used
    private static final ChallengeType CHALLENGE_TYPE = ChallengeType.DNS;

    // RSA key size of generated key pairs
    private static final int KEY_SIZE = 2048;

    public static final String CAN_NOT_CONVERT_FILE_IN_PCCS_8_FORMATE_FOR_ENTERPRISE = "Can not convert file in pccs8 formate for enterprise->{}";


    private enum ChallengeType {
        DNS
    }

    private final Vault vault;
    private final DomainService domainService;
    private final ParticipantRepository participantRepository;
    private final ScheduleService scheduleService;

    public void createSSLCertificate(Participant participant, String did, String domain, JobKey jobKey) {
        File domainChainFile = new File("/tmp/" + domain + "_chain.crt");
        File csrFile = new File("/tmp/" + domain + ".csr");
        File keyfile = new File("/tmp/" + domain + ".key");
        File pkcs8File = new File("/tmp/pkcs8_" + domain + ".key");

        try {

            // Load the user key file. If there is no key file, create a new one.
            KeyPair userKeyPair = this.loadOrCreateUserKeyPair();

            // Create a session for Let's Encrypt.
            // Use "acme://letsencrypt.org" for production server
            Session session = new Session("acme://letsencrypt.org");

            // Get the Account.
            // If there is no account yet, create a new one.
            Account acct = this.findOrRegisterAccount(session, userKeyPair);

            // Load or create a key pair for the domains. This should not be the userKeyPair!
            KeyPair domainKeyPair = this.loadOrCreateDomainKeyPair(keyfile);

            // Order the certificate
            Order order = acct.newOrder().domain(domain).create();

            // Perform all required authorizations
            for (Authorization auth : order.getAuthorizations()) {
                this.authorize(auth);
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


            log.info("Success! The certificate for domains {} has been generated!", domain);
            log.info("Certificate URL: {}", certificate.getLocation());

            //convert private key in pkcs8 format
            this.convertKeyFileInPKCS8(keyfile.getAbsolutePath(), pkcs8File.getAbsolutePath(), did);

            //save files in vault
            this.uploadCertificatesToVault(participant.getDomain(), participant.getId().toString(), domainChainFile, csrFile, keyfile, pkcs8File);
//            this.s3Utils.uploadFile(certificateChainS3Key, domainChainFile);
//            this.s3Utils.uploadFile(csrS3Key, csrFile);
//            this.s3Utils.uploadFile(keyS3Key, keyfile);
//            this.s3Utils.uploadFile(pkcs8FileS3Key, pkcs8File);


            //create Job tp create ingress and tls secret
            this.scheduleService.createJob(did, StringPool.JOB_TYPE_CREATE_INGRESS, 0);
            if (jobKey != null) {
                //delete job
                this.scheduleService.deleteJob(jobKey);
            }

        } catch (Exception e) {
            log.error("Can not create certificate for did ->{}, domain ->{}", did, participant.getDomain(), e);
            participant.setStatus(RegistrationStatus.CERTIFICATE_CREATION_FAILED.getStatus());
        } finally {
            this.participantRepository.save(participant);
            //delete files
            CommonUtils.deleteFile(domainChainFile, csrFile, keyfile, pkcs8File);
        }
    }

    private static void checkOrderStatus(Order order) throws AcmeException {
        try {
            int attempts = 10;
            while (order.getStatus() != Status.VALID && attempts-- > 0) {
                log.debug("Waiting for order confirmation attempts->{}", attempts);
                // Did the order fail?
                if (order.getStatus() == Status.INVALID) {
                    log.error("Order has failed, reason: {}", order.getError());
                    throw new AcmeException("Order failed... Giving up.");
                }

                // Wait for a few seconds
                Thread.sleep(6000L);

                // Then update the status
                order.update();
            }
        } catch (InterruptedException ex) {
            log.error("interrupted", ex);
            Thread.currentThread().interrupt();
        }
    }

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

    private Account findOrRegisterAccount(Session session, KeyPair accountKey) throws AcmeException {

        Account account = new AccountBuilder()
                .agreeToTermsOfService()
                .useKeyPair(accountKey)
                .create(session);
        log.info("Registered a new user, URL: {}", account.getLocation());

        return account;
    }

    private void authorize(Authorization auth) throws AcmeException {
        log.info("Authorization for domain {}", auth.getIdentifier().getDomain());
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
                challenge = this.dnsChallenge(auth);
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
                    log.debug("Waiting for 30 sec before check of DNS record attempts -> {}", attempts);
                    // Wait for a few seconds
                    Thread.sleep(30000L);

                    // Then update the status
                    challenge.update();
                }
                // Did the authorization fail?
                if (challenge.getStatus() == Status.INVALID) {
                    log.error("Challenge has failed, reason: {}", challenge.getError());
                    throw new AcmeException("Challenge failed... Giving up.");
                }
            } catch (InterruptedException ex) {
                log.error("interrupted", ex);
                Thread.currentThread().interrupt();
            }

            // All reattempts are used up and there is still no valid authorization?
            if (challenge.getStatus() != Status.VALID) {
                throw new AcmeException("Failed to pass the challenge for domain "
                        + auth.getIdentifier().getDomain() + ", ... Giving up.");
            }

            log.info("Challenge has been completed. Remember to remove the validation resource.");

        } finally {
            this.domainService.deleteTxtRecordForSSLCertificate(domain, valuesToBeAdded);
        }
    }


    private Challenge dnsChallenge(Authorization auth) throws AcmeException {
        // Find a single dns-01 challenge
        Dns01Challenge challenge = auth.findChallenge(Dns01Challenge.TYPE);
        if (challenge == null) {
            throw new AcmeException("Found no " + Dns01Challenge.TYPE + " challenge, don't know what to do...");
        }

        String valuesToBeAdded = challenge.getDigest();
        String domain = Dns01Challenge.toRRName(auth.getIdentifier());

        //Create TXT records
        this.domainService.createTxtRecordForSSLCertificate(domain, valuesToBeAdded);

        return challenge;
    }


    private void convertKeyFileInPKCS8(String file, String outputFile, String did) {
        try {
            ProcessBuilder pb = new ProcessBuilder("openssl", "pkcs8", "-topk8", "-in", file, "-nocrypt", "-out", outputFile);
            Process p = pb.start();
            int exitCode = p.waitFor();
            if (exitCode == 0) {
                log.debug("key file converted in pkcs8 format foe did id->{}", did);
            } else {
                log.error(CAN_NOT_CONVERT_FILE_IN_PCCS_8_FORMATE_FOR_ENTERPRISE, did);
            }
        } catch (InterruptedException e) {
            log.error(CAN_NOT_CONVERT_FILE_IN_PCCS_8_FORMATE_FOR_ENTERPRISE, did, e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error(CAN_NOT_CONVERT_FILE_IN_PCCS_8_FORMATE_FOR_ENTERPRISE, did, e);
        }

    }

    private void uploadCertificatesToVault(String domainName, String secretName, File domainChain, File csrFile, File keyFile, File pkcs8Key) throws IOException {
        // this.s3Utils.uploadFile(certificateChainS3Key, domainChainFile);
        ////            this.s3Utils.uploadFile(csrS3Key, csrFile);
        ////            this.s3Utils.uploadFile(keyS3Key, keyfile);
        ////            this.s3Utils.uploadFile(pkcs8FileS3Key, pkcs8File);
        this.uploadCertificatesToVault(domainName, secretName,
                new String(Files.readAllBytes(domainChain.toPath())), new String(Files.readAllBytes(csrFile.toPath())),
                new String(Files.readAllBytes(keyFile.toPath())), new String(Files.readAllBytes(pkcs8Key.toPath())));
    }

    public void uploadCertificatesToVault(String domain, String secretName, String domainChain, String csr, String key, String pkcs8Key) throws IOException {
        Map<String, Object> data = new HashMap<>();
        if (StringUtils.hasText(domainChain)) {
            data.put("x509CertificateChain.pem", domainChain);
        }
        if (StringUtils.hasText(csr)) {
            data.put(domain + ".csr", csr);
        }
        if (StringUtils.hasText(key)) {
            data.put(domain + ".key", key);
        }
        if (StringUtils.hasText(pkcs8Key)) {
            data.put("pkcs8_" + domain + ".key", pkcs8Key);
        }
        this.vault.put(secretName, data);
    }
}