package in.ezeshop.customerbase.entities;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.DeviceRegistration;
import com.backendless.HeadersManager;
import com.backendless.Messaging;
import com.backendless.exceptions.BackendlessException;
import com.backendless.persistence.local.UserIdStorageFactory;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

import in.ezeshop.appbase.backendAPI.CommonServices;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.entities.MyAreas;
import in.ezeshop.appbase.entities.MyCities;
import in.ezeshop.appbase.utilities.MsgPushService;
import in.ezeshop.common.MyGlobalSettings;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Cashback;
import in.ezeshop.common.database.CustAddress;
import in.ezeshop.common.database.CustomerOps;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.database.Customers;
import in.ezeshop.common.database.Transaction;
import in.ezeshop.customerbase.backendAPI.CustomerServices;
import in.ezeshop.customerbase.backendAPI.CustomerServicesNoLogin;

/**
 * Created by adgangwa on 17-07-2016.
 */
public class CustomerUser {
    private static final String TAG = "CustApp-CustomerUser";

    private static CustomerUser mInstance;

    private Customers mCustomer;
    private boolean mPseudoLoggedIn;
    private String mUserToken;
    // Flag to indicate, if this device's registration for push messaging is to be checked
    //private boolean mChkMsgDevReg;
    // List of addresses
    private List<CustAddress> mAddresses;

    /*
     * Singleton class
     */
    private CustomerUser(){
        //mCustomerUser = new BackendlessUser();
    }

    public static CustomerUser getInstance() {
        /*if(mInstance==null) {
            mInstance = new CustomerUser();
        }*/
        return mInstance;
    }
    private static void createInstance() {
        if (mInstance == null) {
            LogMy.d(TAG, "Creating CustomerUser instance");
            mInstance = new CustomerUser();
            mInstance.mPseudoLoggedIn = false;
            //mInstance.mChkMsgDevReg = false;
        }
    }

    /*
     * Static public methods
     */
    public static void reset() {
        if(mInstance!=null) {
            mInstance.mCustomer = null;
            mInstance.mAddresses = null;
            //mInstance.mBackendlessUser = null;
            mInstance = null;
        }
    }

