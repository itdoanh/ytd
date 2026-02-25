package com.my.downloader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * LogManager - Thu thập tất cả log từ API để debug
 * Giúp phát hiện lỗi khi API online nhưng không lấy được link download
 */
public class LogManager {
    
    private static final List<String> logs = new ArrayList<>();
    private static final int MAX_LOGS = 200; // Giữ tối đa 200 log entries
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
    
    /**
     * Thêm log entry
     */
    public static synchronized void log(String tag, String message) {
        String timestamp = timeFormat.format(new Date());
        String logEntry = String.format("[%s] %s: %s", timestamp, tag, message);
        
        logs.add(logEntry);
        
        // Giới hạn kích thước log
        if (logs.size() > MAX_LOGS) {
            logs.remove(0);
        }
        
        // In ra Logcat để debug
        android.util.Log.d("Y2MATE_DEBUG", logEntry);
    }
    
    /**
     * Log request HTTP
     */
    public static void logRequest(String url, String method, String body) {
        log("REQUEST", String.format("%s %s\nBody: %s", method, url, body));
    }
    
    /**
     * Log response HTTP
     */
    public static void logResponse(String url, int statusCode, String body) {
        log("RESPONSE", String.format("%s\nStatus: %d\nBody: %s", url, statusCode, 
            body.length() > 500 ? body.substring(0, 500) + "..." : body));
    }
    
    /**
     * Log lỗi
     */
    public static void logError(String location, String error) {
        log("ERROR", String.format("%s - %s", location, error));
    }
    
    /**
     * Log thành công
     */
    public static void logSuccess(String location, String message) {
        log("SUCCESS", String.format("%s - %s", location, message));
    }
    
    /**
     * Lấy tất cả log để hiển thị
     */
    public static synchronized String getAllLogs() {
        if (logs.isEmpty()) {
            return "Chưa có log nào.\n\nHướng dẫn: Thử thêm link YouTube và bấm START để xem log chi tiết.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== DEBUG LOG - Tổng cộng ").append(logs.size()).append(" entries ===\n\n");
        
        for (String log : logs) {
            sb.append(log).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Xóa tất cả log
     */
    public static synchronized void clearLogs() {
        logs.clear();
        log("SYSTEM", "Log đã được xóa");
    }
    
    /**
     * Log phân tích JSON
     */
    public static void logJsonParsing(String field, String value) {
        log("JSON_PARSE", String.format("%s = %s", field, value));
    }
}
