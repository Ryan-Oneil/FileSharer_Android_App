package biz.oneilindustries.filesharer.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {

    private String id;
    private String title;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.s")
    private Date expiryDatetime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.s")
    private Date creationDate;
    private long size = 0;
    private long views = 0;

    @JsonIgnore
    private List<SharedFile> files;

    public Link() {
    }

    public Link(String id, String title, Date expiryDatetime, Date creationDate, long size, long views) {
        this.id = id;
        this.title = title;
        this.expiryDatetime = expiryDatetime;
        this.creationDate = creationDate;
        this.size = size;
        this.views = views;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getExpiryDatetime() {
        return expiryDatetime;
    }

    public void setExpiryDatetime(Date expiryDatetime) {
        this.expiryDatetime = expiryDatetime;
    }

    public List<SharedFile> getFiles() {
        return files;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public long getViews() {
        return views;
    }

    public void setFiles(List<SharedFile> files) {
        this.files = files;
    }

    public void addFile(SharedFile file) {
        if (this.files == null) {
            this.files = new ArrayList<>();
        }
        this.files.add(file);
    }

    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

        return "{" +
                "\"title\":\"" + title + '\"' +
                ", \"expires\": \"" + formatter.format(expiryDatetime) + "\"}";
    }
}
