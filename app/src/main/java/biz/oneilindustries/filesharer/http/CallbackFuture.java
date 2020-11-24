package biz.oneilindustries.filesharer.http;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CallbackFuture implements Callback {
    public final CompletableFuture<Response> future = new CompletableFuture<>();

    public CallbackFuture() {}

    @Override
    public void onFailure(Call call, IOException e) {
        future.completeExceptionally(e);
    }

    @Override
    public void onResponse(Call call, Response response) {
        future.complete(response);
    }
}