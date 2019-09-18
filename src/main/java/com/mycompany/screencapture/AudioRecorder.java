package com.mycompany.screencapture;

import java.util.Date;

/**
 * Created by almatarm on 2019-09-02.
 */
public class AudioRecorder {
    public static void main(String[] args) {
        int h = 30;
        int m = 30;
        long duration = 1000 * 60 * (m + h * 60);
        System.out.println("duration = " + duration);
        System.out.println(new Date());
        Helper.delay(duration);
        System.out.println(new Date());
        Helper.Mouse.clickB1(144, 95);
    }
}
