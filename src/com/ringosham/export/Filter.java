package com.ringosham.export;

import com.ringosham.objects.Song;

import java.util.HashMap;
import java.util.Iterator;
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

        //Filter practice maps - Any beatmaps that are titled stream practice and jump practice
        if (filterPractice) {
            songList.removeIf(song ->  {
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
            for (Song song : songList)
                if (song.getDuration() < farmSeconds)
                    songList.remove(song);
        }

        //Filter duplicates based on the length of the file
        if (filterDuplicates) {
            int j = songList.size();
            for (int i = 0; i < j; i++) {
                Iterator<Song> iterator = songList.iterator();
                for (int k = 0; k < i + 1; k++)
                    iterator.next();
                while (iterator.hasNext()) {
                    Song songA = songList.get(i);
                    Song songB = iterator.next();
                    //System.out.println(songA.getTitle() + " -------- " + songB.getTitle());
                    if (songA.getTitle().toLowerCase().trim().equals(songB.getTitle().toLowerCase().trim())) {
                        if (Math.abs(songA.getDuration() - songB.getDuration()) < filterSeconds) {
                            iterator.remove();
                        }
                        else {
                            if (songA.getDuration() > songB.getDuration())
                                songA.setFullVersion(true);
                            else
                                songB.setFullVersion(true);
                        }
                    }
                }
                j = songList.size();
            }
        }
        return songList;
    }
}