    /*
     * Methods to restore loginId / password
     */
    public static int resetPassword(String custMobile, String secret) {
        try {
            CustomerServicesNoLogin.getInstance().resetCustomerPassword(custMobile, secret);
            LogMy.d(TAG,"generatePassword success");
        } catch (BackendlessException e) {
            LogMy.e(TAG,"Customer password generate failed: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    /*
     * Login-Logout methods
     * CustomerUser singleton instance is created on successfull login
     * and destroyed on logout / explicit reset.
     */
    /*public static void isValidLogin(AsyncCallback<Boolean> responder) {
        //return Backendless.UserService.isValidLogin();
        Backendless.UserService.isValidLogin( responder );
    }*/

    public static int tryAutoLogin() {
        LogMy.d(TAG, "In tryAutoLogin");
        try {
            if(!Backendless.UserService.isValidLogin()) {
                return ErrorCodes.NOT_LOGGED_IN;
            }
            BackendlessUser user = null;
            String currentUserObjectId = UserIdStorageFactory.instance().getStorage().get();
            LogMy.d(TAG, "currentUserObjectId: "+currentUserObjectId);

            if(currentUserObjectId!=null && !currentUserObjectId.isEmpty()) {
                user = getCustUserById(currentUserObjectId);
                if(user==null) {
                    return ErrorCodes.GENERAL_ERROR;
                }
                Backendless.UserService.setCurrentUser(user);
            }
            int retStatus = loadOnLogin(user);
            if( retStatus!= ErrorCodes.NO_ERROR) {
                return retStatus;
            }
        } catch (BackendlessException e) {
            LogMy.d(TAG,"Auto Login failed: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    public static int login(String userId, String password) {
        LogMy.d(TAG, "In login");
        try {
            //BackendlessUser user = Backendless.UserService.login(userId, password, true);
            BackendlessUser user = Backendless.UserService.login(userId, password, false);
            LogMy.d(TAG, "Customer Login Success: " + userId);

            int retStatus = loadOnLogin(user);
            if( retStatus!= ErrorCodes.NO_ERROR) {
                logout();
                return retStatus;
            }

            // register device for push messages
            mInstance.processDevRegForMsging();

        } catch (BackendlessException e) {
            LogMy.e(TAG,"Login failed: "+e.toString());
            logout();
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    /*
     * If no 'device id' present in Customer object -OR- 'device id' present is not same as this device's id
     * then, register this device's id with google for messaging and save the same in Customer object
     */
    private void processDevRegForMsging() {
        LogMy.d(TAG, "In processDevRegForMsging: "+ Messaging.DEVICE_ID);
        if(mInstance.getCustomer()==null || mPseudoLoggedIn) {
            return;
        }

        try {
            String curDevId = mInstance.getCustomer().getMsgDevId();
            if(curDevId==null || curDevId.isEmpty() || !curDevId.equals(Messaging.DEVICE_ID)) {
                // Most probably first login case
                // Register this device's id with google for messaging
                LogMy.d(TAG, "First login case probably");
                AppCommonUtil.submitDevRegForMsging();

                // The device is not registered yet completely - and only the request is submitted
                // It may take upto few seconds while registration gets completed/updated in google servers
                // So, we are not updating 'device id' in Customer object yet
                // Once registration is done, backendless will call 'onRegistered()' function of 'MsgPushService' class
                // We will update 'device id' in Customer object only after that

            } else {
                // Customer object has some device id set - but it may be different than current device's id
                // i.e. scenario wherein user logged with another device previously
                // So, check if current device's id is registered
                // If not, register the same and update in Customer object
                // If registered, check if its same as that in Customer object - if not, update the same.

                LogMy.d(TAG, "Device probably already registered: "+curDevId);
                if(checkMsgDevReg()==ErrorCodes.DEV_NOT_REG_FOR_MSGING) {
                    // as not registered, so try to register again
                    LogMy.e(TAG, "Device not registered anymore, so trying to register again");
                    AppCommonUtil.submitDevRegForMsging();
                }
            }
        } catch(Exception e) {
            LogMy.e(TAG,"Device registration for messaging failed: "+e.toString());
        }

    }

    // Check if current device is registered with google for messaging
    // If no, return error.
    // If yes, check if same device id is there in Customer object.
    //      If not, update the same and return Success
    //      If yes, return Success
    public int checkMsgDevReg() {
        LogMy.d(TAG, "In checkMsgDevReg");
        if (mInstance.getCustomer() == null || mPseudoLoggedIn) {
            return ErrorCodes.GENERAL_ERROR;
        }

        //setChkMsgDevReg(false);
        // Set flag to - disable checking again for 'device registration'
        // This gets set to true in 'onRegistered()' function of 'MsgPushService' class
        // i.e. when any previously sent registration request gets completed
        MsgPushService.setChkMsgDevReg(false);

        try {
            // to check this device's registration - retrieve registration data for this device
            String newDevId = AppCommonUtil.checkDevRegForMsging();
            if (newDevId == null || newDevId.isEmpty()) {
                LogMy.d(TAG, "This device is not registered for messaging: " + Messaging.DEVICE_ID);
                return ErrorCodes.DEV_NOT_REG_FOR_MSGING;
            }

            // Checking for 'mCustomer.getMsgDevId()' is not required but still doing
            if (mCustomer.getMsgDevId() == null || mCustomer.getMsgDevId().isEmpty() || !mCustomer.getMsgDevId().equals(newDevId)) {
                LogMy.d(TAG, "Trying to update customer object for deviceId: " + newDevId);
                String updatedDevId = CommonServices.getInstance().setMsgDeviceId(mInstance.getCustomer().getMobile_num(), newDevId);
                mCustomer.setMsgDevId(updatedDevId);
                LogMy.d(TAG, "Updated device registration Id: " + mCustomer.getMsgDevId());
            }

            // device registration for messaging verified fine
            return ErrorCodes.NO_ERROR;

        } catch (Exception e) {
            LogMy.e(TAG, "BackendlessException in checkMsgDevReg: " + e.toString());
            return AppCommonUtil.getLocalErrorCode((BackendlessException) e);
        }
    }

    public int saveCustAddress(CustAddress addr, Boolean setAsDefault) {
        LogMy.d(TAG, "In saveCustAddress: "+addr.getAreaNIDB().getValidated());
        if(mPseudoLoggedIn) {
            return ErrorCodes.OPERATION_NOT_ALLOWED;
        }

        try {
            mAddresses = CustomerServices.getInstance().saveCustAddress(addr, setAsDefault);
            LogMy.d(TAG,"saveCustAddress success: "+mAddresses.size());

            // If new area add case - update area list too
            if(!addr.getAreaNIDB().getValidated()) {
                int status = MyAreas.fetchAreas(addr.getAreaNIDB().getCity().getCity());
                if (status != ErrorCodes.NO_ERROR) {
                    // add manually - even though 'area id' will be missing
                    LogMy.e(TAG, "Area list resync failed");
                    MyAreas.addArea(addr.getAreaNIDB());
                }
            }
        } catch (BackendlessException e) {
            LogMy.e(TAG,"saveCustAddress failed: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    // This method doesn't do any login, but downloads corresponding customer Backendless user and object
    public static int pseudoLogin(String customerMob) {
        LogMy.d(TAG, "In pseudoLogin: "+customerMob);

        try {
            // create instance of CustomerUser class
            createInstance();
            mInstance.loadCustomer(customerMob);
            mInstance.mPseudoLoggedIn = true;

        } catch (BackendlessException e) {
            LogMy.e(TAG,"Exception while pseudo login: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    public static int logout() {
        LogMy.d(TAG, "In logout");
        if(mInstance!=null && !mInstance.mPseudoLoggedIn) {
            try {
                Backendless.UserService.logout();
                //LogMy.d(TAG, "Logout Success: " + mInstance.mCustomer.getMobile_num());
            } catch (BackendlessException e) {
                LogMy.e(TAG, "Logout failed: " + e.toString());
                return AppCommonUtil.getLocalErrorCode(e);
            }
        } else {
            LogMy.d(TAG,"No need to logout");
        }
        // reset all
        reset();
        return ErrorCodes.NO_ERROR;
    }

    public static int enableAccount(String userId, String passwd, String rcvdOtp, String pin) {
        LogMy.d(TAG, "In enableAccount");
        try {
            CustomerServicesNoLogin.getInstance().enableCustAccount(userId, passwd, rcvdOtp, pin);
            LogMy.d(TAG,"enableAccount success");
        } catch (BackendlessException e) {
            LogMy.e(TAG,"enableAccount failed: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    public List<CustomerOps> fetchCustomerOps() throws BackendlessException {
        LogMy.d(TAG, "In fetchCustomerOps");
        isLoginValid();

        return CustomerServices.getInstance().getCustomerOps(mCustomer.getPrivate_id());
    }

    /*
     * Methods to change profile / settings
     */
    public int changePassword(String oldPasswd, String newPasswd) {
        LogMy.d(TAG, "In changePassword: ");
        if(mPseudoLoggedIn) {
            return ErrorCodes.OPERATION_NOT_ALLOWED;
        }

        try {
            isLoginValid();
            CommonServices.getInstance().changePassword(mCustomer.getMobile_num(), oldPasswd, newPasswd);
            LogMy.d(TAG,"changePassword success");
        } catch (BackendlessException e) {
            LogMy.e(TAG,"Change password failed: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }

        return ErrorCodes.NO_ERROR;
    }

    //public int changeMobileNum(String cardNum, String pin, String newMobile, String otp) {
    public int changeMobileNum(String pin, String newMobile, String otp) {
        if(mPseudoLoggedIn) {
            return ErrorCodes.OPERATION_NOT_ALLOWED;
        }

        try {
            isLoginValid();
            CommonServices.getInstance().execCustomerOp(DbConstants.OP_CHANGE_MOBILE, mCustomer.getMobile_num(),"",otp,pin,newMobile);
            LogMy.d(TAG,"changeMobileNum success");

        } catch (BackendlessException e) {
            LogMy.e(TAG,"changeMobileNum failed: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }

        return ErrorCodes.NO_ERROR;
    }

    public int changePin(String oldPin, String newPin, String secret) {
        if(mPseudoLoggedIn) {
            return ErrorCodes.OPERATION_NOT_ALLOWED;
        }

        try {
            isLoginValid();
            if(oldPin==null || newPin==null) {
                // PIN reset scenario
                CommonServices.getInstance().execCustomerOp(DbConstants.OP_RESET_PIN, mCustomer.getMobile_num(),
                        secret, "", oldPin, newPin);
            } else {
                // PIN change scenario
                CommonServices.getInstance().execCustomerOp(DbConstants.OP_CHANGE_PIN, mCustomer.getMobile_num(),
                        "", "", oldPin, newPin);
            }

        } catch (BackendlessException e) {
            LogMy.e(TAG,"changePin failed: "+e.toString());
            return AppCommonUtil.getLocalErrorCode(e);
        }

        return ErrorCodes.NO_ERROR;
    }

    public List<Cashback> fetchCashbacks(Long updatedSince) throws BackendlessException {
        LogMy.d(TAG, "In fetchCashback");
        isLoginValid();
        return CustomerServices.getInstance().getCashbacks(mCustomer.getPrivate_id(), updatedSince);
    }

    public List<Transaction> fetchTxns(String whereClause) throws BackendlessException {
        LogMy.d(TAG, "In fetchTxns");
        isLoginValid();
        return CustomerServices.getInstance().getTransactions(mCustomer.getPrivate_id(), whereClause);
    }


    /*
     * Getter / Setter
     */
    public String getUserToken() {
        return mUserToken;
    }

    public Customers getCustomer()
    {
        return mCustomer;
    }

    public boolean isPseudoLoggedIn() {
        return mPseudoLoggedIn;
    }

    /*public boolean isChkMsgDevReg() {
        return mChkMsgDevReg;
    }

    public synchronized void setChkMsgDevReg(boolean chkMsgDevReg) {
        this.mChkMsgDevReg = chkMsgDevReg;
    }*/

    public List<CustAddress> getAllAddress() {
        return mAddresses;
    }

    public CustAddress getAddress(String id) {
        if (!(mAddresses == null || mAddresses.isEmpty())) {
            for (CustAddress addr :
                    mAddresses) {
                if (addr.getId().equals(id)) {
                    return addr;
                }
            }
            LogMy.wtf(TAG,"Address not found: "+id);
        } else {
            LogMy.d(TAG,"No addresses available for customer");
        }
        return null;
    }

    /*
     * Private helper methods
     */
    private static BackendlessUser getCustUserById(String objectId) {
        ArrayList<String> relationProps = new ArrayList<>();
        relationProps.add("customer");
        relationProps.add("customer.membership_card");
        return Backendless.Data.of( BackendlessUser.class ).findById( objectId, relationProps );
    }

    private static int loadOnLogin(BackendlessUser user) {
        LogMy.d(TAG, "In loadOnLogin");
        int userType = (Integer)user.getProperty("user_type");
        if( userType != DbConstants.USER_TYPE_CUSTOMER) {
            String userId = (String) user.getProperty("user_id");
            // wrong user type
            LogMy.e(TAG,"Invalid usertype in customer app: "+userType+", "+userId);
            return ErrorCodes.USER_WRONG_ID_PASSWD;
        }

        // create instance of CustomerUser class
        createInstance();
        mInstance.mCustomer = (Customers) user.getProperty("customer");
        mInstance.initWithCustObject();

        // load all child objects
        //mInstance.loadCustomer(userId);
        LogMy.d(TAG, "Customer Load Success: " + mInstance.mCustomer.getMobile_num());

        // Store user token
        mInstance.mUserToken = HeadersManager.getInstance().getHeader(HeadersManager.HeadersEnum.USER_TOKEN_KEY);
        if(mInstance.mUserToken == null || mInstance.mUserToken.isEmpty()) {
            logout();
            return ErrorCodes.GENERAL_ERROR;
        }

        int status = AppCommonUtil.loadGlobalSettings(MyGlobalSettings.RunMode.appCustomer);
        if( status != ErrorCodes.NO_ERROR ) {
            return status;
        }
        status = MyCities.initSync();
        if( status != ErrorCodes.NO_ERROR ) {
            return status;
        }
        /*status = MyAreas.fetchAreas();
        if( status != ErrorCodes.NO_ERROR ) {
            return status;
        }*/

        return status;
    }

    private void isLoginValid() {
        // Not working properly - so commenting it out
        /*String userToken = UserTokenStorageFactory.instance().getStorage().get();
        if(userToken==null || userToken.isEmpty()) {
            LogMy.e(TAG,"User token is null. Auto logout scenario");
            throw new BackendlessException(String.valueOf(ErrorCodes.NOT_LOGGED_IN), "");
        }*/
    }

    private void loadCustomer(String mobileNum) {
        mCustomer = CommonServices.getInstance().getCustomer(mobileNum);
        initWithCustObject();
    }

    private void initWithCustObject() {
        // map cashback and transaction table
        //Backendless.Data.mapTableToClass(mCustomer.getCashback_table(), Cashback.class);

        String[] csvFields = mCustomer.getTxn_tables().split(CommonConstants.CSV_DELIMETER, -1);
        for (String str : csvFields) {
            Backendless.Data.mapTableToClass(str, Transaction.class);
        }

        csvFields = mCustomer.getCashback_table().split(CommonConstants.CSV_DELIMETER, -1);
        for (String str : csvFields) {
            Backendless.Data.mapTableToClass(str, Cashback.class);
        }

        // extract addresses from customer object and reset it to null
        // 'addresses' field is not actually stored in DB - and only used as transport during login
        mAddresses = mCustomer.getAddressesNIDB();
        // important to reset to null - so as does not get stored in DB
        mCustomer.setAddressesNIDB(null);
        if(mAddresses==null) {
            LogMy.d(TAG,"Customer addresses are null");
            // Give memory - even if no element added
            // to avoid checking for null at all places - size is already checked for
            mAddresses = new ArrayList<>();
        }

        // Set user id for crashlytics
        if(AppConstants.USE_CRASHLYTICS) {
            Crashlytics.setUserIdentifier(mCustomer.getPrivate_id());
        }
    }
}
