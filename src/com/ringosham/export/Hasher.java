package com.ringosham.export;

import com.ringosham.controllers.Controller;
import com.ringosham.objects.Song;
import javafx.concurrent.Task;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.gagravarr.vorbis.VorbisFile;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Hasher extends Task<List<Song>> {

    @Override
    protected List<Song> call() {
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
                    long duration = 0;
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
                    }

                    //Hashing is mandatory to remove completely identical songs
                    if (hash == null) {
                        FileInputStream stream;
                        //Old osu maps uses md5 as well
                        final MessageDigest md5 = MessageDigest.getInstance("MD5");
                        final byte[] buffer = new byte[1024 * 1024];
                        try {
                            stream = new FileInputStream(fileLocation);
                            int byteRead = 0;
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
                            Exporter.failSongs.add(finalTitle + " - " + finalAuthor);
                            player.getError().printStackTrace();
                            player.dispose();
                        });
                        while (player.getStatus() == MediaPlayer.Status.UNKNOWN)
                            Thread.sleep(1);
                        if (player.getStatus() != MediaPlayer.Status.DISPOSED)
                            duration = (long) mediaFile.getDuration().toSeconds();
                        else
                            break;
                        song.add(new Song(hash, fileLocation, title, author, duration, null, unicodeTitle, unicodeAuthor, false));
                    } else {
                        VorbisFile vorbisFile = new VorbisFile(fileLocation);
                        duration = fileLocation.length() / vorbisFile.getInfo().getBitrateNominal();
                        song.add(new Song(hash, fileLocation, title, author, duration, null, unicodeTitle, unicodeAuthor, true));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            workDone++;
            updateProgress(workDone, progressMax);
        }
        System.out.println("Analysing complete");
        return song;
    }
}
