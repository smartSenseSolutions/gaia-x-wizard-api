/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.core.service.enterprise;

import eu.gaiax.wizard.api.models.RegisterRequest;
import eu.gaiax.wizard.api.models.RegistrationStatus;
import eu.gaiax.wizard.api.models.StringPool;
import eu.gaiax.wizard.api.models.setting.AWSSettings;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.job.ScheduleService;
import eu.gaiax.wizard.dao.entity.Enterprise;
import eu.gaiax.wizard.dao.repository.EnterpriseRepository;
import org.quartz.SchedulerException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Registration service.
 *
 * @author Nitin
 * @version 1.0
 */
@Service
public class RegistrationService {

    private final EnterpriseRepository enterpriseRepository;

    private final ScheduleService scheduleService;

    private final AWSSettings awsSettings;

    /**
     * Instantiates a new Registration service.
     *
     * @param enterpriseRepository the enterprise repository
     * @param scheduleService      the schedule service
     * @param awsSettings          the aws settings
     */
    public RegistrationService(EnterpriseRepository enterpriseRepository, ScheduleService scheduleService, AWSSettings awsSettings) {
        this.enterpriseRepository = enterpriseRepository;
        this.scheduleService = scheduleService;
        this.awsSettings = awsSettings;
    }

    /**
     * Test long.
     *
     * @return the long
     */
    public long test() {
        return enterpriseRepository.count();
    }

    /**
     * Register enterprise enterprise.
     *
     * @param registerRequest the register request
     * @return the enterprise
     * @throws SchedulerException the scheduler exception
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public Enterprise registerEnterprise(RegisterRequest registerRequest) throws SchedulerException {
        //check legal name
        Validate.isTrue(enterpriseRepository.existsByLegalName(registerRequest.getLegalName())).launch("duplicate.legal.name");

        //check email
        Validate.isTrue(enterpriseRepository.existsByEmail(registerRequest.getEmail())).launch("duplicate.email");

        //check sub domain
        Validate.isTrue(enterpriseRepository.existsBySubDomainName(registerRequest.getSubDomainName())).launch("duplicate.sub.domain");

        //check registration number
        Validate.isTrue(enterpriseRepository.existsByLegalRegistrationNumber(registerRequest.getLegalRegistrationNumber())).launch("duplicate.registration.number");

        String subdomain = registerRequest.getSubDomainName().toLowerCase() + "." + awsSettings.getBaseDomain();
        //save enterprise details
        Enterprise enterprise = enterpriseRepository.save(Enterprise.builder()
                .email(registerRequest.getEmail())
                .headquarterAddress(registerRequest.getHeadquarterAddress())
                .legalAddress(registerRequest.getLegalAddress())
                .legalName(registerRequest.getLegalName())
                .legalRegistrationNumber(registerRequest.getLegalRegistrationNumber())
                .legalRegistrationType(registerRequest.getLegalRegistrationType())
                .status(RegistrationStatus.STARTED.getStatus())
                .subDomainName(subdomain.trim())
                .password(BCrypt.hashpw(registerRequest.getPassword(), BCrypt.gensalt()))
                .build());

        //create job to create subdomain
        scheduleService.createJob(enterprise.getId(), StringPool.JOB_TYPE_CREATE_SUB_DOMAIN, 0);
        return enterprise;
    }
}
