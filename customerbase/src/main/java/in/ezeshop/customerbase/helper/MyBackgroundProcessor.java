package in.ezeshop.customerbase.helper;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.backendless.Backendless;
import com.backendless.HeadersManager;
import com.backendless.exceptions.BackendlessException;
import com.backendless.files.BackendlessFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.ezeshop.appbase.backendAPI.CommonServices;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.entities.MyAreas;
import in.ezeshop.appbase.entities.MyCashback;
import in.ezeshop.appbase.entities.MyTransaction;
import in.ezeshop.appbase.utilities.FileFetchr;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.database.Cashback;
import in.ezeshop.common.database.CustomerOrder;
import in.ezeshop.common.database.Merchants;
import in.ezeshop.common.database.Transaction;
import in.ezeshop.customerbase.backendAPI.CustomerServices;
import in.ezeshop.customerbase.entities.CustomerUser;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.BackgroundProcessor;
import in.ezeshop.appbase.utilities.LogMy;

/**
 * Created by adgangwa on 17-07-2016.
 */
public class MyBackgroundProcessor <T> extends BackgroundProcessor<T> {
    private final static String TAG = "CustApp-MyBackgroundProcessor";

    private MyRetainedFragment mRetainedFragment;

    public MyBackgroundProcessor(Handler responseHandler, MyRetainedFragment retainedFragment) {
        super(responseHandler);
        mRetainedFragment = retainedFragment;
    }

    private class MessageLogin implements Serializable {
        public String userId;
        public String password;
    }
    private class MessageChangePassword implements Serializable {
        public String oldPasswd;
        public String newPasswd;
    }
    private class MessageChangePin implements Serializable {
        public String oldPin;
        public String newPin;
        public String secret;
    }
    private class MessageGetCb implements Serializable {
        public Context ctxt;
        public Long updatedSince;
    }
    private class MessageFileDownload implements Serializable {
        public Context ctxt;
        public String fileUrl;
    }

    /*
     * Add request methods
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

    /*public void addAutoLoginReq() {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_AUTO_LOGIN, null).sendToTarget();
    }
    public void addLoginRequest(String userId, String password) {
        MessageLogin msg = new MessageLogin();
        msg.userId = userId;
        msg.password = password;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_LOGIN, msg).sendToTarget();
    }
    public void addLogoutRequest() {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_LOGOUT, null).sendToTarget();
    }
    public void addPasswordRequest(String loginId, String secret1) {
        LogMy.d(TAG, "In addPasswordRequest");
        MessageLogin msg = new MessageLogin();
        msg.userId = loginId;
        msg.password = secret1;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_GENERATE_PWD, msg).sendToTarget();
    }
    public void addPasswdChangeReq(String oldPasswd, String newPasswd) {
        MessageChangePassword msg = new MessageChangePassword();
        msg.oldPasswd = oldPasswd;
        msg.newPasswd = newPasswd;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_CHANGE_PASSWD,msg).sendToTarget();
    }
    public void addChangeMobileRequest() {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_CHANGE_MOBILE,null).sendToTarget();
    }
    public void addFetchCbRequest(Long updatedSince, Context ctxt) {
        MessageGetCb msg = new MessageGetCb();
        msg.ctxt = ctxt;
        msg.updatedSince = updatedSince;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_FETCH_CB,msg).sendToTarget();
    }
    public void addPinChangeRequest(String oldPin, String newPin, String secret) {
        MessageChangePin msg = new MessageChangePin();
        msg.oldPin = oldPin;
        msg.newPin = newPin;
        msg.secret = secret;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_CHANGE_PIN,msg).sendToTarget();
    }
    public void addFetchTxnsRequest(String query) {
        LogMy.d(TAG, "In addFetchTxnsRequest");
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_FETCH_TXNS, query).sendToTarget();
    }
    public void addFetchTxnFilesRequest(Context context) {
        LogMy.d(TAG, "In addFetchTxnFilesRequest");
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_FETCH_TXN_FILES, context).sendToTarget();
    }
    public void addEnableRequest(String userId, String password) {
        LogMy.d(TAG, "In addEnableRequest");
        MessageLogin msg = new MessageLogin();
        msg.userId = userId;
        msg.password = password;
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_ENABLE_ACC, msg).sendToTarget();
    }
    public void addCustomerOpsReq() {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_FETCH_CUSTOMER_OPS, null).sendToTarget();
    }
    public void addCheckMsgDevRegReq() {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_MSG_DEV_REG_CHK, null).sendToTarget();
    }
    public void saveCustAddrReq(Boolean setAsDefault) {
        mRequestHandler.obtainMessage(MyRetainedFragment.REQUEST_SAVE_CUST_ADDR, setAsDefault).sendToTarget();
    }*/


