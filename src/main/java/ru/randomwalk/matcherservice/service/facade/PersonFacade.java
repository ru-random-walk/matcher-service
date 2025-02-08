package ru.randomwalk.matcherservice.service.facade;

import ru.randomwalk.matcherservice.model.dto.ClubDto;
import ru.randomwalk.matcherservice.model.dto.PersonDto;
import ru.randomwalk.matcherservice.model.dto.UserScheduleDto;

import java.util.List;

public interface PersonFacade {

    PersonDto getPersonInfo(String userName);

    List<ClubDto> getClubs(String userName);

    List<UserScheduleDto> getUserSchedule(String userName);
}
