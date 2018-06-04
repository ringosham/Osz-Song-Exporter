package com.ringosham.export;

import com.ringosham.controllers.Controller;
import com.ringosham.objects.Settings;
import com.ringosham.objects.Song;
import com.victorlaerte.asynctask.AsyncTask;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class Exporter extends AsyncTask<Void, Object, Void> {
    //Constructor
    private Controller ui;
    private Settings settings;

    //Status
    static int failCount = 0;

    public Exporter(Controller ui, Settings settings) {
        this.ui = ui;
        this.settings = settings;
    }

    //Clean up
    private void deleteTempDirectory() {
        if (Converter.convertDir.exists())
            for (File file : Converter.convertDir.listFiles())
                if (!file.delete())
                    file.deleteOnExit();
        if (!Converter.convertDir.delete())
            Converter.convertDir.deleteOnExit();
    }

    @Override
    public void onPreExecute() {
        ui.exportButton.setDisable(true);
        ui.oszExport.setDisable(true);
    }

    @Override
    public void progressCallback(Object... params) {
        String action = (String) params[0];
        switch (action) {
            case "text":
                String text = (String) params[1];
                ui.progressText.setText(text);
                break;
            case "progress":
                double workDone = ((Number) params[1]).doubleValue();
                double max = ((Number) params[2]).doubleValue();
                ui.progress.setProgress(workDone / max);
                break;
            case "console":
                String consoleText = (String) params[1];
                ui.consoleArea.appendText(consoleText + "\n");
                break;
            default:
        }
    }

    @Override
    public Void doInBackground(Void... params) {
        failCount = 0;
        int copiedCount;
        publishProgress("console", "Started exporting at " + Calendar.getInstance().getTime());
        //Lesson learned. DO NOT pipe the console to TextArea.
        //System.out will often sends too many updates (Updates are sent character by character) to the UI, causing it to crash.
        //Don't ask me why it happens. Even I have no idea why it throws NullPointerExceptions.
        StringBuilder builder = new StringBuilder();
        builder.append("Export directory: ");
        builder.append(settings.getExportDirectory().getAbsolutePath());
        publishProgress("console", builder.toString());
        publishProgress("text", "Analysing beatmaps...");
        Hasher hasher = new Hasher();
        hasher.progressProperty().addListener(((observable, oldValue, newValue) -> publishProgress("progress", newValue.doubleValue(), 1)));
        hasher.consoleProperty().addListener(((observable, oldValue, newValue) -> publishProgress("console", newValue)));
        //Export variables
        List<Song> songList = hasher.start();

        publishProgress("text", "Filtering beatmaps...");
        publishProgress("progress", -1, 1);
        Filter filter = new Filter(songList, settings.isFilterPractice(), settings.isFilterDuplicates(), settings.getFilterSeconds());
        songList = filter.start();
        builder = new StringBuilder();
        builder.append("Filtered songs down to ");
        builder.append(songList.size());
        builder.append(" songs");
        publishProgress("console", builder.toString());

        if (settings.isConvertOgg()) {
            deleteTempDirectory();
            for (Song song : songList) {
                if (song.isOgg()) {
                    if (song.getUnicodeTitle() != null && song.getUnicodeAuthor() != null)
                        publishProgress("text", "Converting " + song.getUnicodeTitle() + " - " + song.getUnicodeAuthor());
                    else
                        publishProgress("text", "Converting " + song.getTitle() + " - " + song.getAuthor());
                    Converter converter = new Converter(song);
                    converter.consoleProperty().addListener(((observable, oldValue, newValue) -> publishProgress("console", newValue)));
                    song.setFileLocation(converter.start());
                }
            }
        }

        //The copier handles the renaming as well
        Copier copier = new Copier(songList, settings.isRenameAsBeatmap(), settings.isOverwrite(), settings.getExportDirectory(), settings.isFilterDuplicates(), settings.isRomanjiNaming());
        copier.progressProperty().addListener(((observable, oldValue, newValue) -> publishProgress("progress", newValue.doubleValue(), 1)));
        copier.progressTextProperty().addListener(((observable, oldValue, newValue) -> publishProgress("text", newValue)));
        copier.consoleProperty().addListener(((observable, oldValue, newValue) -> publishProgress("console", newValue)));
        copiedCount = copier.start();

        //Add tags after copying
        if (settings.isApplyTags()) {
            Tagger tagger = new Tagger(songList, settings.isApplyTags(), settings.isOverrideTags());
            tagger.progressProperty().addListener(((observable, oldValue, newValue) -> publishProgress("progress", newValue.doubleValue(), 1)));
            tagger.textProperty().addListener(((observable, oldValue, newValue) -> publishProgress("text", newValue)));
            tagger.consoleProperty().addListener(((observable, oldValue, newValue) -> publishProgress("console", newValue)));
            tagger.start();
        }
        builder = new StringBuilder();
        builder.append("Total exported songs: ");
        builder.append(copiedCount);
        publishProgress("console", builder.toString());
        builder = new StringBuilder();
        builder.append("Total songs that failed to copy: ");
        builder.append(failCount);
        publishProgress("console", builder.toString());
        publishProgress("text", "Cleaning up...");
        deleteTempDirectory();
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(settings.getExportDirectory());
        } catch (IOException ignored) {
        }
        publishProgress("progress", 0, 1);
        publishProgress("text", "Ready.");
        return null;
    }

    @Override
    public void onPostExecute(Void params) {
        ui.exportButton.setDisable(false);
        ui.oszExport.setDisable(false);
    }
}
