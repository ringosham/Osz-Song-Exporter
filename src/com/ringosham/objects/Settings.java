package com.ringosham.objects;

import java.io.File;

public class Settings {

    private final boolean convertOgg;
    private final boolean filterPractice;
    private final boolean overwrite;
    private final boolean applyTags;
    private final boolean overrideTags;
    private final boolean renameAsBeatmap;
    private final boolean filterDuplicates;
    private final boolean romajiNaming;
    private final boolean mirrorOutput;
    private final boolean filterFarm;
    private final int farmSeconds;
    private final int filterSeconds;
    private final File exportDirectory;


    public Settings(boolean convertOgg, boolean filterPractice, boolean overwrite, boolean applyTags, boolean overrideTags, boolean renameAsBeatmap, boolean romajiNaming, boolean filterDuplicates, boolean mirrorOutput, boolean filterFarm, int farmSeconds, int filterSeconds,  File exportDirectory) {
        this.convertOgg = convertOgg;
        this.filterPractice = filterPractice;
        this.overwrite = overwrite;
        this.applyTags = applyTags;
        this.overrideTags = overrideTags;
        this.renameAsBeatmap = renameAsBeatmap;
        this.filterDuplicates = filterDuplicates;
        this.romajiNaming = romajiNaming;
        this.mirrorOutput = mirrorOutput;
        this.filterFarm = filterFarm;
        this.farmSeconds = farmSeconds;
        this.filterSeconds = filterSeconds;
        this.exportDirectory = exportDirectory;
    }

    public boolean isConvertOgg() {
        return convertOgg;
    }

    public boolean isFilterPractice() {
        return filterPractice;
    }

    public boolean isApplyTags() {
        return applyTags;
    }

    public boolean isOverrideTags() {
        return overrideTags;
    }

    public boolean isRenameAsBeatmap() {
        return renameAsBeatmap;
    }

    public boolean isFilterDuplicates() {
        return filterDuplicates;
    }

    public int getFilterSeconds() {
        return filterSeconds;
    }

    public File getExportDirectory() {
        return exportDirectory;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public boolean isRomajiNaming() {
        return romajiNaming;
    }

    public boolean isMirrorOutput() {
        return mirrorOutput;
    }

    public boolean isFilterFarm() {
        return filterFarm;
    }

    public int getFarmSeconds() {
        return farmSeconds;
    }
}