    @Override
    protected int handleMsg(Message msg) {
        int error = ErrorCodes.NO_ERROR;

        // It checks with internet site - so checking only during login
        /*if( msg.what==MyRetainedFragment.REQUEST_LOGIN &&
                !AppCommonUtil.isInternetConnected()) {
            return ErrorCodes.NO_INTERNET_CONNECTION;
        }*/

        MessageBgJob data = (MessageBgJob)msg.obj;
        switch(msg.what) {
            case MyRetainedFragment.REQUEST_LOGIN:
                error = loginCustomer(data);
                break;
            case MyRetainedFragment.REQUEST_LOGOUT:
                error = logout();
                break;
            case MyRetainedFragment.REQUEST_GENERATE_PWD:
                error = generatePassword(data);
                break;
            case MyRetainedFragment.REQUEST_CHANGE_PASSWD:
                error = changePassword(data);
                break;
            case MyRetainedFragment.REQUEST_CHANGE_MOBILE:
                error = changeMobileNum();
                break;
            case MyRetainedFragment.REQUEST_FETCH_CB:
                error = fetchCashbacks(data);
                break;
            case MyRetainedFragment.REQUEST_CHANGE_PIN:
                error = changePin(data);
                break;
            case MyRetainedFragment.REQUEST_FETCH_TXNS:
                error = fetchTransactions(data.argStr1);
                break;
            case MyRetainedFragment.REQUEST_FETCH_TXN_FILES:
                error = fetchTxnFiles(data.ctxt);
                break;
            case MyRetainedFragment.REQUEST_ENABLE_ACC:
                error = enableAccount(data);
                break;
            case MyRetainedFragment.REQUEST_FETCH_CUSTOMER_OPS:
                error = fetchCustomerOps();
                break;
            case MyRetainedFragment.REQUEST_AUTO_LOGIN:
                error = tryAutoLogin();
                break;
            case MyRetainedFragment.REQUEST_MSG_DEV_REG_CHK:
                error = chkMsgDevReg();
                break;
            case MyRetainedFragment.REQUEST_SAVE_CUST_ADDR:
                error = saveCustAddress(data.argBool1);
                break;
            case MyRetainedFragment.REQUEST_FETCH_AREAS:
                error = MyAreas.fetchAreas(data.argStr1);
                break;
            case MyRetainedFragment.REQUEST_FETCH_MCHNT_BY_AREA:
                error = fetchMchntsForDelivery(data);
                break;
            case MyRetainedFragment.REQUEST_CREATE_ORDER:
                error = createCustomerOrder(data);
                break;
        }
        return error;
    }

