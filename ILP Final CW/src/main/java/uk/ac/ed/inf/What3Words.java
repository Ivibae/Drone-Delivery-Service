package uk.ac.ed.inf;

/**
 * Class that represents a location using the What3Words representation.
 */
public class What3Words {

    /** LongLat object representing the what3Words location */
    private LongLat coordinates;


    public LongLat getCoordinates() {
        return coordinates;
    }


    public void setCoordinates(LongLat coordinates) {
        this.coordinates = coordinates;
    }
}
