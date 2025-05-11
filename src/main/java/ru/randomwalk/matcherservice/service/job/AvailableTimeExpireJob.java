package ru.randomwalk.matcherservice.service.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.repository.AvailableTimeRepository;
import ru.randomwalk.matcherservice.repository.DayLimitRepository;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AvailableTimeExpireJob implements Job {

    private final AvailableTimeRepository availableTimeRepository;
    private final DayLimitRepository dayLimitRepository;

    @Transactional
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDate currentDate = LocalDate.now();
        log.info("Starting AvailableTimeExpireJob. Deleting all available times before {} in UTC", currentDate);

        int availableTimeDeleted = availableTimeRepository.deleteAllByDateBefore(currentDate);
        int dayLimitDeleted = dayLimitRepository.deleteAllByDateBefore(currentDate);

        log.info("End of AvailableTimeExpireJob. Deleted {} availableTimes and {} dayLimits", availableTimeDeleted, dayLimitDeleted);
    }
}
