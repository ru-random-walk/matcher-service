package ru.randomwalk.matcherservice.service.facade;

import ru.randomwalk.matcherservice.model.dto.AppointmentDetailsDto;
import ru.randomwalk.matcherservice.model.dto.RequestForAppointmentDto;

public interface InternalFacade {

    AppointmentDetailsDto requestForAppointment(RequestForAppointmentDto dto);
}
