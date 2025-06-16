package com.example.mutiralmm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mutiralmm.services.ApiService;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    EditText etNewUsername, etNewPassword;
    Button btnRegister;
    TextView tvBackToLogin;

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNewUsername = findViewById(R.id.etNewUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        apiService = new ApiService(this);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etNewUsername.getText().toString().trim();
                String password = etNewPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Username dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validasi panjang password minimal
                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnRegister.setEnabled(false);
                btnRegister.setText("Registering...");

                // Gunakan method register, bukan login
                apiService.register(username, password, new ApiService.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        runOnUiThread(() -> {
                            try {
                                boolean success = response.getBoolean("success");

                                if (success) {
                                    Toast.makeText(RegisterActivity.this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_SHORT).show();

                                    // Kembali ke LoginActivity setelah registrasi berhasil
                                    finish();

                                } else {
                                    String message = response.optString("message", "Registrasi gagal");
                                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(RegisterActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            } finally {
                                btnRegister.setEnabled(true);
                                btnRegister.setText("Register");
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, "Registrasi gagal: " + error, Toast.LENGTH_SHORT).show();
                            btnRegister.setEnabled(true);
                            btnRegister.setText("Register");
                        });
                    }
                });
            }
        });

        // Aksi jika klik "Sudah punya akun? Login di sini"
        tvBackToLogin.setOnClickListener(v -> finish()); // Kembali ke LoginActivity
    }
}