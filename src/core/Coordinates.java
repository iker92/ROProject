package core;

/**
 * Coordinates is a class used to hold the X and Y values of the spatial coordinates of the points.
 */
public class Coordinates {

    public double x;
    public double y;


    /**
     * Coordinates(int x, int y) is the basic contructor (which requires the X and Y values)
     * @param x is the horizontal coordinate value
     * @param y is the vertical coordinate value
     */
    public Coordinates(int x, int y){
        this.x=x;
        this.y=y;
    }
}
