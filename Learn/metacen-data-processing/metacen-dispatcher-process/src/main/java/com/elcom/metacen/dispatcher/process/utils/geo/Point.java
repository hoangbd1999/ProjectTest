package com.elcom.metacen.dispatcher.process.utils.geo;

/**
 *
 * @author anhdv
 */
public class Point {

    protected double x; // longitude
    
    protected double y; // latitude

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(double y) {
        this.y = y;
    }
}
