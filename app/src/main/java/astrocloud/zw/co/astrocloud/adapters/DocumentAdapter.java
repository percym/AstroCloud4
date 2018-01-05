package astrocloud.zw.co.astrocloud.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import astrocloud.zw.co.astrocloud.R;
import astrocloud.zw.co.astrocloud.fragments.SlideshowDialogFragment;
import astrocloud.zw.co.astrocloud.models.Image;
import astrocloud.zw.co.astrocloud.models.DocumentModel;

;

/**
 * Created by Lincoln on 31/03/16.
 */

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.MyViewHolder> implements Filterable {

    private DatabaseReference databaseReference ;
    private Context mContext;
    private List<DocumentModel> mOriginalPhotoValues = new ArrayList<>();  // Original Values
    private List<DocumentModel> mDisplayedPhotoValues = new ArrayList<>();
    private String TAG = SlideshowDialogFragment.class.getSimpleName();
    int[] file_formats = new int[]{ R.drawable.mp3, R.drawable.ogpp,R.drawable.gpp};

    // Values to be displayed
    public DocumentAdapter(Context context, DatabaseReference ref) {
        mContext = context;
        databaseReference = ref;

        // Create child event listener
        // [START child_event_listener_recycler]
        Query musicQuery = databaseReference;
        musicQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                fillArrayListWithContactsDB(dataSnapshot);
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
                for(int i= 0 ; i < mDisplayedPhotoValues.size(); i ++ ){
                    if(mDisplayedPhotoValues.get(i).getKey().contains(picKey)){
                        picIndex= i;
                    }
                }
                if(picIndex > -1 ){
                    mDisplayedPhotoValues.remove(picIndex);
                    mOriginalPhotoValues.remove(picIndex);
                    notifyDataSetChanged();
                }else{
                    Log.d(TAG, "no value found");
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
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mDisplayedPhotoValues = (List<DocumentModel>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                List<Image> FilteredArrList = new ArrayList<>();

                if (mOriginalPhotoValues == null) {
                    mOriginalPhotoValues = new ArrayList<>(mDisplayedPhotoValues); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = mOriginalPhotoValues.size();
                    results.values = mOriginalPhotoValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalPhotoValues.size(); i++) {
                        String data = mOriginalPhotoValues.get(i).getUrl();
                        if (data.toLowerCase().contains(constraint.toString())) {
                            FilteredArrList.add(new Image(mOriginalPhotoValues.get(i).getUrl(), mOriginalPhotoValues.get(i).getUrl(),mOriginalPhotoValues.get(i).getUrl(),mOriginalPhotoValues.get(i).getUrl(),mOriginalPhotoValues.get(i).getUrl()));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;
        public TextView file_name;
        public TextView file_size;

        public MyViewHolder(View view) {
            super(view);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            file_name = view.findViewById(R.id.file_name);
        }
    }




    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_thumbnail, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.thumbnail.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_music_player));
        holder.file_name.setText(mDisplayedPhotoValues.get(position).getName().toString());
    }

    @Override
    public int getItemCount() {
        return  mDisplayedPhotoValues.size();
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private DocumentAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final DocumentAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    private void fillArrayListWithContactsDB(DataSnapshot dataSnapshot) {

        DocumentModel DocumentModel = dataSnapshot.getValue(DocumentModel.class);
        mDisplayedPhotoValues.add(new DocumentModel(DocumentModel.getUrl(), DocumentModel.getName(), DocumentModel.getSizeInBytes(),DocumentModel.getKey()));
        mOriginalPhotoValues.add(new DocumentModel(DocumentModel.getUrl(), DocumentModel.getName(),DocumentModel.getSizeInBytes(),DocumentModel.getKey()));

        notifyDataSetChanged();
    }

    public List<DocumentModel> getmOriginalPhotoValues() {
        return mOriginalPhotoValues;
    }

    public void setmOriginalPhotoValues(List<DocumentModel> mOriginalPhotoValues) {
        this.mOriginalPhotoValues = mOriginalPhotoValues;
    }

    public List<DocumentModel> getmDisplayedPhotoValues() {
        return mDisplayedPhotoValues;
    }

    public void setmDisplayedPhotoValues(List<DocumentModel> mDisplayedPhotoValues) {
        this.mDisplayedPhotoValues = mDisplayedPhotoValues;
    }
}