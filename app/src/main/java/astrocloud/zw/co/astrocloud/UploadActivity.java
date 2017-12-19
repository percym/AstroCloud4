package astrocloud.zw.co.astrocloud;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
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
import android.widget.Toast;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import net.ralphpina.permissionsmanager.PermissionsManager;
import net.ralphpina.permissionsmanager.PermissionsResult;

import java.io.File;
import java.util.ArrayList;

import astrocloud.zw.co.astrocloud.fragments.FragmentContacts;
import astrocloud.zw.co.astrocloud.fragments.FragmentPhotos;
import astrocloud.zw.co.astrocloud.models.ContactModel;
import astrocloud.zw.co.astrocloud.models.Image;
import astrocloud.zw.co.astrocloud.models.ImageModel;
import astrocloud.zw.co.astrocloud.utils.AppConfig;
import astrocloud.zw.co.astrocloud.utils.GLOBALDECLARATIONS;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
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
    private DatabaseReference contactsChildReference;
    private DatabaseReference uploadedFilesChildReference;
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private AwesomeInfoDialog awesomeInfoDialog;
    private AwesomeInfoDialog awesomeInfocontacts;
    private ArrayList<ContactModel> arrayListWithContacts;
    private CoordinatorLayout main_content;
    private AwesomeInfoDialog awesomeErrorDialog;
    private FirebaseStorage mStorageReference;
    private StorageReference mImagesStorageReference;
    private StorageReference mUserStorageReference;
    private ArrayList<String> photoPaths;
    private Uri file;
    private StorageMetadata metadata;
    private UploadTask uploadTask;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

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
     //   uploadedFilesChildReference = contactsChildReference.child("fileUrls");
        
        //Storage for images
      //  mStorageReference = FirebaseStorage.getInstance().getReference(AppConfig.FIRESTOREDBURL);
        mStorageReference = FirebaseStorage.getInstance(AppConfig.FIRESTOREDBURL);
        mImagesStorageReference = mStorageReference.getReference("images");
        uploadedFilesChildReference = contactsDatabase.child("user_files").child(userId).child("images");
        // Create the adapter that will return a fragment
        // for each of the three
        // primary sections of the activity.

        main_content= findViewById(R.id.main_content);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);



        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        for(int i = 0 ; i < mSectionsPagerAdapter.getCount();i++) {
            tabLayout.getTabAt(i).setText(mSectionsPagerAdapter.getPageTitle(i+1));
        }

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

                        case 1: {
                            rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_image_upload));
                            if (rightLowerMenu.isOpen()) {
                                rightLowerMenu.close(true);

                            }
                            if (rightLowerMenu.isOpen()) {
                                rightLowerMenu.updateItemPositions();
                            }
                            rlIcon3.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (PermissionsManager.get().isStorageGranted()) {
                                        photoPaths = new ArrayList<>();
                                        FilePickerBuilder.getInstance().setMaxCount(20)
                                                .setSelectedFiles(photoPaths)
                                                .setActivityTheme(R.style.AppTheme_PopupOverlay)
                                                .pickPhoto(UploadActivity.this);
                                      //  imageUploaderToFireStore(photoPaths);

                                    }else if (PermissionsManager.get().neverAskForContacts(UploadActivity.this)){

                                        showPermissionsDialogueStorage();

                                    }else {
                                        PermissionsManager.get().requestContactsPermission()
                                                .subscribe(new Action1<PermissionsResult>() {
                                                    @Override
                                                    public void call(PermissionsResult permissionsResult) {
                                                        if (!permissionsResult.isGranted()) {
                                                            showPermissionsDialogueStorage();

                                                        }else {
                                                            imageUploaderToFireStore(photoPaths);

                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                            break;

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
                        snackShower("pano");

                    }
                });
                rlIcon2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FragmentContacts.newInstance(0);
                    }
                });


                rlIcon1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Fragment fragmentWithContactsToRestore = mSectionsPagerAdapter.getItem(0);
                        if(fragmentWithContactsToRestore instanceof FragmentContacts){
                         //   ((FragmentContacts)fragmentWithContactsToRestore).getArrayListContactsToDisplay();
                            arrayListWithContacts= GLOBALDECLARATIONS.GLOBAL_CONTACTS_ARRAYLIST;
                            if(arrayListWithContacts != null) {
                                if (PermissionsManager.get().isContactsGranted()){
                                    rightLowerMenu.close(true);

                                    showcontactsRestoration();
                                    contactsRestorer(arrayListWithContacts);
                                    dismissRestoration();
                                }else if (PermissionsManager.get().neverAskForContacts(UploadActivity.this)){

                                    showPermissionsDialogue();
                                }else {
                                    PermissionsManager.get().requestContactsPermission()
                                            .subscribe(new Action1<PermissionsResult>() {
                                                @Override
                                                public void call(PermissionsResult permissionsResult) {
                                                    if (!permissionsResult.isGranted()) {
                                                        showPermissionsDialogue();

                                                    }else {
                                                        rightLowerMenu.close(true);
                                                        showcontactsRestoration();
                                                        contactsRestorer(arrayListContacts);
                                                        dismissRestoration();
                                                    }
                                                }
                                            });

                                }

                            }else {
                                snackShower("No contacts found");
                            }

                        }
                    }
                });

                rlIcon3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        rightLowerMenu.close(true);

                        if (PermissionsManager.get().isContactsGranted()) {

                                awesomeErrorDialog = new AwesomeInfoDialog(UploadActivity.this);
                                awesomeErrorDialog
                                        .setTitle(R.string.app_name)
                                        .setMessage(" Do you want to upload your contacts to your cloud account ?")
                                        .setDialogIconOnly(R.drawable.ic_app_icon)
                                        .setColoredCircle(R.color.white)
                                        .setCancelable(false)
                                        .setPositiveButtonText(getString(R.string.upload))
                                        .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                                        .setPositiveButtonTextColor(R.color.white)
                                        .setNegativeButtonText(getString(R.string.cancel))
                                        .setNegativeButtonbackgroundColor(R.color.dialogErrorBackgroundColor)
                                        .setNegativeButtonTextColor(R.color.white)
                                        .setPositiveButtonClick(new Closure() {
                                            @Override
                                            public void exec() {
                                                writecontacts();
                                            }
                                        })
                                        .setNegativeButtonClick(new Closure() {
                                            @Override
                                            public void exec() {

                                            }
                                        })
                                        .show();

                        } else if (PermissionsManager.get().neverAskForContacts(UploadActivity.this)) {

                            showFetchcontactsDialogue();
                        } else {
                            PermissionsManager.get().requestContactsPermission()
                                    .subscribe(new Action1<PermissionsResult>() {
                                        @Override
                                        public void call(PermissionsResult permissionsResult) {
                                            if (!permissionsResult.isGranted()) {
                                                showPermissionsDialogue();

                                            }else {
                                             writecontacts();

                                            }
                                        }
                                    });


                        }

                    }
                });
                break;

            case 1: {
//                if (rightLowerMenu.isOpen()) {
//                    rightLowerMenu.updateItemPositions();
//                }
//                rlIcon3.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (PermissionsManager.get().isStorageGranted()) {
//                            FilePickerBuilder.getInstance().setMaxCount(20)
//                                    .setSelectedFiles(photoPaths)
//                                    .setActivityTheme(R.style.AppTheme_PopupOverlay)
//                                    .pickPhoto(UploadActivity.this);
//
//                        }
//                    }
//                });
//                break;
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

        snackShower("Contacts Uploaded");
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

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 1:
                    return "Contacts";
                case 2:
                    return "Photos";
                case 3:
                    return "Contacts";
            }
            return super.getPageTitle(position);
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
    public void showPermissionsDialogueStorage(){
        new AwesomeSuccessDialog(this)
                .setTitle("AstroCloud")
                .setMessage("Permission to read and write storage is needed for application to function properly")
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

    private void contactsRestorer(ArrayList<ContactModel> contactModelArrayList){
        ArrayList<ContentProviderOperation>  ops= new ArrayList<ContentProviderOperation>();
        for (int i = 0; i <= contactModelArrayList.size() - 1; i++) {
            String name = contactModelArrayList.get(i).getName();
            String mobileno = contactModelArrayList.get(i).getNumber();
            int rawContactID = ops.size();
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            // Adding insert operation to operations list
            // to insert display name in the table ContactsContract.Data
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build());
            // Adding insert operation to operations list
            // to insert Mobile Number in the table ContactsContract.Data
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileno)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());

            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                snackShower("Contacts successfully restored");
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }

        }

    }

    private void snackShower(String message) {
        Snackbar snackbar;
        snackbar = Snackbar.make(main_content, message, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.mainGreen));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackbar.show();
    }

    public void showcontactsRestoration() {
        awesomeInfocontacts = new AwesomeInfoDialog(this);
        awesomeInfocontacts.setTitle(R.string.app_name)
                .setMessage("Restoring your contacts")
                .setDialogIconOnly(R.drawable.ic_app_icon)
                .setColoredCircle(R.color.white)
                .setCancelable(false)
                .show();
    }

    public void dismissRestoration(){
        awesomeInfocontacts.hide();
    }
    public void showFetchcontactsDialogue() {
        awesomeInfoDialog = new AwesomeInfoDialog(this);
        awesomeInfoDialog
                .setTitle(R.string.app_name)
                .setMessage("Fetching your contacts")
                .setDialogIconOnly(R.drawable.ic_app_icon)
                .setColoredCircle(R.color.white)
                .setCancelable(false)
                .show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if(resultCode == Activity.RESULT_OK && data != null){
                    photoPaths = new ArrayList<>();
                    photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                    //start upload
                   imageUploaderToFireStore(photoPaths);

                }
                break;
        }
    }

    private void imageUploaderToFireStore(ArrayList<String> arrayListPhotos){
        final int id =1;

        for (String looper : arrayListPhotos){
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(getApplicationContext(),"AstroCloud")
                    .setContentTitle("AstroCloud")
                    .setContentText("Uploading image to your cloud account" )
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_cloud_upload);

            file = Uri.fromFile(new File(looper));
            metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();
            uploadTask = mImagesStorageReference.child(userId + "/"+file.getLastPathSegment()).putFile(file,metadata);

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    mBuilder.setProgress(100, progress.intValue(), false);
                    mBuilder.setContentText("Uploading image");
                    // Displays the progress bar for the first time.
                    mNotifyManager.notify(id, mBuilder.build());

                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    mBuilder.setContentText("Upload paused");
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    writeNewUpload(task.getResult().getDownloadUrl().toString(), task.getResult().getMetadata().getName(),
                            task.getResult().getMetadata().getSizeBytes());
                    mBuilder.setContentText("Upload completed");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mBuilder.setContentText("Upload failed " + e.getMessage());

                }
            });
        }

        photoPaths = new ArrayList<>();

    }

    private void writeNewUpload(String downloadUrl, String name, Long sizeInBytes){

        String key = uploadedFilesChildReference.push().getKey();
        ImageModel imageModel = new ImageModel(downloadUrl, name, sizeInBytes, key);
        uploadedFilesChildReference.child(key).setValue(imageModel);

    }

}


