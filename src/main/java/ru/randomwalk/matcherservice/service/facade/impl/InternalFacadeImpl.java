package ru.randomwalk.matcherservice.service.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.randomwalk.matcherservice.model.dto.AppointmentDetailsDto;
import ru.randomwalk.matcherservice.model.dto.RequestForAppointmentDto;
import ru.randomwalk.matcherservice.model.entity.Person;
import ru.randomwalk.matcherservice.service.AppointmentCreationService;
import ru.randomwalk.matcherservice.service.PersonService;
import ru.randomwalk.matcherservice.service.facade.InternalFacade;
import ru.randomwalk.matcherservice.service.mapper.AppointmentMapper;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;
import ru.randomwalk.matcherservice.service.validation.AppointmentValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalFacadeImpl implements InternalFacade {

    private final AppointmentCreationService appointmentCreationService;
    private final AppointmentValidator validator;
    private final PersonService personService;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional
    public AppointmentDetailsDto requestForAppointment(RequestForAppointmentDto dto) {
        Person requester = personService.findById(dto.requesterId());
        Person partner = personService.findById(dto.partnerId());
        validator.validateRequestForWalk(dto, requester, partner);

        var createdAppointment = appointmentCreationService.createRequestedAppointment(
                dto.requesterId(),
                dto.partnerId(),
                dto.startTime(),
                GeometryUtil.createPoint(dto.longitude(), dto.latitude())
        );

        return appointmentMapper.toDto(createdAppointment, List.of(requester.getId(), partner.getId()));
    }
}
