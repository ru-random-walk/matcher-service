package ru.randomwalk.matcherservice.config.quartz;

import lombok.RequiredArgsConstructor;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.randomwalk.matcherservice.service.job.AvailableTimeExpireJob;
import ru.randomwalk.matcherservice.service.job.OutboxExpireJob;

import java.util.TimeZone;

@Configuration
@RequiredArgsConstructor
public class AvailableTimeExpireJobConfig {

    // Каждый час
    private static final String JOB_TRIGGER_CRON = "0 0 * * * ?";

    @Bean
    public JobDetail availableTimeExpireJobDetail() {
        return JobBuilder.newJob()
                .storeDurably()
                .withIdentity("AvailableTimeExpireJob")
                .ofType(AvailableTimeExpireJob.class)
                .requestRecovery()
                .build();
    }

    @Bean
    public Trigger availableTimeExpireJobTrigger(@Qualifier("availableTimeExpireJobDetail") JobDetail availableTimeExpireJob) {
        return TriggerBuilder.newTrigger()
                .forJob(availableTimeExpireJob)
                .withIdentity(availableTimeExpireJob.getKey().getName())
                .withSchedule(
                        CronScheduleBuilder
                                .cronSchedule(JOB_TRIGGER_CRON)
                                .inTimeZone(TimeZone.getTimeZone("UTC"))
                )
                .build();
    }

}
