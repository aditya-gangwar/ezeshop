package in.ezeshop.internal;

import android.app.Application;

import com.backendless.Backendless;
import com.crashlytics.android.Crashlytics;
import com.helpshift.All;
import com.helpshift.Core;
import com.helpshift.InstallConfig;
import com.helpshift.exceptions.InstallException;

import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.MyGlobalSettings;
import io.fabric.sdk.android.Fabric;

/**
 * Created by adgangwa on 09-12-2016.
 */
public class InternalApp extends Application {
    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();
        // Required initialization logic here!

        // App level initializations - once in main activity
        Backendless.initApp(this, AppConstants.BACKENDLESS_APP_ID, AppConstants.ANDROID_SECRET_KEY, AppConstants.VERSION);
        com.backendless.Backendless.setUrl( AppConstants.BACKENDLESS_HOST );

        // Map all tables to class here - except 'cashback' and 'transaction'
        AppCommonUtil.initTableToClassMappings();
        MyGlobalSettings.setRunMode(MyGlobalSettings.RunMode.appInternalUser);

        // Init crashlytics
        if(AppConstants.USE_CRASHLYTICS) {
            Fabric.with(this, new Crashlytics());
        }

        // Helpshift Init - For Help section in Merchants App
        InstallConfig installConfig = new InstallConfig.Builder()
                .setNotificationIcon(R.drawable.logo_blue)
                .build();
        Core.init(All.getInstance());
        try {
            Core.install(this,
                    "edd7afe788e184aa8c12d8aa278fb467",
                    "myecash.helpshift.com",
                    "myecash_platform_20161206110224571-d81994b14f0dbad",
                    installConfig);

        } catch (InstallException e) {
            LogMy.e("MerchantApp", "Helpshift: Invalid install credentials : ", e);
        }

    }
}

