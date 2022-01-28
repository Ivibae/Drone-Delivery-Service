package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing all the different no-fly zones where the drone can not go through
 */
public class NoFlyZones {

    /** ArrayList of type mapbox.geojson.Polygon representing the different no-fly zones in mapbox.geojson.Polygon type */
    private ArrayList<Polygon> noFlyZonesPolygons;

    /** ArrayList that contains ArrayLists of LongLats. It represents all the endpoints of the different straight lines representing
     *  the borders of all no-fly zones, and therefore every smaller sub-ArrayList represents all the endpoints of the lines
     *  representing the borders of a particular enclosed No-fly zone area */
    private ArrayList<ArrayList<LongLat>> noFlyZonesPoints;


    /**
     * Constructor of the NoFlyZones class
     * @param noFlyZonesPolygons an arrayList of type mapbox.geojson.Polygon representing the different no-fly zones
     *                           in mapbox.geojson.Polygon type
     */
    public NoFlyZones(ArrayList<Polygon> noFlyZonesPolygons) {
        this.noFlyZonesPolygons = noFlyZonesPolygons;
        setNoFlyZonesPoints(noFlyZonesPolygons);
    }


    /**
     * Getter of the {@link #noFlyZonesPoints} ArrayList
     * @return {@link #noFlyZonesPoints} representing the endpoints of the different straight lines representing
     * the borders of all the no-fly zones.
     */
    public ArrayList<ArrayList<LongLat>> getNoFlyZonesPoints() {
        return noFlyZonesPoints;
    }


    /**
     * Setter of the {@link #getNoFlyZonesPoints()}
     * @param noFlyZonesPolygons ArrayList of type mapbox.geojson.Polygon representing all the different no-fly zones in
     *                           mapbox.geojson.Polygon type
     */
    public void setNoFlyZonesPoints(ArrayList<Polygon> noFlyZonesPolygons) {
        ArrayList<ArrayList<LongLat>> noFlyZonesPointsArray = new ArrayList<>();
        for (Polygon polygon : noFlyZonesPolygons){
            ArrayList<LongLat> noFlyZonePolygonPoints = new ArrayList<>();
            for (List<Point> listOfPoints : polygon.coordinates()){
                for(Point point:listOfPoints){
                    var coordinates = point.coordinates();
                    LongLat pointCoordinates = new LongLat(coordinates.get(0), coordinates.get(1));
                    noFlyZonePolygonPoints.add(pointCoordinates);
                }
            }
            noFlyZonesPointsArray.add(noFlyZonePolygonPoints);
        }
        noFlyZonesPoints = noFlyZonesPointsArray;
    }



}
