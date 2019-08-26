package com.mycompany.screencapture;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

import static com.mycompany.screencapture.ScribdHtmlParser.FONT_SIZE_H3;

/**
 * Created by almatarm on 24/08/2019.
 */
public class ScribdParser {
    boolean lastLineEndsWithHyphen = false;
    boolean isLineEndsWithHyphen = false;
    boolean isTheLastLineInParagraph = false;
    boolean isBoldStart = false;
    boolean isBoldEnd = false;

    String text;
    Map<String, String> params = new HashMap<>();
    Set<String> fontSizes = new HashSet<>();
    StringBuilder buff = new StringBuilder();
    StringBuilder secBuff = new StringBuilder();


    public void addContent(String content) {
        Document doc = Jsoup.parse(content);
        Elements textElements =
                doc.getElementsByAttribute("data-position");
                //doc.getElementsByClass("text_line");
        textElements.forEach(element -> processElement(element));
    }

    private void processElement(Element element) {
        preUpdateStatus(element);
        if(!element.getElementsByTag("img").isEmpty()) {
            processImage(element);
        } else if(element.childNodeSize() == 2) {
            processTextOnlyElement(element);
        } else {
            processMixedElement(element);
        }
        postUpdateStatus(element);
    }


    private void processImage(Element element) {
        Element img = element.getElementsByTag("img").iterator().next();
        String src = img.attr("src");
        String imgFileName = src.substring(src.lastIndexOf("/") +1, src.indexOf("?"));
        buff.append(String.format("\n<img src=\"%s\" height=%s width=%s />\n", imgFileName, img.attr("height"), img.attr("width")));
    }

    private void processTextOnlyElement(Element element) {
        try {
            if (isLineEndsWithHyphen) {
                text = text.substring(0, text.indexOf("-"));
            }
            if (lastLineEndsWithHyphen) {
                //Get first word and append it to last line
                int firstSpaceIdx = text.indexOf(" ");
                if (firstSpaceIdx == -1) firstSpaceIdx = text.length() -1;
                secBuff.append(text.substring(0, firstSpaceIdx) + "\n");
                text = text.substring(firstSpaceIdx + 1);
            }
            if(isBoldStart) secBuff.append("<b>");
            secBuff.append("\t" + text + (isLineEndsWithHyphen ? "" : "\n"));
            if(isBoldEnd) secBuff.append("</b>");

            if (isTheLastLineInParagraph) {
                String tag = getParagraphTag(fontSizes);
                buff.append(String.format("<%s>\n%s</%s>\n", tag, secBuff.toString(), tag));
                secBuff.delete(0, secBuff.length());
                fontSizes.clear();
            }
        } catch (RuntimeException e) {
            System.out.println(element.toString());
            e.printStackTrace();
        }
    }

    StringBuilder linkText = new StringBuilder();
    private void processMixedElement(Element element) {
        element.children().forEach(e -> {

            //Handle Links
            if(!e.children().isEmpty() && e.child(0).hasClass("first_link_part")) {
                linkText.delete(0, linkText.length());
            }

            if(e.hasClass("link_part") || (!e.children().isEmpty() && e.child(0).hasClass("link_part"))) {
                linkText.append(e.text().trim().isEmpty()? " " : e.text());
            }

            if(!e.children().isEmpty() && e.child(0).hasClass("last_link_part")) {
                System.out.println(linkText.toString());
            }
        });
//        if(isLineEndsWithHyphen) {
//            text = text.substring(0, text.indexOf("-"));
//        }
//        if(lastLineEndsWithHyphen) {
//            //Get first word and append it to last line
//            int firstSpaceIdx = text.indexOf(" ");
//            if(firstSpaceIdx > -1);
//            secBuff.append(text.substring(0, firstSpaceIdx) + "\n");
//            text = text.substring(firstSpaceIdx + 1);
//        }
//        secBuff.append("\t" + text + (isLineEndsWithHyphen ? "" : "\n"));
//
//        if(isTheLastLineInParagraph) {
//            String tag = getParagraphTag(fontSizes);
//            buff.append(String.format("<%s>\n%s</%s>\n", tag, secBuff.toString(), tag));
//            secBuff.delete(0, secBuff.length());
//            fontSizes.clear();
//        }
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

    public static final String STYLE_FONT_SIZE = "font-size";
    public static final String STYLE_FONT_WEIGHT = "font-weight";
    public Map<String, String> parseElement(Element element) {
        Map<String, String> param = new HashMap<>();
        String[] styles = element.attr("style").split(";");
        for(String style: styles) {
            String[] split = style.split(":");
            if(split.length == 2) {
                String key = split[0];
                String value = split[1];

                switch (key.trim()) {
                    case STYLE_FONT_SIZE:
                        value = value.trim();
                        value = value.substring(0, value.indexOf("px"));
                        param.put(STYLE_FONT_SIZE, value);
                        break;
                    case STYLE_FONT_WEIGHT:
                        value = value.trim();
                        if(param.containsKey(STYLE_FONT_WEIGHT)) {
                            isBoldStart = false;
                            isBoldEnd = false;
                        } else {
                            param.put(STYLE_FONT_WEIGHT, value);
                            isBoldStart = true;
                            isBoldEnd = false;
                        }
                        break;

                }
            }
        }
        if(!element.attr("style").contains(STYLE_FONT_WEIGHT)) {
            isBoldStart = false; isBoldEnd = true;
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

    public static void main(String[] args) {
        String bookName = "Frank Lloyd Wright and Mason City - Roy R. Behrens";
        bookName = "Craft Coffee - Jessica Easto and Andreas Willhoff";
        bookName = "Beginner Calisthenics";

        String prefix = "Chapter";

        File baseDir = new File(System.getProperty("user.home") + "/Desktop/Scribd/raw/" + bookName);
        List<File> pages = Arrays.asList(baseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(prefix);
            }
        }));
        Collections.sort(pages);

        ScribdParser parser = new ScribdParser();
        for (File chapter : pages) {
            System.out.println(chapter.getAbsolutePath());
            parser.addContent(Helper.iFile.read(chapter.getAbsolutePath()));
        }
        Helper.iFile.write(new File(baseDir, baseDir.getName() + ".html").getAbsolutePath(), parser.getHTML());

    }

}
