package ru.randomwalk.matcherservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.randomwalk.matcherservice.model.dto.AppointmentDetailsDto;
import ru.randomwalk.matcherservice.service.facade.AppointmentFacade;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/appointment")
@PreAuthorize("hasAuthority('DEFAULT_USER')")
public class AppointmentController {

    private final AppointmentFacade appointmentFacade;

    @DeleteMapping("/{appointmentId}/cancel")
    @Operation(summary = "Cancel an appointed walk by appointmentId")
    public void cancelAppointment(
            @PathVariable UUID appointmentId,
            Principal principal
    ) {
        log.info("DELETE /appointment/{}/cancel from user {}", appointmentId, principal.getName());
        appointmentFacade.cancelAppointment(appointmentId, principal.getName());
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "Get appointment details by id")
    public AppointmentDetailsDto getAppointment(@PathVariable UUID appointmentId, Principal principal) {
        log.info("GET /appointment/{} from user {}", appointmentId, principal.getName());
        return appointmentFacade.getAppointmentById(appointmentId, principal.getName());
    }

    @PutMapping("/{appointmentId}/approve")
    @Operation(summary = "Approve requested appointment")
    public void approveAppointment(@PathVariable UUID appointmentId, Principal principal) {
        log.info("PUT /appointment/{}/approve from user {}", appointmentId, principal.getName());
        appointmentFacade.approveAppointment(appointmentId, principal.getName());
    }

    @PutMapping("/{appointmentId}/reject")
    @Operation(summary = "Reject requested appointment")
    public void rejectAppointment(@PathVariable UUID appointmentId, Principal principal) {
        log.info("PUT /appointment/{}/reject from user {}", appointmentId, principal.getName());
        appointmentFacade.rejectAppointment(appointmentId, principal.getName());
    }
}
