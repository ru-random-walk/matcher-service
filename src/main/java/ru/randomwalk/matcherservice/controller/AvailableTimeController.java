package ru.randomwalk.matcherservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.randomwalk.matcherservice.model.dto.request.AppointmentRequestDto;
import ru.randomwalk.matcherservice.model.dto.response.UserScheduleDto;
import ru.randomwalk.matcherservice.service.facade.AvailableTimeFacade;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/available-time")
public class AvailableTimeController {

    private final AvailableTimeFacade availableTimeFacade;

    @PostMapping("/add")
    @Operation(summary = "Add available time to schedule and search for appointments")
    public void addAvailableTime(
            @Validated @RequestBody AppointmentRequestDto request,
            Principal principal
    ) {
        log.info("POST /appointment/add request from {} with body: {}", principal.getName(), request);
        availableTimeFacade.addAvailableTime(request, principal);
    }

    @PutMapping("/change")
    @Operation(summary = "Change available time and search for appointments")
    public List<UserScheduleDto> changeSchedule(
            @Validated @RequestBody AppointmentRequestDto request,
            Principal principal
    ) {
        log.info("PUT /appointment/available-time/change from user {} with body: {}", principal.getName(), request);
        return Collections.emptyList();
    }
}
