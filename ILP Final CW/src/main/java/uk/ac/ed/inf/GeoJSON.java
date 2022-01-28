package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that is responsible for handling mapbox.geojson objects
 */
public class GeoJSON {

    /**
     * Given an object of type Drone, this method takes the route of type ArrayList of LongLat of the given drone and
     * returns the route of that drone as a mapbox.geojson featureCollection of LineStrings, by connecting with a
     * Linestring every two points of the route.
     * @param drone the object of type Drone for which we want the route as a featureCollection of LineStrings.
     * @return a mapbox.geojson featureCollection of LineStrings of the route of the drone.
     */
    public static FeatureCollection translateRouteToGeoJSON(Drone drone){
        ArrayList<LongLat> route = drone.getRoute();
        List<Point> points = new ArrayList<>();
        for (var longLatPoint : route){
            var point = Point.fromLngLat(longLatPoint.getLongitude(), longLatPoint.getLatitude());
            points.add(point);
        }
        LineString lines = LineString.fromLngLats(points);
        Geometry geometry = (Geometry)lines;
        Feature feature = Feature.fromGeometry(geometry);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        return featureCollection;
    }
}
