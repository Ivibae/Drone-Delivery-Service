package uk.ac.ed.inf;

import java.util.*;

/**
 * Class that represents the restaurant menu.
 */
public class Menus {


    /** String representing the name of the machine where the web server is running */
    public final String MachineName;

    /** String representing the port where the server is running */
    public final String WebServerPort;

    /** Integer representing the standard delivery charge of 50 pence per delivery */
    private static final int DELIVERY_CHARGE = 50;

    /** String representing the location in the server of the restaurants data */
    private static final String MENUS_SERVER_LOCATION = "/menus/menus.json";

    /** List of objects of type Restaurant representing all available restaurants */
    private final List<Restaurant> availableRestaurants;


    /**
     * Class constructor of the class Menus.
     * @param MachineName The name of the machine where the web server is running.
     * @param WebServerPort The port where the web server is running.
     */
    public Menus(String MachineName, String WebServerPort){
        this.MachineName = MachineName;
        this.WebServerPort = WebServerPort;
        availableRestaurants = getRestaurants();

    }


    /**
     * Method that given a list of restaurants and an item, finds the price in pence of the item of one of the
     * restaurants.
     * @param restaurants list of all restaurants.
     * @param item the item for which we want to find the price
     * @return the price of the item in pence if found, or returns 0 otherwise.
     */
    public int getPriceOfItem (List<Restaurant> restaurants, String item){
        for (Restaurant restaurant : restaurants){
            if (restaurant.getMenuHashmap().containsKey(item)){
                return restaurant.getMenuHashmap().get(item);
            }
        }
        return 0;
    }


    /**
     * Method that constructs the name of the URL and returns a list with all the different objects of type
     * Restaurant.
     * @return the list of all the restaurants.
     */
    public List<Restaurant> getRestaurants() {

        String menusURL = "http://" + MachineName + ":" + WebServerPort + MENUS_SERVER_LOCATION;

        return HTTPClient.getRestaurantRequest(menusURL);

    }


    /**
     * Method that accepts a variable number of strings representing different items, and returns the
     * total price in pence of having those items delivered to you by drone, adding the {@value DELIVERY_CHARGE}
     * pence standard delivery charge.
     * @param items variable number of strings representing the items that are to be delivered.
     * @return the total cost in pence of having all given items delivered.
     */
    public int getDeliveryCost(String... items){

        int totalCost = DELIVERY_CHARGE;


        for (String item : items){
            totalCost += getPriceOfItem(availableRestaurants, item);
        }

        return totalCost;
    }


    /**
     * Method that, given an item, returns the location of the restaurant that serves that item.
     * @param item the item for which we want to find the corresponding restaurant location.
     * @return the location of the restaurant that serves that item
     */
    public String getRestaurantLocationOfItem(String item){
        for (Restaurant restaurant: availableRestaurants){
            if (restaurant.getMenuHashmap().containsKey(item)){
                return restaurant.getLocation();
            }
        }
        return null;
    }


}
