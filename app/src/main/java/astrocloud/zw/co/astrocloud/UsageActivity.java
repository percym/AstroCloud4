package astrocloud.zw.co.astrocloud;

import android.app.Application;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.snatik.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;

import astrocloud.zw.co.astrocloud.adapters.ModelAdapter;
import astrocloud.zw.co.astrocloud.intefaces.ClickListener;
import astrocloud.zw.co.astrocloud.models.Model;
import astrocloud.zw.co.astrocloud.utils.GLOBALDECLARATIONS;

public class UsageActivity extends AppCompatActivity {
    private TextView tv_photo_count, tv_music_count, tv_docs_count, tv_contact_count, tv_video_count, tvActualStore;
    private RelativeLayout bottomsheet;
    private BottomSheetBehavior sheetBehavior;
    private Button btnShow, btnScan;
    RecyclerView recyclerFiles;
    private File root;
    private ArrayList<File> fileList = new ArrayList<File>();
    private ArrayList<Model> modelList = new ArrayList<>();
    private ArrayList<Model> imageList;
    private ArrayList<Model> musicList;
    private ArrayList<Model> docList;
    private ArrayList<Model> filesContainer;
    ModelAdapter modelAdapter;
    private GridLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tv_photo_count = findViewById(R.id.tv_photo_count);
        tv_music_count = findViewById(R.id.tv_music_count);
        tv_docs_count = findViewById(R.id.tv_docs_count);
        tv_contact_count = findViewById(R.id.tv_contact_count);
        tv_video_count = findViewById(R.id.tv_video_count);
        tvActualStore = findViewById(R.id.tvActualStore);
        bottomsheet = findViewById(R.id.bottomsheet);
        sheetBehavior = BottomSheetBehavior.from(bottomsheet);
        btnShow = findViewById(R.id.btn_show);
        btnScan = findViewById(R.id.btnscan);
        recyclerFiles = findViewById(R.id.recycler_files);




        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                root = new File(Environment.getRootDirectory()
                        .getAbsolutePath());
                getfile(root);
               imageList= new ArrayList<>();
               musicList= new ArrayList<>();
               docList= new ArrayList<>();
               filesContainer = new ArrayList<>();
                parseAllImages();
                parseAllMusic();
                parseAllDocuments();

