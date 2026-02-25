#!/bin/bash

# Script để commit và push các thay đổi lên GitHub
# Chạy script này bằng lệnh: bash COMMIT_CHANGES.sh

echo "🔍 Đang kiểm tra các file đã thay đổi..."
git status

echo ""
echo "➕ Đang thêm tất cả các file vào staging area..."
git add app/src/main/java/com/my/downloader/LogManager.java
git add app/src/main/java/com/my/downloader/Y2mateHelper.java
git add app/src/main/java/com/my/downloader/MainActivity.java
git add app/src/main/res/layout/activity_main.xml
git add DEBUG_LOG_GUIDE.md

echo ""
echo "📝 Đang commit với message..."
git commit -m "feat: Thêm hệ thống logging debug chi tiết

- Tạo LogManager class để thu thập tất cả log từ API
- Thêm logging toàn diện vào Y2mateHelper (request/response/errors)
- Thêm nút 'Show Debug Log' trong UI với tính năng:
  * Hiển thị tất cả log chi tiết
  * Copy log vào clipboard
  * Xóa log
- Thêm DEBUG_LOG_GUIDE.md với hướng dẫn sử dụng

Giúp debug vấn đề: API online nhưng không lấy được link download"

echo ""
echo "🚀 Đang push lên GitHub..."
git push origin main

echo ""
echo "✅ Hoàn tất! Các thay đổi đã được lưu lên repository."
