package in.ezeshop.customerbase.helper;

import android.content.Context;
import android.os.Handler;

import java.io.File;
import java.util.List;
import java.util.Map;

import in.ezeshop.appbase.entities.MyCashback;
import in.ezeshop.appbase.utilities.TxnReportsHelper2;
import in.ezeshop.common.database.CustAddress;
import in.ezeshop.common.database.CustomerOps;
import in.ezeshop.common.database.CustomerOrder;
import in.ezeshop.common.database.Merchants;
import in.ezeshop.common.database.Transaction;
import in.ezeshop.customerbase.entities.CustomerStats;
import in.ezeshop.customerbase.entities.CustomerUser;
import in.ezeshop.appbase.utilities.BackgroundProcessor;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.RetainedFragment;

/**
 * Created by adgangwa on 17-07-2016.
 */
public class MyRetainedFragment extends RetainedFragment {
    private static final String TAG = "CustApp-MyRetainedFragment";

    // Requests that this fragment can execute in background
    public static final int REQUEST_LOGIN = 0;
    public static final int REQUEST_LOGOUT = 1;
    public static final int REQUEST_GENERATE_PWD = 2;
    public static final int REQUEST_CHANGE_PASSWD = 3;
    public static final int REQUEST_CHANGE_MOBILE = 4;
    public static final int REQUEST_FETCH_CB = 5;
    public static final int REQUEST_CHANGE_PIN = 6;
    public static final int REQUEST_FETCH_TXNS = 7;
    public static final int REQUEST_FETCH_TXN_FILES = 8;
    public static final int REQUEST_ENABLE_ACC = 9;
    public static final int REQUEST_FETCH_CUSTOMER_OPS = 10;
    public static final int REQUEST_AUTO_LOGIN = 11;
    public static final int REQUEST_MSG_DEV_REG_CHK = 12;
    public static final int REQUEST_SAVE_CUST_ADDR = 13;
    public static final int REQUEST_FETCH_AREAS = 14;
    public static final int REQUEST_FETCH_MCHNT_BY_AREA = 15;
    public static final int REQUEST_CREATE_ORDER = 16;

    // Threads taken care by this fragment
    private MyBackgroundProcessor<String> mBackgroundProcessor;

    public CustomerUser mCustomerUser;
    public String mUserToken;

    // In-memory stores
    // MerchantID -> MyCashback object - collective - as fetched from DB
    public Map<String, MyCashback> mCashbacks;
    // Area ID -> Merchant object (delivering in this area)
    public Map<String,List<Merchants>> mAreaToMerchants;
    // Temporary store to pass MyCashback object across fragment in same activity (i.e. one owning this retainedFragment object)
    public MyCashback mSelectCashback;

    // stats for the customer
    public CustomerStats stats;
    public List<CustomerOps> mLastFetchCustOps;

    // params for mobile number change operation
    public String mPinMobileChange;
    public String mNewMobileNum;
    public String mOtpMobileChange;

    // members used by 'Txn Reports Activity' to store its state, and its fragments
    public List<String> mMissingFiles;
    // 'Txn Reports Activity' store the helper instance here in onSaveInstance
    public TxnReportsHelper2 mTxnReportHelper;
    public List<Transaction> mLastFetchTransactions;

    // params for enabling account
    public String mAccEnablePin;
    public String mAccEnableOtp;

    // customer order/address related parameters
    public List<File> mPrescripImgs;
    public CustomerOrder mCustOrder;
    public Transaction mCurrTxn;
    // address selected for delivery or for add/update
    public CustAddress mSelectedAddress;
    //public String mSelectedAddrId;
    //public String mSelectedMchntId;
    //public String mOrderComments;

    public void reset() {
        LogMy.d(TAG,"In reset");
        mPinMobileChange = null;
        mNewMobileNum = null;
        mOtpMobileChange = null;
        //mLastFetchCashbacks = null;
        mPrescripImgs = null;
        //mSelectedAddrId = null;
        mSelectedAddress = null;
        //mSelectedMchntId = null;
        //mOrderComments=null;
        mCustOrder = null;
        mSelectCashback= null;
    }

    /*
     * Method to add request for processing by background thread
     */
    public void addBackgroundJob(int requestCode, Context ctxt, String callingFragTag,
                                String argStr1, String argStr2, String argStr3, Long argLong1, Boolean argBool1) {
        // transparently pass to background thread
        mBackgroundProcessor.addBackgroundJob(requestCode, ctxt, callingFragTag, argStr1, argStr2, argStr3, argLong1, argBool1);
    }

