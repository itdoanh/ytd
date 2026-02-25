# 🔍 Hướng Dẫn Debug & Phân Tích Lỗi API

## 📋 Tổng Quan Cải Tiến

### ✅ Đã Fix & Cải Thiện:

1. **Thêm LogManager** - Hệ thống logging toàn diện
   - Thu thập tất cả log từ API calls
   - Lưu trữ lịch sử 200 log entries gần nhất
   - Timestamp chi tiết từng bước

2. **Logging Chi Tiết Trong Y2mateHelper**
   - Log tất cả HTTP requests (URL, method, body)
   - Log tất cả HTTP responses (status code, body)
   - Log quá trình parse JSON
   - Log errors với stack trace
   - Log thành công/thất bại từng bước

3. **Nút Debug Log Trong UI**
   - Hiển thị tất cả log chi tiết
   - Copy log vào clipboard
   - Xóa log để bắt đầu mới

---

## 🐛 Phân Tích Vấn Đề "API Online Nhưng Không Lấy Được Link"

### Nguyên nhân có thể:

1. **API Endpoint thay đổi format response**
   - YT1S/Y2mate APIs thường xuyên thay đổi cấu trúc JSON
   - Tên field khác (dlink → url, k → token, v.v.)

2. **Thiếu headers hoặc cookies**
   - Một số API yêu cầu thêm headers (Referer, Origin)
   - Rate limiting hoặc blocking

3. **Token/K value không đúng format**
   - Analyze API trả về token sai format
   - Convert API không nhận token

4. **Video bị hạn chế**
   - Video 18+, private, hoặc geo-restricted
   - Copyright strikes

---

## 🔧 Cách Sử Dụng Debug Log

### Bước 1: Thêm Link & START
1. Dán link YouTube vào ô input
2. Bấm nút "+" để thêm vào danh sách
3. Bấm "START (Phân tích)" để gọi API

### Bước 2: Xem Log Chi Tiết
1. Bấm nút **"🔍 Hiển thị Chi Tiết Log (Debug)"**
2. Xem toàn bộ quá trình:
   - API Test: Kiểm tra server online/offline
   - Request: Xem URL, headers, body gửi đi
   - Response: Xem status code, JSON trả về
   - JSON Parse: Xem từng field được parse
   - Error: Xem lỗi chi tiết

### Bước 3: Phân Tích
- Kiểm tra xem API nào đang được dùng
- Xem response JSON có chứa `dlink`, `url`, hoặc `k` không
- Kiểm tra status code (200 = OK, 403/404/500 = lỗi server)
- Xem error messages từ API

---

## 📊 Ví Dụ Log Thành Công

```
[12:34:56.123] API_TEST: Đang test: https://yt1s.ltd
[12:34:56.500] API_TEST: https://yt1s.ltd response code: 200
[12:34:56.501] SUCCESS: API ONLINE: https://yt1s.ltd

[12:35:00.100] ANALYZE: URL: https://youtu.be/abc123 -> VideoID: abc123
[12:35:00.101] ANALYZE: Sử dụng API: https://yt1s.ltd
[12:35:00.102] REQUEST: POST https://yt1s.ltd/api/ajaxSearch
Body: url=...&ajax=1&lang=en

[12:35:01.200] RESPONSE: https://yt1s.ltd/api/ajaxSearch
Status: 200
Body: {"status":"ok","title":"Demo Video","links":{...}}

[12:35:01.250] JSON_PARSE: status = ok
[12:35:01.251] JSON_PARSE: title = Demo Video
[12:35:01.252] YT1S_ANALYZE: MP4 object: có
[12:35:01.253] YT1S_ANALYZE: MP4[720]: q=720p, size=25MB, k=dXN...
[12:35:01.300] SUCCESS: Tìm thấy 3 định dạng MP4
[12:35:01.350] SUCCESS: Phân tích thành công: Demo Video
```

---

## 📊 Ví Dụ Log Lỗi

```
[12:40:00.100] ANALYZE: URL: https://youtu.be/xyz789 -> VideoID: xyz789
[12:40:00.101] REQUEST: POST https://yt1s.ltd/api/ajaxSearch
[12:40:01.200] RESPONSE: Status: 200
Body: {"status":"error","msg":"Video not found"}

[12:40:01.250] JSON_PARSE: status = error
[12:40:01.251] ERROR: YT1S_ANALYZE: Status not ok: error
[12:40:01.252] API_TEST: Đang test API tiếp theo...
```

---

## 🛠️ Cách Fix Khi Gặp Lỗi

### Lỗi: "Không lấy được link"
1. Xem log, tìm dòng `RESPONSE` của Convert API
2. Kiểm tra JSON có chứa field `dlink` không
3. Nếu không có `dlink`, API có thể đã đổi tên field

**Fix:** Cập nhật code parse JSON trong [Y2mateHelper.java](app/src/main/java/com/my/downloader/Y2mateHelper.java):
```java
String dlink = json.optString("dlink", "");
if (dlink.isEmpty()) {
    dlink = json.optString("url", ""); // Thử field khác
}
```

### Lỗi: "Tất cả API đều offline"
1. Kiểm tra kết nối internet
2. API domains có thể đã chết/đổi URL
3. Cập nhật `API_DOMAINS` trong Y2mateHelper.java

### Lỗi: "Status OK nhưng dlink rỗng"
- API trả về success nhưng không có link
- Có thể video đang trong hàng đợi convert
- Thử lại sau 5-10 giây

---

## 🎯 Các File Đã Thay Đổi

1. **LogManager.java** (MỚI)
   - Class quản lý logging toàn bộ app

2. **Y2mateHelper.java**
   - Thêm logging vào tất cả API calls
   - Log request/response/errors
   - Thêm 60+ dòng log chi tiết

3. **MainActivity.java**
   - Thêm nút "Show Debug Log"
   - Method `showDebugLog()` với chức năng:
     * Hiển thị log
     * Copy log to clipboard
     * Clear log

4. **activity_main.xml**
   - Thêm button `btnShowLog`

---

## 💡 Tips

- **Copy log trước khi clear**: Dùng nút "Copy Log" để lưu lại trước khi xóa
- **Log tự động giới hạn**: Chỉ lưu 200 entries gần nhất để tránh tràn bộ nhớ
- **Logcat**: Tất cả log cũng được ghi vào Android Logcat với tag `Y2MATE_DEBUG`

---

## 🚀 Test Ngay

1. Mở app
2. Thêm link: `https://youtu.be/dQw4w9WgXcQ`
3. Bấm START
4. Bấm nút "🔍 Hiển thị Chi Tiết Log"
5. Xem toàn bộ quá trình API

---

**Phát triển bởi GitHub Copilot với Claude Sonnet 4.5**
