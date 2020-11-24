package biz.oneilindustries.filesharer.ui;

import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import biz.oneilindustries.filesharer.FileUtils;
import biz.oneilindustries.filesharer.service.FileShareService;

import static biz.oneilindustries.filesharer.ui.ManageLinkFragment.PICKFILE_RESULT_CODE;

public abstract class FileChooserFragment extends Fragment {

    private FileShareService fileShareService;

    public FileChooserFragment(FileShareService fileShareService) {
        this.fileShareService = fileShareService;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICKFILE_RESULT_CODE && data != null) {
            ArrayList<File> files = new ArrayList<>();
            FileUtils fileUtils = new FileUtils(getContext());

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                int index = 0;

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

            if (!files.isEmpty()) {
                fileProcessAction(files);
//                ManageLinkFragment.UploadNewFilesToLink uploadNewFilesToLink = new ManageLinkFragment.UploadNewFilesToLink();
//                UploadTask uploadTask = new UploadTask(String.format("%s/link/add/%s", BACK_END_URL, link.getId()), files, link);
//                uploadNewFilesToLink.execute(uploadTask);
            }
        }
    }

    public abstract void fileProcessAction(List<File> files);
}
