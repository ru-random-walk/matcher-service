package ru.randomwalk.matcherservice.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.randomwalk.matcherservice.model.dto.PersonDto;
import ru.randomwalk.matcherservice.model.entity.Person;

@Mapper(componentModel = "spring", uses = {ClubMapper.class})
public interface PersonMapper {

    PersonDto toDto(Person entity);

    Person createPersonEntity(RegisteredUserInfoEvent addPersonDto);

}
