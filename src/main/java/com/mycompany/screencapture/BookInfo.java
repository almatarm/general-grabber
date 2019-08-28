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
