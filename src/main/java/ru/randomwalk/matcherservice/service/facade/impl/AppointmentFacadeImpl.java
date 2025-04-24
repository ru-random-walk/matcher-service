package ru.randomwalk.matcherservice.service.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.model.dto.AppointmentDetailsDto;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.exception.MatcherForbiddenException;
import ru.randomwalk.matcherservice.service.AppointmentDetailsService;
import ru.randomwalk.matcherservice.service.facade.AppointmentFacade;
import ru.randomwalk.matcherservice.service.mapper.AppointmentMapper;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentFacadeImpl implements AppointmentFacade {

    private final AppointmentDetailsService appointmentDetailsService;
    private final AppointmentMapper appointmentMapper;

    @Override
    public AppointmentDetailsDto getAppointmentById(UUID appointmentId, String userName) {
        UUID personId = UUID.fromString(userName);
        List<UUID> appointmentParticipantIds = appointmentDetailsService.getAppointmentParticipants(appointmentId);

        if (!appointmentParticipantIds.contains(personId)) {
            throw new MatcherForbiddenException("Access is forbidden");
        }
        AppointmentDetails appointmentDetails = appointmentDetailsService.getById(appointmentId);

        return appointmentMapper.toDto(appointmentDetails, appointmentParticipantIds);
    }

    @Override
    public void cancelAppointment(UUID appointmentId, String userName) {
        UUID personId = UUID.fromString(userName);
        List<UUID> appointmentParticipantIds = appointmentDetailsService.getAppointmentParticipants(appointmentId);

        if (!appointmentParticipantIds.contains(personId)) {
            throw new MatcherForbiddenException();
        }

        appointmentDetailsService.cancelAppointmentByPerson(appointmentId, personId);
    }

    @Override
    @Transactional
    public void approveAppointment(UUID appointmentId, String userName) {
        UUID personId = UUID.fromString(userName);
        var appointment = appointmentDetailsService.getById(appointmentId);

        checkPersonHasRightsToApproveAppointment(appointment, personId);

        appointmentDetailsService.approveRequestedAppointment(appointment);
    }

    @Override
    @Transactional
    public void rejectAppointment(UUID appointmentId, String userName) {
        UUID personId = UUID.fromString(userName);
        var appointment = appointmentDetailsService.getById(appointmentId);

        checkPersonHasRightsToApproveAppointment(appointment, personId);

        appointmentDetailsService.rejectRequestedAppointment(appointment);
    }

    private void checkPersonHasRightsToApproveAppointment(AppointmentDetails appointment, UUID personId) {
        boolean isMember = appointment.getMembers().stream()
                .anyMatch(person -> Objects.equals(person.getId(), personId));
        boolean isNotRequester = appointment.getRequesterId() != personId;

        if (!isMember || isNotRequester) {
            throw new MatcherForbiddenException("You cannot approve this appointment");
        }
    }

}
