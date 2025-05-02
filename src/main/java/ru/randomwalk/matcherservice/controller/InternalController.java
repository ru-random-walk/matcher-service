package ru.randomwalk.matcherservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.randomwalk.matcherservice.model.dto.AppointmentDetailsDto;
import ru.randomwalk.matcherservice.model.dto.RequestForAppointmentDto;
import ru.randomwalk.matcherservice.service.facade.InternalFacade;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MATCHER_API_CLIENT_SCOPE')")
@RequestMapping("/internal")
@Slf4j
public class InternalController {

    private final InternalFacade facade;

    @PostMapping("/appointment/request")
    @Operation(description = "Create request for appointment")
    public AppointmentDetailsDto requestForAppointment(@RequestBody RequestForAppointmentDto dto) {
        log.info("Got request for appointment: {}", dto);
        return facade.requestForAppointment(dto);
    }
}
