package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.InputEvent;

public class Main extends Application {

    private static Main instance = null;
    public static Stage stageIn;
    private InputEvent MouseEvent;

    @Override
    public void start(Stage primaryStage) throws Exception{
        stageIn = primaryStage;
        FXMLLoader root = new FXMLLoader(getClass().getResource("sample.fxml"));

        primaryStage.setTitle("Paint Application");
        primaryStage.setScene(new Scene(root.load()));
        stageIn.setWidth((900));
        stageIn.setHeight(500);
        primaryStage.show();

        Controller grapher = root.getController();
    }
    //singleton for stage
    public static Main getInstance()
    {
        if (instance == null)
        {
            instance = new Main();
        }
        return instance;
    }
    public Stage getStage()
    {
        return stageIn;
    }
    public static void main(String[] args) {
        launch(args);
    }


}
