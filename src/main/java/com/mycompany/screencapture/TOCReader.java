package com.mycompany.screencapture;

import org.apache.commons.lang.StringUtils;

import java.util.*;

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
        int chapterIdx = 1;
        for(String title : toc) {
            int level = getLevel(title, "  ");
            NavPoint np = new NavPoint(
                    String.format("TOC%03d", chapterIdx), title,
                    String.format("text/Chapter%03d.html", chapterIdx));
            chapterIdx++;
            np.setLevel(level);

            if(np.getLevel() == 1) {
                nps.add(np);
            } else {
                for(int i = npsNotNested.size() -1; i > 0; i--) {
                    if(np.getLevel() - npsNotNested.get(i).getLevel() == 1) {
                        npsNotNested.get(i).getChildren().add(np);
                        break;
                    }
                }
            }
            npsNotNested.add(np);
        }

        StringBuilder buff = new StringBuilder();
        int playOrder = 1;
        for(NavPoint np : nps) {
            buff.append(np.getTags(playOrder));
            playOrder += np.getTotalChildren();
        }
        return buff.toString();
    }

    public Map<String, Integer> getTocLevels() {
        if (tocLevels.isEmpty()) getNavPointsTag();
        return tocLevels;
    }

    private int getLevel(String title, String tab) {
        return StringUtils.countMatches(title, tab) + 1;
    }
}
