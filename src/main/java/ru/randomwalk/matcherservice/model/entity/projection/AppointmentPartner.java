package ru.randomwalk.matcherservice.model.entity.projection;

import java.util.UUID;

public interface AppointmentPartner {
    UUID getPartnerId();
    UUID getAppointmentId();
}
