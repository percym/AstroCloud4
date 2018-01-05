package astrocloud.zw.co.astrocloud.fragments;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.appolica.flubber.Flubber;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import astrocloud.zw.co.astrocloud.R;
import astrocloud.zw.co.astrocloud.adapters.MusicAdapter;
import astrocloud.zw.co.astrocloud.adapters.VideosAdapter;
import astrocloud.zw.co.astrocloud.models.MusicModel;
import astrocloud.zw.co.astrocloud.models.VideoModel;
import astrocloud.zw.co.astrocloud.utils.AppConfig;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Percy M on 11/9/2016.
 */

public class FragmentMusic extends Fragment {
    private String TAG = FragmentMusic.class.getSimpleName();

    private ProgressDialog pDialog;
    private MusicAdapter mAdapter;
    private RecyclerView recyclerView;
    private RelativeLayout folder_state_container;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private FirebaseStorage mStorageReference;
    private StorageReference mMusicStorageReference;
    private StorageReference mUserStorageReference;
    private DatabaseReference userfilesDatabase;
    private DatabaseReference contactsChildReference;
    private DatabaseReference uploadedFilesChildReference;
    CircleImageView emptyfolder, imageUploadContacts;
    Handler h = new Handler();
    int delay = 15000; //15 seconds
    Runnable runnable;


    private LinearLayoutManager mLayoutManager;
    ArrayList<MusicModel> music = new ArrayList<>();
    private MusicModel musicFile;
    private AwesomeInfoDialog awesomeErrorDialog;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private DownloadManager downloadManager     ;
    private long refid;


    public FragmentMusic() {
        // Required empty public constructor

    }

    public static FragmentMusic newInstance(int val) {
        FragmentMusic fr = new FragmentMusic();
        Bundle args = new Bundle();
        args.putInt("val", val);
        fr.setArguments(args);
        return fr;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        setHasOptionsMenu(true);
        recyclerView = (RecyclerView) view.findViewById(R.id.videoRecyclerView);
        folder_state_container = (RelativeLayout) view.findViewById(R.id.folder_state_container);
        emptyfolder = view.findViewById(R.id.empty_folder_icon);
        imageUploadContacts = view.findViewById(R.id.imageUploadContacts);
        imageUploadContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        //initialise the FireStore
        //mStorageReference = FirebaseStorage.getInstance().getReference(AppConfig.FIRESTOREDBURL);
        mStorageReference = FirebaseStorage.getInstance(AppConfig.FIRESTOREDBURL);
        mMusicStorageReference = mStorageReference.getReference("music");
        mUserStorageReference = mMusicStorageReference.child(userId);

        userfilesDatabase = FirebaseDatabase.getInstance().getReference();
        contactsChildReference = userfilesDatabase.child("user_files");
        uploadedFilesChildReference = contactsChildReference.child(userId).child("music");
        pDialog = new ProgressDialog(getActivity());
        mAdapter = new MusicAdapter(getContext(),uploadedFilesChildReference);
        mLayoutManager = new GridLayoutManager(getActivity(),1);
        // StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new MusicAdapter.RecyclerTouchListener(getActivity(), recyclerView, new MusicAdapter.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                 musicFile= new MusicModel();

          musicFile= mAdapter.getmDisplayedPhotoValues().get(position);
                PopupMenu popupMenu = new PopupMenu(getActivity(),view, Gravity.CENTER);
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.menu_file_options,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.download_file:
                                //download the file
                                // instantiate it within the onCreate method
                               Uri Download_Uri = Uri.parse(musicFile.getUrl());

                                DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                                request.setAllowedOverRoaming(false);
                                request.setTitle("AstroCloud Downloading ");
                                request.setDescription(musicFile.getName ());
                                request.setVisibleInDownloadsUi(true);
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC,musicFile.getName()+ musicFile.getType());


                                refid = downloadManager.enqueue(request);


                                return true;
                            case R.id.delete_file:
                                //delete file
                                awesomeErrorDialog = new AwesomeInfoDialog(getContext());
                                awesomeErrorDialog
                                        .setTitle(R.string.app_name)
                                        .setMessage(" Are you sure you want to delete " + musicFile.getName() + "?")
                                        .setDialogIconOnly(R.drawable.ic_app_icon)
                                        .setColoredCircle(R.color.white)
                                        .setCancelable(false)
                                        .setPositiveButtonText(getString(R.string.delete))
                                        .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                                        .setPositiveButtonTextColor(R.color.white)
                                        .setNegativeButtonText(getString(R.string.cancel))
                                        .setNegativeButtonbackgroundColor(R.color.dialogErrorBackgroundColor)
                                        .setNegativeButtonTextColor(R.color.white)
                                        .setPositiveButtonClick(new Closure() {
                                            @Override
                                            public void exec() {
                                                fileChewer(musicFile, userId, position);
                                            }
                                        })
                                        .setNegativeButtonClick(new Closure() {
                                            @Override
                                            public void exec() {

                                            }
                                        })
                                        .show();

                                return true;
                        }


                        return false;
                    }
                });
                popupMenu.show();



            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        refreshScreen();

        return view;
    }


    private void refreshScreen(){
        if (mAdapter.getmDisplayedPhotoValues().size() > 0 ){
            folder_state_container.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }else{
            folder_state_container.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            Flubber.with()
                    .animation(Flubber.AnimationPreset.ROTATION)
                    .interpolator(Flubber.Curve.BZR_EASE_IN_OUT_CUBIC)
                    .repeatCount(2)
                    .duration(2000)
                    .autoStart(true)
                    .createFor(emptyfolder);

        }

    }

    public void setFullscreen() {
        setFullscreen(getActivity());
    }

    public static boolean isImmersiveAvailable() {
        return Build.VERSION.SDK_INT >= 19;
    }

    public void setFullscreen(Activity activity) {
        if (Build.VERSION.SDK_INT > 10) {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;

            if (isImmersiveAvailable()) {
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        } else {
            activity.getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }


    @Override
    public void onResume() {
        super.onResume();

        h.postDelayed(new Runnable() {
            public void run() {
                refreshScreen();

                runnable = this;

                h.postDelayed(runnable, delay);
            }
        }, delay);

    }
//    @Override
//    public void onCreateOptio     nsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.frag_documents, menu);
//        MenuItem searchViewItem = menu.findItem(R.id.search);
//        SearchView searchView = new SearchView(((PBMainActivity) getContext()).getSupportActionBar().getThemedContext());
//        MenuItemCompat.setShowAsAction(searchViewItem, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
//        MenuItemCompat.setActionView(searchViewItem, searchView);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                mAdapter.getFilter().filter(query);
//                mAdapter.notifyDataSetChanged();
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                mAdapter.getFilter().filter(newText);
//                mAdapter.notifyDataSetChanged();
//                return false;
//            }
//        });
//    }

    private void fileChewer(final MusicModel fileToDelete, String uid , final int pos){
        StorageReference localReferencePath = mMusicStorageReference.child(uid).child(fileToDelete.getName());
        localReferencePath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                music.remove(pos);
                dataChewer(fileToDelete.getKey());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Deletion error" , Toast.LENGTH_LONG);

            }
        });

    }

    private void dataChewer(String ref){
        DatabaseReference localReference = uploadedFilesChildReference.child(ref);
        localReference.setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }



}
