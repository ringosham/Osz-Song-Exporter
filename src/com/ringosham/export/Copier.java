package com.ringosham.export;

import com.ringosham.objects.Song;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Copier {

    private final ReadOnlyStringWrapper progressText = new ReadOnlyStringWrapper();
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();
    private ReadOnlyStringWrapper console = new ReadOnlyStringWrapper();

    private int copiedCount = 0;
    private int workDone = 0;
    private Map<String, Integer> dupCount = new HashMap<>();

    private List<Song> songList;
    private boolean renameAsBeatmap;
    private boolean overwrite;
    private File exportDirectory;
    private boolean filterDuplicates;
    
    ReadOnlyStringProperty consoleProperty() {
        return console;
    }
    Copier(List<Song> songList, boolean renameAsBeatmap, boolean overwrite, File exportDirectory, boolean filterDuplicates) {
        this.songList = songList;
        this.renameAsBeatmap = renameAsBeatmap;
        this.overwrite = overwrite;
        this.exportDirectory = exportDirectory;
        this.filterDuplicates = filterDuplicates;
    }

    int start() {
        for (Song song : songList) {
            String filename;
            if (renameAsBeatmap) {
                if (song.getUnicodeTitle() != null && song.getUnicodeAuthor() != null)
                    if (song.getUnicodeTitle().isEmpty() && song.getUnicodeAuthor().isEmpty())
                        filename = song.getTitle() + " - " + song.getAuthor();
                    else
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
                filename = getValidFileName(filename);
            }
            else
                filename = song.getBeatmapID();
            progressText.set("Copying " + filename);
            if (song.getFileLocation().getName().toLowerCase().endsWith(".mp3"))
                filename = filename + ".mp3";
            else
                filename = filename + ".ogg";
            File outputFile = new File(exportDirectory.getAbsolutePath(), filename);
            try {
                if (overwrite)
                    Files.copy(song.getFileLocation().toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                else if (outputFile.length() != song.getFileLocation().length())
                    Files.copy(song.getFileLocation().toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                copiedCount++;
                song.setOutputFile(outputFile);
            } catch (IOException e) {
                console.set("Failed copying " + song.getTitle() + " - " + song.getAuthor());
                Exporter.failCount++;
                e.printStackTrace();
            }
            workDone++;
            double progressDouble = ((double) workDone) / songList.size();
            progress.set(progressDouble);
        }
        return copiedCount;
    }

    //Remove any illegal characters in the file name
    private String getValidFileName(String name) {
        return name.replaceAll("\\*", "").replaceAll("<", "").replaceAll(">", "")
                .replaceAll("\\|", "").replaceAll("\\?", "").replaceAll(":", "")
                .replaceAll("\"", "").replaceAll("\\\\",",").replaceAll("/", ",");
    }

    ReadOnlyStringProperty progressTextProperty() {
        return progressText;
    }

    ReadOnlyDoubleProperty progressProperty() {
        return progress;
    }
}
