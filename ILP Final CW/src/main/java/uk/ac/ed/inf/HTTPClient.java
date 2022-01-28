package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;


import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


/**
 * Class that is responsible for performing web server operations.
 */
public class HTTPClient {


    /** Unique HttpClient that sends the requests and receives the responses from the web server.*/
    private static final HttpClient client = HttpClient.newHttpClient();

    /** String representing the Url prefix */
    private static final String URL_PREFIX = "http://";

    /** String representing the separators of the words Url */
    private static final String WORDS_URL_SEPARATOR = "/";

    /** String representing the what3Words folder of an url */
    private static final String WHAT3WORDS_URL_FOLDER = "/words/";

    /** String representing the buildings folder of an url */
    private static final String BUILDINGS_URL_FOLDER = "/buildings/";

    /** String representing the what3Words suffix of the url */
    private static final String WHAT3WORDS_URL_SUFFIX = "details.json";

    /** String representing the NoFlyZones suffix of the url */
    private static final String NOFLYZONES_URL_SUFFIX = "no-fly-zones.geojson";

    /** String representing the Landmarks suffix of the url */
    private static final String LANDMARKS_URL_SUFFIX = "landmarks.geojson";



    /**
     * Method that downloads the list of restaurants from the server.
     * @param menusURL string representing the URL of the webserver.
     * @return The list of downloaded restaurants .
     */
    public static List<Restaurant> getRestaurantRequest(String menusURL) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(menusURL)).build();

        List<Restaurant> restaurantList = null;

        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            //If the status code is correct.
            if (response.statusCode() == 200){

                String jsonString = response.body();

                Type listType = new TypeToken<List<Restaurant>>() {}.getType();

                restaurantList = new Gson().fromJson(jsonString, listType);
            }
            else {
                System.out.println("The data could not be obtained");
                System.exit(1);
            }
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return restaurantList;
    }


    /**
     * Method that obtains the noFlyZones as NoFlyZones objects from the web server by connecting to the
     * {@link #buildNoFlyZonesUrl()} url.
     * @return the noFlyZones as NoFlyZones objects.
     * @throws IOException if the noFlyZones could not be obtained.
     * @throws InterruptedException if the process was interrupted.
     */
    public static NoFlyZones getNoFlyZones() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(buildNoFlyZonesUrl())).build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {

                String noFlyZoneJsonString = response.body();
                ArrayList<Polygon> noFlyZonesArray = getNoFlyZonesFromJsonString(noFlyZoneJsonString);
                NoFlyZones noFlyZones = new NoFlyZones(noFlyZonesArray);
                return noFlyZones;
            }

        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;

    }


    /**
     * Helper method that constructs the noFlyZones Url
     * @return the noFlyZones Url
     */
    private static String buildNoFlyZonesUrl() {
        return URL_PREFIX + App.getMachineName() + ":" + App.getWebServerPort() + BUILDINGS_URL_FOLDER + NOFLYZONES_URL_SUFFIX;
    }


    /**
     * Helper method that returns the noFlyZones as an ArrayList of mapbox.geojson.Polygon objects by reading from the given
     * json Sting.
     * @param jsonString the json String from which we read the no-fly zones
     * @return the noFlyZones as an ArrayList of mapbox.geojson.Polygon objects
     */
    public static ArrayList<Polygon> getNoFlyZonesFromJsonString(String jsonString) {

        List<Feature> features = FeatureCollection.fromJson(jsonString).features();
        ArrayList<Polygon> noFlyZonesArrayList = new ArrayList<>();
        for (Feature feature : features) {
            if (feature.geometry().getClass().equals(Polygon.class)) {
                noFlyZonesArrayList.add((Polygon) feature.geometry());
            }
        }

        return noFlyZonesArrayList;
    }



    /**
     * Method that, given a location in form What3Words String, it transforms it into a LongLat Location by reading from
     * the words folder in the web server.
     * @param location the location in What3Words form.
     * @return the corresponding LongLat location
     */
    public static LongLat translateLocation(String location) {
        HttpClient client = HttpClient.newHttpClient();
        String[] words = location.split("\\.");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildWhat3WordsUrl(words))).build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode == 200) {
                What3Words word = new Gson().fromJson(response.body(), What3Words.class);
                LongLat coordinate = word.getCoordinates();
                return coordinate;
            } else if (statusCode == 404){
                System.err.println("Error 404: The server cannot find the requested resource");
            } else {
                System.err.println("The status code is " + statusCode);
            }
        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
        }
        return null;
    }


    /**
     * Helper method that builds the relevant What3Words Url for the given list of words.
     * @param words the list containing the 3 words forming the relevant What3Words location.
     * @return the URL to get the What3Words translation.
     */
    private static String buildWhat3WordsUrl(String[] words) {
        return  URL_PREFIX + App.getMachineName()+ ":" + App.getWebServerPort() + WHAT3WORDS_URL_FOLDER + words[0]
                + WORDS_URL_SEPARATOR + words[1] + WORDS_URL_SEPARATOR + words[2] + WORDS_URL_SEPARATOR + WHAT3WORDS_URL_SUFFIX;
    }


    /**
     * Method that obtains the landmarks as ArrayList of LongLat objects from the web server by connecting to the
     * {@link #buildLandmarksUrl()} url.
     * @return the landmarks as ArrayList of LongLat objects
     * @throws IOException if the landmarks could not be obtained.
     * @throws InterruptedException if the process was interrupted.
     */
    public static ArrayList<LongLat> getLandmarks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(buildLandmarksUrl())).build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {

                String landmarksJsonString = response.body();
                ArrayList<Point> landmarksPointsArray = getLandmarksFromJsonString(landmarksJsonString);
                return LongLat.translateLandmarksToLongLat(landmarksPointsArray);
            }

        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;

    }

    /**
     * Helper method that constructs the landmarks Url
     * @return the landmarks Url
     */
    private static String buildLandmarksUrl() {
        return URL_PREFIX + App.getMachineName() + ":" + App.getWebServerPort() + BUILDINGS_URL_FOLDER + LANDMARKS_URL_SUFFIX;
    }


    /**
     * Helper method of the {@link #getLandmarks()} method that returns the Landmarks as an ArrayList of
     * mapbox.geojson.Point objects by reading from the given json Sting.
     * @param jsonString the json String from which we read the landmarks
     * @return the landmarks as an ArrayList of mapbox.geojson.Point objects
     */
    public static ArrayList<Point> getLandmarksFromJsonString(String jsonString){

        List<Feature> features = FeatureCollection.fromJson(jsonString).features();
        ArrayList<Point> landmarksArray = new ArrayList<>();
        for (Feature feature : features) {
            if (feature.geometry().getClass().equals(Point.class)) {
                landmarksArray.add((Point) feature.geometry());
            }
        }

        return landmarksArray;

    }


}
