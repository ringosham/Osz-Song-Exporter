package com.ringosham.export;

import com.ringosham.controllers.Controller;
import com.ringosham.objects.Settings;
import com.ringosham.objects.Song;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Exporter extends Task<Void> {
    //Constructor
    private Controller ui;
    private Settings settings;

    //Status
    static int failCount = 0;
    static List<String> failSongs = new ArrayList<>();

    //Export variables
    private LinkedList<Song> songList = new LinkedList<>();

    public Exporter(Controller ui, Settings settings) {
        this.ui = ui;
        this.settings = settings;
    }

    @Override
    protected Void call() {
        failCount = 0;
        ui.exportButton.setDisable(true);
        updateMessage("Analysing beatmaps...");
        Hasher hasher = new Hasher();
        //FIXME Acquire return value from task
        hasher.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> songList = hasher.getValue());
        ui.progress.progressProperty().bind(hasher.progressProperty());
        Thread hashThread = new Thread(hasher);
        hashThread.start();
        try {
            hashThread.join(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Song song : songList) {
            System.out.println("Title: " + song.getTitle());
            System.out.println("Author: " + song.getAuthor());
            System.out.println("Duration: " + song.getDuration());
        }
        ui.exportButton.setDisable(false);
        return null;
    }
}
