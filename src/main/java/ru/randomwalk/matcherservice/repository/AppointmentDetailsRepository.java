package ru.randomwalk.matcherservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.randomwalk.matcherservice.model.entity.AppointmentDetails;
import ru.randomwalk.matcherservice.model.entity.projection.AppointmentPartner;

import java.util.List;
import java.util.UUID;

public interface AppointmentDetailsRepository extends JpaRepository<AppointmentDetails, UUID> {

    @Query(value = """
        select person_id as partnerId, appointment_id as appointmentId from appointment
        where appointment.person_id != :personId and appointment_id in :appointmentIds
        """, nativeQuery = true)
    List<AppointmentPartner> getAllPartnerIdsForAppointmentsOfPerson(@Param("personId") UUID personId, @Param("appointmentIds") List<UUID> appointmentIds);
}
