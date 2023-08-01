/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.core.service.job;


import eu.gaiax.wizard.api.model.StringPool;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * The type Schedule service.
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleService.class);

    private final Scheduler scheduler;

    /**
     * Delete job.
     *
     * @param jobKey the job key
     */
    public void deleteJob(JobKey jobKey) {
        try {
            this.scheduler.deleteJob(jobKey);
            LOGGER.debug("Job deleted for group-{}, name-{}", jobKey.getGroup(), jobKey.getName());
        } catch (SchedulerException e) {
            LOGGER.error("Can not delete job with group-{}, name-{}", jobKey.getGroup(), jobKey.getName());
        }
    }

    /**
     * Create job.
     *
     * @param did   the did
     * @param type  the type
     * @param count the count
     * @throws SchedulerException the scheduler exception
     */
    public void createJob(String did, String type, int count) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(ScheduledJobBean.class)
                .withIdentity(UUID.randomUUID().toString(), type)
                .storeDurably()
                .requestRecovery()
                .usingJobData(StringPool.DID, did)
                .usingJobData(StringPool.JOB_TYPE, type)
                .build();

        SimpleTrigger activateEnterpriseUserTrigger = TriggerBuilder.newTrigger()
                .forJob(job)
                .withIdentity(UUID.randomUUID().toString(), type)
                .startAt(new Date(System.currentTimeMillis() + 10000)) //start after 10 sec
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(count).withIntervalInSeconds(30))
                .build();
        this.scheduler.scheduleJob(job, activateEnterpriseUserTrigger);
        LOGGER.debug("{}: job created for enterprise id->{}", type, did);
    }
}
