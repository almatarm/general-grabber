package com.mycompany.screencapture;

import com.almatarm.app.common.AppSettings;
import com.almatarm.lego.io.Exec;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.velocity.VelocityContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

import static com.mycompany.screencapture.ScribdHtmlParser.FONT_SIZE_H3;

/**
 * Created by almatarm on 24/08/2019.
 */
public class ScribdBookParser {

    int chapterCount;
    int firstChapterIdx = 5;
    boolean isTOCProccessed = false;
    boolean newChapter = false;
    boolean isChapterHeadingANumber = false;
    int elementsProccessedAfterHeading = 0;
    String coverImage = null;
    List<String> toc = new ArrayList<>();
    Map<String, Integer> tocLevels;

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
    StringBuilder buff = null;
    StringBuilder secBuff = new StringBuilder();
    Map<String, StringBuilder> htmls = new HashMap<>();
    BookInfo bookInfo;

    int chapterIdx = 1;
    int linkIdx = 0;

    public ScribdBookParser(int chapterCount, BookInfo bookInfo, Map<String, Integer> tocLevels) {
        this.chapterCount = chapterCount;
        this.bookInfo = bookInfo;
        this.tocLevels = tocLevels;
    }

    private StringBuilder getBuffer(String name) {
        if(htmls.containsKey(name)) {
            return htmls.get(name);
        }
        htmls.put(name, new StringBuilder());
        return htmls.get(name);
    }

    public void addContent(String name, String content) {
        buff = getBuffer(name);
        Document doc = Jsoup.parse(content);
        buff.append(String.format("<a name=\"TOC%03d\"/>\n", chapterIdx++));
        if(chapterIdx > firstChapterIdx) newChapter = true;
        Elements textElements =
                doc.getElementsByAttribute("data-position");
                //doc.getElementsByClass("text_line");
        textElements.forEach(element -> processElement(element));
    }

    private void processElement(Element element) {
        elementsProccessedAfterHeading++;
        if(!element.getElementsByTag("img").isEmpty() && !isMixedElement(element)) {
            buff.append(processImage(element, true));
        } else if(!isMixedElement(element)) {
            preUpdateStatus(element, false);
            processTextOnlyElement(element);
        } else {
            processMixedElement(element);
        }
        postUpdateStatus(element);
        if(elementsProccessedAfterHeading > 1) {
            isChapterHeadingANumber = false;
        }
    }

    private boolean isMixedElement(Element e) {
        return !e.children().isEmpty() && e.child(0).hasAttr("data-lineindex");
    }

    private String processImage(Element element, boolean lineBreak) {
        String html;
        Element img = element.getElementsByTag("img").iterator().next();
        String src = img.attr("src");
        String imgFileName = src.substring(src.lastIndexOf("/") +1, src.indexOf("?"));

        int width  = img.attr("width").trim().isEmpty()  ? 0 : (int) (Integer.parseInt(img.attr("width")) * bookInfo.imageResizeFactor);
        int height = img.attr("height").trim().isEmpty() ? 0 : (int) (Integer.parseInt(img.attr("height")) * bookInfo.imageResizeFactor);

        if(!lineBreak) {
            html = String.format("\n<img style=\"text-align:center\" src=\"../images/%s\" height=%s width=%s />\n", imgFileName, img.attr("height"), img.attr("width"));
        } else if (img.attr("width").trim().isEmpty() || width <= bookInfo.fullImageWidth) {
            html = String.format("\n<center><img style=\"text-align:center\" src=\"../images/%s\" height=%s width=%s align=\"middle\"/>\n</br></center>\n", imgFileName, height, width);
        } else {
            html = String.format(
                    "<div style=\"text-indent:0;text-align:center;margin-right:auto;margin-left:auto;width:99%%;page-break-before:auto;page-break-inside:avoid;page-break-after:auto;\">\n" +
                            "  <div style=\"margin-left:0;margin-right:0;text-align:center;text-indent:0;width:100%%;\">\n" +
                            "    <p style=\"display:inline-block;text-indent:0;width:100%%;\">\n" +
                            "      <img  src=\"../images/%s\" style=\"width:99%%;\" />\n" +
                            "    </p>\n" +
                            "  </div>\n" +
                            "</div>\n",
                    imgFileName
            );
        }
        if(coverImage == null) coverImage = imgFileName;
        return html;
    }

