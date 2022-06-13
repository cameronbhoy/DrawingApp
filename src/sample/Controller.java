package sample;

import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;
import java.util.Vector;


//controller for sample.fxml
public class Controller  {
    //declare all variables that will need to be referenced throughout the controller
    static boolean save = false;
    boolean lastDoneWasLoad = false;
    public static double xOffset = 0, yOffset = 0;
    protected double ogX, ogY;
    static boolean rightClick = false;
    GraphicsContext gc;
    boolean linePushed = false, freehandPushed = false;
    double startX, startY, endX, endY;
    ColorPicker cpLine = new ColorPicker(Color.BLACK);
    Stack<Lines> drawings = new Stack<Lines>(); //this one is used to hold all of our lines (fh and str8)
    Stack<Lines> backupDrawings = new Stack<Lines>(); //for undo-ing a load
    Stack<Lines> redoHistory = new Stack<Lines>();
    Lines line = new Lines();
    Vector<Double> listX = new Vector<Double>();
    Vector<Double> listY = new Vector<Double>();
    int rightClickCount = 0;
    static boolean drawn = false;
    //im making a test comment in main for testing rebase
    boolean rebaseMaybe = true;



    //AINT NO PARTIES HERE BOYS



    //grab all of the objects that are mentioned in the fxml file
    @FXML
    private Canvas canvas = new Canvas(2000,2000);

    @FXML
    private Button Save;

    @FXML
    private Button Load;

    @FXML
    private Button Line;

    @FXML
    private Button FreeHand;

    @FXML
    private Button Undo;

    @FXML
    protected Button changeButt; //this is the save button

//-------------------------------------------------------------------------------------------------------------------------
    //initializer
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(4); //set line width
        changeButt = new Button("gook");
        redraw(drawings); //(re)draw contents of drawings
    }

