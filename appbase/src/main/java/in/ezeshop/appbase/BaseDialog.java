package in.ezeshop.appbase;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.LogMy;

/**
 * Created by adgangwa on 15-02-2017.
 */

public abstract class BaseDialog extends DialogFragment implements
        DialogInterface.OnClickListener, View.OnTouchListener, View.OnClickListener {
    private static final String TAG = "BaseApp-BaseDialog";

    /*
     * Below code is same in BaseFragment also
     */
    private long mLastClickTime;
    private long mLastDialogClickTime;
    private long mLastTouchTime;

    // Anstract fxs to handle 'Btn Click' and 'Touch on Text fields'
    public abstract boolean handleTouchUp(View v);
    public abstract void handleDialogBtnClick(DialogInterface dialog, int which);

    public void handleBtnClick(View v) {
        // Doing nothing - the subclass should override this
        // Not making it abstract - to avoid adding the same in all existing dialog classes
    }

    @Override
    public void onClick(View v) {
        long currentClickTime= SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime- mLastClickTime;

        mLastClickTime = currentClickTime;

        if(elapsedTime<=AppConstants.MIN_CLICK_INTERVAL)
            return;

        handleBtnClick(v);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        long currentClickTime= SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime- mLastDialogClickTime;

        mLastDialogClickTime =currentClickTime;

        if(elapsedTime<=AppConstants.MIN_CLICK_INTERVAL)
            return;

        handleDialogBtnClick(dialog, which);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction()!=MotionEvent.ACTION_UP) {
            return true;
        }

        long currentClickTime= SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime-mLastTouchTime;
        mLastTouchTime=currentClickTime;

        if(elapsedTime<=AppConstants.MIN_CLICK_INTERVAL) {
            LogMy.d(TAG,"Double Touch Case");
            return true;
        }

        return handleTouchUp(v);
    }
}
