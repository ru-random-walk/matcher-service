package ru.randomwalk.matcherservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "APPOINTMENT_DETAILS")
public class AppointmentDetails {
    @Id
    private UUID id;

    @Column(name = "CREATED_AT")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "ENDED_AT")
    private LocalDateTime endedAt;

    @Column(name = "STARTS_AT")
    private LocalDateTime startsAt;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
}
