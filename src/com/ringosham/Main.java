package com.ringosham;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Main extends Application {

    public static final String appTitle = "Osu! Exporter by Ringosham";

    @Override
    public void start(Stage stage) throws Exception {
        //TODO Renaming options (Keep original file name (not recommended. Automatically add suffix to the name), or rename it after beatmap name)
        //TODO Remove practise maps (Allow in settings. Just add suffix to the file name)
        //TODO Ogg conversion
        //TODO Mp3Tag encoding conversion (Detect non UTF-8 mp3 tags and converts them)
        //TODO Option to add mp3 tags (Options to overwrite or not)
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(appTitle);
        alert.setHeaderText("Disclaimer");
        String disclaimer = "By using this program, you agree the following." + "\n" +
                "1. You should not use this program will redistribute songs you download from osu! illegally" + "\n" +
                "2. The creator of this program will not be responsible for your actions while using this program";
        alert.setContentText(disclaimer);
        alert.showAndWait();
        Parent root = FXMLLoader.load(getClass().getResource("fxml/main.fxml"));
        stage.setTitle(appTitle);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
