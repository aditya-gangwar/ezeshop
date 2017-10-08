package in.ezeshop.customerbase.helper;

import android.content.Context;
import android.os.Handler;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import in.ezeshop.appbase.entities.MyCashback;
import in.ezeshop.appbase.utilities.TxnReportsHelper2;
import in.ezeshop.common.database.CustomerOps;
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

    // Requests that this fragment executes in backend
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

    // Threads taken care by this fragment
    private MyBackgroundProcessor<String> mBackgroundProcessor;

    public CustomerUser mCustomerUser;
    public String mUserToken;

    // Cashback Data
    public List<MyCashback> mLastFetchCashbacks;
    public Map<String, MyCashback> mCashbacks;
    //public Date mCbsUpdateTime;

    // stats for the customer
    public CustomerStats stats;
    public List<CustomerOps> mLastFetchCustOps;

    // params for mobile number change operation
    public String mPinMobileChange;
    public String mNewMobileNum;
    public String mOtpMobileChange;
    //public String mCardMobileChange;

    // members used by 'Txn Reports Activity' to store its state, and its fragments
    public List<String> mMissingFiles;
    // 'Txn Reports Activity' store the helper instance here in onSaveInstance
    public TxnReportsHelper2 mTxnReportHelper;
    public List<Transaction> mLastFetchTransactions;

    // params for enabling account
    //public String mAccEnableCardNum;
    public String mAccEnablePin;
    public String mAccEnableOtp;

    // customer order related parameters
    public List<File> mPrescripImgs;
    public String mSelectedAddrId;

    public void reset() {
        LogMy.d(TAG,"In reset");
        mPinMobileChange = null;
        mNewMobileNum = null;
        mOtpMobileChange = null;
        mLastFetchCashbacks = null;
        mPrescripImgs = null;
        mSelectedAddrId = null;
    }

    /*
     * Methods to add request for processing by background thread
     */
    public void tryAutoLogin() {
        mBackgroundProcessor.addAutoLoginReq();
    }
    public void loginCustomer(String loginId, String password) {
        mBackgroundProcessor.addLoginRequest(loginId, password);
    }
    public void logoutCustomer() {
        mBackgroundProcessor.addLogoutRequest();
    }
    public void generatePassword(String loginId, String secret1) {
        mBackgroundProcessor.addPasswordRequest(loginId, secret1);
    }
    public void changePassword(String oldPasswd, String newPasswd) {
        mBackgroundProcessor.addPasswdChangeReq(oldPasswd, newPasswd);
    }
    public void changeMobileNum() {
        mBackgroundProcessor.addChangeMobileRequest();
    }
    public void fetchCashback(Long updatedSince, Context ctxt) {
        mBackgroundProcessor.addFetchCbRequest(updatedSince, ctxt);
    }
    public void changePin(String oldPin, String newPin, String secret) {
        mBackgroundProcessor.addPinChangeRequest(oldPin, newPin, secret);
    }
    public void fetchTransactions(String whereClause) {
        mBackgroundProcessor.addFetchTxnsRequest(whereClause);
    }
    public void fetchTxnFiles(Context context) {
        mBackgroundProcessor.addFetchTxnFilesRequest(context);
    }
    public void enableAccount(String loginId, String passwd) {
        mBackgroundProcessor.addEnableRequest(loginId, passwd);
    }
    public void fetchCustomerOps() {
        mBackgroundProcessor.addCustomerOpsReq();
    }

    public void checkMsgDevReg() {
        mBackgroundProcessor.addCheckMsgDevRegReq();
    }

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

