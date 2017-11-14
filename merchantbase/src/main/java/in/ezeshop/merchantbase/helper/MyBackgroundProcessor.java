package in.ezeshop.merchantbase.helper;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.backendless.exceptions.BackendlessException;

import in.ezeshop.appbase.backendAPI.CommonServices;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Cashback;
import in.ezeshop.common.database.MerchantStats;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.BackgroundProcessor;
import in.ezeshop.appbase.utilities.FileFetchr;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.database.Transaction;
import in.ezeshop.merchantbase.backendAPI.MerchantServices;
import in.ezeshop.merchantbase.entities.MerchantUser;
import in.ezeshop.appbase.entities.MyCashback;
import in.ezeshop.appbase.entities.MyTransaction;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by adgangwa on 27-02-2016.
 */
public class MyBackgroundProcessor<T> extends BackgroundProcessor<T> {
    private final static String TAG = "MchntApp-MyBackgroundProcessor";

    private MyRetainedFragment mRetainedFragment;

    public MyBackgroundProcessor(Handler responseHandler, MyRetainedFragment retainedFragment) {
        super(responseHandler);
        mRetainedFragment = retainedFragment;
    }

    /*
     * Add request method
     */
    public void addBackgroundJob(int requestCode, Context ctxt, String callingFragTag,
                                 String argStr1, String argStr2, String argStr3, Long argLong1, Boolean argBool1) {
        LogMy.d(TAG,"Adding background job: "+requestCode);

        MessageBgJob data = new MessageBgJob();
        data.requestCode = requestCode;
        data.ctxt = ctxt;
        data.callingFragTag = callingFragTag;
        data.argStr1 = argStr1;
        data.argStr2 = argStr2;
        data.argStr3 = argStr3;
        data.argLong1 = argLong1;
        data.argBool1 = argBool1;
        mRequestHandler.obtainMessage(requestCode, data).sendToTarget();
    }


    /*private class MessageLogin implements Serializable {
        public String userId;
        public String passwd;
        public String deviceId;
        public String otp;
    }
    private class MessageResetPassword implements Serializable {
        public String userId;
        public String brandName;
        public String deviceId;
    }
    private class MessageCustRegister implements Serializable {
        public String firstName;
        public String lastName;
        public String mobileNum;
        public String otp;
        public String qrCode;
        public String dob;
        public int sex;
    }
    private class MessageDelDevice implements Serializable {
        public String curDeviceId;
        public int index;
    }
    private class MessageChangePassword implements Serializable {
        public String oldPasswd;
        public String newPasswd;
    }
    private class MessageFileDownload implements Serializable {
        public Context ctxt;
        public String fileUrl;
    }
    private class MessageForgotId implements Serializable {
        public String mobileNum;
        public String deviceId;
    }
    private class MessageCancelTxn implements Serializable {
        public String txnId;
        public String cardId;
        public String pin;
        public boolean isOtp;
    }
    private class MessageTxnCommit implements Serializable {
        public String pin;
        public boolean isOtp;
    }
    private class MessageImgUpload implements Serializable {
        public File file;
        public String remoteDir;
    }
    private class MessageLoadTest implements Serializable {
        public String custId;
        public String pin;
        public int reps;
    }*/

    /*
     * Add request methods - Assumes that MerchantUser is instantiated
     */
    /*public void addMerchantOpsReq() {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_FETCH_MERCHANT_OPS, null).sendToTarget();
    }

    public void addArchiveTxnsRequest() {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_ARCHIVE_TXNS,null).sendToTarget();
    }

    public void addMerchantStatsRequest() {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_MERCHANT_STATS,null).sendToTarget();
    }*/

    /*public void addDeleteDeviceRequest(Integer index, String curDeviceId) {
        MessageDelDevice msg = new MessageDelDevice();
        msg.curDeviceId = curDeviceId;
        msg.index = index;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_DELETE_TRUSTED_DEVICE, msg).sendToTarget();
    }*/

