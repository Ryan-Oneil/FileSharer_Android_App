package biz.oneilindustries.filesharer.ui;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import biz.oneilindustries.filesharer.DTO.FinishedUploadTask;
import biz.oneilindustries.filesharer.DTO.Link;
import biz.oneilindustries.filesharer.DTO.SharedFile;
import biz.oneilindustries.filesharer.DTO.UploadTask;
import biz.oneilindustries.filesharer.R;
import biz.oneilindustries.filesharer.SharedFileAdapter;
import biz.oneilindustries.filesharer.http.FileUploader;
import biz.oneilindustries.filesharer.service.AuthService;
import biz.oneilindustries.filesharer.service.FileShareService;

import static biz.oneilindustries.filesharer.config.Values.BACK_END_URL;
import static biz.oneilindustries.filesharer.config.Values.SHARE_URL;

public class ManageLinkFragment extends FileChooserFragment {

    public static final int PICKFILE_RESULT_CODE = 1;
    private FileShareService fileShareService;
    private Link link;
    private SharedFileAdapter sharedFileAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_manage_shared_link, container, false);
        fileShareService = new FileShareService(root.getContext());

        if (ContextCompat.checkSelfPermission(root.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(root.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        Bundle bundle = getArguments();
        String linkId = bundle.getString("id");
        link = fileShareService.getLocalLink(linkId);

        if (link == null) {
            getActivity().onBackPressed();

            return root;
        }

        EditText title = root.findViewById(R.id.editLinkTitle);
        title.setText(link.getTitle().isEmpty() ? "No Title Set" : link.getTitle());

        EditText expires = root.findViewById(R.id.editExpires);
        expires.setText(link.getExpiryDatetime().toString());

        ArrayList<SharedFile> files = fileShareService.getLinkFiles(link);

        if (files.isEmpty()) {
            fileShareService.fetchLinkDetails(linkId);

            files = fileShareService.getLinkFiles(link);
        }
        ListView listView = root.findViewById(R.id.link_manage_view_files);
        sharedFileAdapter = new SharedFileAdapter(root.getContext(), R.layout.uploaded_files_list_item, R.id.link_manage_view_files, files);
        listView.setAdapter(sharedFileAdapter);

        Button deleteLink = root.findViewById(R.id.deleteLinkButton);
        deleteLink.setOnClickListener(v -> {
            fileShareService.deleteLink(link);
            getActivity().onBackPressed();
        });

        Button copyURL = root.findViewById(R.id.copyUrlButton);
        copyURL.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) root.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Share URL", SHARE_URL + link.getId());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(root.getContext(), "Copied URL", Toast.LENGTH_SHORT).show();
        });

        Button viewURL = root.findViewById(R.id.viewLinkButton);
        viewURL.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SHARE_URL + link.getId()));
            startActivity(browserIntent);
        });

        Button addFiles = root.findViewById(R.id.addFilesToLinkButton);
        addFiles.setOnClickListener(getButtonClickListener());

        Button saveLinkButton = root.findViewById(R.id.saveLinkButton);
        saveLinkButton.setOnClickListener(v -> {
            String titleValue = title.getText().toString();

            if (!titleValue.isEmpty() && !titleValue.equalsIgnoreCase(link.getTitle())) {
                link.setTitle(titleValue);
                String errorMessage = fileShareService.updateLinkDetails(link);

                if (!errorMessage.isEmpty()) {
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Updated Link Details", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return root;
    }

    @Override
    public void fileProcessAction(List<File> files) {
        UploadNewFilesToLink uploadNewFilesToLink = new UploadNewFilesToLink();
        UploadTask uploadTask = new UploadTask(String.format("%s/link/add/%s", BACK_END_URL, link.getId()), files, link);
        uploadNewFilesToLink.execute(uploadTask);
    }

    private class UploadNewFilesToLink extends AsyncTask<UploadTask, Void, FinishedUploadTask> {

        @Override
        protected FinishedUploadTask doInBackground(UploadTask... uploadTasks) {
            FileUploader fileUploader = new FileUploader(new AuthService(getContext()));
            UploadTask uploadTask = uploadTasks[0];

            try {
                return new FinishedUploadTask(new ObjectMapper().readValue(fileUploader.uploadFiles(uploadTask.getFiles(), uploadTask.getUrl()),
                        new TypeReference<List<SharedFile>>() {}), uploadTask.getLink().getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(FinishedUploadTask finishedUploadTask) {
            fileShareService.addNewFilesToLink(finishedUploadTask.getFiles(), link);
            finishedUploadTask.getFiles().forEach(file -> sharedFileAdapter.add(file));

            super.onPostExecute(finishedUploadTask);
        }
    }
}