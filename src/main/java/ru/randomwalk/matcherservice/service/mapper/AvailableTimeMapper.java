package ru.randomwalk.matcherservice.service.mapper;

import com.nimbusds.jose.util.Pair;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.control.DeepClone;
import ru.randomwalk.matcherservice.model.dto.request.AvailableTimeRequestDto;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AvailableTimeMapper {

    List<AvailableTime> fromRequests(List<AvailableTimeRequestDto> dtos, UUID personId);

    @DeepClone
    @Mapping(target = "id", ignore = true)
    AvailableTime clone(AvailableTime entityToClone);

    default List<AvailableTime> getEntitiesFromRequest(AvailableTimeRequestDto dto, UUID personId) {
        return dto.timeFrames().stream()
                .map(timeFrame -> ru.randomwalk.matcherservice.model.entity.AvailableTime.builder()
                        .timezone(dto.timezone())
                        .personId(personId)
                        .date(dto.date())
                        .timeFrom(timeFrame.timeFrom())
                        .timeUntil(timeFrame.timeUntil())
                        .build()
                ).collect(Collectors.toList());
    }
}
