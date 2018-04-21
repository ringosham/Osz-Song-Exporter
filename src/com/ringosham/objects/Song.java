package com.ringosham.objects;

import java.io.File;

public class Song {
    private String hash;
    private File fileLocation;
    private String title;
    private String author;
    private long duration;
    private File outputLocation;
    private String unicodeTitle;
    private String unicodeAuthor;
    private boolean isOgg;
    private boolean isFullVersion;
    private File albumArt;

    public Song(String hash, File fileLocation, String title, String author, long duration, File outputLocation, String unicodeTitle, String unicodeAuthor, File albumArt, boolean isOgg) {
        this.hash = hash;
        this.fileLocation = fileLocation;
        this.title = title;
        this.author = author;
        this.duration = duration;
        this.outputLocation = outputLocation;
        this.unicodeTitle = unicodeTitle;
        this.unicodeAuthor = unicodeAuthor;
        this.albumArt = albumArt;
        this.isOgg = isOgg;
    }

    public File getFileLocation() {
        return fileLocation;
    }

    public String getTitle() {
        return title;
    }

    public long getDuration() {
        return duration;
    }

    public String getAuthor() {
        return author;
    }

    public String getFileName() {
        if (fileLocation == null)
            return null;
        return fileLocation.getName();
    }

    public File getOutputLocation() {
        return outputLocation;
    }

    public String getUnicodeTitle() {
        return unicodeTitle;
    }

    public String getUnicodeAuthor() {
        return unicodeAuthor;
    }

    public boolean isOgg() {
        return isOgg;
    }

    public String getHash() {
        return hash;
    }

    public void setOutputLocation(File outputLocation) {
        this.outputLocation = outputLocation;
    }

    public void setFileLocation(File fileLocation) {
        this.fileLocation = fileLocation;
    }

    public boolean isFullVersion() {
        return isFullVersion;
    }

    public void setFullVersion(boolean fullVersion) {
        isFullVersion = fullVersion;
    }

    public File getAlbumArt() {
        return albumArt;
    }
}
