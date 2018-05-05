package com.ringosham.export;

import com.mpatric.mp3agic.*;
import com.ringosham.objects.Song;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.apache.tika.parser.txt.CharsetDetector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 *  Handles tags in mp3s (Adding tags and encoding problems)
 */
class Tagger {

    private final ReadOnlyStringWrapper progressText = new ReadOnlyStringWrapper();
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();

    private List<Song> songList;
    private boolean applyTags;
    private boolean overrideTags;
    private boolean fixEncoding;
    private ReadOnlyStringWrapper console = new ReadOnlyStringWrapper();

    ReadOnlyStringProperty consoleProperty() {
        return console;
    }

    Tagger(List<Song> songList, boolean applyTags, boolean overrideTags, boolean fixEncoding) {
        this.songList = songList;
        this.applyTags = applyTags;
        this.overrideTags = overrideTags;
        this.fixEncoding = fixEncoding;
    }

    void start() {
        int workDone = 0;
        for (Song song : songList) {
            if (song.getOutputFile().getName().endsWith(".ogg"))
                continue;
            //ID3v2 supports album arts
            ID3v2 v2Tag;
            try {
                boolean changesMade = false;
                Mp3File mp3 = new Mp3File(song.getOutputFile());
                if (overrideTags) {
                    applyTags(mp3, song);
                    changesMade = true;
                }
                else {
                    if (mp3.hasId3v1Tag())
                        mp3.removeId3v1Tag();
                    if (mp3.hasId3v2Tag()) {
                        v2Tag = mp3.getId3v2Tag();
                        String artist = v2Tag.getArtist();
                        String title = v2Tag.getTitle();
                        if ((title == null || artist == null) && applyTags) {
                            applyTags(mp3, song);
                            changesMade = true;
                        }
                        else if ((title.isEmpty() || artist.isEmpty()) && applyTags) {
                            applyTags(mp3, song);
                            changesMade = true;
                        }
                        if (fixEncoding && !changesMade) {
                            if (song.getUnicodeTitle() == null)
                                progressText.set("Fixing encoding on " + song.getTitle() + " - " + song.getAuthor());
                            else
                                progressText.set("Fixing encoding on " + song.getUnicodeTitle() + " - " + song.getUnicodeAuthor());
                            CharsetDetector detector = new CharsetDetector();
                            artist = detector.getString(artist.getBytes(), "utf-8");
                            title = detector.getString(title.getBytes(), "utf-8");
                            ID3v24Tag tag = new ID3v24Tag();
                            tag.setTitle(title);
                            tag.setArtist(artist);
                            tag.setAlbumImage(v2Tag.getAlbumImage(), v2Tag.getAlbumImageMimeType());
                            tag.setAlbum(v2Tag.getAlbum());
                            tag.setAlbumArtist(v2Tag.getAlbumArtist());
                            mp3.setId3v2Tag(tag);
                            changesMade = true;
                        }
                    } else {
                        if (applyTags) {
                            applyTags(mp3, song);
                            changesMade = true;
                        }
                    }
                }
                if (changesMade) {
                    String tempFilename = UUID.randomUUID().toString();
                    String parent = song.getOutputFile().getParent();
                    String filename = song.getOutputFile().getName();
                    mp3.save(parent + "/" + tempFilename);
                    if (!song.getOutputFile().delete())
                        song.getOutputFile().deleteOnExit();
                    new File(parent, tempFilename).renameTo(new File(parent, filename));
                }
            } catch (IOException | UnsupportedTagException | InvalidDataException | NotSupportedException e) {
                console.set("Failed processing MP3 tag. Skipping " + song.getTitle() + " - " + song.getAuthor());
                e.printStackTrace();
            }
            workDone++;
            double progressDouble = ((double) workDone) / songList.size();
            progress.set(progressDouble);
        }
    }

    private void applyTags(Mp3File mp3, Song song) {
        if (song.getUnicodeTitle() == null)
            progressText.set("Applying tags to " + song.getTitle() + " - " + song.getAuthor());
        else
            progressText.set("Applying tags to " + song.getUnicodeTitle() + " - " + song.getUnicodeAuthor());
        mp3.setId3v2Tag(generateTag(song));
    }

    private ID3v24Tag generateTag(Song song, String title, String author) {
        ID3v24Tag tag = new ID3v24Tag();
        tag.setTitle(title);
        tag.setArtist(author);
        File albumArt = song.getAlbumArt();
        if (albumArt != null) {
            String mimeType;
            if (albumArt.getName().toLowerCase().endsWith(".png"))
                mimeType = "image/png";
            else if (albumArt.getName().toLowerCase().endsWith(".bmp"))
                mimeType = "image/bmp";
            else
                mimeType = "image/jpeg";
            try {
                BufferedImage image = ImageIO.read(albumArt);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                String type = mimeType.replaceFirst("image/", "");
                if (type.equals("jpeg"))
                    type = "jpg";
                ImageIO.write(image, type, stream);
                stream.flush();
                byte[] artBytes = stream.toByteArray();
                stream.close();
                tag.setAlbumImage(artBytes, mimeType);
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
                console.set("Cannot read album art. Skipping album art for " + song.getTitle() + " - " + song.getAuthor());
            }
        }
        return tag;
    }

    private ID3v24Tag generateTag(Song song) {
        return generateTag(song, song.getUnicodeTitle(), song.getUnicodeAuthor());
    }

    ReadOnlyStringProperty textProperty() {
        return progressText;
    }

    ReadOnlyDoubleProperty progressProperty() {
        return progress;
    }
}
