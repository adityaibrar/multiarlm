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
    private SharedPreferences sharedPreferences;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        initViews();
        initApiService();

        // Validasi user login sebelum melanjutkan
        if (!validateUserSession()) {
            return; // Jika tidak valid, activity akan di-finish
        }

        setupRecyclerView();
        setupSearch();
        setupBackButton();
        loadAlbums();
    }

    private void initViews() {
        rvAlbum = findViewById(R.id.rvAlbum);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        // progressBar = findViewById(R.id.progressBar); // Pastikan ada di layout
    }

    private void initApiService() {
        apiService = new ApiService(this);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
    }

    private boolean validateUserSession() {
        // Cek apakah user sudah login
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        userId = sharedPreferences.getInt("user_id", 1);

        if (!isLoggedIn || userId == -1) {
            Toast.makeText(this, "Sesi login telah berakhir. Silakan login kembali.", Toast.LENGTH_LONG).show();

            // Hapus data session
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Kembali ke login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return false;
        }

        return true;
    }

    private void setupRecyclerView() {
        allFolders = new ArrayList<>();
        filteredFolders = new ArrayList<>();
        adapter = new AlbumAdapter(this, filteredFolders);
        rvAlbum.setLayoutManager(new LinearLayoutManager(this));
        rvAlbum.setAdapter(adapter);
    }

    private void loadAlbums() {
        if (userId == -1) {
            Toast.makeText(this, "User ID tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // Menggunakan getDocuments dengan parameter default
        apiService.getAlbums(userId,  new ApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    try {
//                        Log.e("userId: ", u);
                        showLoading(false);

                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONObject data = response.getJSONObject("data");
                            JSONArray documentsArray = data.getJSONArray("documents");

                            // Group documents by year to create album folders
                            Map<String, List<JSONObject>> documentsByYear = new HashMap<>();

                            for (int i = 0; i < documentsArray.length(); i++) {
                                JSONObject docObj = documentsArray.getJSONObject(i);
                                String year = docObj.getString("doc_year");

                                if (!documentsByYear.containsKey(year)) {
                                    documentsByYear.put(year, new ArrayList<>());
                                }
                                documentsByYear.get(year).add(docObj);
                            }

                            allFolders.clear();

                            // Create album folders from grouped documents
                            for (Map.Entry<String, List<JSONObject>> entry : documentsByYear.entrySet()) {
                                String year = entry.getKey();
                                List<JSONObject> docs = entry.getValue();

                                // Find the latest modified document for lastModified
                                long latestModified = 0;
                                for (JSONObject doc : docs) {
                                    try {
                                        // Convert created_at to timestamp (simplified)
                                        String createdAt = doc.getString("created_at");
                                        long timestamp = System.currentTimeMillis(); // Fallback
                                        // You might want to parse the actual date string here
                                        if (timestamp > latestModified) {
                                            latestModified = timestamp;
                                        }
                                    } catch (Exception e) {
                                        latestModified = System.currentTimeMillis();
                                    }
                                }

                                AlbumAdapter.AlbumFolder albumFolder = new AlbumAdapter.AlbumFolder(
                                        "Dokumen " + year,
                                        "documents/" + year,
                                        docs.size(),
                                        latestModified
                                );

                                allFolders.add(albumFolder);
                            }

                            // Sort folders by name (year) descending
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
                    if (error.contains("401") || error.contains("unauthorized")) {
                        Toast.makeText(AlbumActivity.this, "Sesi login berakhir. Silakan login kembali.", Toast.LENGTH_LONG).show();

                        // Hapus session dan kembali ke login
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        Intent intent = new Intent(AlbumActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
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

        // Validasi ulang session ketika activity resume
        if (validateUserSession()) {
            // Refresh data ketika kembali dari activity lain
            loadAlbums();
        }
    }

    // Method untuk mendapatkan current user ID (jika dibutuhkan di tempat lain)
    public int getCurrentUserId() {
        return userId;
    }

    // Method untuk mendapatkan username (jika dibutuhkan)
    public String getCurrentUsername() {
        return sharedPreferences.getString("username", "");
    }
}