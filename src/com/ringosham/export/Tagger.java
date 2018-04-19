package com.ringosham.export;

import com.ringosham.objects.Song;
import javafx.concurrent.Task;

import java.util.List;

/**
 *  Handles tags in mp3s (Adding tags and encoding problems)
 */
public class Tagger extends Task<Void> {


    private List<Song> songList;
    private boolean applyTags;
    private boolean overrideTags;
    private boolean fixEncoding;

    Tagger(List<Song> songList, boolean applyTags, boolean overrideTags, boolean fixEncoding) {
        this.songList = songList;
        this.applyTags = applyTags;
        this.overrideTags = overrideTags;
        this.fixEncoding = fixEncoding;
    }

    @Override
    protected Void call() {
        return null;
    }
}
