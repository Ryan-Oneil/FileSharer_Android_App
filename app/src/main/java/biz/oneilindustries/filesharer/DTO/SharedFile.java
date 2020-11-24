package biz.oneilindustries.filesharer.DTO;

public class SharedFile {

    private String id;
    private String name;
    private long size;
    private Link link;

    public SharedFile() {
    }

    public SharedFile(String id, String name, long size, Link link) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }
}