    /*public void addChangeMobileRequest() {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_CHANGE_MOBILE,null).sendToTarget();
    }
    public void addImgUploadRequest(File file, String remoteDir) {
        LogMy.d(TAG, "In addImgUploadRequest");
        MessageImgUpload msg = new MessageImgUpload();
        msg.file = file;
        msg.remoteDir = remoteDir;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_UPLOAD_IMG, msg).sendToTarget();
    }
    public void changePassword(String oldPasswd, String newPasswd) {
        LogMy.d(TAG, "In changePassword:  ");
        MessageChangePassword msg = new MessageChangePassword();
        msg.oldPasswd = oldPasswd;
        msg.newPasswd = newPasswd;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_CHANGE_PASSWD,msg).sendToTarget();
    }
    public void addCustomerOp() {
        LogMy.d(TAG, "In addCustomerOp");
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_ADD_CUSTOMER_OP,null).sendToTarget();
    }
    public void addForgotIdRequest(String mobileNum, String deviceId) {
        LogMy.d(TAG, "In addForgotIdRequest");
        MessageForgotId msg = new MessageForgotId();
        msg.deviceId = deviceId;
        msg.mobileNum = mobileNum;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_FORGOT_ID, msg).sendToTarget();
    }
    public void addPasswordRequest(String brandName, String deviceId, String userId) {
        LogMy.d(TAG, "In addPasswordRequest");
        MessageResetPassword msg = new MessageResetPassword();
        msg.brandName = brandName;
        msg.deviceId = deviceId;
        msg.userId = userId;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_GENERATE_MERCHANT_PWD, msg).sendToTarget();
    }
    public void addCommitTransRequest(String pin, boolean isOtp) {
        LogMy.d(TAG, "In addCommitTransRequest");
        MessageTxnCommit msg = new MessageTxnCommit();
        msg.pin = pin;
        msg.isOtp = isOtp;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_PROCESS_TRANS, msg).sendToTarget();
    }
    public void addCashbackRequest(String custId) {
        LogMy.d(TAG, "In addCashbackRequest:  " + custId);
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_GET_CASHBACK, custId).sendToTarget();
    }
    public void addCustRegRequest(String mobileNum, String dob, int sex, String qrCode, String otp, String firstName, String lastName) {
        LogMy.d(TAG, "In addCustRegRequest:  " + mobileNum);
        MessageCustRegister msg = new MessageCustRegister();
        msg.firstName = firstName;
        msg.lastName = lastName;
        msg.mobileNum = mobileNum;
        msg.otp = otp;
        msg.qrCode = qrCode;
        msg.dob = dob;
        msg.sex = sex;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_REGISTER_CUSTOMER,msg).sendToTarget();
    }
    public void addLoginRequest(String userId, String password, String deviceId, String otp) {
        LogMy.d(TAG, "In addLoginRequest");
        MessageLogin msg = new MessageLogin();
        msg.userId = userId;
        msg.deviceId = deviceId;
        msg.passwd = password;
        msg.otp = otp;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_LOGIN, msg).sendToTarget();
    }
    public void addMerchantSettingsRequest() {
        LogMy.d(TAG, "In addMerchantSettingsRequest");
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_UPDATE_MERCHANT_SETTINGS).sendToTarget();
    }
    public void addLogoutRequest() {
        LogMy.d(TAG, "In addLogoutRequest");
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_LOGOUT_MERCHANT).sendToTarget();
    }
    public void addFetchTxnsRequest(String query) {
        LogMy.d(TAG, "In addFetchTxnsRequest");
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_FETCH_TXNS, query).sendToTarget();
    }
    public void addFetchTxnFilesRequest(Context context) {
        LogMy.d(TAG, "In addFetchTxnFilesRequest");
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_FETCH_FILES, context).sendToTarget();
    }
    public void addCustFileDownloadReq(Context context, String fileURL) {
        LogMy.d(TAG, "In addFileDownloadRequest: " + fileURL);
        MessageFileDownload msg = new MessageFileDownload();
        msg.ctxt = context;
        msg.fileUrl = fileURL;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_CUST_DATA_FILE_DOWNLOAD, msg).sendToTarget();
    }*/
    /*public void addCancelTxnReq(String txnId, String cardId, String pin, boolean isOtp) {
        MessageCancelTxn msg = new MessageCancelTxn();
        msg.cardId = cardId;
        msg.txnId = txnId;
        msg.pin = pin;
        msg.isOtp = isOtp;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_CANCEL_TXN, msg).sendToTarget();
    }
    public void addGenTxnOtpReq(String custMobileOrId) {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_GEN_TXN_OTP, custMobileOrId).sendToTarget();
    }
    public void createMchntOrder(String sku, int qty, int totalPrice) {
        MessageMcntOrder msg = new MessageMcntOrder();
        msg.qty = qty;
        msg.skuOrId = sku;
        msg.totalPrice = totalPrice;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_CRT_MCHNT_ORDER, msg).sendToTarget();
    }
    public void addFetchMchntOrderReq() {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_FETCH_MERCHANT_ORDERS, null).sendToTarget();
    }
    public void addDeleteMchntOrder(String orderId) {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_DELETE_MCHNT_ORDER, orderId).sendToTarget();
    }

    public void addLoadTestReq(String custId, String pin, int reps) {
        MessageLoadTest msg = new MessageLoadTest();
        msg.custId = custId;
        msg.pin = pin;
        msg.reps = reps;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_LOAD_TEST, msg).sendToTarget();
    }

    public void addCustIdReq(String custMobile) {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_GET_CUST_ID, custMobile).sendToTarget();
    }*/

