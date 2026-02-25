package com.my.downloader;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.my.downloader.databinding.ActivityMainBinding;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private List<VideoItem> videoList = new ArrayList<>();
    private VideoAdapter adapter;

    // Lắng nghe khi tải xong để báo chỗ lưu
    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "✅ Tải Xong! Đã lưu vào mục DOWNLOAD của máy.", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        // 1. AUTO TEST API KHI MỞ APP (Kèm hiển thị Log chi tiết)
        binding.toolbar.setTitle("Đang kiểm tra API...");
        Y2mateHelper.testApi((isOnline, logMsg) -> {
            if(isOnline) {
                binding.toolbar.setTitle("Y2Mate - API: ONLINE 🟢");
            } else {
                binding.toolbar.setTitle("Y2Mate - API: LỖI 🔴");
                showErrorLog("Log lỗi Server", logMsg);
            }
        });

        // 2. SETUP DANH SÁCH & NÚT TẢI
        adapter = new VideoAdapter(videoList, new VideoAdapter.OnAction() {
            @Override public void onRemove(int position) {
                videoList.remove(position);
                adapter.notifyItemRemoved(position);
            }
            @Override public void onDownload(VideoItem item) {
                showDownloadOptions(item); 
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // 3. THÊM LINK
        binding.btnAdd.setOnClickListener(v -> {
            String url = binding.edtUrl.getText().toString();
            if(url.contains("youtu")) {
                videoList.add(new VideoItem(url));
                adapter.notifyItemInserted(videoList.size()-1);
                binding.edtUrl.setText("");
            } else Toast.makeText(this, "Link không hợp lệ!", Toast.LENGTH_SHORT).show();
        });

        // 4. NÚT START (GỌI ANALYZE)
        binding.btnStartProcess.setOnClickListener(v -> {
            for(int i=0; i<videoList.size(); i++) {
                int pos = i;
                VideoItem item = videoList.get(i);
                if(!item.isReady) {
                    Y2mateHelper.analyze(item, new Y2mateHelper.ApiCallback() {
                        @Override public void onSuccess(VideoItem result) { adapter.notifyItemChanged(pos); }
                        @Override public void onError(String msg) { 
                            showErrorLog("Log lỗi Analyze API", msg); 
                        }
                    });
                }
            }
        });
    }

    // Hiển thị menu chọn chất lượng từ y2mate
    private void showDownloadOptions(VideoItem item) {
        List<String> labels = new ArrayList<>();
        List<String> tokens = new ArrayList<>();
        List<String> extensions = new ArrayList<>();

        for(String q : item.mp4Formats.keySet()) { labels.add("🎬 Video " + q); tokens.add(item.mp4Formats.get(q)); extensions.add("mp4"); }
        for(String q : item.mp3Formats.keySet()) { labels.add("🎵 Nhạc " + q); tokens.add(item.mp3Formats.get(q)); extensions.add("mp3"); }

        new AlertDialog.Builder(this)
            .setTitle("Chọn chất lượng:")
            .setItems(labels.toArray(new String[0]), (dialog, which) -> {
                String token = tokens.get(which);
                String ext = extensions.get(which);
                Toast.makeText(this, "Đang lấy link thật...", Toast.LENGTH_SHORT).show();
                
                // Gọi CONVERT API để lấy D-Link
                Y2mateHelper.convert(item.vid, token, new Y2mateHelper.ConvertCallback() {
                    @Override public void onSuccess(String dlink) { startRealDownload(dlink, item.title, ext); }
                    @Override public void onError(String msg) { 
                        showErrorLog("Log lỗi Convert API", msg);
                    }
                });
            }).show();
    }

    // Tải Direct Link thật sự
    private void startRealDownload(String dlink, String title, String ext) {
        try {
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(dlink));
            String cleanTitle = title.replaceAll("[^a-zA-Z0-9 -]", "") + "." + ext;
            
            req.setTitle(cleanTitle);
            req.setDescription("Tiến trình đang chạy...");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, cleanTitle);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if(manager != null) {
                manager.enqueue(req);
                Toast.makeText(this, "Bắt đầu tải xuống...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            showErrorLog("Lỗi Trình tải xuống", e.getMessage());
        }
    }

    // HÀM HIỆN BẢNG LOG LỖI (GIÚP DEBUG DỄ DÀNG)
    private void showErrorLog(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle("⚠️ " + title)
            .setMessage(message)
            .setPositiveButton("Đóng", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete); // Tắt receiver khi thoát app
    }
}