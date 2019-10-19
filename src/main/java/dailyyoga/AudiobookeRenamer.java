package dailyyoga;

import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.v2.APICID3V2Frame;
import org.smallutil.io.MP3Util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by almatarm on 10/10/2019.
 */
public class AudiobookeRenamer {
    public static void main(String[] args) throws ID3Exception {
        File dir = new File("/Users/almatarm/Documents/Book");
        File[] files = dir.listFiles();
        for(File file : files) {

            String name = file.getName();
            String pattern = "(\\d\\d).*";
            // Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(name);

            if (m.find()) {
                int track =  Integer.parseInt(m.group(1).trim());
                String name2 = String.format("%02d Chapter %02d.mp3",
                        track, track);
                System.out.println(name + " -> " + name2);

                File mp3File = new File(dir, name2);
                file.renameTo(mp3File);
                MP3Util.updateTag(mp3File.getAbsolutePath(), track, String.format("Chapter %02d", track), "Artist",
                        "AlbumX", "Books & Spoken", "", 2019, null);
            } else {
                System.out.println(name);
            }

        }
    }
}