    @Override
    protected int handleMsg(Message msg) {
        int error = ErrorCodes.NO_ERROR;
        try {

            // It checks with internet site - so checking only during login
            /*if( msg.what==MyRetainedFragment.REQUEST_LOGIN &&
                    !AppCommonUtil.isInternetConnected()) {
                return ErrorCodes.NO_INTERNET_CONNECTION;
            }*/

            MessageBgJob data = (MessageBgJob)msg.obj;
            switch (msg.what) {
                case MyRetainedFragment.REQUEST_GET_CASHBACK:
                    error = getCashback(data);
                    break;
                case MyRetainedFragment.REQUEST_LOGIN:
                    error = loginMerchant(data);
                    break;
                case MyRetainedFragment.REQUEST_REGISTER_CUSTOMER:
                    error = registerCustomer(data);
                    break;
                case MyRetainedFragment.REQUEST_PROCESS_TRANS:
                    //commitCashTrans((Transaction) msg.obj);
                    error = commitCashTrans(data);
                    break;
                case MyRetainedFragment.REQUEST_UPDATE_MERCHANT_SETTINGS:
                    error = updateMerchantSettings();
                    break;
                case MyRetainedFragment.REQUEST_LOGOUT_MERCHANT:
                    error = logoutMerchant();
                    break;
                case MyRetainedFragment.REQUEST_FETCH_TXNS:
                    error = fetchTransactions(data);
                    break;
                case MyRetainedFragment.REQUEST_FETCH_FILES:
                    error = fetchFiles(data);
                    break;
                case MyRetainedFragment.REQUEST_GENERATE_MERCHANT_PWD:
                    error = generatePassword(data);
                    break;
                case MyRetainedFragment.REQUEST_ADD_CUSTOMER_OP:
                    error = executeCustOp();
                    break;
                case MyRetainedFragment.REQUEST_CHANGE_PASSWD:
                    error = changePassword(data);
                    break;
                /*case MyRetainedFragment.REQUEST_UPLOAD_IMG:
                    error = uploadImgFile(data);
                    break;
                case MyRetainedFragment.REQUEST_DELETE_TRUSTED_DEVICE:
                    error = deleteDevice((MessageDelDevice) msg.obj);
                    break;*/
                case MyRetainedFragment.REQUEST_CHANGE_MOBILE:
                    error = changeMobileNum();
                    break;
                case MyRetainedFragment.REQUEST_MERCHANT_STATS:
                    error = fetchMerchantStats();
                    break;
                case MyRetainedFragment.REQUEST_FORGOT_ID:
                    error = forgotId(data);
                    break;
                case MyRetainedFragment.REQUEST_ARCHIVE_TXNS:
                    error = archiveTxns();
                    break;
                case MyRetainedFragment.REQUEST_CUST_DATA_FILE_DOWNLOAD:
                    error = downloadFile(data.ctxt, data.argStr1, false);
                    break;
                case MyRetainedFragment.REQUEST_FETCH_MERCHANT_OPS:
                    error = fetchMerchantOps();
                    break;
                /*case MyRetainedFragment.REQUEST_CANCEL_TXN:
                    error = cancelTxn((MessageCancelTxn) msg.obj);
                    break;
                case MyRetainedFragment.REQUEST_CRT_MCHNT_ORDER:
                    error = createMchntOrder((MessageMcntOrder) msg.obj);
                    break;
                case MyRetainedFragment.REQUEST_FETCH_MERCHANT_ORDERS:
                    error = fetchMchntOrders();
                    break;
                case MyRetainedFragment.REQUEST_DELETE_MCHNT_ORDER:
                    error = deleteMchntOrder((String) msg.obj);
                    break;*/
                case MyRetainedFragment.REQUEST_GEN_TXN_OTP:
                    error = genTxnOtp(data);
                    break;
                case MyRetainedFragment.REQUEST_LOAD_TEST:
                    error = MerchantUser.getInstance().startLoad(data.argStr1, data.argStr2, data.argLong1);
                    break;
                case MyRetainedFragment.REQUEST_GET_CUST_ID:
                    error = getCustomerId(data);
                    break;
                case MyRetainedFragment.REQUEST_MSG_DEV_REG_CHK:
                    error = chkMsgDevReg();
                    break;
                case MyRetainedFragment.REQUEST_FETCH_PENDING_ORDERS:
                    error = fetchPendingOrders(data);
                    break;
                case MyRetainedFragment.REQUEST_CANCEL_ORDER:
                    error = cancelOrder(data);
                    break;
            }
        } catch (Exception e) {
            LogMy.e(TAG,"Unhandled exception in BG thread", e);
            error = ErrorCodes.GENERAL_ERROR;
        }
        return error;
    }

