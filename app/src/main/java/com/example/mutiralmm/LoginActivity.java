package com.example.mutiralmm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mutiralmm.helpers.SessionManager;
import com.example.mutiralmm.services.ApiService;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvRegister;
    DBHelper dbHelper;
    ApiService apiService;

    private SessionManager sessionManager;

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
        sessionManager = new SessionManager(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Username dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnLogin.setEnabled(false);
                btnLogin.setText("Logging in...");

                apiService.login(username, password, new ApiService.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        runOnUiThread(() -> {
                            try {
                                boolean success = response.getBoolean("success");

                                if (success) {
                                    JSONObject userData = response.getJSONObject("data");
                                    int userId = userData.getInt("user_id");
                                    String userFullName = userData.optString("full_name", username);

                                    // MENGGUNAKAN SESSIONMANAGER - LEBIH SEDERHANA!
                                    sessionManager.createUserSession(userId, username, userFullName);

                                    Toast.makeText(LoginActivity.this, "Login sukses", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                    intent.putExtra("user_id", userId);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    String message = response.optString("message", "Login gagal");
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(LoginActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            } finally {
                                btnLogin.setEnabled(true);
                                btnLogin.setText("Login");
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Login gagal: " + error, Toast.LENGTH_SHORT).show();
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
        sessionManager.checkAutoLogin(this, DashboardActivity.class);
    }
}