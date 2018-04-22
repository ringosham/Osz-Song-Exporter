package com.ringosham.export;

import com.ringosham.objects.Song;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.util.List;

/**
 *  Handles tags in mp3s (Adding tags and encoding problems)
 */
public class Tagger {

    private final ReadOnlyStringWrapper progressText = new ReadOnlyStringWrapper();

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

    protected void start() {
        return;
    }

    ReadOnlyStringProperty textProperty() {
        return progressText;
    }
}
