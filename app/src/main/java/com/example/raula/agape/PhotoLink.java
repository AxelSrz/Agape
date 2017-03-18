package com.example.raula.agape;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by raula on 3/14/2017.
 */

public class PhotoLink {
    String photoLink;
    String photoTitle;

    static HashMap<String, Bitmap> loadedPhotos = new HashMap<>();

    PhotoLink(){}

    PhotoLink(String photoL, String title){
        photoLink = photoL;
        photoTitle = title;
    }
}
