package ru.randomwalk.matcherservice.config.quartz;

import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {

    @Bean
    public Scheduler scheduler(List<Trigger> triggers, SchedulerFactoryBean factory) throws SchedulerException {
        factory.setWaitForJobsToCompleteOnShutdown(true);
        factory.setTransactionManager(new JdbcTransactionManager());
        var scheduler = factory.getScheduler();
        rescheduleJobs(triggers, scheduler);
        scheduler.start();
        return scheduler;
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
