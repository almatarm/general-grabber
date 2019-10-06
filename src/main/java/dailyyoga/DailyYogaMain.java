package dailyyoga;

import com.mycompany.screencapture.Helper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

import static org.jsoup.nodes.Entities.EscapeMode.base;

/**
 * Created by almatarm on 03/10/2019.
 */
public class DailyYogaMain {
    public static void main(String[] args) throws IOException {
        StringBuilder main = new StringBuilder();


        main.append("#/bin/bash\n");

        File out = new File("/Users/almatarm/dailyyoga");
        out.mkdirs();

        File root = new File("/Users/almatarm/.dailyyoga/plugs/en_");
        File[] folders = root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith("com") && pathname.isDirectory();
            }
        });
        for(int i = 0; i < folders.length; i++) {
            StringBuilder buff = new StringBuilder();
            StringBuilder ccBuff = new StringBuilder();

            File base = folders[i];

            System.out.println(base.getAbsolutePath());
            Map<String, String> stringStringMap = stringKeys(base);
            if(stringStringMap.isEmpty()) continue;
            System.out.println("===================================");
            for (String key : stringStringMap.keySet()) {
                System.out.println(key + ": " + stringStringMap.get(key));
            }
            String output = stringStringMap.get("plugName");
            output += append("session_info_instructor", stringStringMap);
            output += append("session_info_class_level", stringStringMap);
            output += append("session_info_goal", stringStringMap);
            output +=  " - " + stringStringMap.get("DURATION");

            System.out.println("===================================");
            List<Act> actList = actList(base);
            buff.append("#/bin/bash\n");
            buff.append("mkdir -p video\n");
//            buff.append("ffmpeg -f lavfi -i color=white:s=1280x720:r=24000/1001 -f lavfi -i anullsrc -ar 48000 -ac 2 -t 3 video/empty.mp4\n");
            for(Act act: actList) {
                System.out.println(stringStringMap.get(act.getTitleString()) + "\t" + act);
                String fileName = stringStringMap.get(act.getTitleString());
//                buff.append(String.format("ffmpeg -i %s -vf scale=960:540 output.mp4%n", act.getPlayFile()));
//                buff.append(String.format("mv -f output.mp4 %s%n", act.getPlayFile()));
                buff.append(String.format("ffmpeg -i %s -itsoffset %s -i %s -c:v copy -c:a aac -sstrict experimental 'video/%s.mp4'%n",
                        act.getPlayFile(), getOffset(act.getStartTime()), act.getAudioFile(), fileName));
                if(ccBuff.length() == 0) {
                    ccBuff.append(String.format("ffmpeg -y \\\n" +
                            "  -i 'video/%s.mp4' \\\n", fileName ));
                } else {
                    ccBuff.append(String.format("  -i 'video/%s.mp4' \\\n", fileName ));
                }
            }
            ccBuff.append(String.format("  -filter_complex \"concat=n=%d:v=1:a=1 [outv][outa]\" -map \"[outv]\" -map \"[outa]\" '%s.mp4' \n",
                    actList.size(), output ));
            buff.append(ccBuff);
            File compile = new File(base, "compile");
            Helper.iFile.write(compile.getAbsolutePath(), buff.toString());
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxr--r--");
            Files.setPosixFilePermissions(compile.toPath(), permissions);

            main.append(String.format("# %s%n", base.getName()));
            main.append(String.format("echo %s%n", base.getAbsolutePath()));
            main.append(String.format("cd %s%n", base.getAbsolutePath()));
            main.append(String.format("rm -rf %s%n", base.getAbsolutePath() + "/video"));
            main.append(String.format("%s%n", base.getAbsolutePath() + "/compile"));
            main.append(String.format("cp '%s.mp4' %s%n", output,  out.getAbsolutePath()));
            main.append(String.format("rm -rf %s%n", base.getAbsolutePath() + "/video"));
            main.append("cd ..\n\n");
            System.out.println(buff.toString());
        }

        System.out.println("===============");
        System.out.println(main.toString());


        File compile = new File(root, "compileAll");
        Helper.iFile.write(compile.getAbsolutePath(), main.toString());
        Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxr--r--");
        Files.setPosixFilePermissions(compile.toPath(), permissions);
    }

    public static Map<String, String> stringKeys(File base) {
        HashMap<String, String> keys = new HashMap<>();
        try {
            File fXmlFile = new File(base, "strings.xml");
            if(!fXmlFile.exists()) return keys;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("string");
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    keys.put(eElement.getAttribute("name"), eElement.getTextContent()
                            .replaceAll("）", "").replaceAll("\\Q)\\E", "").replaceAll("\\Q(\\E", "")
                            .replaceAll("'","").
                                    trim());
                }
            }

            nList = doc.getElementsByTagName("item");
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if(nNode.getTextContent().contains(" min")) {
                    keys.put("DURATION", nNode.getTextContent().trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keys;
    }

    public static List<Act> actList(File base) {
        List<Act> actList = new ArrayList<>();
        try {
            File fXmlFile = new File(base, "act_library.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("Act");
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String filterCategory = eElement.getAttribute("dailyYoga:filterCategory");
                    String filterLevel = eElement.getAttribute("dailyYoga:filterLevel");
                    String playTime = eElement.getAttribute("dailyYoga:playTime");
                    String playFile = eElement.getAttribute("dailyYoga:playFile").replaceAll("@","").trim();
                    String titleString = eElement.getAttribute("dailyYoga:titleString")
                            .replaceAll("@string/", "").trim();

                    String audioFile = null;
                    String startTime = null;
                    Node audios = eElement.getElementsByTagName("Audios").item(0);
                    NodeList childNodes = audios.getChildNodes();
                    for(int j = 0; j < childNodes.getLength(); j++) {
                        if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Element e = (Element) childNodes.item(j);
                            audioFile = e.getAttribute("dailyYoga:id").replaceAll("@","").trim();
                            startTime = e.getAttribute("dailyYoga:startTime");
                        }
                    }
                    actList.add(new Act(filterCategory, filterLevel, playTime, playFile, titleString, audioFile, startTime));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return actList;
    }

    public static String getOffset(String offset) {
        int iOffset = Integer.parseInt(offset);
        return String.format("00:00:%04.1f", iOffset/1000.0);
    }

    public static String append(String key, Map<String, String> stringStringMap) {
        if(stringStringMap.containsKey(key)) {
            String value = stringStringMap.get(key);
            if(value.contains(":")) value = value.substring(value.indexOf(":") + 1).trim();
            return  " - " + value;
        }
        return "";
    }
}

class Act {
    String filterCategory;
    String filterLevel;
    String playTime;
    String playFile;
    String titleString;
    String audioFile;
    String startTime;

    public Act(String filterCategory, String filterLevel, String playTime, String playFile, String titleString,
               String audioFile, String startTime) {
        this.filterCategory = filterCategory;
        this.filterLevel = filterLevel;
        this.playTime = playTime;
        this.playFile = playFile;
        this.titleString = titleString;
        this.audioFile = audioFile;
        this.startTime = startTime;
    }

    public String getFilterCategory() {
        return filterCategory;
    }

    public void setFilterCategory(String filterCategory) {
        this.filterCategory = filterCategory;
    }

    public String getFilterLevel() {
        return filterLevel;
    }

    public void setFilterLevel(String filterLevel) {
        this.filterLevel = filterLevel;
    }

    public String getPlayTime() {
        return playTime;
    }

    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }

    public String getPlayFile() {
        return playFile;
    }

    public void setPlayFile(String playFile) {
        this.playFile = playFile;
    }

    public String getTitleString() {
        return titleString;
    }

    public void setTitleString(String titleString) {
        this.titleString = titleString;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "Act{" +
                "filterCategory='" + filterCategory + '\'' +
                ", filterLevel='" + filterLevel + '\'' +
                ", playTime='" + playTime + '\'' +
                ", playFile='" + playFile + '\'' +
                ", titleString='" + titleString + '\'' +
                ", audioFile='" + audioFile + '\'' +
                ", startTime='" + startTime + '\'' +
                '}';
    }
}