                ArrayList <Model> xy= imageList;
                ArrayList <Model> xc= musicList;
                ArrayList <Model> xd= docList;
                filesContainer.addAll(imageList);
                filesContainer.addAll(musicList);
                modelAdapter= new ModelAdapter(getApplicationContext(), filesContainer, new ClickListener() {
                    @Override
                    public void onPositionClicked(int position) {
                        Model toBeUploaded =modelAdapter.getModelAt(position);


                    }

                    @Override
                    public void onLongClicked(int position) {

                    }
                });
                mLayoutManager = new GridLayoutManager(getApplicationContext(),1);
                recyclerFiles.setLayoutManager(mLayoutManager);
                recyclerFiles.setAdapter(modelAdapter);
                recyclerFiles.setVisibility(View.VISIBLE);
                recyclerFiles.invalidate();
                modelAdapter.notifyDataSetChanged();


            }
        });
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        // btnBottomSheet.setText("Close Sheet");
                    }
                    break;

                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        //  btnBottomSheet.setText("Expand Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }


            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    //btnBottomSheet.setText("Close sheet");
                } else {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    //btnBottomSheet.setText("Expand sheet");
                }
            }
        });


        double picDouble = GLOBALDECLARATIONS.PICTURES_DATABASE_SIZE.doubleValue();
        double musDouble = GLOBALDECLARATIONS.MUSIC_DATABASE_SIZE.doubleValue();
        double docDouble = GLOBALDECLARATIONS.DOCUMENT_DATABASE_SIZE.doubleValue();
        double videoDouble = GLOBALDECLARATIONS.VIDEO_DATABASE_SIZE.doubleValue();
        long contactCount = GLOBALDECLARATIONS.CONTACTS_COUNT;
        tv_photo_count.setText(format(picDouble, 2));
        tv_music_count.setText(format(musDouble, 2));
        tv_docs_count.setText(format(docDouble, 2));
        tv_video_count.setText(format(videoDouble, 2));
        tv_contact_count.setText(Long.toString(contactCount));
        tvActualStore.setText(format((picDouble + docDouble + musDouble + videoDouble), 2));


    }

    public static String format(double bytes, int digits) {
        String[] dictionary = {"bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        int index = 0;
        for (index = 0; index < dictionary.length; index++) {
            if (bytes < 1024) {
                break;
            }
            bytes = bytes / 1024;
        }
        return String.format("%." + digits + "f", bytes) + " " + dictionary[index];
    }


    public ArrayList<File> getfile(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {


                if (listFile[i].getName().endsWith(".png")
                        || listFile[i].getName().endsWith(".jpg")
                        || listFile[i].getName().endsWith(".jpeg")
                        || listFile[i].getName().endsWith(".gif")
                        || listFile[i].getName().endsWith(".doc")
                        || listFile[i].getName().endsWith(".docx")
                        || listFile[i].getName().endsWith(".xls")
                        || listFile[i].getName().endsWith(".xlsx")
                        || listFile[i].getName().endsWith(".xlsx")
                        || listFile[i].getName().endsWith(".ppt")
                        || listFile[i].getName().endsWith(".pptx")
                        || listFile[i].getName().endsWith(".pdf")
                        || listFile[i].getName().endsWith(".pub")
                        || listFile[i].getName().endsWith(".rtf")
                        || listFile[i].getName().endsWith(".txt")
                        || listFile[i].getName().endsWith(".ogg")
                        || listFile[i].getName().endsWith(".3gpp")
                        || listFile[i].getName().endsWith(".wav")
                        || listFile[i].getName().endsWith(".mp3")
                        || listFile[i].getName().endsWith(".mp4")
                        || listFile[i].getName().endsWith(".avi")
                        || listFile[i].getName().endsWith(".webm")
                        )

                {
                    fileList.add(listFile[i]);
                }
            }

        }

        return fileList;
    }


//    public ArrayList<Model> getLastMediaExternal() {
//        int id = 0;
////        final String[] projection = new String[] { "_id","_size","_data","_display_name","mime_type"};
//        modelListExternal = new ArrayList<>();
//        final String[] projection = new String[]{MediaStore.Files.FileColumns._ID,
//                MediaStore.Files.FileColumns.DATA,
//                MediaStore.Files.FileColumns.DISPLAY_NAME,
//                MediaStore.Files.FileColumns.MIME_TYPE,
//                MediaStore.Files.FileColumns.SIZE};
//
//        String selectionMimeType =
//                MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
//                        MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
//                        MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
//                        MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
//                        MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
//                        MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR "
//                        + MediaStore.Files.FileColumns.MIME_TYPE + "=?  ";
//
//
//        //-------------images
//        String jpg = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg");
//        String png = MimeTypeMap.getSingleton().getMimeTypeFromExtension("png");
//        String jpeg = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpeg");
//        String gif = MimeTypeMap.getSingleton().getMimeTypeFromExtension("gif");
//        String bmp = MimeTypeMap.getSingleton().getMimeTypeFromExtension("bmp");
//        String webp = MimeTypeMap.getSingleton().getMimeTypeFromExtension("webp");
//        //--------------music
//        String mp3 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");
//        String m4a = MimeTypeMap.getSingleton().getMimeTypeFromExtension("m4a");
//        //String acc  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("acc");
//        String ts = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ts");
//        String txt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt");
//        String pdf = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
//        String xls = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls");
//        String xlt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlt");
//        String xlm = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlt");
//        String xltx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xltx");
////            String xltm  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xltm");
////            String pptx  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pptx");
////            String ptmp  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ptmp");
//        String ppt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppt");
//        String pot = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pot");
//        String pps = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pps");
////            String potx  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("potx");
////            String potm  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("potm");
////            String ppam  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppam");
//        String doc = MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc");
//        String dot = MimeTypeMap.getSingleton().getMimeTypeFromExtension("dot");
//        String docx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx");
////            String docm  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docm");
//        String dotx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("dotx");
//        String dotm = MimeTypeMap.getSingleton().getMimeTypeFromExtension("dotm");
//        String docb = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docb");
//
//
//        String[] selectionArgsPdf = new String[]{jpg, png, gif, bmp, webp, txt, ts, pdf, xls, xlt, xltx, ppt, pot, pps, doc, dot, docx, dotx, jpeg};
//
//        final Cursor cursor = getContentResolver().query(MediaStore.Images.Media.INTERNAL_CONTENT_URI, projection, selectionMimeType, selectionArgsPdf, "date_added DESC");
//
//        if (cursor != null && cursor.moveToFirst()) {
//
//            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
//            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
//            int displayName = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
//            int mimeType = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
//            int size = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
//
//            int idCol = cursor.getInt(idColumn);
//            String dataCol = cursor.getString(dataColumn);
//            String nameCol = cursor.getString(displayName);
//            String mimeCol = cursor.getString(mimeType);
//            long sizeCol = cursor.getLong(size);
//
//
//            modelList.add(new Model(dataCol, nameCol, mimeCol, sizeCol, idCol));
//
//
//        }
//        return modelList;
//    }

