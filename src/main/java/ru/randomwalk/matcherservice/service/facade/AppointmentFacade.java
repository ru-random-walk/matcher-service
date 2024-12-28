package ru.randomwalk.matcherservice.service.facade;

import ru.randomwalk.matcherservice.model.dto.AppointmentDetailsDto;

import java.util.UUID;

public interface AppointmentFacade {
    AppointmentDetailsDto getAppointmentById(UUID appointmentId, String userName);
    void deleteAppointment(UUID appointmentId, String userName);
}
