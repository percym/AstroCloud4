package astrocloud.zw.co.astrocloud.utils;

import java.util.ArrayList;

import astrocloud.zw.co.astrocloud.models.ContactModel;
import astrocloud.zw.co.astrocloud.models.ImageModel;

/**
 * Created by Percy M on 12/12/2017.
 */

public class GLOBALDECLARATIONS {

    public static ArrayList<ContactModel> GLOBAL_CONTACTS_ARRAYLIST= new ArrayList<>();
    public static String FIRESTORE_DB_PATH= "gs://astrocloudzw.appspot.com/";
    public static Long  TOTAL_AVAILABLE_SPACE= 10737418240L;
    public static Long PICTURES_DATABASE_SIZE =0L;
    public static Long  MUSIC_DATABASE_SIZE=0L;
    public static Long  VIDEO_DATABASE_SIZE=0L;
    public static Long DOCUMENT_DATABASE_SIZE =0L ;
    public static  long CONTACTS_COUNT =0L ;
    public static ArrayList<ImageModel> IMAGESARRAY = new ArrayList<>();



}
