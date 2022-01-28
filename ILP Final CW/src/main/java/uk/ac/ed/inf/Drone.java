package uk.ac.ed.inf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that represents a drone that, for a given list of orders to do and the no-fly zones, calculates the route
 * to perform as many orders as possible while avoiding the no-fly zones.
 */
public class Drone {


    /** Integer representing the maximum number of shops the drone can visit. */
    private static final int MAXIMUM_NUMBER_OF_SHOPS = 2;

    /** Integer representing the maximum number of moves the drone can perform. */
    private static final int MAXIMUM_NUMBER_OF_MOVES = 1500;

    /** LongLat representing the coordinates of Appleton Tower, from where the drone starts the route and where it should
     * return after performing the orders for the days */
    public static final LongLat APPLETON_TOWER_COORDINATES = new LongLat(-3.186874, 55.944494);

    /** Integer representing the angle which indicates that the drone is hovering*/
    private static final int HOVERING_ANGLE = -999;

    /** ArrayList of OrderDetails representing the orders that the algorithm in {@link #setOrderDetailsToDo(ArrayList)}
     * decides to do. */
    public ArrayList<OrderDetails> orderDetailsToDo;

    /** ArrayList of LongLat objects representing the coordinates we need to visit when doing the orders in
     * {@link #orderDetailsToDo}: the coordinates of the restaurants, and the coordinates where we need to deliver
     * the order to. */
    public ArrayList<LongLat> coordinatesToVisit;

    /** ArrayList of LongLat objects representing all of the coordinates the drone visits after making every move. */
    public ArrayList<LongLat> route;

    /** ArrayList of Integers of length {@link #totalNumberOfMovesUsed}, and where for the ith move the drone does,
     * the ith element of angles indicates the angle that the drone has travelled with for that movement. */
    public ArrayList<Integer> angles = new ArrayList<>();

    /** ArrayList of Strings of length {@link #totalNumberOfMovesUsed}, and where for the ith move the drone does,
     * the ith element of orderNumbers indicates the order number corresponding for that movement. */
    public ArrayList<String> orderNumbers = new ArrayList<>();

    /** ArrayList of LongLat representing all the landmarks given by us in the web server */
    private final ArrayList<LongLat> landmarks = HTTPClient.getLandmarks();

    /** Integer representing the total number of moves performed by the drone when doing the orders indicated in
     * {@link #orderDetailsToDo} */
    private Integer totalNumberOfMovesUsed = 0;

    /** ArrayList that contains ArrayLists of LongLats. It represents all the endpoints of the different straight lines
     * representing the borders of all no-fly zones, and therefore every smaller sub-ArrayList represents all the
     * endpoints of the lines representing the borders of a particular enclosed No-fly zone area */
    private final ArrayList<ArrayList<LongLat>> noFlyZonesPoints;

    /** Integer representing the total price that would be achieved if we were to do all of the orders that there are
     * in the database for the given date. */
    private Integer totalPrice = 0;

    /** Integer representing the total price that is actually achieved when we perform all the orders in
     * {@link #orderDetailsToDo}. */
    private Integer priceDone = 0;

    private double percentageMonetaryValue = (double) priceDone/totalPrice;


    /**
     * Constructor of the Drone class.
     * @param orderDetailsArrayList list of OrderDetails representing the orders that have been placed for the day.
     * @param noFlyZonesPoints ArrayList of ArrayList of LongLat representing the coordinates of the points forming the
     *                         boundaries of the no-fly zones.
     * @throws IOException if an I/O exception occurs
     * @throws InterruptedException if the thread is interrupted
     */
    public Drone(ArrayList<OrderDetails> orderDetailsArrayList, ArrayList<ArrayList<LongLat>> noFlyZonesPoints) throws IOException, InterruptedException {
        this.noFlyZonesPoints = noFlyZonesPoints;


        System.out.println("The total number of orders are:");
        System.out.println(orderDetailsArrayList.size());

        setTotalPrice(orderDetailsArrayList);


        setOrderDetailsToDo(orderDetailsArrayList);

        setPriceDone();
        setPercentageMonetaryValue();

        System.out.println("The total number of orders done are:");
        System.out.println(getOrderDetailsToDo().size());

        setCoordinatesToVisit(getOrderDetailsToDo());
        setRoute(getCoordinatesToVisit());

    }


