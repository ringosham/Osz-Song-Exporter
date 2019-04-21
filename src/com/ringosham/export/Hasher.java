package com.ringosham.export;

import com.ringosham.controllers.Controller;
import com.ringosham.objects.Song;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.MultimediaInfo;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Hasher {

    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();
    private final ReadOnlyStringWrapper console = new ReadOnlyStringWrapper();

    ReadOnlyDoubleProperty progressProperty() {
        return progress;
    }
    ReadOnlyStringProperty consoleProperty() {
        return console;
    }

    List<Song> start() {
        long workDone = 0;
        List<Song> song = new LinkedList<>();
        long progressMax = Controller.beatmapDir.listFiles(File::isDirectory).length;
        for (File beatmap : Controller.beatmapDir.listFiles(File::isDirectory)) {
            //We only need to look for one single beatmap file. This doesn't need to loop
            for (File osuFile : Objects.requireNonNull(beatmap.listFiles(pathname -> pathname.getName().endsWith(".osu")))) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(osuFile), StandardCharsets.UTF_8));
                    String line;
                    String hash = null;
                    File fileLocation = null;
                    String title = null;
                    String author = null;
                    String unicodeTitle = null;
                    String unicodeAuthor = null;
                    File albumArt = null;
                    long duration;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("AudioFilename: "))
                            fileLocation = new File(beatmap, line.replace("AudioFilename: ", ""));
                        if (line.startsWith("AudioHash: "))
                            hash = line.replace("AudioHash: ", "");
                        if (line.startsWith("Title:"))
                            title = line.replace("Title:", "");
                        if (line.startsWith("Artist:"))
                            author = line.replace("Artist:", "");
                        if (line.startsWith("TitleUnicode:"))
                            unicodeTitle = line.replace("TitleUnicode:", "");
                        if (line.startsWith("ArtistUnicode:"))
                            unicodeAuthor = line.replace("ArtistUnicode:", "");
                        if (line.toLowerCase().matches("\\d,\\d,\"(.+(jpg|png|bmp))\"") ||
                                line.toLowerCase().matches("\\d,\\d,\"(.+(jpg|png|bmp))\",\\d,\\d")) {
                            Pattern pattern = Pattern.compile("\\d,\\d,\"(.+)\"");
                            Matcher matcher = pattern.matcher(line);
                            if (matcher.find())
                                albumArt = new File(beatmap.getAbsolutePath() + "/" + matcher.group(1));
                            break;
                        }
                    }
                    //Hashing is mandatory to remove completely identical songs
                    if (hash == null) {
                        FileInputStream stream;
                        //Old osu maps uses md5 to store the hash, but new ones don't
                        final MessageDigest md5 = MessageDigest.getInstance("MD5");
                        final byte[] buffer = new byte[1024 * 1024];
                        try {
                            stream = new FileInputStream(fileLocation);
                            int byteRead;
                            while ((byteRead = stream.read(buffer)) >= 0)
                                if (byteRead > 0)
                                    md5.update(buffer, 0, byteRead);
                            hash = String.format("%032x", new BigInteger(1, md5.digest()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    reader.close();
                    //Determining song length
                    try {
                        Encoder encoder = new Encoder();
                        MultimediaInfo info = encoder.getInfo(fileLocation);
                        duration = info.getDuration() / 1000;
                        song.add(new Song(hash, fileLocation, title, author, duration, unicodeTitle, unicodeAuthor, albumArt, fileLocation.getName().toLowerCase().endsWith(".ogg")));
                    } catch (EncoderException e) {
                        Exporter.failCount++;
                        console.set("Failed reading MP3: " + title + " - " + author);
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    console.set("Failed reading beatmap: " + beatmap.getName());
                    e.printStackTrace();
                }
                break;
            }
            workDone++;
            double progressDouble = ((double) workDone) / progressMax;
            progress.set(progressDouble);
        }
        return song;
    }
}
