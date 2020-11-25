package biz.oneilindustries.filesharer.http;

import android.content.Context;

import java.util.concurrent.CompletableFuture;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static biz.oneilindustries.filesharer.config.Values.BACK_END_URL;

public class APICaller {

    private OkHttpClient httpClient;

    public APICaller(Context context) {
        httpClient = new OkHttpClient.Builder()
                .authenticator(new TokenAuth(context))
                .followRedirects(false)
                .retryOnConnectionFailure(true)
                .build();
    }

    public CompletableFuture<Response> getCall(String endpoint) {
        final Request request = new Request.Builder()
                .url(BACK_END_URL + endpoint)
                .get()
                .build();

        CallbackFuture future = new CallbackFuture();
        httpClient.newCall(request).enqueue(future);

        return future.future.thenApply(response -> response);
    }

    public CompletableFuture<Response> postCall(String endpoint, RequestBody body) {
        final Request request = new Request.Builder()
                .url(BACK_END_URL + endpoint)
                .post(body)
                .build();

        CallbackFuture future = new CallbackFuture();
        httpClient.newCall(request).enqueue(future);

        return future.future.thenApply(response -> response);
    }

    public CompletableFuture<Response> deleteCall(String endpoint) {
        final Request request = new Request.Builder()
                .url(BACK_END_URL + endpoint)
                .delete()
                .build();

        CallbackFuture future = new CallbackFuture();
        httpClient.newCall(request).enqueue(future);

        return future.future.thenApply(response -> response);
    }

    public CompletableFuture<Response> updateCall(String endpoint, RequestBody requestBody) {
        final Request request = new Request.Builder()
                .url(BACK_END_URL + endpoint)
                .put(requestBody)
                .build();

        CallbackFuture future = new CallbackFuture();
        httpClient.newCall(request).enqueue(future);

        return future.future.thenApply(response -> response);
    }
}
