package com.ringosham.export;

import com.ringosham.objects.Song;

import java.util.*;

public class Filter {

    private List<Song> songList = new LinkedList<>();
    private boolean filterPractice;
    private boolean filterDuplicates;
    private int filterSeconds;

    public Filter(List<Song> songList, boolean filterPractice, boolean filterDuplicates, int filterSeconds) {
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
        for (String key : songMap.keySet())
            songList.add(songMap.get(key));

        //Filter practice maps - Any beatmaps that are titled stream practice and jump practice
        if (filterPractice) {
            Iterator<Song> iterator = songList.iterator();
            Song song;
            while ((song = iterator.next()) != null)
                if (song.getTitle().toLowerCase().contains("stream practice") || song.getTitle().toLowerCase().contains("stream practise") ||
                        song.getUnicodeTitle().toLowerCase().contains("stream practice") || song.getUnicodeTitle().toLowerCase().contains("stream practise") ||
                        song.getTitle().toLowerCase().contains("jump practice") || song.getTitle().toLowerCase().contains("jump practise") ||
                        song.getUnicodeTitle().toLowerCase().contains("jump practice") || song.getUnicodeTitle().toLowerCase().contains("jump practise"))
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
