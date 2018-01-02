package astrocloud.zw.co.astrocloud.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.volokh.danylo.video_player_manager.manager.VideoItem;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;

import java.util.ArrayList;
import java.util.List;


import astrocloud.zw.co.astrocloud.R;
import astrocloud.zw.co.astrocloud.adapters.items.BaseVideoItem;
import astrocloud.zw.co.astrocloud.models.ImageModel;
import astrocloud.zw.co.astrocloud.models.VideoModel;
import astrocloud.zw.co.astrocloud.models.VideoModelContainer;

/**
 * Created by Percy M on 12/20/2017.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoViewHolder> {
    private DatabaseReference databaseReference ;
    private   VideoPlayerManager mVideoPlayerManager;
    private ArrayList<VideoModel> mList = new ArrayList<>();
    private final Context mContext;

    public VideoAdapter(VideoPlayerManager videoPlayerManager, Context context, DatabaseReference ref){
        mVideoPlayerManager = videoPlayerManager;
        mContext = context;
        databaseReference = ref;
        Query videosQuery = databaseReference;
        videosQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fillArrayListWithVideo(dataSnapshot);
                notifyDataSetChanged();


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String picKey = dataSnapshot.getKey();
                int picIndex = -1;
                for(int i= 0 ; i < mList.size(); i ++ ){
                    if(mList.get(i).getKey().contains(picKey)){
                        picIndex= i;
                    }
                }
                if(picIndex > -1 ){
                    mList.remove(picIndex);
                    notifyDataSetChanged();
                }else{
                    Log.d("Videos", "no value found");
                }


                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseVideoItem videoItem = mList.get(viewType);
        View resultView = videoItem.createView(parent, mContext.getResources().getDisplayMetrics().widthPixels);
        return new VideoViewHolder(resultView);

    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
    private void fillArrayListWithVideo(DataSnapshot dataSnapshot) {

        VideoModelContainer videoModel = dataSnapshot.getValue(VideoModelContainer.class);
        mList.add(new VideoModel(videoModel.getUrl(), videoModel.getName(), videoModel.getSizeInBytes(),videoModel.getKey(),mVideoPlayerManager));

        notifyDataSetChanged();
    }

    public List<VideoModel> getmList() {
        return mList;
    }

    public void setmList(ArrayList<VideoModel> mList) {
        this.mList = mList;
    }


}

