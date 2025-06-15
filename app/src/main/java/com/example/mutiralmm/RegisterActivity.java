package com.example.mutiralmm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class RegisterActivity extends AppCompatActivity {

    EditText etNewUsername, etNewPassword;
    Button btnRegister;
    TextView tvBackToLogin; // Tambahkan TextView untuk kembali ke login

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNewUsername = findViewById(R.id.etNewUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin); // Hubungkan TextView

        //hubungkan dengan database lokal
        dbHelper = new DBHelper(this);

        btnRegister.setOnClickListener(v -> {
            String username = etNewUsername.getText().toString().trim();
            String password = etNewPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Isi semua field", Toast.LENGTH_SHORT).show();
            } else {
//                registerToServer(username, password); // Kirim ke server PHP
//            dbHelper.insertUser(username, password);
                if (dbHelper.insertUser(username, password)) {
                    Toast.makeText(RegisterActivity.this, "Register sukses", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                } else {
                    Toast.makeText(RegisterActivity.this, "Username atau password salah", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Aksi jika klik "Sudah punya akun? Login di sini"
        tvBackToLogin.setOnClickListener(v -> finish()); // Kembali ke LoginActivity
    }

//    private void registerToServer(String username, String password) {
//        String urlString = "http://192.168.1.5/project/register.php"; // Ganti dengan IP server PHP kamu
//
//        new Thread(() -> {
//            try {
//                URL url = new URL(urlString);
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("POST");
//                conn.setDoOutput(true);
//                conn.setDoInput(true);
//
//                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//
//                String postData = "username=" + URLEncoder.encode(username, "UTF-8")
//                        + "&password=" + URLEncoder.encode(password, "UTF-8");
//
//                writer.write(postData);
//                writer.flush();
//                writer.close();
//                os.close();
//
//                int responseCode = conn.getResponseCode();
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                    String response = in.readLine();
//                    in.close();
//
//                    runOnUiThread(() -> {
//                        if (response.equalsIgnoreCase("success")) {
//                            Toast.makeText(RegisterActivity.this, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show();
//                            finish(); // Kembali ke login
//                        } else if (response.equalsIgnoreCase("exists")) {
//                            Toast.makeText(RegisterActivity.this, "Username sudah terdaftar", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(RegisterActivity.this, "Pendaftaran gagal", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() ->
//                        Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
//                );
//            }
//        }).start();
//    }
}
