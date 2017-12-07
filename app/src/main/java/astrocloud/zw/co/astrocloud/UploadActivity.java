package astrocloud.zw.co.astrocloud;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import net.ralphpina.permissionsmanager.PermissionsManager;
import net.ralphpina.permissionsmanager.PermissionsResult;

import java.util.ArrayList;

import astrocloud.zw.co.astrocloud.fragments.FragmentContacts;
import astrocloud.zw.co.astrocloud.fragments.FragmentPhotos;
import astrocloud.zw.co.astrocloud.models.ContactModel;
import rx.functions.Action1;

public class UploadActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACTS =101 ;
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
    private AwesomeInfoDialog awesomeInfoDialog;


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
//        PermissionsManager.init(this);

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
        PermissionsManager.init(this);
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
        final ImageView rlIcon0 = new ImageView( this);
        final ImageView rlIcon1 = new ImageView( this);
        final ImageView rlIcon2 = new ImageView( this);
        final ImageView rlIcon3 = new ImageView(this);


     //   rlIcon1.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload_contacts));

        rlIcon0.setImageDrawable(getResources().getDrawable(R.drawable.ic_search));
        rlIcon1.setImageDrawable(getResources().getDrawable(R.drawable.ic_download));
        rlIcon2.setImageDrawable(getResources().getDrawable(R.drawable.ic_reload));
        rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload_contacts));

        final FloatingActionMenu rightLowerMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(rLSubBuilder.setContentView(rlIcon0).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon1).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon2).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon3).build())
                .attachTo(rightLowerButton)
                .build();
            mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()){
                        case 0:
                            if(rightLowerMenu.isOpen()){
                                rightLowerMenu.close(true);

                            }
                            rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload_contacts));

                            break;

                        case 1:{
                            rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_file_upload_white_48dp));
                            if(rightLowerMenu.isOpen()){
                                rightLowerMenu.close(true);

                            }

                        }

                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });



        switch (mViewPager.getCurrentItem()){
            case 0:
                rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload_contacts));
                rlIcon0.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                rlIcon2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FragmentContacts.newInstance(0);
                    }
                });


                rlIcon3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(PermissionsManager.get().isContactsGranted()) {
                            rightLowerMenu.close(true);

                            writecontacts();
                        }else if(PermissionsManager.get().neverAskForContacts(UploadActivity.this)) {
                            showPermissionsDialogue();
                        }else {
                            PermissionsManager.get().requestContactsPermission()
                                    .subscribe(new Action1<PermissionsResult>() {
                                        @Override
                                        public void call(PermissionsResult permissionsResult) {
                                          if(!permissionsResult.isGranted()){
                                              showPermissionsDialogue();

                                          }
                                        }
                                    });

                        }
                    }
                });
                break;

            case 1:{
                rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_file_upload_white_48dp));
                if(rightLowerMenu.isOpen()){
                    rightLowerMenu.updateItemPositions();
                }

            }
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
                    return FragmentPhotos.newInstance(position);
                default:return FragmentContacts.newInstance(position);
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    public void onClickGoToAppSettings() {
        PermissionsManager.get()
                .intentToAppSettings(this);
    }
    public void showPermissionsDialogue(){
        new AwesomeSuccessDialog(this)
                .setTitle("AstroCloud")
                .setMessage("Permission to read and write contacts is needed for application to function properly")
                .setColoredCircle(R.color.white)
                .setDialogIconOnly(R.drawable.ic_app_icon)
                .setCancelable(false)
                .setPositiveButtonText(getString(R.string.give_permissions))
                .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                .setPositiveButtonTextColor(R.color.white)
                .setNegativeButtonText(getString(R.string.dialog_no_button))
                .setNegativeButtonbackgroundColor(R.color.dialogErrorBackgroundColor)
                .setNegativeButtonTextColor(R.color.white)
                .setPositiveButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        //click
                        onClickGoToAppSettings();
                    }
                })
                .setNegativeButtonClick(new Closure() {
                    @Override
                    public void exec() {

                    }
                })
                .show();

    }


    public void showUploadcontactsDialogue(){
        awesomeInfoDialog = new AwesomeInfoDialog(this);
        awesomeInfoDialog
                .setTitle(R.string.app_name)
                .setMessage("Uploading your contacts")
                .setDialogIconOnly(R.drawable.ic_app_icon)
                .setColoredCircle(R.color.white)
                .setCancelable(false)
                .show();

    }

}
