package com.ringosham.controllers;

import com.ringosham.Main;
import com.ringosham.export.Exporter;
import com.ringosham.objects.Settings;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.PrintStream;

public class Controller {
    //All UI elements
    @FXML
    private FlowPane pane;
    @FXML
    public Label progressText;
    @FXML
    public ProgressBar progress;
    @FXML
    public Button exportButton;
    @FXML
    private TextArea consoleArea;
    @FXML
    private CheckBox convertCheckbox;
    @FXML
    private CheckBox filterPractice;
    @FXML
    private CheckBox overwriteCheckbox;
    @FXML
    private CheckBox addTags;
    @FXML
    private CheckBox overrideTags;
    @FXML
    private CheckBox fixEncoding;
    @FXML
    private ToggleGroup renameOptions;
    @FXML
    private RadioButton renameBeatmap;
    @FXML
    private RadioButton useBeatmapID;
    @FXML
    private CheckBox filterDuplicates;
    @FXML
    private TextField filterSeconds;

    public static File beatmapDir = new File(System.getProperty("user.home") + "/AppData/Local/Osu!/Songs");

    public Controller() {}

    @FXML
    private void initialize() {
        //UI initialization
        String convertTooltip = "Some old beatmaps may use ogg files instead of mp3. Disabling this will ensure audio quality, " +
                "but your music player will likely not able to read ogg tags (Album arts and song info)";
        String encodingTooltip = "Some very old mp3s have different encoding in their tags. This is why some songs appear as ????? in your music player";
        String addTagTooltip = "Automatically add mp3 tags based on beatmap info to the exported songs";
        String overrideTooltip = "Overrides the existing mp3 tag, in case you don't like it.";
        String useIDTooltip = "Includes the beatmap ID in the file name. Ensures there are no file conflicts, " +
                "but it will be harder to find full versions of the song when it is mixed with TV sizes";
        String renameTooltip = "The renamed file will have this following format: \"(Song name) - (Song author)\"\n" +
                "Full versions of the song will have \"(Full version)\" in the file name";
        String practiseTooltip = "Skips through any maps that are labelled as \"Stream practice\" and \"Jump practice\"";
        String filterTooltip = "The program will try to differentiate full length songs and TV size songs through the length of the song. " +
                "Highly recommended if you have a lot of beatmaps.";
        String overwriteTooltip = "Overwrite the file even if it already exists. Otherwise it will overwrite if the file sizes are different";
        convertCheckbox.setTooltip(new Tooltip(convertTooltip));
        fixEncoding.setTooltip(new Tooltip(encodingTooltip));
        overrideTags.setTooltip(new Tooltip(overrideTooltip));
        useBeatmapID.setTooltip(new Tooltip(useIDTooltip));
        filterPractice.setTooltip(new Tooltip(practiseTooltip));
        filterDuplicates.setTooltip(new Tooltip(filterTooltip));
        renameBeatmap.setTooltip(new Tooltip(renameTooltip));
        addTags.setTooltip(new Tooltip(addTagTooltip));
        overwriteCheckbox.setTooltip(new Tooltip(overwriteTooltip));
        //Console output
        Console console = new Console(this.consoleArea);
        PrintStream stream = new PrintStream(console, true);
        System.setOut(stream);
        //Console auto scroll
        consoleArea.textProperty().addListener((observable, oldValue, newValue) -> consoleArea.setScrollTop(Double.MAX_VALUE));

        //Some checks to make sure stuff works
        if (!beatmapDir.isDirectory()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(Main.appTitle);
            alert.setHeaderText("Osu! folder not found");
            alert.setContentText("Cannot find osu! installation. Please select your osu! installation folder.");
            alert.showAndWait();
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select osu! install directory");
            File newBeatmapDir = chooser.showDialog(pane.getScene().getWindow());
            if (newBeatmapDir != null)
                beatmapDir = newBeatmapDir;
            else
                Platform.exit();
        }
        if (!beatmapDir.canRead()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(Main.appTitle);
            alert.setHeaderText("Cannot read osu! folder");
            alert.setContentText("Please fix the permission of your osu! installation folder");
            alert.showAndWait();
            Platform.exit();
        }
        progressText.setText("Ready. " + beatmapDir.listFiles(File::isDirectory).length + " songs found. (Estimate)");
        overrideTags.setDisable(true);
        filterPractice.setSelected(true);
        filterDuplicates.setSelected(true);
        filterSeconds.setText("10");
        filterSeconds.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 2 || !newValue.matches("\\d*") || newValue.equals("0"))
                filterSeconds.setText(oldValue);
        });
    }

    @FXML
    private void onAddTagChecked() {
        if (!addTags.isSelected()) {
            overrideTags.setDisable(true);
            overrideTags.setSelected(false);
        }
        else
            overrideTags.setDisable(false);
    }

    @FXML
    private void onFilterDuplicatesChecked() {
        if (!filterDuplicates.isSelected()) {
            filterSeconds.setDisable(true);
            filterSeconds.setText("");
        }
        else
            filterSeconds.setDisable(false);
    }

    @FXML
    private void onExportButtonClick() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select export directory");
        File exportDirectory = chooser.showDialog(pane.getScene().getWindow());
        if (exportDirectory != null) {
            boolean renameAsBeatmap = ((RadioButton) renameOptions.getSelectedToggle()).getText().equals("Rename after beatmap");
            int seconds;
            if (!filterDuplicates.isSelected())
                seconds = 0;
            else
                seconds = Integer.parseInt(filterSeconds.getText());
            Settings settings = new Settings(convertCheckbox.isSelected(), filterPractice.isSelected(), overwriteCheckbox.isSelected(),
                    addTags.isSelected(), overrideTags.isSelected(), fixEncoding.isSelected(), renameAsBeatmap,
                    filterDuplicates.isSelected(), seconds, exportDirectory);
            consoleArea.clear();
            Exporter exporter = new Exporter(this, settings);
            progressText.textProperty().bind(exporter.messageProperty());
            progress.progressProperty().bind(exporter.progressProperty());
            Thread thread = new Thread(exporter);
            thread.start();
        }
    }
}
