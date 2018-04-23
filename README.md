#Osu! Exporter
A simple utility for exporting all songs from your osu! library

Do not use this program for distributing songs illegally. I, the creator of this program, am not responsible for such actions performed by the user of the program. 
##Features
* Proper file renaming (or rename it after beatmap ID if you really want to)
* Adding MP3 tags based on beatmap info
* Filter practice songs
* **Filter any similar songs with the same names to prevent duplicates** (Extremely useful for saving space and if you have a large amount of beatmaps) 
* Conversion from ogg to mp3 (Conversion is done via ffmpeg)
* Fixing encoding to old mp3 tags (Very old mp3s don't use UTF-8, which is the song shows up as "????" in your music player)

The above features are fully configurable!
##Compiling
Just compile it with Intellij. That's it.
##License
This program is under Apache license 2.0
##Dependencies
[Jave](http://www.sauronsoftware.it/projects/jave/index.php) - Wrapper for ffmpeg - Under GPL v3 license

[VorbisJava](https://github.com/Gagravarr/VorbisJava) - Ogg and vorbis tools for Java - Under Apache license 2.0