package com.ringosham.export;

import com.ringosham.objects.Song;
import javafx.concurrent.Task;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.gagravarr.vorbis.VorbisFile;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class Hasher extends Task<Song> {

    static long workDone = 0;

    private File beatmap;
    private int progressMax;
    private CountDownLatch latch;

    Hasher(File beatmap, int progressMax, CountDownLatch latch) {
        this.beatmap = beatmap;
        this.progressMax = progressMax;
        this.latch = latch;
    }

    @Override
    protected Song call() {
        Song song = null;
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
                        hash = String.format("%064x", new BigInteger(1, md5.digest()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                reader.close();

                //Determining song length
                if (fileLocation.getName().endsWith(".mp3")) {
                    Media mediaFile = new Media(fileLocation.toURI().toString());
                    MediaPlayer player = new MediaPlayer(mediaFile);
                    while (player.getStatus() != MediaPlayer.Status.READY)
                        Thread.sleep(1);
                    duration = (long) mediaFile.getDuration().toSeconds();
                    song = new Song(hash, fileLocation, title, author, duration, null, unicodeTitle, unicodeAuthor, false);
                } else {
                    VorbisFile vorbisFile = new VorbisFile(fileLocation);
                    duration = fileLocation.length() / vorbisFile.getInfo().getBitrateNominal();
                    song = new Song(hash, fileLocation, title, author, duration, null, unicodeTitle, unicodeAuthor, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            workDone++;
            updateProgress(workDone, progressMax);
        }
        latch.countDown();
        return song;
    }
}
