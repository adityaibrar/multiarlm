package com.example.mutiralmm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mutiralmm.adapters.AlbumAdapter;
import com.example.mutiralmm.helpers.SessionManager;
import com.example.mutiralmm.services.ApiService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumActivity extends AppCompatActivity {

    private RecyclerView rvAlbum;
    private EditText etSearch;
    private ImageView btnBack;
    private ProgressBar progressBar;
    private AlbumAdapter adapter;
    private List<AlbumAdapter.AlbumFolder> allFolders;
    private List<AlbumAdapter.AlbumFolder> filteredFolders;

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        sessionManager = new SessionManager(this);

        initViews();
        initApiService();

        setupRecyclerView();
        setupSearch();
        setupBackButton();
        loadAlbums();
    }

    private void initViews() {
        rvAlbum = findViewById(R.id.rvAlbum);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
         progressBar = findViewById(R.id.progressBar); // Pastikan ada di layout
    }

    private void initApiService() {
        apiService = new ApiService(this);
    }

    private void setupRecyclerView() {
        allFolders = new ArrayList<>();
        filteredFolders = new ArrayList<>();
        adapter = new AlbumAdapter(this, filteredFolders);
        rvAlbum.setLayoutManager(new LinearLayoutManager(this));
        rvAlbum.setAdapter(adapter);
    }

    private void loadAlbums() {
        int userId = sessionManager.getCurrentUserId();

        showLoading(true);

        // Menggunakan getDocuments dengan parameter default
        apiService.getAlbums(userId,  new ApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    try {
                        showLoading(false);
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONObject data = response.getJSONObject("data");
                            JSONArray albumsArray = data.getJSONArray("albums");

                            allFolders.clear();

                            for (int i = 0; i < albumsArray.length(); i++) {
                                JSONObject albumObj = albumsArray.getJSONObject(i);

                                String name = albumObj.getString("name");
                                int fileCount = albumObj.getInt("fileCount");
                                long lastModified = albumObj.getLong("lastModified");

                                AlbumAdapter.AlbumFolder albumFolder = new AlbumAdapter.AlbumFolder(
                                        "Dokumen " + name,
                                        name,
                                        fileCount,
                                        lastModified
                                );

                                allFolders.add(albumFolder);
                            }

                            // Sort descending by year
                            allFolders.sort((f1, f2) -> f2.getName().compareTo(f1.getName()));

                            updateFilteredFolders();

                            if (allFolders.isEmpty()) {
                                Toast.makeText(AlbumActivity.this, "Belum ada dokumen tersimpan", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String message = response.optString("message", "Gagal memuat dokumen");
                            Toast.makeText(AlbumActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AlbumActivity.this, "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);

                    // Cek apakah error karena unauthorized
                    if (!sessionManager.handleApiError(AlbumActivity.this, error)) {
                        // Jika bukan error unauthorized, tampilkan pesan error biasa
                        Toast.makeText(AlbumActivity.this, "Failed to load documents: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void updateFilteredFolders() {
        filteredFolders.clear();
        filteredFolders.addAll(allFolders);
        adapter.notifyDataSetChanged();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFolders(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterFolders(String query) {
        filteredFolders.clear();

        if (query.isEmpty()) {
            filteredFolders.addAll(allFolders);
        } else {
            for (AlbumAdapter.AlbumFolder folder : allFolders) {
                if (folder.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredFolders.add(folder);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        rvAlbum.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}