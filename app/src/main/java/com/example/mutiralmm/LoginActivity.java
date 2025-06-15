package com.example.mutiralmm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mutiralmm.services.ApiService;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvRegister;
    DBHelper dbHelper;
    ApiService apiService;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        dbHelper = new DBHelper(this);
        apiService = new ApiService(this);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Validasi input
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Username dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tampilkan loading (opsional)
                btnLogin.setEnabled(false);
                btnLogin.setText("Logging in...");

                apiService.login(username, password, new ApiService.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        runOnUiThread(() -> {
                            try {
                                boolean success = response.getBoolean("success");

                                if (success) {
                                    // Ambil data user dari response
                                    JSONObject userData = response.getJSONObject("data");
                                    int userId = userData.getInt("user_id");
                                    String userFullName = userData.optString("full_name", username);

                                    // Simpan data user ke SharedPreferences
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt("user_id", userId);
                                    editor.putString("username", username);
                                    editor.putString("full_name", userFullName);
                                    editor.putBoolean("is_logged_in", true);
                                    editor.apply(); // Gunakan apply() untuk async

                                    Toast.makeText(LoginActivity.this, "Login sukses", Toast.LENGTH_SHORT).show();

                                    // Pindah ke Dashboard
                                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                    intent.putExtra("user_id", userId); // Untuk backward compatibility
                                    startActivity(intent);
                                    finish(); // Tutup LoginActivity

                                } else {
                                    String message = response.optString("message", "Login gagal");
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(LoginActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            } finally {
                                // Reset tombol login
                                btnLogin.setEnabled(true);
                                btnLogin.setText("Login");
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Login gagal: " + error, Toast.LENGTH_SHORT).show();

                            // Reset tombol login
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Login");
                        });
                    }
                });
            }
        });

        // Arahkan ke halaman register
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Cek apakah user sudah login
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        if (isLoggedIn) {
            // Jika sudah login, langsung ke Dashboard
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }
}