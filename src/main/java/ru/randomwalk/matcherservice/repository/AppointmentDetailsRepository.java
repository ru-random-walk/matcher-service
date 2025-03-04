package ru.randomwalk.matcherservice.repository;

import jakarta.persistence.QueryHint;
import org.hibernate.jpa.AvailableHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.projection.AppointmentPartner;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface AppointmentDetailsRepository extends JpaRepository<AppointmentDetails, UUID> {

    @Query(value = """
        select person_id as partnerId, appointment_id as appointmentId from appointment
        where appointment.person_id != :personId and appointment_id in :appointmentIds
        """, nativeQuery = true)
    List<AppointmentPartner> getAllPartnerIdsForAppointmentsOfPerson(@Param("personId") UUID personId, @Param("appointmentIds") List<UUID> appointmentIds);

    @Query(value = """
        select person_id from appointment where appointment_id = :appointmentId
        """, nativeQuery = true)
    List<UUID> getAppointmentPartnerIds(UUID appointmentId);

    @QueryHints(@QueryHint(name = AvailableHints.HINT_FETCH_SIZE, value = "50"))
    Stream<AppointmentDetails> getAllByStartsAtBeforeAndStatusNotIn(OffsetDateTime walkEndTime, List<AppointmentStatus> finalStatuses);
}
