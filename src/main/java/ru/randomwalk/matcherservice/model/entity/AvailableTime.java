package ru.randomwalk.matcherservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "AVAILABLE_TIME")
public class AvailableTime {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "PERSON_ID")
    private UUID personId;

    @Column(name = "TIME_FROM", nullable = false)
    private Time timeFrom;

    @Column(name = "TIME_UNTIL", nullable = false)
    private Time timeUntil;

    @Column(name = "TIMEZONE", nullable = false)
    private Integer timezone;

    @Column(name = "DATE", nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "PERSON_ID", referencedColumnName = "PERSON_ID", insertable = false, updatable = false)
    @JoinColumn(name = "DATE", referencedColumnName = "DATE", insertable = false, updatable = false)
    private DayLimit dayLimit;
}
