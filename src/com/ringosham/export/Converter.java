package com.ringosham.export;

import com.ringosham.objects.Song;
import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import org.gagravarr.vorbis.VorbisFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

class Converter {

    static final File convertDir = new File(System.getProperty("java.io.tmpdir") + "/convertOgg");
    private Song song;

    Converter(Song song) {
        this.song = song;
    }

    File start() {
        if (!convertDir.exists())
            convertDir.mkdir();
        int bitrate;
        try {
            VorbisFile ogg = new VorbisFile(song.getFileLocation());
            bitrate = ogg.getInfo().getBitrateNominal();
            ogg.close();
        } catch (IOException e) {
            System.out.println("Failed reading ogg file. Keeping ogg format: " + song.getTitle() + " - " + song.getAuthor());
            e.printStackTrace();
            return song.getFileLocation();
        }
        File output = new File(convertDir.getAbsolutePath(), UUID.randomUUID().toString() + ".mp3");
        Encoder encoder = new Encoder();
        AudioAttributes audioInfo = new AudioAttributes();
        audioInfo.setBitRate(bitrate);
        EncodingAttributes attributes = new EncodingAttributes();
        attributes.setAudioAttributes(audioInfo);
        attributes.setFormat("mp3");
        try {
            encoder.encode(song.getFileLocation(), output, attributes);
        } catch (EncoderException e) {
            System.out.println("Failed reading ogg file. Keeping ogg format: " + song.getTitle() + " - " + song.getAuthor());
            e.printStackTrace();
            return song.getFileLocation();
        }
        return output;
    }
}
