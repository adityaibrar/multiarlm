package com.example.mutiralmm;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mutiralmm.adapters.AlbumAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    private RecyclerView rvAlbum;
    private EditText etSearch;
    private ImageView btnBack;
    private AlbumAdapter adapter;
    private List<AlbumAdapter.AlbumFolder> allFolders;
    private List<AlbumAdapter.AlbumFolder> filteredFolders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        initViews();
        setupRecyclerView();
        loadFolders();
        setupSearch();
        setupBackButton();
    }

    private void initViews() {
        rvAlbum = findViewById(R.id.rvAlbum);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        allFolders = new ArrayList<>();
        filteredFolders = new ArrayList<>();
        adapter = new AlbumAdapter(this, filteredFolders);
        rvAlbum.setLayoutManager(new LinearLayoutManager(this));
        rvAlbum.setAdapter(adapter);
    }

    private void loadFolders() {
        // Clear existing data to prevent duplicates
        allFolders.clear();

        File picturesDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);

        if (picturesDir != null && picturesDir.exists()) {
            File[] folders = picturesDir.listFiles();

            if (folders != null) {
                for (File folder : folders) {
                    if (folder.isDirectory()) {
                        int fileCount = countImageFiles(folder);
                        if (fileCount > 0) { // Hanya tampilkan folder yang memiliki gambar
                            long lastModified = getLastModifiedTime(folder);
                            AlbumAdapter.AlbumFolder albumFolder = new AlbumAdapter.AlbumFolder(
                                    folder.getName(),
                                    folder.getAbsolutePath(),
                                    fileCount,
                                    lastModified
                            );
                            allFolders.add(albumFolder);
                        }
                    }
                }
            }
        }

        // Sort berdasarkan nama folder (tahun) secara descending
        allFolders.sort((f1, f2) -> f2.getName().compareTo(f1.getName()));

        // Update filtered folders and notify adapter
        updateFilteredFolders();
    }

    private void updateFilteredFolders() {
        filteredFolders.clear();
        filteredFolders.addAll(allFolders);
        adapter.notifyDataSetChanged();
    }

    private int countImageFiles(File folder) {
        File[] files = folder.listFiles();
        if (files == null) return 0;

        int count = 0;
        for (File file : files) {
            if (file.isFile() && isImageFile(file.getName())) {
                count++;
            }
        }
        return count;
    }

    private boolean isImageFile(String fileName) {
        String name = fileName.toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") ||
                name.endsWith(".bmp") || name.endsWith(".webp");
    }

    private long getLastModifiedTime(File folder) {
        long lastModified = folder.lastModified();
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isImageFile(file.getName())) {
                    if (file.lastModified() > lastModified) {
                        lastModified = file.lastModified();
                    }
                }
            }
        }

        return lastModified;
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data ketika kembali dari activity lain
        loadFolders();
    }
}