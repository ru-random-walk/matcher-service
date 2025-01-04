package ru.randomwalk.matcherservice.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.random.walk.dto.RegisteredUserInfoEvent;
import ru.randomwalk.matcherservice.model.dto.LocationDto;
import ru.randomwalk.matcherservice.model.dto.PersonDto;
import ru.randomwalk.matcherservice.model.dto.request.AddPersonDto;
import ru.randomwalk.matcherservice.model.entity.Person;

@Mapper(componentModel = "spring", uses = {ClubMapper.class})
public interface PersonMapper {

    @Mapping(target = "currentPosition", source = "entity", qualifiedByName = "getLocationDtoFromPersonEntity")
    PersonDto toDto(Person entity);

    Person createPersonEntity(RegisteredUserInfoEvent addPersonDto);

    @Named("getLocationDtoFromPersonEntity")
    default LocationDto getLocationDtoFromPersonEntity(Person person) {
        if (person == null) {
            return null;
        }

        return LocationDto.builder()
                .latitude(person.getLatitude())
                .longitude(person.getLongitude())
                .searchAreaMeters(person.getSearchAreaInMeters())
                .build();
    }

}
