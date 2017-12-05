package astrocloud.zw.co.astrocloud.fragments;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import astrocloud.zw.co.astrocloud.R;
import astrocloud.zw.co.astrocloud.adapters.ContactsAdapter;
import astrocloud.zw.co.astrocloud.models.ContactModel;

/**
 * Created by Percy M on 11/30/2017.
 */

public class FragmentContacts extends Fragment {

    RecyclerView contactsRecyclerView;
    RelativeLayout folderStateContainer;
    View view;
    private static final  String TAG = FragmentContacts.class.getCanonicalName();
    private ArrayList<Object> arrayListContacts = new ArrayList<>();
    private ArrayList<ContactModel> arrayListContactsToDisplay = new ArrayList<>();
    private DatabaseReference contactsDatabase;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference contactsChildReference;
    Query myContactsQuery;
    ContactsAdapter adapter;
    GenericTypeIndicator<HashMap<String,Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_contacts,container,false);
        contactsRecyclerView= view.findViewById(R.id.contactsRecyclerView);

        contactsDatabase = FirebaseDatabase.getInstance().getReference();
        contactsChildReference= contactsDatabase.child("contacts");



//        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                        .setAction("Action", null).show();
//                writecontacts();
//
//            }
//        });

        getContacts();
        adapter = new ContactsAdapter(getContext(),arrayListContactsToDisplay);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        contactsRecyclerView.setLayoutManager(mLayoutManager);
        contactsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        contactsRecyclerView.setAdapter(adapter);
        return view;
    }

    public static FragmentContacts newInstance(int val) {
        FragmentContacts fragmentContacts = new FragmentContacts();
        // Supply val input as an argument.
        Bundle args = new Bundle();
        args.putInt("val", val);
        fragmentContacts.setArguments(args);
        return fragmentContacts;
    }

    private ArrayList<Object> getContacts(){
//        contactsChildReference.child(userId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                fillArrayListWithContacts(dataSnapshot);
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//
//        });
        myContactsQuery= contactsDatabase.child("contacts").child(userId);
        myContactsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fillArrayListWithContacts(dataSnapshot);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        myContactsQuery.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                fillArrayListWithContacts(dataSnapshot);
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//               adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        return    arrayListContacts;

    }

    private void fillArrayListWithContacts(DataSnapshot dataSnapshot) {
        arrayListContacts.clear();
        arrayListContactsToDisplay.clear();
        String cleanedName ="";
        String cleanedNumber ="";
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            Log.e(TAG, ds.toString());
            ContactModel contactModel = ds.getValue(ContactModel.class);




            arrayListContactsToDisplay.add(contactModel);
        }
        adapter.notifyDataSetChanged();
//        Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
//        Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
//        while (iterator.hasNext()){
//            ContactModel contact = iterator.next().getValue(ContactModel.class);
//            arrayListContactsToDisplay.add(new ContactModel(contact.getName(),contact.getNumber()));
//            Object obj = iterator.next().getKey();
//            Object obj2 = iterator.next();
//
//        }
    }


}
