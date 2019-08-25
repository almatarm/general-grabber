package com.mycompany.screencapture;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.mycompany.screencapture.ScribdHtmlParser.FONT_SIZE_H3;

/**
 * Created by almatarm on 24/08/2019.
 */
public class ScribdParser {
    boolean lastLineEndsWithHyphen = false;
    boolean isLineEndsWithHyphen = false;
    boolean isTheLastLineInParagraph = false;

    String text;
    Map<String, String> params = new HashMap<>();
    Set<String> fontSizes = new HashSet<>();
    StringBuilder buff = new StringBuilder();
    StringBuilder secBuff = new StringBuilder();


    public void addContent(String content) {
        Document doc = Jsoup.parse(content);
        Elements textElements = doc.getElementsByClass("text_line");
        textElements.forEach(element -> processElement(element));
    }

    private void processElement(Element element) {
        preUpdateStatus(element);
        if(element.childNodeSize() == 2) {
//            processTextOnlyElement(element);
        } else {
            processMixedElement(element);
        }
        postUpdateStatus(element);
    }

    private void processTextOnlyElement(Element element) {
        if(isLineEndsWithHyphen) {
            text = text.substring(0, text.indexOf("-"));
        }
        if(lastLineEndsWithHyphen) {
            //Get first word and append it to last line
            int firstSpaceIdx = text.indexOf(" ");
            if(firstSpaceIdx > -1);
            secBuff.append(text.substring(0, firstSpaceIdx) + "\n");
            text = text.substring(firstSpaceIdx + 1);
        }
        secBuff.append("\t" + text + (isLineEndsWithHyphen ? "" : "\n"));

        if(isTheLastLineInParagraph) {
            String tag = getParagraphTag(fontSizes);
            buff.append(String.format("<%s>\n%s</%s>\n", tag, secBuff.toString(), tag));
            secBuff.delete(0, secBuff.length());
            fontSizes.clear();
        }
    }

    private void processMixedElement(Element element) {
        if(isLineEndsWithHyphen) {
            text = text.substring(0, text.indexOf("-"));
        }
        if(lastLineEndsWithHyphen) {
            //Get first word and append it to last line
            int firstSpaceIdx = text.indexOf(" ");
            if(firstSpaceIdx > -1);
            secBuff.append(text.substring(0, firstSpaceIdx) + "\n");
            text = text.substring(firstSpaceIdx + 1);
        }
        secBuff.append("\t" + text + (isLineEndsWithHyphen ? "" : "\n"));

        if(isTheLastLineInParagraph) {
            String tag = getParagraphTag(fontSizes);
            buff.append(String.format("<%s>\n%s</%s>\n", tag, secBuff.toString(), tag));
            secBuff.delete(0, secBuff.length());
            fontSizes.clear();
        }
    }
    private void preUpdateStatus(Element element) {
        text = element.text();
        params = parseElement(element);
        if(params.containsKey(STYLE_FONT_SIZE)) {
            fontSizes.add(params.get(STYLE_FONT_SIZE));
        }
        isLineEndsWithHyphen = text.endsWith("-");
        isTheLastLineInParagraph = endOfParagraph(element);
    }

    private void postUpdateStatus(Element element) {
        lastLineEndsWithHyphen = isLineEndsWithHyphen;
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

    public String getHTML() {
        return buff.toString();
    }


}
