package dailyyoga;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by almatarm on 26/02/2020.
 */
public class RemoveDublicateAudio {
    public static void main(String[] args) throws IOException {
        File dir = new File("/Volumes/almatarm/Audio/Audiobooks");
        File[] authors = dir.listFiles(pathname -> pathname.isDirectory());

        int a = 0;
        for(File author: authors) {
            Map<String, ArrayList<File>> booksMap = new HashMap<>();

            File[] books = author.listFiles(pathname -> pathname.isDirectory());

            for(File book: books) {
                int max = -1;
                for(File mp3 : book.listFiles()) {
                    int trackNo = getTrack(mp3);
                    max = max < trackNo? trackNo : max;
                }
                if(max < book.listFiles().length) {
                    System.out.println("Max = " + max + ", " + book.listFiles().length + "\t" + book.getAbsolutePath());
                }

            }

//            if(a++ == 5) break;
//            for(File book: books) {
//                String key = book.getName().replaceAll(" \\(Unabridged\\)", "");
//
//                if(!booksMap.containsKey(key)) booksMap.put(key, new ArrayList<File>());
//                booksMap.get(key).add(book);
//            }
//
//            for(String book : booksMap.keySet()) {
//                if(booksMap.get(book).size() > 1) {
//                    for(File b : booksMap.get(book)) {
//                        System.out.println(b.list().length + "\t" + b.getAbsolutePath());
//                        if(b.getName().contains(" (Unabridged)") &&
//                                booksMap.get(book).get(0).list().length == booksMap.get(book).get(0).list().length) {
//                            System.out.println("rm " + b.getAbsolutePath());
//                            FileUtils.deleteDirectory(b);
//                        }
//
//                    }
//                }
//            }


        }
    }

    private static int getTrack(File file) {
        try {
            if (file.getAbsolutePath().endsWith(".mp3")) {
                String name = file.getName().substring(0, file.getName().length() - 4);
                String trackStr = name.substring(name.lastIndexOf(" ")).trim();
                //            System.out.println(trackStr);
                return Integer.parseInt(trackStr);
            }
        } catch (Exception e) {}
        return -1;
    }
}
