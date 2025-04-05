package ru.randomwalk.matcherservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.randomwalk.matcherservice.model.dto.AvailableTimeModifyDto;
import ru.randomwalk.matcherservice.service.facade.AvailableTimeFacade;

import java.security.Principal;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/available-time")
@PreAuthorize("hasAuthority('DEFAULT_USER')")
public class AvailableTimeController {

    private final AvailableTimeFacade availableTimeFacade;

    @PostMapping("/add")
    @Operation(summary = "Add available time to schedule and search for appointments")
    public void addAvailableTime(
            @Validated @RequestBody AvailableTimeModifyDto request,
            Principal principal
    ) {
        log.info("POST /available-time/add request from {} with body: {}", principal.getName(), request);
        availableTimeFacade.addAvailableTime(request, principal);
    }

    @PutMapping("/{id}/change")
    @Operation(summary = "Change available time and search for appointments")
    public void changeSchedule(
            @PathVariable UUID id,
            @Validated @RequestBody AvailableTimeModifyDto request,
            Principal principal
    ) {
        log.info("PUT /available-time/{}/change from user {} with body: {}", id, principal.getName(), request);
        availableTimeFacade.changeExistingAvailableTime(id, request, principal);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete available time by id")
    public void deleteAvailableTime(
            @PathVariable UUID id,
            Principal principal
    ) {
        log.info("DELETE /available-time/{} request from user {}", id, principal.getName());
        availableTimeFacade.deleteAvailableTime(id, principal);
    }
}
