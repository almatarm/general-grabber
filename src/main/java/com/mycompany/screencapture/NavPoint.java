package com.mycompany.screencapture;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by almatarm on 28/08/2019.
 */
public class NavPoint {
    String id;
    String title;
    String src;

    List<NavPoint> children = new ArrayList<>();

    int totalChildren = 0;

    public NavPoint(String id, String title, String src) {
        this.id = id;
        this.title = title;
        this.src = src;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public List<NavPoint> getChildren() {
        return children;
    }

    public void setChildren(List<NavPoint> children) {
        this.children = children;
    }

    public int getTotalChildren() {
        return totalChildren;
    }

    public String getTags(int playOrder) {
        return getTags(playOrder, 1);
    }

    public String getTags(int playOrder, int tab) {
        StringBuilder buff = new StringBuilder();

        String n1 =     "   <navPoint id=\"%s\" playOrder=\"%d\">\n" +
                        "       <navLabel>\n" +
                        "           <text>%s</text>\n" +
                        "       </navLabel>\n" +
                        "       <content src=\"%s\"/>\n";
        String n2 =     "   </navPoint>\n\n";

        for(int i = 1; i < tab; i++) {
            n1 = n1.replace("   ", "        ");
            n2 = n2.replace("   ", "        ");
        }

        buff.append(String.format(n1, id, playOrder, title, src));
        totalChildren++;
        if (!children.isEmpty()) {
            children.forEach( (child) -> {
                buff.append(child.getTags(playOrder + totalChildren, tab+1));
                totalChildren++;
            });
        }

        buff.append(n2);
        return buff.toString();
    }
}
