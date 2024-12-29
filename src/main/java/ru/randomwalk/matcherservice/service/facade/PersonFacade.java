package ru.randomwalk.matcherservice.service.facade;

import ru.randomwalk.matcherservice.model.dto.ClubDto;
import ru.randomwalk.matcherservice.model.dto.LocationDto;
import ru.randomwalk.matcherservice.model.dto.PersonDto;
import ru.randomwalk.matcherservice.model.dto.request.ClubFilterRequest;
import ru.randomwalk.matcherservice.model.dto.response.UserScheduleDto;

import java.util.List;

public interface PersonFacade {

    List<ClubDto> changeClubFilter(ClubFilterRequest request, String userName);

    void changeCurrentLocation(LocationDto request, String userName);

    PersonDto getPersonInfo(String userName);

    List<ClubDto> getClubs(Boolean inFilter, String userName);

    LocationDto getLocationInfo(String userName);

    List<UserScheduleDto> getUserSchedule(String userName);
}
