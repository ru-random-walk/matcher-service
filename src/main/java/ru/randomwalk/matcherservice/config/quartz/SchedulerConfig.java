package ru.randomwalk.matcherservice.config.quartz;

import lombok.RequiredArgsConstructor;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import ru.randomwalk.matcherservice.service.job.OutboxSendingJob;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {

    private static final Integer OUTBOX_JOB_START_INTERVAL_IN_SEC = 5;

    @Bean
    public Scheduler scheduler(List<Trigger> triggers, List<JobDetail> jobDetails, SchedulerFactoryBean factory) throws SchedulerException {
        factory.setWaitForJobsToCompleteOnShutdown(true);
        factory.setTransactionManager(new JdbcTransactionManager());
        factory.setJobDetails(jobDetails.toArray(new JobDetail[0]));
        factory.setTriggers(triggers.toArray(new Trigger[0]));

        var scheduler = factory.getScheduler();
        rescheduleJobs(triggers, scheduler);
        scheduler.start();
        return scheduler;
    }

    @Bean
    public JobDetail outboxSendingJobDetail() {
        return JobBuilder.newJob()
                .storeDurably()
                .withIdentity("OutboxSendingJob")
                .ofType(OutboxSendingJob.class)
                .build();
    }

    @Bean
    public Trigger outboxSendingJobTrigger(@Qualifier("outboxSendingJobDetail") JobDetail outboxSendingJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(outboxSendingJobDetail)
                .withIdentity(outboxSendingJobDetail.getKey().getName())
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInSeconds(OUTBOX_JOB_START_INTERVAL_IN_SEC)
                                .repeatForever()
                )
                .build();
    }

    private void rescheduleJobs(List<Trigger> triggers, Scheduler scheduler) {
        triggers.forEach(trigger -> {
            try {
                if (!scheduler.checkExists(trigger.getJobKey())) {
                    scheduler.scheduleJob(trigger);
                } else {
                    scheduler.rescheduleJob(trigger.getKey(), trigger);
                }
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
