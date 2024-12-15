package ru.randomwalk.matcherservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.TimeZoneColumn;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@ToString
@Table(name = "AVAILABLE_TIME")
public class AvailableTime {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "PERSON_ID")
    private UUID personId;

    @Column(name = "TIME_FROM", nullable = false)
    private OffsetTime timeFrom;

    @Column(name = "TIME_UNTIL", nullable = false)
    private OffsetTime timeUntil;

    @Column(name = "TIMEZONE")
    private String timezone;

    @Column(name = "DATE", nullable = false)
    private LocalDate date;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PERSON_ID", referencedColumnName = "PERSON_ID", insertable = false, updatable = false)
    @JoinColumn(name = "DATE", referencedColumnName = "DATE", insertable = false, updatable = false)
    private DayLimit dayLimit;

    public OffsetTime getTimeFrom() {
        return timeFrom.withOffsetSameInstant(ZoneOffset.of(timezone));
    }

    public OffsetTime getTimeUntil() {
        return timeUntil.withOffsetSameInstant(ZoneOffset.of(timezone));
    }

}