    /**
     * Method that, given a list with all of the orderDetails that we could do for a given date, it chooses which orders
     * to perform before the drone runs out of moves {@link #MAXIMUM_NUMBER_OF_MOVES}. The method chosen to choose which
     * orders to perform is the following: in each iteration, we choose the method that would give us the maximum price
     * paid per move that we have to make. We include in the distance that the drone has to fly the distance to get to
     * the restaurant(s), as well as the distance to the location where we have to drop the items. That way, the order
     * will lean towards doing high-paying orders that are near where the drone is when finishing dropping off the
     * previous order, thus minimising lost moves. Finally, we also take into account whether we will be able to return
     * to Appleton Tower after completing the next order, and if we can not do it then we do not do that order. The
     * implementation works for any amount of landmarks, as if we would not be able to access a location, we would
     * not perform that order. Once we have decided which orders to do, we store them in the ArrayList of OrderDetails
     * {@link #orderDetailsToDo}. In this method, we also count the number of moves we use, storing them in
     * {@link #totalNumberOfMovesUsed}, as well as completing the {@link #orderNumbers} attribute.
     * @param orderDetailsArrayList list with all the orderDetails that we could do for a given date.
     */
    public void setOrderDetailsToDo(ArrayList<OrderDetails> orderDetailsArrayList) {
        ArrayList<OrderDetails> orderOfTheOrders = new ArrayList<>();
        ArrayList<OrderDetails> remainingOrders = orderDetailsArrayList;
        LongLat originalLocation = APPLETON_TOWER_COORDINATES;
        Integer totalNumberOfMovesUsed = 0;
        OrderDetails nextOrder = getNextOrder(originalLocation, remainingOrders );
        Integer movesAdded = 10000;
        if (nextOrder != null) {
            movesAdded = getNumberOfMoves(originalLocation, nextOrder, nextOrder.getDeliverFromLongLat());
        }
        while ((nextOrder != null) && canReturnToAppleton(movesAdded, nextOrder, totalNumberOfMovesUsed) && remainingOrders.size() > 1){
            orderOfTheOrders.add(nextOrder);
            remainingOrders.remove(nextOrder);
            totalNumberOfMovesUsed += movesAdded;
            setOrderNumbers(nextOrder.getOrderNo(), movesAdded);
            originalLocation = nextOrder.getDeliverToLongLat();
            nextOrder = getNextOrder(originalLocation, remainingOrders);
            if (nextOrder != null) {
                movesAdded = getNumberOfMoves(originalLocation, nextOrder, nextOrder.getDeliverFromLongLat());
            }

        }
        // If there is only one more movement left, and we can do it
        if((nextOrder != null) && canReturnToAppleton(movesAdded, nextOrder, totalNumberOfMovesUsed)) {
            orderOfTheOrders.add(nextOrder);
            remainingOrders.remove(nextOrder);
            totalNumberOfMovesUsed += movesAdded;
            originalLocation = nextOrder.getDeliverToLongLat();
            setOrderNumbers(nextOrder.getOrderNo(), movesAdded);
        }
        if((nextOrder == null)) {
            nextOrder = remainingOrders.get(0);
            if (canReturnToAppleton(movesAdded, nextOrder, totalNumberOfMovesUsed) && canPerformNextOrder(originalLocation, nextOrder)) {
                orderOfTheOrders.add(nextOrder);
                remainingOrders.remove(nextOrder);
                movesAdded = getNumberOfMoves(originalLocation, nextOrder, nextOrder.getDeliverFromLongLat());
                totalNumberOfMovesUsed += movesAdded;
                setOrderNumbers(nextOrder.getOrderNo(), movesAdded);

            }
        }
        //We check if the original location is still Appleton Tower, that is, it has not moved at all. If not then we proceed as normal
        if (originalLocation != APPLETON_TOWER_COORDINATES) {
            totalNumberOfMovesUsed += travelToDestination(originalLocation, APPLETON_TOWER_COORDINATES).size();
            setOrderNumbers("--------", travelToDestination(originalLocation, APPLETON_TOWER_COORDINATES).size());
        }

        this.totalNumberOfMovesUsed = totalNumberOfMovesUsed;
        orderDetailsToDo = orderOfTheOrders;
    }

