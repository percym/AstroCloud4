package astrocloud.zw.co.astrocloud.fragments;

/**
 * Created by Percy M on 11/10/2016.
 */

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.github.rubensousa.bottomsheetbuilder.BottomSheetMenuDialog;
import com.github.rubensousa.bottomsheetbuilder.adapter.BottomSheetItemClickListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import net.ralphpina.permissionsmanager.PermissionsManager;
import net.ralphpina.permissionsmanager.PermissionsResult;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import astrocloud.zw.co.astrocloud.App;
import astrocloud.zw.co.astrocloud.R;
import astrocloud.zw.co.astrocloud.UploadActivity;
import astrocloud.zw.co.astrocloud.models.ImageModel;
import astrocloud.zw.co.astrocloud.models.VideoModel;
import astrocloud.zw.co.astrocloud.utils.AppConfig;
import astrocloud.zw.co.astrocloud.utils.FilenameUtils;
import rx.functions.Action1;


public class VideoSlideshowDialogFragment extends DialogFragment  {
    private String TAG = VideoSlideshowDialogFragment.class.getSimpleName();
    private ArrayList<VideoModel> videos;
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private TextView lblCount, lblTitle, lblDate;
    private int selectedPosition = 0;
    private BottomSheetMenuDialog mBottomSheetDialog;
    private BottomSheetBehavior mBehavior;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DatabaseReference contactsDatabase;
    private DatabaseReference uploadedFilesChildReference;
    private FirebaseStorage mStorageReference;
    private StorageReference mVideosStorageReference;
    private AwesomeInfoDialog awesomeErrorDialog;
    private ProgressDialog progressDialog;


    static VideoSlideshowDialogFragment newInstance() {
        VideoSlideshowDialogFragment f = new VideoSlideshowDialogFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       final View v = inflater.inflate(R.layout.fragment_image_slider, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        lblCount = (TextView) v.findViewById(R.id.lbl_count);
        lblTitle = (TextView) v.findViewById(R.id.title);
        lblDate = (TextView) v.findViewById(R.id.date);

        //intialise database
        contactsDatabase = FirebaseDatabase.getInstance().getReference();
        uploadedFilesChildReference = contactsDatabase.child("user_files").child(userId).child("videos");

        //Storage for images
        //  mStorageReference = FirebaseStorage.getInstance().getReference(AppConfig.FIRESTOREDBURL);
        mStorageReference = FirebaseStorage.getInstance(AppConfig.FIRESTOREDBURL);
        mVideosStorageReference = mStorageReference.getReference("user_files/"+userId+"/videos");
        videos = (ArrayList<VideoModel>) getArguments().getSerializable("videos");
        selectedPosition = getArguments().getInt("position");


        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);


        setCurrentItem(selectedPosition);


        return v;
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
        displayMetaInfo(selectedPosition);
    }

    //  page change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            displayMetaInfo(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void displayMetaInfo(int position) {
        lblCount.setText((position + 1) + " of " + videos.size());

        VideoModel video = videos.get(position);
        lblTitle.setText(video.getName());
       // lblDate.setText(image.getName());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    //  adapter
    public class MyViewPagerAdapter extends PagerAdapter  {

