package ru.randomwalk.matcherservice.service;

import org.locationtech.jts.geom.Point;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.projection.AppointmentPartner;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentDetailsService {
    AppointmentDetails createAppointment(UUID personId, UUID partnerId, OffsetDateTime startsAt, Point approximateLocation);

    AppointmentDetails requestForAppointment(UUID requesterId, UUID partnerId, OffsetDateTime startAt, Point location);

    List<AppointmentPartner> getAllPartnerIdsForPersonAppointments(UUID personId, List<AppointmentDetails> appointments);

    List<UUID> getAppointmentParticipants(UUID appointmentId);

    AppointmentDetails getById(UUID appointmentId);

    void cancelAppointmentByPerson(UUID appointmentId, UUID initiatorId);

    void changeStatus(AppointmentDetails appointment, AppointmentStatus toStatus);

    void changeStatus(UUID appointmentId, AppointmentStatus toStatus);

    List<AppointmentDetails> getAllNotPastAppointmentsForPersonSchedule(UUID personId);
}
