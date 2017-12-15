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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.github.rubensousa.bottomsheetbuilder.BottomSheetMenuDialog;
import com.github.rubensousa.bottomsheetbuilder.adapter.BottomSheetItemClickListener;

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
import astrocloud.zw.co.astrocloud.models.Image;
import astrocloud.zw.co.astrocloud.models.ImageModel;


public class SlideshowDialogFragment extends DialogFragment {
    private String TAG = SlideshowDialogFragment.class.getSimpleName();
    private ArrayList<ImageModel> images;
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private TextView lblCount, lblTitle, lblDate;
    private int selectedPosition = 0;
    private BottomSheetMenuDialog mBottomSheetDialog;
    private BottomSheetBehavior mBehavior;


    static SlideshowDialogFragment newInstance() {
        SlideshowDialogFragment f = new SlideshowDialogFragment();
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

        images = (ArrayList<ImageModel>) getArguments().getSerializable("images");
        selectedPosition = getArguments().getInt("position");

        Log.e(TAG, "position: " + selectedPosition);
        Log.e(TAG, "images size: " + images.size());

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
        lblCount.setText((position + 1) + " of " + images.size());

        ImageModel image = images.get(position);
        lblTitle.setText(image.getName());
       // lblDate.setText(image.getName());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    //  adapter
    public class MyViewPagerAdapter extends PagerAdapter {

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
        public Object instantiateItem(ViewGroup container, int position)  {
            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View view = layoutInflater.inflate(R.layout.image_fullsreen_preview, container, false);
            final ImageView imageViewPreview = (ImageView) view.findViewById(R.id.image_preview);
            final ImageModel image = images.get(position);
            pDialog = new ProgressDialog(getActivity());
            pDialog.setCancelable(false);
          //  String FileDetails = getIntent().getStringExtra("FileDetails");
         //   String[] FileDetailsFields = FileDetails.split("\\*", -1);

            Glide.with(getActivity()).load(image.getUrl())
                    .thumbnail(0.5f)
                    .into(imageViewPreview);


            imageViewPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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
//                                            if (Build.VERSION.SDK_INT >= 23) {
//                                                verifyStoragePermissions(getActivity());
//                                                if (getPermissionStatus(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == 0 && (getPermissionStatus(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0) ) {
//                                                    saveImageLocally(imageViewPreview);
//                                                } else {
//                                                    Toast.makeText(getContext(), "Allow all permissions for application to run properly ", Toast.LENGTH_LONG);
//                                                }
//                                            } else {
//                                                saveImageLocally(imageViewPreview);
//                                            }
//                                             break;
                                        }
                                        case R.id.image_delete:{
//                                            MarkedFileLocationLink = "/userFiles/" + GlobalDeclarations.UserAccountID + "/" + getFileType(image.getName()) + "/" + image.getName();
//                                            deleteFile(Base64Encode(MarkedFileLocationLink), AppConfig.URL_DELETE_FILE);
//                                            break;
                                        }
                                        case R.id.image_share:{
                                            if (Build.VERSION.SDK_INT >= 23) {
                                                verifyStoragePermissions(getActivity());
                                                if (getPermissionStatus(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == 0 && (getPermissionStatus(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0) ) {
                                                    final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                                    shareIntent.setType("image/jpg");
                                                    shareIntent.putExtra(Intent.EXTRA_STREAM, getImageLocal(imageViewPreview));
                                                    shareIntent.putExtra(Intent.EXTRA_TITLE,"Shared  from Astrocloud");
                                                    // shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Shared  from Astrocloud");
                                                    startActivity(Intent.createChooser(shareIntent, "Share image using"));
                                                } else {
                                                    Toast.makeText(getContext(), "Allow all permissions for application to run properly ", Toast.LENGTH_LONG);
                                                }


                                            }else {
                                                final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                                shareIntent.setType("image/jpg");
                                                shareIntent.putExtra(Intent.EXTRA_STREAM, getImageLocal(imageViewPreview));
                                                shareIntent.putExtra(Intent.EXTRA_TITLE,"Shared  from Astrocloud");
                                                // shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Shared  from Astrocloud");
                                                startActivity(Intent.createChooser(shareIntent, "Share image using"));
                                            }

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

                }
            });

            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View) obj);
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }


        private String saveImageLocally(ImageView iv) {
            iv.buildDrawingCache();

            Bitmap bmp = iv.getDrawingCache();

            File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); //context.getExternalFilesDir(null);
            String imagename=SlideshowDialogFragment.this.lblTitle.getText().toString();
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

            return "true";
        }

        private Uri getImageLocal(ImageView iv) {
            iv.buildDrawingCache();

            Bitmap bmp = iv.getDrawingCache();

            File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); //context.getExternalFilesDir(null);
            String imagename=SlideshowDialogFragment.this.lblTitle.getText().toString();
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
                case "mp4" : fileType =  "Video"; break;
                case "avi" : fileType =  "Video"; break;
                case "png" : fileType =  "Pictures"; break;
                case "jpg" : fileType =  "Pictures"; break;
            }
            return fileType;
        }
    }
}