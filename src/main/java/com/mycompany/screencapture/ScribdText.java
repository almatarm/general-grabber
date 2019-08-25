package com.mycompany.screencapture;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;

import static com.sun.tools.doclint.Entity.image;

/**
 * Created by almatarm on 23/08/2019.
 */
public class ScribdText {
    public static void main(String args[]) {
        ScribdParser parser = new ScribdParser();

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
        int firstPageNum = 1;
        int repeat = 200;

        //int renderedSrcX = 1341, renderedSrcY = 85; //Mac
        int renderedSrcX = 3262, renderedSrcY = 88; //Dell Screen

        //Create directory on the desktop
        File baseDir = new File(System.getProperty("user.home") + "/Desktop/Scribd/raw/" + bookName);
        System.out.println(baseDir);
        baseDir.mkdirs();

        Helper.showWindow(e -> {
            Helper.delay(5000);
            System.out.println("Ready");

            boolean lastPageHasImages = false;
            for(int i = 0; i < repeat; i++) {
                Helper.Mouse.rightClick(renderedSrcX, renderedSrcY);
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
                        String fileName = src.substring(src.lastIndexOf("/") +1, src.indexOf("?"));
                        String filePath = new File(baseDir, fileName).getAbsolutePath();
                        System.out.println("Downloading... " + url + " -> " + filePath);
                        Helper.Web.downloadImage(url, filePath);
                        Helper.delay(5000);
                    });
                    lastPageHasImages = !imgs.isEmpty();
                }
                String pagePath = new File(baseDir, String.format("Page %03d.html", i+firstPageNum)).getAbsolutePath();
                Helper.iFile.write(pagePath, Helper.ClipBoard.getContent());
                parser.addContent(contents);

                Helper.Chrome.closeTab();
                Helper.delay(1000);
                Helper.clickRight();
                Helper.delay((int) (Math.random() * 50000 + 1000) + (lastPageHasImages? 10000: 0));
            }
        });
    }
}
