package astrocloud.zw.co.astrocloud;

import android.app.Activity;
import android.app.ActivityManager;
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
import com.kbeanie.multipicker.api.AudioPicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.AudioPickerCallback;
import com.kbeanie.multipicker.api.callbacks.VideoPickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenAudio;
import com.kbeanie.multipicker.api.entity.ChosenVideo;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import net.alhazmy13.mediapicker.Video.VideoPicker;
import net.ralphpina.permissionsmanager.PermissionsManager;
import net.ralphpina.permissionsmanager.PermissionsResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import astrocloud.zw.co.astrocloud.fragments.FragmentContacts;
import astrocloud.zw.co.astrocloud.fragments.FragmentDocument;
import astrocloud.zw.co.astrocloud.fragments.FragmentMusic;
import astrocloud.zw.co.astrocloud.fragments.FragmentPhotos;
import astrocloud.zw.co.astrocloud.fragments.FragmentVideos;
import astrocloud.zw.co.astrocloud.models.ContactModel;
import astrocloud.zw.co.astrocloud.models.DocumentModel;
import astrocloud.zw.co.astrocloud.models.FileUploadModel;
import astrocloud.zw.co.astrocloud.models.ImageModel;
import astrocloud.zw.co.astrocloud.models.MusicModel;
import astrocloud.zw.co.astrocloud.utils.AppConfig;
import astrocloud.zw.co.astrocloud.utils.FireSizeCalculator;
import astrocloud.zw.co.astrocloud.utils.GLOBALDECLARATIONS;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import rx.functions.Action1;

