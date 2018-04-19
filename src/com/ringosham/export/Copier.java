package com.ringosham.export;

import com.ringosham.objects.Song;
import javafx.concurrent.Task;

import java.util.List;

public class Copier extends Task<Integer> {

    private List<Song> songList;
    private boolean renameAsBeatmap;
    private boolean overwrite;

    Copier(List<Song> songList, boolean renameAsBeatmap, boolean overwrite) {
        this.songList = songList;
        this.renameAsBeatmap = renameAsBeatmap;
        this.overwrite = overwrite;
    }

    @Override
    protected Integer call() {
        return null;
    }
}
