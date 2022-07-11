# Drone-Delivery-Service

The idea of this project is to program the behaviour of a drone that is part of a drone-delivery service. The drone however has the following constraints: (i) the service is expected to be popular with more orders demanded than what the drone is able to deliver, (ii) only one drone is available for making the deliveries, (iii) the drone cannot carry more than one order at a time, (iv) the drone must avoid buildings in the no-fly zone, and (v) the drone can only fly for a limited time before its battery will run out and it will need to be recharged. The drone must also always stay inside the confinement zone, and the objective is to maximize the amount of money earned in a day.

To access the information about the orders, the program must access a database using SQL, and to access the information for the locations (in ) of the no-fly-zones, the restaurants, their menus, etc. the program must get the information by automatically accessing a website. It is important to note that the program is data-driven, that is, this program could be used with any other locations, no-fly-zones, etc. and would calculate the path accordingly.

More information about how this Object-Oriented program works, with an explanation of all classes found in the program and how they interact with each other, can be found in the "ilp_report.pdf" file. An explanation for the algorithm that I have implemented to maximize the profit gained can also be found in the same document, from page 6 onwards.

Below are two examples of GeoJSON maps showing the confinement area (the gray squares), the no-fly zones (the red buildings), and the path the drone has calculated as optimal for that given day given the orders found on the database. They correspond for the drone paths for the 7th of May and the 5th of August respectively.


![07-05-2023](https://user-images.githubusercontent.com/60312030/178376781-3da580ec-1fb2-4908-bcb1-68b422aacd4a.png)

![05-08-2023](https://user-images.githubusercontent.com/60312030/178376794-758304b0-5909-453b-bb2b-ad0fd5f08009.png)
