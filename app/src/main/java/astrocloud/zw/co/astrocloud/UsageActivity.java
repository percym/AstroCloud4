package astrocloud.zw.co.astrocloud;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import astrocloud.zw.co.astrocloud.utils.FireSizeCalculator;
import astrocloud.zw.co.astrocloud.utils.GLOBALDECLARATIONS;

public class UsageActivity extends AppCompatActivity {
    private TextView tv_photo_count,tv_music_count,tv_docs_count,tv_contact_count,tv_video_count,tvActualStore;

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
