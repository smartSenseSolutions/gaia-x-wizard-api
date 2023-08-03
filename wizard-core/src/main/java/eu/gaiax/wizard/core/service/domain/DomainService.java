/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.core.service.domain;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.*;
import eu.gaiax.wizard.api.exception.ParticipantNotFoundException;
import eu.gaiax.wizard.api.model.RegistrationStatus;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.setting.AWSSettings;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.job.ScheduleService;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DomainService {

    private final AWSSettings awsSettings;
    private final AmazonRoute53 amazonRoute53;
    private final ParticipantRepository participantRepository;
    private final ScheduleService scheduleService;

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


        request.setHostedZoneId(this.awsSettings.hostedZoneId());
        ChangeResourceRecordSetsResult result = this.amazonRoute53.changeResourceRecordSets(request);

        if (action.name().equalsIgnoreCase("CREATE")) {
            String status = result.getChangeInfo().getStatus();
            String changeId = result.getChangeInfo().getId();
            int count = 0;
            log.debug("status  ->{} count - >{}", status, ++count);

            while (!status.equalsIgnoreCase(ChangeStatus.INSYNC.name()) && count <= 12) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    log.error("Interrupted!", e);
                    Thread.currentThread().interrupt();
                }
                GetChangeRequest getChangeRequest = new GetChangeRequest()
                        .withId(changeId);
                ChangeInfo changeInfo = this.amazonRoute53.getChange(getChangeRequest).getChangeInfo();
                status = changeInfo.getStatus();
                log.debug("status  ->{} count - >{}", status, ++count);
            }
        }

        log.info("TXT record updated -> {}, result-> {}", domainName, result);
    }

    public void deleteTxtRecordForSSLCertificate(String domainName, String value) {
        try {
            this.updateTxtRecords(domainName, value, ChangeAction.DELETE);
        } catch (Exception e) {
            log.error("Can not delete txt records for domain ->{}", domainName, e); //TODO need to check if record is already exist
        }
        log.info("TXT record deleted -> {}", domainName);
    }


    public void createTxtRecordForSSLCertificate(String domainName, String value) {
        try {
            this.updateTxtRecords(domainName, value, ChangeAction.CREATE);
        } catch (Exception e) {
            log.error("Can not create txt records for domain ->{}", domainName, e); //TODO need to check if record is already created
        }
        log.info("TXT record created -> {} ", domainName);
    }

    public void createSubDomain(UUID participantId) {
        Participant participant = this.participantRepository.findById(participantId).orElse(null);
        Validate.isNull(participant).launch(new ParticipantNotFoundException("Participant not found"));
        try {
            String domainName = participant.getDomain();
            ResourceRecord resourceRecord = new ResourceRecord();
            resourceRecord.setValue(this.awsSettings.serverIp());

            ResourceRecordSet recordsSet = new ResourceRecordSet();
            recordsSet.setResourceRecords(List.of(resourceRecord));
            recordsSet.setType(RRType.A);
            recordsSet.setTTL(900L);
            recordsSet.setName(domainName);

            Change change = new Change(ChangeAction.CREATE, recordsSet);

            ChangeBatch batch = new ChangeBatch(List.of(change));

            ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest();
            request.setChangeBatch(batch);

            request.setHostedZoneId(this.awsSettings.hostedZoneId());
            ChangeResourceRecordSetsResult result = this.amazonRoute53.changeResourceRecordSets(request);
            log.info("subdomain created -> {} for participant id->{}, result-> {}", domainName, participant.getId(), result);
            participant.setStatus(RegistrationStatus.DOMAIN_CREATED.getStatus());

            //create job to create certificate
            this.createCertificateCreationJob(participant);
        } catch (Exception e) {
            log.error("Can not create sub domain for participant -> {}", participant.getId(), e);
            participant.setStatus(RegistrationStatus.DOMAIN_CREATION_FAILED.getStatus());
        } finally {
            this.participantRepository.save(participant);
        }
    }

    private void createCertificateCreationJob(Participant participant) {
        try {
            this.scheduleService.createJob(participant.getId().toString(), StringPool.JOB_TYPE_CREATE_CERTIFICATE, 0); //try for 3 time for certificate
        } catch (SchedulerException e) {
            log.error("Can not create certificate creation job for enterprise->{}", participant.getDid(), e);
            participant.setStatus(RegistrationStatus.CERTIFICATE_CREATION_FAILED.getStatus());
        }
    }
}