package com.mycompany.screencapture;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.*;

import static sun.jvm.hotspot.oops.CellTypeState.top;

/**
 * Created by almatarm on 28/08/2019.
 */
public class TOCReader {
    List<String> toc;
    Map<String, Integer> tocLevels = new HashMap<>();

    public TOCReader(List<String> toc) {
        this.toc = toc;
    }

    public String getNavPointsTag() {
        List<NavPoint> nps = new ArrayList<>();
        List<NavPoint> npsNotNested = new ArrayList<>();
        Stack<NavPoint> topNps = new Stack<>();
        NavPoint lastNp = null;
        int chapterIdx = 1;
        int topLevel = 1;
        for(String title : toc) {
            int level = getLevel(title, "  ");
            System.out.println(level + "\t" + title);
            NavPoint np = new NavPoint(
                    String.format("TOC%03d", chapterIdx), title,
                    String.format("text/Chapter%03d.html", chapterIdx));
            chapterIdx++;
            np.setLevel(level);


            if(np.getLevel() == 1) {
                nps.add(np);
//            }
//            else if(!npsNotNested.isEmpty() &&
//                    np.getLevel() < npsNotNested.get(npsNotNested.size() -1).getLevel()) {
//                //Level is higher than previous one
//
            } else {
                for(int i = npsNotNested.size() -1; i > 0; i--) {
                    if(np.getLevel() - npsNotNested.get(i).getLevel() == 1) {
                        npsNotNested.get(i).getChildren().add(np);
                        break;
                    }
                }
            }
            npsNotNested.add(np);

//            if(lastNp == null) {
//                lastNp = np;
//                nps.add(np);
//                continue;
//            }
//
//            if(np.getLevel() == lastNp.getLevel()) {
//                if(!topNps.isEmpty()) {
//                    topNps.peek().getChildren().add(np);
//                } else {
//                    nps.add(np);
//                }
//                lastNp = np;
//            } else if (np.getLevel() > lastNp.getLevel()) {
//                topNps.push(lastNp);
//                lastNp.getChildren().add(np);
//                lastNp = np;
//            } else if (np.getLevel() < lastNp.getLevel()) {
//                topNps.pop();
//                while (!topNps.isEmpty() && np.getLevel() < topNps.peek().getLevel())
//                    topNps.pop();
//
//                if(!topNps.isEmpty()) {
//                    topNps.peek().getChildren().add(np);
//                } else {
//                    nps.add(np);
//                }
//                lastNp = np;
//            }
        }

        StringBuilder buff = new StringBuilder();
        int playOrder = 1;
        for(NavPoint np : nps) {
            buff.append(np.getTags(playOrder));
            playOrder += np.getTotalChildren();
        }
        System.out.println(buff.toString());
        return buff.toString();
    }

    public Map<String, Integer> getTocLevels() {
        if (tocLevels.isEmpty()) getNavPointsTag();
        return tocLevels;
    }

    private int getLevel(String title, String tab) {
//        int level = 1;
//        int chIdx = 0;
//        while(chIdx < title.length()) {
//            if(title.charAt(chIdx) == '\t' || title.charAt(chIdx) == ' ') {
//                level++;
//            } else {
//                break;
//            }
//            chIdx++;
//        }
//        return level;
        return StringUtils.countMatches(title, tab) + 1;
    }
}