    /**
     * Given the current location and the remaining orders, this method iterates through a Hashmap it creates to return
     * the next order that we should do, that is, the order has the highest the price per movement performed.
     * @param originalLocation the current location of the drone
     * @param remainingOrders the remaining orders that we can perform
     * @return the order that has the highest the price per movement performed from our current location.
     */
    private OrderDetails getNextOrder(LongLat originalLocation, ArrayList<OrderDetails> remainingOrders) {
        HashMap<OrderDetails, Double> pricePerMovementOfRemainingOrderDetails = new HashMap<OrderDetails, Double>();
        for (OrderDetails remainingOrder : remainingOrders){
            if (canPerformNextOrder(originalLocation, remainingOrder)){
                Integer price = remainingOrder.getPrice();
                var restaurants = remainingOrder.getDeliverFromLongLat();
                Integer numberOfMoves = getNumberOfMoves(originalLocation, remainingOrder, restaurants);
                Double pricePerMovement = (double) (price / numberOfMoves);
                pricePerMovementOfRemainingOrderDetails.put(remainingOrder, pricePerMovement);
            }

        }
        OrderDetails nextOrder = getMaximumValueOfHashmap(pricePerMovementOfRemainingOrderDetails);
        return nextOrder;
    }


    /**
     * Boolean that returns true if we can complete the next order and if then we would have enough moves left to return to
     * Appleton Tower, or returns false otherwise.
     * @param movesAdded Moves added by performing the next Order
     * @param nextOrder OrderDetails representing the next order to perform
     * @param totalNumberOfMovesUsed the total number of moves used in the day so far
     * @return True if we can do the next order and then return to Appleton, false otherwise
     */
    private boolean canReturnToAppleton(Integer movesAdded, OrderDetails nextOrder, Integer totalNumberOfMovesUsed){
        int totalMovesNeededToReturnToAppleton = movesAdded
                + travelToDestination(nextOrder.getDeliverToLongLat(), APPLETON_TOWER_COORDINATES).size();

        return totalNumberOfMovesUsed + totalMovesNeededToReturnToAppleton < MAXIMUM_NUMBER_OF_MOVES;
    }


    /**
     * Method that, given the current location the drone is in and the details of the next order, calculates if there is
     * a possible path connecting our original location, the restaurants of the next order and the coordinates where we
     * need to deliver to the next order. This method will always return true with the original values for the restaurants,
     * noFlyZones and landmarks, but will prevent the drone from trying to perform orders which it can not fly to if the
     * values for the restaurants, noFlyZones or landmarks change in such a way that some coordinates are unreachable.
     * @param originalLocation the current location the drone is in
     * @param nextOrder the details of the next order
     * @return true if it is possible to perform the next order from the current location by making legal moves, false
     * otherwise.
     */
    private boolean canPerformNextOrder(LongLat originalLocation, OrderDetails nextOrder){
        var restaurants = nextOrder.getDeliverFromLongLat();
        if (restaurants.size() == 1){
            try {
                var numberOfMoves = travelToDestination(originalLocation, restaurants.get(0)).size()
                        + travelToDestination(restaurants.get(0), nextOrder.getDeliverToLongLat()).size();
            }
            catch (NullPointerException e){
                return false;
            }

        }
        else if (restaurants.size() == 2){
            try {
                var numberOfMoves1 = travelToDestination(originalLocation, restaurants.get(0)).size()
                        + travelToDestination(restaurants.get(0), restaurants.get(1)).size()
                        + travelToDestination(restaurants.get(1), nextOrder.getDeliverToLongLat()).size();
            }
            catch (NullPointerException e){
                try {
                    var numberOfMoves2 = travelToDestination(originalLocation, restaurants.get(1)).size()
                            + travelToDestination(restaurants.get(1), restaurants.get(0)).size()
                            + travelToDestination(restaurants.get(0), nextOrder.getDeliverToLongLat()).size();
                }
                catch (NullPointerException e2) {
                    return false;
                }
            }

        }

        return true;

    }


