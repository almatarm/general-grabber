package com.mycompany.screencapture;


import com.almatarm.app.common.AppSettings;
import org.apache.commons.configuration2.Configuration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by almatarm on 23/08/2019.
 */
public class ScribdText {
    public static void main(String args[]) {


//        String contents = Helper.iFile.read(new File("File 1").getAbsolutePath());
//        contents = contents.substring(contents.indexOf("<!DOCTYPE html>"));
//        parser.addContent(contents);
//
//        contents = Helper.iFile.read(new File("File 2").getAbsolutePath());
//        contents = contents.substring(contents.indexOf("<!DOCTYPE html>"));
//        parser.addContent(contents);
//
//        String html = parser.getHTML();
//        Helper.ClipBoard.setContent(html);
//        System.out.println(html);



        String bookName = "Frank Lloyd Wright and Mason City - Roy R. Behrens";
        bookName = "Craft Coffee - Jessica Easto and Andreas Willhoff";
//        bookName = "Beginner Calisthenics";
        bookName = "Exercise Samples";
        bookName = "The Good Earth";
        bookName = "The Elements of Style";
        bookName = "Requiem for a Dream";
        bookName = "The World As I See It";
        bookName = "Intuitive Eating A Revolutionary Program that Works";
        bookName = "Exercise Medicine Physiological Principles and Clinical Applications";
        bookName = "Intuitive Eating, 2nd Edition: A Revolutionary Program That Works";
        bookName = "Intuitive Eating: 30 Intuitive Eating Tips";
        bookName = "Eat Dirt";
        bookName = "How to Talk so Little Kids Will Listen";
        bookName = "The Power of Posture";
        bookName = "Photovoltaic Design and Installation For Dummies";
        bookName = "Yoga Assists";
        bookName = "Paleo Workouts For Dummies";
        bookName = "Song of Kali";

        AppSettings settings = new AppSettings("Scribd");
        Configuration config = settings.getAppConfiguration();

//        config.addProperty("renderedSrcX", 3262);
//        config.addProperty("renderedSrcY", 88);
//        config.addProperty("tocX", 2014);
//        config.addProperty("tocY", 88);
//        config.addProperty("books_dir", "Sync/Scribd Books");
//        settings.save();

        int start = 0;
        int repeat =230;
        boolean autoPageDetect = true;
        boolean downloadImages = true;

        int renderedSrcX = config.getInt("renderedSrcX");
        int renderedSrcY = config.getInt("renderedSrcY");
        int tocX = config.getInt("tocX");
        int tocY = config.getInt("tocY");

//        int renderedSrcX = 1828, renderedSrcY = 88; //iMac Work
//        int tocX = 576, tocY = 134; //iMac Work

        //Create directory on the desktop
        File baseDir = new File(System.getProperty("user.home") + "/"
                + config.getString("books_dir") + "/" + bookName);
        System.out.println(baseDir);
        baseDir.mkdirs();


        File src = new File(baseDir, "src");
        src.mkdir();


        ActionListener parseByPage = e -> {
            Helper.delay(5000);
            System.out.println("Ready");

            for(int i = 0; i < repeat; i++) {
                writePageToDisk(renderedSrcX, renderedSrcY, src, String.format("Page%03d.html", start+ i), downloadImages);
            }
        };


        ActionListener parseByChapter = e -> {
            int tocCount = repeat;

            Helper.delay(5000);
            System.out.println("Ready");

            if(autoPageDetect) {
                Helper.Mouse.clickB1(tocX, tocY);
                String renderedPage = getRenderedPage(renderedSrcX, renderedSrcY);
                tocCount = parseTOCCount(renderedPage, src);
                System.out.println("tocCount = " + tocCount);
                Helper.Mouse.clickB1(tocX, tocY);
            }

            for(int i = start; i < tocCount; i++) {
                System.out.println("Chapter " + i);
                Helper.Mouse.clickB1(tocX, tocY);
//                Helper.delay(500);
                Helper.tab();
                for(int j = 0; j < i; j++) {
                    Helper.arrowDown();
                }
                Helper.enter();

                Helper.delay(5000);
                writePageToDisk(renderedSrcX, renderedSrcY, src, String.format("Chapter%03d.html", i+1), downloadImages);
            }
        };

        Helper.showWindow(parseByChapter);
    }

    private static void writePageToDisk(int renderedSrcX, int renderedSrcY, File baseDir, String fileName) {
        writePageToDisk(renderedSrcX, renderedSrcY, baseDir, fileName, true);
    }

    private static void writePageToDisk(int renderedSrcX, int renderedSrcY, File baseDir, String fileName, boolean downloadImages) {
        boolean pageHasImages = false;

        Helper.Mouse.clickB1(renderedSrcX, renderedSrcY);
        Helper.delay(1000);
        Helper.selectAll();
        Helper.copy();

        String contents = Helper.ClipBoard.getContent();
        contents = contents.substring(contents.indexOf("<!DOCTYPE html>"));


        //Get Images
        if(downloadImages) {
            Document doc = Jsoup.parse(contents);
            Element aria_main = doc.getElementById("aria_main");
            if (aria_main != null) {
                Elements imgs = aria_main.getElementsByTag("img");
                imgs.forEach(img -> {
                    String src = img.attr("src");
                    String url = "https://www.scribd.com" + src;
                    String imgFileName = src.substring(src.lastIndexOf("/") + 1, src.indexOf("?"));
                    String filePath = new File(baseDir, imgFileName).getAbsolutePath();
                    System.out.println("Downloading... " + url + " -> " + filePath);
                    if(!new File(filePath).exists()) {
                        Helper.Web.downloadImage(url, filePath);
                        Helper.delay(5000);
                    }
                });
                pageHasImages = !imgs.isEmpty();
            }
        }
        String pagePath = new File(baseDir,fileName).getAbsolutePath();
        Helper.iFile.write(pagePath, Helper.ClipBoard.getContent());

        Helper.Chrome.closeTab();
        Helper.delay(1000);
        Helper.clickRight();

//        Helper.delay((int) (Math.random() * 20000) + (pageHasImages? 10000: 0));
    }

    private static String getRenderedPage(int renderedSrcX, int renderedSrcY) {
        Helper.Mouse.clickB1(renderedSrcX, renderedSrcY);
        Helper.delay(1000);
        Helper.selectAll();
        Helper.copy();

        String contents = Helper.ClipBoard.getContent();
        contents = contents.substring(contents.indexOf("<!DOCTYPE html>"));

        Helper.Chrome.closeTab();
        Helper.delay(1000);
        Helper.clickRight();

        return contents;
    }


    private static int parseTOCCount(String contents, File src) {
        Document doc = Jsoup.parse(contents);
        Elements chapterTitles = doc.getElementsByClass("chapter_title");
        StringBuilder buffWithPages = new StringBuilder();
        StringBuilder buff = new StringBuilder();
        for (Element chapterTitle : chapterTitles) {
            String text = chapterTitle.text();
            int sIdx = text.lastIndexOf(",");
            String title = text.substring(0, sIdx).trim();
            String page  = text.substring(sIdx +1).replace("page", "").trim();
            buffWithPages.append(String.format("%-10s %s%n", page, title));
            buff.append(String.format("%s%n", title));
        }
        String pagePath = new File(src,"toc").getAbsolutePath();
        Helper.iFile.write(pagePath, buff.toString());

        pagePath = new File(src,"toc_with_pages").getAbsolutePath();
        Helper.iFile.write(pagePath, buffWithPages.toString());

        return chapterTitles.size();
    }
}
