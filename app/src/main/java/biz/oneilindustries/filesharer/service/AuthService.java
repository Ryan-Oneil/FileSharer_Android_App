package biz.oneilindustries.filesharer.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import biz.oneilindustries.filesharer.database.FileShareDatabaseManager;
import biz.oneilindustries.filesharer.exception.InvalidLoginException;
import biz.oneilindustries.filesharer.http.CallbackFuture;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static biz.oneilindustries.filesharer.config.Values.BACK_END_URL;

public class AuthService {

    private OkHttpClient client;
    private Context context;
    private SharedPreferences sharedPreferences;

    public AuthService(Context context) {
        this.context = context;
        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .callTimeout(5, TimeUnit.SECONDS)
                .build();
        sharedPreferences = context.getSharedPreferences("FileShare",
                MODE_PRIVATE);
    }

    public boolean loginUser(final String username, final String password) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        //Saves the user login details for fetching refresh tokens in future use
        edit.putString("username", username);
        edit.putString("password", password);
        edit.commit();

        RequestBody body = RequestBody.create(String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password), MediaType.get("application/json; charset=utf-8"));
        final Request request = new Request.Builder()
                .url(BACK_END_URL + "/login")
                .post(body)
                .build();

        CallbackFuture callbackFuture = new CallbackFuture();
        Call call = client.newCall(request);

        call.enqueue(callbackFuture);
        CompletableFuture<Response> futureResponse = callbackFuture.future.thenApplyAsync(response -> response);

        Response response = futureResponse.join();

        if (!response.isSuccessful()) {
            final Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> Toast.makeText(context, "Invalid Login Details", Toast.LENGTH_SHORT).show());
            return false;
        } else {
            //Gets refresh jwt from response
            String refreshToken = response.header("Authorization");
            edit.putBoolean("isLoggedIn", true);
            edit.putString("refreshToken", refreshToken);

            edit.commit();

            return true;
        }
    }

    public String fetchRefreshToken() throws IOException {
        String refreshToken = "";
        String username = sharedPreferences.getString("username", "");
        String password = sharedPreferences.getString("password", "");

        if (username.isEmpty() || password.isEmpty()) {
            throw new InvalidLoginException("Missing Login credentials. Please relogin");
        }
        RequestBody body = RequestBody.create(String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password), MediaType.get("application/json; charset=utf-8"));

        final Request request = new Request.Builder()
                .url(BACK_END_URL + "/login")
                .post(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        SharedPreferences.Editor edit = sharedPreferences.edit();

        if (!response.isSuccessful()) {
            edit.remove("password");
            edit.putBoolean("isLoggedIn", false);
        } else {
            refreshToken = response.header("Authorization");
            edit.putString("refreshToken", refreshToken);
            edit.putBoolean("isLoggedIn", true);
        }
        edit.commit();

        return refreshToken;
    }

    public String getRefreshToken() {
        return sharedPreferences.getString("refreshToken", "");
    }

    public String getAuthToken() {
        return sharedPreferences.getString("authToken", "");
    }

    public String fetchAuthToken() throws IOException {
        String authToken = "";
        RequestBody body = RequestBody.create(getRefreshToken(), null);

        final Request request = new Request.Builder()
                .url(BACK_END_URL + "/token/refresh")
                .post(body)
                .build();

        Call call = client.newCall(request);

        Response response = call.execute();
        SharedPreferences.Editor edit = sharedPreferences.edit();

        if (!response.isSuccessful()) {
            edit.remove("refreshToken");
        } else {
            authToken = response.body().string();
            edit.putString("authToken", authToken);
        }
        edit.commit();

        return authToken;
    }

    public void logout() {
        FileShareDatabaseManager fileShareDatabaseManager = new FileShareDatabaseManager(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();

        fileShareDatabaseManager.open();
        fileShareDatabaseManager.clearDatabase();
        fileShareDatabaseManager.close();

        edit.remove("username");
        edit.remove("password");
        edit.remove("refreshToken");
        edit.remove("authToken");
        edit.remove("isLoggedIn");

        edit.commit();
    }

    public boolean registerNewAccount(String username, String password, String email) {
        RequestBody body = RequestBody.create(String.format("{\"name\": \"%s\", \"password\": \"%s\", \"email\": \"%s\"}", username, password, email),
                MediaType.get("application/json; charset=utf-8"));
        final Request request = new Request.Builder()
                .url(BACK_END_URL + "/auth/register")
                .post(body)
                .build();

        CallbackFuture callbackFuture = new CallbackFuture();
        Call call = client.newCall(request);

        call.enqueue(callbackFuture);
        CompletableFuture<Response> futureResponse = callbackFuture.future.thenApplyAsync(response -> response);
        CompletableFuture<String> futureMessage = futureResponse.thenApplyAsync(response -> {
            if (!response.isSuccessful()) {
                try {
                    return response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return "";
        });
        String error = futureMessage.join();
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        if (!error.isEmpty()) {
            mainHandler.post(() -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show());
            return false;
        } else {
            mainHandler.post(() -> Toast.makeText(context, "A confirmation email has been sent", Toast.LENGTH_SHORT).show());
            return true;
        }
    }
}
