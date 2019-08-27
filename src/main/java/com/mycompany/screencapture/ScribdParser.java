package com.mycompany.screencapture;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static com.mycompany.screencapture.ScribdHtmlParser.FONT_SIZE_H3;
import static java.nio.file.StandardCopyOption.*;

/**
 * Created by almatarm on 24/08/2019.
 */
public class ScribdParser {

    int chapterCount;

    enum Status {
        TagBegin, TagBody, TagEnd, NoTag;
    }
    boolean lastLineEndsWithHyphen   = false;
    boolean isLineEndsWithHyphen     = false;
    boolean isLineBreak = false;
    Status bold = Status.NoTag;
    Status italic = Status.NoTag;

    String text;
    Map<String, String> params = new HashMap<>();
    Set<String> fontSizes = new HashSet<>();
    StringBuilder buff = new StringBuilder();
    StringBuilder secBuff = new StringBuilder();


    int chapterIdx = 1;
    int linkIdx = 1;

    public ScribdParser(int chapterCount) {
        this.chapterCount = chapterCount;
    }

    public void addContent(String content) {
        Document doc = Jsoup.parse(content);
        if(buff.length() > 0) {
            buff.append(String.format("<a name=\"TOC%03d\"/>\n", chapterIdx));
            chapterIdx++;
        }
        Elements textElements =
                doc.getElementsByAttribute("data-position");
                //doc.getElementsByClass("text_line");
        textElements.forEach(element -> processElement(element));
    }

    private void processElement(Element element) {
        if(!element.getElementsByTag("img").isEmpty()) {
            processImage(element);
        } else if(!isMixedElement(element)) {
            preUpdateStatus(element, false);
            processTextOnlyElement(element);
        } else {
            processMixedElement(element);
        }
        postUpdateStatus(element);
    }

    private boolean isMixedElement(Element e) {
        return !e.children().isEmpty() && e.child(0).hasAttr("data-lineindex");
    }

    private void processImage(Element element) {
        Element img = element.getElementsByTag("img").iterator().next();
        String src = img.attr("src");
        String imgFileName = src.substring(src.lastIndexOf("/") +1, src.indexOf("?"));
        buff.append(String.format("\n<img src=\"images/%s\" height=%s width=%s />\n<br/>\n", imgFileName, img.attr("height"), img.attr("width")));
    }