//-------------------------------------------------------------------------------------------------------------------------
    //function for line
    public void onLine() {
        redraw(drawings); //(re)draw contents of drawings
        freehandPushed = false;
        if(linePushed) //if this is true, then that means that line was already pushed down, because otherwise it would be false (set to false in all other buttons)
        {
            linePushed = false;
        }
        else //button wasn't already pressed
        {
            linePushed = true;
        }

        //If the mouse is clicked, check to see if it was a right click
        canvas.setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.SECONDARY || e.isControlDown()) //if true, it was a right click
            {
                if(drawings.isEmpty()) //ensure drawings isn't empty
                {
                    System.out.println("There are no lines to delete");
                    return;
                }
                //collect location of click
                double mouseX = e.getX();
                double mouseY = e.getY();

                System.out.println("right clicked");
                if(!drawings.empty()) { //if there is something in drawings
                    rightClick = true;  //update rightClick
                    rightClickCount++;
                    drawn = false;

                    //loop through all of drawings to find which (if any) line is being clicked for removal
                    //loop through drawings to find line, if any, the user is right clicking for deletion
                    for (int i = 0; i < drawings.size(); i++) {
                        Lines currentLine = drawings.elementAt(i); //easy way to denote current line loop is on
                        //see if right clicked on free hand line
                        if(currentLine instanceof FreeHandLines) //if the current line is a fh line
                        {
                            FreeHandLines tempFHLine = (FreeHandLines) currentLine;
                            for(int j = 0; j < tempFHLine.getLineY().size() - 1; j ++) { //loop through the coordinate lists of fh lines
                                //get the coordinates of one of the micro-lines
                                double xStart = tempFHLine.getLineX().get(j);
                                double xEnd = tempFHLine.getLineX().get(j+1);
                                double yStart = tempFHLine.getLineY().get(j);
                                double yEnd = tempFHLine.getLineY().get(j+1);
                                //see if any of those coordinates are where the mouse was clicked. if so, remove that whole fh line
                                if((xStart == mouseX && yStart == mouseY) || (xEnd == mouseX && yEnd == mouseY))
                                {
                                    double numerator = Math.abs(((xEnd - xStart) * (yStart - e.getY()) - (xStart - e.getX()) * (yEnd - yStart)));
                                    double denominator = Math.sqrt(Math.pow(xEnd - xStart, 2) + (Math.pow(yEnd - yStart, 2)));
                                    if ((numerator / denominator) <= 5) {
                                        lastDoneWasLoad = false; //update boolean
                                        redoHistory.push(tempFHLine); //add in case of redo
                                        drawings.removeElementAt(i); //remove current line
                                        redraw(drawings); //(re)draw contents of drawings
                                    }
                                }
                            }
                        }
                        //see if right clicked on straight line
                        else if(currentLine instanceof Lines) { //if the current line is just a straight line
                            //math to determine if a point/mouse-click lies w/in 5 pixels of a line
                            double numerator = Math.abs(((currentLine.xEnd - currentLine.xStart) * (currentLine.yStart - e.getY()) - (currentLine.xStart - e.getX()) * (currentLine.yEnd - currentLine.yStart)));
                            double denominator = Math.sqrt(Math.pow(currentLine.xEnd - currentLine.xStart, 2) + (Math.pow(currentLine.yEnd - currentLine.yStart, 2)));
                            if ((numerator / denominator) <= 5) {
                                lastDoneWasLoad = false; //update boolean
                                redoHistory.push(currentLine); //add in case of redo
                                drawings.removeElementAt(i); //remove current line
                                redraw(drawings); //(re)draw contents of drawings
                            }
                        }
                        else
                        {
                            System.out.println("This is RIGGED");
                        }
                    }
                    return;
                }
            }
        });

        //actions to do if the mouse is pressed down
        canvas.setOnMousePressed(e -> {
            //get coordinates of the click (will be used for drag)
            ogX = e.getX();
            ogY = e.getY();
            listX = new Vector<Double>();
            listY = new Vector<Double>();

            //if it was NOT a right click..
            if(e.getButton() != MouseButton.SECONDARY || !e.isControlDown()) {
                startX = e.getX();
                endX = e.getY();
                line.setStartX(e.getX());
                line.setStartY(e.getY());

            }

        });

        //actions to do if the mouse is dragged
        canvas.setOnMouseDragged(e -> {
            if(linePushed) { //if the line button is actively pushed down (meaning user wants to draw a straight line)
                //draw grey line so user can see where their line will be drawn
                gc.setStroke(Color.LIGHTGRAY);
                line.setEndX(e.getX());
                line.setEndY(e.getY());
                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());

                redraw(drawings); //(re)draw contents of drawings

                //loop through all of drawings and put them on the canvas. This is keeping the already drawn lines there
                for (int i = 0; i < drawings.size(); i++) {
                    Lines currentLine = drawings.elementAt(i);
                    Lines temp = currentLine;
                    gc.setStroke(Color.BLACK);
                    gc.strokeLine(currentLine.getStartX(), currentLine.getStartY(), currentLine.getEndX(), currentLine.getEndY());
                    redraw(drawings);
                }
                gc.setStroke(Color.LIGHTGRAY);
                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
            }
            else
            {
                xOffset = ogX - e.getX();
                yOffset = ogY - e.getY();
                ogX = e.getX();
                ogY = e.getY();
                for(int i = 0; i < drawings.size(); i++)
                {
                    Lines currLine = drawings.get(i);
                    if(currLine instanceof FreeHandLines) {
                        FreeHandLines curr = (FreeHandLines) drawings.get(i);
                        for (int j = 0; j < curr.getLineX().size(); j++) {
                            System.out.println("iter: " + i + "  x val before: " + curr.getLineX().get(j) + "  y val before: " + curr.getLineY().get(j));
                            double newX = curr.getLineX().get(j) - (xOffset);
                            curr.getLineX().set(j, newX);
                            double newY = curr.getLineY().get(j) - (yOffset);
                            curr.getLineY().set(j, newY);
                            System.out.println("iter: " + i + "  x val after: " + newX + "  y val after: " + newY);
                        }
                        redraw(drawings);
                    }
                    else
                    {
                        currLine.setStartX(currLine.getXStart() - xOffset);
                        currLine.setEndX(currLine.getXEnd() - xOffset);
                        currLine.setStartY(currLine.getYStart() - yOffset);
                        currLine.setEndY(currLine.getYEnd() - yOffset);
                        redraw(drawings);
                    }
                }
            }
        });

        //actions to take when the mouse is released
        canvas.setOnMouseReleased(e -> {
            drawn = true;
            rightClick = false; //update rightClick
            if(linePushed) {
                //get values for the final resting spot of the line in conjunction w the starting points
                endX = e.getX();
                endY = e.getY();
                gc.setStroke(cpLine.getValue());
                line.setEndX(e.getX());
                line.setEndY(e.getY());
                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY()); //actually draw the completed line

                drawings.push(new Lines(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY())); //add the latest line to drawings
                save = true;
                lastDoneWasLoad = false; //update lastDoneWasLoad, because in fact, thats not the last thing done now
            }
            //changeButt.setText("Save *"); //change text if drawing hasnt been saved
        });

    }
