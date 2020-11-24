package biz.oneilindustries.filesharer.DTO;

import java.io.File;
import java.util.List;

public class UploadTask {

    private String url;
    private List<File> files;
    private Link link;

    public UploadTask(String url, List<File> files, Link link) {
        this.url = url;
        this.files = files;
        this.link = link;
    }

    public String getUrl() {
        return url;
    }

    public List<File> getFiles() {
        return files;
    }

    public Link getLink() {
        return link;
    }
}
