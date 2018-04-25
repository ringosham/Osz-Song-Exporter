#Osu! Exporter
A simple utility for exporting all songs from your osu! library.

I find it funny that most of these utility software I found in Github either don't work or lacking in features. So I decided to make one myself!
##Disclaimer
This program simply copy, as well as converting songs already stored the user's computer. The copied and converted files are stored in the user's device only. These songs are intended for personal use only. The user, however, are allow to do anything with the files under the confines of copyright law. The user is the only one liable for any copyright infringing actions.

This program is also not affiliated to the rhythm game "osu!".

In short,

Do not use this program for distributing songs illegally. The creator of this program will not be responsible for such actions performed by the user of the program. 
##Features
* Proper file renaming (or rename it after beatmap ID if you really want to)
* Adding MP3 tags based on beatmap info
* Filter practice songs
* **Filter any similar songs with the same names to prevent duplicates** (Extremely useful for saving space and if you have a large amount of beatmaps) 
* Conversion from ogg to mp3 (Conversion is done via FFmpeg, included in the app)
* Fixing encoding to old mp3 tags (Very old mp3s don't use UTF-8, which is the song shows up as "????" in your music player)

The above features are fully configurable!
##Compiling
Just compile it with Intellij. That's it.
##Things to note
The FFmpeg binary is only 32-bit in Linux. Please make sure you have the dependencies for conversion.
##License
This program is under Apache license 2.0
##Dependencies
[Jave](http://www.sauronsoftware.it/projects/jave/index.php) - Java Audio Video Encoder, also a wrapper for FFmpeg - Under GPL v3 license

[VorbisJava](https://github.com/Gagravarr/VorbisJava) - Ogg and vorbis tools for Java - Under Apache license 2.0

[FFmpeg project](https://ffmpeg.org) - FFmprg project - Cross platform record, stream, convert video and audio utility - [FFmpeg license](https://ffmpeg.org/legal.html)