package astrocloud.zw.co.astrocloud.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import astrocloud.zw.co.astrocloud.R;
import astrocloud.zw.co.astrocloud.models.Model;

/**
 * Created by Percy M on 6/27/2016.
 */
public class ModelAdapter extends RecyclerView.Adapter <ModelAdapter.ViewHolder>  implements Filterable {

    private ArrayList<Model> mOriginalDocValues; // Original Values
    private ArrayList<Model> mDisplayedDocValues;    // Values to be displayed
    Context context;
    int[] file_formats = new int[]{ R.drawable.doc, R.drawable.ppt, R.drawable.xls, R.drawable.txt, R.drawable.rtf, R.drawable.pdf, R.drawable.file , R.drawable.ppt};
    public ModelAdapter(Context context, ArrayList<Model> mDocumentsArrayList) {
        this.mOriginalDocValues = mDocumentsArrayList;
        this.mDisplayedDocValues = mDocumentsArrayList;
        this.context = context;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.multi_list_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if ( mDisplayedDocValues.get(position).getName()!= null) {
            holder.tvFirstLine.setText(mDisplayedDocValues.get(position).getName().toString());
            if( mDisplayedDocValues.get(position).getMime() != null){
                holder.tvFirstLineLeft.setText(mDisplayedDocValues.get(position).getMime());
            }
            if(mDisplayedDocValues.get(position).getDateCreated()!= null){
                holder.tvSecondLine.setText(mDisplayedDocValues.get(position).getDateCreated());
            }
            if (mDisplayedDocValues.get(position).getName().endsWith("doc")||mDisplayedDocValues.get(position).getName().endsWith("docx")){
                Drawable imageDrawable = context.getResources().getDrawable(R.drawable.doc);
                holder.imv.setImageDrawable(imageDrawable);
            }else if (mDisplayedDocValues.get(position).getName().endsWith("xls")||mDisplayedDocValues.get(position).getName().endsWith("xlsx")){
                Drawable imageDrawable = context.getResources().getDrawable(R.drawable.ppt);
                holder.imv.setImageDrawable(imageDrawable);
            }else if (mDisplayedDocValues.get(position).getName().endsWith("ppt")||mDisplayedDocValues.get(position).getName().endsWith("pptx")){
                Drawable imageDrawable = context.getResources().getDrawable(R.drawable.ppt);
                holder.imv.setImageDrawable(imageDrawable);
            } else if (mDisplayedDocValues.get(position).getName().endsWith("pdf")){
                    Drawable imageDrawable = context.getResources().getDrawable(R.drawable.pdf);
                    holder.imv.setImageDrawable(imageDrawable);
            }else if (mDisplayedDocValues.get(position).getName().endsWith("rtf")){
                    Drawable imageDrawable = context.getResources().getDrawable(R.drawable.rtf);
                    holder.imv.setImageDrawable(imageDrawable);
            }else if (mDisplayedDocValues.get(position).getName().endsWith("txt")){
                    Drawable imageDrawable = context.getResources().getDrawable(R.drawable.txt);
                    holder.imv.setImageDrawable(imageDrawable);
            }else if (mDisplayedDocValues.get(position).getName().endsWith("png")){
                holder.imv.setImageURI(Uri.parse(mDisplayedDocValues.get(position).getUrl()));
            }else if (mDisplayedDocValues.get(position).getName().endsWith("jpg")){
            holder.imv.setImageURI(Uri.parse(mDisplayedDocValues.get(position).getUrl()));
                Glide.with(context).load(mDisplayedDocValues.get(position).getUrl()).thumbnail(0.1f).into(holder.imv);

            }
       }



    }



        @Override
        public int getItemCount () {
            return mDisplayedDocValues.size();
        }

    public int setFileExtensionIcon(String fileName) {

        int iconLocation = 0;

        if (fileName.endsWith("doc")) {
            iconLocation = file_formats[0];
        } else if (fileName.endsWith("docx")) {
            iconLocation = file_formats[0];
        } else if (fileName.endsWith("xls")) {
            iconLocation = file_formats[2];
        } else if (fileName.endsWith("xlsx")) {
            iconLocation = file_formats[2];
        } else if (fileName.endsWith("ppt")) {
            iconLocation = file_formats[1];
        } else if (fileName.endsWith("pptx")) {
            iconLocation = file_formats[1];
        } else if (fileName.endsWith("pdf")) {
            iconLocation = file_formats[5];
        } else if (fileName.endsWith("pub")) {
            iconLocation = file_formats[5];
        } else if (fileName.endsWith("rtf")) {
            iconLocation = file_formats[4];
        } else if (fileName.endsWith("txt")) {
            iconLocation = file_formats[3];

        }
        return iconLocation ;
    }


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mDisplayedDocValues = (ArrayList<Model>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<Model> FilteredArrList = new ArrayList<>();

                if (mOriginalDocValues == null) {
                    mOriginalDocValues = new ArrayList<>(mDisplayedDocValues); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = mOriginalDocValues.size();
                    results.values = mOriginalDocValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalDocValues.size(); i++) {
                        String data = mOriginalDocValues.get(i).getName();
                        if (data.toLowerCase().contains(constraint.toString())) {
                            FilteredArrList.add(new Model(mOriginalDocValues.get(i).getUrl(), mOriginalDocValues.get(i).getName(), mOriginalDocValues.get(i).getMime(), mOriginalDocValues.get(i).getSizeInBytes(), mDisplayedDocValues.get(i).getId(), mOriginalDocValues.get(i).getDateCreated()));
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

    public class ViewHolder  extends RecyclerView.ViewHolder{
        RelativeLayout llContainer;
        TextView tvFirstLine,tvFirstLineLeft,tvSecondLine;
        ImageView imv;

        ViewHolder(View view){
            super(view);
            llContainer = (RelativeLayout) view.findViewById(R.id.contacts_layout);
            tvFirstLine = (TextView)  view.findViewById(R.id.firstLine);
            tvFirstLineLeft = (TextView)  view.findViewById(R.id.firstLineLeft);
            tvSecondLine=(TextView) view.findViewById(R.id.secondLine);
                imv=(ImageView) view.findViewById(R.id.icon);

        }
    }
}
