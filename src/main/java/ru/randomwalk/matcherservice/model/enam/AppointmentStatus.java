package ru.randomwalk.matcherservice.model.enam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum AppointmentStatus {
    REQUESTED(false, true),
    APPOINTED(true, true),
    IN_PROGRESS(true, true),
    DONE(false, false, true),
    CANCELED(false, false, true);

    private final boolean isActive;
    private final boolean showInSchedule;
    private boolean isTerminal = false;

    private static final Set<AppointmentStatus> FROM_REQUESTED_ALLOWED_TRANSITIONS = EnumSet.of(
            APPOINTED, CANCELED
    );

    private static final Set<AppointmentStatus> FROM_APPOINTED_ALLOWED_TRANSITIONS = EnumSet.of(
            IN_PROGRESS, DONE
    );

    private static final Set<AppointmentStatus> FROM_IN_PROGRESS_ALLOWED_TRANSITIONS = EnumSet.of(
            DONE
    );

    public boolean isAllowedTransition(AppointmentStatus toStatus) {
        return switch (this) {
            case REQUESTED -> FROM_REQUESTED_ALLOWED_TRANSITIONS.contains(toStatus);
            case APPOINTED -> FROM_APPOINTED_ALLOWED_TRANSITIONS.contains(toStatus);
            case IN_PROGRESS -> FROM_IN_PROGRESS_ALLOWED_TRANSITIONS.contains(toStatus);
            default -> false;
        };
    }
}
