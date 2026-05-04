package com.ringosham.export;

import com.ringosham.objects.Song;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.MultimediaInfo;

import java.io.File;
import java.util.UUID;

class Converter {

    static final File convertDir = new File(System.getProperty("java.io.tmpdir") + "/convertOgg");
    private final Song song;
    private final ReadOnlyStringWrapper console = new ReadOnlyStringWrapper();
    
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
            MultimediaObject multimediaObject = new MultimediaObject(song.getFileLocation());
            Encoder encoder = new Encoder();
            MultimediaInfo info = multimediaObject.getInfo();
            bitrate = info.getAudio().getBitRate();
            AudioAttributes audioInfo = new AudioAttributes();
            audioInfo.setCodec("libmp3lame");
            audioInfo.setBitRate(bitrate);
            EncodingAttributes attributes = new EncodingAttributes();
            attributes.setAudioAttributes(audioInfo);
            attributes.setOutputFormat("mp3");
            output = new File(convertDir.getAbsolutePath(), UUID.randomUUID() + ".mp3");
            encoder.encode(multimediaObject, output, attributes);
        } catch (EncoderException e) {
            console.set("Failed reading ogg file. Keeping ogg format: " + song.getTitle() + " - " + song.getAuthor());
            e.printStackTrace();
            return song.getFileLocation();
        }
        return output;
    }
}
