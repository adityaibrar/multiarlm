package com.example.mutiralmm.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mutiralmm.FolderDetailActivity;
import com.example.mutiralmm.ImageViewActivity;
import com.example.mutiralmm.R;
import com.example.mutiralmm.services.ApiService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageDetailAdapter extends RecyclerView.Adapter<ImageDetailAdapter.ViewHolder> {

    private Context context;
    private List<FolderDetailActivity.DocumentItem> documents;
    private ApiService apiService;
    private int requestCode;

    public ImageDetailAdapter(Context context, int requestCode) {
        this.context = context;
        this.documents = new ArrayList<>();
        this.apiService = new ApiService(context);
        this.requestCode = requestCode;
    }

    // Overload constructor untuk backward compatibility
    public ImageDetailAdapter(Context context) {
        this(context, 100); // Default request code
    }

    public void setDocuments(List<FolderDetailActivity.DocumentItem> documents) {
        this.documents = documents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FolderDetailActivity.DocumentItem document = documents.get(position);
        holder.bind(document);
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private TextView tvDocName;
        private TextView tvDocDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvDocName = itemView.findViewById(R.id.tvDocName);
            tvDocDate = itemView.findViewById(R.id.tvDocDate);
        }

        public void bind(FolderDetailActivity.DocumentItem document) {
            String imagePath = document.getImagePath();

            tvDocName.setText(document.getDocName());
            tvDocDate.setText(document.getDocDate());

            // Gabungkan base URL dengan image_path
            String fullImageUrl = apiService.getImageUrl(document.getImagePath());
            Log.d("ImageURL", fullImageUrl);

            Glide.with(context)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .into(ivImage);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ImageViewActivity.class);

                // Kirim data dengan benar
                intent.putExtra("id", document.getId());
                intent.putExtra("image_path", fullImageUrl);
                intent.putExtra("doc_name", document.getDocName());
                intent.putExtra("doc_date", document.getDocDate());
                intent.putExtra("doc_number", document.getDocNumber());
                intent.putExtra("doc_desc", document.getDocDesc());

                // Debug log
                Log.d("ImageDetailAdapter", "Sending document ID: " + document.getId());
                Log.d("ImageDetailAdapter", "Sending doc name: " + document.getDocName());

                // PENTING: Gunakan startActivityForResult untuk mendapatkan callback
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, requestCode);
                } else {
                    context.startActivity(intent);
                    Log.w("ImageDetailAdapter", "Context is not Activity, cannot use startActivityForResult");
                }
            });
        }
    }
}