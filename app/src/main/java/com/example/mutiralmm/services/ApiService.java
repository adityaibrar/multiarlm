package com.example.mutiralmm.services;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiService {

    private static final String TAG = "ApiService";
    private static final String BASE_URL = "http://10.0.2.2:8000/multiarlm/"; // Ganti dengan IP server Anda

    private OkHttpClient client;
    private Context context;

    public ApiService(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    // Interface untuk callback
    public interface ApiCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }

    public interface ApiListCallback {
        void onSuccess(JSONArray response);
        void onError(String error);
    }

    // Authentication Methods
    public void register(String username, String password, ApiCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    json.toString()
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "auth.php?action=register")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Register request failed", e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    handleJsonResponse(response, callback);
                }
            });

        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
        }
    }

    public void login(String username, String password, ApiCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    json.toString()
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "auth.php?action=login")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Login request failed", e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    handleJsonResponse(response, callback);
                }
            });

        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
        }
    }

    // Document Methods
    public void uploadDocument(int userId, String docName, String docDate,
                               String docNumber, String docDesc, File imageFile, ApiCallback callback) {

        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), imageFile);

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", String.valueOf(userId))
                .addFormDataPart("doc_name", docName)
                .addFormDataPart("doc_date", docDate)
                .addFormDataPart("doc_number", docNumber)
                .addFormDataPart("doc_desc", docDesc)
                .addFormDataPart("image", imageFile.getName(), fileBody);

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(BASE_URL + "dokumen.php?action=upload")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Upload document request failed", e);
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleJsonResponse(response, callback);
            }
        });
    }

    public void getDocuments(int userId, int page, int limit, String search, String year, ApiCallback callback) {
        String url = BASE_URL + "dokumen.php?action=list&user_id=" + userId +
                "&page=" + page + "&year="+ year +"&limit=" + limit;

        if (search != null && !search.isEmpty()) {
            url += "&search=" + search;
        }

        if (year != null && !year.isEmpty()) {
            url += "&year=" + year;
        }

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Get documents request failed", e);
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleJsonResponse(response, callback);
            }
        });
    }

    public void getAlbums(int userId, ApiCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "dokumen.php?action=get_by_year&user_id=" + userId)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Get albums request failed", e);
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleJsonResponse(response, callback);
            }
        });
    }

    public void deleteDocument(int documentId, int userId, ApiCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("document_id", documentId);
            json.put("user_id", userId);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    json.toString()
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "dokumen.php?action=delete")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Delete document request failed", e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    handleJsonResponse(response, callback);
                }
            });

        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
        }
    }

    public void updateDocument(int documentId, int userId, String docName, String docDate,
                               String docNumber, String docDesc, ApiCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("document_id", documentId);
            json.put("user_id", userId);

            if (docName != null) json.put("doc_name", docName);
            if (docDate != null) json.put("doc_date", docDate);
            if (docNumber != null) json.put("doc_number", docNumber);
            if (docDesc != null) json.put("doc_desc", docDesc);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    json.toString()
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "dokumen.php?action=update")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Update document request failed", e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    handleJsonResponse(response, callback);
                }
            });

        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
        }
    }

    // Helper method untuk handle JSON response
    private void handleJsonResponse(Response response, ApiCallback callback) throws IOException {
        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            Log.d(TAG, "Response: " + responseBody);

            try {
                JSONObject jsonResponse = new JSONObject(responseBody);
                callback.onSuccess(jsonResponse);
            } catch (JSONException e) {
                Log.e(TAG, "JSON parsing error", e);
                callback.onError("Invalid response format");
            }
        } else {
            Log.e(TAG, "Request failed with code: " + response.code());
            callback.onError("Server error: " + response.code());
        }
    }

    // Utility method untuk mendapatkan URL gambar lengkap
    public String getImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        // Jika path sudah lengkap (dimulai dengan http), return as is
        if (imagePath.startsWith("http")) {
            return imagePath;
        }

        // Jika path relatif, tambahkan base URL
        return BASE_URL + imagePath;
    }
}