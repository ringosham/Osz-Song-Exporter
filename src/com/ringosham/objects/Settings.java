package com.ringosham.objects;

import java.io.File;

public class Settings {

    private boolean convertOgg;
    private boolean filterPractice;
    private boolean overwrite;
    private boolean applyTags;
    private boolean overrideTags;
    private boolean fixEncoding;
    private boolean renameAsBeatmap;
    private boolean filterDuplicates;
    private int filterSeconds;
    private File exportDirectory;


    public Settings(boolean convertOgg, boolean filterPractice, boolean overwrite,  boolean applyTags, boolean overrideTags, boolean fixEncoding, boolean renameAsBeatmap, boolean filterDuplicates, int filterSeconds, File exportDirectory) {
        this.convertOgg = convertOgg;
        this.filterPractice = filterPractice;
        this.overwrite = overwrite;
        this.applyTags = applyTags;
        this.overrideTags = overrideTags;
        this.fixEncoding = fixEncoding;
        this.renameAsBeatmap = renameAsBeatmap;
        this.filterDuplicates = filterDuplicates;
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

    public boolean isFixEncoding() {
        return fixEncoding;
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
}
