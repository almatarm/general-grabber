package com.mycompany.screencapture;

import java.util.*;


/**
 * Created by almatarm on 28/08/2019.
 */
public class TOCGenerator {
    int firstChapterIdx;
    List<String> toc;
    Map<String, Integer> tocLevels = new HashMap<>();
    private List<String> level1 = Arrays.asList("introduction", "chapter", "appendix", "resources", "index", "acknowledgements", "about the author");

    public TOCGenerator(int firstChapterIdx, List<String> toc) {
        this.firstChapterIdx = firstChapterIdx;
        this.toc = toc;
    }

    public String getNavPointsTag() {
        List<NavPoint> nps = new ArrayList<>();
        NavPoint activeNp = null;
        int chapterIdx = 0;
        for(String title : toc) {
            NavPoint np = new NavPoint(
                    String.format("TOC%03d", firstChapterIdx + chapterIdx), title,
                    String.format("text/Chapter%03d.html", firstChapterIdx + chapterIdx));
            chapterIdx++;

            if(level1.contains(title.toLowerCase()) || title.toLowerCase().startsWith("chapter")) {
                activeNp = null;
            }

            if(activeNp == null) {// && !level1.contains(title.toLowerCase())) {
                nps.add(np);
                tocLevels.put(title, 1);
            } else {
                activeNp.getChildren().add(np);
                tocLevels.put(title, 2);
            }

            if(level1.contains(title.toLowerCase()) || title.toLowerCase().startsWith("chapter")) {
                activeNp = np;
            }
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
}
