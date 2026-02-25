#!/bin/bash
# Script commit tất cả thay đổi lên GitHub

set -e  # Exit khi có lỗi

cd /workspaces/ytd

echo "📦 Thêm tất cả file đã thay đổi..."
git add app/src/main/java/com/my/downloader/Y2mateHelper.java \
        app/src/main/java/com/my/downloader/MainActivity.java \
        app/src/main/java/com/my/downloader/VideoAdapter.java \
        app/src/main/java/com/my/downloader/VideoItem.java

echo ""
echo "📝 Chuẩn bị commit..."
git commit -m "feat: lấy tiêu đề và thumbnail YouTube trực tiếp

- Thêm fetchYouTubeMeta() để lấy metadata từ YouTube oEmbed API
- Lấy thumbnail + tiêu đề ngay khi thêm link (không cần chờ Analyze)
- Cập nhật UI hiển thị thumbnail ngay cả khi chưa ready
- Sử dụng hqdefault.jpg từ YouTube CDN để nhanh hơn
- Thêm method extractVideoIdFromUrl() công khai
- Cải thiện UX: người dùng thấy ảnh/tiêu đề ngay, không phải chờ lâu" \
       -m "Improvements:
- YouTube oEmbed API được gọi song song với phân tích video
- Thumbnail + tiêu đề cập nhật tự động trên UI khi nhận được
- Xử lý fallback nếu không lấy được từ oEmbed (vẫn có thumbnail từ CDN)
- Thêm logging cho quá trình fetch metadata"

echo ""
echo "🚀 Push lên GitHub..."
git push origin main

echo ""
echo "✅ Hoàn tất! Tất cả thay đổi đã lưu lên repository."
echo "📌 Xem chi tiết tại: https://github.com/itdoanh/ytd"
