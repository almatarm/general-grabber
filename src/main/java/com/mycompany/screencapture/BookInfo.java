package com.mycompany.screencapture;


/**
 * Created by almatarm on 28/08/2019.
 */
public class BookInfo {

    public String uuid;
    public String title;
    public String author;
    public int fullImageWidth;
    public float imageResizeFactor;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getFullImageWidth() {
        return fullImageWidth;
    }

    public void setFullImageWidth(int fullImageWidth) {
        this.fullImageWidth = fullImageWidth;
    }

    public float getImageResizeFactor() {
        return imageResizeFactor;
    }

    public void setImageResizeFactor(float imageResizeFactor) {
        this.imageResizeFactor = imageResizeFactor;
    }
}
