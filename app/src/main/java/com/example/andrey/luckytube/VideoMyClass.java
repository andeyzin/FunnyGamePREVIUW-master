package com.example.andrey.luckytube;


import android.graphics.Bitmap;

public class VideoMyClass {
    String title;
    Bitmap preview;
    String videoId;

    VideoMyClass(String title, Bitmap preview, String videoId) {
        this.title = title;
        this.preview = preview;
        this.videoId = videoId;
    }
}