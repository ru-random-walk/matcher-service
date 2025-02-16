package ru.randomwalk.matcherservice.service.mapper;

import org.locationtech.jts.geom.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.control.DeepClone;
import ru.randomwalk.matcherservice.model.dto.AvailableTimeModifyDto;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.time.OffsetTime;
import java.util.HashSet;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AvailableTimeMapper {

    @DeepClone
    @Mapping(target = "id", ignore = true)
    AvailableTime clone(AvailableTime entityToClone);


    @Mapping(target = "personId", source = "personId")
    @Mapping(target = "timezone", source = "createDto.timeFrom", qualifiedByName = "getTimeZone")
    @Mapping(target = "location", source = "createDto", qualifiedByName = "getLocation")
    @Mapping(target = "clubsInFilter", expression = "java(new java.util.HashSet(createDto.clubsInFilter()))")
    AvailableTime fromModifyDto(AvailableTimeModifyDto createDto, UUID personId);

    @Named("getTimeZone")
    default String getTimeZone(OffsetTime offsetTime) {
        return offsetTime.getOffset().getId();
    }

    @Named("getLocation")
    default Point getLocation(AvailableTimeModifyDto createDto) {
        return GeometryUtil.createPoint(createDto.longitude(), createDto.latitude());
    }

    default AvailableTime replaceAvailableTime(AvailableTime availableTime, AvailableTimeModifyDto modifyDto) {
        availableTime.setTimeFrom(modifyDto.timeFrom());
        availableTime.setTimeUntil(modifyDto.timeUntil());
        availableTime.setLocation(getLocation(modifyDto));
        availableTime.setTimezone(getTimeZone(modifyDto.timeFrom()));
        availableTime.setClubsInFilter(new HashSet<>(modifyDto.clubsInFilter()));
        return availableTime;
    }

}
