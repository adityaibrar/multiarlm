package com.example.mutiralmm.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.mutiralmm.LoginActivity;

/**
 * SessionManager untuk mengelola session user dan navigasi
 * Mengurangi kode redundan untuk validasi session dan redirect
 */
public class SessionManager {

    private SharedPreferencesHelper prefsHelper;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        this.prefsHelper = SharedPreferencesHelper.getInstance(context);
    }

    /**
     * Menyimpan session user setelah login berhasil
     * @param userId ID user
     * @param username Username
     * @param fullName Nama lengkap
     */
    public void createUserSession(int userId, String username, String fullName) {
        prefsHelper.saveUserSession(userId, username, fullName);
    }

    /**
     * Mendapatkan data user saat ini
     * @return UserData object
     */
    public SharedPreferencesHelper.UserData getCurrentUser() {
        return prefsHelper.getUserData();
    }

    /**
     * Mengecek apakah user sudah login
     * @return true jika sudah login dan session valid
     */
    public boolean isUserLoggedIn() {
        return prefsHelper.isSessionValid();
    }

    /**
     * Validasi session user dengan auto redirect ke login jika tidak valid
     * @param activity Activity yang melakukan validasi
     * @return true jika session valid, false jika tidak valid (dan sudah redirect)
     */
    public boolean validateSession(Activity activity) {
        return validateSession(activity, true);
    }

    /**
     * Validasi session user
     * @param activity Activity yang melakukan validasi
     * @param autoRedirect Apakah otomatis redirect ke login jika session tidak valid
     * @return true jika session valid, false jika tidak valid
     */
    public boolean validateSession(Activity activity, boolean autoRedirect) {
        if (!prefsHelper.isSessionValid()) {
            if (autoRedirect) {
                showSessionExpiredAndRedirect(activity);
            }
            return false;
        }
        return true;
    }

    /**
     * Validasi session dengan custom message
     * @param activity Activity yang melakukan validasi
     * @param customMessage Pesan custom yang akan ditampilkan
     * @return true jika session valid, false jika tidak valid
     */
    public boolean validateSession(Activity activity, String customMessage) {
        if (!prefsHelper.isSessionValid()) {
            showSessionExpiredAndRedirect(activity, customMessage);
            return false;
        }
        return true;
    }

    /**
     * Logout user dan redirect ke login
     * @param activity Activity yang melakukan logout
     */
    public void logout(Activity activity) {
        logout(activity, "Logout berhasil");
    }

    /**
     * Logout user dengan custom message
     * @param activity Activity yang melakukan logout
     * @param message Pesan yang akan ditampilkan
     */
    public void logout(Activity activity, String message) {
        prefsHelper.clearUserSession();
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        redirectToLogin(activity);
    }

    /**
     * Menampilkan pesan session expired dan redirect ke login
     * @param activity Activity yang akan di-finish
     */
    public void showSessionExpiredAndRedirect(Activity activity) {
        showSessionExpiredAndRedirect(activity, "Sesi login telah berakhir. Silakan login kembali.");
    }

    /**
     * Menampilkan pesan session expired dan redirect ke login dengan custom message
     * @param activity Activity yang akan di-finish
     * @param message Pesan custom
     */
    public void showSessionExpiredAndRedirect(Activity activity, String message) {
        prefsHelper.clearUserSession();
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        redirectToLogin(activity);
    }

    /**
     * Redirect ke LoginActivity
     * @param activity Activity yang akan di-finish
     */
    public void redirectToLogin(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * Handle error dari API yang mungkin karena unauthorized
     * @param activity Activity yang melakukan request
     * @param error Error message dari API
     * @return true jika error karena unauthorized dan sudah di-handle, false jika bukan
     */
    public boolean handleApiError(Activity activity, String error) {
        if (error.contains("401") || error.contains("unauthorized")) {
            showSessionExpiredAndRedirect(activity);
            return true;
        }
        return false;
    }

    /**
     * Mendapatkan user ID saat ini
     * @return User ID, -1 jika tidak valid
     */
    public int getCurrentUserId() {
        return prefsHelper.getUserId();
    }

    /**
     * Mendapatkan username saat ini
     * @return Username, empty string jika tidak ada
     */
    public String getCurrentUsername() {
        return prefsHelper.getUsername();
    }

    /**
     * Mendapatkan nama lengkap user saat ini
     * @return Nama lengkap, empty string jika tidak ada
     */
    public String getCurrentFullName() {
        return prefsHelper.getFullName();
    }

    /**
     * Auto redirect ke dashboard jika user sudah login (untuk LoginActivity)
     * @param activity LoginActivity
     * @param dashboardClass Class dashboard yang akan dituju
     */
    public void checkAutoLogin(Activity activity, Class<?> dashboardClass) {
        if (isUserLoggedIn()) {
            Intent intent = new Intent(activity, dashboardClass);
            activity.startActivity(intent);
            activity.finish();
        }
    }
}