    /**
     * Helper method that, given the current location of the drone, the order we want to do and the restaurants of that
     * order, returns the shortest number of moves performed by the drone when performing that order.
     * @param originalLocation the current location of the drone
     * @param remainingOrder the order we want to do
     * @param restaurants the restaurants belonging to that order.
     * @return the shortest number of moves performed by the drone when performing that order.
     */
    private Integer getNumberOfMoves(LongLat originalLocation, OrderDetails remainingOrder, ArrayList<LongLat> restaurants) {
        Integer numberOfMoves = 10000;
        if (restaurants.size() == 1){
            numberOfMoves = travelToDestination(originalLocation, restaurants.get(0)).size()
                    + travelToDestination(restaurants.get(0), remainingOrder.getDeliverToLongLat()).size();
        }
        else if (restaurants.size() == 2){
            //The try and catch are performed to catch NullPointerExceptions that happen when we have some moves that we
            //unable to perform. However, we only use this method when we know we can make a move, so at least one of the
            //paths must be legal.
            try {
                var numberOfMoves1 = travelToDestination(originalLocation, restaurants.get(0)).size()
                        + travelToDestination(restaurants.get(0), restaurants.get(1)).size()
                        + travelToDestination(restaurants.get(1), remainingOrder.getDeliverToLongLat()).size();
                var numberOfMoves2 = travelToDestination(originalLocation, restaurants.get(1)).size()
                        + travelToDestination(restaurants.get(1), restaurants.get(0)).size()
                        + travelToDestination(restaurants.get(0), remainingOrder.getDeliverToLongLat()).size();
                numberOfMoves = Math.min(numberOfMoves1, numberOfMoves2);
            } catch (NullPointerException e) {
                try {
                    numberOfMoves = travelToDestination(originalLocation, restaurants.get(0)).size()
                            + travelToDestination(restaurants.get(0), restaurants.get(1)).size()
                            + travelToDestination(restaurants.get(1), remainingOrder.getDeliverToLongLat()).size();
                }
                catch (NullPointerException e1){
                    numberOfMoves = travelToDestination(originalLocation, restaurants.get(1)).size()
                            + travelToDestination(restaurants.get(1), restaurants.get(0)).size()
                            + travelToDestination(restaurants.get(0), remainingOrder.getDeliverToLongLat()).size();
                }
            }
        }
        else{
            System.err.println("The maximum number of restaurants for an order is :" + MAXIMUM_NUMBER_OF_SHOPS);
        }
        return numberOfMoves;
    }

    /**
     * Setter of the {@link #orderNumbers} attribute
     * @param orderNo order number that the drone is performing
     * @param numberOfMoves number of moves the drone is performing in that order
     */
    public void setOrderNumbers(String orderNo, Integer numberOfMoves){
        for (int i = 0; i < numberOfMoves; i++) {
            orderNumbers.add(orderNo);
        }
    }

