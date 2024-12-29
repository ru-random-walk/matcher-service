package ru.randomwalk.matcherservice.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.control.DeepClone;
import ru.randomwalk.matcherservice.model.dto.request.AvailableTimeRequestDto;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.model.entity.DayLimit;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AvailableTimeMapper {

    @DeepClone
    @Mapping(target = "id", ignore = true)
    AvailableTime clone(AvailableTime entityToClone);

    default List<AvailableTime> fromRequests(List<AvailableTimeRequestDto> dtos, UUID personId) {
        return dtos.stream()
                .flatMap(dto -> getEntitiesFromRequest(dto, personId).stream())
                .collect(Collectors.toList());
    }

    default List<AvailableTime> getEntitiesFromRequest(AvailableTimeRequestDto dto, UUID personId) {
        return dto.timeFrames().stream()
                .map(timeFrame -> AvailableTime.builder()
                        .timezone(timeFrame.timeFrom().getOffset().getId())
                        .personId(personId)
                        .date(dto.date())
                        .timeFrom(timeFrame.timeFrom())
                        .timeUntil(timeFrame.timeUntil())
                        .dayLimit(buildDayLimit(personId, dto.date(), dto.walkCount()))
                        .build()
                ).collect(Collectors.toList());
    }


    default DayLimit buildDayLimit(UUID personId, LocalDate date, Integer walkCount) {
        return DayLimit.builder()
                .walkCount(walkCount)
                .dayLimitId(new DayLimit.DayLimitId(personId, date))
                .build();
    }


}