    private int cancelOrder(MessageBgJob opData) {
        try {
            // Status can be changed only for pending orders
            Transaction txn = mRetainedFragment.mPendingCustOrders.get(opData.argStr1);
            if (txn!=null) {
                Transaction dbTxn = MerchantUser.getInstance().cancelOrder(txn.getTrans_id(), opData.argStr2);
                LogMy.d(TAG, "cancelOrder success");

                // update local order object
                //updatedOrder.setAddressNIDB(order.getAddressNIDB());
                dbTxn.getCustOrder().setCustomerNIDB(txn.getCustOrder().getCustomerNIDB());
                dbTxn.getCustOrder().setMerchantNIDB(txn.getCustOrder().getMerchantNIDB());
                //mRetainedFragment.mSelCustOrder = updatedOrder;

                // Order is closed - delete from pending order list
                mRetainedFragment.mPendingCustOrders.remove(dbTxn.getTrans_id());

            } else {
                // I shouldn't be here
                LogMy.wtf(TAG,"cancelOrder: Order object is null: "+opData.argStr1);
            }
        } catch (BackendlessException e) {
            LogMy.e(TAG,"Exception in cancelOrder: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    private int chkMsgDevReg() {
        LogMy.d(TAG, "In chkMsgDevReg");
        return MerchantUser.getInstance().checkMsgDevReg();
    }

    private int fetchPendingOrders(MessageBgJob opData) {
        mRetainedFragment.mPendingCustOrders = null;

        try {
            List<Transaction> orders = MerchantServices.getInstance().fetchPendingOrders(MerchantUser.getInstance().getMerchantId());
            for (Transaction item :
                    orders) {
                if(mRetainedFragment.mPendingCustOrders==null) {
                    mRetainedFragment.mPendingCustOrders = new HashMap<>(orders.size());
                }
                mRetainedFragment.mPendingCustOrders.put(item.getTrans_id(), item);
            }
            LogMy.d(TAG,"fetched order objects: "+mRetainedFragment.mPendingCustOrders.size());

        } catch (BackendlessException e) {
            LogMy.e(TAG,"Exception in fetchPendingOrders: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    private int fetchMerchantOps() {
        mRetainedFragment.mLastFetchMchntOps = null;

        try {
            mRetainedFragment.mLastFetchMchntOps = MerchantUser.getInstance().fetchMerchantOps();
            LogMy.d(TAG,"fetchMerchantOps success: "+mRetainedFragment.mLastFetchMchntOps.size());

        } catch (BackendlessException e) {
            LogMy.e(TAG,"Exception in fetchMerchantOps: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    private int archiveTxns() {
        return MerchantUser.getInstance().archiveTxns();
    }

    private int loginMerchant(MessageBgJob opData) {
        LogMy.d(TAG, "In loginMerchant");
        return MerchantUser.login(opData.argStr1, opData.argStr2, "", opData.argStr3);
    }
    
    private int fetchMerchantStats() {
        mRetainedFragment.mMerchantStats = null;
        try {
            MerchantStats stats = MerchantUser.getInstance().fetchStats();
            mRetainedFragment.mMerchantStats = stats;

            long updateTime = (mRetainedFragment.mMerchantStats.getUpdated()==null) ?
                    mRetainedFragment.mMerchantStats.getCreated().getTime() :
                    mRetainedFragment.mMerchantStats.getUpdated().getTime();
            LogMy.d(TAG,"getMerchantStats success: "+updateTime);

        } catch (BackendlessException e) {
            LogMy.e(TAG,"Exception in fetchMerchantStats: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    private int changeMobileNum() {
        return MerchantUser.getInstance().changeMobileNum(mRetainedFragment.mVerifyParamMobileChange,
                mRetainedFragment.mNewMobileNum, mRetainedFragment.mOtpMobileChange);
    }

    /*private int deleteDevice(MessageDelDevice msg) {
        return MerchantUser.getInstance().deleteTrustedDevice(msg.index, msg.curDeviceId);
    }*/

    /*private int uploadImgFile(MessageBgJob opData) {
        File file = msg.file;
        try {
            MerchantUser.getInstance().uploadImgFile(file, msg.remoteDir);

        } catch (BackendlessException e) {
            LogMy.e(TAG,"BackendlessException in uploadImgFile: "+file.getAbsolutePath()+", "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);

        } catch(Exception e) {
            LogMy.e(TAG,"Exception in uploadImgFile: "+file.getAbsolutePath()+", "+e.toString(),e);
            return ErrorCodes.GENERAL_ERROR;
        } finally {
            // delete local file
            if(!file.delete()) {
                LogMy.w(TAG,"Failed to delete local image file: "+file.getAbsolutePath());
            }
        }
        return ErrorCodes.NO_ERROR;
    }*/

    private int changePassword(MessageBgJob opData) {
        return MerchantUser.getInstance().changePassword(opData.argStr1, opData.argStr2);
    }

    private int executeCustOp() {
        try
        {
            String imgFilename = MerchantUser.getInstance().executeCustOp(mRetainedFragment.mCustomerOp);
            LogMy.d(TAG,"executeCustOp returned img filename as: "+imgFilename);
            mRetainedFragment.mCustomerOp.setImageFilename(imgFilename);
        }
        catch( BackendlessException e )
        {
            int errCode = AppCommonUtil.getLocalErrorCode(e);
            if(errCode==ErrorCodes.OP_SCHEDULED) {
                // Retrieve imgFilename from exception message
                //String imgFilename = e.getMessage();
                //LogMy.d(TAG,"executeCustOp returned img filename as: "+imgFilename);
                mRetainedFragment.mCustomerOp.setImageFilename(AppCommonUtil.mErrorParams.imgFileName);

            } else if(errCode!=ErrorCodes.OTP_GENERATED) {
                LogMy.e(TAG, "exec customer op failed: "+ e.toString());
            }
            return errCode;
        }
        return ErrorCodes.NO_ERROR;
    }

    private int generatePassword(MessageBgJob opData) {
        return MerchantUser.resetPassword(opData.argStr1,opData.argStr2, "");
    }

    private int forgotId(MessageBgJob opData) {
        return MerchantUser.forgotId(opData.argStr1, "");
    }

    private int registerCustomer(MessageBgJob opData) {
        mRetainedFragment.mCurrCashback = null;
        mRetainedFragment.mCurrCustomer = null;

        try {
            Cashback cashback = MerchantUser.getInstance().registerCustomer(opData.argStr1, "", CommonConstants.SEX_UNKNOWN, "",
                    opData.argStr3, opData.argStr2, "");

            /*mRetainedFragment.mCurrCashback = new MyCashback();
            mRetainedFragment.mCurrCashback.init(cashback, true);*/
            mRetainedFragment.mCurrCashback = new MyCashback(cashback);
            mRetainedFragment.mCurrCustomer = mRetainedFragment.mCurrCashback.getCustomer();

        } catch (Exception e) {
            mRetainedFragment.mCurrCashback = null;
            mRetainedFragment.mCurrCustomer = null;

            if(e instanceof BackendlessException) {
                int error = AppCommonUtil.getLocalErrorCode((BackendlessException)e);
                if(error!=ErrorCodes.OTP_GENERATED) {
                    LogMy.e(TAG, "Exception in registerCustomer: "+ e.toString());
                }
                return error;
            } else {
                return ErrorCodes.GENERAL_ERROR;
            }
        }
        return ErrorCodes.NO_ERROR;
    }

    private int getCashback(MessageBgJob opData) {
        mRetainedFragment.mCurrCashback = null;
        mRetainedFragment.mCurrCustomer = null;

        try {
            Cashback cashback = MerchantUser.getInstance().fetchCashback(opData.argStr1);

            /*mRetainedFragment.mCurrCashback = new MyCashback();
            mRetainedFragment.mCurrCashback.init(cashback, true);*/
            mRetainedFragment.mCurrCashback = new MyCashback(cashback);
            mRetainedFragment.mCurrCustomer = mRetainedFragment.mCurrCashback.getCustomer();

        } catch (Exception e) {
            mRetainedFragment.mCurrCashback = null;
            mRetainedFragment.mCurrCustomer = null;

            LogMy.e(TAG, "Exception in getCashback: "+ e.toString());
            if(e instanceof BackendlessException) {
                return AppCommonUtil.getLocalErrorCode((BackendlessException) e);
            } else {
                return ErrorCodes.GENERAL_ERROR;
            }
        }
        return ErrorCodes.NO_ERROR;
    }

    private int commitCashTrans(MessageBgJob opData) {
        int errorCode =  MerchantUser.getInstance().processTxn(mRetainedFragment.mCurrTransaction, opData.argStr1, opData.argBool1, false);
        if(errorCode==ErrorCodes.NO_ERROR) {
            mRetainedFragment.mCurrCashback.resetOnlyCashback(mRetainedFragment.mCurrTransaction.getTransaction().getCashback());
        }
        return errorCode;
    }

    private int genTxnOtp(MessageBgJob opData) {
        return MerchantUser.getInstance().genTxnOtp(opData.argStr1);
    }

    private int updateMerchantSettings() {
        return MerchantUser.getInstance().updateSettings();
    }

    private int logoutMerchant() {
        return MerchantUser.logoutSync();
    }

    private int fetchTransactions(MessageBgJob opData) {
        mRetainedFragment.mLastFetchTransactions = null;

        try {
            isSessionValid();
            mRetainedFragment.mLastFetchTransactions = MyTransaction.fetch(opData.argStr1, MerchantUser.getInstance().getMerchant().getTxn_table());
            if (mRetainedFragment.mLastFetchTransactions == null || mRetainedFragment.mLastFetchTransactions.size() == 0) {
                return ErrorCodes.NO_DATA_FOUND;
            }
            return ErrorCodes.NO_ERROR;

        } catch (BackendlessException e) {
            mRetainedFragment.mLastFetchTransactions = null;
            LogMy.e(TAG, "Exception in fetchTransactions: "+ e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
    }

    /*private int cancelTxn(MessageCancelTxn msg) {
        //return MerchantUser.getInstance().cancelTxn(msg.txnId, msg.cardId, msg.pin);
        return MerchantUser.getInstance().cancelTxn(mRetainedFragment.mCurrTransaction, msg.cardId, msg.pin, msg.isOtp);
    }

    private int fetchMchntOrders() {
        mRetainedFragment.mLastFetchMchntOrders = null;

        try {
            mRetainedFragment.mLastFetchMchntOrders = MerchantUser.getInstance().fetchMchntOrders();
            LogMy.d(TAG,"fetchMchntOrders success: "+mRetainedFragment.mLastFetchMchntOrders.size());

            // sort by time
            Collections.sort(mRetainedFragment.mLastFetchMchntOrders, new AppCommonUtil.MchntOrderComparator());
            Collections.reverse(mRetainedFragment.mLastFetchMchntOrders);

        } catch (BackendlessException e) {
            LogMy.e(TAG,"Exception in fetchMchntOrders: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    private int createMchntOrder(MessageMcntOrder msg) {
        try {
            MerchantOrders order = MerchantUser.getInstance().createMchntOrder(msg.skuOrId, msg.qty, msg.totalPrice);

            if(mRetainedFragment.mLastFetchMchntOrders==null) {
                mRetainedFragment.mLastFetchMchntOrders = new ArrayList<>();
            }
            mRetainedFragment.mLastFetchMchntOrders.add(order);

            // sort by time
            Collections.sort(mRetainedFragment.mLastFetchMchntOrders, new AppCommonUtil.MchntOrderComparator());
            Collections.reverse(mRetainedFragment.mLastFetchMchntOrders);

        } catch (BackendlessException e) {
            LogMy.e(TAG, "Exception in createMchntOrder: "+ e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    private int deleteMchntOrder(String orderId) {
        int status = MerchantUser.getInstance().deleteMchntOrder(orderId);
        if(status==ErrorCodes.NO_ERROR) {
            // remove from local list also
            Iterator<MerchantOrders> it = mRetainedFragment.mLastFetchMchntOrders.iterator();
            while (it.hasNext()) {
                if (it.next().getOrderId().equals(orderId)) {
                    it.remove();
                    break;
                }
            }
        }
        return status;
    }*/

    private int getCustomerId(MessageBgJob opData) {
        try {
            mRetainedFragment.mTempStr = CommonServices.getInstance().getCustomerId(opData.argStr1);

        } catch(BackendlessException e) {
            LogMy.e(TAG, "getCustomerId failed: " + e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    // Fetches given files from backend
    private int fetchFiles(MessageBgJob opData) {
        int errorCode = ErrorCodes.NO_ERROR;

        try {
            isSessionValid();
        } catch (BackendlessException e) {
            return AppCommonUtil.getLocalErrorCode(e);
        }

        // create a copy of list
        // intentionally done - as original list will be modified for successfully fetched files
        List<String> missingFiles = new ArrayList<>(mRetainedFragment.mMissingFiles);

        //MessageFileDownload msg = new MessageFileDownload();
        for(int i=0; i<missingFiles.size(); i++) {
            errorCode = downloadFile(opData.ctxt, missingFiles.get(i), opData.argBool1);
            //remove from missing files list
            if(errorCode==ErrorCodes.NO_ERROR) {
                //LogMy.d(TAG,"File found remotely, removing from missing list: "+missingFiles.get(i));
                mRetainedFragment.mMissingFiles.remove(missingFiles.get(i));
            }
        }
        return errorCode;
    }

    private int downloadFile(Context ctxt, String filepath, boolean isFullUrl) {
        try {
            //String filepath = msg.fileUrl;
            String fileURL = null;
            if(!isFullUrl) {
                fileURL = AppConstants.BACKEND_FILE_BASE_URL + filepath;
            } else {
                fileURL = filepath;
            }
            //String filename = filepath.substring(filepath.lastIndexOf('/')+1);
            String filename = Uri.parse(fileURL).getLastPathSegment();

            LogMy.d(TAG,"Fetching "+fileURL+", Filename: "+filename);
            byte[] bytes = new FileFetchr().getUrlBytes(fileURL, mRetainedFragment.mUserToken);
            LogMy.d(TAG,"File downloaded successfully: "+filename);

            FileOutputStream outputStream;
            outputStream = ctxt.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.close();

        } catch(FileNotFoundException fnf) {
            LogMy.d(TAG, "File not found: "+fnf.toString());
            return ErrorCodes.FILE_NOT_FOUND;
        } catch(Exception e) {
            LogMy.e(TAG, "Failed to fetch file: "+e.toString());
            return ErrorCodes.GENERAL_ERROR;
        }
        return ErrorCodes.NO_ERROR;
    }

    private void isSessionValid() {
        try {
            CommonServices.getInstance().isSessionValid();
            LogMy.d(TAG,"Session is valid");
        } catch (BackendlessException e) {
            LogMy.e(TAG, "Session not valid: "+ e.toString());
            throw e;
        }
    }
}
