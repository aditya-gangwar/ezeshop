package in.ezeshop.appbase.utilities;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.Serializable;

/**
 * Created by adgangwa on 17-07-2016.
 */
public abstract class BackgroundProcessor<T> extends HandlerThread {
    private final static String TAG = "BaseApp-BackgroundProcessor";

    protected Handler mRequestHandler;
    protected Handler mResponseHandler;
    protected BackgroundProcessorListener mListener;

    // Generic structure to pass data to background job
    public static class MessageBgJob implements Serializable {
        // identify requested operation
        public int requestCode;
        // Superset of parameters required by all supported background operations
        public Context ctxt;
        public String callingFragTag;   // null if not called by fragment
        // instead of list/hashmap of parameters, using few generic fields
        // meaning of values depend on the operation requested
        public String argStr1;
        public String argStr2;
        public String argStr3;
        public Long argLong1;
        public Boolean argBool1;
    }

    public interface BackgroundProcessorListener {
        //void onResult(int errorCode, int operationCode);
        void onResult(int errorCode, MessageBgJob opData);
        boolean isUiReady();
        boolean isQuitting();
    }
    public void setOnResultListener(BackgroundProcessorListener listener) {
        mListener = listener;
    }

    public BackgroundProcessor(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                // return result to calling UI thread
                final int currOperation = msg.what;
                //final int errorCode = (err==ErrorCodes.NO_ERROR) ? handleMsg(msg) : err;
                final int errorCode = handleMsg(msg);
                final MessageBgJob opData = (MessageBgJob) msg.obj;

                /*if(mListener.isQuitting()) {
                    LogMy.d(TAG,"Fragment is quitting: "+currOperation+", "+errorCode);
                    return;
                }*/

                /*while (!mListener.isUiReady()) {
                    try {
                        LogMy.d(TAG,"Before wait");
                        wait();
                        LogMy.d(TAG,"After wait");
                    } catch (InterruptedException e) {
                    }
                }*/

                if(mListener.isUiReady()) {
                    LogMy.d(TAG,"UI is ready");
                    mResponseHandler.post(new Runnable() {
                        public void run() {
                            //mListener.onResult(errorCode, currOperation);
                            mListener.onResult(errorCode, opData);
                        }
                    });
                }
            }
        };
    }

    protected abstract int handleMsg(Message msg);

}

