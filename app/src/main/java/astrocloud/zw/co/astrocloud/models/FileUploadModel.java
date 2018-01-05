package astrocloud.zw.co.astrocloud.models;

/**
 * Created by Percy M on 1/4/2018.
 */

public class FileUploadModel {
    private String url;
    private String type;

    public FileUploadModel(String url, String type) {
        this.url = url;
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
