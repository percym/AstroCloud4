package astrocloud.zw.co.astrocloud.models;

/**
 * Created by Percy M on 12/13/2017.
 */

public class ImageModel {
    private String url;
    private String name;
    private Long sizeInBytes;

    public ImageModel(String url , String name , Long sizeInBytes) {
        this.url = url;
        this.name = name;
        this.sizeInBytes = sizeInBytes;
    }


    public ImageModel() {
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
}
