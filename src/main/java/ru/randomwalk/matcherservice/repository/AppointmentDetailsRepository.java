package ru.randomwalk.matcherservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.projection.AppointmentPartner;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

    @Query(value = """
        select ad.* from appointment_details as ad
        inner join matcher.appointment a on ad.id = a.appointment_id
        where ad.status::text not in :statusesToExclude
        and date(ad.starts_at at time zone 'UTC') >= :afterDate
        and a.person_id = :personId
    """, nativeQuery = true)
    List<AppointmentDetails> getAllAppointmentsForPersonThatStartsAfterDateAndNotInStatuses(UUID personId, LocalDate afterDate, List<String> statusesToExclude);
}
