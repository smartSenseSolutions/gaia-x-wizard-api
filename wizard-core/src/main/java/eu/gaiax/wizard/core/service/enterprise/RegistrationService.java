/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.core.service.enterprise;

import eu.gaiax.wizard.api.model.RegisterRequest;
import eu.gaiax.wizard.api.model.RegistrationStatus;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.setting.AWSSettings;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.job.ScheduleService;
import eu.gaiax.wizard.core.service.keycloak.KeycloakService;
import eu.gaiax.wizard.dao.entity.Enterprise;
import eu.gaiax.wizard.dao.repository.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
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
@RequiredArgsConstructor
public class RegistrationService {

    private final EnterpriseRepository enterpriseRepository;

    private final ScheduleService scheduleService;

    private final AWSSettings awsSettings;

    private final KeycloakService keycloakService;

    /**
     * Test long.
     *
     * @return the long
     */
    public long test() {
        return this.enterpriseRepository.count();
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
        Validate.isTrue(this.enterpriseRepository.existsByLegalName(registerRequest.getLegalName())).launch("duplicate.legal.name");

        //check email
        Validate.isTrue(this.enterpriseRepository.existsByEmail(registerRequest.getEmail())).launch("duplicate.email");

        //check sub domain
        Validate.isTrue(this.enterpriseRepository.existsBySubDomainName(registerRequest.getSubDomainName())).launch("duplicate.sub.domain");

        //check registration number
        Validate.isTrue(this.enterpriseRepository.existsByLegalRegistrationNumber(registerRequest.getLegalRegistrationNumber())).launch("duplicate.registration.number");

        String subdomain = registerRequest.getSubDomainName().toLowerCase() + "." + this.awsSettings.baseDomain();
        //save enterprise details
        Enterprise enterprise = this.enterpriseRepository.save(Enterprise.builder()
            .email(registerRequest.getEmail())
            .headquarterAddress(registerRequest.getHeadquarterAddress())
            .legalAddress(registerRequest.getLegalAddress())
            .legalName(registerRequest.getLegalName())
            .legalRegistrationNumber(registerRequest.getLegalRegistrationNumber())
            .legalRegistrationType(registerRequest.getLegalRegistrationType())
            .status(RegistrationStatus.STARTED.getStatus())
            .subDomainName(subdomain.trim())
            .build());

        // add enterprise to keycloak
        this.keycloakService.addUser(registerRequest.getLegalName(), registerRequest.getEmail(), 12L);
        this.keycloakService.sendRequiredActionsEmail(registerRequest.getEmail());

        //create job to create subdomain
        this.scheduleService.createJob(enterprise.getId(), StringPool.JOB_TYPE_CREATE_SUB_DOMAIN, 0);
        return enterprise;
    }

    public void sendResetEmail(String email) {
        Validate.isFalse(this.enterpriseRepository.existsByEmail(email)).launch("invalid.email");
        this.keycloakService.sendRequiredActionsEmail(email);
    }
}
