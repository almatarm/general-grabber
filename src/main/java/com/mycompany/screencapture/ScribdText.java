package com.mycompany.screencapture;


import java.awt.*;
import java.io.File;

/**
 * Created by almatarm on 23/08/2019.
 */
public class ScribdText {
    public static void main(String args[]) {

        String contents = RobotHelper.iFile.read(new File("File 1").getAbsolutePath());
        contents = contents.substring(contents.indexOf("<!DOCTYPE html>"));
        String content = new ScribdHtmlParser(contents).getCleanHtml();
        RobotHelper.ClipBoard.setContent(content);
        System.out.println(content);

//        RobotHelper.showWindow(e -> {



//            RobotHelper.delay(5000);
//            System.out.println("Ready");
//
//            int repeat = 2;
//
//            for(int i = 0; i < repeat; i++) {
//                RobotHelper.Mouse.rightClick(3262, 88);
//                RobotHelper.delay(1000);
//                RobotHelper.selectAll();
//                RobotHelper.copy();
//                RobotHelper.iFile.write(new File("File " + (i + 1)).getAbsolutePath(), RobotHelper.ClipBoard.getContent());
//                RobotHelper.Chrome.closeTab();
//                RobotHelper.delay(1000);
//                RobotHelper.clickRight();
//                RobotHelper.delay((int) (Math.random() * 50000 + 1000));
//            }
//        });
    }
}
