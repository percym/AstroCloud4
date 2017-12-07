package astrocloud.zw.co.astrocloud.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.appolica.flubber.Flubber;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeErrorDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
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

import astrocloud.zw.co.astrocloud.R;
import astrocloud.zw.co.astrocloud.adapters.ContactsAdapter;
import astrocloud.zw.co.astrocloud.models.ContactModel;
import de.hdodenhof.circleimageview.CircleImageView;

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
    AwesomeInfoDialog awesomeInfoDialog;
    CircleImageView emptyfolder ;
    RelativeLayout  folder_state_container;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_contacts,container,false);
        contactsRecyclerView= view.findViewById(R.id.contactsRecyclerView);
        emptyfolder = view.findViewById(R.id.empty_folder_icon);
        folder_state_container = view.findViewById(R.id.folder_state_container);
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

//        contactsRecyclerView.setVisibility(View.GONE);
        folder_state_container.setVisibility(View.VISIBLE);
        showFetchcontactsDialogue();
        getContacts();
        adapter = new ContactsAdapter(getContext(),arrayListContactsToDisplay);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        contactsRecyclerView.setLayoutManager(mLayoutManager);
        contactsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        contactsRecyclerView.setAdapter(adapter);


//        Flubber.with()
//                .animation(Flubber.AnimationPreset.ROTATION)
//                .interpolator(Flubber.Curve.BZR_EASE_IN_OUT_CUBIC)
//                .repeatCount(2)
//                .duration(2000)
//                .autoStart(true)
//                .createFor(emptyfolder);
            adapter.notifyDataSetChanged();
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
    arrayListContactsToDisplay.clear();
    arrayListContacts.clear();
        myContactsQuery= contactsDatabase.child("contacts").child(userId);
        myContactsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
              fillArrayListWithContactsDB(dataSnapshot);
              adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                fillArrayListWithContactsDBRemoved(dataSnapshot);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String databaseErrorString = databaseError.getDetails();
            showFetchErrorDialogue(databaseErrorString);

            }
        });
        myContactsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fillArrayListWithContactsDB(dataSnapshot);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String databaseErrorString = databaseError.getDetails().toString();
                showFetchErrorDialogue(databaseErrorString);

            }
        });
        awesomeInfoDialog.hide();
        return    arrayListContacts;

    }

    private void fillArrayListWithContacts(DataSnapshot dataSnapshot) {
//        arrayListContacts.clear();
//        arrayListContactsToDisplay.clear();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
        //   Log.e(TAG, ds.toString());
            ContactModel contactModel = ds.getValue(ContactModel.class);

            arrayListContactsToDisplay.add(contactModel);

        }
        if((arrayListContactsToDisplay.size()>0)){
            awesomeInfoDialog.hide();
            contactsRecyclerView.setVisibility(View.VISIBLE);
            folder_state_container.setVisibility(View.GONE);
        }else {
            awesomeInfoDialog.hide();
            contactsRecyclerView.setVisibility(View.GONE);
            folder_state_container.setVisibility(View.VISIBLE);
            Flubber.with()
                    .animation(Flubber.AnimationPreset.ROTATION)
                    .interpolator(Flubber.Curve.BZR_EASE_IN_OUT_CUBIC)
                    .repeatCount(2)
                    .duration(2000)
                    .autoStart(true)
                    .createFor(emptyfolder);

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

        contactsRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {


            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }
    private void fillArrayListWithContactsDB(DataSnapshot dataSnapshot) {
;
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            //   Log.e(TAG, ds.toString());
            ContactModel contactModel = dataSnapshot.getValue(ContactModel.class);

//        Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
//        Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
//        while (iterator.hasNext()){
//            String id=String.valueOf(iterator.child("id").getValue);
//            ContactModel contact = iterator.next().getValue(ContactModel.class);
//
//            arrayListContactsToDisplay.add(new ContactModel(contact.getName(),contact.getNumber()));
//            Object obj = iterator.next().getKey();
//            Object obj2 = iterator.next();
//

            arrayListContactsToDisplay.add(contactModel);

        }
        if((arrayListContactsToDisplay.size()>0)){
            awesomeInfoDialog.hide();
            contactsRecyclerView.setVisibility(View.VISIBLE);
            folder_state_container.setVisibility(View.GONE);
        }else {
            awesomeInfoDialog.hide();
            contactsRecyclerView.setVisibility(View.GONE);
            folder_state_container.setVisibility(View.VISIBLE);
            Flubber.with()
                    .animation(Flubber.AnimationPreset.ROTATION)
                    .interpolator(Flubber.Curve.BZR_EASE_IN_OUT_CUBIC)
                    .repeatCount(2)
                    .duration(2000)
                    .autoStart(true)
                    .createFor(emptyfolder);

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

        contactsRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {


            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }
    private void fillArrayListWithContactsDBRemoved(DataSnapshot dataSnapshot) {

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            //   Log.e(TAG, ds.toString());
            ContactModel contactModel = dataSnapshot.getValue(ContactModel.class);

//        Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
//        Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
//        while (iterator.hasNext()){
//            String id=String.valueOf(iterator.child("id").getValue);
//            ContactModel contact = iterator.next().getValue(ContactModel.class);
//
//            arrayListContactsToDisplay.add(new ContactModel(contact.getName(),contact.getNumber()));
//            Object obj = iterator.next().getKey();
//            Object obj2 = iterator.next();
//

            arrayListContactsToDisplay.remove(contactModel);
            adapter.notifyDataSetChanged();
        }



        if((arrayListContactsToDisplay.size()>0)){
            awesomeInfoDialog.hide();
            contactsRecyclerView.setVisibility(View.VISIBLE);
            folder_state_container.setVisibility(View.GONE);
        }else {
            awesomeInfoDialog.hide();
            contactsRecyclerView.setVisibility(View.GONE);
            folder_state_container.setVisibility(View.VISIBLE);
            Flubber.with()
                    .animation(Flubber.AnimationPreset.ROTATION)
                    .interpolator(Flubber.Curve.BZR_EASE_IN_OUT_CUBIC)
                    .repeatCount(2)
                    .duration(2000)
                    .autoStart(true)
                    .createFor(emptyfolder);

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

        contactsRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {


            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }
    private void fillArrayListWithContactsDBChanged(DataSnapshot dataSnapshot) {
        ;
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            //   Log.e(TAG, ds.toString());
            ContactModel contactModel = dataSnapshot.getValue(ContactModel.class);

//        Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
//        Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
//        while (iterator.hasNext()){
//            String id=String.valueOf(iterator.child("id").getValue);
//            ContactModel contact = iterator.next().getValue(ContactModel.class);
//
//            arrayListContactsToDisplay.add(new ContactModel(contact.getName(),contact.getNumber()));
//            Object obj = iterator.next().getKey();
//            Object obj2 = iterator.next();
//

            arrayListContactsToDisplay.remove(contactModel);
            arrayListContactsToDisplay.remove(contactModel);
            adapter.notifyDataSetChanged();
        }



        if((arrayListContactsToDisplay.size()>0)){
            awesomeInfoDialog.hide();
            contactsRecyclerView.setVisibility(View.VISIBLE);
            folder_state_container.setVisibility(View.GONE);
        }else {
            awesomeInfoDialog.hide();
            contactsRecyclerView.setVisibility(View.GONE);
            folder_state_container.setVisibility(View.VISIBLE);
            Flubber.with()
                    .animation(Flubber.AnimationPreset.ROTATION)
                    .interpolator(Flubber.Curve.BZR_EASE_IN_OUT_CUBIC)
                    .repeatCount(2)
                    .duration(2000)
                    .autoStart(true)
                    .createFor(emptyfolder);

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

        contactsRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {


            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }
    public void showFetchcontactsDialogue(){
        awesomeInfoDialog = new AwesomeInfoDialog(getActivity());
        awesomeInfoDialog
                .setTitle(R.string.app_name)
                .setMessage("Fetching your contacts")
                .setDialogIconOnly(R.drawable.ic_app_icon)
                .setColoredCircle(R.color.white)
                .setCancelable(false)
                .show();

    }
    public void showFetchErrorDialogue(String databaseErrorString){

        new AwesomeErrorDialog(getActivity())
                .setTitle(R.string.app_name)
                .setMessage(databaseErrorString)
                .setDialogIconOnly(R.drawable.ic_app_icon)
                .setCancelable(true).setButtonText(getString(R.string.dialog_ok_button))
                .setButtonBackgroundColor(R.color.dialogErrorBackgroundColor)
                .setButtonText("Close")
                .setErrorButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        // click
                    }
                })
                .show();
    }

    }

