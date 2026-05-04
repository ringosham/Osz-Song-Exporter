package com.ringosham.export;

import com.ringosham.objects.Song;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Filter {

    private final boolean filterFarm;
    private final int farmSeconds;
    private final boolean filterPractice;
    private final boolean filterDuplicates;
    private final int filterSeconds;
    private List<Song> songList;

    public Filter(List<Song> songList, boolean filterPractice, boolean filterDuplicates, int filterSeconds, boolean filterFarm, int farmSeconds) {
        this.songList = songList;
        this.filterFarm = filterFarm;
        this.farmSeconds = farmSeconds;
        this.songList.addAll(songList);
        this.filterPractice = filterPractice;
        this.filterDuplicates = filterDuplicates;
        this.filterSeconds = filterSeconds;
    }

    List<Song> start() {
        //Remove exact duplicates based on hash. The easiest method would be a HashMap
        Map<String, Song> songMap = new HashMap<>();
        for (Song song : songList)
            songMap.putIfAbsent(song.getHash(), song);
        songList.clear();
        songList.addAll(songMap.values());

        //Filter duplicates based on the length of the file
        if (filterDuplicates) {
            Map<String, List<Song>> titleToSongs = new HashMap<>();
            for (Song song : songList) {
                String key = song.getTitle().toLowerCase().trim();
                titleToSongs.computeIfAbsent(key, k -> new ArrayList<>()).add(song);
            }
            List<Song> result = new ArrayList<>();

            for (List<Song> group : titleToSongs.values()) {
                if (group.size() == 1) {
                    result.add(group.get(0));
                } else {
                    group.sort(Comparator.comparingLong(Song::getDuration));
                    List<Song> filtered = new ArrayList<>();
                    filtered.add(group.get(0));
                    for (int i = 1; i < group.size(); i++) {
                        if (group.get(i).getDuration() - filtered.get(filtered.size() - 1).getDuration() >= filterSeconds) {
                            filtered.add(group.get(i));
                        }
                    }
                    if (filtered.size() > 1) {
                        filtered.get(filtered.size() - 1).setFullVersion(true);
                    }
                    result.addAll(filtered);
                }
            }

            songList.clear();
            songList.addAll(result);
        }

        //Filter practice maps - Any beatmaps that are titled stream practice and jump practice
        if (filterPractice) {
            songList.removeIf(song -> {
                String title = song.getTitle().trim().toLowerCase();
                String unicodeTitle = null;
                if (song.getUnicodeTitle() != null)
                    unicodeTitle = song.getUnicodeTitle().trim().toLowerCase();
                String[] filters = {"stream practice", "stream practise", "jump practice", "jump practise"};
                for (String filter : filters) {
                    if (title.contains(filter))
                        return true;
                    if (unicodeTitle != null)
                        if (unicodeTitle.contains(filter))
                            return true;
                }
                return false;
            });
        }

        //Filter based on song length
        if (filterFarm) {
            songList.removeIf(song -> song.getDuration() < farmSeconds);
        }


        return songList;
    }
}
