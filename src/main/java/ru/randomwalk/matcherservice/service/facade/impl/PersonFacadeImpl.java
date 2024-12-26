package ru.randomwalk.matcherservice.service.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.model.dto.ClubDto;
import ru.randomwalk.matcherservice.model.dto.LocationDto;
import ru.randomwalk.matcherservice.model.dto.PersonDto;
import ru.randomwalk.matcherservice.model.dto.request.ClubFilterRequest;
import ru.randomwalk.matcherservice.model.dto.response.UserScheduleDto;
import ru.randomwalk.matcherservice.model.entity.Club;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.facade.PersonFacade;
import ru.randomwalk.matcherservice.service.mapper.ClubMapper;
import ru.randomwalk.matcherservice.service.mapper.PersonMapper;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonFacadeImpl implements PersonFacade {

    private final PersonService personService;
    private final PersonMapper personMapper;
    private final ClubMapper clubMapper;

    @Override
    public List<ClubDto> changeClubFilter(ClubFilterRequest request, String userName) {
        UUID personId = UUID.fromString(userName);
        List<Club> personClubs = personService.changeClubsInFilter(personId, request.filterType(), request.clubsInFilter());
        return clubMapper.toDtos(personClubs);
    }

    @Override
    public void changeCurrentLocation(LocationDto request, String userName) {
        personService.changeCurrentLocation(
                UUID.fromString(userName),
                request.longitude(),
                request.latitude(),
                request.searchAreaMeters()
        );
    }

    @Override
    public PersonDto getPersonInfo(String userName) {
        UUID id = UUID.fromString(userName);
        Person person = personService.findById(id);
        return personMapper.toDto(person);
    }

    @Override
    public List<ClubDto> getClubs(Boolean inFilter, String userName) {
        UUID personId = UUID.fromString(userName);
        List<Club> clubs = personService.getClubsForPerson(personId, inFilter);
        return clubMapper.toDtos(clubs);
    }

    @Override
    public LocationDto getLocationInfo(String userName) {
        UUID personId = UUID.fromString(userName);
        Person person = personService.findById(personId);
        return personMapper.getLocationDtoFromPersonEntity(person);
    }

    @Override
    public List<UserScheduleDto> getUserSchedule(String userName) {
        UUID personId = UUID.fromString(userName);
        Person person = personService.findById(personId);

        return personMapper.getScheduleForPerson(person);
    }

}
