package ru.randomwalk.matcherservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import org.locationtech.jts.geom.Point;
import ru.randomwalk.matcherservice.service.util.GeometryUtil;


@Embeddable
@Data
public class Location {
    @Column(name = "LOCATION")
    private Point point;

    @Column(name = "CITY")
    private String city;

    @Column(name = "STREET")
    private String street;

    @Column(name = "BUILDING")
    private String building;

    public double getLongitude() {
        return GeometryUtil.getLongitude(point);
    }

    public double getLatitude() {
        return GeometryUtil.getLatitude(point);
    }
}
