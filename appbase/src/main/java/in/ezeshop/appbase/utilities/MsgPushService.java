package in.ezeshop.appbase.utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.backendless.push.BackendlessPushService;

/**
 * Created by adgangwa on 27-09-2017.
 */

public class MsgPushService extends BackendlessPushService
{
    private static final String TAG = "CustApp-CustPushService";

    private static boolean mChkMsgDevReg;

    public static boolean isChkMsgDevReg() {
        return mChkMsgDevReg;
    }

    public static void setChkMsgDevReg(boolean mChkMsgDevReg) {
        MsgPushService.mChkMsgDevReg = mChkMsgDevReg;
    }

    @Override
    public void onRegistered(Context context, String registrationId )
    {
        LogMy.d(TAG, "In onRegistered");
        //super.onRegistered(context, registrationId);

        //CustomerUser.getInstance().setChkMsgDevReg(true);
        setChkMsgDevReg(true);
    }

    @Override
    public void onUnregistered( Context context, Boolean unregistered )
    {
        //LogMy.d(TAG, "In onUnregistered");
    }

    @Override
    public boolean onMessage( Context context, Intent intent )
    {
        //String message = intent.getStringExtra( "message" );
        //Toast.makeText( context, "Push message received. Message: " + message, Toast.LENGTH_LONG ).show();

        // Below code taken from Backendless default push notification handling
        try {
            String tickerText = intent.getStringExtra("android-ticker-text");
            String contentTitle = intent.getStringExtra("android-content-title");
            String contentText = intent.getStringExtra("android-content-text");
            if(tickerText != null && tickerText.length() > 0) {
                /*int appIcon = context.getApplicationInfo().icon;
                if(appIcon == 0) {
                    appIcon = 17301651;
                }*/

                Intent notificationIntent = context.getPackageManager().getLaunchIntentForPackage(context.getApplicationInfo().packageName);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
                Notification notification = (new NotificationCompat.Builder(context))
                        .setSmallIcon(in.ezeshop.appbase.R.drawable.logo_red).setTicker(tickerText)
                        .setContentTitle(contentTitle).setContentText(contentText)
                        .setContentIntent(contentIntent).setWhen(System.currentTimeMillis()).build();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                /*int customLayout = context.getResources().getIdentifier("notification", "layout", context.getPackageName());
                int customLayoutTitle = context.getResources().getIdentifier("title", "id", context.getPackageName());
                int customLayoutDescription = context.getResources().getIdentifier("text", "id", context.getPackageName());
                int customLayoutImageContainer = context.getResources().getIdentifier("image", "id", context.getPackageName());
                int customLayoutImage = context.getResources().getIdentifier("push_icon", "drawable", context.getPackageName());
                if(customLayout > 0 && customLayoutTitle > 0 && customLayoutDescription > 0 && customLayoutImageContainer > 0) {
                    //NotificationLookAndFeel notificationManager = new NotificationLookAndFeel();
                    //notificationManager.extractColors(context);
                    RemoteViews contentView = new RemoteViews(context.getPackageName(), customLayout);
                    contentView.setTextViewText(customLayoutTitle, contentTitle);
                    contentView.setTextViewText(customLayoutDescription, contentText);
                    contentView.setTextColor(customLayoutTitle, notificationManager.getTextColor());
                    contentView.setFloat(customLayoutTitle, "setTextSize", notificationManager.getTextSize());
                    contentView.setTextColor(customLayoutDescription, notificationManager.getTextColor());
                    contentView.setFloat(customLayoutDescription, "setTextSize", notificationManager.getTextSize());
                    contentView.setImageViewResource(customLayoutImageContainer, customLayoutImage);
                    notification.contentView = contentView;
                }*/

                NotificationManager notificationManager1 = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager1.notify(0, notification);
            }
        } catch (Throwable var18) {
            LogMy.e(TAG, "Error processing push notification", var18);
        }

        // When returning 'true', default Backendless onMessage implementation will be executed.
        // The default implementation displays the notification in the Android Notification Center.
        // Returning false, cancels the execution of the default implementation.
        return false;
    }

    @Override
    public void onError( Context context, String message )
    {
        //Toast.makeText( context, message, Toast.LENGTH_SHORT).show();
    }
}