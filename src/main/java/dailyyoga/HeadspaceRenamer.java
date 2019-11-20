package dailyyoga;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by almatarm on 10/10/2019.
 */
public class HeadspaceRenamer {
    public static void main(String[] args) {
        File dir = new File("/Users/almatarm/Downloads/headspace/Headspace/Basics/I");
        File[] files = dir.listFiles();
        for(File file : files) {

            String name = file.getName();
            String pattern =
                    ".*s(\\d+)-(\\d\\d)m-.*";

            // Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(name);

            if (m.find()) {
                int track =  Integer.parseInt(m.group(1).trim());
                int min = Integer.parseInt(m.group(2).trim());
                String name2 = String.format("%02d Sleep Health - %2d min.mp3",
                        track, min);
//                name2 = String.format("%02d %s", track, name.substring(3));
//                System.out.println(name + " -> " + name2);
//                file.renameTo(new File(dir, name2));
            } else {
                System.out.println(name);
            }

        }
    }
}
