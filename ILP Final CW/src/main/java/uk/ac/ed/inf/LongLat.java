package uk.ac.ed.inf;

import com.google.gson.annotations.SerializedName;
import com.mapbox.geojson.Point;


import java.util.ArrayList;

import static java.lang.Math.*;


/**
 * Class that represents the location of a point using its longitude and latitude.
 */
public class LongLat {

    /** Double representing the longitude of the location of the point*/
    @SerializedName("lng")
    public double longitude;

    /** Double representing the latitude of the location of the point*/
    @SerializedName("lat")
    public double latitude;

    /** Double representing the length in degrees of the distance we fly in a move*/
    private static final double MOVE_DISTANCE = 0.00015;

    /**
     * Double representing the distance in degrees two points have to be from one another in order for them to be defined
     * as being close.
     */
    private static final double DISTANCE_TOLERANCE = 0.00015;

    /** Integer representing the angle which indicates that the drone is hovering*/
    private static final int HOVERING_ANGLE = -999;


    /** Double representing the northern limit of the confinement area*/
    private static final double NORTHERN_LATITUDE_CONFINEMENT_LIMIT = 55.946233;

    /** Double representing the southern limit of the confinement area*/
    private static final double SOUTHERN_LATITUDE_CONFINEMENT_LIMIT = 55.942617;

    /** Double representing the western limit of the confinement area*/
    private static final double WESTERN_LONGITUDE_CONFINEMENT_LIMIT = -3.192473;

    /** Double representing the eastern limit of the confinement area*/
    private static final double EASTERN_LONGITUDE_CONFINEMENT_LIMIT = -3.184319;




    /**
     * Constructor of the LongLat class.
     * @param longitude double indicating the longitude of the location of the point.
     * @param latitude double indicating the latitude of the location of the point.
     */
    public LongLat(double longitude, double latitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }



    /**
     * Method that returns true if the drone is within the drone confinement area and false if it is not.
     * @return True if the longitude and latitude are within the required parameters, false otherwise.
     */
    private boolean isConfined(){
        if (longitude > WESTERN_LONGITUDE_CONFINEMENT_LIMIT && longitude < EASTERN_LONGITUDE_CONFINEMENT_LIMIT){
            if (latitude > SOUTHERN_LATITUDE_CONFINEMENT_LIMIT && latitude < NORTHERN_LATITUDE_CONFINEMENT_LIMIT){
                return true;
            }
        }
        return false;
    }



