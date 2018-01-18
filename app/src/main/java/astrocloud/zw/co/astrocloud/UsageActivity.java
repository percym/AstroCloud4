package astrocloud.zw.co.astrocloud;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import astrocloud.zw.co.astrocloud.utils.GLOBALDECLARATIONS;

public class UsageActivity extends AppCompatActivity {
    private TextView tv_photo_count,tv_music_count,tv_docs_count,tv_contact_count,tv_video_count,tvActualStore;
    private RelativeLayout bottomsheet;
    private BottomSheetBehavior sheetBehavior;
    private Button btnShow;
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
        bottomsheet =findViewById(R.id.bottomsheet);
        sheetBehavior = BottomSheetBehavior.from(bottomsheet);
        btnShow= findViewById(R.id.btn_show);

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

//        BottomNavigationItem bottomNavigationItem = new BottomNavigationItem
//                ("Pics", ContextCompat.getColor(this, R.color.some_green), R.drawable.ic_photo_btm);
//        BottomNavigationItem bottomNavigationItem1 = new BottomNavigationItem
//                ("Music", ContextCompat.getColor(this, R.color.some_green), R.drawable.ic_music_btm);
//        BottomNavigationItem bottomNavigationItem2 = new BottomNavigationItem
//                ("Videos", ContextCompat.getColor(this, R.color.some_green), R.drawable.ic_video_btm);
//        BottomNavigationItem bottomNavigationItem3 = new BottomNavigationItem
//                ("Music", ContextCompat.getColor(this, R.color.some_green), R.drawable.ic_doc_btm);
//        bottomNavigation.addTab(bottomNavigationItem);
//        bottomNavigation.addTab(bottomNavigationItem1);
//        bottomNavigation.addTab(bottomNavigationItem2);
//        bottomNavigation.addTab(bottomNavigationItem3);
//
//        bottomNavigation.setOnBottomNavigationItemClickListener(new OnBottomNavigationItemClickListener() {
//            @Override
//            public void onNavigationItemClick(int index) {
//                switch (index){
//                    case 0:{
//                        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
//                            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//                            //btnBottomSheet.setText("Close sheet");
//                        } else {
//
//                            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                        }
//                        break;
//                    }
//                }
//            }
//        });

//        bottomNavigation.setOnSelectedItemChangeListener(new OnSelectedItemChangeListener() {
//            @Override
//            public void onSelectedItemChanged(int itemId) {
//                switch (itemId){
//                    case R.id.tab_pictures:
//                        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
//                            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                            //btnBottomSheet.setText("Close sheet");
//                        } else {
//                            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//                            //btnBottomSheet.setText("Expand sheet");
//                        }
//                        break;
//                }
//            }
//        });


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


        double picDouble=  GLOBALDECLARATIONS.PICTURES_DATABASE_SIZE.doubleValue();
        double musDouble=  GLOBALDECLARATIONS.MUSIC_DATABASE_SIZE.doubleValue();
        double docDouble=  GLOBALDECLARATIONS.DOCUMENT_DATABASE_SIZE.doubleValue();
        double videoDouble=  GLOBALDECLARATIONS.VIDEO_DATABASE_SIZE.doubleValue();
        long contactCount = GLOBALDECLARATIONS.CONTACTS_COUNT;
        tv_photo_count.setText(format(picDouble,2));
        tv_music_count.setText(format(musDouble,2));
        tv_docs_count.setText(format(docDouble,2));
        tv_video_count.setText(format(videoDouble,2));
        tv_contact_count.setText(Long.toString(contactCount));
        tvActualStore.setText(format((picDouble +docDouble + musDouble + videoDouble),2));



    }
    public static String format(double bytes, int digits) {
        String[] dictionary = { "bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
        int index = 0;
        for (index = 0; index < dictionary.length; index++) {
            if (bytes < 1024) {
                break;
            }
            bytes = bytes / 1024;
        }
        return String.format("%." + digits + "f", bytes) + " " + dictionary[index];
    }

}
