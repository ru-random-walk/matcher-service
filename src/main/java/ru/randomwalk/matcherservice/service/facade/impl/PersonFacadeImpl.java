package ru.randomwalk.matcherservice.service.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.randomwalk.matcherservice.model.dto.ClubDto;
import ru.randomwalk.matcherservice.model.dto.PersonDto;
import ru.randomwalk.matcherservice.model.dto.UserScheduleDto;
import ru.randomwalk.matcherservice.model.entity.Club;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.facade.PersonFacade;
import ru.randomwalk.matcherservice.service.mapper.ClubMapper;
import ru.randomwalk.matcherservice.service.mapper.PersonMapper;
import ru.randomwalk.matcherservice.service.mapper.ScheduleMapper;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonFacadeImpl implements PersonFacade {

    private final PersonService personService;
    private final PersonMapper personMapper;
    private final ClubMapper clubMapper;
    private final ScheduleMapper scheduleMapper;

    @Override
    public PersonDto getPersonInfo(String userName) {
        UUID id = UUID.fromString(userName);
        Person person = personService.findById(id);
        return personMapper.toDto(person);
    }

    @Override
    public List<ClubDto> getClubs(String userName) {
        UUID personId = UUID.fromString(userName);
        List<Club> clubs = personService.getClubsForPerson(personId);
        return clubMapper.toDtos(clubs);
    }

    @Override
    public List<UserScheduleDto> getUserSchedule(String userName) {
        UUID personId = UUID.fromString(userName);
        Person person = personService.findById(personId);

        return scheduleMapper.getScheduleForPerson(person);
    }

}
