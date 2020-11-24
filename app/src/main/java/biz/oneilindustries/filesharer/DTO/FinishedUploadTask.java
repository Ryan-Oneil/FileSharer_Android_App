package biz.oneilindustries.filesharer.DTO;

import java.util.List;

public class FinishedUploadTask {

    private List<SharedFile> files;
    private String linkId;

    public FinishedUploadTask(List<SharedFile> files, String linkId) {
        this.files = files;
        this.linkId = linkId;
    }

    public List<SharedFile> getFiles() {
        return files;
    }

    public String getLinkId() {
        return linkId;
    }
}
