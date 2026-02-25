# 📝 Hướng dẫn Commit và Push lên GitHub

## ⚠️ Lưu Ý Quan Trọng
Terminal trong workspace bị giới hạn. Vui lòng làm theo các bước sau để lưu code lên repository:

---

## 🚀 Cách 1: Sử dụng VS Code Source Control

1. **Mở tab Source Control** (biểu tượng nhánh bên trái hoặc `Ctrl+Shift+G`)

2. **Xem các file đã thay đổi:**
   - ✅ `app/src/main/java/com/my/downloader/LogManager.java` (MỚI)
   - ✅ `app/src/main/java/com/my/downloader/Y2mateHelper.java`
   - ✅ `app/src/main/java/com/my/downloader/MainActivity.java`
   - ✅ `app/src/main/res/layout/activity_main.xml`
   - ✅ `DEBUG_LOG_GUIDE.md` (MỚI)

3. **Stage tất cả thay đổi:**
   - Bấm dấu `+` bên cạnh mỗi file
   - Hoặc bấm `+` ở "Changes" để stage tất cả

4. **Commit với message:**
   ```
   feat: Thêm hệ thống logging debug chi tiết

   - Tạo LogManager class để thu thập tất cả log từ API
   - Thêm logging toàn diện vào Y2mateHelper (request/response/errors)
   - Thêm nút 'Show Debug Log' trong UI với tính năng:
     * Hiển thị tất cả log chi tiết
     * Copy log vào clipboard
     * Xóa log
   - Thêm DEBUG_LOG_GUIDE.md với hướng dẫn sử dụng

   Giúp debug vấn đề: API online nhưng không lấy được link download
   ```

5. **Push lên GitHub:**
   - Bấm nút "Sync Changes" hoặc "Push"
   - Hoặc sử dụng menu: `...` → `Push`

---

## 🚀 Cách 2: Sử dụng Terminal Thủ Công

Mở terminal mới (không phải bash hiện tại) và chạy:

```bash
# Di chuyển vào thư mục project
cd /workspaces/ytd

# Kiểm tra status
git status

# Thêm tất cả file
git add .

# Commit với message
git commit -m "feat: Thêm hệ thống logging debug chi tiết

- Tạo LogManager class để thu thập tất cả log từ API
- Thêm logging toàn diện vào Y2mateHelper (request/response/errors)
- Thêm nút Show Debug Log trong UI
- Thêm DEBUG_LOG_GUIDE.md với hướng dẫn sử dụng"

# Push lên GitHub
git push origin main
```

---

## 🚀 Cách 3: Sử dụng Script Có Sẵn

Script đã được tạo sẵn: `COMMIT_CHANGES.sh`

Chạy lệnh:
```bash
chmod +x COMMIT_CHANGES.sh
./COMMIT_CHANGES.sh
```

---

## 📦 Tổng Hợp Các File Đã Thay Đổi

### Files Mới:
1. **LogManager.java** - Hệ thống logging toàn diện
2. **DEBUG_LOG_GUIDE.md** - Hướng dẫn sử dụng và debug
3. **COMMIT_CHANGES.sh** - Script tự động commit
4. **HUONG_DAN_COMMIT.md** - File này

### Files Đã Sửa:
1. **Y2mateHelper.java** 
   - Thêm 60+ dòng logging chi tiết
   - Log tất cả HTTP requests/responses
   - Log parse JSON từng bước

2. **MainActivity.java**
   - Thêm nút btnShowLog
   - Method showDebugLog() với 3 options (View/Copy/Clear)

3. **activity_main.xml**
   - Thêm button "🔍 Hiển thị Chi Tiết Log (Debug)"

---

## ✅ Kiểm Tra Sau Khi Push

1. Truy cập: https://github.com/itdoanh/ytd
2. Xem commit mới nhất
3. Kiểm tra tất cả files đã được cập nhật

---

## 🆘 Nếu Gặp Lỗi

### Lỗi: "Permission denied"
```bash
chmod +x COMMIT_CHANGES.sh
```

### Lỗi: "Nothing to commit"
Các file đã được commit rồi, không cần làm gì thêm.

### Lỗi: "Failed to push"
```bash
git pull origin main
git push origin main
```

---

**Tạo bởi GitHub Copilot - Claude Sonnet 4.5**
