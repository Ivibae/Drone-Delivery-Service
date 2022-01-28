package uk.ac.ed.inf;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This is the main class of the java application.
 */
public class App
{

    /** String representing the port where the database is running */
    private static String databasePort = "1527";

    /** String representing the port where the web server is running */
    private static String webServerPort = "80";

    /** String representing the machine name */
    private static final String machineName  = "localhost";

    /** String representing the machine name */
    private static final String DATE_SEPARATOR = "-";


    /**
     * Main method that takes as user inputs 5 different values: the day, month, and the year, the web server port
     * number and the database port number, and outputs a .geojson file detailing the deliveries of the drone for that
     * given day and two databases logging the relevant information about the drone’s path.
     * @param args the arguments given by the user, should be 5 integers representing the day, month, and the year, the
     *             web server port number and the database port number
     * @throws IOException if an I/O exception occurs
     * @throws InterruptedException if the process was interrupted.
     */
    public static void main(String[] args ) throws IOException, InterruptedException {


        String day = args[0];
        String month = args[1];
        String year = args[2];
        String webServerPort = args[3];
        String databasePort = args[4];

        setDatabasePort(databasePort);
        setWebServerPort(webServerPort);

        String date = buildDate(day, month, year);

        Database database = new Database();
        ArrayList<OrderDetails> listOrderDetails = database.getOrderDetails(date);
        OrderDetails.setOrderDetailsFields(listOrderDetails);


        NoFlyZones noFlyZones = HTTPClient.getNoFlyZones();
        ArrayList<ArrayList<LongLat>> noFlyZonesPoints = noFlyZones.getNoFlyZonesPoints();

        Drone drone = new Drone(listOrderDetails, noFlyZonesPoints);


        System.out.println("The sample monetary value is:");
        System.out.println(drone.getPercentageMonetaryValue());


        OutputFiles.writeGeoJSONFile(day, month, year, drone);
        Database.writeDatabaseTableDeliveries(drone.getOrderDetailsToDo());
        Database.writeDatabaseTableFlightpath(drone.getRoute(), drone.getOrderNumbers(), drone.getAngles());





    }



    /**
     * Helper method that given the day, the month and the year, builds the date in format YYYY-MM-DD.
     * @param day the day of the date.
     * @param month the month of the date.
     * @param year the year of the date.
     * @return the date as a String in format YYYY-MM-DD.
     */
    private static String buildDate(String day, String month, String year){
        return year + DATE_SEPARATOR + month + DATE_SEPARATOR + day;
    }


    public static void setDatabasePort(String databasePort) {
        App.databasePort = databasePort;
    }

    public static void setWebServerPort(String webServerPort) {
        App.webServerPort = webServerPort;
    }

    public static String getDatabasePort() {
        return databasePort;
    }

    public static String getWebServerPort() {
        return webServerPort;
    }

    public static String getMachineName() {
        return machineName;
    }
}
