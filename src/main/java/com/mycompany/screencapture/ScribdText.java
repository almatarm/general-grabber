package com.mycompany.screencapture;


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
        bookName = "Beginner Calisthenics";







        int start = 11;
        int repeat = 30;

//        int renderedSrcX = 1341, renderedSrcY = 85; //Mac
//        int renderedSrcX = 3262, renderedSrcY = 88; //Dell Screen
        int renderedSrcX = 1828, renderedSrcY = 88; //iMac Work

        int tocX = 576, tocY = 134; //iMac Work

        //Create directory on the desktop
        File baseDir = new File(System.getProperty("user.home") + "/Desktop/Scribd/raw/" + bookName);
        System.out.println(baseDir);
        baseDir.mkdirs();


        ActionListener parseByPage = e -> {
            Helper.delay(5000);
            System.out.println("Ready");

            for(int i = 0; i < repeat; i++) {
                writePageToDisk(renderedSrcX, renderedSrcY, baseDir, String.format("Page %03d.html", start+ i));
            }
        };


        ActionListener parseByChapter = e -> {
            Helper.delay(5000);
            System.out.println("Ready");

            for(int i = start; i <= repeat; i++) {
                System.out.println("Chapter " + i);
                Helper.Mouse.clickB1(tocX, tocY);
                Helper.delay(500);
                Helper.tab();
                for(int j = 0; j < i -1; j++) {
                    Helper.arrowDown();
                    Helper.delay(120);
                }
                Helper.enter();

                Helper.delay(5000);
                writePageToDisk(renderedSrcX, renderedSrcY, baseDir, String.format("Chapter %03d.html", i));
            }
        };

        Helper.showWindow(parseByChapter);
    }

    private static void writePageToDisk(int renderedSrcX, int renderedSrcY, File baseDir, String fileName) {
        boolean pageHasImages = false;

        Helper.Mouse.clickB1(renderedSrcX, renderedSrcY);
        Helper.delay(1000);
        Helper.selectAll();
        Helper.copy();

        String contents = Helper.ClipBoard.getContent();
        contents = contents.substring(contents.indexOf("<!DOCTYPE html>"));

        //Get Images
        Document doc = Jsoup.parse(contents);
        Element aria_main = doc.getElementById("aria_main");
        if(aria_main != null) {
            Elements imgs = aria_main.getElementsByTag("img");
            imgs.forEach(img -> {
                String src = img.attr("src");
                String url = "https://www.scribd.com" + src;
                String imgFileName = src.substring(src.lastIndexOf("/") +1, src.indexOf("?"));
                String filePath = new File(baseDir, imgFileName).getAbsolutePath();
                System.out.println("Downloading... " + url + " -> " + filePath);
                Helper.Web.downloadImage(url, filePath);
                Helper.delay(5000);
            });
            pageHasImages = !imgs.isEmpty();
        }
        String pagePath = new File(baseDir,fileName).getAbsolutePath();
        Helper.iFile.write(pagePath, Helper.ClipBoard.getContent());

        Helper.Chrome.closeTab();
        Helper.delay(1000);
        Helper.clickRight();

        Helper.delay((int) (Math.random() * 20000) + (pageHasImages? 10000: 0));
    }

}
