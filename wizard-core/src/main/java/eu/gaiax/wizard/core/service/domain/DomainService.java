/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.core.service.domain;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import eu.gaiax.wizard.api.model.RegistrationStatus;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.setting.AWSSettings;
import eu.gaiax.wizard.core.service.job.ScheduleService;
import eu.gaiax.wizard.dao.entity.Enterprise;
import eu.gaiax.wizard.dao.repository.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The type Domain service.
 *
 * @author Nitin
 * @version 1.0
 */
@Service
public class DomainService {


    private static final Logger LOGGER = LoggerFactory.getLogger(DomainService.class);

    private final AWSSettings awsSettings;

    private final AmazonRoute53 amazonRoute53;

    private final EnterpriseRepository enterpriseRepository;

    private final ScheduleService scheduleService;


    /**
     * Instantiates a new Domain service.
     *
     * @param awsSettings          the aws settings
     * @param enterpriseRepository the enterprise repository
     * @param scheduleService      the schedule service
     */
    public DomainService(AWSSettings awsSettings, EnterpriseRepository enterpriseRepository, ScheduleService scheduleService) {
        this.awsSettings = awsSettings;
        this.amazonRoute53 = getAmazonRoute53();
        this.enterpriseRepository = enterpriseRepository;
        this.scheduleService = scheduleService;
    }

    /**
     * Update txt records.
     *
     * @param domainName the domain name
     * @param value      the value
     * @param action     the action
     */
    public void updateTxtRecords(String domainName, String value, ChangeAction action) {
        ResourceRecord resourceRecord = new ResourceRecord();
        resourceRecord.setValue("\"" + value + "\"");

        ResourceRecordSet recordsSet = new ResourceRecordSet();
        recordsSet.setResourceRecords(List.of(resourceRecord));
        recordsSet.setType(RRType.TXT);
        recordsSet.setTTL(900L);
        recordsSet.setName(domainName);

        Change change = new Change(action, recordsSet);

        ChangeBatch batch = new ChangeBatch(List.of(change));

        ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest();
        request.setChangeBatch(batch);


        request.setHostedZoneId(awsSettings.getHostedZoneId());
        ChangeResourceRecordSetsResult result = amazonRoute53.changeResourceRecordSets(request);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted!", e);
            Thread.currentThread().interrupt();
        }
        LOGGER.info("TXT record updated -> {}, result-> {}", domainName, result);
    }

    /**
     * Delete txt record for ssl certificate.
     *
     * @param domainName the domain name
     * @param value      the value
     */
    public void deleteTxtRecordForSSLCertificate(String domainName, String value) {
        try {
            updateTxtRecords(domainName, value, ChangeAction.DELETE);
        } catch (Exception e) {
            LOGGER.error("Can not delete txt records for domain ->{}", domainName, e); //TODO need to check if record is already exist
        }
        LOGGER.info("TXT record deleted -> {}", domainName);
    }


    /**
     * Create txt record for ssl certificate.
     *
     * @param domainName the domain name
     * @param value      the value
     */
    public void createTxtRecordForSSLCertificate(String domainName, String value) {
        try {
            updateTxtRecords(domainName, value, ChangeAction.CREATE);
        } catch (Exception e) {
            LOGGER.error("Can not create txt records for domain ->{}", domainName, e); //TODO need to check if record is already created
        }
        LOGGER.info("TXT record created -> {} ", domainName);
    }

    /**
     * Create sub domain.
     *
     * @param enterpriseId the enterprise id
     */
    public void createSubDomain(long enterpriseId) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId).orElse(null);
        if (enterprise == null) {
            LOGGER.error("Invalid enterprise id ->{}", enterpriseId);
            return;
        }
        try {
            String domainName = enterprise.getSubDomainName();
            ResourceRecord resourceRecord = new ResourceRecord();
            resourceRecord.setValue(awsSettings.getServerIp());

            ResourceRecordSet recordsSet = new ResourceRecordSet();
            recordsSet.setResourceRecords(List.of(resourceRecord));
            recordsSet.setType(RRType.A);
            recordsSet.setTTL(900L);
            recordsSet.setName(domainName);

            Change change = new Change(ChangeAction.CREATE, recordsSet);

            ChangeBatch batch = new ChangeBatch(List.of(change));

            ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest();
            request.setChangeBatch(batch);

            request.setHostedZoneId(awsSettings.getHostedZoneId());
            ChangeResourceRecordSetsResult result = amazonRoute53.changeResourceRecordSets(request);
            LOGGER.info("subdomain created -> {} for enterprise id->{}, result-> {}", domainName, enterpriseId, result);
            enterprise.setStatus(RegistrationStatus.DOMAIN_CREATED.getStatus());

            //create job to create certificate
            createCertificateCreationJob(enterpriseId, enterprise);
        } catch (Exception e) {
            LOGGER.error("Can not create sub domain for enterprise->{}", enterpriseId, e);
            enterprise.setStatus(RegistrationStatus.DOMAIN_CREATION_FAILED.getStatus());
        } finally {
            enterpriseRepository.save(enterprise);
        }
    }

    private void createCertificateCreationJob(long enterpriseId, Enterprise enterprise) {
        try {
            scheduleService.createJob(enterpriseId, StringPool.JOB_TYPE_CREATE_CERTIFICATE, 0); //try for 3 time for certificate
        } catch (SchedulerException e) {
            LOGGER.error("Can not create certificate creation job for enterprise->{}", enterprise, e);
            enterprise.setStatus(RegistrationStatus.CERTIFICATE_CREATION_FAILED.getStatus());
        }
    }

    private AmazonRoute53 getAmazonRoute53() {
        return AmazonRoute53ClientBuilder.standard().withCredentials(new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        return new AWSCredentials() {
                            @Override
                            public String getAWSAccessKeyId() {
                                return awsSettings.getAccessKey();
                            }

                            @Override
                            public String getAWSSecretKey() {
                                return awsSettings.getSecretKey();
                            }
                        };
                    }

                    @Override
                    public void refresh() {
                        //Do nothing
                    }
                })
                .withRegion(Regions.US_EAST_1)
                .build();
    }
}