package com.my.downloader;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.my.downloader.databinding.ItemVideoBinding;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private List<VideoItem> list;
    private OnAction listener;

    public interface OnAction {
        void onRemove(int position);
        void onDownload(VideoItem item); // Bấm để chọn chất lượng
    }

    public VideoAdapter(List<VideoItem> list, OnAction listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoViewHolder(ItemVideoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem item = list.get(position);
        holder.binding.tvTitle.setText(item.title);

        if (item.isReady) {
            holder.binding.tvStatus.setText("Sẵn sàng tải");
            holder.binding.tvStatus.setTextColor(0xFF4CAF50);
        } else {
            holder.binding.tvStatus.setText("Chờ nhấn Start...");
            holder.binding.tvStatus.setTextColor(0xFF757575);
        }

        if (item.thumbUrl != null && !item.thumbUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(item.thumbUrl).into(holder.binding.imgThumb);
        } else {
            holder.binding.imgThumb.setImageDrawable(null);
        }

        holder.binding.btnRemove.setOnClickListener(v -> listener.onRemove(position));
        holder.binding.btnDownload.setOnClickListener(v -> { if(item.isReady) listener.onDownload(item); });
    }

    @Override public int getItemCount() { return list.size(); }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ItemVideoBinding binding;
        public VideoViewHolder(ItemVideoBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}