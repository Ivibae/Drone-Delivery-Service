package uk.ac.ed.inf;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Class responsible for handling the output files.
 */
public class OutputFiles {

    /**
     * String representing the prefix of the filename we want to save the GeoJSON file with
     */
    private final static String GEOJSON_FILE_PREFIX = "drone-";

    /**
     * String representing the suffix of the filename we want to save the GeoJSON file with
     */
    private final static String GEOJSON_FILE_SUFFIX = ".geojson";


    /**
     * Method that given the date of the flight, and the drone movements for that day, writes a GeoJSON file for the
     * movements of the drone for that given date.
     * @param day day of the date of the flight
     * @param month month of the date of the flight
     * @param year year of the date of the flight
     * @param drone object of type Drone that represents the drone and its movements for the given day
     * @throws IOException exception obtained when the GeoJson file could not be written correctly.
     */
    protected static void writeGeoJSONFile(String day, String month, String year, Drone drone) throws IOException {

        String filename = GEOJSON_FILE_PREFIX + day + "-" + month + "-" + year + GEOJSON_FILE_SUFFIX;
        FileWriter readings = new FileWriter(filename);

        readings.write(GeoJSON.translateRouteToGeoJSON(drone).toJson());
        readings.close();

    }




}
