package sample;

import java.util.Vector;

public class FreeHandLines extends Lines {
    private Vector<Double> lineX;
    private Vector<Double> lineY;
    String lineType;

    FreeHandLines(){ lineType = "freehand"; }

    //constructor
    FreeHandLines(Vector<Double> lineX_in, Vector<Double> lineY_in) {
        super();
        lineX = lineX_in;
        lineY = lineY_in;
        lineType = "freehand";
        //lineList = new Lines[1000];
    }

    //methods and accessors for fh lines' data

    public String getLineType()
    {
        return lineType;
    }

    public Vector<Double> getLineX()
    {
        return lineX;
    }

    public Vector<Double> getLineY()
    {
        return lineY;
    }
}
