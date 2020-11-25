package biz.oneilindustries.filesharer.http;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import biz.oneilindustries.filesharer.service.AuthService;

//Had to choose another HTTPClient mid way due to OkHTTP client not being able to transfer large files without crashes
public class FileUploader  {

    private AuthService authService;

    public FileUploader(AuthService authService) {
        this.authService = authService;
    }

    public String uploadFiles(List<File> files, String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        MultipartEntityBuilder entitybuilder = MultipartEntityBuilder.create();
        entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        files.forEach(file -> entitybuilder.addBinaryBody("file[]", file, ContentType.APPLICATION_OCTET_STREAM, file.getName()));

        HttpEntity mutiPartHttpEntity = entitybuilder.build();
        RequestBuilder reqbuilder = RequestBuilder.post()
                .setHeader("Authorization", getAuthToken())
                .setEntity(mutiPartHttpEntity)
                .setUri(url);

        HttpUriRequest multipartRequest = reqbuilder.build();

        try {
            HttpResponse httpresponse = httpclient.execute(multipartRequest);
            return EntityUtils.toString(httpresponse.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getAuthToken() throws IOException {
        String authToken = authService.getAuthToken();

        if (isJWTExpired(authToken)) {
            String refreshToken = authService.getRefreshToken();

            if (isJWTExpired(refreshToken)) {
                authService.fetchRefreshToken();
            }
            authToken = authService.fetchAuthToken();
        }
        return authToken;
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
