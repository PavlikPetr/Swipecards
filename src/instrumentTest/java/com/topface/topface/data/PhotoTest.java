package com.topface.topface.data;

import android.test.InstrumentationTestCase;

import java.util.HashMap;

public class PhotoTest extends InstrumentationTestCase {

    public void testGetSuitableLink() {
        HashMap<String, String> links = new HashMap<String, String>();
        links.put("c64x64", "url1");
        links.put("c128x128", "url2");
        links.put("c192x192", "url3");
        links.put("r640x960", "url4");

        Photo photo = new Photo(1, links, 0, 0);

        assertEquals("url2", photo.getSuitableLink(40, 40));
        assertEquals("url2", photo.getSuitableLink(40, 41));
        assertEquals("url2", photo.getSuitableLink(101, 101));
        assertEquals("url2", photo.getSuitableLink(105, 106));
        assertEquals("url2", photo.getSuitableLink(129, 129));
        assertEquals("url3", photo.getSuitableLink(190, 190));
        assertEquals("url3", photo.getSuitableLink(300, 300));
        assertEquals("url3", photo.getSuitableLink(400, 400));
        assertEquals("url4", photo.getSuitableLink(700, 700));
        assertEquals("url4", photo.getSuitableLink(256, 400));
        assertEquals("url4", photo.getSuitableLink(1000, 1500));
    }

}
