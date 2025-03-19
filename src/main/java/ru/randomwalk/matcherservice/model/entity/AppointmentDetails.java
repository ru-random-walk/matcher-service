package ru.randomwalk.matcherservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.locationtech.jts.geom.Point;
import ru.randomwalk.matcherservice.model.enam.AppointmentStatus;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "APPOINTMENT_DETAILS")
public class AppointmentDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "CREATED_AT")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "UPDATED_AT")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @Column(name = "ENDED_AT")
    private OffsetDateTime endedAt;

    @Column(name = "STARTS_AT")
    private OffsetDateTime startsAt;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AppointmentStatus status;

    @Column(name = "APPROXIMATE_LOCATION")
    private Point location;

    public LocalDate getStartDate() {
        return startsAt.toLocalDate();
    }

    public String getTimezone() {
        return startsAt.getOffset().toString();
    }

    public double getLongitude() {
        return GeometryUtil.getLongitude(location);
    }

    public double getLatitude() {
        return GeometryUtil.getLatitude(location);
    }
}
