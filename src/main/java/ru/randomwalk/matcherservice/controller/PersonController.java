package ru.randomwalk.matcherservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.randomwalk.matcherservice.model.dto.ClubDto;
import ru.randomwalk.matcherservice.model.dto.PersonDto;
import ru.randomwalk.matcherservice.model.dto.UserScheduleDto;
import ru.randomwalk.matcherservice.service.facade.PersonFacade;

import java.security.Principal;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/person")
public class PersonController {

    private final PersonFacade personFacade;

    @GetMapping("/info")
    @Operation(summary = "Get current user information")
    public PersonDto getPersonInfo(Principal principal) {
        log.info("GET /person/info for {}", principal.getName());
        return personFacade.getPersonInfo(principal.getName());
    }

    @GetMapping("/clubs")
    @Operation(summary = "Get information about clubs")
    public List<ClubDto> getClubs(
            Principal principal
    ) {
        log.info("GET /person/clubs for user {}", principal.getName());
        return personFacade.getClubs(principal.getName());
    }

    @GetMapping("/schedule")
    @Operation(summary = "Get user's schedule")
    public List<UserScheduleDto> getUserSchedule(Principal principal) {
        log.info("GET /person/schedule for user {}", principal.getName());
        return personFacade.getUserSchedule(principal.getName());
    }
}
