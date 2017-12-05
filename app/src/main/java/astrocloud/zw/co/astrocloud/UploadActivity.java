package astrocloud.zw.co.astrocloud;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.util.ArrayList;

import astrocloud.zw.co.astrocloud.fragments.FragmentContacts;
import astrocloud.zw.co.astrocloud.models.ContactModel;

public class UploadActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private FirebaseAuth mAuth;

    private ArrayList<ContactModel> arrayListContacts = new ArrayList<>();
    private DatabaseReference contactsDatabase;
    DatabaseReference contactsChildReference;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null || currentUser.isAnonymous()){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(UploadActivity.this, MainActivity.class));
            finish();

        }

    }

    //----------------------------------------------contats
    private void writecontacts(){

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Email.ADDRESS,};

        Cursor people = getContentResolver().query(uri, projection, null, null, null);

        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        //people.moveToFirst();
        arrayListContacts.clear();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        contactsDatabase = FirebaseDatabase.getInstance().getReference();
        contactsChildReference= contactsDatabase.child("contacts");
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


        final ImageView fabIconNew = new ImageView(this);
        fabIconNew.setImageDrawable(getResources().getDrawable(R.drawable.ic_green_cloud));
        final FloatingActionButton rightLowerButton = new FloatingActionButton.Builder(this)
                .setContentView(fabIconNew)
                .build();

        SubActionButton.Builder rLSubBuilder = new SubActionButton.Builder(this);
        ImageView rlIcon0 = new ImageView( this);
        ImageView rlIcon1 = new ImageView( this);
        ImageView rlIcon2 = new ImageView(this);


     //   rlIcon1.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload_contacts));

        rlIcon0.setImageDrawable(getResources().getDrawable(R.drawable.ic_search));
        rlIcon1.setImageDrawable(getResources().getDrawable(R.drawable.ic_reload));
        rlIcon2.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload_contacts));

        final FloatingActionMenu rightLowerMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(rLSubBuilder.setContentView(rlIcon0).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon1).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon2).build())
                .attachTo(rightLowerButton)
                .build();

        switch (mViewPager.getCurrentItem()){
            case 0:
                rlIcon2.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload_contacts));

                rlIcon1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getApplicationContext(),"pano",Toast.LENGTH_LONG).show();
                    }
                });

                rlIcon2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        writecontacts();
                    }
                });
                break;
        }
    }
    private void writeNewUser(ArrayList<ContactModel> arrayListContactsTobeWritten) {

        if (!TextUtils.isEmpty(userId)) {
            if (arrayListContactsTobeWritten != null) {


                for (int i = 0; i < arrayListContactsTobeWritten.size(); i++) {

                    contactsChildReference.child(userId).child(nameformater(arrayListContactsTobeWritten.get(i).getName())).setValue(arrayListContactsTobeWritten.get(i));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_upload, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position){
                case 0:
                    return FragmentContacts.newInstance(position);
                case 1:
                    return PlaceholderFragment.newInstance(position);
                default:return FragmentContacts.newInstance(position);
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }



}
