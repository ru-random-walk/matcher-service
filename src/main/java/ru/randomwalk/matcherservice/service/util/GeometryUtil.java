package ru.randomwalk.matcherservice.service.util;

import lombok.experimental.UtilityClass;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

@UtilityClass
public class GeometryUtil {

    private static final int SRID = 4326;
    private final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID);

    public Point createPoint(double longitude, double latitude) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    public void setPointLongitude(double longitude, Point point) {
        point.getCoordinate().setX(longitude);
    }

    public void setPointLatitude(double latitude, Point point) {
        point.getCoordinate().setY(latitude);
    }

    public double getLatitude(Point point) {
        return point.getCoordinate().getY();
    }

    public double getLongitude(Point point) {
        return point.getCoordinate().getX();
    }
}
