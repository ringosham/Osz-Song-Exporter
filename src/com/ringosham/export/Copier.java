package com.ringosham.export;

import com.ringosham.objects.Song;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Copier extends Task<Integer> {

    private int copiedCount = 0;
    private int workDone = 0;
    private Map<String, Integer> dupCount = new HashMap<>();

    private List<Song> songList;
    private boolean renameAsBeatmap;
    private boolean overwrite;
    private File exportDirectory;
    private boolean filterDuplicates;

    Copier(List<Song> songList, boolean renameAsBeatmap, boolean overwrite, File exportDirectory, boolean filterDuplicates) {
        this.songList = songList;
        this.renameAsBeatmap = renameAsBeatmap;
        this.overwrite = overwrite;
        this.exportDirectory = exportDirectory;
        this.filterDuplicates = filterDuplicates;
    }

    @Override
    protected Integer call() {
        for (Song song : songList) {
            String filename;
            if (renameAsBeatmap) {
                if (song.getUnicodeTitle() != null && song.getUnicodeAuthor() != null)
                    filename = song.getUnicodeTitle() + " - " + song.getUnicodeAuthor();
                else
                    filename = song.getTitle() + " - " + song.getAuthor();
                if (song.isFullVersion())
                    filename = filename + " (Full version)";
                //If filter duplicates is turned off, just rename with (1),(2),(3),etc.. suffix
                if (!filterDuplicates) {
                    if (dupCount.containsKey(song.getTitle())) {
                        dupCount.put(song.getTitle(), dupCount.get(song.getTitle()) + 1);
                        filename = filename + " (" + dupCount.get(song.getTitle()) + ")";
                    }
                    dupCount.put(song.getTitle(), 0);
                }
            }
            else
                filename = song.getBeatmapID();
            if (song.getFileLocation().getName().toLowerCase().endsWith(".mp3"))
                filename = filename + ".mp3";
            else
                filename = filename + ".ogg";
            File outputFile = new File(exportDirectory.getAbsolutePath() + "/" + filename);
            try {
                if (overwrite)
                    Files.copy(song.getFileLocation().toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                else if (outputFile.length() != song.getFileLocation().length())
                    Files.copy(song.getFileLocation().toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                copiedCount++;
            } catch (IOException e) {
                System.out.println("Failed copying " + song.getTitle() + " - " + song.getAuthor());
                Exporter.failCount++;
                e.printStackTrace();
            }
            updateProgress(workDone++, songList.size());
        }
        return copiedCount;
    }
}
