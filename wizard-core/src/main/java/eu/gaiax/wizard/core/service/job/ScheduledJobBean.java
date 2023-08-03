/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.core.service.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.core.service.domain.DomainService;
import eu.gaiax.wizard.core.service.k8s.K8SService;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.core.service.ssl.CertificateService;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/**
 * The type Scheduled job bean.
 */
@Component
@Slf4j
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class ScheduledJobBean extends QuartzJobBean {
    private final DomainService domainService;
    private final CertificateService certificateService;
    private final K8SService k8SService;
    private final SignerService signerService;
    private final ParticipantRepository participantRepository;
    private final ObjectMapper mapper;

    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDetail jobDetail = context.getJobDetail();
        String jobType = jobDetail.getJobDataMap().getString(StringPool.JOB_TYPE);
        Participant participant = this.participantRepository.getByDid(jobDetail.getJobDataMap().getString(StringPool.DID));

        switch (jobType) {
            case StringPool.JOB_TYPE_CREATE_SUB_DOMAIN -> this.domainService.createSubDomain(participant);
            case StringPool.JOB_TYPE_CREATE_CERTIFICATE ->
                    this.certificateService.createSSLCertificate(participant, participant.getDid(), participant.getDomain(), jobDetail.getKey());
            case StringPool.JOB_TYPE_CREATE_INGRESS -> this.k8SService.createIngress(participant);
            case StringPool.JOB_TYPE_CREATE_DID -> this.signerService.createDid(participant);
            case StringPool.JOB_TYPE_CREATE_PARTICIPANT ->
                    this.signerService.createParticipantJson(participant, participant.getId().toString(), participant.isOwnDidSolution());
            default -> log.error("Invalid job type -> {}", jobType);
        }
        log.info("job completed");
    }

}
