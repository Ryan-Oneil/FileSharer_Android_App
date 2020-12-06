package biz.oneilindustries.filesharer.http;

import android.content.Context;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Date;

import biz.oneilindustries.filesharer.service.AuthService;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

//Class to be used during auth calls in okhttp httpclient
public class TokenAuth implements Authenticator {

    private AuthService authService;

    public TokenAuth(Context context) {
        this.authService = new AuthService(context);
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NotNull Response response) throws IOException {
        String refreshToken = authService.getRefreshToken();

        synchronized (this) {
            if (refreshToken.isEmpty() || isJWTExpired(refreshToken)) {
                authService.fetchRefreshToken();
            }
            final String newAuthToken = authService.fetchAuthToken();

            return response.request().newBuilder()
                    .header("Authorization", newAuthToken)
                    .build();
        }
    }

    private boolean isJWTExpired(String token) {
        try {
            token = token.replace("Refresh ", "");
            DecodedJWT jwt = JWT.decode(token);

            return jwt.getExpiresAt().before(new Date());
        } catch (JWTDecodeException ignored){
        }
        return true;
    }
}