    /*public void tryAutoLogin() {
        //mBackgroundProcessor.addAutoLoginReq();
        mBackgroundProcessor.addBackgroundJob(REQUEST_AUTO_LOGIN, null, null, null, null, null, null,null);
    }
    public void loginCustomer(String loginId, String password) {
//        mBackgroundProcessor.addLoginRequest(loginId, password);
        mBackgroundProcessor.addBackgroundJob(REQUEST_LOGIN, null, null, loginId, password, null, null,null);
    }
    public void logoutCustomer() {
        //mBackgroundProcessor.addLogoutRequest();
        mBackgroundProcessor.addBackgroundJob(REQUEST_LOGOUT, null, null, null, null, null, null, null);
    }
    public void generatePassword(String loginId, String secret1) {
        //mBackgroundProcessor.addPasswordRequest(loginId, secret1);
        mBackgroundProcessor.addBackgroundJob(REQUEST_GENERATE_PWD, null, null, loginId, secret1, null, null, null);
    }
    public void changePassword(String oldPasswd, String newPasswd) {
        //mBackgroundProcessor.addPasswdChangeReq(oldPasswd, newPasswd);
        mBackgroundProcessor.addBackgroundJob(REQUEST_CHANGE_PASSWD, null, null, oldPasswd, newPasswd, null, null, null);
    }
    public void changeMobileNum() {
        //mBackgroundProcessor.addChangeMobileRequest();
        mBackgroundProcessor.addBackgroundJob(REQUEST_CHANGE_MOBILE, null, null, null, null, null, null, null);
    }
    public void fetchCashback(Long updatedSince, Context ctxt) {
        //mBackgroundProcessor.addFetchCbRequest(updatedSince, ctxt);
        mBackgroundProcessor.addBackgroundJob(REQUEST_FETCH_CB, ctxt, null, null, null, null, updatedSince, null);
    }
    public void changePin(String oldPin, String newPin, String secret) {
        //mBackgroundProcessor.addPinChangeRequest(oldPin, newPin, secret);
        mBackgroundProcessor.addBackgroundJob(REQUEST_CHANGE_PIN, null, null, oldPin, newPin, secret, null, null);
    }
    public void fetchTransactions(String whereClause) {
        //mBackgroundProcessor.addFetchTxnsRequest(whereClause);
        mBackgroundProcessor.addBackgroundJob(REQUEST_FETCH_TXNS, null, null, whereClause, null, null, null, null);
    }
    public void fetchTxnFiles(Context context) {
        //mBackgroundProcessor.addFetchTxnFilesRequest(context);
        mBackgroundProcessor.addBackgroundJob(REQUEST_FETCH_TXN_FILES, context, null, null, null, null, null, null);
    }
    public void enableAccount(String loginId, String passwd) {
        //mBackgroundProcessor.addEnableRequest(loginId, passwd);
        mBackgroundProcessor.addBackgroundJob(REQUEST_ENABLE_ACC, null, null, loginId, passwd, null, null, null);
    }
    public void fetchCustomerOps() {
        //mBackgroundProcessor.addCustomerOpsReq();
        mBackgroundProcessor.addBackgroundJob(REQUEST_FETCH_CUSTOMER_OPS, null, null, null, null, null, null, null);
    }

    public void checkMsgDevReg() {
        //mBackgroundProcessor.addCheckMsgDevRegReq();
        mBackgroundProcessor.addBackgroundJob(REQUEST_MSG_DEV_REG_CHK, null, null, null, null, null, null, null);
    }

    public void saveCustAddress(Boolean setAsDefault) {
        //mBackgroundProcessor.saveCustAddrReq(setAsDefault);
        mBackgroundProcessor.addBackgroundJob(REQUEST_SAVE_CUST_ADDR, null, null, null, null, null, null, setAsDefault);
    }*/

    @Override
    protected void doOnActivityCreated() {
        mCustomerUser = CustomerUser.getInstance();
    }

    @Override
    protected BackgroundProcessor<String> getBackgroundProcessor() {
        if(mBackgroundProcessor == null) {
            LogMy.d(TAG,"Creating background thread.");
            Handler responseHandler = new Handler();
            mBackgroundProcessor = new MyBackgroundProcessor<>(responseHandler, MyRetainedFragment.this);
        }
        return mBackgroundProcessor;
    }

    @Override
    protected void doOnDestroy() {
        // nothing to do
    }
}

