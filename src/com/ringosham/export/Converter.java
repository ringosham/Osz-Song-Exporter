package com.ringosham.export;

import com.ringosham.objects.Song;
import javafx.concurrent.Task;

import java.io.File;

public class Converter extends Task<File> {

    private final File convertDir = new File(System.getProperty("java.io.tmpdir") + "/convert");
    private Song song;

    Converter(Song song) {
        this.song = song;
    }

    @Override
    protected File call() {
        return null;
    }
}
