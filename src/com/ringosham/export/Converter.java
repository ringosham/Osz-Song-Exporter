package com.ringosham.export;

import com.ringosham.objects.Song;

import java.io.File;

public class Converter {

    private final File convertDir = new File(System.getProperty("java.io.tmpdir") + "/convert");
    private Song song;

    Converter(Song song) {
        this.song = song;
    }

    public File start() {
        return null;
    }
}