    /**
     * Method that returns true if the next movement is within the drone confinement area and does not intersect with
     * any of the no-fly zones, and false if it is not.
     * @param nextPosition the next position
     * @param noFlyZonesPoints the no fly zones
     * @return true if the next movement is within the drone confinement area and does not intersect with any of the
     * no-fly zones, false otherwise.
     */
    public boolean isValidMovement(LongLat nextPosition, ArrayList<ArrayList<LongLat>> noFlyZonesPoints){
        if (!nextPosition.isConfined()){
            return false;
        }
        for (ArrayList<LongLat> longLatArrayList : noFlyZonesPoints){
            for (int i = 0; i < longLatArrayList.size(); i++) {
                int nextIndex;
                if(i == longLatArrayList.size()-1){
                    nextIndex = 0;
                } else{
                    nextIndex = i+1;
                }
                if(intersectsWith(new LongLat(getLongitude(), getLatitude()), nextPosition, longLatArrayList.get(i), longLatArrayList.get(nextIndex))){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Calculates the pythagorean distance from our current position to the new location.
     * @param newLocation LongLat object from which we calculate the distance from.
     * @return the Pythagorean distance in degrees from our current position to the new location.
     */
    public double distanceTo (LongLat newLocation){
        return sqrt(Math.pow(newLocation.latitude - latitude, 2) + Math.pow(newLocation.longitude - longitude, 2));
    }


    /**
     * Method that returns true if the given LongLat object is within the {@value DISTANCE_TOLERANCE} degrees of
     * tolerance to the new location, false otherwise.
     * @param newLocation LongLat object detailing the location of the new point.
     * @return true if the drone is within {@value DISTANCE_TOLERANCE} degrees of destination, false otherwise.
     */
    public boolean closeTo (LongLat newLocation){
        return (distanceTo(newLocation) < DISTANCE_TOLERANCE);
    }


    /**
     * Method that, given an angle, returns a LongLat object representing the position of the drone if it were to make
     * a move in the direction of the given angle.
     * @param angle Integer representing the angle of the movement. It should be a multiple of 10. If the angle is {@value HOVERING_ANGLE}
     *              then the drone is hovering and thus not moving.
     * @return a LongLat object representing the new location of the drone if it were to move in the direction of the
     * given angle.
     */
    public LongLat nextPosition (int angle){
        LongLat originalLongLat = new LongLat(longitude, latitude);

        if (angle == HOVERING_ANGLE){
            return originalLongLat;
        }

        return new LongLat(longitude + MOVE_DISTANCE * cos(toRadians(angle)),
                latitude + MOVE_DISTANCE * sin(toRadians(angle)));
    }


    /**
     * Given the destination in LongLat form, this method calculates the angle necessary to go from the current location
     * of the LongLat to the given destination. This angle is returned as the nearest multiple of 10 of the calculated
     * angle. It is important to note that we use the convention that 0 means go East, 90 means go North, 180 means go
     * West, and 270 means go South, with the other multiples of ten between 0 and 350 representing the
     * obvious directions between these four major compass directions.
     * @param destination The destination in LongLat form representing the point we want to go to.
     * @return the angle necessary to reach the given destination, rounded to the nearest multiple of 10.
     */
    public int nextAngle(LongLat destination) {
        var x = destination.getLongitude() - this.longitude;
        var y = destination.getLatitude() - this.latitude;
        var angle = Math.toDegrees(Math.atan(y / x));
        double angleFromEast = getAngleFromEast(x, y, angle);
        return getRoundedAngle(angleFromEast);
    }



    /**
     * Helper method for the {@link #nextAngle(LongLat)} method that, given the distance in x, the distance in y, and
     * the angle, calculates the angle anti-clockwise, where east is equal to 0 degrees.
     * @param x The difference in longitude between the destination and the current longitude
     * @param y The difference in latitude between the destination and the current longitude
     * @param angle The calculated angle
     * @return the angle anti-clockwise, where east is equal to 0 degrees.
     */
    private double getAngleFromEast(double x, double y, double angle) {
        var angleFromEast = 0.0;
        if (x > 0 && y > 0) {
            angleFromEast = angle;
        } else if (x < 0 && y > 0) {
            angleFromEast = 180 - Math.abs(angle);
        } else if (x < 0 && y < 0) {
            angleFromEast = 180 + angle;
        } else if (x > 0 && y < 0) {
            angleFromEast = 360 - (Math.abs(angle));
        }
        return angleFromEast;
    }

    /**
     * Helper method that, given the angle, it rounds the result up or down to the nearest multiple of 10
     * @param angle angle that we want to round up
     * @return the angle rounded up or down to the nearest multiple of 10.
     */
    private int getRoundedAngle(double angle) {
        var angleRoundedDown = (int) (angle - angle % 10);
        var angleRoundedUp = (int) ((10 - angle % 10) + angle);
        if ((angleRoundedUp - angle) < (angle - angleRoundedDown)) {
            if (angleRoundedUp == 360){
                return 0;
            }
            return angleRoundedUp;
        } else {
            return angleRoundedDown;
        }
    }


    /**
     * Method that checks whether a path from the line between the points originLine1 and endLine1 intersects with the
     * line containing the points originLine2 and endLine2.
     * @param originLine1 The origin point of the first line
     * @param endLine1 The end point of the first line
     * @param originLine2 The origin point of the second line
     * @param endLine2 The end point of the second line
     * @return True if the lines they represent intersect, false otherwise.
     */
    public boolean intersectsWith(LongLat originLine1, LongLat endLine1, LongLat originLine2, LongLat endLine2){
        double originLine1Long = originLine1.getLongitude();
        double originLine1Lat = originLine1.getLatitude();

        double endLine1Long = endLine1.getLongitude();
        double endLine1Lat = endLine1.getLatitude();

        double originLine2Long = originLine2.getLongitude();
        double originLine2Lat = originLine2.getLatitude();

        double endLine2Long = endLine2.getLongitude();
        double endLine2Lat = endLine2.getLatitude();


        // If there is no overlap in the longitude values of both lines, then both lines do not intersect
        if ( Math.max(originLine1Long, endLine1Long) < Math.min(originLine2Long, endLine2Long)) {
            return false;
        }

        // We construct two infinite y = mx + c lines
        else {
            if (originLine2Long == endLine2Long) {
                double m1 = (originLine1Lat - endLine1Lat) / (originLine1Long - endLine1Long);
                double c1 = originLine1Lat - (m1 * originLine1Long);

                if ((m1 * originLine1Long + c1) > Math.min(originLine2Lat, endLine2Lat) &&
                        (m1 * originLine1Long + c1) < Math.max(originLine2Lat, endLine2Lat)) {
                    return true;
                }
                else {
                    return false;
                }
            }

            if (originLine1Long == endLine1Long) {

                double m2 = (originLine2Lat - endLine2Lat) / (originLine2Long - endLine2Long);
                double c2 = originLine2Lat - (m2 * originLine2Long);

                if ((m2 * originLine1Long + c2) >= Math.min(originLine1Lat, endLine1Lat) &&
                        (m2 * originLine1Long + c2) <= Math.max(originLine1Lat, endLine1Lat)) {
                    return true;
                }
                else {
                    return false;
                }
            }

            double m1 = (originLine1Lat - endLine1Lat) / ( originLine1Long - endLine1Long);
            double m2 = (originLine2Lat - endLine2Lat) / (originLine2Long - endLine2Long);

            double c1 = originLine1Lat - (m1 * originLine1Long);
            double c2 = originLine2Lat - (m2 * originLine2Long);

            // If both lines have the same slope and different longitude, they don't intersect
            if (m1 == m2 && originLine1Long != originLine2Long) {
                return false;
            }

            // We check if the point (x, y) lies on both lines.
            double x = (c2 - c1) / (m1 - m2);
            double y1 = (m1 * x) + c1;
            double y2 = (m2 * x) + c2;
            if (Math.abs(y2 - y1) < 1e-10) {
                if ( x < Math.max(Math.min(originLine1Long, endLine1Long), Math.min(originLine2Long, endLine2Long))
                        || x > Math.min(Math.max(originLine1Long, endLine1Long), Math.max(originLine2Long, endLine2Long))) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper method that transforms lists of mapbox.geojson.Point into lists of LongLat.
     * @param landmarksPointsArray ArrayList of mapbox.geojson.Point representing the landmarks
     * @return the LongLat locations of the landmarks.
     */
    public static ArrayList<LongLat> translateLandmarksToLongLat(ArrayList<Point> landmarksPointsArray) {
        ArrayList<LongLat> landmarksLongLatArray= new ArrayList<>();
        for(Point point: landmarksPointsArray){
            var coordinates = point.coordinates();
            LongLat pointCoordinates = new LongLat(coordinates.get(0), coordinates.get(1));
            landmarksLongLatArray.add(pointCoordinates);
        }
        return landmarksLongLatArray;
    }



    @Override
    public String toString() {
        return "LongLat{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}
