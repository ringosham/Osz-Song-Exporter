package com.ringosham.export;

import com.ringosham.controllers.Controller;
import com.ringosham.objects.Song;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.gagravarr.vorbis.VorbisFile;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Hasher {

    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();

    ReadOnlyDoubleProperty progressProperty() {
        return progress;
    }

    List<Song> start() {
        long workDone = 0;
        List<Song> song = new LinkedList<>();
        long progressMax = Controller.beatmapDir.listFiles(File::isDirectory).length;
        for (File beatmap : Controller.beatmapDir.listFiles(File::isDirectory)) {
            //We only need to look for one single beatmap file. This doesn't need to loop
            for (File osuFile : Objects.requireNonNull(beatmap.listFiles(pathname -> pathname.getName().endsWith(".osu")))) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(osuFile));
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
                            fileLocation = new File(beatmap.getAbsolutePath() + "/" + line.replace("AudioFilename: ", ""));
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
                        if (line.matches("0,0,\"(.+)\"")) {
                            Pattern pattern = Pattern.compile("0,0,\"(.+)\"");
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
                    if (fileLocation.getName().toLowerCase().endsWith(".mp3")) {
                        Media mediaFile = new Media(fileLocation.toURI().toString());
                        MediaPlayer player = new MediaPlayer(mediaFile);
                        String finalTitle = title;
                        String finalAuthor = author;
                        player.setOnError(() -> {
                            Exporter.failCount++;
                            System.out.println("Failed reading MP3: " + finalTitle + " - " + finalAuthor);
                            player.getError().printStackTrace();
                            player.dispose();
                        });
                        while (player.getStatus() == MediaPlayer.Status.UNKNOWN)
                            Thread.sleep(1);
                        if (player.getStatus() != MediaPlayer.Status.DISPOSED)
                            duration = (long) mediaFile.getDuration().toSeconds();
                        else
                            break;
                        song.add(new Song(hash, fileLocation, title, author, duration, unicodeTitle, unicodeAuthor, albumArt, false));
                    } else {
                        VorbisFile vorbisFile = new VorbisFile(fileLocation);
                        //Nominal bitrate is not accurate enough. About 10 seconds in error
                        duration = (fileLocation.length() * 8) / vorbisFile.getInfo().getBitrateNominal();
                        song.add(new Song(hash, fileLocation, title, author, duration, unicodeTitle, unicodeAuthor, albumArt, true));
                    }
                } catch (Exception e) {
                    System.out.println("Failed reading beatmap: " + beatmap.getName());
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
