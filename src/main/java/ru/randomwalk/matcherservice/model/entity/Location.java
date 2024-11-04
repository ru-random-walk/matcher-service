package ru.randomwalk.matcherservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "Location")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "REGION")
    private String region;

    @Column(name = "CITY")
    private String city;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "STREET")
    private String street;

    @Column(name = "HOUSE")
    private String house;

    @Column(name = "POSITION")
    private Point position;

    public double getLongitude() {
        return GeometryUtil.getLongitude(position);
    }

    public double getLatitude() {
        return GeometryUtil.getLatitude(position);
    }

    public void setLongitude(double longitude) {
        GeometryUtil.setPointLongitude(longitude, position);
    }

    public void setLatitude(double latitude) {
        GeometryUtil.setPointLatitude(latitude, position);
    }
}
