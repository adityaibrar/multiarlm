package com.example.mutiralmm;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

        // Load image
        if (imagePath != null) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Uri imageUri = Uri.fromFile(imageFile);
                ivFullImage.setImageURI(imageUri);
            }
        }

        // Set document details
        tvDocName.setText(docName != null ? docName : "");
        tvDocDate.setText(docDate != null ? docDate : "");
        tvDocNumber.setText(docNumber != null ? docNumber : "");
        tvDocDesc.setText(docDesc != null ? docDesc : "");
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }
}