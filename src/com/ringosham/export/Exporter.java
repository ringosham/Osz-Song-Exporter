package com.ringosham.export;

import com.ringosham.controllers.Controller;
import com.ringosham.objects.Settings;
import com.ringosham.objects.Song;
import javafx.concurrent.Task;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class Exporter extends Task<Void> {
    //Constructor
    private Controller ui;
    private Settings settings;

    //Status
    static int failCount = 0;

    public Exporter(Controller ui, Settings settings) {
        this.ui = ui;
        this.settings = settings;
    }

    @Override
    protected Void call() {
        failCount = 0;
        int copiedCount = 0;
        System.out.println("Started exporting at " + Calendar.getInstance().getTime());
        System.out.println("Export directory: " + settings.getExportDirectory().getAbsolutePath());
        ui.exportButton.setDisable(true);
        //There seems to be a Java bug with updateMessage. Sometimes crashes elements that are binded.
        //This makes no sense as it worked upon recompiling multiple times
        //I suspect it's the compiler's fault
        updateMessage("Analysing beatmaps...");
        Hasher hasher = new Hasher();
        hasher.progressProperty().addListener(((observable, oldValue, newValue) -> updateProgress(newValue.doubleValue(), 1)));
        //Export variables
        List<Song> songList = hasher.start();

        updateMessage("Filtering beatmaps...");
        updateProgress(-1, 1);
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
                        updateMessage("Converting " + song.getUnicodeTitle() + " - " + song.getUnicodeAuthor());
                    else
                        updateMessage("Converting " + song.getTitle() + " - " + song.getAuthor());
                    Converter converter = new Converter(song);
                    song.setFileLocation(converter.start());
                }
            }
        }

        //The copier handles the renaming as well
        Copier copier = new Copier(songList, settings.isRenameAsBeatmap(), settings.isOverwrite(), settings.getExportDirectory(), settings.isFilterDuplicates());
        copier.progressProperty().addListener(((observable, oldValue, newValue) -> updateProgress(newValue.doubleValue(), 1)));
        copier.progressTextProperty().addListener(((observable, oldValue, newValue) -> updateMessage(newValue)));
        copiedCount = copier.start();

        //Add tags after copying
        if (settings.isFixEncoding() || settings.isApplyTags()) {
            Tagger tagger = new Tagger(songList, settings.isApplyTags(), settings.isOverrideTags(), settings.isFixEncoding());
            tagger.textProperty().addListener(((observable, oldValue, newValue) -> updateMessage(newValue)));
        }
        System.out.println("---------------------------------");
        System.out.println("Export complete");
        System.out.println("Total exported songs: " + copiedCount);
        System.out.println("Total songs that failed to copy: " + failCount);
        System.out.println("---------------------------------");
        updateMessage("Cleaning up...");
        deleteTempDirectory();
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(settings.getExportDirectory());
        } catch (IOException ignored) {
        }
        updateMessage("Ready.");
        ui.exportButton.setDisable(false);
        return null;
    }

    private void deleteTempDirectory() {
        if (Converter.convertDir.exists())
            for (File file : Converter.convertDir.listFiles())
                if (!file.delete())
                    file.deleteOnExit();
        if (!Converter.convertDir.delete())
            Converter.convertDir.deleteOnExit();
    }
}