    private void processTextOnlyElement(Element element) {
        try {
            Helper.Debug.println("text: " + text);
            if (lastLineEndsWithHyphen) {
                //Get first word and append it to last line
                int firstSpaceIdx = text.indexOf(" ");
                if (firstSpaceIdx == -1) firstSpaceIdx = text.length() -1;

                while(secBuff.length() > 0 && secBuff.charAt(secBuff.length() -1) == ' ') secBuff.deleteCharAt(secBuff.length() -1);
                secBuff.append(text.substring(0, firstSpaceIdx) + "\n");
                text = text.substring(firstSpaceIdx + 1);
            }
            if(bold == Status.TagBegin) secBuff.append("<b>");
            if(italic == Status.TagBegin) secBuff.append("<i>");
            if(italic == Status.TagEnd) secBuff.append("</i>");
            if(bold == Status.TagEnd) secBuff.append("</b>");
            secBuff.append("\t" + text + (isLineEndsWithHyphen ? "" : "\n"));


            if (isLineBreak) {
                String tag = null;
                if(newChapter) {
                    newChapter = false;
                    toc.add(secBuff.toString());
                    if(tocLevels != null) tag = "h" + tocLevels.get(secBuff.toString());
                }

                if(tag == null) tag = getParagraphTag(fontSizes);
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
//                int firstSpaceIdx = text.indexOf(" ");
//                if (firstSpaceIdx == -1) firstSpaceIdx = text.length() -1;
                while(secBuff.charAt(secBuff.length() -1) == ' ') secBuff.deleteCharAt(secBuff.length() -1);
                secBuff.append(text.trim() + "\n");
                text = "";
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
                if (isTOCProccessed && linkIdx < chapterCount) {
                    secBuff.append(String.format("<a href=\"#TOC%03d\">%s</a>", firstChapterIdx + linkIdx, linkText));
                    linkIdx++;
                } else if(isChapterHeadingANumber) {
                    secBuff.append(String.format("<h1>%s</h1>", linkText));
                    if(elementsProccessedAfterHeading > 0) isChapterHeadingANumber = false;
                } else {
                    //secBuff.append(String.format("<a href=#>%s</a>", linkText));
                    secBuff.append(String.format("<u>%s</u>", linkText));
                }
                isLink = true;
            }

            if (!isLink) {
                if(e.tag().getName().equals("img")) {
                    secBuff.append(processImage(e, false));
                } else {
                    if(text.contains("&nbsp;")) secBuff.append("<span>");
                    secBuff.append(text + " ");
                    if(text.contains("&nbsp;")) secBuff.append("</span>");
                }
            }

            if (isLineBreak) {
                String tag = null;
                if(newChapter) {
                    newChapter = false;
                    toc.add(linkText.toString());
                    if(tocLevels != null) {
                        tag = "h" + tocLevels.get(linkText.toString());
                        buff.append(String.format("<%s>\n%s</%s>\n", tag, linkText, tag));
                    }

                }

                if(tag == null) {
                    tag = getParagraphTag(fontSizes);
                    buff.append(String.format("<%s>\n%s</%s>\n", tag, secBuff.toString(), tag));
                }
                secBuff.delete(0, secBuff.length());
                fontSizes.clear();
            }
        });
    }

    private void preUpdateStatus(Element element, boolean mixed) {
        text = element.text();
        if(NumberUtils.isNumber(text.trim()) && buff.length() == 19 && tocLevels != null) {
            isChapterHeadingANumber = true;
            elementsProccessedAfterHeading = 0;
        }
        if(text.contains("Content")) isTOCProccessed = true;
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
            if(!fontsSizes.iterator().next().equals("24"))
                System.out.println(fontsSizes.iterator().next());
        }
        return "p";
    }

    public String getHTML() {
        return buff.toString();
    }

    public List<String> getTOCList() {
        return toc;
    }

    public Map<String, StringBuilder> getHtmls() {
        return htmls;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public static void main(String[] args) {
        Helper.Debug.isDebug = false;

//        names.forEach(bookName -> processBook(bookName));
        processBook(Books.getLastName());
    }

    private static void processBook(String bookName) {
        String prefix = "Chapter";

        AppSettings settings = new AppSettings("Scribd");
        Configuration config = settings.getAppConfiguration();

        File baseDir = new File(System.getProperty("user.home") + "/"
                + config.getString("books_dir") + "/" + bookName);
        System.out.println(baseDir.getAbsolutePath());
        File src = new File(baseDir, "src");
        List<File> pages = Arrays.asList(src.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(prefix);
            }
        }));
        Collections.sort(pages);


        //Get or Create Book Info
        BookInfo book = getBookInfo(src, bookName);

        //First Run to get all the links & TOC
        ScribdBookParser parser = new ScribdBookParser(pages.size() - 1, book, null);
        for (File chapter : pages) {
            parser.addContent(chapter.getName().replaceAll(" ", ""), Helper.iFile.read(chapter.getAbsolutePath()));
        }
        Map<String, Integer> tocLevels = new TOCGenerator(5, parser.getTOCList()).getTocLevels();

        //Second Run to fix TOC
        parser = new ScribdBookParser(pages.size() - 1, book, tocLevels);
        for (File chapter : pages) {
            parser.addContent(chapter.getName().replaceAll(" ", ""), Helper.iFile.read(chapter.getAbsolutePath()));
        }

        try {
            //Creating epub - http://www.jedisaber.com/eBooks/Tutorial.shtml
            //https://github.com/krisztianmukli/epub-boilerplate/wiki/The-toc.ncx-File
            File epub = new File(baseDir, "epub");
            epub.delete();
            epub.mkdirs();

            //write contents
            File oebps = new File(epub, "OEBPS");
            oebps.mkdir();
            File text = new File(oebps, "text");
            text.mkdir();
            Map<String, StringBuilder> htmls = parser.getHtmls();
            for(String fileName: htmls.keySet()) {
                VelocityContext context = new VelocityContext();
                context.put("title", fileName);
                context.put("body", htmls.get(fileName.toString()));
                Helper.iFile.write(new File(text, fileName).getAbsolutePath(), Helper.VelocityTemplate.evaluate("PageTemplate.html", context));
            }

            //Copy images
            File images = new File(oebps, "images");
            images.mkdir();
            List<File> imagesFiles = Arrays.asList(src.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return !file.getName().contains("html") && file.isFile();
                }
            }));
            for(File file: imagesFiles) {
                FileUtils.copyFile(file, new File(images, file.getName()));
            }
            //Copy Cover Image
            FileUtils.copyFile(new File(images, parser.getCoverImage()), new File(images, "cover.jpg"));


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


            //Generate toc.ncx
            File toc = new File(src, "toc");
            VelocityContext context = new VelocityContext();
            context.put("uuid", book.uuid);
            context.put("depth", 2);
            context.put("title", book.title);
            context.put("navPoints", new TOCReader(Files.readAllLines(toc.toPath())).getNavPointsTag());
            Helper.iFile.write(new File(oebps, "toc.ncx").getAbsolutePath(), Helper.VelocityTemplate.evaluate("OEBPS/toc.ncx", context));

            //Generate content.opf
            List<String> chapters = new ArrayList<>();
            pages.forEach(chapter -> {
                String name = chapter.getName().replaceAll(" ", "");
                chapters.add(name.substring(0, name.length() - 5));
            });
            context = new VelocityContext();
            context.put("uuid", book.uuid);
            context.put("title", book.title);
            context.put("author", book.author);
            context.put("chapters", chapters);
            Helper.iFile.write(new File(oebps, "content.opf").getAbsolutePath(), Helper.VelocityTemplate.evaluate("OEBPS/content.opf", context));

            //Copy Style
            //Meta-info
            File styles = new File(
                    parser.getClass().getClassLoader().getResource("OEBPS/styles").getFile()
            );
            FileUtils.copyDirectory(styles, new File(oebps, "styles"));


            //create zip script
            context = new VelocityContext();
            context.put("title", book.title);
            context.put("epub_dir", epub.getAbsolutePath());
            File zip = new File(epub, "zip_epub");
            Helper.iFile.write(zip.getAbsolutePath(), Helper.VelocityTemplate.evaluate("zip_epub", context));
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxr--r--");
            Files.setPosixFilePermissions(zip.toPath(), permissions);

            Exec.executeBatchScript(zip);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BookInfo getBookInfo(File src, String bookName) {
        BookInfo bookInfo = null;
        try {
            File data = new File(src, "info.json");
            if (data.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                bookInfo = mapper.readValue(data, BookInfo.class);
            } else {
                String title = bookName;
                String author = "author";
                if (bookName.contains("|")) {
                    title  = bookName.substring(0, bookName.indexOf("|")).trim();
                    author = bookName.substring(bookName.indexOf("|") + 1).trim();
                }
                bookInfo = new BookInfo();
                bookInfo.uuid = UUID.randomUUID().toString();
                bookInfo.title = title;
                bookInfo.author = author;
                bookInfo.fullImageWidth = 400;
                bookInfo.imageResizeFactor = 0.75f;
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(data, bookInfo);
            }
        } catch( Exception e) {
            e.printStackTrace();
        }
        return bookInfo;
    }


}
