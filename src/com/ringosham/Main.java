package com.ringosham;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        //TODO Remove duplicates
        //TODO Basic copying
        //TODO Renaming options (Keep original file name (not recommended. Automatically add suffix to the name), or rename it after beatmap name)
        //TODO Remove practise maps (Allow in settings. Just add suffix to the file name)
        //TODO Ogg conversion
        //TODO Mp3Tag encoding conversion (Detect non UTF-8 mp3 tags and converts them)
        //TODO Option to add mp3 tags (Options to overwrite or not)
        Parent root = FXMLLoader.load(getClass().getResource("fxml/main.fxml"));
        stage.setTitle("Osu! Exporter by Ringosham");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
