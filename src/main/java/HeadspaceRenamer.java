import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by almatarm on 10/10/2019.
 */
public class HeadspaceRenamer {
    public static void main(String[] args) {
        File dir = new File("/Volumes/SOLARIS/Audio/Headspace/Pro Level/II");
        File[] files = dir.listFiles();
        for(File file : files) {

//            "Day 2- 10 mins";
            String name = file.getName();
            String pattern = ".*day (\\d+)- (\\d+) [Mm]in.*";

            // Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(name);

            if (m.find()) {
                String name2 = String.format("%02d Headspace Pro Level II - %2d min.mp3",
                        Integer.parseInt(m.group(1).trim()),
                        Integer.parseInt(m.group(2).trim()));
                System.out.println(name + " -> " + name2);
                file.renameTo(new File(dir, name2));
            } else {
                System.out.println(name);
            }

        }
    }
}
