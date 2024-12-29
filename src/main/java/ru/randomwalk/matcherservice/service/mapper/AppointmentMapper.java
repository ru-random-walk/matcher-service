package ru.randomwalk.matcherservice.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.randomwalk.matcherservice.model.dto.AppointmentDetailsDto;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "participants", source = "participantIds")
    AppointmentDetailsDto toDto(AppointmentDetails entity, List<UUID> participantIds);
}
