package astrocloud.zw.co.astrocloud.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import astrocloud.zw.co.astrocloud.R;
import astrocloud.zw.co.astrocloud.adapters.GalleryAdapter;
import astrocloud.zw.co.astrocloud.models.Image;
import astrocloud.zw.co.astrocloud.utils.AppConfig;
import astrocloud.zw.co.astrocloud.utils.GLOBALDECLARATIONS;

/**
 * Created by Percy M on 11/9/2016.
 */

public class FragmentPhotos extends Fragment {
    private String TAG = FragmentPhotos.class.getSimpleName();
    //private static final String endpoint = "http://192.168.1.9/mobile_api/user_files.php?user_account_id=a6df6e3a841a3d958934690595ff99866b58387c&content_type=img";

    private String media_url = "";
    private ArrayList<Image> images;
    private ProgressDialog pDialog;
    private GalleryAdapter mAdapter;
    private RecyclerView recyclerView;
    private RelativeLayout folder_state_container;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private FirebaseStorage mStorageReference;
    private StorageReference mImagesStorageReference;
    private StorageReference mUserStorageReference;



    public FragmentPhotos() {
        // Required empty public constructor

    }
    public static FragmentPhotos newInstance( int val) {
        FragmentPhotos fr = new FragmentPhotos();
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
        View view = inflater.inflate(R.layout.activity_glide, container, false);
        setHasOptionsMenu(true);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        folder_state_container = (RelativeLayout) view.findViewById(R.id.folder_state_container);
        //initialise the FireStore
        //mStorageReference = FirebaseStorage.getInstance().getReference(AppConfig.FIRESTOREDBURL);
        mStorageReference = FirebaseStorage.getInstance(AppConfig.FIRESTOREDBURL);
        mImagesStorageReference = mStorageReference.getReference("images");
        mUserStorageReference = mImagesStorageReference.child(userId);
        pDialog = new ProgressDialog(getActivity());
        images = new ArrayList<>();
        mAdapter = new GalleryAdapter(getActivity(), images);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(),2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

         recyclerView.addOnItemTouchListener(new GalleryAdapter.RecyclerTouchListener(getActivity(), recyclerView, new GalleryAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("images", images);
                bundle.putInt("position", position);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
//                SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();
//                newFragment.setArguments(bundle);
//                newFragment.show(ft, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    //    fetchImages();


        return view;
    }
    public void setFullscreen() {
        setFullscreen(getActivity());
    }
    public static boolean isImmersiveAvailable() {
        return android.os.Build.VERSION.SDK_INT >= 19;
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


//    private void fetchImages() {
//
//        pDialog.setMessage("Downloading pictures");
//        pDialog.show();
//        //do not delete to be
//        media_url=AppConfig.URL_GET_USER_FILES + "?user_account_id=" + GlobalDeclarations.UserAccountID+"&content_type=img";
//        Log.d(TAG, media_url);
//        JsonArrayRequest req = new JsonArrayRequest(media_url,
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        Log.d(TAG, response.toString());
//                        pDialog.hide();
//
//                        images.clear();
//                        if(response.length()>0) {
//                            for (int i = 0; i < response.length(); i++) {
//                                try {
//                                    JSONObject object = response.getJSONObject(i);
//                                    Image image = new Image();
//                                    image.setName(object.getString("name"));
//
//                                    JSONObject url = object.getJSONObject("url");
//                                    image.setSmall(Uri.decode(url.getString("small")));
//                                    String del = Uri.decode(url.getString("small"));
//                                    //image.setMedium(url.getString("medium"));
//                                    image.setLarge(Uri.decode(url.getString("large")));
//                                    String del2 = Uri.decode(url.getString("large"));
//                                    image.setTimestamp(object.getString("timestamp"));
//
//                                    images.add(image);
//
//                                } catch (JSONException e) {
//                                    recyclerView.setVisibility(View.GONE);
//                                    folder_state_container.setVisibility(View.VISIBLE);
//                                }
//                            }
//
//                            mAdapter.notifyDataSetChanged();
//                        }else{
//                            recyclerView.setVisibility(View.GONE);
//                            folder_state_container.setVisibility(View.VISIBLE);
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e(TAG, "Error: " + error.getMessage());
//                pDialog.hide();
//            }
//
//        });
//
//        // Adding request to request queue
//        AppController.getInstance().addToRequestQueue(req);
//    }
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

    }
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
}
