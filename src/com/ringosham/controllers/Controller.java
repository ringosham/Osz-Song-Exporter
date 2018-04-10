package com.ringosham.controllers;

import com.ringosham.export.Exporter;
import com.ringosham.objects.Settings;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.*;
import java.io.File;

public class Controller {
    //All UI elements
    @FXML
    public Label progressText;
    @FXML
    public ProgressBar progress;
    @FXML
    public Button exportButton;
    @FXML
    private CheckBox convertCheckbox;
    @FXML
    private CheckBox filterPractice;
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
    private RadioButton keepOriginal;
    @FXML
    private CheckBox filterDuplicates;
    @FXML
    private TextField filterSeconds;

    public static File beatmapDir = new File(System.getProperty("user.home") + "/AppData/Local/Osu!/Songs");

    public Controller() {}

    @FXML
    private void initialize() {
        //Look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        //UI initialization
        String convertTooltip = "Some old beatmaps may use ogg files instead of mp3. While disabling this will ensure audio quality, " +
                "you may risk copying duplicate songs";
        String encodingTooltip = "Some very old mp3s have different encoding in their tags. This is why some songs appear as ????? in your music player";
        String addTagTooltip = "Automatically add mp3 tags based on beatmap info to the exported songs";
        String overrideTooltip = "Overrides the existing mp3 tag, in case you don't like it.";
        String keepTooltip = "Keeping original file names can ensure that every file is copied," +
                " but old beatmaps will have ambiguous file names such as music.mp3, which are not very helpful";
        String renameTooltip = "The renamed file will have this following format: \"(Song name) - (Song author)\"";
        String practiseTooltip = "Skips through any maps that are labelled as \"Stream practice\" and \"Jump practice\"";
        String filterTooltip = "The program will try to differentiate full length songs and TV size songs through the length of the song. " +
                "Highly recommended if you have a lot of beatmaps.";
        convertCheckbox.setTooltip(new Tooltip(convertTooltip));
        fixEncoding.setTooltip(new Tooltip(encodingTooltip));
        overrideTags.setTooltip(new Tooltip(overrideTooltip));
        keepOriginal.setTooltip(new Tooltip(keepTooltip));
        filterPractice.setTooltip(new Tooltip(practiseTooltip));
        filterDuplicates.setTooltip(new Tooltip(filterTooltip));
        renameBeatmap.setTooltip(new Tooltip(renameTooltip));
        addTags.setTooltip(new Tooltip(addTagTooltip));
        if (!beatmapDir.isDirectory()) {
            JOptionPane.showConfirmDialog(null, "Cannot find osu! installation. Please select your osu! installation folder.", "Osu! folder not found", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showDialog(null, "Select osu! install directory");
            if (result == JFileChooser.APPROVE_OPTION)
                beatmapDir = chooser.getSelectedFile();
            else if (result == JFileChooser.CANCEL_OPTION)
                Platform.exit();
        }
        if (!beatmapDir.canRead()) {
            JOptionPane.showConfirmDialog(null, "Please fix the permission of your osu! installation folder", "Cannot read osu! folder", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            Platform.exit();
        }
        progressText.setText("Ready. " + beatmapDir.listFiles(File::isDirectory).length + " songs found. (Estimate)");
        overrideTags.setDisable(true);
        fixEncoding.setDisable(true);
        filterSeconds.setDisable(true);
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
            fixEncoding.setDisable(true);
            fixEncoding.setSelected(false);
        }
        else {
            overrideTags.setDisable(false);
            fixEncoding.setDisable(false);
        }
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
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showDialog(null, "Start export process");
        if (result == JFileChooser.APPROVE_OPTION) {
            boolean renameAsBeatmap = ((RadioButton) renameOptions.getSelectedToggle()).getText().equals("Rename after beatmap");
            int seconds;
            if (!filterDuplicates.isSelected())
                seconds = 0;
            else
                seconds = Integer.parseInt(filterSeconds.getText());
            Settings settings = new Settings(convertCheckbox.isSelected(), filterPractice.isSelected(), addTags.isSelected(),
                    overrideTags.isSelected(), fixEncoding.isSelected(), renameAsBeatmap, filterDuplicates.isSelected(),
                    seconds);
            Exporter exporter = new Exporter(this, settings);
            exporter.start();
        }
    }
}
