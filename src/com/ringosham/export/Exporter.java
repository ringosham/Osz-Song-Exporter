package com.ringosham.export;

import com.ringosham.controllers.Controller;
import com.ringosham.objects.Settings;
import com.ringosham.objects.Song;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Exporter {
    //Constructor
    private Controller ui;
    private Settings settings;

    public Exporter(Controller ui, Settings settings) {
        this.ui = ui;
        this.settings = settings;
    }

    @FXML
    public void start() {
        ui.exportButton.setDisable(true);
        ui.progressText.setText("Analysing beatmaps...");
        ui.progress.setProgress(0);
        List<Song> songList = new LinkedList<>();

        List<Hasher> hashers = new ArrayList<>();
        int latchSize = Controller.beatmapDir.listFiles(File::isDirectory).length;
        Hasher.workDone = 0;
        //Countdown latch
        CountDownLatch latch = new CountDownLatch(hashers.size());
        for (File beatmap : Controller.beatmapDir.listFiles(File::isDirectory)) {
            Hasher hasher = new Hasher(beatmap, latchSize, latch);
            hasher.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> songList.add(hasher.getValue()));
            ui.progress.progressProperty().bind(hasher.progressProperty());
            hashers.add(hasher);
        }
        ExecutorService executor = Executors.newFixedThreadPool(hashers.size());
        for (Hasher task : hashers) {
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            executor.execute(thread);
        }
        //Wait for all threads to finish
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        for (Song song : songList) {
            System.out.println("Title: " + song.getTitle());
            System.out.println("Author: " + song.getAuthor());
            System.out.println("Duration: " + song.getDuration());
        }
    }
}
