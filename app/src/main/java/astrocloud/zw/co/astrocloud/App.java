package astrocloud.zw.co.astrocloud;

import android.app.Application;

import net.ralphpina.permissionsmanager.PermissionsManager;

/**
 * Created by Percy M on 12/11/2017.
 */

public class App extends Application {

    public App(){}

    // Create the instance
    private static App instance;


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
}
