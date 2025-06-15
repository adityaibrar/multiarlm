package com.example.mutiralmm.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mutiralmm.FolderDetailActivity;
import com.example.mutiralmm.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private List<AlbumFolder> folders;
    private Context context;

    public AlbumAdapter(Context context, List<AlbumFolder> folders) {
        this.context = context;
        this.folders = folders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlbumFolder folder = folders.get(position);
        holder.bind(folder);
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFolderName;
        private TextView tvFolderInfo;
        private ImageView ivFolderIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolderName = itemView.findViewById(R.id.tvFolderName);
            tvFolderInfo = itemView.findViewById(R.id.tvFolderInfo);
            ivFolderIcon = itemView.findViewById(R.id.ivFolderIcon);
        }

        public void bind(AlbumFolder folder) {
            tvFolderName.setText(folder.getName());

            // Format info: jumlah berkas dan tanggal terakhir
            String info = folder.getFileCount() + " Berkas | " +
                    formatDate(folder.getLastModified());
            tvFolderInfo.setText(info);

            // Set click listener untuk membuka folder
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, FolderDetailActivity.class);
                intent.putExtra("folder_path", folder.getPath());
                intent.putExtra("folder_name", folder.getName());
                context.startActivity(intent);
            });
        }

        private String formatDate(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    // Model untuk folder
    public static class AlbumFolder {
        private String name;
        private String path;
        private int fileCount;
        private long lastModified;

        public AlbumFolder(String name, String path, int fileCount, long lastModified) {
            this.name = name;
            this.path = path;
            this.fileCount = fileCount;
            this.lastModified = lastModified;
        }

        // Getters
        public String getName() { return name; }
        public String getPath() { return path; }
        public int getFileCount() { return fileCount; }
        public long getLastModified() { return lastModified; }
    }
}