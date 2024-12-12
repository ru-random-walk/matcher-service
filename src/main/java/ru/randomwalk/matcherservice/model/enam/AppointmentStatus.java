package ru.randomwalk.matcherservice.model.enam;

import lombok.Getter;

@Getter
public enum AppointmentStatus {
    REQUESTED(true),
    APPOINTED(true),
    DONE(false),
    CANCELED(false);

    private final boolean isActive;

    AppointmentStatus(boolean isActive) {
        this.isActive = isActive;
    }
}
