package ru.randomwalk.matcherservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.randomwalk.matcherservice.model.dto.AppointmentDetailsDto;
import ru.randomwalk.matcherservice.model.dto.response.UserScheduleDto;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/appointment")
public class AppointmentController {

    @DeleteMapping("/{appointmentId}/cancel")
    @Operation(summary = "Cancel an appointed walk by appointmentId")
    public List<UserScheduleDto> cancelAppointment(
            @PathVariable UUID appointmentId,
            Principal principal
    ) {
        log.info("DELETE /appointment/cancel for appointment {} from user {}", appointmentId, principal.getName());
        return Collections.emptyList();
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "Get appointment details by id")
    public AppointmentDetailsDto getAppointment(@PathVariable UUID appointmentId, Principal principal) {
        log.info("GET /appointment/{} from user {}", appointmentId, principal.getName());
        return null;
    }
}
