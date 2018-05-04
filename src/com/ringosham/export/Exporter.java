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
            default:
        }
    }

    @Override
    public Void doInBackground(Void... params) {
        failCount = 0;
        int copiedCount;
        System.out.println("Started exporting at " + Calendar.getInstance().getTime());
        System.out.println("Export directory: " + settings.getExportDirectory().getAbsolutePath());
        publishProgress("text", "Analysing beatmaps...");
        Hasher hasher = new Hasher();
        hasher.progressProperty().addListener(((observable, oldValue, newValue) -> publishProgress("progress", newValue.doubleValue(), 1)));
        //Export variables
        List<Song> songList = hasher.start();

        publishProgress("text", "Filtering beatmaps...");
        publishProgress("progress", -1, 1);
        Filter filter = new Filter(songList, settings.isFilterPractice(), settings.isFilterDuplicates(), settings.getFilterSeconds());
        songList = filter.start();
        System.out.println("---------------------------------");
        System.out.println("Filtered songs down to " + songList.size() + " songs");
        System.out.println("---------------------------------");

        if (settings.isConvertOgg()) {
            deleteTempDirectory();
            for (Song song : songList) {
                if (song.isOgg()) {
                    if (song.getUnicodeTitle() != null && song.getUnicodeAuthor() != null)
                        publishProgress("text", "Converting " + song.getUnicodeTitle() + " - " + song.getUnicodeAuthor());
                    else
                        publishProgress("text", "Converting " + song.getTitle() + " - " + song.getAuthor());
                    Converter converter = new Converter(song);
                    song.setFileLocation(converter.start());
                }
            }
        }

        //The copier handles the renaming as well
        Copier copier = new Copier(songList, settings.isRenameAsBeatmap(), settings.isOverwrite(), settings.getExportDirectory(), settings.isFilterDuplicates());
        copier.progressProperty().addListener(((observable, oldValue, newValue) -> publishProgress("progress", newValue.doubleValue(), 1)));
        copier.progressTextProperty().addListener(((observable, oldValue, newValue) -> publishProgress("text", newValue)));
        copiedCount = copier.start();

        //Add tags after copying
        if (settings.isFixEncoding() || settings.isApplyTags()) {
            Tagger tagger = new Tagger(songList, settings.isApplyTags(), settings.isOverrideTags(), settings.isFixEncoding());
            tagger.textProperty().addListener(((observable, oldValue, newValue) -> publishProgress("text", newValue)));
        }
        System.out.println("---------------------------------");
        System.out.println("Export complete");
        System.out.println("Total exported songs: " + copiedCount);
        System.out.println("Total songs that failed to copy: " + failCount);
        System.out.println("---------------------------------");
        publishProgress("text", "Cleaning up...");
        deleteTempDirectory();
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(settings.getExportDirectory());
        } catch (IOException ignored) {
        }
        publishProgress("text", "Ready.");
        return null;
    }

    @Override
    public void onPostExecute(Void params) {
        ui.exportButton.setDisable(false);
    }
}
