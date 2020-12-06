package biz.oneilindustries.filesharer.ui;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import biz.oneilindustries.filesharer.FileUtils;
import biz.oneilindustries.filesharer.service.FileShareService;

import static biz.oneilindustries.filesharer.ui.ManageLinkFragment.PICKFILE_RESULT_CODE;

public abstract class FileChooserFragment extends Fragment {

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICKFILE_RESULT_CODE && data != null) {
            FileShareService fileShareService = new FileShareService(getContext());

            ArrayList<File> files = new ArrayList<>();
            FileUtils fileUtils = new FileUtils(getContext());

            //Checks if multiple files were selected
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                int index = 0;

                //Loops through all files
                while (index < count) {
                    Uri uri = data.getClipData().getItemAt(index).getUri();
                    String filePath = fileUtils.getPath(uri);
                    File file = fileShareService.getCachedFile(uri, filePath);

                    if (file.exists()) {
                        files.add(file);
                    }
                    index++;
                }
            } else if (data.getData() != null) {
                String filePath = fileUtils.getPath(data.getData());
                File file = fileShareService.getCachedFile(data.getData(), filePath);

                if (file != null && file.exists()) {
                    files.add(file);
                }
            }

            //Calls the abstract method which is implemented by sub fragment
            if (!files.isEmpty()) {
                fileProcessAction(files);
            }
        }
    }

    public abstract void fileProcessAction(List<File> files);

    //Returns the button click listener for opening the file browser/chooser
    public View.OnClickListener getButtonClickListener() {
        return v -> {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
        };
    }
}
