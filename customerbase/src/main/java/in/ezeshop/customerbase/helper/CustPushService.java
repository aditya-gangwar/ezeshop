package in.ezeshop.customerbase.helper;

import android.content.Context;
import android.content.Intent;

import com.backendless.push.BackendlessPushService;

import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.customerbase.entities.CustomerUser;

/**
 * Created by adgangwa on 27-09-2017.
 */

public class CustPushService extends BackendlessPushService
{
    private static final String TAG = "CustApp-CustPushService";

    @Override
    public void onRegistered(Context context, String registrationId )
    {
        LogMy.d(TAG, "In onRegistered");
        //super.onRegistered(context, registrationId);

        CustomerUser.getInstance().setChkMsgDevReg(true);
    }

    /*@Override
    public void onUnregistered( Context context, Boolean unregistered )
    {
        //LogMy.d(TAG, "In onUnregistered");
    }

    @Override
    public boolean onMessage( Context context, Intent intent )
    {
        //String message = intent.getStringExtra( "message" );
        //Toast.makeText( context, "Push message received. Message: " + message, Toast.LENGTH_LONG ).show();

        // When returning 'true', default Backendless onMessage implementation will be executed.
        // The default implementation displays the notification in the Android Notification Center.
        // Returning false, cancels the execution of the default implementation.
        return true;
    }

    @Override
    public void onError( Context context, String message )
    {
        //Toast.makeText( context, message, Toast.LENGTH_SHORT).show();
    }*/
}