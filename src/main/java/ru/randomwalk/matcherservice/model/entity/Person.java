package ru.randomwalk.matcherservice.model.entity;

import brave.internal.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import ru.randomwalk.matcherservice.model.enam.FilterType;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;
import static ru.randomwalk.matcherservice.service.MatcherConstants.PERSON_DEFAULT_SEARCH_AREA_IN_METERS;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "PERSON")
public class Person {
    @Id
    private UUID id;

    @Column(name = "FULL_NAME")
    private String fullName;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "AGE")
    private Integer age;

    @Column(name = "CURRENT_POSITION")
    private Point currentPosition;

    @Column(name = "SEARCH_AREA_METERS")
    private Integer searchAreaInMeters = PERSON_DEFAULT_SEARCH_AREA_IN_METERS;

    @Column(name = "GROUP_FILTER_TYPE")
    @Enumerated(EnumType.STRING)
    private FilterType groupFilterType;

    @Column(name = "IN_SEARCH")
    private boolean inSearch;

    @Column(name = "IN_SEARCH_FROM")
    private OffsetDateTime inSearchFrom;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID")
    private List<Club> clubs = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID")
    private List<AvailableTime> availableTimes = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "APPOINTMENT",
            joinColumns = {@JoinColumn(name = "PERSON_ID")},
            inverseJoinColumns = {@JoinColumn(name = "APPOINTMENT_ID")}
    )
    private List<AppointmentDetails> appointments = new ArrayList<>();

    public void setSearchAreaInMeters(@Nullable Integer searchAreaInMeters) {
        this.searchAreaInMeters = requireNonNullElse(searchAreaInMeters, PERSON_DEFAULT_SEARCH_AREA_IN_METERS);
    }

    public double getLongitude() {
        return GeometryUtil.getLongitude(currentPosition);
    }

    public double getLatitude() {
        return GeometryUtil.getLatitude(currentPosition);
    }

    public void setLongitude(double longitude) {
        GeometryUtil.setPointLongitude(longitude, currentPosition);
    }

    public void setLatitude(double latitude) {
        GeometryUtil.setPointLatitude(latitude, currentPosition);
    }

    public void setInSearch(boolean inSearch) {
        if (inSearch) {
            inSearchFrom = OffsetDateTime.now(ZoneOffset.UTC);
        } else {
            inSearchFrom = null;
        }
        this.inSearch = inSearch;
    }
}