    /**
     * Method that takes the list of the orders that the drone has decided to perform and populates the
     * {@link #coordinatesToVisit} attribute, which represents the list of  coordinates  (for  the corresponding
     * restaurant(s)  and  for  the  delivery locations)  the  drone  has to visit to complete the given orders.
     * @param orderDetailsArrayList list of orders that the drone has decided to perform.
     */
    public void setCoordinatesToVisit(ArrayList<OrderDetails> orderDetailsArrayList){
        ArrayList<LongLat> coordinatesToVisit = new ArrayList<LongLat>();
        coordinatesToVisit.add(APPLETON_TOWER_COORDINATES);
        for (OrderDetails orderDetails: orderDetailsArrayList){
            var restaurantsLongLats = orderDetails.getDeliverFromLongLat();
            var destinationLongLat = orderDetails.getDeliverToLongLat();
            if (restaurantsLongLats.size() == 1){
                coordinatesToVisit.add(restaurantsLongLats.get(0));
            }
            else if (restaurantsLongLats.size() == 2) {
                //We need to find the shortest path for visiting from our current location the two restaurants and then
                // deliver the order. The try and catch are performed to catch NullPointerExceptions that happen when we
                // have some moves that we are unable to perform. However, if we set the coordinates then when we know
                // we can make a move, so at least one of the paths must be legal.
                try {
                    var numberOfMoves1 = travelToDestination(coordinatesToVisit.get(coordinatesToVisit.size() - 1), restaurantsLongLats.get(0)).size()
                            + travelToDestination(restaurantsLongLats.get(0), restaurantsLongLats.get(1)).size()
                            + travelToDestination(restaurantsLongLats.get(1), destinationLongLat).size();
                    var numberOfMoves2 = travelToDestination(coordinatesToVisit.get(coordinatesToVisit.size() - 1), restaurantsLongLats.get(1)).size()
                            + travelToDestination(restaurantsLongLats.get(1), restaurantsLongLats.get(0)).size()
                            + travelToDestination(restaurantsLongLats.get(0), destinationLongLat).size();
                    if (numberOfMoves1 < numberOfMoves2) {
                        coordinatesToVisit.add(restaurantsLongLats.get(0));
                        coordinatesToVisit.add(restaurantsLongLats.get(1));
                    } else {
                        coordinatesToVisit.add(restaurantsLongLats.get(1));
                        coordinatesToVisit.add(restaurantsLongLats.get(0));
                    }
                }
                catch (NullPointerException e){
                    try {
                        var numberOfMoves1 = travelToDestination(coordinatesToVisit.get(coordinatesToVisit.size() - 1), restaurantsLongLats.get(0)).size()
                                + travelToDestination(restaurantsLongLats.get(0), restaurantsLongLats.get(1)).size()
                                + travelToDestination(restaurantsLongLats.get(1), destinationLongLat).size();
                        coordinatesToVisit.add(restaurantsLongLats.get(0));
                        coordinatesToVisit.add(restaurantsLongLats.get(1));
                    }
                    catch (NullPointerException e1){
                        coordinatesToVisit.add(restaurantsLongLats.get(1));
                        coordinatesToVisit.add(restaurantsLongLats.get(0));
                    }
                }

            }
            else{
                System.err.println("The maximum number of restaurants for an order is :" + MAXIMUM_NUMBER_OF_SHOPS);

            }

            coordinatesToVisit.add(destinationLongLat);
        }
        this.coordinatesToVisit = coordinatesToVisit;
    }

    /**
     * Given the key coordinates we need to visit, the setRoute method constructs a move-by-move route by avoiding
     * the no-fly zones and staying inside the confinement area while visiting each coordinate to visit in order. It
     * then populates the {@link #route} field with the resulting route.
     * @param coordinatesToVisit the list of coordinates we need to visit.
     */
    public void setRoute(ArrayList<LongLat> coordinatesToVisit) {
        angles.clear();
        ArrayList<LongLat> route = new ArrayList<>();
        route.add(coordinatesToVisit.get(0));
        for (int i = 0; i < coordinatesToVisit.size(); i++) {
            int nextIndex= i+1;
            if(i == coordinatesToVisit.size()-1){
                nextIndex = 0;
            }
            var nextDestination = coordinatesToVisit.get(nextIndex);
            var currentPosition = coordinatesToVisit.get(i);
            var movementsArray = travelToDestination(currentPosition, nextDestination);
            if (movementsArray != null) {
                route.addAll(movementsArray);
            }

        }
        this.route = route;
    }

