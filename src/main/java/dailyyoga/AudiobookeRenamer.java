package dailyyoga;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.smallutil.io.MP3Util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by almatarm on 10/10/2019.
 */
public class AudiobookeRenamer {
    public static void main(String[] args) throws Exception {
        File metadata = new File("/Users/almatarm/Documents/5.mp3");
        MP3File metadataMp3 = (MP3File) AudioFileIO.read(metadata);
        Tag tag = metadataMp3.getTag();

//        if(true) return;

        File dir = new File("/Users/almatarm/Documents/Book");
        int firstTrack = 1;
        File[] files = dir.listFiles();
        int totalTracks = dir.listFiles(((dir1, name) -> name.endsWith("mp3"))).length;

        for(File file : files) {
            if(file.getName().equals(".DS_Store")) {
                System.out.println("Deleting .DS_STORE");
                file.delete();
                continue;
            }
            String name = file.getName();
            String pattern = "(\\d+).*";
            // Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(name);

            if (m.find()) {
                int track =  Integer.parseInt(m.group(1).trim()) + (firstTrack - 1);
                String name2 = String.format("%03d Chapter %03d.mp3",
                        track, track);
                System.out.println(name + " -> " + name2);

                File mp3File = new File(dir, name2);
                file.renameTo(mp3File);

                if(tag == null) {
                    MP3Util.updateTag(mp3File.getAbsolutePath(), track, String.format("Chapter %02d", track), "Artist",
                            "AlbumX", "Books & Spoken", "", 2019, null);
                } else {
                    AudioFile f = AudioFileIO.read(mp3File);
                    tag.setField(FieldKey.TITLE, String.format("Chapter %03d", track));
                    tag.setField(FieldKey.TRACK, track + "");
                    tag.setField(FieldKey.TRACK_TOTAL, totalTracks + "");
                    f.setTag(tag);
                    f.commit();
                }
            } else {
                System.out.println(name);
            }

        }
    }
}