    private int createCustomerOrder(MessageBgJob opData) {
        LogMy.d(TAG, "In createCustomerOrder");

        if(CustomerUser.getInstance().isPseudoLoggedIn()) {
            return ErrorCodes.OPERATION_NOT_ALLOWED;
        }

        try {
            // First upload all prescriptions
            List<String> urls = null;
            if(mRetainedFragment.mPrescripImgs.size() > 0) {
                urls = new ArrayList<>(mRetainedFragment.mPrescripImgs.size());
                String remoteDir = CommonUtils.getCustPrescripDir(CustomerUser.getInstance().getCustomer().getPrivate_id());
                for (File file :
                        mRetainedFragment.mPrescripImgs) {
                    String url = uploadFileSync(file, remoteDir);
                    if(url==null) {
                        return ErrorCodes.FILE_UPLOAD_FAILED;
                    } else {
                        urls.add(url);
                    }
                }
            }
            // Create order now
            mRetainedFragment.mCustOrder = CustomerServices.getInstance().createCustomerOrder(
                    mRetainedFragment.mCustOrder.getMerchantId(),
                    mRetainedFragment.mCustOrder.getAddressId(),
                    mRetainedFragment.mCustOrder.getCustComments(),
                    urls);

        } catch (BackendlessException e) {
            LogMy.e(TAG,"createCustomerOrder failed: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    private int saveCustAddress(Boolean setAsDefault) {
        LogMy.d(TAG, "In saveCustAddress");
        return CustomerUser.getInstance().saveCustAddress(mRetainedFragment.mCustAddrToSave, setAsDefault);
    }

    private int chkMsgDevReg() {
        LogMy.d(TAG, "In chkMsgDevReg");
        return CustomerUser.getInstance().checkMsgDevReg();
    }

    private int tryAutoLogin() {
        LogMy.d(TAG, "In tryAutoLogin");
        return CustomerUser.tryAutoLogin();
    }

    private int loginCustomer(MessageBgJob opData) {
        LogMy.d(TAG, "In loginCustomer");
        return CustomerUser.login(opData.argStr1, opData.argStr2);
    }

    private int logout() {
        return CustomerUser.logout();
    }

    private int generatePassword(MessageBgJob opData) {
        return CustomerUser.resetPassword(opData.argStr1, opData.argStr2);
    }

    private int changePassword(MessageBgJob opData) {
        return CustomerUser.getInstance().changePassword(opData.argStr1, opData.argStr2);
    }

    private int changeMobileNum() {
        return CustomerUser.getInstance().changeMobileNum(mRetainedFragment.mPinMobileChange,
                mRetainedFragment.mNewMobileNum, mRetainedFragment.mOtpMobileChange);
    }

    private int fetchMchntsForDelivery(MessageBgJob opData) {
        LogMy.d(TAG,"In fetchMchntsForDelivery: "+opData.argStr1);
        try {
            List<Merchants> mchnts = CustomerServices.getInstance().mchntsByDeliveryArea(opData.argStr1);

            if (mRetainedFragment.mAreaToMerchants == null) {
                mRetainedFragment.mAreaToMerchants = new HashMap<>();
            } else {
                mRetainedFragment.mAreaToMerchants.clear();
            }
            mRetainedFragment.mAreaToMerchants.put(opData.argStr1, mchnts);

            if(mchnts.size() > 0) {
                // fetch mchnt DPs for newly fetched merchants - if not already in memory
                // ignore any error
                try {
                    List<String> dpFileNames = new ArrayList<>(mchnts.size());
                    for (Merchants m : mchnts) {
                        dpFileNames.add(m.getDisplayImage());
                    }
                    fetchMchntDpFiles(opData.ctxt, dpFileNames);
                } catch(Exception ex) {
                    LogMy.e(TAG,"fetchMchntsForDelivery: Exception from fetchMchntDpFiles",ex);
                }
            }
        } catch (BackendlessException e) {
            //mRetainedFragment.mLastFetchCashbacks = null;
            LogMy.e(TAG, "Exception in fetchCashbacks: "+ e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    private int fetchCashbacks(MessageBgJob opData) {
        //mRetainedFragment.mLastFetchCashbacks = null;
        try {
            List<Cashback> cashbacks = CustomerUser.getInstance().fetchCashbacks(opData.argLong1);

            if(cashbacks.size() > 0) {
                //mRetainedFragment.mLastFetchCashbacks = new ArrayList<>(cashbacks.size());
                if(mRetainedFragment.mCashbacks==null) {
                    mRetainedFragment.mCashbacks = new HashMap<>(cashbacks.size());
                } else {
                    // clear old data - only when new data is received
                    mRetainedFragment.mCashbacks.clear();
                }

                List<String> dpFileNames = new ArrayList<>(cashbacks.size());
                for (Cashback cb : cashbacks) {
                    MyCashback myCb = new MyCashback(cb, false);
                    mRetainedFragment.mCashbacks.put(myCb.getMerchantId(), myCb);
                    dpFileNames.add(myCb.getMerchant().getDpFilename());
                    //myCb.init(cb, false);
                    //mRetainedFragment.mLastFetchCashbacks.add(myCb);
                }

                // fetch mchnt DPs for newly fetched cashbacks - if not already in memory
                // ignore any error
                try {
                    fetchMchntDpFiles(opData.ctxt,dpFileNames);
                } catch(Exception ex) {
                    LogMy.e(TAG,"Exception from fetchMchntDpFiles",ex);
                }
            }
        } catch (BackendlessException e) {
            //mRetainedFragment.mLastFetchCashbacks = null;
            LogMy.e(TAG, "Exception in fetchCashbacks: "+ e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    private int fetchMchntDpFiles(Context ctxt, List<String> fileNames) {
        LogMy.d(TAG,"In fetchMchntDpFiles");
        int errorCode = ErrorCodes.NO_ERROR;

        // check which files need to be fetched
        // i.e. not present locally
        List<String> missingFiles = null;
        for (String fileName :
                fileNames) {
            //String dpFilename = myCb.getMerchant().getDpFilename();

            File file = ctxt.getFileStreamPath(fileName);
            if(file == null || !file.exists()) {
                if(missingFiles==null) {
                    missingFiles = new ArrayList<>(mRetainedFragment.mCashbacks.size());
                }
                // file does not exist
                LogMy.d(TAG,"Missing mchnt dp file: "+fileName);
                String filepath = CommonConstants.MERCHANT_DISPLAY_IMAGES_DIR + fileName;
                LogMy.d(TAG,"Missing mchnt dp filepath: "+filepath);
                missingFiles.add(filepath);
            }
        }

        if(missingFiles!=null) {
            //MessageFileDownload msg = new MessageFileDownload();
            for(int i=0; i<missingFiles.size(); i++) {
                //msg.ctxt = ctxt;
                //msg.fileUrl = missingFiles.get(i);
                errorCode = downloadFile(ctxt, missingFiles.get(i));
                //remove from missing files list
                if(errorCode==ErrorCodes.NO_ERROR) {
                    LogMy.d(TAG,"Downloaded mchnt dp file: "+missingFiles.get(i));
                }
            }
        }

        return errorCode;
    }

    private int changePin(MessageBgJob opData) {
        return CustomerUser.getInstance().changePin(opData.argStr1, opData.argStr2, opData.argStr3);
    }

    private int fetchTransactions(String query) {
        mRetainedFragment.mLastFetchTransactions = null;
        int errorCode = ErrorCodes.NO_ERROR;

        try {
            isSessionValid();

            List<Transaction> txns= null;
            String[] csvFields = CustomerUser.getInstance().getCustomer().getTxn_tables().split(CommonConstants.CSV_DELIMETER);

            // fetch cashback records from each table
            for(int i=0; i<csvFields.length; i++) {
                // fetch txns for this customer in this table
                List<Transaction> data = MyTransaction.fetch(query, csvFields[i]);
                if (data != null) {
                    if(txns==null) {
                        txns= new ArrayList<>();
                    }
                    // add all fetched records from this table to final set
                    txns.addAll(data);
                }
            }

            /*List<Transaction> txns = CustomerUser.getInstance().fetchTxns(query);*/
            if(txns!=null && txns.size() > 0) {
                mRetainedFragment.mLastFetchTransactions = txns;
            } else {
                errorCode = ErrorCodes.NO_DATA_FOUND;
            }
        } catch (BackendlessException e) {
            LogMy.e(TAG, "Exception in fetchTransactions: "+ e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }

        return errorCode;
    }

    private int fetchTxnFiles(Context ctxt) {
        int errorCode = ErrorCodes.NO_ERROR;

        try {
            isSessionValid();
        } catch (BackendlessException e) {
            return AppCommonUtil.getLocalErrorCode(e);
        }

        // create a copy of list
        List<String> missingFiles = new ArrayList<>(mRetainedFragment.mMissingFiles);

        //MessageFileDownload msg = new MessageFileDownload();
        for(int i=0; i<missingFiles.size(); i++) {
            // convert filepath to complete URL
            //https://api.backendless.com/09667f8b-98a7-e6b9-ffeb-b2b6ee831a00/v1/files/<filepath>
            //msg.ctxt = ctxt;
            //msg.fileUrl = missingFiles.get(i);
            errorCode = downloadFile(ctxt, missingFiles.get(i));
            //remove from missing files list
            if(errorCode==ErrorCodes.NO_ERROR) {
                LogMy.d(TAG,"Txn file found remotely, removing from missing list: "+missingFiles.get(i));
                mRetainedFragment.mMissingFiles.remove(missingFiles.get(i));
            }
        }

        return errorCode;
    }

    private int downloadFile(Context ctxt, String filepath) {
        //String filepath = msg.fileUrl;
        String fileURL = AppConstants.BACKEND_FILE_BASE_URL + filepath;
        //String filename = filepath.substring(filepath.lastIndexOf('/')+1);
        String filename = Uri.parse(fileURL).getLastPathSegment();
        LogMy.d(TAG,"Fetching "+fileURL+", Filename: "+filename);

        FileOutputStream outputStream;
        try {
            byte[] bytes = new FileFetchr().getUrlBytes(fileURL, mRetainedFragment.mUserToken);

            //outputStream = msg.ctxt.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream = ctxt.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.close();

        } catch(FileNotFoundException fnf) {
            LogMy.d(TAG, "File not found: "+fnf.toString());
            return ErrorCodes.FILE_NOT_FOUND;
        } catch(IOException ioe) {
            LogMy.e(TAG, "Failed to fetch file: "+ioe.toString());
            return ErrorCodes.GENERAL_ERROR;
        }
        return ErrorCodes.NO_ERROR;
    }

    private int enableAccount(MessageBgJob opData) {
        LogMy.d(TAG, "In enableAccount");
        return CustomerUser.enableAccount(opData.argStr1, opData.argStr2,
                mRetainedFragment.mAccEnableOtp, mRetainedFragment.mAccEnablePin);
    }

    private int fetchCustomerOps() {
        mRetainedFragment.mLastFetchCustOps = null;

        try {
            mRetainedFragment.mLastFetchCustOps = CustomerUser.getInstance().fetchCustomerOps();
            LogMy.d(TAG,"fetchCustomerOps success: "+mRetainedFragment.mLastFetchCustOps.size());

        } catch (BackendlessException e) {
            LogMy.e(TAG,"Exception in fetchCustomerOps: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
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

    private String uploadFileSync(File imgFile, String remoteDir) {
        // upload file
        try {
            for( String key : HeadersManager.getInstance().getHeaders().keySet() ) {
                LogMy.d(TAG, "In uploadFileSync: " + key + "," + HeadersManager.getInstance().getHeaders().get(key));
            }

            BackendlessFile file = Backendless.Files.upload(imgFile, remoteDir, true);
            LogMy.d(TAG, "File uploaded successfully at :" + file.getFileURL());
            return file.getFileURL();

        } catch(Exception e) {
            LogMy.e(TAG, "File upload failed: " + e.toString());
        }
        return null;
    }

}
