package biz.oneilindustries.filesharer.ui;

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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import biz.oneilindustries.filesharer.DTO.UploadTask;
import biz.oneilindustries.filesharer.R;
import biz.oneilindustries.filesharer.listadapters.SelectedFileAdapter;
import biz.oneilindustries.filesharer.http.FileUploader;
import biz.oneilindustries.filesharer.service.AuthService;
import biz.oneilindustries.filesharer.service.FileShareService;

import static biz.oneilindustries.filesharer.config.Values.BACK_END_URL;

public class UploadFragment extends FileChooserFragment {

    private SelectedFileAdapter selectedFileAdapter;
    private ArrayList<File> selectedFiles = new ArrayList<>();
    private FileShareService fileShareService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_upload, container, false);
        fileShareService = new FileShareService(getContext());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());// 2018-06-19T22:38:28+0000
        Date expires = fileShareService.getTwoWeeksExpiry();

        Button addFilesButton = root.findViewById(R.id.share_add_files_button);
        addFilesButton.setOnClickListener(getButtonClickListener());

        EditText editText = root.findViewById(R.id.share_link_expires_input);
        editText.setText(String.format("Expires: %s", expires.toString()));

        Button shareButton = root.findViewById(R.id.share_add_share_button);
        shareButton.setOnClickListener(v -> {
            if (selectedFiles.isEmpty()) {
                Toast.makeText(getContext(), "No files selected", Toast.LENGTH_SHORT).show();
            } else {
                EditText titleInput = root.findViewById(R.id.share_link_title_input);
                String title = titleInput.getText().toString();
                String uploadURL = String.format("%s/share?title=%s&expires=%s", BACK_END_URL, title, formatter.format(expires));

                ShareNewLinkTask shareNewLinkTask = new ShareNewLinkTask();
                shareNewLinkTask.execute(new UploadTask(uploadURL, selectedFiles, null));
            }
        });
        selectedFileAdapter = new SelectedFileAdapter(getContext(), R.layout.uploaded_files_list_item, R.id.link_share_new_files, selectedFiles);
        ListView chosenFiles = root.findViewById(R.id.link_share_new_files);
        chosenFiles.setAdapter(selectedFileAdapter);

        return root;
    }

    @Override
    public void fileProcessAction(List<File> files) {
        files.forEach(file -> selectedFileAdapter.add(file));
    }

    private class ShareNewLinkTask extends AsyncTask<UploadTask, Void, String> {

        @Override
        protected String doInBackground(UploadTask... uploadTasks) {
            FileUploader fileUploader = new FileUploader(new AuthService(getContext()));
            UploadTask uploadTask = uploadTasks[0];

            try {
                return fileUploader.uploadFiles(uploadTask.getFiles(), uploadTask.getUrl());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String linkURL) {
            String linkId = linkURL.substring(linkURL.lastIndexOf("/") + 1);

            //Fetches newly uploaded link details as the upload api returns the shared url
            fileShareService.fetchLinkDetails(linkId);

            super.onPostExecute(linkURL);
            Toast.makeText(getContext(), "Files have been shared!", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        }
    }
}