package astrocloud.zw.co.astrocloud.models;

import java.io.Serializable;

/**
 * Created by Percy M on 12/13/2017.
 */

public class ImageModel  implements Serializable{
    private String url;
    private String name;
    private Long sizeInBytes;
    private String key;



    public ImageModel() {
    }

    public ImageModel(String downloadUrl, String name, Long sizeInBytes, String key) {
        this.url = downloadUrl;
        this.name = name;
        this.sizeInBytes = sizeInBytes;
        this.key = key;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
