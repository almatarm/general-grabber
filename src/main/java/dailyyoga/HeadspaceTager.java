package dailyyoga;

import org.blinkenlights.jid3.ID3Exception;
import org.smallutil.io.MP3Util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by almatarm on 10/10/2019.
 */
public class HeadspaceTager {
    public static void main(String[] args) throws ID3Exception {
        File dir = new File("/Users/almatarm/Downloads/headspace/Headspace/Sleep Health");
        File[] files = dir.listFiles();
        for(File mp3File : files) {

            String name = mp3File.getName();
            String pattern = "(\\d+).*\\s(\\d+)\\smin.mp3";
            // Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(name);

            if (m.find()) {
                int track =  Integer.parseInt(m.group(1).trim());
                int min = Integer.parseInt(m.group(2).trim());
                String title = "Sleep Health";
                String name2 = String.format("%03d %s - %d min.mp3",
                        track, title, min);
                System.out.println(name + " -> " + name2);

                System.out.println("--> " + track);
                System.out.println("--> " + min);
                System.out.println(name2.substring(0, name2.length() -4));
                File mp3File2 = new File(dir, name2);
                mp3File.renameTo(mp3File2);
                MP3Util.updateTag(mp3File2.getAbsolutePath(), track, name2.substring(0, name2.length() -4),
                        "Headspace", title, "Mindfulness",
                        "A range of guided meditations and exercises, developed in partnership with leading sleep scientists and designed to promote sleep health.",
                        2019, null);
            } else {
                System.out.println(name);
            }

        }
    }
}
