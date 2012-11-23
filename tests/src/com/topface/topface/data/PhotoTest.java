package com.topface.topface.data;

import android.test.InstrumentationTestCase;

import java.util.HashMap;

public class PhotoTest extends InstrumentationTestCase {

    public void testGetSuitableLink() {
        HashMap<String,String> links = new HashMap<String, String>();
//        links.put("c128x128","url1");
//        links.put("c64x64","url2");
//        links.put("c192x192","url3");
//        links.put("r640x960","url4");
//        links.put("r960x640","url5");
        links.put("c128x-","url6");
        links.put("c64x-","url7");
        links.put("c203x-","url8");

        Photo photo = new Photo(1, links);

        String url1 = photo.getSuitableLink(128,128);
        assertEquals("url6",url1);
        String url2 = photo.getSuitableLink(64,64);
        assertEquals("url6",url2);
//        String url3 = photo.getSuitableLink(640,640);
//        assertEquals("url5",url3);
  }

}
