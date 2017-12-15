package astrocloud.zw.co.astrocloud;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.Volley;

import net.ralphpina.permissionsmanager.PermissionsManager;

/**
 * Created by Percy M on 12/11/2017.
 */

public class App extends Application {

    private static final String TAG = App.class.getCanonicalName() ;

    public App(){}

    // Create the instance
    private static App instance;
    private RequestQueue mRequestQueue;

    public synchronized static App getInstance()
    {
        PermissionsManager.init(instance);

        if (instance== null) {
            return instance;
        }else  {
                if (instance == null)
                    instance = new App();
            }

        // Return the instance
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        PermissionsManager.init(this);
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        // Adding request to request queue
        int socketTimeout = 15000; //15 seconds timeout to cater for sending email
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
