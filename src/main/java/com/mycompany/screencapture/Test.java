package com.mycompany.screencapture;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bouncycastle.asn1.x509.X509ObjectIdentifiers.id;

/**
 * Created by almatarm on 02/11/2019.
 */
public class Test {
    public static void main(String[] args) {
        String contents = Helper.iFile.read("/Users/almatarm/tmp/page.html");

        getImageUrls(contents).forEach(url -> System.out.println(url));
    }

    public static List<String> getImageUrls(String content) {
        List<String> links = new ArrayList<>();

        Pattern p = Pattern.compile("<img.*?>");
        Matcher m = p.matcher(content);

        int count = 0;
        while( m.find() ) {
            String imgTxt = m.group();
            if (imgTxt.contains("scepub")) {
                String src = imgTxt.substring(imgTxt.indexOf("/scepub"));
                src = src.substring(0, src.indexOf("\""));
                String url = "https://www.scribd.com" + src;
                links.add(url);
            }
        }

        return links;
    }
}
