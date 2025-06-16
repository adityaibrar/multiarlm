package com.example.mutiralmm;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mutiralmm.helpers.SessionManager;
import com.example.mutiralmm.services.ApiService;

import org.json.JSONObject;

public class ImageViewActivity extends AppCompatActivity {

    private ImageView ivFullImage;
    private TextView tvDocName, tvDocDate, tvDocNumber, tvDocDesc;
    private Button btnDelete;
    private ApiService apiService;
    private int documentId = -1;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        // Inisialisasi view
        initViews();

        // Inisialisasi layanan
        apiService = new ApiService(this);
        sessionManager = new SessionManager(this);

        // Ambil data dari intent
        if (!getDataFromIntent()) {
            Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup tombol kembali
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        // Setup tombol hapus
        setupDeleteButton();
    }

    private void initViews() {
        ivFullImage = findViewById(R.id.ivFullImage);
        tvDocName = findViewById(R.id.tvDocName);
        tvDocDate = findViewById(R.id.tvDocDate);
        tvDocNumber = findViewById(R.id.tvDocNumber);
        tvDocDesc = findViewById(R.id.tvDocDesc);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private boolean getDataFromIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            Log.e("GetDataError", "Intent is null");
            return false;
        }

        // PERBAIKAN: Ambil ID sebagai integer langsung
        documentId = intent.getIntExtra("id", -1);
        if (documentId == -1) {
            Log.e("GetDataError", "Document ID not found or invalid");
            return false;
        }

        String imagePath = intent.getStringExtra("image_path");
        String docName = intent.getStringExtra("doc_name");
        String docDate = intent.getStringExtra("doc_date");
        String docNumber = intent.getStringExtra("doc_number");
        String docDesc = intent.getStringExtra("doc_desc");

        // Debug log untuk memeriksa data yang diterima
        Log.d("ImageViewActivity", "Document ID: " + documentId);
        Log.d("ImageViewActivity", "Image Path: " + imagePath);
        Log.d("ImageViewActivity", "Doc Name: " + docName);
        Log.d("ImageViewActivity", "Doc Date: " + docDate);

        // Set text views dengan fallback values
        tvDocName.setText(docName != null && !docName.isEmpty() ? docName : "Tidak ada nama");
        tvDocDate.setText(docDate != null && !docDate.isEmpty() ? docDate : "Tidak ada tanggal");
        tvDocNumber.setText(docNumber != null && !docNumber.isEmpty() ? docNumber : "Tidak ada nomor");
        tvDocDesc.setText(docDesc != null && !docDesc.isEmpty() ? docDesc : "Tidak ada deskripsi");

        // Load image
        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this)
                    .load(imagePath)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .into(ivFullImage);
        } else {
            Log.w("ImageViewActivity", "Image path is null or empty");
            ivFullImage.setImageResource(R.drawable.ic_image_placeholder);
        }

        return true;
    }

    private void setupDeleteButton() {
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        int userId = sessionManager.getCurrentUserId();
        if (documentId == -1 || userId == -1) {
            Toast.makeText(this, "Data tidak valid", Toast.LENGTH_SHORT).show();
            Log.e("DeleteError", "Invalid data - Document ID: " + documentId + ", User ID: " + userId);
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Hapus Dokumen")
                .setMessage("Apakah Anda yakin ingin menghapus dokumen ini?")
                .setPositiveButton("Ya", (dialog, which) -> deleteDocument())
                .setNegativeButton("Batal", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteDocument() {
        int userId = sessionManager.getCurrentUserId();
        if (documentId == -1 || userId == -1) {
            Toast.makeText(this, "User atau dokumen tidak dikenali", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("DeleteDocument", "Attempting to delete document ID: " + documentId + " for user: " + userId);

        apiService.deleteDocument(documentId, userId, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.optString("message", "Dokumen berhasil dihapus");

                        if (success) {
                            Toast.makeText(ImageViewActivity.this, message, Toast.LENGTH_SHORT).show();
                            // Set result untuk memberitahu activity sebelumnya bahwa ada perubahan
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(ImageViewActivity.this, "Gagal: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e("DeleteError", "Parsing error", e);
                        Toast.makeText(ImageViewActivity.this, "Respons tidak valid dari server", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e("DeleteError", "API Error: " + error);
                    // Cek apakah error karena unauthorized
                    if (!sessionManager.handleApiError(ImageViewActivity.this, error)) {
                        Toast.makeText(ImageViewActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}