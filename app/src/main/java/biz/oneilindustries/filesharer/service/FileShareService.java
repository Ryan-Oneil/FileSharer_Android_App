package biz.oneilindustries.filesharer.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import biz.oneilindustries.filesharer.DTO.Link;
import biz.oneilindustries.filesharer.DTO.SharedFile;
import biz.oneilindustries.filesharer.database.FileShareDatabaseManager;
import biz.oneilindustries.filesharer.http.APICaller;
import biz.oneilindustries.filesharer.http.FileUploader;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static biz.oneilindustries.filesharer.config.Values.APP_NAME;

public class FileShareService {

    private APICaller apiCaller;
    private SharedPreferences sharedPreferences;
    private ObjectMapper objectMapper;
    private FileShareDatabaseManager fileShareDatabaseManager;
    private Context context;
    private FileUploader fileUploader;

    public FileShareService(Context context) {
        this.context = context;
        apiCaller = new APICaller(context);
        sharedPreferences = context.getSharedPreferences(APP_NAME,
                MODE_PRIVATE);
        objectMapper = new ObjectMapper();
        this.fileShareDatabaseManager = new FileShareDatabaseManager(context);
        this.fileUploader = new FileUploader(new AuthService(context));
    }

    public List<Link> fetchUserLinks() {
        String username = sharedPreferences.getString("username", "uni");
        CompletableFuture<Response> futureResponse = apiCaller.getCall(String.format("/user/%s/links", username));

        CompletableFuture<List<Link>> future = futureResponse.thenApplyAsync(response -> {
            try {
                String links = JsonPath.parse(response.body().string()).read("$..links.*").toString();

                return objectMapper.readValue(links, new TypeReference<List<Link>>() {});
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
        List<Link> links = future.join();

        fileShareDatabaseManager.open();

        links.forEach(link -> fileShareDatabaseManager.updateOrInsertLink(link));

        fileShareDatabaseManager.close();

        return links;
    }

    public Link fetchLinkDetails(String linkId) {
        CompletableFuture<Response> futureResponse = apiCaller.getCall(String.format("/info/%s", linkId));

        CompletableFuture<Link> future = futureResponse.thenApplyAsync(response -> {
            try {
                String responseString = response.body().string();
                String files = JsonPath.parse(responseString).read("$..files.*").toString();
                Link link = objectMapper.readValue(responseString, Link.class);
                List<SharedFile> filesList = objectMapper.readValue(files, new TypeReference<List<SharedFile>>() {});

                link.setFiles(filesList);

                return link;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
        Link link = future.join();

        fileShareDatabaseManager.open();

        link.getFiles().forEach(sharedFile -> fileShareDatabaseManager.updateOrInsertFile(sharedFile, link.getId()));

        fileShareDatabaseManager.close();

        return link;
    }

    public ArrayList<Link> getUserLinks() {
        fileShareDatabaseManager.open();

        ArrayList<Link> links = fileShareDatabaseManager.getLinks();

        fileShareDatabaseManager.close();

        return links;
    }

    public ArrayList<SharedFile> getLinkFiles(Link link) {
        fileShareDatabaseManager.open();

        ArrayList<SharedFile> files = fileShareDatabaseManager.getsLinkFiles(link.getId());
        files.forEach(file -> file.setLink(link));
        link.setFiles(files);

        fileShareDatabaseManager.close();

        return files;
    }

    public String deleteFile(SharedFile file) {
        fileShareDatabaseManager.open();
        fileShareDatabaseManager.deleteFile(file);
        fileShareDatabaseManager.close();

        CompletableFuture<Response> futureResponse = apiCaller.deleteCall(String.format("/file/delete/%s", file.getId()));

        CompletableFuture<String> future = futureResponse.thenApplyAsync(response -> {
            try {
                if (response.isSuccessful()) {
                    return "Deleted File";
                }else {
                    return response.body().string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Unable to delete file";
        });
        return future.join();
    }

    public Link getLocalLink(String linkId) {
        fileShareDatabaseManager.open();

        Link link = fileShareDatabaseManager.getLink(linkId);

        fileShareDatabaseManager.close();

        return link;
    }

    public String deleteLink(Link link) {
        fileShareDatabaseManager.open();
        fileShareDatabaseManager.deleteLink(link);
        fileShareDatabaseManager.close();

        CompletableFuture<Response> futureResponse = apiCaller.deleteCall(String.format("/delete/%s", link.getId()));
        CompletableFuture<String> future = futureResponse.thenApplyAsync(response -> {
            try {
                if (response.isSuccessful()) {
                    return "Deleted Link";
                } else {
                    return response.body().string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Unable to delete Link";
        });
        return future.join();
    }

    public void addNewFilesToLink(List<SharedFile> files, Link link) {
        fileShareDatabaseManager.open();
        files.forEach(file -> fileShareDatabaseManager.updateOrInsertFile(file, link.getId()));
        fileShareDatabaseManager.close();
    }

    public File getCachedFile(Uri uri, String filePath) {
        File file = new File(filePath);

        try {
            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r", null);

            InputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
            File cacheFile = new File(context.getCacheDir(), file.getName());
            OutputStream outputStream = new FileOutputStream(cacheFile);
            IOUtils.copy(inputStream, outputStream);

            return cacheFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String updateLinkDetails(Link link) {
        String json = link.toString();

        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        CompletableFuture<Response> futureResponse = apiCaller.updateCall(String.format("/link/edit/%s", link.getId()), requestBody);
        CompletableFuture<String> errorMessage = futureResponse.thenApplyAsync(response -> {
            if (response.isSuccessful()) {
                fileShareDatabaseManager.open();
                fileShareDatabaseManager.updateOrInsertLink(link);
                fileShareDatabaseManager.close();
            } else {
                try {
                    return response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return "";
        });
        return errorMessage.join();
    }
}
