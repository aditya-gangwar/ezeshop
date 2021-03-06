package in.ezeshop.customerbase;

/*
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.ezeshop.appbase.OtpPinInputDialog;
import in.ezeshop.appbase.SingleWebViewActivity;
import in.ezeshop.appbase.entities.MyCashback;
import in.ezeshop.common.MyGlobalSettings;
import in.ezeshop.common.CsvConverter;
import in.ezeshop.customerbase.entities.CustomerStats;
import in.ezeshop.customerbase.entities.CustomerUser;
import in.ezeshop.customerbase.helper.MyRetainedFragment;
import in.ezeshop.appbase.PasswdChangeDialog;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Customers;
import in.ezeshop.appbase.utilities.AppAlarms;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.common.DateUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;


public class CashbackActivityCust extends AppCompatActivity implements
        MyRetainedFragment.RetainedFragmentIf, DialogFragmentWrapper.DialogFragmentWrapperIf,
        PasswdChangeDialog.PasswdChangeDialogIf, MobileChangeDialog.MobileChangeDialogIf,
        OtpPinInputDialog.OtpPinInputDialogIf, CashbackListFragment.CashbackListFragmentIf,
        PinResetDialog.PinResetDialogIf, PinChangeDialog.PinChangeDialogIf,
        MchntDetailsDialogCustApp.MerchantDetailsDialogIf, CustomerOpListFrag.CustomerOpListFragIf {

    private static final String TAG = "CustApp-CashbackActivity";
    public static final String INTENT_EXTRA_USER_TOKEN = "extraUserToken";

    private static final int RC_TXN_REPORT = 9005;

    private static final String RETAINED_FRAGMENT = "retainedFrag";
    private static final String CASHBACK_LIST_FRAGMENT = "CashbackListFrag";
    private static final String ERROR_FRAGMENT = "ErrorFrag";

    private static final String DIALOG_BACK_BUTTON = "dialogBackButton";
    private static final String DIALOG_CHANGE_PASSWORD = "dialogChangePasswd";
    private static final String DIALOG_CHANGE_MOBILE = "dialogChangeMobile";
    private static final String DIALOG_CHANGE_MOBILE_PIN = "dialogChangeMobilePin";
    private static final String DIALOG_PIN_RESET = "dialogPinReset";
    private static final String DIALOG_PIN_CHANGE = "dialogPinChange";
    private static final String DIALOG_CUSTOMER_DETAILS = "dialogCustomerDetails";
    private static final String CUSTOMER_OPS_LIST_FRAG = "CustomerOpsListFrag";
    private static final String DIALOG_NOTIFY_CB_FETCH_ERROR = "dialogCbFetchError";
    private static final String DIALOG_SESSION_TIMEOUT = "dialogSessionTimeout";


    MyRetainedFragment mRetainedFragment;
    FragmentManager mFragMgr;
    // this will never be null, as it only gets destroyed with cashback activity itself
    CashbackListFragment mMchntListFragment;

    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    ActionBarDrawerToggle mDrawerToggle;
    NavigationView mNavigationView;

    private AppCompatImageView mTbImage;
    private EditText mTbTitle;
    private EditText mTbTitle2;
    //private LinearLayout mTbLayoutSubhead1;
    private EditText mTbSubhead1Text1;
    //private EditText mTbSubhead1Text2;
    //private View mTbImgAcc;

    private CustomerUser mCustomerUser;
    private Customers mCustomer;

    // Activity state members: These are to be saved for restore in event of activity recreation
    boolean mExitAfterLogout;
    int mLastMenuItemId;
    long mGetCbSince;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cust_cashback);

        mCustomerUser = CustomerUser.getInstance();
        mCustomer = mCustomerUser.getCustomer();
        mFragMgr = getFragmentManager();

        // Initialize retained fragment before other fragments
        // Check to see if we have retained the worker fragment.
        mRetainedFragment = (MyRetainedFragment)mFragMgr.findFragmentByTag(RETAINED_FRAGMENT);
        // If not retained (or first time running), we need to create it.
        if (mRetainedFragment == null) {
            mRetainedFragment = new MyRetainedFragment();
            // Tell it who it is working with.
            mFragMgr.beginTransaction().add(mRetainedFragment, RETAINED_FRAGMENT).commit();
        }

        // store passed logged in user token
        mRetainedFragment.mUserToken = getIntent().getStringExtra(INTENT_EXTRA_USER_TOKEN);

        if(savedInstanceState!=null) {
            mExitAfterLogout = savedInstanceState.getBoolean("mExitAfterLogout");
            mLastMenuItemId = savedInstanceState.getInt("mLastMenuItemId");
            mGetCbSince = savedInstanceState.getLong("mGetCbSince");
        }

        // reference to views
        mTbSubhead1Text1 = (EditText) findViewById(R.id.tb_curr_account) ;
        //mTbSubhead1Text2 = (EditText) findViewById(R.id.tb_curr_cashback) ;
        //mTbImgAcc = findViewById(R.id.tb_acc_img) ;

        // Setup a toolbar to replace the action bar.
        initToolbar();
        updateTbForCustomer();

        // Setup navigation drawer
        initNavDrawer();
    }

    @Override
    public void onBgThreadCreated() {
        // read file, if 'cashback store' is empty
        LogMy.d(TAG,"In onBgThreadCreated");
        try {
            boolean fetchData = false;
            if (mRetainedFragment.mCashbacks == null) {
                if (!processCbDataFile(false)) {
                    fetchData = true;
                }
            } else if (custStatsRefreshReq(mRetainedFragment.mCbsUpdateTime.getTime())) {
                LogMy.d(TAG,"Data available, but expired");
                // data in memory, but has expired
                fetchData = true;
                mGetCbSince = mRetainedFragment.mCbsUpdateTime.getTime();
            }
            if (fetchData) {
                // fetch data from DB
                fetchCbData();
            } else {
                startCashbackListFrag();
            }
        } catch (Exception e) {
            AppCommonUtil.cancelProgressDialog(true);
            LogMy.e(TAG, "Exception in CashbackActivityCust:onBgThreadCreated", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), false, true)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
            if(mRetainedFragment.mCashbacks == null) {
                // No data to show
                startErrorFrag(false, null, null);
            }
        }
    }

    //@Override
    private void setDrawerState(boolean isEnabled) {
        LogMy.d(TAG, "In setDrawerState: " + isEnabled);

        if ( isEnabled ) {
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerToggle.syncState();
        }
        else {
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            // show back arrow
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

            mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mDrawerToggle.isDrawerIndicatorEnabled()) {
                        onBackPressed();
                    }
                }
            });
            mDrawerToggle.syncState();
        }
    }

    private void initNavDrawer() {
        LogMy.d(TAG, "In initNavDrawer");

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //NavigationView navigationView;
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        View headerLayout = mNavigationView.getHeaderView(0);
        TextView headerTitle = (TextView)headerLayout.findViewById(R.id.drawer_header_title);
        headerTitle.setText(mCustomer.getName());
        TextView headerMobile = (TextView)headerLayout.findViewById(R.id.drawer_header_mobile);
        headerMobile.setText(mCustomer.getMobile_num());

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.drawer_open,  R.string.drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);

        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });

        setDrawerState(true);
    }

    private void initToolbar() {
        LogMy.d(TAG, "In initToolbar");
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        initTbViews();

        mTbImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show customer details dialog
                CustDetailsDialogCustApp dialog = new CustDetailsDialogCustApp();
                dialog.show(mFragMgr, DIALOG_CUSTOMER_DETAILS);
            }
        });

        mTbTitle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    // show customer details dialog
                    CustDetailsDialogCustApp dialog = new CustDetailsDialogCustApp();
                    dialog.show(mFragMgr, DIALOG_CUSTOMER_DETAILS);
                    return true;
                }
                return false;
            }
        });

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void selectDrawerItem(MenuItem item) {

        if(!mRetainedFragment.getResumeOk())
            return;

        mLastMenuItemId = item.getItemId();
        int i = item.getItemId();

        // Not able to use switch() - as not allowed in library modules
        if (i == R.id.menu_txns) {
            // show latest txns
            startTxnReportActivity(null,null);

        } else if(i == R.id.menu_operations) {
            AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
            mRetainedFragment.fetchCustomerOps();
        } else if (i == R.id.menu_change_mobile) {
            // show mobile change dialog
            MobileChangeDialog dialog = new MobileChangeDialog();
            dialog.show(mFragMgr, DIALOG_CHANGE_MOBILE);

        } else if (i == R.id.menu_forgot_pin) {
            PinResetDialog dialog = new PinResetDialog();
            dialog.show(mFragMgr, DIALOG_PIN_RESET);

        } else if (i == R.id.menu_change_pin) {
            PinChangeDialog dialog = new PinChangeDialog();
            dialog.show(getFragmentManager(), DIALOG_PIN_CHANGE);

        } else if (i == R.id.menu_change_passwd) {
            PasswdChangeDialog dialog = new PasswdChangeDialog();
            dialog.show(getFragmentManager(), DIALOG_CHANGE_PASSWORD);

        } else if (i == R.id.menu_contact) {
            Intent intent = new Intent(this, SingleWebViewActivity.class );
            intent.putExtra(SingleWebViewActivity.INTENT_EXTRA_URL, MyGlobalSettings.getContactUrl());
            startActivity(intent);
        }

        // Highlight the selected item has been done by NavigationView
        //item.setChecked(true);
        // Set action bar title
        //setTitle(item.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();

    }

    @Override
    public boolean refreshMchntList() {
        if(custStatsRefreshReq(mRetainedFragment.mCbsUpdateTime.getTime())) {
            // Fetch from scratch
            // While we could have implemented fetching only latest data here
            // However intentionally fetching from scratch - to avoid scenario wherein due to
            // some un-foreseen bug - wrong CB data is getting displayed always from local file
            mGetCbSince = 0;
            mRetainedFragment.mCashbacks = null;
            mRetainedFragment.mCbsUpdateTime = null;
            deleteCbFile();
            fetchCbData();
            return true;
        }
        return false;
    }

    private void updateTbForCustomer() {
        //mTbLayoutSubhead1.setVisibility(View.VISIBLE);

        // no error case: all cashback values available
        mTbTitle.setText(mCustomer.getMobile_num());
        mTbTitle2.setVisibility(View.GONE);
        if(mCustomer.getAdmin_status()!=DbConstants.USER_STATUS_ACTIVE ) {
            mTbTitle2.setVisibility(View.VISIBLE);
            //String secret = "~ "+mRetainedFragment.mCurrCashback.getCashback().getCustomer().getName();
            mTbTitle2.setText(DbConstants.userStatusDesc[mCustomer.getAdmin_status()]);
            mTbTitle2.setTextColor(ContextCompat.getColor(this, R.color.red_negative));

        }

        //mTbSubhead1Text1.setText(AppCommonUtil.getAmtStr(mRetainedFragment.mCurrCashback.getCurrAccBalance()));
        //mTbSubhead1Divider.setVisibility(View.VISIBLE);
        //mTbSubhead1Text2.setVisibility(View.VISIBLE);
        //mTbSubhead1Text2.setText(AppCommonUtil.getAmtStr(mRetainedFragment.mCurrCashback.getCurrCbBalance()));
    }

    private void initTbViews() {
        mTbImage = (AppCompatImageView) mToolbar.findViewById(R.id.tb_image) ;
        mTbTitle = (EditText) mToolbar.findViewById(R.id.tb_title) ;
        mTbTitle2 = (EditText) mToolbar.findViewById(R.id.tb_title_2) ;
        //mTbLayoutSubhead1 = (LinearLayout) mToolbar.findViewById(R.id.tb_layout_subhead1) ;
        //mTbSubhead1Text1 = (EditText) mToolbar.findViewById(R.id.tb_curr_cashload) ;
        //mTbSubhead1Text2 = (EditText) mToolbar.findViewById(R.id.tb_curr_cashback) ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBgProcessResponse(int errorCode, int operation) {
        LogMy.d(TAG,"In onBgProcessResponse: "+operation+", "+errorCode);

        // Session timeout case - show dialog and logout - irrespective of invoked operation
        if(errorCode==ErrorCodes.SESSION_TIMEOUT || errorCode==ErrorCodes.NOT_LOGGED_IN) {
            AppCommonUtil.cancelProgressDialog(true);
            DialogFragmentWrapper.createNotification(AppConstants.notLoggedInTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                    .show(mFragMgr, DIALOG_SESSION_TIMEOUT);
            return;
        }

        try {
            switch (operation) {
                case MyRetainedFragment.REQUEST_LOGOUT:
                    onLogoutResponse(errorCode);
                    break;
                case MyRetainedFragment.REQUEST_CHANGE_PASSWD:
                    passwordChangeResponse(errorCode);
                    break;
                case MyRetainedFragment.REQUEST_CHANGE_MOBILE:
                    onChangeMobileResponse(errorCode);
                    break;
                case MyRetainedFragment.REQUEST_FETCH_CB:
                    onFetchCbResponse(errorCode);
                    break;
                case MyRetainedFragment.REQUEST_CHANGE_PIN:
                    onPinChangeResponse(errorCode);
                    break;
                case MyRetainedFragment.REQUEST_FETCH_CUSTOMER_OPS:
                    AppCommonUtil.cancelProgressDialog(true);
                    if (errorCode == ErrorCodes.NO_ERROR) {
                        startCustomerOpsFrag();
                    } else if (errorCode == ErrorCodes.NO_DATA_FOUND) {
                        String error = String.format(getString(R.string.ops_no_data_info), MyGlobalSettings.getOpsKeepDays().toString());
                        DialogFragmentWrapper.createNotification(AppConstants.noDataFailureTitle, error, false, false)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;
            }
        } catch (Exception e) {
            AppCommonUtil.cancelProgressDialog(true);
            LogMy.e(TAG, "Exception in CashbackActivityCust", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), false, true)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    private void onFetchCbResponse(int errorCode) {
        LogMy.d(TAG, "In onFetchCbResponse: " + errorCode);
        AppCommonUtil.cancelProgressDialog(true);

        // read data from file, ir-respective of result from DB i.e. error, 0 records or whatever
        // dont read though - if records fetched are from scratch
        if(mGetCbSince != 0) {
            // some data was there in file earlier - read the same
            if(!processCbDataFile(true)) {
                // if some error in reading file - fetch all records from scratch
                mGetCbSince = 0;
                fetchCbData();
                return;
            }
        }

        // If here, means:
        // - either DB data was fetched from scratch
        // - or data available locally, and is read successfully in memory

        if(errorCode==ErrorCodes.NO_ERROR || errorCode==ErrorCodes.NO_DATA_FOUND) {
            // check if any data fetched
            if(mRetainedFragment.mLastFetchCashbacks!=null && mRetainedFragment.mLastFetchCashbacks.size() > 0 ) {

                if(mRetainedFragment.mCashbacks == null) {
                    // records fetched from scratch - no need to check for file
                    LogMy.d(TAG, "All fetched Cb records are from scratch: " + mRetainedFragment.mLastFetchCashbacks.size());
                    mRetainedFragment.mCashbacks = new HashMap<>();
                    //mRetainedFragment.stats = new CustomerStats();
                }
                // add all fetched records to the 'cashback store'
                // this will override any existing daat from file, with latest data from DB
                for (MyCashback cb :
                        mRetainedFragment.mLastFetchCashbacks) {
                    LogMy.d(TAG,"Adding CB row from DB to local store: "+cb.getMerchantId()+", "+cb.getClCredit());
                    mRetainedFragment.mCashbacks.put(cb.getMerchantId(), cb);
                    // Add to total stats
                    //mRetainedFragment.stats.addToStats(cb);
                }
                // reset to null
                mRetainedFragment.mLastFetchCashbacks = null;
                // set time for current set of records
                mRetainedFragment.mCbsUpdateTime = new Date();

                // write complete 'in memory cashback store' to the file
                //writeCbsToFile();

            } else {
                LogMy.d(TAG, "No updated data available in DB");
                if(mRetainedFragment.mCashbacks != null) {
                    // local data available from file - mark the same as latest
                    mRetainedFragment.mCbsUpdateTime = new Date();
                }
            }

            // Final merged CB records should be in 'mRetainedFragment.mCashbacks' now
            if(mRetainedFragment.mCashbacks == null) {
                // No data available locally - none fetched from DB
                // The customer has no cashbacks yet
                startErrorFrag(true,
                        "You are not registered with any Merchant yet",
                        "Get registered to save more!");
            } else {
                // write all data with time to the local file
                if(!writeCbsToFile())
                {
                    // try to delete file, like if partially created
                    deleteCbFile();
                }
                startCashbackListFrag();
            }

        } else {
            // Failed to fetch new data - show old one, if available
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                    .show(mFragMgr, DIALOG_NOTIFY_CB_FETCH_ERROR);

            // but you will need to capture 'back button press' in dialog too
            if(mRetainedFragment.mCashbacks == null) {
                // No data available locally, also error fetching data from DB
                startErrorFrag(false, null, null);
            } else {
                startCashbackListFrag();
            }
        }
    }

    private void deleteCbFile() {
        // try to delete file, like if partially created
        try {
            deleteFile(AppCommonUtil.getCashbackFileName(mCustomerUser.getCustomer().getPrivate_id()));
        } catch (Exception e) {
            // ignore exception
        }
    }

    private boolean writeCbsToFile() {
        LogMy.d(TAG,"In createCsvReport");

        FileOutputStream outputStream = null;
        try {
            String fileName = AppCommonUtil.getCashbackFileName(mCustomerUser.getCustomer().getPrivate_id());

            StringBuilder sb = new StringBuilder();
            // current time as first line in file
            sb.append(System.currentTimeMillis()).append(CommonConstants.NEWLINE_SEP);
            for (Map.Entry<String, MyCashback> entry : mRetainedFragment.mCashbacks.entrySet()) {
                MyCashback cb = entry.getValue();
                String csvStr = CsvConverter.csvStrFromCb(cb.getCurrCashback());
                sb.append(csvStr).append(CommonConstants.NEWLINE_SEP);
            }

            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(sb.toString().getBytes());
            outputStream.close();

        } catch(Exception e) {
            LogMy.e(TAG,"exception in writeCbsToFile: "+e.toString(),e);
            if(outputStream!=null) {
                try {
                    outputStream.close();
                } catch(Exception ex){
                    // ignore exception
                }
            }
            return false;
        }
        return true;
    }

    private void onChangeMobileResponse(int errorCode) {
        LogMy.d(TAG, "In onChangeMobileResponse: " + errorCode);
        AppCommonUtil.cancelProgressDialog(true);

        if(errorCode==ErrorCodes.NO_ERROR) {
            DialogFragmentWrapper.createNotification(AppConstants.defaultSuccessTitle, AppConstants.custMobileChangeSuccessMsg, false, false)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
            // merchant operation success, reset to null
            changeMobileParamsReset();
            // logout and ask user to login again with new mobile number
            logoutCustomer();

        } else if(errorCode==ErrorCodes.OTP_GENERATED) {
            // OTP sent successfully to new mobile, ask for the same
            // show the 'mobile change dialog' again
            MobileChangeDialog dialog = new MobileChangeDialog();
            dialog.show(mFragMgr, DIALOG_CHANGE_MOBILE);

        } else if(errorCode==ErrorCodes.WRONG_OTP) {
            // show error but don't reset
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);

        } else {
            // reset in case of any error
            changeMobileParamsReset();
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }
    private void changeMobileParamsReset() {
        mRetainedFragment.mNewMobileNum = null;
        mRetainedFragment.mPinMobileChange = null;
        mRetainedFragment.mOtpMobileChange = null;
    }

    @Override
    public void onPinOtp(String pinOrOtp, String tag) {
        if(tag.equals(DIALOG_CHANGE_MOBILE_PIN)) {
            mRetainedFragment.mPinMobileChange = pinOrOtp;
            // dispatch customer op for execution
            changeMobileNum();
        }
    }

    @Override
    //public void changeMobileNumOk(String newMobile, String cardNum) {
    public void changeMobileNumOk(String newMobile) {
        mRetainedFragment.mNewMobileNum = newMobile;
        //mRetainedFragment.mCardMobileChange = cardNum;

        // ask for customer PIN
        String txnDetail = String.format(AppConstants.msgChangeCustMobilePin,newMobile);
        OtpPinInputDialog dialog = OtpPinInputDialog.newInstance(AppConstants.titleChangeCustMobilePin, txnDetail, "Enter PIN", CommonConstants.PIN_LEN);
        dialog.show(mFragMgr, DIALOG_CHANGE_MOBILE_PIN);
    }

    @Override
    public void changeMobileNumOtp(String otp) {
        LogMy.d(TAG, "In changeMobileNumOtp: " + otp);
        mRetainedFragment.mOtpMobileChange = otp;
        changeMobileNum();
    }

    @Override
    public void changeMobileNumReset() {
        // reset and restart dialog
        changeMobileParamsReset();
        MobileChangeDialog dialog = new MobileChangeDialog();
        dialog.show(mFragMgr, DIALOG_CHANGE_MOBILE);
    }

    private void changeMobileNum() {
        int resultCode = AppCommonUtil.isNetworkAvailableAndConnected(this);
        if ( resultCode != ErrorCodes.NO_ERROR) {
            // Show error notification dialog
            DialogFragmentWrapper.createNotification(AppConstants.noInternetTitle, AppCommonUtil.getErrorDesc(resultCode), false, true)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
        } else {
            AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
            mRetainedFragment.changeMobileNum();
        }
    }

    private void logoutCustomer() {
        int resultCode = AppCommonUtil.isNetworkAvailableAndConnected(this);
        if ( resultCode != ErrorCodes.NO_ERROR) {
            // Show error notification dialog
            DialogFragmentWrapper.createNotification(AppConstants.noInternetTitle, AppCommonUtil.getErrorDesc(resultCode), false, true)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
        } else {
            // show progress dialog
            AppCommonUtil.showProgressDialog(this, AppConstants.progressLogout);
            mRetainedFragment.logoutCustomer();
        }
    }

    private void onLogoutResponse(int errorCode) {
        LogMy.d(TAG, "In onLogoutResponse: " + errorCode);

        AppCommonUtil.cancelProgressDialog(true);
        CustomerUser.reset();

        //Start Login Activity
        if(!mExitAfterLogout) {
            Intent intent = new Intent( this, LoginCustActivity.class );
            // clear cashback activity from backstack
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onPasswdChangeData(String oldPasswd, String newPassword) {
        AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
        mRetainedFragment.changePassword(oldPasswd, newPassword);
    }

    private void passwordChangeResponse(int errorCode) {
        LogMy.d(TAG, "In passwordChangeResponse: " + errorCode);
        AppCommonUtil.cancelProgressDialog(true);

        if(errorCode==ErrorCodes.NO_ERROR) {
            DialogFragmentWrapper.createNotification(AppConstants.pwdChangeSuccessTitle, AppConstants.pwdChangeSuccessMsg, false, false)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
            logoutCustomer();

        } else {
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    @Override
    public void onPinChangeData(String oldPin, String newPin) {
        AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
        mRetainedFragment.changePin(oldPin, newPin, null);
    }

    @Override
    public void onPinResetData(String secret) {
        AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
        mRetainedFragment.changePin(null, null, secret);
    }

    private void onPinChangeResponse(int errorCode) {
        LogMy.d(TAG, "In onPinChangeResponse: " + errorCode);
        AppCommonUtil.cancelProgressDialog(true);

        if(mLastMenuItemId==R.id.menu_change_pin) {
            if(errorCode==ErrorCodes.NO_ERROR) {
                DialogFragmentWrapper.createNotification(AppConstants.defaultSuccessTitle, AppConstants.pinChangeSuccessMsg, false, false)
                        .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
            } else {
                DialogFragmentWrapper.createNotification(AppConstants.pinChangeFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                        .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
            }

        } else if(mLastMenuItemId==R.id.menu_forgot_pin) {
            if(errorCode == ErrorCodes.OP_SCHEDULED) {
                // Show success notification dialog
                String msg = String.format(AppConstants.pinGenerateSuccessMsg, Integer.toString(AppCommonUtil.mErrorParams.opScheduledMins));
                DialogFragmentWrapper.createNotification(AppConstants.defaultSuccessTitle, msg, false, false)
                        .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);

            } else if(errorCode == ErrorCodes.DUPLICATE_ENTRY) {
                // Old request is already pending
                String msg = String.format(AppConstants.pinGenerateDuplicateRequestMsg, Integer.toString(MyGlobalSettings.getCustPasswdResetMins()));
                DialogFragmentWrapper.createNotification(AppConstants.pinResetFailureTitle, msg, false, true)
                        .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);

            } else {
                // Show error notification dialog
                DialogFragmentWrapper.createNotification(AppConstants.pinResetFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                        .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            }
        }
    }

    @Override
    public void getMchntTxns(String id, String name) {
        startTxnReportActivity(id, name);
    }

    private void startTxnReportActivity(String mchntId, String name) {
        // start reports activity
        Intent intent = new Intent( this, TxnReportsCustActivity.class );
        if(mchntId!=null) {
            intent.putExtra(TxnReportsCustActivity.EXTRA_MERCHANT_ID, mchntId);
        }
        if(name!=null) {
            intent.putExtra(TxnReportsCustActivity.EXTRA_MERCHANT_NAME, name);
        }
        startActivityForResult(intent, RC_TXN_REPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if(requestCode == RC_TXN_REPORT) {
                if(resultCode == ErrorCodes.SESSION_TIMEOUT) {
                    DialogFragmentWrapper.createNotification(AppConstants.notLoggedInTitle, AppCommonUtil.getErrorDesc(resultCode), false, true)
                            .show(mFragMgr, DIALOG_SESSION_TIMEOUT);
                }
            }
        }
        catch (Exception e) {
            AppCommonUtil.cancelProgressDialog(true);
            LogMy.e(TAG, "Exception in CashbackActivity:onActivityResult: "+requestCode+", "+resultCode, e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), false, true)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }



    public void onDialogResult(String tag, int indexOrResultCode, ArrayList<Integer> selectedItemsIndexList) {
        LogMy.d(TAG, "In onDialogResult: " + tag);

        if (tag.equals(DIALOG_BACK_BUTTON)) {
            mExitAfterLogout = true;
            // not logging out
            logoutCustomer();
            //finish();
        } else if(tag.equals(DIALOG_SESSION_TIMEOUT)) {
            mExitAfterLogout = false;
            logoutCustomer();
        }
    }

    private void startCashbackListFrag() {

        // trying to start cbList fragment - means all cb records are available now
        if(mRetainedFragment.mCashbacks != null) {
            // recalculate stats
            mRetainedFragment.stats = new CustomerStats();
            for(MyCashback cb: mRetainedFragment.mCashbacks.values()) {
                mRetainedFragment.stats.addToStats(cb);
            }

            // Update 'Total Balance'
            AppCommonUtil.showAmtColor(this,null,mTbSubhead1Text1,mRetainedFragment.stats.getClBalance(),false);

            // create or refresh cashback list fragment
            mMchntListFragment = (CashbackListFragment) mFragMgr.findFragmentByTag(CASHBACK_LIST_FRAGMENT);
            if (mMchntListFragment == null) {
                //setDrawerState(false);
                mMchntListFragment = new CashbackListFragment();
                mFragMgr.beginTransaction()
                        .add(R.id.fragment_container_1, mMchntListFragment, CASHBACK_LIST_FRAGMENT)
                        .addToBackStack(CASHBACK_LIST_FRAGMENT)
                        .commit();

            } else if(mMchntListFragment.isVisible()){
                LogMy.d(TAG,"CashbackListFragment already available and visible");
                mMchntListFragment.refreshData();
            }
        }
    }

    private void startErrorFrag(boolean isInfo, String info1, String info2) {
        if (mFragMgr.findFragmentByTag(ERROR_FRAGMENT) == null) {
            //setDrawerState(false);

            Fragment fragment = ErrorFragment.newInstance(isInfo, info1, info2);
            FragmentTransaction transaction = mFragMgr.beginTransaction();

            // Add over the existing fragment
            transaction.replace(R.id.fragment_container_1, fragment, ERROR_FRAGMENT);
            //transaction.addToBackStack(CASHBACK_LIST_FRAGMENT);

            // Commit the transaction
            transaction.commit();
        }
    }

    private void startCustomerOpsFrag() {
        if (mFragMgr.findFragmentByTag(CUSTOMER_OPS_LIST_FRAG) == null) {

            Fragment fragment = new CustomerOpListFrag();
            FragmentTransaction transaction = mFragMgr.beginTransaction();

            // Add over the existing fragment
            transaction.replace(R.id.fragment_container_1, fragment, CUSTOMER_OPS_LIST_FRAG);
            transaction.addToBackStack(CUSTOMER_OPS_LIST_FRAG);

            // Commit the transaction
            transaction.commit();
        }
    }

    @Override
    public MyRetainedFragment getRetainedFragment() {
        return mRetainedFragment;
    }

    @Override
    public void onBackPressed() {
        LogMy.d(TAG,"In onBackPressed: "+mFragMgr.getBackStackEntryCount());

        if(!mRetainedFragment.getResumeOk())
            return;

        try {
            if (this.mDrawer.isDrawerOpen(GravityCompat.START)) {
                this.mDrawer.closeDrawer(GravityCompat.START);
                return;
            }

            if ((mMchntListFragment != null && mMchntListFragment.isVisible()) ||
                    mFragMgr.getBackStackEntryCount() == 0) {
                DialogFragmentWrapper.createConfirmationDialog(AppConstants.exitGenTitle, AppConstants.exitAppMsg, false, false)
                        .show(mFragMgr, DIALOG_BACK_BUTTON);
            } else {
                mFragMgr.popBackStackImmediate();
            }
        } catch (Exception e) {
            AppCommonUtil.cancelProgressDialog(true);
            LogMy.e(TAG, "Exception in CashbackActivityCust", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), false, true)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        LogMy.d(TAG,"In onPostCreate: ");
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    private void fetchCbData() {
        LogMy.d(TAG,"Fetching cb data from backend: "+mGetCbSince);
        int resultCode = AppCommonUtil.isNetworkAvailableAndConnected(this);
        if ( resultCode != ErrorCodes.NO_ERROR) {
            // Show error notification dialog
            DialogFragmentWrapper.createNotification(AppConstants.noInternetTitle, AppCommonUtil.getErrorDesc(resultCode), false, true)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
        } else {
            AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
            mRetainedFragment.fetchCashback(mGetCbSince, this);
        }
    }

    // Reads and process local cashback data file
    // If the fx returns true, then 'mRetainedFragment.mCashbacks' will definitly have some data
    private boolean processCbDataFile(boolean ignoreTime) {
        LogMy.d(TAG,"In processCbDataFile: "+ignoreTime);
        String fileName = AppCommonUtil.getCashbackFileName(mCustomerUser.getCustomer().getPrivate_id());

        try {
            InputStream inputStream = openFileInput(fileName);
            if ( inputStream != null ) {
                int lineCnt = 0;

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    if(lineCnt==0) {
                        // first line is header giving file creation epoch time
                        String[] csvFields = receiveString.split(CommonConstants.CSV_DELIMETER, -1);
                        long fileCreateTime = Long.parseLong(csvFields[0]);
                        if(custStatsRefreshReq(fileCreateTime) &&
                                !ignoreTime ) {
                            LogMy.d(TAG,"Cb file available data older than configured time: "+fileCreateTime);
                            // file is older than configured no refresh duration
                            mGetCbSince = fileCreateTime;
                            // don't read file further, will be done after updated records are fetched from DB
                            return false;
                        } else {
                            mRetainedFragment.mCashbacks = new HashMap<>();
                            //mRetainedFragment.stats = new CustomerStats();
                            mRetainedFragment.mCbsUpdateTime = new Date(fileCreateTime);
                        }
                    } else {
                        // ignore empty lines
                        if(!receiveString.equals(CommonConstants.NEWLINE_SEP)) {
                            processCbCsvRecord(receiveString);
                        }
                    }

                    lineCnt++;
                }

                if(mRetainedFragment.mCashbacks==null || mRetainedFragment.mCashbacks.size()==0) {
                    // probably empty file - or no line after first
                    LogMy.w(TAG,"Empty or invalid Cashback CSV file");
                    mRetainedFragment.mCashbacks = null;
                    //mRetainedFragment.stats = null;
                    mRetainedFragment.mCbsUpdateTime = null;
                    return false;
                }

                inputStream.close();
                LogMy.d(TAG,"Processed "+lineCnt+" lines from "+fileName);
            } else {
                String error = "openFileInput returned null for Cashback CSV file: "+fileName;
                LogMy.e(TAG, error);
                throw new FileNotFoundException(error);
            }
        } catch (Exception ex) {
            if(!(ex instanceof FileNotFoundException)) {
                LogMy.e(TAG,"Exception while reading cb file: "+ex.toString(),ex);
            }
            // reset all, fetch all data fresh
            mRetainedFragment.mCashbacks = null;
            //mRetainedFragment.stats = null;
            mRetainedFragment.mCbsUpdateTime = null;
            mGetCbSince = 0;
            return false;
        }

        return true;
    }

    private void processCbCsvRecord(String csvString) {
        MyCashback cb = new MyCashback(csvString, false);
        //cb.init(csvString, false);
        mRetainedFragment.mCashbacks.put(cb.getMerchantId(), cb);
        //mRetainedFragment.stats.addToStats(cb);
        LogMy.d(TAG,"Added new item in cashback store: "+mRetainedFragment.mCashbacks.size());
    }

    private boolean custStatsRefreshReq(long lastUpdate) {

        if(MyGlobalSettings.getCustNoRefreshMins()==(24*60)) {
            // 24 is treated as special case as 'once in a day'
            DateUtil todayMidnight = (new DateUtil()).toMidnight();
            if(lastUpdate < todayMidnight.getTime().getTime()) {
                // Last update was not today
                return true;
            }
        } else {
            // Check if updated in last 'cust no refresh hours'
            long timeDiff = (new Date()).getTime() - lastUpdate;
            long noRefreshDuration = MyGlobalSettings.getCustNoRefreshMins()*CommonConstants.MILLISECS_IN_MINUTE;
            if( timeDiff > noRefreshDuration ) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        LogMy.d(TAG, "In onResume: ");
        super.onResume();
        if(getFragmentManager().getBackStackEntryCount()==0) {
            // no fragment in backstack - so flag wont get set by any fragment - so set it here
            // though this shud never happen - as CashbackActivity always have a fragment
            mRetainedFragment.setResumeOk(true);
        }
        if(AppCommonUtil.getProgressDialogMsg()!=null) {
            AppCommonUtil.showProgressDialog(this, AppCommonUtil.getProgressDialogMsg());
        }
        setDrawerState(true);
        AppCommonUtil.setUserType(DbConstants.USER_TYPE_CUSTOMER);
    }

    @Override
    protected void onPause() {
        LogMy.d(TAG,"In onPause: ");
        super.onPause();
        // no need to do this in each fragment - as activity onPause will always get called
        mRetainedFragment.setResumeOk(false);
        AppCommonUtil.cancelProgressDialog(false);
        setDrawerState(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //CustomerUser.reset();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("mExitAfterLogout", mExitAfterLogout);
        outState.putInt("mLastMenuItemId", mLastMenuItemId);
        outState.putLong("mGetCbSince", mGetCbSince);
    }
}
*/