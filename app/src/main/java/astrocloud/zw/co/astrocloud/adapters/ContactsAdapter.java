package astrocloud.zw.co.astrocloud.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import astrocloud.zw.co.astrocloud.R;
import astrocloud.zw.co.astrocloud.models.ContactModel;

/**
 * Created by Percy M on 6/27/2016.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private ArrayList<ContactModel> mOriginalValues; // Original Values
    private ArrayList<ContactModel> mDisplayedValues;    // Values to be displayed
    private Context context;
    public ContactsAdapter(Context context, ArrayList<ContactModel> mContactsArrayList) {
        this.mOriginalValues     = mContactsArrayList;
        this.mDisplayedValues = mContactsArrayList;
        this.context = context;
        }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView  = LayoutInflater.from(context).inflate(R.layout.contacts_recycler_row_item,parent,false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.tvname.setText(mDisplayedValues.get(position).getName());
        holder.tvnumber.setText(mDisplayedValues.get(position).getNumber());

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDisplayedValues.size();
    }


  //  @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mDisplayedValues = (ArrayList<ContactModel>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<ContactModel> FilteredArrList = new ArrayList<ContactModel>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<ContactModel>(mDisplayedValues); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        String data = mOriginalValues.get(i).getName();
                        if (data.toLowerCase().contains(constraint.toString())) {
                            FilteredArrList.add(new ContactModel(mOriginalValues.get(i).getName(), mOriginalValues.get(i).getNumber()));
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

    public void removeItem(int position) {
        mDisplayedValues.remove(position);
        mOriginalValues.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,mDisplayedValues.size());
        notifyItemRangeChanged(position,mOriginalValues.size());
    }




    public class ViewHolder  extends  RecyclerView.ViewHolder{
        RelativeLayout llContainer;
        TextView tvname,tvnumber;
        ImageView imv;
        public ViewHolder(View convert) {
            super(convert);
            llContainer = (RelativeLayout) convert.findViewById(R.id.contacts_layout);
            tvname = (TextView) convert.findViewById(R.id.firstLine);
            tvnumber = (TextView) convert.findViewById(R.id.secondLine);
            imv=(ImageView)convert.findViewById(R.id.icon);
        }

    }


}
