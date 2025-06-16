package com.example.mutiralmm;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.mutiralmm.helpers.SessionManager;
import com.example.mutiralmm.services.ApiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UploadActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_STORAGE = 100;
    private static final int REQUEST_PERMISSION_CAMERA = 101;

    private SessionManager sessionManager;

    ImageView uploadImage;
    Button saveButton, btnAmbilFoto;
    EditText etDocName, etDocDate, etDocNumber, etDocDesc;

    Uri selectedImageUri;
    String savedImagePath;
    File photoFile;
    ApiService apiService;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    uploadImage.setImageURI(selectedImageUri);
                }
            });

    private final ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && photoFile != null) {
                    selectedImageUri = Uri.fromFile(photoFile);
                    uploadImage.setImageURI(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadImage = findViewById(R.id.uploadImage);
        btnAmbilFoto = findViewById(R.id.btnAmbilFoto);
        etDocName = findViewById(R.id.etDocName);
        etDocDate = findViewById(R.id.etDocDate);
        etDocNumber = findViewById(R.id.etDocNumber);
        etDocDesc = findViewById(R.id.etDocDesc);
        saveButton = findViewById(R.id.saveButton);

        apiService = new ApiService(this);
        sessionManager = new SessionManager(this);

        uploadImage.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_STORAGE);
            } else {
                openGallery();
            }
        });

        btnAmbilFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_PERMISSION_CAMERA);
            } else {
                openCamera();
            }
        });

        etDocDate.setOnClickListener(v -> showDatePicker());

        saveButton.setOnClickListener(v -> saveData());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void openCamera() {
        try {
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
            photoFile = new File(storageDir, fileName);
            Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePhotoLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal membuka kamera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year);
            etDocDate.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveData() {
        String docName = etDocName.getText().toString().trim();
        String docDate = etDocDate.getText().toString().trim();
        String docNumber = etDocNumber.getText().toString().trim();
        String docDesc = etDocDesc.getText().toString().trim();

        if (selectedImageUri == null) {
            Toast.makeText(this, "Silakan pilih atau ambil gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (docName.isEmpty() || docDate.isEmpty() || docNumber.isEmpty() || docDesc.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        String year = "";
        try {
            Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(docDate);
            year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(date);
        } catch (Exception e) {
            Toast.makeText(this, "Format tanggal salah", Toast.LENGTH_SHORT).show();
            return;
        }

        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), year);
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            Toast.makeText(this, "Gagal membuat folder tahun", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            String imageFileName = "IMG_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(storageDir, imageFileName);
            OutputStream outStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
            outStream.flush();
            outStream.close();
            savedImagePath = imageFile.getAbsolutePath();
            int userId = sessionManager.getCurrentUserId();
            apiService.uploadDocument(
                    userId,
                    docName,
                    docDate,
                    docNumber,
                    docDesc,
                    imageFile,new ApiService.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            runOnUiThread(() -> {
                                Toast.makeText(UploadActivity.this, "Dokumen berhasil disimpan", Toast.LENGTH_SHORT).show();
                                clearForm();
                                startActivity(new Intent(UploadActivity.this, DashboardActivity.class));
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() ->
                                    Toast.makeText(UploadActivity.this, "Upload gagal: " + error, Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                    );
        } catch (Exception e) {
            Toast.makeText(this, "Gagal menyimpan gambar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Gagal Menyimpan gambar: ", e.getMessage());
            return;
        }

    }

    private void clearForm() {
        uploadImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.uploadimg));
        etDocName.setText("");
        etDocDate.setText("");
        etDocNumber.setText("");
        etDocDesc.setText("");
        selectedImageUri = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else if (requestCode == REQUEST_PERMISSION_CAMERA && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "Izin ditolak", Toast.LENGTH_SHORT).show();
        }
    }
}
