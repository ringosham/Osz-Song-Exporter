package com.ringosham.objects;

import java.io.File;

public record Settings(boolean convertOgg, boolean filterPractice, boolean overwrite, boolean applyTags,
                       boolean overrideTags, boolean renameAsBeatmap, boolean romajiNaming, boolean filterDuplicates,
                       boolean mirrorOutput, boolean filterFarm, int farmSeconds, int filterSeconds,
                       File exportDirectory) {

}
