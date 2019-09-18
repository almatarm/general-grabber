package com.mycompany.screencapture;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by almatarm on 28/08/2019.
 */
public class BookInfo {
    static Map<String, BookInfo> books = new HashMap<>();

    static {
        books.put("Craft Coffee: A Manual: Brewing a Better Cup at Home", new BookInfo(
                        "dc2958ef-7f8a-4776-9d6e-c43b22782f9b",
                        "Craft Coffee: A Manual: Brewing a Better Cup at Home",
                        "Jessica Easto and Andreas Willhoff"));

        books.put("Beginner Calisthenics", new BookInfo(
                "61008250-9d65-4ebb-b416-10f7b9d9e98f",
                "Beginner Calisthenics",
                "Heather LIndell"));

        books.put("The World As I See It", new BookInfo(
                "29937dd3-0c40-4b7c-804d-a562956b8b72",
                "The World As I See It",
                "Albert Einstein"));

        books.put("Eat Dirt", new BookInfo(
                "d1b32683-24ca-48a5-89bc-8e33108998e7",
                "Eat Dirt - Why Leaky Gut May Be the Root Cause of Your Health Problems and 5 Surprising Steps to Cure It",
                "Josh Axe"));

    }

    public static BookInfo find(String bookName) {
        return books.get(bookName);
    }

    public BookInfo(String uuid, String title, String author) {
        this.uuid = uuid;
        this.title = title;
        this.author = author;
    }

    public String uuid;
    public String title;
    public String author;

}
