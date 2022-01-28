package uk.ac.ed.inf;

/**
 * Class that represents an item from the menu of a restaurant.
 */
public class MenuItem {

    /** String representing the name of the item of the menu.*/
    private final String item;

    /** Integer representing the price in pence of the item.*/
    private final int pence;


    /**
     * Constructor for the MenuItem class
     * @param item the name of the item in the menu
     * @param pence the price in pence of the item
     */
    public MenuItem(String item, int pence) {
        this.item = item;
        this.pence = pence;
    }


    public String getItem() {
        return item;
    }


    public int getPence() {
        return pence;
    }

}
