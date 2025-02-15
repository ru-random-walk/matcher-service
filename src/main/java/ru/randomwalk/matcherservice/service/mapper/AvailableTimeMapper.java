package ru.randomwalk.matcherservice.service.mapper;

import org.locationtech.jts.geom.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.control.DeepClone;
import ru.randomwalk.matcherservice.model.dto.AvailableTimeCreateDto;
import ru.randomwalk.matcherservice.model.entity.AvailableTime;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.time.OffsetTime;
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
    AvailableTime fromCreationDto(AvailableTimeCreateDto createDto, UUID personId);

    @Named("getTimeZone")
    default String getTimeZone(OffsetTime offsetTime) {
        return offsetTime.getOffset().getId();
    }

    @Named("getLocation")
    default Point getLocation(AvailableTimeCreateDto createDto) {
        return GeometryUtil.createPoint(createDto.longitude(), createDto.latitude());
    }

}
