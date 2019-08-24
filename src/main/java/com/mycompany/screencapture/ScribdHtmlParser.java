package com.mycompany.screencapture;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.awt.SystemColor.text;

/**
 * Created by almatarm on 23/08/2019.
 */
public class ScribdHtmlParser {
    String content;

    public static final String FONT_SIZE_NORMAL = "24";
    public static final String FONT_SIZE_H3 = "30";

    ScribdHtmlParser(String content) {
        this.content = content;
    }

    public String getCleanHtml() {
        boolean lastLineEndsWithHyphen = false;
        StringBuilder buff = new StringBuilder();
        StringBuilder secBuff = new StringBuilder();

        Set<String> fontSizes = new HashSet<>();

        Document doc = Jsoup.parse(content);
        Elements textElements = doc.getElementsByClass("text_line");
        for( Element element: textElements) {
            String text = element.text();
            Map<String, String> params = parseElement(element);
            if(params.containsKey(STYLE_FONT_SIZE)) {
                fontSizes.add(params.get(STYLE_FONT_SIZE));
            }

            boolean endWithHyphen = text.endsWith("-");
            if(endWithHyphen) {
                text = text.substring(0, text.indexOf("-"));
            }
            if(lastLineEndsWithHyphen) {
                //Get first word and append it to last line
                int firstSpaceIdx = text.indexOf(" ");
                if(firstSpaceIdx > -1);
                secBuff.append(text.substring(0, firstSpaceIdx) + "\n");
                text = text.substring(firstSpaceIdx + 1);
            }
            secBuff.append("\t" + text + (endWithHyphen? "" : "\n"));

            if(endOfParagraph(element)) {
                String tag = getParagraphTag(fontSizes);
                buff.append(String.format("<%s>\n%s</%s>\n", tag, secBuff.toString(), tag));
                secBuff.delete(0, secBuff.length());
                fontSizes.clear();
            }

            lastLineEndsWithHyphen = endWithHyphen;
        }
        return buff.toString();
    }

    private boolean endOfParagraph(Element element) {
        Elements spans = element.getElementsByTag("span");
        if(!spans.isEmpty()) {
            if (spans.last().hasAttr("data-linebreak")) {
                return true;
            }
        }
        return false;
    }

    public static String STYLE_FONT_SIZE = "font-size";
    public Map<String, String> parseElement(Element element) {
        Map<String, String> param = new HashMap<>();
        String[] styles = element.attr("style").split(";");
        for(String style: styles) {
            String[] split = style.split(":");
            if(split.length == 2) {
                String key = split[0];
                String value = split[1];

                switch (key.trim()) {
                    case "font-size":
                        value = value.trim();
                        value = value.substring(0, value.indexOf("px"));
                        param.put(STYLE_FONT_SIZE, value);
                        break;

                }
            }
        }
        return param;
    }

    private String getParagraphTag(Set<String> fontsSizes) {
        if(fontsSizes.size() == 1) {
            if(fontsSizes.iterator().next().equals(FONT_SIZE_H3)) {
                return "h3";
            }
        }
        return "p";
    }


}
