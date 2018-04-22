package com.ringosham.export;

import com.ringosham.controllers.Controller;
import com.ringosham.objects.Settings;
import com.ringosham.objects.Song;
import javafx.concurrent.Task;

import java.awt.*;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class Exporter extends Task<Void> {
    //Constructor
    private Controller ui;
    private Settings settings;

    //Status
    static int failCount = 0;
    private int copiedCount = 0;

    //Export variables
    private List<Song> songList = new LinkedList<>();

    public Exporter(Controller ui, Settings settings) {
        this.ui = ui;
        this.settings = settings;
    }

    @Override
    protected Void call() {
        //FIXME Need workaround with Task update UI.
        //FIXME Maybe send all threads from controller?
        //FIXME Maybe remove task functionality from every class except this one?
        failCount = 0;
        copiedCount = 0;
        System.out.println("Started exporting at " + Calendar.getInstance().getTime());
        System.out.println("Export directory: " + settings.getExportDirectory().getAbsolutePath());
        ui.exportButton.setDisable(true);
        //There seems to be a Java bug with updateMessage. Sometimes crashes elements that are binded.
        //I suspect it's the compiler's fault
        updateMessage("Analysing beatmaps...");
        Hasher hasher = new Hasher();
        hasher.setOnSucceeded(event -> songList = hasher.getValue());
        ui.progress.progressProperty().bind(hasher.progressProperty());
        Thread hashThread = new Thread(hasher);
        hashThread.setDaemon(true);
        hashThread.start();
        try {
            hashThread.join(0);
            //Wait for the onSucceeded listener
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        updateMessage("Filtering beatmaps...");
        updateProgress(Long.MIN_VALUE, Long.MAX_VALUE);
        Filter filter = new Filter(songList, settings.isFilterPractice(), settings.isFilterDuplicates(), settings.getFilterSeconds());
        filter.setOnSucceeded(event -> songList = filter.getValue());
        Thread filterThread = new Thread(filter);
        filterThread.setDaemon(true);
        filterThread.start();
        try {
            filterThread.join(0);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (settings.isConvertOgg()) {
            for (Song song : songList) {
                if (song.isOgg()) {
                    if (song.getUnicodeTitle() != null && song.getUnicodeAuthor() != null)
                        updateMessage("Converting " + song.getUnicodeTitle() + " - " + song.getUnicodeAuthor());
                    else
                        updateMessage("Converting " + song.getTitle() + " - " + song.getAuthor());
                    Converter converter = new Converter(song);
                    converter.setOnSucceeded(event -> song.setFileLocation(converter.getValue()));
                    Thread convertThread = new Thread(converter);
                    convertThread.setDaemon(true);
                    convertThread.start();
                    try {
                        convertThread.join(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //The copier handles the renaming as well
        Copier copier = new Copier(songList, settings.isRenameAsBeatmap(), settings.isOverwrite(), settings.getExportDirectory(), settings.isFilterDuplicates());
        copier.setOnSucceeded(event -> copiedCount = copier.getValue());
        ui.progress.progressProperty().bind(copier.progressProperty());
        //ui.progressText.textProperty().bind(copier.messageProperty());
        updateMessage("Copying songs...");
        Thread copyThread = new Thread(copier);
        copyThread.setDaemon(true);
        copyThread.start();
        try {
            copyThread.join(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //Add tags after copying
        if (settings.isFixEncoding() || settings.isApplyTags()) {
            Tagger tagger = new Tagger(songList, settings.isApplyTags(), settings.isOverrideTags(), settings.isFixEncoding());
            ui.progressText.textProperty().bind(tagger.messageProperty());
            Thread tagThread = new Thread(tagger);
            tagThread.setDaemon(true);
            tagThread.start();
            try {
                tagThread.join(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Export complete");
        System.out.println("Total exported songs: " + copiedCount);
        System.out.println("Total songs that failed to copy: " + failCount);
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(settings.getExportDirectory());
        } catch (IOException ignored) {
        }
        ui.progressText.textProperty().unbind();
        ui.exportButton.setDisable(false);
        return null;
    }
}
