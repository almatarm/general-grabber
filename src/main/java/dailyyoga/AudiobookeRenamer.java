package dailyyoga;

import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.v2.APICID3V2Frame;
import org.smallutil.io.MP3Util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.name;

/**
 * Created by almatarm on 10/10/2019.
 */
public class AudiobookeRenamer {
    public static void main(String[] args) throws ID3Exception {
        File dir = new File("/Users/almatarm/Documents/Books2");
        int firstTrack = 13;
        File[] files = dir.listFiles();
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
                MP3Util.updateTag(mp3File.getAbsolutePath(), track, String.format("Chapter %02d", track), "Artist",
                        "AlbumX", "Books & Spoken", "", 2019, null);
            } else {
                System.out.println(name);
            }

        }
    }
}
