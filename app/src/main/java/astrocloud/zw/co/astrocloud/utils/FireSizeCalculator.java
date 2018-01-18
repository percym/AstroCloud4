package astrocloud.zw.co.astrocloud.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import astrocloud.zw.co.astrocloud.models.DocumentModel;
import astrocloud.zw.co.astrocloud.models.ImageModel;
import astrocloud.zw.co.astrocloud.models.MusicModel;
import astrocloud.zw.co.astrocloud.models.VideoModel;

/**
 * Created by Percy M on 1/9/2018.
 */

public class FireSizeCalculator extends Service{
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DatabaseReference userDatabaseReference;
    private DatabaseReference picturesDatabaseReference;
    private DatabaseReference videoDatabaseReference;
    private DatabaseReference musicDatabaseReference;
    private DatabaseReference documentDatabaseReference;
    private DatabaseReference contactsDatabaseReference;



    private long picturesDatabaseSize;
    private long musicDatabaseSize;
    private long videoDatabaseSize;
    private long documentDatabaseSize;
    private long contactsCount;

    public FireSizeCalculator(){

        userDatabaseReference = FirebaseDatabase.getInstance().getReference();
        picturesDatabaseReference =  userDatabaseReference.child("user_files").child(userId).child("pictures");
        musicDatabaseReference =  userDatabaseReference.child("user_files").child(userId).child("music");
        videoDatabaseReference =  userDatabaseReference.child("user_files").child(userId).child("videos");
        documentDatabaseReference =  userDatabaseReference.child("user_files").child(userId).child("documents");
        contactsDatabaseReference =  userDatabaseReference.child("contacts").child(userId);


    }

    @Override
    public void onCreate() {
        super.onCreate();
        // To avoid cpu-blocking, we create a background handler to run our service
        HandlerThread  thread = new HandlerThread("AstroCloud",Process.THREAD_PRIORITY_BACKGROUND);

        // start the new handler thread
        thread.start();

        mServiceLooper= thread.getLooper();
        // start the service using the background handler
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {



        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // call a new service handler. The service ID can be used to identify the service
        Message message = mServiceHandler.obtainMessage();
        message.arg1 = startId;
        mServiceHandler.sendMessage(message);
        return super.onStartCommand(intent, flags, startId);
    }

    // Object responsible for
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Well calling mServiceHandler.sendMessage(message); from onStartCommand,
            // this method will be called.

            // Add your cpu-blocking activity here
            try {
                getImagesDatabaseSize(picturesDatabaseReference);
                getMusicDatabaseReference(musicDatabaseReference);
                getVideoDatabaseReference(videoDatabaseReference);
                getDocumentDatabaseReference(documentDatabaseReference);
                getContactDatabaseReference(contactsDatabaseReference);


            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
//            showToast("Finishing TutorialService, id: " + msg.arg1);
            // the msg.arg1 is the startId used in the onStartCommand, so we can track the running sevice here.
            stopSelf(msg.arg1);
        }

        private void showToast(String s) {
            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
        }
    }

    public long getPicturesDatabaseSize() {
        return picturesDatabaseSize;
    }
    public long getMusicDatabaseSize() { return musicDatabaseSize; }
    public long getVideoDatabaseSize() {
        return videoDatabaseSize;
    }
    public long getDocumentDatabaseSize() {   return documentDatabaseSize; }
    public long getContactsCount() {
        return contactsCount;
    }
    public long getTotalUsedSpace(){
        return musicDatabaseSize + picturesDatabaseSize + videoDatabaseSize;
    }

    public long getImagesDatabaseSize(DatabaseReference imagesDatabaseReference){
        DatabaseReference localDatabaseReference = imagesDatabaseReference;
        localDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ImageModel imageModel = new ImageModel();
                imageModel = dataSnapshot.getValue(ImageModel.class);
                picturesDatabaseSize += imageModel.getSizeInBytes();
                GLOBALDECLARATIONS.PICTURES_DATABASE_SIZE = picturesDatabaseSize;
            }



            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                ImageModel imageModel = new ImageModel();
                imageModel = dataSnapshot.getValue(ImageModel.class);
                picturesDatabaseSize -= imageModel.getSizeInBytes();
                GLOBALDECLARATIONS.PICTURES_DATABASE_SIZE = picturesDatabaseSize;
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return picturesDatabaseSize;
    }
    public long getMusicDatabaseReference(final DatabaseReference musicDatabaseReference) {
        DatabaseReference localDatabaseReference = musicDatabaseReference;
        localDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MusicModel musicModel = new MusicModel();
                musicModel = dataSnapshot.getValue(MusicModel.class);
                musicDatabaseSize += musicModel.getSizeInBytes();
                GLOBALDECLARATIONS.MUSIC_DATABASE_SIZE = musicDatabaseSize;

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                MusicModel musicModel = new MusicModel();
                musicModel = dataSnapshot.getValue(MusicModel.class);
                musicDatabaseSize -= musicModel.getSizeInBytes();
                GLOBALDECLARATIONS.MUSIC_DATABASE_SIZE = musicDatabaseSize;

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return musicDatabaseSize;
    }
    public long getVideoDatabaseReference(final DatabaseReference videoDatabaseReference) {
        DatabaseReference localDatabaseReference = videoDatabaseReference;
        localDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
               VideoModel videoModel = new VideoModel();
                videoModel = dataSnapshot.getValue(VideoModel.class);
                videoDatabaseSize += videoModel.getSizeInBytes();
                GLOBALDECLARATIONS.VIDEO_DATABASE_SIZE = videoDatabaseSize;

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                VideoModel videoModel = new VideoModel();
                videoModel = dataSnapshot.getValue(VideoModel.class);
                videoDatabaseSize -= videoModel.getSizeInBytes();
                GLOBALDECLARATIONS.VIDEO_DATABASE_SIZE = videoDatabaseSize;

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return videoDatabaseSize;
    }
    public long getDocumentDatabaseReference(final DatabaseReference documentDatabaseReference) {
        DatabaseReference localDatabaseReference = documentDatabaseReference;
        localDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DocumentModel documentModel = new DocumentModel();
                documentModel = dataSnapshot.getValue(DocumentModel.class);
                documentDatabaseSize += documentModel.getSizeInBytes();
                GLOBALDECLARATIONS.DOCUMENT_DATABASE_SIZE = documentDatabaseSize;

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                DocumentModel documentModel = new DocumentModel();
                documentModel = dataSnapshot.getValue(DocumentModel.class);
                documentDatabaseSize -= documentModel.getSizeInBytes();
                GLOBALDECLARATIONS.DOCUMENT_DATABASE_SIZE = documentDatabaseSize;

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return musicDatabaseSize;
    }
    public long getContactDatabaseReference(final DatabaseReference contactDatabaseReference) {
        DatabaseReference localDatabaseReference = contactDatabaseReference;
        localDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                contactsCount= dataSnapshot.getChildrenCount();
                GLOBALDECLARATIONS.CONTACTS_COUNT = contactsCount;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return musicDatabaseSize;
    }

}
