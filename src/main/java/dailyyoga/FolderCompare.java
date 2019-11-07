package dailyyoga;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.c;

/**
 * Created by almatarm on 31/10/2019.
 */
public class FolderCompare {
    public static void main(String args[]) throws IOException {
        boolean copy = true;
        String folder1 = "/Users/almatarm/Music/iTunes/iTunes Media/Music";
        String folder2 = "/Users/almatarm/pCloud Drive/Audio";

        List<String> files1  = new ArrayList<>();
        List<String> files2  = new ArrayList<>();
        List<String> diff    = new ArrayList<>();

        Files.walk(Paths.get(folder1))
                .filter(Files::isRegularFile)
                .forEach((file) -> files1.add(file.toString().replaceAll(folder1, "")));


        Files.walk(Paths.get(folder2))
                .filter(Files::isRegularFile)
                .forEach((file) -> files2.add(file.toString().replaceAll(folder2, "")));


        files1.forEach((name) -> {
            if(!files2.contains(name)) {
                System.out.println(String.format("cp -v '%s/%s' '%s/%s'", folder1, name, folder2, name));
                diff.add(name);
            }
        });

        System.out.println(diff.size() + " Files Found!");
    }
}
