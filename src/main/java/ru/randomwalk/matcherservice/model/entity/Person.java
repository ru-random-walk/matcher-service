package ru.randomwalk.matcherservice.model.entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.randomwalk.matcherservice.model.enam.FilterType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "AGE")
    private Integer age;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOCATION_ID")
    private Location location;

    @Column(name = "SEARCH_AREA_METERS")
    private Integer searchAreaInMeters;

    @Column(name = "GROUP_FILTER_TYPE")
    @Enumerated(EnumType.STRING)
    private FilterType groupFilterType;

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

}
