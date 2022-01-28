package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;

import static java.sql.Date.valueOf;


/**
 * Class handling the queries regarding the databases
 */
public class Database {

    /** String representing the protocol of the jdbc string */
    private static final String JDBC_STRING_PROTOCOL = "jdbc:derby://";

    /** String representing the database of the jdbc string */
    private static final String JDBC_STRING_DATABASE = "/derbyDB";

    /** String representing the jdbc String which we use to connect to the database */
    private static final String jdbcString = buildJdbcString();

    /** java.sql.Connection object that represents a connection with the database */
    private static Connection conn;


    /**
     * Constructor of the Database class.
     */
    public Database() {
        try {
            conn = DriverManager.getConnection(jdbcString);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds the jdbc String to connect to the database
     * @return the built jdbc String
     */
    private static String buildJdbcString() {
        return JDBC_STRING_PROTOCOL + App.getMachineName() + ":" + App.getDatabasePort() + JDBC_STRING_DATABASE;
    }



    /**
     * This method returns a populated list of orderDetails for a given date by reading the orders database
     * @param date the date for the orders we want to obtain
     * @return an ArrayList of OrderDetails representing all of the orders in the database for a given date
     */
    public ArrayList<OrderDetails> getOrderDetails(String date) {

        Date sqlDate = valueOf(date);
        String dateQuery = "select * from orders where deliveryDate=(?)";

        ArrayList<OrderDetails> orderDetailsList = new ArrayList<>();

        try {


            PreparedStatement psOrderDetailsQuery = conn.prepareStatement(dateQuery);

            psOrderDetailsQuery.setDate(1, sqlDate);

            ResultSet results = psOrderDetailsQuery.executeQuery();

            while (results.next()) {
                String orderNo = results.getString("orderNo");
                String customer = results.getString("customer");
                String deliverTo = results.getString("deliverTo");
                OrderDetails dateOrderDetails = new OrderDetails(orderNo, customer, deliverTo, null);
                orderDetailsList.add(dateOrderDetails);
            }
        } catch (SQLException sqlexception) {
            sqlexception.printStackTrace();
        }

        var completedOrderDetailsList = getCompletedOrderDetailsList(orderDetailsList);

        return completedOrderDetailsList;
    }



    /**
     * Method that for each order, obtains and sets the corresponding items fields to the corresponding order by reading
     * from the OrdersDetails database
     * @param orderDetailsList the ArrayList of OrderDetails that we want to add the items to
     * @return that ArrayList of OrderDetails with the items field completed.
     */
    private ArrayList<OrderDetails> getCompletedOrderDetailsList(ArrayList<OrderDetails> orderDetailsList){

        ArrayList<OrderDetails> completedOrderDetailsList = new ArrayList<>();

        for (OrderDetails order :orderDetailsList){

            String orderNoQuery = "select * from orderDetails where orderNo=(?)";

            ArrayList<String> items = new ArrayList<>();

            try {
                PreparedStatement psOrderDetailsQuery = conn.prepareStatement(orderNoQuery);

                var orderNo = order.getOrderNo();

                psOrderDetailsQuery.setString(1, orderNo);

                ResultSet results = psOrderDetailsQuery.executeQuery();

                while (results.next()) {
                    String item = results.getString("item");
                    items.add(item);
                }
            } catch (SQLException sqlexception) {
                sqlexception.printStackTrace();
            }
            OrderDetails completeOrder = new OrderDetails(order.getOrderNo(), order.getCustomer(),
                    order.getDeliverTo(), items);

            completedOrderDetailsList.add(completeOrder);
        }

        return completedOrderDetailsList;
    }


    /**
     * Method that, given the list of OrderDetails with the orders the drone is doing, writes the output database table
     * deliveries with the relevant information.
     * @param ordersToDo list of OrderDetails with the orders the drone is doing.
     */
    public static void writeDatabaseTableDeliveries(ArrayList<OrderDetails> ordersToDo) {
        try {
            Statement statement = conn.createStatement();
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            ResultSet resultSet = databaseMetadata.getTables(null, null, "DELIVERIES", null);
            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet.next()) {
                statement.execute("drop table deliveries");
            }
            statement.execute("create table deliveries(orderNo char(8), deliveredTo varchar(19), costInPence int)");

            PreparedStatement psDeliveries = conn.prepareStatement("insert into deliveries values (?, ?, ?)");

            for (OrderDetails order : ordersToDo) {
                psDeliveries.setString(1, order.getOrderNo());
                psDeliveries.setString(2, order.getDeliverTo());
                psDeliveries.setInt(3, order.getPrice());
                psDeliveries.execute();
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.err.println("Could not write database table deliveries");
        }


    }

    /**
     * Method that, given the route of the moves the drone is doing, and the lists for the corresponding angles and
     * order numbers for each of those moves, writes the output database table flightpath with the relevant information.
     * @param route ArrayList of LongLat objects representing all of the coordinates the drone visits after making every
     *              move
     * @param orderNumbers ArrayList of Strings of length equal to the number of movements the drone does, and where for
     *                    the ith move the drone does, the ith element of orderNumbers indicates the order number
     *                    corresponding for that movement.
     * @param angles ArrayList of Integers of length equal to the number of movements the drone does, and where for the
     *              ith move the drone does, the ith element of angles indicates the angle that the drone has travelled
     *              with for that movement.
     */
    public static void writeDatabaseTableFlightpath(ArrayList<LongLat> route, ArrayList<String> orderNumbers, ArrayList<Integer> angles) {
        try {
            Statement statement = conn.createStatement();
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            ResultSet resultSet = databaseMetadata.getTables(null, null, "FLIGHTPATH", null);
            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet.next()) {
                statement.execute("drop table flightpath");
            }
            statement.execute("create table flightpath(orderNo char(8), fromLongitude double, fromLatitude double," +
                    "angle integer, toLongitude double, toLatitude double)");

            PreparedStatement psFlightpath = conn.prepareStatement("insert into flightpath values (?, ?, ?, ?, ?, ?)");

            for (int i = 0; i < angles.size(); i++) {
                psFlightpath.setString(1, orderNumbers.get(i));
                psFlightpath.setDouble(2, route.get(i).getLongitude());
                psFlightpath.setDouble(3, route.get(i).getLatitude());
                psFlightpath.setInt(4, angles.get(i));
                psFlightpath.setDouble(5, route.get(i+1).getLongitude());
                psFlightpath.setDouble(6, route.get(i+1).getLatitude());
                psFlightpath.execute();
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.err.println("Could not write database table flightpath");
        }


    }




}