package ru.randomwalk.matcherservice.model.entity;

import brave.internal.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import ru.randomwalk.matcherservice.config.converter.UUIDSetConverter;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;
import static ru.randomwalk.matcherservice.service.util.MatcherConstants.DEFAULT_SEARCH_AREA_IN_METERS;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @Column(name = "LOCATION")
    private Point location;

    @Column(name = "SEARCH_AREA_METERS")
    private Integer searchAreaInMeters = DEFAULT_SEARCH_AREA_IN_METERS;

    @Column(name = "CLUBS_IN_FILTER")
    @Convert(converter = UUIDSetConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<UUID> clubsInFilter = new HashSet<>();

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID", referencedColumnName = "PERSON_ID", insertable = false, updatable = false)
    @JoinColumn(name = "DATE", referencedColumnName = "DATE", insertable = false, updatable = false)
    private DayLimit dayLimit;

    public OffsetTime getTimeFrom() {
        return timeFrom.withOffsetSameInstant(ZoneOffset.of(timezone));
    }

    public OffsetTime getTimeUntil() {
        return timeUntil.withOffsetSameInstant(ZoneOffset.of(timezone));
    }

    public void setSearchAreaInMeters(@Nullable Integer searchAreaInMeters) {
        this.searchAreaInMeters = requireNonNullElse(searchAreaInMeters, DEFAULT_SEARCH_AREA_IN_METERS);
    }

    public double getLongitude() {
        return GeometryUtil.getLongitude(location);
    }

    public double getLatitude() {
        return GeometryUtil.getLatitude(location);
    }

}
