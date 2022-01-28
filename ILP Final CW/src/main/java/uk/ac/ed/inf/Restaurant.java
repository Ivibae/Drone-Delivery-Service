package uk.ac.ed.inf;

import java.util.HashMap;
import java.util.List;


/**
 * Class that represents a restaurant and that has parameters name, location and menu in order to match the three
 * fields for a restaurant of the JSON file menus.json.
 */
public class Restaurant {

    /** String representing the name of the restaurant */
    private final String name;

    /** String representing the location of the restaurant */
    private final String location;

    /** Menu of the restaurant which consists on a list of MenuItem objects */
    private final List<MenuItem> menu;


    /**
     * Constructor for the Restaurant class.
     * @param name name of the restaurant.
     * @param location location of the restaurant in What3Words form.
     * @param menu MenuItem object representing the menu of a restaurant.
     */
    public Restaurant(String name, String location, List<MenuItem> menu) {
        this.name = name;
        this.location = location;
        this.menu = menu;
    }


    /**
     * Getter of the location
     * @return the location of the restaurant
     */
    public String getLocation() {
        return location;
    }


    /**
     * Method that creates a Hashmap where the keys are the items from the menu of the restaurant and the values are
     * the corresponding prices of those menu items.
     * @return Hashmap where the keys are the items from the menu of the restaurant and the values are the corresponding
     * prices of those menu items.
     */
    public HashMap<String, Integer> getMenuHashmap(){
        HashMap<String, Integer> itemsPrice = new HashMap<String, Integer>();
        for (MenuItem item : menu){
            int price = item.getPence();
            itemsPrice.put(item.getItem(), price);
        }
        return itemsPrice;
    }



}
