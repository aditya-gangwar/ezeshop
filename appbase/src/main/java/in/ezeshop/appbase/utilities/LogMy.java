package in.ezeshop.appbase.utilities;


import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.HashMap;
import java.util.Map;

import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.common.constants.CommonConstants;

/**
 * Created by adgangwa on 30-08-2016.
 */
public class LogMy {
    public static void d(String tag, String msg) {
        //if(CommonConstants.IS_PRODUCTION_RELEASE) return;
        if(AppConstants.DEBUG_MODE) {
            Log.d(tag,msg);
        }
        if(AppConstants.DEBUG_MODE) {
            logCrashlytics(msg);
        }
    }
    public static void i(String tag, String msg) {
        //if(CommonConstants.IS_PRODUCTION_RELEASE) return;
        if(AppConstants.DEBUG_MODE) {
            Log.i(tag,msg);
        }
        logCrashlytics(msg);
    }
    public static void w(String tag, String msg) {
        if(AppConstants.DEBUG_MODE) {
            Log.w(tag,msg);
        }
        logCrashlytics(msg);
    }
    public static void e(String tag, String msg) {
        if(AppConstants.DEBUG_MODE) {
            Log.e(tag,msg);
        }
        logCrashlytics(msg);
    }
    public static void e(String tag, String msg, Exception e) {
        if(AppConstants.DEBUG_MODE) {
            Log.e(tag,msg,e);
        } else {
            // raise alarm
            Map<String,String> params = new HashMap<>();
            params.put("Msg",msg);
            params.put("e_Msg",e.getMessage());
            //params.put("e_StackTrace",e.getStackTrace().toString());
            AppAlarms.exception("",AppCommonUtil.getUserType(),tag,params);
        }
        logCrashlytics(msg);
    }
    public static void e(String tag, String msg, Throwable t) {
        if(AppConstants.DEBUG_MODE) {
            Log.e(tag,msg,t);
        }
        logCrashlytics(msg);
    }
    public static void wtf(String tag, String msg) {
        if(AppConstants.DEBUG_MODE) {
            Log.wtf(tag,msg);
        } else {
            // raise alarm
            Map<String,String> params = new HashMap<>();
            params.put("Msg",msg);
            AppAlarms.wtf("",AppCommonUtil.getUserType(),tag,params);
        }
        logCrashlytics(msg);
    }

    private static void logCrashlytics(String msg) {
        if(AppConstants.USE_CRASHLYTICS) {
            Crashlytics.log(msg);
        }
    }
}