public class UploadActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACTS = 101;
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
    private DatabaseReference uploadedVideoChildReference;
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private AwesomeInfoDialog awesomeInfoDialog;
    private AwesomeInfoDialog awesomeInfocontacts;
    private ArrayList<ContactModel> arrayListWithContacts;
    private CoordinatorLayout main_content;
    private AwesomeInfoDialog awesomeErrorDialog;
    private FirebaseStorage mStorageReference;
    private StorageReference mImagesStorageReference;
    private StorageReference mDocumentsStorageReference;
    private StorageReference mUserStorageReference;
    private ArrayList<String> photoPaths;
    private Uri file;
    private StorageMetadata metadata;
    private UploadTask uploadTask;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private ArrayList<String> videoPaths;
    private StorageReference mVideosStorageReference;

    private ArrayList<FileUploadModel> musicPaths;
    private ArrayList<String> documentPaths;
    private AudioPicker audioPicker;
    private StorageReference mMusicStorageReference;
    private DatabaseReference uploadedMusicChildReference;
    private com.kbeanie.multipicker.api.VideoPicker videoPickers;
    private DatabaseReference uploadedDocumentChildReference;
    int[] file_formats = new int[]{ R.drawable.mp3, R.drawable.ogpp, R.drawable.threegpp,R.drawable.wav};


    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || currentUser.isAnonymous()) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(UploadActivity.this, MainActivity.class));
            finish();

        }

    }

    //----------------------------------------------contats
    private void writecontacts() {

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Email.ADDRESS,};

        Cursor people = getContentResolver().query(uri, projection, null, null, null);

        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        //people.moveToFirst();
        arrayListContacts.clear();
        for (people.moveToFirst(); !people.isAfterLast(); people.moveToNext()) {

            String name = people.getString(indexName);
            String number = people.getString(indexNumber);
            arrayListContacts.add(new ContactModel(name, number));
        }
        people.close();
        writeNewUser(arrayListContacts);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        audioPicker = new AudioPicker(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contactsDatabase = FirebaseDatabase.getInstance().getReference();
        contactsChildReference = contactsDatabase.child("contacts");
        //   uploadedFilesChildReference = contactsChildReference.child("fileUrls");

        //Storage for images
        //  mStorageReference = FirebaseStorage.getInstance().getReference(AppConfig.FIRESTOREDBURL);
        mStorageReference = FirebaseStorage.getInstance(AppConfig.FIRESTOREDBURL);
        mImagesStorageReference = mStorageReference.getReference("user_files/" + userId + "/pictures");
        mVideosStorageReference = mStorageReference.getReference("user_files/" + userId + "/videos");
        mMusicStorageReference = mStorageReference.getReference("user_files/" + userId + "/music");
        mDocumentsStorageReference = mStorageReference.getReference("user_files/" + userId + "/documents");

        uploadedFilesChildReference = contactsDatabase.child("user_files").child(userId).child("pictures");
        uploadedVideoChildReference = contactsDatabase.child("user_files").child(userId).child("videos");
        uploadedMusicChildReference = contactsDatabase.child("user_files").child(userId).child("music");
        uploadedDocumentChildReference = contactsDatabase.child("user_files").child(userId).child("documents");


        // Create the adapter that will return a fragment
        // for each of the three
        // primary sections of the activity.

        main_content = findViewById(R.id.main_content);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        int p = mSectionsPagerAdapter.getCount();
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            tabLayout.getTabAt(i).setText(mSectionsPagerAdapter.getPageTitle(1 + i));
        }

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


        final ImageView fabIconNew = new ImageView(this);
        fabIconNew.setImageDrawable(getResources().getDrawable(R.drawable.ic_green_cloud));
        final FloatingActionButton rightLowerButton = new FloatingActionButton.Builder(this)
                .setContentView(fabIconNew)
                .build();

        SubActionButton.Builder rLSubBuilder = new SubActionButton.Builder(this);
        final ImageView rlIcon0 = new ImageView(this);
        final ImageView rlIcon1 = new ImageView(this);
        final ImageView rlIcon2 = new ImageView(this);
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
                switch (tab.getPosition()) {
                    case 4:
                        rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_upload_contacts));
                        if (rightLowerMenu.isOpen()) {
                            rightLowerMenu.close(true);

                        }


                        rlIcon1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Fragment fragmentWithContactsToRestore = mSectionsPagerAdapter.getItem(0);
                                if (fragmentWithContactsToRestore instanceof FragmentContacts) {
                                    //   ((FragmentContacts)fragmentWithContactsToRestore).getArrayListContactsToDisplay();
                                    arrayListWithContacts = GLOBALDECLARATIONS.GLOBAL_CONTACTS_ARRAYLIST;
                                    if (arrayListWithContacts != null) {
                                        if (PermissionsManager.get().isContactsGranted()) {
                                            rightLowerMenu.close(true);

                                            showcontactsRestoration();
                                            contactsRestorer(arrayListWithContacts);
                                            dismissRestoration();
                                        } else if (PermissionsManager.get().neverAskForContacts(UploadActivity.this)) {

                                            showPermissionsDialogue();
                                        } else {
                                            PermissionsManager.get().requestContactsPermission()
                                                    .subscribe(new Action1<PermissionsResult>() {
                                                        @Override
                                                        public void call(PermissionsResult permissionsResult) {
                                                            if (!permissionsResult.isGranted()) {
                                                                showPermissionsDialogue();

                                                            } else {
                                                                rightLowerMenu.close(true);
                                                                showcontactsRestoration();
                                                                contactsRestorer(arrayListContacts);
                                                                dismissRestoration();
                                                            }
                                                        }
                                                    });

                                        }

                                    } else {
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

                                                    } else {
                                                        writecontacts();

                                                    }
                                                }
                                            });


                                }

                            }
                        });


                        break;

                    case 0: {
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
                                    documentPaths = new ArrayList<>();
                                    FilePickerBuilder.getInstance().setMaxCount(20)
                                            .setSelectedFiles(documentPaths)
                                            .setActivityTheme(R.style.AppTheme_PopupOverlay)
                                            .pickPhoto(UploadActivity.this);
                                    //  imageUploaderToFireStore(photoPaths);

                                } else if (PermissionsManager.get().neverAskForContacts(UploadActivity.this)) {

                                    showPermissionsDialogueStorage();

                                } else {
                                    PermissionsManager.get().requestContactsPermission()
                                            .subscribe(new Action1<PermissionsResult>() {
                                                @Override
                                                public void call(PermissionsResult permissionsResult) {
                                                    if (!permissionsResult.isGranted()) {
                                                        showPermissionsDialogueStorage();

                                                    } else {
                                                        imageUploaderToFireStore(photoPaths);

                                                    }
                                                }
                                            });
                                }
                            }
                        });
                        break;
                    }
                    case 2: {
                        rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_video_upload));
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
                                    videoPaths = new ArrayList<>();
                                    videoPickers = new com.kbeanie.multipicker.api.VideoPicker(UploadActivity.this);
                                    videoPickers.setVideoPickerCallback(new VideoPickerCallback() {
                                        @Override
                                        public void onVideosChosen(List<ChosenVideo> videos) {
                                            for (ChosenVideo video : videos) {
                                                videoPaths.add(video.getOriginalPath());

                                            }
                                            if (videoPaths != null) {
                                                videoUploaderToFireStore(videoPaths);
                                            }


                                        }

                                        @Override
                                        public void onError(String s) {

                                        }
                                    });
                                    videoPickers.allowMultiple();
                                    videoPickers.pickVideo();

                                } else if (PermissionsManager.get().neverAskForContacts(UploadActivity.this)) {

                                    showPermissionsDialogueStorage();

                                } else {
                                    PermissionsManager.get().requestContactsPermission()
                                            .subscribe(new Action1<PermissionsResult>() {
                                                @Override
                                                public void call(PermissionsResult permissionsResult) {
                                                    if (!permissionsResult.isGranted()) {
                                                        showPermissionsDialogueStorage();

                                                    } else {
                                                        videoUploaderToFireStore(videoPaths);

                                                    }
                                                }
                                            });
                                }
                            }
                        });
                        break;

                    }


                    case 1: {
                        rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_music));
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
                                    musicPaths = new ArrayList<>();

                                    audioPicker.setAudioPickerCallback(new AudioPickerCallback() {
                                        @Override
                                        public void onAudiosChosen(List<ChosenAudio> audios) {
                                            // Display Files;

                                            for (ChosenAudio audio : audios) {


                                                musicPaths.add(new FileUploadModel(audio.getOriginalPath(), audio.getOriginalPath()));

                                            }
                                            if (musicPaths != null) {
                                                musicUploaderToFireStore(musicPaths);
                                            }


                                        }

                                        @Override
                                        public void onError(String message) {
                                            // Handle errors
                                        }
                                    });
                                    audioPicker.allowMultiple();
                                    audioPicker.pickAudio();

                                    //  imageUploaderToFireStore(photoPaths);

                                } else if (PermissionsManager.get().neverAskForContacts(UploadActivity.this)) {

                                    showPermissionsDialogueStorage();

                                } else {
                                    PermissionsManager.get().requestContactsPermission()
                                            .subscribe(new Action1<PermissionsResult>() {
                                                @Override
                                                public void call(PermissionsResult permissionsResult) {
                                                    if (!permissionsResult.isGranted()) {
                                                        showPermissionsDialogueStorage();

                                                    } else {
                                                        videoUploaderToFireStore(videoPaths);

                                                    }
                                                }
                                            });
                                }
                            }
                        });
                        break;


                    }

                    case 3: {
                        rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_cloud_document));
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
                                    documentPaths = new ArrayList<>();
                                    FilePickerBuilder.getInstance().setMaxCount(20)
                                            .setSelectedFiles(documentPaths)
                                            .setActivityTheme(R.style.AppTheme_PopupOverlay)
                                            .pickFile(UploadActivity.this);
                                    //  imageUploaderToFireStore(photoPaths);

                                } else if (PermissionsManager.get().neverAskForContacts(UploadActivity.this)) {

                                    showPermissionsDialogueStorage();

                                } else {
                                    PermissionsManager.get().requestContactsPermission()
                                            .subscribe(new Action1<PermissionsResult>() {
                                                @Override
                                                public void call(PermissionsResult permissionsResult) {
                                                    if (!permissionsResult.isGranted()) {
                                                        showPermissionsDialogueStorage();

                                                    } else {
                                                        imageUploaderToFireStore(photoPaths);

                                                    }
                                                }
                                            });
                                }
                            }
                        });
                        break;
                    }
                    default: {
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
                                    documentPaths = new ArrayList<>();
                                    FilePickerBuilder.getInstance().setMaxCount(20)
                                            .setSelectedFiles(documentPaths)
                                            .setActivityTheme(R.style.AppTheme_PopupOverlay)
                                            .pickPhoto(UploadActivity.this);
                                    //  imageUploaderToFireStore(photoPaths);

                                } else if (PermissionsManager.get().neverAskForContacts(UploadActivity.this)) {

                                    showPermissionsDialogueStorage();

                                } else {
                                    PermissionsManager.get().requestContactsPermission()
                                            .subscribe(new Action1<PermissionsResult>() {
                                                @Override
                                                public void call(PermissionsResult permissionsResult) {
                                                    if (!permissionsResult.isGranted()) {
                                                        showPermissionsDialogueStorage();

                                                    } else {
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

//                    case 0: {
//                        rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_image_upload));
//                        if (rightLowerMenu.isOpen()) {
//                            rightLowerMenu.close(true);
//
//                        }
//                        if (rightLowerMenu.isOpen()) {
//                            rightLowerMenu.updateItemPositions();
//                        }
//                        rlIcon3.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                if (PermissionsManager.get().isStorageGranted()) {
//                                    photoPaths = new ArrayList<>();
//                                    FilePickerBuilder.getInstance().setMaxCount(20)
//                                            .setSelectedFiles(photoPaths)
//                                            .setActivityTheme(R.style.AppTheme_PopupOverlay)
//                                            .pickPhoto(UploadActivity.this);
//                                    //  imageUploaderToFireStore(photoPaths);
//
//                                } else if (PermissionsManager.get().neverAskForContacts(UploadActivity.this)) {
//
//                                    showPermissionsDialogueStorage();
//
//                                } else {
//                                    PermissionsManager.get().requestContactsPermission()
//                                            .subscribe(new Action1<PermissionsResult>() {
//                                                @Override
//                                                public void call(PermissionsResult permissionsResult) {
//                                                    if (!permissionsResult.isGranted()) {
//                                                        showPermissionsDialogueStorage();
//
//                                                    } else {
//                                                        imageUploaderToFireStore(photoPaths);
//
//                                                    }
//                                                }
//                                            });
//                                }
//                            }
//                        });
//                        break;
//                    }
//
//
//
//                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        boolean checkService = isMyServiceRunning(FireSizeCalculator.class);
        if(!isMyServiceRunning(FireSizeCalculator.class)){
            startService(new Intent(getBaseContext(), FireSizeCalculator.class));
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

    private String nameformater(String name) {
        String[] badChars = {".", "#", "$", "[", "]"};
        for (int i = 0; i < badChars.length; i++) {
            if (name.contains((badChars[i]))) {
                String toBeReplaced = badChars[i];
                name = name.replace(toBeReplaced, "");
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
        }else  if (id==R.id.my_cloud){
           startActivity( new Intent(this, UsageActivity.class));

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

            switch (position) {
                case 0:
                    return FragmentPhotos.newInstance(position);
                case 1:
                    return FragmentMusic.newInstance(position);
                case 2:
                    return FragmentVideos.newInstance(position);
                case 3:
                    return FragmentDocument.newInstance(position);
                case 4:
                    return FragmentContacts.newInstance(position);


                default:
                    return FragmentPhotos.newInstance(position);
            }

        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 5;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 1:
                    return "Images";
                case 2:
                    return "Music";
                case 3:
                    return "Videos";
                case 4:
                    return "Documents ";
                case 5:
                    return "Contacts";
            }
            return null;
        }


    }

    public void onClickGoToAppSettings() {
        PermissionsManager.get()
                .intentToAppSettings(this);
    }

    public void showPermissionsDialogue() {
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

    public void showPermissionsDialogueStorage() {
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


    public void showUploadcontactsDialogue() {
        awesomeInfoDialog = new AwesomeInfoDialog(this);
        awesomeInfoDialog
                .setTitle(R.string.app_name)
                .setMessage("Uploading your contacts")
                .setDialogIconOnly(R.drawable.ic_app_icon)
                .setColoredCircle(R.color.white)
                .setCancelable(false)
                .show();

    }

    private void contactsRestorer(ArrayList<ContactModel> contactModelArrayList) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
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

    public void dismissRestoration() {
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
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    photoPaths = new ArrayList<>();
                    photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                    //start upload
                    imageUploaderToFireStore(photoPaths);

                }
                break;

            case FilePickerConst.REQUEST_CODE_DOC:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    documentPaths = new ArrayList<>();
                    documentPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                    documentUploaderToFireStore(documentPaths);

                }
                break;

        }
        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            videoPaths = (ArrayList<String>) data.getSerializableExtra(VideoPicker.EXTRA_VIDEO_PATH);
            videoUploaderToFireStore(videoPaths);
        }

        if (requestCode == Picker.PICK_AUDIO && resultCode == RESULT_OK) {
            audioPicker.submit(data);

        }

        if (resultCode == RESULT_OK) {
            if (requestCode == Picker.PICK_VIDEO_DEVICE) {
                videoPickers.submit(data);
            }
        }
    }

    private void musicUploaderToFireStore(ArrayList<FileUploadModel> arrayListPhotos) {
        final int id = 1;

        for (final FileUploadModel looper : arrayListPhotos) {
            final String extension = looper.getUrl().substring(looper.getUrl().lastIndexOf(".")+1);
            if (!setFileExtensionIcon(looper.getUrl()).isEmpty()) {
                mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(getApplicationContext(), "AstroCloud")
                        .setContentTitle("AstroCloud")
                        .setContentText("Uploading audio to your cloud account")
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_stat_cloud_upload);

                file = Uri.fromFile(new File(looper.getUrl()));
                metadata = new StorageMetadata.Builder()
                        .setContentType("audio/mpeg")
                        .build();
                uploadTask = mMusicStorageReference.child(file.getLastPathSegment()).putFile(file, metadata);

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        Double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        mBuilder.setProgress(100, progress.intValue(), false);
                        mBuilder.setContentText("Uploading audio");
                        // Displays the progress bar for the first time.
                        mNotifyManager.notify(id, mBuilder.build());

                    }
                }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                        mBuilder.setContentText("Upload paused");
                        mNotifyManager.notify(id, mBuilder.build());
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        writeNewUploadMusic(task.getResult().getDownloadUrl().toString(), task.getResult().getMetadata().getName(),
                                task.getResult().getMetadata().getSizeBytes(), extension);
                        mBuilder.setContentText("Upload completed");
                        mNotifyManager.notify(id, mBuilder.build());

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mBuilder.setContentText("Upload failed " + e.getMessage());
                        mNotifyManager.notify(id, mBuilder.build());

                    }
                });
            }

            musicPaths = new ArrayList<>();
        }
    }

    private void videoUploaderToFireStore(ArrayList<String> arrayListPhotos) {
        final int id = 1;

        for (String looper : arrayListPhotos) {
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(getApplicationContext(), "AstroCloud")
                    .setContentTitle("AstroCloud")
                    .setContentText("Uploading image to your cloud account")
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_cloud_upload);

            file = Uri.fromFile(new File(looper));
            metadata = new StorageMetadata.Builder()
                    .setContentType("video/mpeg")
                    .build();
            uploadTask = mVideosStorageReference.child(file.getLastPathSegment()).putFile(file, metadata);

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    mBuilder.setProgress(100, progress.intValue(), false);
                    mBuilder.setContentText("Uploading video");
                    // Displays the progress bar for the first time.
                    mNotifyManager.notify(id, mBuilder.build());

                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    mBuilder.setContentText("Upload paused");
                    mNotifyManager.notify(id, mBuilder.build());
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    writeNewVideoUpload(task.getResult().getDownloadUrl().toString(), task.getResult().getMetadata().getName(),
                            task.getResult().getMetadata().getSizeBytes());
                    mBuilder.setContentText("Upload completed");
                    mNotifyManager.notify(id, mBuilder.build());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mBuilder.setContentText("Upload failed " + e.getMessage());
                    mNotifyManager.notify(id, mBuilder.build());

                }
            });
        }

        photoPaths = new ArrayList<>();

    }

    private void imageUploaderToFireStore(ArrayList<String> arrayListPhotos) {
        final int id = 1;

        for (String looper : arrayListPhotos) {
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(getApplicationContext(), "AstroCloud")
                    .setContentTitle("AstroCloud")
                    .setContentText("Uploading image to your cloud account")
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_cloud_upload);

            file = Uri.fromFile(new File(looper));
            metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();
            uploadTask = mImagesStorageReference.child(file.getLastPathSegment()).putFile(file, metadata);

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
                    mNotifyManager.notify(id, mBuilder.build());
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    writeNewUpload(task.getResult().getDownloadUrl().toString(), task.getResult().getMetadata().getName(),
                            task.getResult().getMetadata().getSizeBytes());
                    mBuilder.setContentText("Upload completed");
                    mNotifyManager.notify(id, mBuilder.build());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mBuilder.setContentText("Upload failed " + e.getMessage());
                    mNotifyManager.notify(id, mBuilder.build());

                }
            });
        }

        photoPaths = new ArrayList<>();

    }

    private void writeNewUpload(String downloadUrl, String name, Long sizeInBytes) {
        String key = uploadedFilesChildReference.push().getKey();
        ImageModel imageModel = new ImageModel(downloadUrl, name, sizeInBytes, key);
        uploadedFilesChildReference.child(key).setValue(imageModel);

    }

    private void writeNewVideoUpload(String downloadUrl, String name, Long sizeInBytes) {
        String key = uploadedVideoChildReference.push().getKey();
        ImageModel imageModel = new ImageModel(downloadUrl, name, sizeInBytes, key);
        uploadedVideoChildReference.child(key).setValue(imageModel);

    }

    private void writeNewUploadMusic(String downloadUrl, String name, Long sizeInBytes, String type) {
        String key = uploadedMusicChildReference.push().getKey();
        MusicModel musicModel = new MusicModel(downloadUrl, name, sizeInBytes, key, type);
        uploadedMusicChildReference.child(key).setValue(musicModel);

    }

    private void documentUploaderToFireStore(ArrayList<String> arrayListPhotos) {
        final int id = 1;


        for (String looper : arrayListPhotos) {
            final String extension = looper.substring(looper.lastIndexOf("."));
            if (!getFileType(looper).isEmpty()) {
                mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(getApplicationContext(), "AstroCloud")
                        .setContentTitle("AstroCloud")
                        .setContentText("Uploading document to your cloud account")
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_stat_cloud_upload);

                file = Uri.fromFile(new File(looper));
                metadata = new StorageMetadata.Builder()
                        .setContentType("docx")
                        .build();
                uploadTask = mDocumentsStorageReference.child(file.getLastPathSegment()).putFile(file, metadata);

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        Double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        mBuilder.setProgress(100, progress.intValue(), false);
                        mBuilder.setContentText("Uploading document");
                        // Displays the progress bar for the first time.
                        mNotifyManager.notify(id, mBuilder.build());

                    }
                }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                        mBuilder.setContentText("Upload paused");
                        mNotifyManager.notify(id, mBuilder.build());
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        writeNewUploadDocument(task.getResult().getDownloadUrl().toString(), task.getResult().getMetadata().getName(),
                                task.getResult().getMetadata().getSizeBytes(), extension);
                        mBuilder.setContentText("Upload completed");
                        mNotifyManager.notify(id, mBuilder.build());

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mBuilder.setContentText("Upload failed " + e.getMessage());
                        mNotifyManager.notify(id, mBuilder.build());

                    }
                });
            }

            documentPaths = new ArrayList<>();
        }
    }

    private void writeNewUploadDocument(String downloadUrl, String name, Long sizeInBytes, String extension) {
        String key = uploadedDocumentChildReference.push().getKey();
        DocumentModel documentModel = new DocumentModel(downloadUrl, name, sizeInBytes, key, extension);
        uploadedDocumentChildReference.child(key).setValue(documentModel);

    }

    public String getFileType(String fileName) {

        String fileType = "";
        if (fileName.endsWith("doc")) {
            fileType = "Documents";
        } else if (fileName.endsWith("docx")) {
            fileType = "Documents";
        } else if (fileName.endsWith("xls")) {
            fileType = "Documents";
        } else if (fileName.endsWith("xlsx")) {
            fileType = "Documents";
        } else if (fileName.endsWith("ppt")) {
            fileType = "Documents";
        } else if (fileName.endsWith("pptx")) {
            fileType = "Documents";
        } else if (fileName.endsWith("pdf")) {
            fileType = "Documents";
        } else if (fileName.endsWith("pdf")) {
            fileType = "Documents";
        } else if (fileName.endsWith("pub")) {
            fileType = "Documents";
        } else if (fileName.endsWith("rtf")) {
            fileType = "Documents";
        } else if (fileName.endsWith("txt")) {
            fileType = "Documents";
        }
        return fileType;
    }

    public String setFileExtensionIcon(String fileName) {
        String iconLocation = "";
        if (fileName.endsWith("mp3")) {
            iconLocation = Integer.toString(file_formats[0]);
        } else if (fileName.endsWith("ogg")){
            iconLocation = Integer.toString(file_formats[1]);
        }else if(fileName.endsWith("3gpp")){
            iconLocation = Integer.toString(file_formats[2]);

        } else if(fileName.endsWith("wav")){
        iconLocation = Integer.toString(file_formats[3]);
    }
        return iconLocation;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}


