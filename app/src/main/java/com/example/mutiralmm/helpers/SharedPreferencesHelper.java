package com.example.mutiralmm.helpers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class untuk mengelola SharedPreferences
 * Mengurangi kode redundan dan menyediakan interface yang bersih untuk menyimpan data user
 */
public class SharedPreferencesHelper {

    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static SharedPreferencesHelper instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    // Private constructor untuk implementasi Singleton
    private SharedPreferencesHelper(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    /**
     * Mendapatkan instance SharedPreferencesHelper (Singleton Pattern)
     * @param context Context aplikasi
     * @return Instance SharedPreferencesHelper
     */
    public static synchronized SharedPreferencesHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesHelper(context);
        }
        return instance;
    }

    /**
     * Menyimpan data login user
     * @param userId ID user
     * @param username Username user
     * @param fullName Nama lengkap user
     */
    public void saveUserSession(int userId, String username, String fullName) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULL_NAME, fullName != null ? fullName : username);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Mendapatkan user ID
     * @return User ID, default -1 jika tidak ada
     */
    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    /**
     * Mendapatkan username
     * @return Username, default empty string jika tidak ada
     */
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    /**
     * Mendapatkan nama lengkap user
     * @return Nama lengkap, default empty string jika tidak ada
     */
    public String getFullName() {
        return sharedPreferences.getString(KEY_FULL_NAME, "");
    }

    /**
     * Mengecek apakah user sudah login
     * @return true jika sudah login, false jika belum
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Validasi session user
     * @return true jika session valid (user login dan user ID valid)
     */
    public boolean isSessionValid() {
        return isLoggedIn() && getUserId() != -1;
    }

    /**
     * Menghapus semua data user (logout)
     */
    public void clearUserSession() {
        editor.clear();
        editor.apply();
    }

    /**
     * Mendapatkan data user dalam bentuk UserData object
     * @return UserData object berisi informasi user
     */
    public UserData getUserData() {
        return new UserData(
                getUserId(),
                getUsername(),
                getFullName(),
                isLoggedIn()
        );
    }

    /**
     * Menyimpan nilai string
     * @param key Key untuk menyimpan data
     * @param value Nilai yang akan disimpan
     */
    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Mendapatkan nilai string
     * @param key Key data yang ingin diambil
     * @param defaultValue Nilai default jika key tidak ditemukan
     * @return Nilai string
     */
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Menyimpan nilai integer
     * @param key Key untuk menyimpan data
     * @param value Nilai yang akan disimpan
     */
    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Mendapatkan nilai integer
     * @param key Key data yang ingin diambil
     * @param defaultValue Nilai default jika key tidak ditemukan
     * @return Nilai integer
     */
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    /**
     * Menyimpan nilai boolean
     * @param key Key untuk menyimpan data
     * @param value Nilai yang akan disimpan
     */
    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Mendapatkan nilai boolean
     * @param key Key data yang ingin diambil
     * @param defaultValue Nilai default jika key tidak ditemukan
     * @return Nilai boolean
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Menghapus data berdasarkan key tertentu
     * @param key Key yang akan dihapus
     */
    public void remove(String key) {
        editor.remove(key);
        editor.apply();
    }

    /**
     * Mengecek apakah key tertentu ada dalam SharedPreferences
     * @param key Key yang ingin dicek
     * @return true jika key ada, false jika tidak
     */
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    /**
     * Inner class untuk menyimpan data user
     */
    public static class UserData {
        private int userId;
        private String username;
        private String fullName;
        private boolean isLoggedIn;

        public UserData(int userId, String username, String fullName, boolean isLoggedIn) {
            this.userId = userId;
            this.username = username;
            this.fullName = fullName;
            this.isLoggedIn = isLoggedIn;
        }

        // Getters
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public boolean isLoggedIn() { return isLoggedIn; }

        // Method untuk validasi
        public boolean isValid() {
            return isLoggedIn && userId != -1;
        }

        @Override
        public String toString() {
            return "UserData{" +
                    "userId=" + userId +
                    ", username='" + username + '\'' +
                    ", fullName='" + fullName + '\'' +
                    ", isLoggedIn=" + isLoggedIn +
                    '}';
        }
    }
}