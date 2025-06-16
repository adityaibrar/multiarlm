package com.example.mutiralmm;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;

public class ImageViewActivity extends AppCompatActivity {

    private ImageView ivFullImage;
    private TextView tvDocName, tvDocDate, tvDocNumber, tvDocDesc;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        initViews();
        loadImageData();
        setupBackButton();
    }

    private void initViews() {
        ivFullImage = findViewById(R.id.ivFullImage);
        tvDocName = findViewById(R.id.tvDocName);
        tvDocDate = findViewById(R.id.tvDocDate);
        tvDocNumber = findViewById(R.id.tvDocNumber);
        tvDocDesc = findViewById(R.id.tvDocDesc);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadImageData() {
        String imagePath = getIntent().getStringExtra("image_path");
        String docName = getIntent().getStringExtra("doc_name");
        String docDate = getIntent().getStringExtra("doc_date");
        String docNumber = getIntent().getStringExtra("doc_number");
        String docDesc = getIntent().getStringExtra("doc_desc");

        // Load image dengan Glide
        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this)
                    .load(imagePath)
                    .placeholder(R.drawable.ic_image_placeholder) // Placeholder saat loading
                    .error(R.drawable.ic_image_error) // Gambar error jika gagal
                    .into(ivFullImage);
        } else {
            ivFullImage.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Set document details
        tvDocName.setText(docName != null ? docName : "Tidak ada nama");
        tvDocDate.setText(docDate != null ? docDate : "Tidak ada tanggal");
        tvDocNumber.setText(docNumber != null ? docNumber : "Tidak ada nomor");
        tvDocDesc.setText(docDesc != null ? docDesc : "Tidak ada deskripsi");
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }
}