//    public ArrayList<Model> getLastMediaInternal() {
//        int id = 0;
////        final String[] projection = new String[] { "_id","_size","_data","_display_name","mime_type"};
//        modelListExternal = new ArrayList<>();
//        final String[] projection = new String[]{MediaStore.MediaColumns._ID,
//                MediaStore.MediaColumns.DATA,
//                MediaStore.MediaColumns.DISPLAY_NAME,
//                MediaStore.MediaColumns.MIME_TYPE,
//                MediaStore.MediaColumns.SIZE};
//
//        String selectionMimeType =
//                MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
//                        MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
//                        MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
//                        MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
//                        MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
//                        MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR "
//                        + MediaStore.Files.FileColumns.MIME_TYPE + "=?  ";
//
//
//        //-------------images
//        String jpg = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg");
//        String png = MimeTypeMap.getSingleton().getMimeTypeFromExtension("png");
//        String jpeg = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpeg");
//        String gif = MimeTypeMap.getSingleton().getMimeTypeFromExtension("gif");
//        String bmp = MimeTypeMap.getSingleton().getMimeTypeFromExtension("bmp");
//        String webp = MimeTypeMap.getSingleton().getMimeTypeFromExtension("webp");
//        //--------------music
//        String mp3 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");
//        String acc  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("acc");
//        String ts = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ts");
//        String txt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt");
//        String pdf = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
//        String xls = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls");
//        String xlt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlt");
//        String xlm = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlt");
//        String xltx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xltx");
////      String xltm  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xltm");
////      String pptx  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pptx");
////      String ptmp  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ptmp");
//        String ppt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppt");
//        String pot = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pot");
//        String pps = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pps");
////      String potx  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("potx");
////      String potm  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("potm");
////      String ppam  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppam");
//        String doc = MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc");
//        String dot = MimeTypeMap.getSingleton().getMimeTypeFromExtension("dot");
//        String docx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx");
////      String docm  = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docm");
//        String dotx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("dotx");
//        String dotm = MimeTypeMap.getSingleton().getMimeTypeFromExtension("dotm");
//        String docb = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docb");
//
//
//        String[] selectionArgsPdf = new String[]{jpg, png, gif, bmp, webp, txt, ts, pdf, xls, xlt, xltx, ppt, pot, pps, doc, dot, docx, dotx, jpeg};
//
//        final Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selectionMimeType, selectionArgsPdf, "date_added DESC");
//
//        if (cursor != null && cursor.moveToFirst()) {
//
//            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
//            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
//            int displayName = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
//            int mimeType = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE);
//            int size = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
//
//            int idCol = cursor.getInt(idColumn);
//            String dataCol = cursor.getString(dataColumn);
//            String nameCol = cursor.getString(displayName);
//            String mimeCol = cursor.getString(mimeType);
//            long sizeCol = cursor.getLong(size);
//
//
//            modelListExternal.add(new Model(dataCol, nameCol, mimeCol, sizeCol, idCol));
//
//
//        }
//        return modelListExternal;
//    }

    private ArrayList<Model> parseAllImages() {
        try {
            String[] projection = {MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.SIZE};

            String selectionMimeType =
                    MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?  ";

            //-------------images
            String jpg = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg");
            String png = MimeTypeMap.getSingleton().getMimeTypeFromExtension("png");
            String jpeg = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpeg");
            String gif = MimeTypeMap.getSingleton().getMimeTypeFromExtension("gif");

            String[] selectionArgsPdf = new String[]{jpg, png, gif, jpeg};


            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection,selectionMimeType,selectionArgsPdf,null);


            int size = cursor.getCount();


           while (cursor.moveToNext()){

                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                    int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                    int displayName = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    int mimeType = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
                    int filesize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                    int dateadded = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);


                    int idCol = cursor.getInt(idColumn);
                    String dataCol = cursor.getString(dataColumn);
                    String nameCol = cursor.getString(displayName);
                    String mimeCol = cursor.getString(mimeType);
                    long sizeCol = cursor.getLong(filesize);
                    String dateAdded = cursor.getString(dateadded);
                    Model container =    new Model(dataCol, nameCol, mimeCol, sizeCol, idCol, dateAdded);
                   imageList.add(container);
                }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return  imageList;
    }
    private ArrayList<Model> parseAllMusic() {
        try {
            String[] projection = {MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_ADDED};

            String selectionMimeType =
                    MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?  ";

            //-------------images
            String mp3 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");
            String ogg = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ogg");
            String tgpp = MimeTypeMap.getSingleton().getMimeTypeFromExtension("3gpp");
            String wav = MimeTypeMap.getSingleton().getMimeTypeFromExtension("wav");

            String[] selectionArgsPdf = new String[]{mp3, ogg, tgpp, wav};


            Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection,selectionMimeType,selectionArgsPdf,null);


            int size = cursor.getCount();


            while (cursor.moveToNext()){

                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                int displayName = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                int mimeType = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
                int filesize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                int dateadded = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);


                int idCol = cursor.getInt(idColumn);
                String dataCol = cursor.getString(dataColumn);
                String nameCol = cursor.getString(displayName);
                String mimeCol = cursor.getString(mimeType);
                long sizeCol = cursor.getLong(filesize);
                String dateAdded = cursor.getString(dateadded);
                Model container =    new Model(dataCol, nameCol, mimeCol, sizeCol, idCol,dateAdded);
               musicList.add(container);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return  musicList;
    }
    private ArrayList<Model> parseAllDocuments(){
        try {
            String[] projection = {MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.SIZE};

            String selectionMimeType =
                    MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?  ";

            //-------------images
            String doc = MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc");
            String docx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx");
            String xls = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls");
            String xlsx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx");
            String ppt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppt");
            String pptx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pptx");
            String pdf = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
            String pub = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pub");
            String rtf = MimeTypeMap.getSingleton().getMimeTypeFromExtension("rtf");
            String txt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt");



            String[] selectionArgsPdf = new String[]{doc, docx, xls, xlsx, ppt, pptx, pdf , pub, rtf , txt};


            Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("internal"),projection,selectionMimeType,selectionArgsPdf,null);


            int size = cursor.getCount();


            while (cursor.moveToNext()){

                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                int displayName = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                int mimeType = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
                int filesize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                int dateadded = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);


                int idCol = cursor.getInt(idColumn);
                String dataCol = cursor.getString(dataColumn);
                String nameCol = cursor.getString(displayName);
                String mimeCol = cursor.getString(mimeType);
                long sizeCol = cursor.getLong(filesize);
                String dateAdded = cursor.getString(dateadded);
                Model container =    new Model(dataCol, nameCol, mimeCol, sizeCol, idCol,dateAdded);
                musicList.add(container);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return  musicList;
    }
}