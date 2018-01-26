package astrocloud.zw.co.astrocloud.models;

import java.io.Serializable;

/**
 * Created by Percy M on 12/13/2017.
 */

public class Model implements Serializable{
    private String url;
    private String name;
    private Long sizeInBytes;
    private int id;
    private String mime;
    private String dateCreated;


    public Model() {
    }

    public Model(String path, String name, String mime,Long sizeInBytes, int id, String DateCreated) {
        this.url = path;
        this.name = name;
        this.sizeInBytes = sizeInBytes;
        this.id = id;

    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public int getId() {
        return id;
    }

    public void setId(String key) {
        this.id = id;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
