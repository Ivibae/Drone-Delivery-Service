package uk.ac.ed.inf;

import java.util.ArrayList;


/**
 * Class representing an object of type OrderDetails, used to store the relevant information for a particular order.
 */
public class OrderDetails {

    /** String representing the order number of the order */
    private final String orderNo;

    /** String representing the customer student ID number for whom we deliver the order */
    private final String customer;

    /** String representing the What3Words location for where we need to deliver the order */
    private final String deliverTo;

    /** ArrayList of Strings representing the What3Words locations of the restaurants from where we pick up the items
     * of the order */
    private ArrayList<String> deliverFrom;

    /** ArrayList of Strings representing the items of the order to be delivered */
    private final ArrayList<String> items;

    /** Integer representing the total price in pence of the order, including the 50 pence delivery charge */
    private Integer price;

    /** Object of type LongLat representing the location for where we need to deliver the order */
    private LongLat deliverToLongLat;

    /** ArrayList of objects of type LongLat, representing the locations of the restaurants from where we pick up the
     * items of the order */
    private ArrayList<LongLat> deliverFromLongLat;

    /**
     * Constructor of the OrderDetails class.
     * @param orderNo the order number
     * @param customer the customer student ID number for whom we deliver the order
     * @param deliverTo What3Words location for where we need to deliver the order
     * @param items items of the order to be delivered
     */
    public OrderDetails(String orderNo, String customer, String deliverTo, ArrayList<String> items) {
        this.orderNo = orderNo;
        this.customer = customer;
        this.deliverTo = deliverTo;
        this.items = items;
    }

    /**
     * Method that sets all the OrderDetails fields that were not set up by the constructor.
     * @param listOrderDetails list with all the orderDetails that we could do for a given date.
     */
    public static void setOrderDetailsFields(ArrayList<OrderDetails> listOrderDetails) {
        setPrices(listOrderDetails);
        setWhat3WordsLocations(listOrderDetails);
        setLongLatLocations(listOrderDetails);
    }

    /**
     * Method that sets the price field for every OrderDetails object in listOrderDetails.
     * @param listOrderDetails list with all the orderDetails that we could do for a given date.
     */
    private static void setPrices(ArrayList<OrderDetails> listOrderDetails){
        for (OrderDetails order : listOrderDetails){
            ArrayList<String> items = order.getItems();
            Menus menu = new Menus(App.getMachineName(), App.getWebServerPort());
            int price = menu.getDeliveryCost(items.toArray(new String[0]));
            order.setPrice(price);
        }
    }

    /**
     * Method that sets the deliverFrom field for every OrderDetails object in listOrderDetails.
     * @param listOrderDetails list with all the orderDetails that we could do for a given date.
     */
    private static void setWhat3WordsLocations(ArrayList<OrderDetails> listOrderDetails){
        for (OrderDetails order : listOrderDetails){
            ArrayList<String> items = order.getItems();
            Menus menu = new Menus(App.getMachineName(), App.getWebServerPort());

            ArrayList<String> restaurantLocationsList = new ArrayList<>();
            for (String item : items){
                String location = menu.getRestaurantLocationOfItem(item);
                if (!restaurantLocationsList.contains(location)){
                    restaurantLocationsList.add(location);
                }
            }
            order.setDeliverFrom(restaurantLocationsList);
        }
    }



    /**
     * Method that sets the both the deliverToCoordinates and the deliverFromCoordinates fields for every OrderDetails
     * object in listOrderDetails.
     * @param listOrderDetails list with all the orderDetails that we could do for a given date.
     */
    private static void setLongLatLocations(ArrayList<OrderDetails> listOrderDetails){
        for (OrderDetails order : listOrderDetails){
            //We first transform the deliverTo of the OrderDetails from What3Words to LongLat
            String deliverTo = order.getDeliverTo();
            // We set the deliverToCoordinates
            LongLat deliverToLongLat = HTTPClient.translateLocation(deliverTo);
            order.setDeliverToLongLat(deliverToLongLat);


            //We then transform the deliverFrom coordinates of the OrderDetails from What3Words to LongLat
            ArrayList<LongLat> deliverFromLongLat = new ArrayList<>();
            for (String deliverFrom : order.getDeliverFrom()){
                LongLat deliverFromCoordinates = HTTPClient.translateLocation(deliverFrom);
                deliverFromLongLat.add(deliverFromCoordinates);

            }
            order.setDeliverFromLongLat(deliverFromLongLat);
        }
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getCustomer() {
        return customer;
    }

    public String getDeliverTo() {
        return deliverTo;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public Integer getPrice() {
        return price;
    }

    public ArrayList<String> getDeliverFrom() {
        return deliverFrom;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public void setDeliverFrom(ArrayList<String> deliverFrom) {
        this.deliverFrom = deliverFrom;
    }

    public LongLat getDeliverToLongLat() {
        return deliverToLongLat;
    }

    public void setDeliverToLongLat(LongLat deliverToLongLat) {
        this.deliverToLongLat = deliverToLongLat;
    }

    public ArrayList<LongLat> getDeliverFromLongLat() {
        return deliverFromLongLat;
    }

    public void setDeliverFromLongLat(ArrayList<LongLat> deliverFromLongLat) {
        this.deliverFromLongLat = deliverFromLongLat;
    }



}