    /**
     * Method that given two LongLat coordinates, constructs a move-by-move route between those two points by avoiding
     * the no-fly zones and staying inside the confinement area. It first tries to go directly using no landmarks, and
     * if the route would not be possible, then it tries to reach the destination by choosing (if possible) the shortest
     * route to reach the destination using one landmark.
     * @param originalLocation the point the drone is currently in.
     * @param destination the point the drone wants to reach.
     * @return an ArrayList of objects of type LongLat that represents the coordinates of the move-by-move route
     * between those two points by avoiding the no-fly zones and staying inside the confinement area.
     */
    private ArrayList<LongLat> travelToDestination(LongLat originalLocation, LongLat destination){

        ArrayList<LongLat> movesList = travelToDestinationWithNoLandmarks(originalLocation, destination);
        // We first see if it is possible to go directly to the destination with no Landmarks
        if (movesList.size() != 0) {
            angles.addAll(getMovesAngles(originalLocation, destination));
            return movesList;
        }
        // If not, we then try to go to the destination using only one landmark
        else{
            // For every landmark we check if we can go there and with how many moves, and we choose the one which gives us the shortest path
            HashMap<ArrayList<LongLat>, ArrayList<Integer>> arrayOfMovesAndOfAngles = new HashMap<ArrayList<LongLat>, ArrayList<Integer>>();
            for (LongLat landmark : landmarks){
                ArrayList<LongLat> moves = travelToDestinationWithNoLandmarks(originalLocation, landmark);
                ArrayList<LongLat> fromLandmarkToDestination = travelToDestinationWithNoLandmarks(landmark, destination);

                ArrayList<Integer> anglesToDestination = getMovesAngles(originalLocation, landmark);
                ArrayList<Integer> anglesFromLandmarkToDestination = getMovesAngles(landmark, destination);

                if(moves.size() != 0 && fromLandmarkToDestination.size() != 0) {
                    //We eliminate the hovering over the landmark
                    moves.remove(moves.size()-1);
                    anglesToDestination.remove(anglesToDestination.size()-1);

                    moves.addAll(fromLandmarkToDestination);
                    anglesToDestination.addAll(anglesFromLandmarkToDestination);
                    arrayOfMovesAndOfAngles.put(moves, anglesToDestination);
                }
            }
            // We iterate over the Hashmap to choose the non-zero path with the least moves
            return getShortestPathUsingLandmarks(arrayOfMovesAndOfAngles);
        }

    }


    /**
     * Helper method of the travelToDestination method. This method constructs a move-by-move route between the two given points
     * by trying to go directly to the destination using no landmarks.
     * @param originalLocation the point the drone is currently in.
     * @param destination the point the drone wants to reach.
     * @return null if we can not go to the Destination with no landmarks, the direct move-by-move route between the two
     * given points otherwise
     */
    private ArrayList<LongLat> travelToDestinationWithNoLandmarks(LongLat originalLocation, LongLat destination){
        ArrayList<LongLat> movesList = new ArrayList<>();
        var currentPosition = originalLocation;
        while (!currentPosition.closeTo(destination)){
            var nextAngle = currentPosition.nextAngle(destination);
            var possibleNextPosition = currentPosition.nextPosition(nextAngle);
            if (currentPosition.isValidMovement(possibleNextPosition, noFlyZonesPoints)) {
                movesList.add(possibleNextPosition);

                //If we reach the next destination we have to hover in the same place for one turn
                if(possibleNextPosition.closeTo(destination)){
                    movesList.add(possibleNextPosition);
                }
                currentPosition = possibleNextPosition;

            }
            else{
                movesList.clear();
                return movesList;
            }
        }
        return movesList;
    }


    /**
     * Method that, given the current drone location and the intended destination, it returns an ArrayList whose length
     * equals the number of moves we would need to perform to arrive to that destination, and where the ith element of
     * the resulting ArrayList represents the angle we would need to perform the ith move from the current drone location
     * to the intended destination
     * @param originalLocation the point the drone is currently in.
     * @param destination the point the drone wants to reach.
     * @return An ArrayList of every angle the drone would perform when travelling from originalLocation to destination.
     */
    private ArrayList<Integer> getMovesAngles(LongLat originalLocation, LongLat destination){
        var currentPosition = originalLocation;
        ArrayList<Integer> movementAngles = new ArrayList<>();
        while (!currentPosition.closeTo(destination)){
            var nextAngle = currentPosition.nextAngle(destination);
            var possibleNextPosition = currentPosition.nextPosition(nextAngle);
            if (currentPosition.isValidMovement(possibleNextPosition, noFlyZonesPoints)) {
                movementAngles.add(nextAngle);
                currentPosition = possibleNextPosition;

            }
            else{
                movementAngles.clear();
                return movementAngles;
            }
        }
        //We account for the hovering at the end
        movementAngles.add(HOVERING_ANGLE);
        return movementAngles;
    }