    private void processTextOnlyElement(Element element) {
        try {
            Helper.Debug.println("text: " + text);
            if (lastLineEndsWithHyphen) {
                //Get first word and append it to last line
                int firstSpaceIdx = text.indexOf(" ");
                if (firstSpaceIdx == -1) firstSpaceIdx = text.length() -1;
                while(secBuff.charAt(secBuff.length() -1) == ' ') secBuff.deleteCharAt(secBuff.length() -1);
                secBuff.append(text.substring(0, firstSpaceIdx) + "\n");
                text = text.substring(firstSpaceIdx + 1);
            }
            if(bold == Status.TagBegin) secBuff.append("<b>");
            if(italic == Status.TagBegin) secBuff.append("<i>");
            if(italic == Status.TagEnd) secBuff.append("</i>");
            if(bold == Status.TagEnd) secBuff.append("</b>");
            secBuff.append("\t" + text + (isLineEndsWithHyphen ? "" : "\n"));


            if (isLineBreak) {
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
        Helper.Debug.println("mixd: " + text);
        element.children().forEach(e -> {
            preUpdateStatus(e, true);

            if (lastLineEndsWithHyphen) {
                //Get first word and append it to last line
                int firstSpaceIdx = text.indexOf(" ");
                if (firstSpaceIdx == -1) firstSpaceIdx = text.length() -1;
                while(secBuff.charAt(secBuff.length() -1) == ' ') secBuff.deleteCharAt(secBuff.length() -1);
                secBuff.append(text.substring(0, firstSpaceIdx) + "\n");
                text = text.substring(firstSpaceIdx + 1);
            }

            boolean isLink = false;
            if(bold == Status.TagBegin) secBuff.append("<b>");
            if(italic == Status.TagBegin) secBuff.append("<i>");
            if(italic == Status.TagEnd) secBuff.append("</i>");
            if(bold == Status.TagEnd) secBuff.append("</b>");

            //Handle Links
            if(!e.children().isEmpty() && e.child(0).hasClass("first_link_part")) {
                linkText.delete(0, linkText.length());
                isLink = true;
            }

            if(e.hasClass("link_part") || (!e.children().isEmpty() && e.child(0).hasClass("link_part"))) {
                linkText.append(e.text().trim().isEmpty()? " " : e.text());
                isLink = true;
            }

            if(!e.children().isEmpty() && e.child(0).hasClass("last_link_part")) {
                if (linkIdx < chapterCount) {
                    secBuff.append(String.format("<a href=\"#TOC%03d\">%s</a>", linkIdx++, linkText));
                } else {
                    secBuff.append(String.format("<a href=#>%s</a>", linkText));
                }
                isLink = true;
            }

            if (!isLink) {
                secBuff.append(text + " ");
            }

            if (isLineBreak) {
                String tag = getParagraphTag(fontSizes);
                buff.append(String.format("<%s>\n%s</%s>\n", tag, secBuff.toString(), tag));
                secBuff.delete(0, secBuff.length());
                fontSizes.clear();
            }
        });
    }
    private void preUpdateStatus(Element element, boolean mixed) {
        text = element.text();
        Helper.Debug.println("preU: " + text);
        params = mixed && !element.children().isEmpty() ? parseElement(element.child(0)): parseElement(element);
        if(params.containsKey(STYLE_FONT_SIZE)) {
            fontSizes.add(params.get(STYLE_FONT_SIZE));
        }
        if(params.containsKey(STYLE_FONT_WEIGHT)) {
            switch(bold) {
                case NoTag:
                    bold = Status.TagBegin;
                    break;
                case TagBegin:
                    bold = Status.TagBody;
                    break;
            }
        } else {
            switch (bold) {
                case TagBegin:
                case TagBody:
                    bold = Status.TagEnd;
                    break;
                default:
                    bold = Status.NoTag;
            }
        }

        if(params.containsKey(STYLE_FONT_STYLE)) {
            switch(italic) {
                case NoTag:
                    italic = Status.TagBegin;
                    break;
                case TagBegin:
                    italic = Status.TagBody;
                    break;
            }
        } else {
            switch (italic) {
                case TagBegin:
                case TagBody:
                    italic = Status.TagEnd;
                    break;
                default:
                    italic = Status.NoTag;
            }
        }

        isLineBreak = endOfParagraph(element);
        isLineEndsWithHyphen = (text.trim().isEmpty() && isLineEndsWithHyphen) || text.endsWith("-");
        if (!text.trim().isEmpty() && isLineEndsWithHyphen) {
            text = text.substring(0, text.indexOf("-"));
        }
        Helper.Debug.println("Hyph: " + isLineEndsWithHyphen);
    }

    private void postUpdateStatus(Element element) {
        lastLineEndsWithHyphen = isLineEndsWithHyphen;
        Helper.Debug.println("post: " + text + "#" + isLineEndsWithHyphen + "#" + lastLineEndsWithHyphen);
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
    public static final String STYLE_FONT_STYLE = "font-style";
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
                        param.put(STYLE_FONT_WEIGHT, value);
                        break;
                    case STYLE_FONT_STYLE:
                        value = value.trim();
                        param.put(STYLE_FONT_STYLE, value);
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

    public static void main(String[] args) {
        Helper.Debug.isDebug = false;
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

        ScribdParser parser = new ScribdParser(pages.size() - 1);
        for (File chapter : pages) {
            System.out.println(chapter.getAbsolutePath());
            parser.addContent(Helper.iFile.read(chapter.getAbsolutePath()));
        }
        String html = parser.getHTML();


        try {
            //Creating epub - http://www.jedisaber.com/eBooks/Tutorial.shtml
            File epub = new File(baseDir, "epub");
            epub.delete();
            epub.mkdirs();

            File oebps = new File(epub, "OEBPS");
            oebps.mkdir();
            Helper.iFile.write(new File(oebps, "contents.html").getAbsolutePath(), html);

            //Copy images
            File images = new File(oebps, "images");
            images.mkdir();
            List<File> imagesFiles = Arrays.asList(baseDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return !file.getName().contains("html") && file.isFile();
                }
            }));
            for(File file: imagesFiles) {
                FileUtils.copyFile(file, new File(images, file.getName()));
            };

            //copy mimeType
            File mimetype = new File(
                    parser.getClass().getClassLoader().getResource("mimetype").getFile()
            );
            FileUtils.copyFile(mimetype, new File(epub, "mimetype"));

            //Meta-info
            File metaInfo = new File(epub, "META-INF");
            metaInfo.mkdir();
            File container = new File(
                    parser.getClass().getClassLoader().getResource("META-INF/container.xml").getFile()
            );
            FileUtils.copyFile(mimetype, new File(metaInfo, "container.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
