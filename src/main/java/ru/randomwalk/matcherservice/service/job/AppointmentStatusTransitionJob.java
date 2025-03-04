package ru.randomwalk.matcherservice.service.job;

import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;
import ru.randomwalk.matcherservice.service.AppointmentDetailsService;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentStatusTransitionJob implements Job {

    public static final String TO_STATUS_KEY = "toStatus";
    public static final String APPOINTMENT_ID_KEY = "appointmentId";

    private final AppointmentDetailsService appointmentDetailsService;

    @Override
    @NewSpan
    public void execute(JobExecutionContext context)  {
        log.info("Executing appointment status transition job {}", context.getJobDetail().getKey());

        var dataMap = context.getJobDetail().getJobDataMap();
        var appointmentId = (UUID) dataMap.get(APPOINTMENT_ID_KEY);
        var toStatus = (AppointmentStatus) dataMap.get(TO_STATUS_KEY);

        appointmentDetailsService.changeStatus(appointmentId, toStatus);

        log.info("Appointment status transition job {} is completed", context.getJobDetail().getKey());
    }
}