    /**
     * Helper method of the {@link #travelToDestination(LongLat, LongLat)} where, given a Hashmap with all the different
     * possible paths to perform using landmarks, and the corresponding list of angles for each of those paths, it
     * returns the shortest path of all and sets the corresponding values of {@link #angles}.
     * @param arrayOfMovesAndOfAngles the Hashmap with all the different possible paths to perform using landmarks,
     *                                and the corresponding list of angles for each of those paths
     * @return null if the ArrayList is empty, the shortest path of the Hashmap otherwise.
     */
    private ArrayList<LongLat> getShortestPathUsingLandmarks(HashMap<ArrayList<LongLat>, ArrayList<Integer>> arrayOfMovesAndOfAngles) {
        Map.Entry<ArrayList<LongLat>, ArrayList<Integer>> minEntry = null;
        for (Map.Entry<ArrayList<LongLat>, ArrayList<Integer>> entry : arrayOfMovesAndOfAngles.entrySet()) {
            if (minEntry == null || (entry.getValue().size() < minEntry.getValue().size() && entry.getValue().size() != 0)) {
                minEntry = entry;
            }
        }
        ArrayList<LongLat> shortestPathUsingLandmarks = new ArrayList<>();
        try {
            shortestPathUsingLandmarks = minEntry.getKey();
            angles.addAll(minEntry.getValue());
            return shortestPathUsingLandmarks;
        }
        catch (NullPointerException e) {
            return null;
        }
    }


    /**
     * Helper method of the {@link #getNextOrder(LongLat, ArrayList)} method, where given a hashmap with all the different
     * order details and their price per movement, it returns the OrderDetails object which has the highest price per
     * movement.
     * @param pricePerMovementOfRemainingOrderDetails hashmap with all the different order details and their price per
     *                                                movement.
     * @return null if the given hashmap is empty, the OrderDetails object which has the highest price per movement
     * otherwise.
     */
    private OrderDetails getMaximumValueOfHashmap(HashMap<OrderDetails, Double> pricePerMovementOfRemainingOrderDetails) {
        Map.Entry<OrderDetails, Double> maxEntry = null;
        for (Map.Entry<OrderDetails, Double> entry : pricePerMovementOfRemainingOrderDetails.entrySet()) {
            if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                maxEntry = entry;
            }
        }
        try {
            return maxEntry.getKey();
        }
        catch (NullPointerException e){
            return null;
        }

    }


    /**
     * Setter of the {@link #totalPrice} attribute.
     * @param orderDetailsArrayList list of OrderDetails representing the orders that have been placed for the day
     */
    public void setTotalPrice(ArrayList<OrderDetails> orderDetailsArrayList) {
        for (var order : orderDetailsArrayList){
            totalPrice += order.getPrice();
        }
    }


    /**
     * Setter for the {@link #priceDone} attribute.
     */
    public void setPriceDone() {
        for (var orderToDo : getOrderDetailsToDo()){
            priceDone += orderToDo.getPrice();
        }
    }

    /**
     * Setter for the {@link #percentageMonetaryValue} attribute.
     */
    public void setPercentageMonetaryValue() {
        this.percentageMonetaryValue = (double) priceDone/totalPrice;
    }

    public ArrayList<LongLat> getRoute() {
        return route;
    }

    public ArrayList<OrderDetails> getOrderDetailsToDo() {
        return orderDetailsToDo;
    }

    public ArrayList<LongLat> getCoordinatesToVisit() {
        return coordinatesToVisit;
    }

    public ArrayList<Integer> getAngles() {
        return angles;
    }

    public ArrayList<String> getOrderNumbers() {
        return orderNumbers;
    }

    public Integer getTotalNumberOfMovesUsed() {
        return totalNumberOfMovesUsed;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public Integer getPriceDone() {
        return priceDone;
    }

    public double getPercentageMonetaryValue() {
        return percentageMonetaryValue;
    }
}
