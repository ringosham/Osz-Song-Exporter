package com.ringosham.export;

import com.ringosham.objects.Song;
import it.sauronsoftware.jave.*;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.io.File;
import java.util.UUID;

class Converter {

    static final File convertDir = new File(System.getProperty("java.io.tmpdir") + "/convertOgg");
    private Song song;
    private ReadOnlyStringWrapper console = new ReadOnlyStringWrapper();
    
    ReadOnlyStringProperty consoleProperty() {
        return console;
    }

    Converter(Song song) {
        this.song = song;
    }

    File start() {
        if (!convertDir.exists())
            convertDir.mkdir();
        int bitrate;
        File output;
        try {
            Encoder encoder = new Encoder();
            MultimediaInfo info = encoder.getInfo(song.getFileLocation());
            bitrate = info.getAudio().getBitRate();
            AudioAttributes audioInfo = new AudioAttributes();
            audioInfo.setBitRate(bitrate);
            EncodingAttributes attributes = new EncodingAttributes();
            attributes.setAudioAttributes(audioInfo);
            attributes.setFormat("mp3");
            output = new File(convertDir.getAbsolutePath(), UUID.randomUUID().toString() + ".mp3");
            encoder.encode(song.getFileLocation(), output, attributes);
        } catch (EncoderException e) {
            console.set("Failed reading ogg file. Keeping ogg format: " + song.getTitle() + " - " + song.getAuthor());
            e.printStackTrace();
            return song.getFileLocation();
        }
        return output;
    }
}
