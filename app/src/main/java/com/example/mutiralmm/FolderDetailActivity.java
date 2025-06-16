package com.example.mutiralmm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mutiralmm.adapters.ImageDetailAdapter;
import com.example.mutiralmm.helpers.SessionManager;
import com.example.mutiralmm.services.ApiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FolderDetailActivity extends AppCompatActivity {

    private RecyclerView rvImages;
    private TextView tvTitle;
    private ImageView btnBack;
    private String folderName;
    private ImageDetailAdapter adapter;
    private SessionManager sessionManager;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_detail);
        sessionManager = new SessionManager(this);

        folderName = getIntent().getStringExtra("folder_name");

        initViews();
        setupRecyclerView();
        initService();
        loadImages();
        setupBackButton();
    }

    private void initService(){
        apiService = new ApiService(this);
    }

    private void initViews() {
        rvImages = findViewById(R.id.rvImages);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);

        tvTitle.setText("Album " + folderName);
    }

    private void setupRecyclerView() {
        adapter = new ImageDetailAdapter(this); // Initialize with empty list
        rvImages.setLayoutManager(new GridLayoutManager(this, 2));
        rvImages.setAdapter(adapter);
    }

    private void loadImages() {
        // Fetch documents from server
        fetchDocumentsFromServer();
    }

    private void fetchDocumentsFromServer() {
        int userId = sessionManager.getCurrentUserId();
        // Gunakan folderName sebagai filter tahun
        String year = folderName.replaceAll("\\D+", ""); // Hapus semua karakter non-angka
        Log.e("isinya year", year);
        apiService.getDocuments(userId, 1, 100, "", year, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONObject data = response.getJSONObject("data");
                            JSONArray documentsArray = data.getJSONArray("documents");

                            List<DocumentItem> documents = new ArrayList<>();

                            for (int i = 0; i < documentsArray.length(); i++) {
                                JSONObject docObj = documentsArray.getJSONObject(i);
                                DocumentItem item = new DocumentItem();
                                item.setId(docObj.getInt("id"));
                                item.setDocName(docObj.getString("doc_name"));
                                item.setDocDate(docObj.optString("doc_date", ""));
                                item.setDocNumber(docObj.getString("doc_number"));
                                item.setDocDesc(docObj.getString("doc_desc"));
                                item.setImagePath(docObj.getString("image_path"));
                                item.setDocYear(docObj.getString("doc_year"));

                                documents.add(item);
                            }

                            // Update adapter dengan data baru
                            adapter.setDocuments(documents);

                        } else {
                            String message = response.optString("message", "Gagal memuat dokumen");
                            Toast.makeText(FolderDetailActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(FolderDetailActivity.this, "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Cek apakah error karena unauthorized
                    if (!sessionManager.handleApiError(FolderDetailActivity.this, error)) {
                        // Jika bukan error unauthorized, tampilkan pesan error biasa
                        Toast.makeText(FolderDetailActivity.this, "Failed to load documents: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    // Model untuk dokumen
    public static class DocumentItem {
        private int id;
        private String docName;
        private String docDate;
        private String docNumber;
        private String docDesc;
        private String imagePath;
        private String docYear;

        // Getters dan Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getDocName() { return docName; }
        public void setDocName(String docName) { this.docName = docName; }

        public String getDocDate() { return docDate; }
        public void setDocDate(String docDate) { this.docDate = docDate; }

        public String getDocNumber() { return docNumber; }
        public void setDocNumber(String docNumber) { this.docNumber = docNumber; }

        public String getDocDesc() { return docDesc; }
        public void setDocDesc(String docDesc) { this.docDesc = docDesc; }

        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }

        public String getDocYear() { return docYear; }
        public void setDocYear(String docYear) { this.docYear = docYear; }
    }
}