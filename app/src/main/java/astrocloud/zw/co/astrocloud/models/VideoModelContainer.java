package astrocloud.zw.co.astrocloud.models;

import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;
import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;

import java.io.Serializable;

import astrocloud.zw.co.astrocloud.adapters.VideoViewHolder;
import astrocloud.zw.co.astrocloud.adapters.items.BaseVideoItem;
import astrocloud.zw.co.astrocloud.interfaces.VideoItem;

/**
 * Created by Percy M on 12/13/2017.
 */

public class VideoModelContainer implements Serializable,VideoItem{
    private String url;
    private String name;
    private Long sizeInBytes;
    private String key;

    public VideoModelContainer(){

    }

    public VideoModelContainer(String downloadUrl, String name, Long sizeInBytes, String key) {

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

    @Override
    public void playNewVideo(MetaData currentItemMetaData, VideoPlayerView player, VideoPlayerManager<MetaData> videoPlayerManager) {

    }

    @Override
    public void stopPlayback(VideoPlayerManager videoPlayerManager) {

    }

}