//-------------------------------------------------------------------------------------------------------------------------
    //function for drawing freehand lines
    @FXML
    public void onFreeHand() {
        linePushed = false; //update linePushed
        redraw(drawings);//(re)draw contents of drawings

        if(freehandPushed) //if this is true, then that means that line was already pushed down, because otherwise it would be false (set to false in all other buttons)
        {
            freehandPushed = false;
        }
        else //not pushed down already
        {
            freehandPushed = true;
        }

        //If the mouse is clicked, check to see if it was a right click
        canvas.setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.SECONDARY || e.isControlDown()) //if true, it was a right click
            {
                if(drawings.isEmpty()) //ensure drawings isn't empty
                {
                    System.out.println("There are no lines to delete");
                    return;
                }
                //collect location of click
                double mouseX = e.getX();
                double mouseY = e.getY();

                System.out.println("right clicked");
                if(!drawings.empty()) { //if there is something in drawings
                    drawn = false;
                    rightClick = true;  //update rightClick
                    rightClickCount++;

                    //loop through all of drawings to find which (if any) line is being clicked for removal
                    //loop through drawings to find line, if any, the user is right clicking for deletion
                    for (int i = 0; i < drawings.size(); i++) {
                        Lines currentLine = drawings.elementAt(i); //easy way to denote current line loop is on
                        //see if right clicked on a free hand line
                        if(currentLine instanceof FreeHandLines) //if the current line is a fh line
                        {
                            FreeHandLines tempFHLine = (FreeHandLines) currentLine;
                            for(int j = 0; j < tempFHLine.getLineY().size() - 1; j ++) { //loop through the coordinate lists of fh lines
                                //get the coordinates of one of the micro-lines
                                double xStart = tempFHLine.getLineX().get(j);
                                double xEnd = tempFHLine.getLineX().get(j+1);
                                double yStart = tempFHLine.getLineY().get(j);
                                double yEnd = tempFHLine.getLineY().get(j+1);
                                //see if any of those coordinates are where the mouse was clicked. if so, remove that whole fh line
                                double numerator = Math.abs(((xEnd - xStart) * (yStart - mouseY) - (xStart - mouseX) * (yEnd - yStart)));
                                double denominator = Math.sqrt(Math.pow(xEnd - xStart, 2) + (Math.pow(yEnd - yStart, 2)));
                                if ((numerator / denominator) <= 5) {
                                    lastDoneWasLoad = false; //update boolean
                                    redoHistory.push(tempFHLine); //add in case of redo
                                    drawings.removeElementAt(i); //remove current line
                                    redraw(drawings); //(re)draw contents of drawings

                                }
                            }
                        }
                        //see if right clicked on staight line
                        else if(currentLine instanceof Lines) { //if the current line is just a straight line
                            //math to determine if a point/mouse-click lies w/in 5 pixels of a line
                            double numerator = Math.abs(((currentLine.xEnd - currentLine.xStart) * (currentLine.yStart - mouseY) - (currentLine.xStart - mouseX) * (currentLine.yEnd - currentLine.yStart)));
                            double denominator = Math.sqrt(Math.pow(currentLine.xEnd - currentLine.xStart, 2) + (Math.pow(currentLine.yEnd - currentLine.yStart, 2)));
                            if ((numerator / denominator) <= 5) {
                                lastDoneWasLoad = false; //update boolean
                                redoHistory.push(currentLine); //add in case of redo
                                drawings.removeElementAt(i); //remove current line
                                redraw(drawings); //(re)draw contents of drawings
                            }
                        }
                        else
                        {
                            System.out.println("This is RIGGED");
                        }
                    }
                    return;
                }
            }
        });

        //actions to do if mouse is pressed down
        canvas.setOnMousePressed(e -> {
            ogX = e.getX();
            ogY = e.getY();
            listX = new Vector<Double>();
            listY = new Vector<Double>();
            //clear to start blank
            listX.clear();
            listY.clear();

            gc.setStroke(cpLine.getValue());
            gc.beginPath();
            //getting coordinates of the start location
            startX = e.getX();
            startY = e.getY();
            //adding these coordinates to the vector lists
            listX.add(startX);
            listY.add(startY);
            gc.lineTo(e.getX(), e.getY()); //draw line
        });

        //actions to take if the mouse is dragged
        canvas.setOnMouseDragged(e -> {
            if(freehandPushed) { //if the freehand button is pushed down
                //draw micro-lines and add the x and y coordinates to the respective lists
                gc.lineTo(e.getX(), e.getY());
                listX.add(e.getX());
                listY.add(e.getY());
                gc.stroke();
            }
            else //if freehand has been un-clicked
            {
                xOffset = ogX - e.getX();
                yOffset = ogY - e.getY();
                ogX = e.getX();
                ogY = e.getY();
                for(int i = 0; i < drawings.size(); i++)
                {
                    Lines currLine = drawings.get(i);
                    if(currLine instanceof FreeHandLines) {
                        FreeHandLines curr = (FreeHandLines) drawings.get(i);
                        for (int j = 0; j < curr.getLineX().size(); j++) {
                            System.out.println("iter: " + i + "  x val before: " + curr.getLineX().get(j) + "  y val before: " + curr.getLineY().get(j));
                            double newX = curr.getLineX().get(j) - (xOffset);
                            curr.getLineX().set(j, newX);
                            double newY = curr.getLineY().get(j) - (yOffset);
                            curr.getLineY().set(j, newY);
                            System.out.println("iter: " + i + "  x val after: " + newX + "  y val after: " + newY);
                        }
                        redraw(drawings);
                    }
                    else
                    {
                        currLine.setStartX(currLine.getXStart() - xOffset);
                        currLine.setEndX(currLine.getXEnd() - xOffset);
                        currLine.setStartY(currLine.getYStart() - yOffset);
                        currLine.setEndY(currLine.getYEnd() - yOffset);
                        redraw(drawings);
                    }
                }
            }
        });

        //actions to take if the mouse has been released
        canvas.setOnMouseReleased(e -> {
            drawn = true;
            rightClick = false; //update boolean
            if(freehandPushed) { //if the fh button is pushed down
                //get coordinates, draw line, add coordinates to respective lists
                gc.lineTo(e.getX(), e.getY());
                endX = e.getX();
                endY = e.getY();
                listX.add(endX);
                listY.add(endY);
                gc.stroke();
                gc.closePath();
                //make a new FreeHandLines object and add it to drawings
                FreeHandLines tempLine = new FreeHandLines(listX, listY);
                drawings.push(tempLine);
                //update some booleans
                save = true;
                lastDoneWasLoad = false;
                changeButt.setText("Save *"); //change the text displayed on save button to indicate that there is a change that hasn't been saved
            }
        });

    }
