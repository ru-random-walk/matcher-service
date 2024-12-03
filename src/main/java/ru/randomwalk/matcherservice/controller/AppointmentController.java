package ru.randomwalk.matcherservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.randomwalk.matcherservice.model.dto.AppointmentDetailsDto;
import ru.randomwalk.matcherservice.model.dto.request.AppointmentRequestDto;
import ru.randomwalk.matcherservice.model.dto.response.AvailableTimeResponseDto;
import ru.randomwalk.matcherservice.service.facade.AppointmentFacade;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/appointment")
public class AppointmentController {

    private final AppointmentFacade appointmentFacade;

    @PostMapping("/add")
    @Operation(summary = "Add available time to schedule and search for appointment")
    public void createAppointment(
            @Validated @RequestBody AppointmentRequestDto request,
            Principal principal
    ) {
        log.info("POST /appointment/create request from {} with body: {}", principal.getName(), request);
        appointmentFacade.createAppointment(request, principal);
    }

    @DeleteMapping("/{appointmentId}/cancel")
    @Operation(summary = "Cancel an appointed walk by appointmentId")
    public List<AvailableTimeResponseDto> cancelAppointment(
            @PathVariable UUID appointmentId,
            Principal principal
    ) {
        log.info("DELETE /appointment/cancel for appointment {} from user {}", appointmentId, principal.getName());
        return Collections.emptyList();
    }

    @PutMapping("/available-time/change")
    @Operation(summary = "Change available time for appointments that are in search")
    public List<AvailableTimeResponseDto> changeSchedule(
            @Validated @RequestBody AppointmentRequestDto request,
            Principal principal
    ) {
        log.info("PUT /appointment/available-time/change from user {} with body: {}", principal.getName(), request);
        return Collections.emptyList();
    }

    @GetMapping("/available-time")
    @Operation(summary = "Get user's available time")
    public List<AvailableTimeResponseDto> getAvailableTime(
        Principal principal
    ) {
        log.info("GET /appointment/available-time from user {}", principal.getName());
        return Collections.emptyList();
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "Get appointment details by id")
    public AppointmentDetailsDto getAppointment(@PathVariable UUID appointmentId, Principal principal) {
        log.info("GET /appointment/{} from user {}", appointmentId, principal.getName());
        return null;
    }
}
