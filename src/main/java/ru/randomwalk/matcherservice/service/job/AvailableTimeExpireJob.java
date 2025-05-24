package ru.randomwalk.matcherservice.service.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.DayLimit;
import ru.randomwalk.matcherservice.repository.AvailableTimeRepository;
import ru.randomwalk.matcherservice.repository.DayLimitRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AvailableTimeExpireJob implements Job {

    private final AvailableTimeRepository availableTimeRepository;
    private final DayLimitRepository dayLimitRepository;

    @Transactional
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime deleteFrom = ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1).toLocalDateTime();
        log.info("Starting AvailableTimeExpireJob. Deleting all available times before {} UTC", deleteFrom);

        List<AvailableTime> availableTimesToDelete = availableTimeRepository.findAllByDateBefore(deleteFrom);
        List<DayLimit.DayLimitId> dayLimitIdsToDelete = getDayLimitIdsFromAvailableTimes(availableTimesToDelete);
        List<DayLimit> dayLimitsToDelete = dayLimitRepository.findAllById(dayLimitIdsToDelete);

        availableTimeRepository.deleteAllInBatch(availableTimesToDelete);
        dayLimitRepository.deleteAllInBatch(dayLimitsToDelete);

        log.info(
                "End of AvailableTimeExpireJob. Deleted {} availableTimes. Deleted {} dayLimits",
                availableTimesToDelete.size(),
                dayLimitsToDelete.size()
        );
    }

    private List<DayLimit.DayLimitId> getDayLimitIdsFromAvailableTimes(List<AvailableTime> availableTime) {
        return availableTime.stream()
                .map(time -> new DayLimit.DayLimitId(time.getPersonId(), time.getDate()))
                .collect(Collectors.toList());
    }
}
