package sample;

import javafx.scene.shape.Shape;
import java.util.Vector;

public class Lines extends Shape {
    //declare variables
    double xStart,xEnd,yStart,yEnd;
    String lineType;
    Vector<Double> listX = new Vector<Double>();
    Vector<Double> listY = new Vector<Double>();

    Lines() { lineType = "straight"; }

    //constructor
    Lines(double xStart_in, double yStart_in, double xEnd_in, double yEnd_in) {
        //instantiate variables
        xStart = xStart_in;
        yStart = yStart_in;
        xEnd = xEnd_in;
        yEnd = yEnd_in;
        lineType = "straight";
    }
    //create accessors and mutators for line class
    public double getXStart()
    {
        return xStart;
    }
    public double getYStart()
    {
        return yStart;
    }
    public double getXEnd()
    {
        return xEnd;
    }
    public double getYEnd()
    {
        return yEnd;
    }

    public void setStartX(double x) {
        xStart = x;
    }

    public String getLineType()
    {
        return lineType;
    }

    public void setStartY(double y) {
        yStart = y;
    }

    public void setEndX(double x) {
        xEnd = x;
    }

    public void setEndY(double y) {
        yEnd = y;
    }

    public double getStartX() {
        return xStart;
    }

    public double getStartY() {
        return yStart;
    }

    public double getEndX() {
        return xEnd;
    }

    public double getEndY() {
        return yEnd;
    }

    public void setLineWidth(double lineWidth) {
        lineWidth = 4;
    }
}
