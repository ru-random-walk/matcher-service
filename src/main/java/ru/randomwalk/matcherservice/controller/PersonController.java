package ru.randomwalk.matcherservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.randomwalk.matcherservice.model.dto.ClubDto;
import ru.randomwalk.matcherservice.model.dto.LocationDto;
import ru.randomwalk.matcherservice.model.dto.PersonDto;
import ru.randomwalk.matcherservice.model.dto.request.ClubFilterRequest;
import ru.randomwalk.matcherservice.model.dto.response.UserScheduleDto;
import ru.randomwalk.matcherservice.service.facade.PersonFacade;

import java.security.Principal;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/person")
public class PersonController {

    private final PersonFacade personFacade;

    @PutMapping("/filter/clubs")
    @Operation(summary = "Change current user clubs filter")
    public List<ClubDto> changeClubFilter(@Validated @RequestBody ClubFilterRequest request, Principal principal) {
        log.info("PUT /person/filter/club from {} with body: {}", principal.getName(), request);
        return personFacade.changeClubFilter(request, principal.getName());
    }

    @PutMapping("/location")
    @Operation(summary = "Change current user location")
    public void changeLocation(@Validated @RequestBody LocationDto request, Principal principal) {
        log.info("PUT /person/location from {} with body {}", principal.getName(), request);
        personFacade.changeCurrentLocation(request, principal.getName());
    }

    @GetMapping("/location")
    @Operation(summary = "Get current user location")
    public LocationDto getLocation(Principal principal) {
        log.info("GET /person/location for {}", principal.getName());
        return personFacade.getLocationInfo(principal.getName());
    }

    @GetMapping("/info")
    @Operation(summary = "Get current user information")
    public PersonDto getPersonInfo(Principal principal) {
        log.info("GET /person/info for {}", principal.getName());
        return personFacade.getPersonInfo(principal.getName());
    }

    @GetMapping("/clubs")
    @Operation(summary = "Get information about clubs in filter")
    public List<ClubDto> getClubs(
            @Parameter(description = """
             If inFilter = null returns all clubs for current user.
             Otherwise returns clubs with the same inFilter value.""")
            @RequestParam(required = false)
            Boolean inFilter,
            Principal principal
    ) {
        log.info("GET /person/clubs, inFilter = {} for user {}", inFilter, principal.getName());
        return personFacade.getClubs(inFilter, principal.getName());
    }

    @GetMapping("/schedule")
    @Operation(summary = "Get user's schedule")
    public List<UserScheduleDto> getUserSchedule(Principal principal) {
        log.info("GET /person/schedule for user {}", principal.getName());
        return personFacade.getUserSchedule(principal.getName());
    }
}
