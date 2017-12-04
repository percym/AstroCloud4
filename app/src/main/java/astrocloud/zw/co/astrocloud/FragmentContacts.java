package astrocloud.zw.co.astrocloud;


import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
    private ArrayList<ContactModel> arrayListContacts = new ArrayList<>();
    private ArrayList<ContactModel> arrayListContactsToDisplay = new ArrayList<>();
    private DatabaseReference contactsDatabase;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference contactsChildReference;
    ContactsAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_contacts,container,false);
        contactsRecyclerView= view.findViewById(R.id.contactsRecyclerView);

        contactsDatabase = FirebaseDatabase.getInstance().getReference();
        contactsChildReference= contactsDatabase.child("contacts");


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                writecontacts();

            }
        });
        getContacts();
        adapter = new ContactsAdapter(getContext(),arrayListContactsToDisplay);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        contactsRecyclerView.setLayoutManager(mLayoutManager);
        contactsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        contactsRecyclerView.setAdapter(adapter);
        return view;
    }

    static FragmentContacts newInstance(int val) {
        FragmentContacts fragmentContacts = new FragmentContacts();
        // Supply val input as an argument.
        Bundle args = new Bundle();
        args.putInt("val", val);
        fragmentContacts.setArguments(args);
        return fragmentContacts;
    }
    //----------------------------------------------contats
    private void writecontacts(){

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Email.ADDRESS,};

        Cursor people = getActivity().getContentResolver().query(uri, projection, null, null, null);

        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        //people.moveToFirst();
        for(people.moveToFirst(); !people.isAfterLast(); people.moveToNext()){

            String name = people.getString(indexName);
            String number = people.getString(indexNumber);

            arrayListContacts.add(new ContactModel(name,number));


        }

//                do {
//
//                    String name = people.getString(indexName);
//                    String number = people.getString(indexNumber);
//                    HashMap<String, Object> NamePhoneType = new HashMap<String, Object>();
//                    NamePhoneType.put("name", name);
//                    NamePhoneType.put("mobileno", number);
//                    Log.d("name+---+number", name + "----" + number);
//                    try {
//                        json = new JSONObject().put("contact_no", number.trim());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                    try {
//                        json.put("name", name.trim());
//                        contactCount++;
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                } while (people.moveToNext());
//                //Log.d("json data new query",postjson.toString().trim());
//                String contactsAsString = String.valueOf(contactCount);
//                Log.d(TAG,contactsAsString);
//                //insertContact(GlobalDeclarations.UserAccountID, contactsFromJson, contactsAsString);
        people.close();
        writeNewUser(arrayListContacts);
    }
    private ArrayList<ContactModel> getContacts(){
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
        contactsChildReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fillArrayListWithContacts(dataSnapshot);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fillArrayListWithContacts(dataSnapshot);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                fillArrayListWithContacts(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                fillArrayListWithContacts(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return    arrayListContactsToDisplay;

    }

    private void fillArrayListWithContacts(DataSnapshot dataSnapshot){
            arrayListContactsToDisplay.clear();
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            ContactModel contact = ds.getValue(ContactModel.class);
            arrayListContactsToDisplay.add(contact);
        }
    }
    private void writeNewUser(ArrayList<ContactModel> arrayListContactsTobeWritten) {

        if (!TextUtils.isEmpty(userId)) {
            if (arrayListContactsTobeWritten != null) {


                for (int i = 0; i < arrayListContactsTobeWritten.size(); i++) {

                    contactsDatabase.child("contacts").child(userId).child(nameformater(arrayListContactsTobeWritten.get(i).getName())).setValue(arrayListContactsTobeWritten.get(i));
                }
            }


        }
    }

    private String nameformater(String name){
        String [] badChars = {".","#","$","[","]"};
        for(int i =0; i < badChars.length; i++) {
            if (name.contains((badChars[i]))){
                String toBeReplaced = badChars[i] ;
             name =name.replace(toBeReplaced,"");
            }
        }
        return name;
    }

}
