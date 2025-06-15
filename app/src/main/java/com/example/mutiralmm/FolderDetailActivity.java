package com.example.mutiralmm;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mutiralmm.adapters.ImageDetailAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderDetailActivity extends AppCompatActivity {

    private RecyclerView rvImages;
    private TextView tvTitle;
    private ImageView btnBack;
    private String folderPath;
    private String folderName;
    private DBHelper dbHelper;
    private ImageDetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_detail);

        folderPath = getIntent().getStringExtra("folder_path");
        folderName = getIntent().getStringExtra("folder_name");

        initViews();
        setupRecyclerView();
        loadImages();
        setupBackButton();
    }

    private void initViews() {
        rvImages = findViewById(R.id.rvImages);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);
        dbHelper = new DBHelper(this);

        tvTitle.setText("Album " + folderName);
    }

    private void setupRecyclerView() {
        adapter = new ImageDetailAdapter(this);
        rvImages.setLayoutManager(new GridLayoutManager(this, 2));
        rvImages.setAdapter(adapter);
    }

    private void loadImages() {
        List<DocumentItem> documents = getDocumentsFromFolder();
        adapter.setDocuments(documents);
    }

    private List<DocumentItem> getDocumentsFromFolder() {
        List<DocumentItem> documents = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DBHelper.COLUMN_DOC_YEAR + " = ?";
        String[] selectionArgs = {folderName};

        Cursor cursor = db.query(
                DBHelper.TABLE_DOC,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DBHelper.COLUMN_DOC_DATE + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                DocumentItem item = new DocumentItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DOC_ID)));
                item.setDocName(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DOC_NAME)));
                item.setDocDate(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DOC_DATE)));
                item.setDocNumber(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DOC_NUMBER)));
                item.setDocDesc(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DOC_DESC)));
                item.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_IMAGE_PATH)));
                item.setDocYear(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DOC_YEAR)));

                // Verifikasi file masih ada
                File imageFile = new File(item.getImagePath());
                if (imageFile.exists()) {
                    documents.add(item);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return documents;
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