package dailyyoga;

import com.almatarm.lego.common.CharUtil;
import org.bouncycastle.pqc.math.linearalgebra.CharUtils;

import java.io.File;
import java.util.ArrayList;

import static com.mycompany.screencapture.Books.names;
import static org.bouncycastle.asn1.x500.style.RFC4519Style.l;

/**
 * Created by almatarm on 01/11/2019.
 */
public class YogaMp4Renamer {
    public static void main(String[] args) {
        File src = new File("/Volumes/Media/Daily Yoga/untitled folder/Advanced Yoga Pose Workshop | Dashama Konah");

        String data = "DAY 1\nLeg Behind the Head  \n" +
                "8 min  69 Kcal  \n" +
                "DAY 2  \n" +
                "Firefly Pose  \n" +
                "4 min  34 Kcal  \n" +
                "DAY 3  \n" +
                "Upward Bow Pose  \n" +
                "6 min  51 Kcal  \n" +
                "DAY 4  \n" +
                "Ultimate King Dancer  \n" +
                "6 min  51 Kcal\n" +
                "DAY 5  \n" +
                "Dwi Pada Koundinyasana + Eka Pada Koundinyasana  \n" +
                "4 min  34 Kcal  \n" +
                "DAY 6  \n" +
                "One-Legged Pigeon Pose  \n" +
                "6 min  51 Kcal  \n" +
                "DAY 7  \n" +
                "Monkey Pose  \n" +
                "8 min  69 Kcal  \n" +
                "DAY 8  \n" +
                "Yoga Chin Balance  \n" +
                "5 min  43 Kcal  \n" +
                "DAY 9  \n" +
                "Forearm Balance & Forearm Stand  \n" +
                "7 min  43 Kcal\n" +
                "DAY 10  \n" +
                "Headstand & Tripod Headstand & Twisted Headstand  \n" +
                "10 min  86 Kcal  \n" +
                "DAY 11  \n" +
                "Full Wheel  \n" +
                "6 min  51 Kcal\n" +
                "DAY 1  \n" +
                "Visualization Meditation Daily Practice  \n" +
                "11 min  95 Kcal ";

        String[] lines = data.split("\n");
        ArrayList<String> names = new ArrayList<>();
        String name = "";
        String txt = "";
        int lineCount = 0;
        for(int i = 0; i < lines.length; i++) {
            if(i % 3 == 0) {
                if(!name.isEmpty()) names.add(name);
                name = "";
                lineCount = 1;
            }
            if(lineCount == 1) {
                txt = lines[i].replaceAll("DAY", "").trim();
                name += String.format("%02d ", Integer.parseInt(txt));
            } else if(lineCount == 2) {
                name += lines[i].trim() + " | ";
            } else if(lineCount == 3) {
                txt = lines[i].replaceAll("min ", "min |");
                name += txt.trim() + ".mp4";
            }
            lineCount++;
        }
        names.add(name);


        File[] files = src.listFiles();
        System.out.println(String.format("cd '%s'", src));
        for(int i = 0; i < files.length; i++) {
            System.out.println(String.format("mv %s '%s'", files[i].getName(), names.get(i)));
        }


    }
}