//-------------------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------------------
    //function to undo a line
    public void onUndo() {
        //update some booleans
        linePushed = false;
        freehandPushed = false;
        //if the last modification to occur was the loading of a file, clear drawings (the loaded file)
        // and set it equal to the values held in the backupDrawings collection
        if(lastDoneWasLoad)
        {
            drawings.clear();
            drawings = backupDrawings;
            redraw(drawings);
        }
        //--------------------I DID NOT COMMENT THIS SECTION AS I BELIEVE YOU ARE STILL WORKING ON IT---------------
        else { //load was NOT the last thing to happen
            if (!drawings.isEmpty()) {
                //see if right clicked was pressed most recently
                if (rightClick && rightClickCount > 0) {
                    if (redoHistory.lastElement() instanceof FreeHandLines)
                        drawings.push((FreeHandLines) redoHistory.lastElement());
                    else
                        drawings.push(redoHistory.lastElement());

                    redraw(drawings);
                    redoHistory.remove(redoHistory.lastElement());

                    rightClickCount--;

                    if(drawn)
                        rightClick = false;
                    if(rightClickCount == 0)
                        rightClick = false;
                }
                //else just remove a line from drawings and redraw canvas
                else {
                    drawings.remove(drawings.lastElement()); //tried index 0 too
                    redraw(drawings);
                    save = true;
                }
            }
        }
        lastDoneWasLoad = false; //update boolean
    }

    // Open a saved photo
    public void onLoad()
    {
        //update booleans
        linePushed = false;
        freehandPushed = false;
        lastDoneWasLoad = true;

        //use file chooser to allow user to choose the file they want to load using gui
        Stage primaryStage = Main.getInstance().getStage();
        FileChooser openFile = new FileChooser();

        //only allow .txt files to be loaded
        openFile.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("text file", "*.txt"));
        openFile.setTitle("Open File");
        save = false;
        File file = openFile.showOpenDialog(primaryStage); //file the user selected
        primaryStage.setTitle(file.getName());

        if (file != null) {
            try {
                //for loop that will put the contents of drawings into backup drawings in the case of an undo
                for(int i = 0; i<drawings.size(); i++)
                {
                    backupDrawings.push(drawings.get(i));
                }
                drawings.clear(); //clear the drawings collection, as it will not hold the contents of the loaded file

                //read in from the file
                Scanner scan = new Scanner(file); //make scanner to read in from files
                while(scan.hasNext()) {
                    int lineType = scan.nextInt(); //this variable tells whether it is a fl or str8 line and how to read based on that
                    if(lineType == 0) //freehand line
                    {
                        int lineSize = scan.nextInt();//get how big the xList and YList will be
                        //vector lists to hold the coordinates of fh micro-lines
                        Vector<Double> tempX = new Vector<Double>();
                        Vector<Double> tempY = new Vector<Double>();
                        //loop through all micro-lines and gather their coordinates
                        for(int j = 0; j < lineSize; j++)
                        {
                            //read in x and y values
                            tempX.add(scan.nextDouble());
                            tempY.add(scan.nextDouble());
                        }
                        FreeHandLines fhTemp = new FreeHandLines(tempX, tempY); //make new fh line to be added to drawings
                        drawings.add(fhTemp); //add to drawings
                    }
                    else if(lineType == 1) //straight line
                    {
                        //make new line and add it to drawings
                        Lines str8Temp = new Lines(scan.nextDouble(), scan.nextDouble(), scan.nextDouble(), scan.nextDouble());
                        drawings.add(str8Temp);
                    }
                    else
                    {
                        System.out.println("There was an error! Somehow linetype is " + lineType);
                    }
                }
                redraw(drawings); //(re)draw contents of drawings
            } catch (Exception ex) {
                System.out.println("Error!");
            }
        }
    }

    //save the drawing to a file
    public void onSave()
    {
        //update booleans
        linePushed = false;
        freehandPushed = false;
        //use file chooser to allow user to easily save and name their file
        Stage primaryStage = Main.getInstance().getStage();
        FileChooser savefile = new FileChooser();
        savefile.setInitialFileName("mySavedFile");
        //require that the save type is a .txt
        savefile.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("text file", "*.txt"));
        savefile.setTitle("Save File");
        save = false;
        File file = savefile.showSaveDialog(primaryStage); //file that the user is creating
        if (file != null) {
            try {
                savefile.setInitialDirectory(file.getParentFile());//make that location the default next time something is to be saved
                //setup to write to a file
                FileOutputStream fos;
                fos = new FileOutputStream(file, false);
                PrintWriter pw = new PrintWriter(fos);
                //loop through drawings to gather tehri coordinates/info
               for(int i = 0; i < drawings.size(); i++)
               {
                   //if the current line is a fh line, write all micro-lines into the file
                   if(drawings.get(i).getClass() == FreeHandLines.class) {
                       FreeHandLines currentLine = (FreeHandLines) drawings.elementAt(i);
                       pw.print(0 + " "); //signifies the line in question is a freehand line
                       pw.print(currentLine.getLineX().size() + "\n"); //tells how many micro-lines there are in this given fh line
                       for(int j = 0; j < currentLine.getLineX().size(); j++)
                       {
                           //write the micro-lines' x and y on a line in the file.  easy to read design ;)
                           pw.println(currentLine.getLineX().get(j) + " " + currentLine.getLineY().get(j));
                       }
                   }
                   else //str8 line
                   {
                       //write the start and end coordinates to the saved file
                       Lines currentLine = drawings.elementAt(i);
                       pw.println(1); //indicate it is a straight line
                       pw.println(currentLine.getXStart() + " " + currentLine.getYStart() + " " + currentLine.getEndX() + " " + currentLine.getEndY());
                   }
               }
               pw.close(); //close er up so it actually writes to the file

            } catch (Exception ex) {
                System.out.println("Error!");
            }
        }
        changeButt.setText("Save"); //indicate on the save button that the changes have been saved

    }
    //redraw function that clears canvas and redraws all lines in drawings stack
    public void redraw(Stack<Lines> drawings) {
        gc.clearRect(0, 0, 2000, 2000); //clear the canvas we're working with
        gc.setStroke(Color.BLACK);
        if (!drawings.empty()) {
            for (int i = 0; i < drawings.size(); i++) {
                //redraw free hand line
                if (drawings.get(i).getClass() == FreeHandLines.class) {
                    FreeHandLines currentLine = (FreeHandLines) drawings.elementAt(i);
                    //getting the coordinates of the micro-lines
                    Vector<Double> tempListX = currentLine.getLineX();
                    Vector<Double> tempListY = currentLine.getLineY();
                    gc.beginPath();
                    for (int n = 0; n < tempListY.size() ; n++) {
                        //actually drawing the micro-lines that form the whole fh line
                        gc.lineTo(tempListX.get(n),tempListY.get(n));
                        gc.stroke();
                    }
                    gc.closePath();
                }
                //redraw line
                else if (drawings.get(i).getClass() == Lines.class) {
                    Lines currentLine = drawings.elementAt(i);
                    gc.strokeLine(currentLine.getStartX(), currentLine.getStartY(), currentLine.getEndX(), currentLine.getEndY());
                }
                else
                {
                    System.out.println("Error with redraw");
                }
            }
        } else {
            System.out.println("drawings is empty");
        }
    }
}