        private LayoutInflater layoutInflater;
        private ProgressDialog pDialog;
        private String MarkedFileLocationLink = "";
        private final int REQUEST_USE_STORAGE = 1;
        private  String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        public static final int GRANTED = 0;
        public static final int DENIED = 1;
        public static final int BLOCKED = 2;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position)   {

            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View view = layoutInflater.inflate(R.layout.video_fullsreen_preview, container, false);
            final VideoView videoViewPreview = view.findViewById(R.id.video_preview);
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Loading video");
            progressDialog.setCancelable(true);
            progressDialog.show();


            final VideoModel video = videos.get(position);


            videoViewPreview.setVideoURI(Uri.parse(video.getUrl()));
            MediaController mediaController = new MediaController(getActivity());
            mediaController.show();
            mediaController.setAnchorView(videoViewPreview);

            videoViewPreview.setMediaController(mediaController);    // perform set on prepared listener event on video view
            videoViewPreview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                progressDialog.dismiss();
                }
            });
            videoViewPreview.start();

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mBottomSheetDialog = new BottomSheetBuilder(getContext(),R.style.AppTheme_BottomSheetDialog)
                            .setMode(BottomSheetBuilder.MODE_GRID)
                            .setMenu(R.menu.menu_bottom_grid_sheet)
                            .expandOnStart(true)
                            .setTitleTextColor(R.color.white)
                            .setItemClickListener(new BottomSheetItemClickListener() {
                                @Override
                                public void onBottomSheetItemClick(MenuItem item) {
                                    Log.d("Item click", item.getTitle() + "");

                                    switch (item.getItemId()){
                                        case R.id.image_download:{

                                            if (PermissionsManager.get().isStorageGranted()) {

                                                saveImageLocally(videoViewPreview);

                                            }else if(PermissionsManager.get().neverAskForStorage(getActivity())){
                                                showPermissionsDialogueStorage();


                                            }else{
                                                PermissionsManager.get().requestContactsPermission()
                                                        .subscribe(new Action1<PermissionsResult>() {
                                                            @Override
                                                            public void call(PermissionsResult permissionsResult) {
                                                                if (!permissionsResult.isGranted()) {
                                                                    showPermissionsDialogueStorage();
                                                                } else {
                                                                    saveImageLocally(videoViewPreview);

                                                                }
                                                            }
                                                        });


                                            }
                                            break;
                                        }
                                        case R.id.image_delete:{
                                            if (PermissionsManager.get().isStorageGranted()) {

                                                awesomeErrorDialog = new AwesomeInfoDialog(getContext());
                                                awesomeErrorDialog
                                                        .setTitle(R.string.app_name)
                                                        .setMessage(" Are you sure you want to delete " + video.getName() + "?")
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
                                                                fileChewer(video, userId, position);
                                                                getActivity().getFragmentManager().popBackStack();
//                                                                 getChildFragmentManager().beginTransaction().remove(SlideshowDialogFragment.this).commit();
                                                                getActivity().getSupportFragmentManager().beginTransaction().remove(VideoSlideshowDialogFragment.this).commit();
                                                            }
                                                        })
                                                        .setNegativeButtonClick(new Closure() {
                                                            @Override
                                                            public void exec() {

                                                            }
                                                        })
                                                        .show();


                                            }else if(PermissionsManager.get().neverAskForStorage(getActivity())){
                                                showPermissionsDialogueStorage();


                                            }else{
                                                PermissionsManager.get().requestContactsPermission()
                                                        .subscribe(new Action1<PermissionsResult>() {
                                                            @Override
                                                            public void call(PermissionsResult permissionsResult) {
                                                                if (!permissionsResult.isGranted()) {
                                                                    showPermissionsDialogueStorage();
                                                                } else {
                                                                    fileChewer(video,userId,position);

//                                                                    getChildFragmentManager().beginTransaction().remove(SlideshowDialogFragment.this).commit()
                                                                    getActivity().getSupportFragmentManager().beginTransaction().remove(VideoSlideshowDialogFragment.this).commit();
                                                                }
                                                            }
                                                        });


                                            }
                                            break;
                                        }
                                        case R.id.image_share:{
                                            if (PermissionsManager.get().isStorageGranted()) {



                                                final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                                shareIntent.setType("image/jpg");
                                                shareIntent.putExtra(Intent.EXTRA_STREAM, getImageLocal(videoViewPreview));
                                                // shareIntent.putExtra(Intent.EXTRA_TITLE,"Shared  from Astrocloud");
                                                // shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Shared  from Astrocloud");
                                                startActivity(Intent.createChooser(shareIntent, "Share image using"));



                                            }else if(PermissionsManager.get().neverAskForStorage(getActivity())){
                                                showPermissionsDialogueStorage();


                                            }else{
                                                PermissionsManager.get().requestContactsPermission()
                                                        .subscribe(new Action1<PermissionsResult>() {
                                                            @Override
                                                            public void call(PermissionsResult permissionsResult) {
                                                                if (!permissionsResult.isGranted()) {
                                                                    showPermissionsDialogueStorage();
                                                                } else {

                                                                    final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                                                    shareIntent.setType("image/jpg");
                                                                    shareIntent.putExtra(Intent.EXTRA_STREAM, getImageLocal(videoViewPreview));
                                                                    shareIntent.putExtra(Intent.EXTRA_TITLE,"Shared  from Astrocloud");
                                                                    // shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Shared  from Astrocloud");
                                                                    startActivity(Intent.createChooser(shareIntent, "Share image using"));

                                                                }
                                                            }
                                                        });


                                            }
                                            break;


                                        }

                                    }
                                }
                            })
                            .createDialog();
                    mBottomSheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    });
                    mBottomSheetDialog.show();

                    return false;
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return videos.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View) obj);
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }


        private String saveImageLocally(VideoView iv) {
            iv.buildDrawingCache();

            Bitmap bmp = iv.getDrawingCache();

            File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); //context.getExternalFilesDir(null);
            String imagename=VideoSlideshowDialogFragment.this.lblTitle.getText().toString();
            File file = new File(storageLoc, imagename);

            try{
                FileOutputStream fos = new FileOutputStream(file);
                // dont want to compress
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                scanFile(getActivity(), Uri.fromFile(file));
                Toast.makeText(getContext(), "The file was successfully saved", Toast.LENGTH_LONG).show(); ;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "true";
        }

        private Uri getImageLocal(VideoView iv) {
            iv.buildDrawingCache();

            Bitmap bmp = iv.getDrawingCache();

            File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); //context.getExternalFilesDir(null);
            String imagename=VideoSlideshowDialogFragment.this.lblTitle.getText().toString();
            File file = new File(storageLoc, imagename);

            try{
                FileOutputStream fos = new FileOutputStream(file);
                // dont want to compress
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                scanFile(getActivity(), Uri.fromFile(file));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return Uri.fromFile(file);
        }
        public void verifyStoragePermissions(Activity activity) {
            // Check if we have read or write permission
            int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED ) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_USE_STORAGE
                );
            }
        }
        public  int getPermissionStatus(Activity activity, String androidPermissionName) {
            if(ContextCompat.checkSelfPermission(activity, androidPermissionName) != PackageManager.PERMISSION_GRANTED) {
                if(!ActivityCompat.shouldShowRequestPermissionRationale(activity, androidPermissionName)){
                    return BLOCKED;
                }
                return DENIED;
            }
            return GRANTED;
        }

        private  void scanFile(Context context, Uri imageUri){
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(imageUri);
            context.sendBroadcast(scanIntent);

        }
        private void deleteFile(final String fileLocation, final String serverProcessURL) {

            String tag_string_req = "req_delete_file";
            pDialog.setMessage("Deleting File...");
            showDialog();

            StringRequest strReq = new StringRequest(Request.Method.POST, serverProcessURL, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    hideDialog();
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            Toast.makeText(getContext(), "The file was successfully deleted", Toast.LENGTH_LONG).show(); ;
                            Intent reload = new Intent(getActivity(), UploadActivity.class);
                            startActivity(reload);
                            getActivity().finish();

                        } else {
                            // Error occurred in registration. Get the error
                            // message
                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting params to register url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("fileLocation", fileLocation);
                    return params;
                }

            };

            // Adding request to request queue
            App.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
        private void showDialog() {
            if (!pDialog.isShowing())
                pDialog.show();
        }

        private void hideDialog() {
            if (pDialog.isShowing())
                pDialog.dismiss();
        }
        public String Base64Encode(String RawString){
            byte[] bytesEncoded = Base64.encodeBase64(RawString.getBytes());
            return new String(bytesEncoded);
        }
        public String getFileType(String fileName) {

            String fileType = "";
            switch (fileName.substring(fileName.indexOf(".") + 1)) {
                case "doc" : fileType =  "Documents"; break;
                case "docx": fileType =  "Documents"; break;
                case "xls" : fileType =  "Documents"; break;
                case "xlsx": fileType =  "Documents"; break;
                case "ppt" : fileType =  "Documents"; break;
                case "pptx": fileType =  "Documents"; break;
                case "pdf" : fileType =  "Documents"; break;
                case "pub" : fileType =  "Documents"; break;
                case "rtf" : fileType =  "Documents"; break;
                case "txt" : fileType =  "Documents"; break;
                case "mp3" : fileType =  "Music"; break;
                case "mp4" : fileType =  "VideoItem"; break;
                case "avi" : fileType =  "VideoItem"; break;
                case "png" : fileType =  "Pictures"; break;
                case "jpg" : fileType =  "Pictures"; break;
            }
            return fileType;
        }

        private void fileChewer(final VideoModel fileToDelete, String uid , final int pos){
            StorageReference localReferencePath = mVideosStorageReference.child(fileToDelete.getName());
            localReferencePath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    videos.remove(pos);
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
    }


    public void showPermissionsDialogueStorage() {
        new AwesomeSuccessDialog(getActivity())
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

    public void onClickGoToAppSettings() {
        PermissionsManager.get()
                .intentToAppSettings(getActivity());
    }

    private void initDownloadTask(ImageModel fileToDownload, String uid){


        mStorageReference.getReference("images").child(uid).child(fileToDownload.getKey());
        File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        mVideosStorageReference.child(uid).child(fileToDownload.getKey()).getFile(storageLoc).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                Log.d(TAG,"file raenda") ;
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//            }
//        });
        StorageReference localReferencePath = mVideosStorageReference.child(uid).child(fileToDownload.getName());
        localReferencePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

            Log.e(TAG,uri.toString());
                String path = "";
                String  downloadUrl= "";
            path = uri.getPath().toString();
            downloadUrl = uri.getEncodedPath().toString();
                Log.e(TAG,path);

            }
        });

        File localFile;
        String ext = FilenameUtils.getExtension(fileToDownload.getName());
        try {
             localFile = File.createTempFile("album2", ext );
            localReferencePath.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(),"Error Downloading File", Toast.LENGTH_LONG);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),"Error Downloading File", Toast.LENGTH_LONG);
        }



//        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
        //imageUri is a valid Uri
//        DownloadManager.Request r =  new DownloadManager.Request(imageUri);
        //without this line, it works
      //  r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        //subpath is valid

//        r.setDestinationInExternalFilesDir(getActivity(), null, storageLoc.getPath());
//        downloadManager.enqueue(r);
    }
}