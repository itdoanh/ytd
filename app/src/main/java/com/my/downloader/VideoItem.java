package com.my.downloader;

import java.util.LinkedHashMap;

public class VideoItem {
    public String url;
    public String title;
    public String thumbUrl;
    public String vid; // ID video từ y2mate
    public boolean isReady = false;
    
    // Lưu các chất lượng (Ví dụ: "1080p (.mp4)") và mã Token tải (k)
    public LinkedHashMap<String, String> mp4Formats = new LinkedHashMap<>();
    public LinkedHashMap<String, String> mp3Formats = new LinkedHashMap<>();

    public VideoItem(String url) {
        this.url = url;
        this.title = "Đang lấy tiêu đề..."; 
    }
}