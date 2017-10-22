package in.ezeshop.customer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.backendless.Backendless;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import in.ezeshop.appbase.constants.AppConstants;

import in.ezeshop.common.MyGlobalSettings;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.customerbase.LoginCustActivity;
import io.fabric.sdk.android.Fabric;

/**
 * Created by adgangwa on 27-04-2016.
 */
public class SplashActivity extends AppCompatActivity
        implements DialogFragmentWrapper.DialogFragmentWrapperIf {
    private static final String TAG = "SplashActivity";

    //private FetchGlobalSettings mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Init crashlytics
        //CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
        if(AppConstants.USE_CRASHLYTICS) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_splash);

        // App level initializations - once in main activity
        Backendless.initApp(this, AppConstants.BACKENDLESS_APP_ID, AppConstants.ANDROID_SECRET_KEY, AppConstants.VERSION);
        com.backendless.Backendless.setUrl( AppConstants.BACKENDLESS_HOST );

        // Map all tables to class here - except 'cashback' and 'transaction'
        AppCommonUtil.initTableToClassMappings();
        MyGlobalSettings.setRunMode(MyGlobalSettings.RunMode.appCustomer);

        String naErrorStr = AppCommonUtil.isDownAsPerLocalData(SplashActivity.this);
        if(naErrorStr!=null) {
            DialogFragmentWrapper.createNotification(AppConstants.serviceNATitle, naErrorStr, false, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        } else {
            startLoginActivity();
        }

        /*if(savedInstanceState==null) {
            int resultCode = AppCommonUtil.isNetworkAvailableAndConnected(SplashActivity.this);
            if ( resultCode != ErrorCodes.NO_ERROR) {
                // Show error notification dialog
                DialogFragmentWrapper.createNotification(AppConstants.noInternetTitle, AppCommonUtil.getErrorDesc(resultCode), false, true)
                        .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            } else {
                AppCommonUtil.showProgressDialog(this, "Loading ...");
                mTask = new FetchGlobalSettings();
                mTask.execute();
            }
        }*/
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginCustActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /*@Override
    public void onDestroy() {
        if(mTask!=null) {
            mTask.cancel(true);
            mTask = null;
            LogMy.d(TAG, "Background thread destroyed");
        }
        super.onDestroy();
    }*/

    @Override
    public void onDialogResult(String tag, int indexOrResultCode, ArrayList<Integer> selectedItemsIndexList) {
        if(tag.equals(DialogFragmentWrapper.DIALOG_NOTIFICATION)) {
            finish();
        }
    }

    /*private class FetchGlobalSettings extends AsyncTask<Void,Void,Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            //return MyGlobalSettings.fetchAreas();
            try {
                MyGlobalSettings.fetchAreas(MyGlobalSettings.RunMode.appCustomer);
            } catch (Exception e) {
                LogMy.e(TAG,"Failed to fetch global settings: "+e.toString());
                AppAlarms.handleException(e);
                if(e instanceof BackendlessException) {
                    return AppCommonUtil.getLocalErrorCode((BackendlessException) e);
                }
                return ErrorCodes.GENERAL_ERROR;
            }
            return ErrorCodes.NO_ERROR;
        }

        @Override
        protected void onPostExecute(Integer errorCode) {
            AppCommonUtil.cancelProgressDialog(true);
            if(errorCode==ErrorCodes.NO_ERROR) {
                // Check for daily downtime
                int startHour = MyGlobalSettings.getDailyDownStartHour();
                int endHour = MyGlobalSettings.getDailyDownEndHour();
                if(endHour > startHour) {
                    int currHour = (new DateUtil()).getHourOfDay();
                    if(currHour >= startHour && currHour < endHour) {
                        // Show error notification dialog
                        String errorStr = "Service is not available daily between "+startHour+":00 and "+endHour+":00 hours.";
                        DialogFragmentWrapper.createNotification(AppConstants.serviceNATitle, errorStr, false, true)
                                .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
                        return;
                    }
                }

                Date disabledUntil = MyGlobalSettings.getServiceDisabledUntil();
                if(disabledUntil == null || System.currentTimeMillis() > disabledUntil.getTime()) {
                    startLoginActivity();
                } else {
                    // Add time at the end of error message
                    SimpleDateFormat mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.MY_LOCALE);
                    String errorStr = AppCommonUtil.getErrorDesc(ErrorCodes.SERVICE_GLOBAL_DISABLED)
                            + mSdfDateWithTime.format(disabledUntil);
                    // Show error notification dialog
                    DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, errorStr, false, true)
                            .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
                }
            } else {
                DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                        .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            }
        }
    }*/
}
