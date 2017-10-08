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
    private boolean mChkMsgDevReg;
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
            mInstance.mChkMsgDevReg = false;
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
            mInstance.regDevForMsging();

        } catch (BackendlessException e) {
            LogMy.e(TAG,"Login failed: "+e.toString());
            logout();
            return AppCommonUtil.getLocalErrorCode(e);
        }
        return ErrorCodes.NO_ERROR;
    }

    private void regDevForMsging() {
        LogMy.d(TAG, "In regDevForMsging: "+ Messaging.DEVICE_ID);
        if(mInstance.getCustomer()==null) {
            return;
        }

        try {
            String curDevId = mInstance.getCustomer().getMsgDevId();
            if(curDevId==null || curDevId.isEmpty() || !curDevId.equals(Messaging.DEVICE_ID)) {
                LogMy.d(TAG, "Trying to register this device for messaging");
                Backendless.Messaging.registerDevice(AppConstants.PUSH_MSG_SEND_ID);
                LogMy.d(TAG, "Device registered succesfully for messaging");

            } else {
                LogMy.d(TAG, "Device probably already registered: "+curDevId);
                if(checkMsgDevReg()==ErrorCodes.DEV_NOT_REG_FOR_MSGING) {
                    // as not registered, so try to register again
                    LogMy.e(TAG, "Device not registered anymore, so trying to register again");
                    Backendless.Messaging.registerDevice(AppConstants.PUSH_MSG_SEND_ID);
                    LogMy.d(TAG, "Device registered succesfully for messaging");
                }
            }
        } catch(Exception e) {
            LogMy.e(TAG,"Device registration for messaging failed: "+e.toString());
        }
    }

    public int checkMsgDevReg() {
        LogMy.d(TAG, "In checkMsgDevReg");
        setChkMsgDevReg(false);

        if(mInstance.getCustomer()==null) {
            return ErrorCodes.GENERAL_ERROR;
        }

        try {
            // to check this device's registration - retrive registration data for this device
            DeviceRegistration regData = Backendless.Messaging.getDeviceRegistration();
            String newDevId = regData.getDeviceId();
            if(newDevId==null || newDevId.isEmpty()) {
                LogMy.d(TAG,"This device is not registered for messaging: "+Messaging.DEVICE_ID);
                return ErrorCodes.DEV_NOT_REG_FOR_MSGING;
            }

            if(mCustomer.getMsgDevId()==null || mCustomer.getMsgDevId().isEmpty() || !mCustomer.getMsgDevId().equals(newDevId)) {
                LogMy.d(TAG, "Trying to update customer object for deviceId: " + newDevId);
                String updatedDevId = CommonServices.getInstance().setMsgDeviceId(mInstance.getCustomer().getMobile_num(), newDevId);
                mCustomer.setMsgDevId(updatedDevId);
                LogMy.d(TAG, "Updated device registration Id: " + mCustomer.getMsgDevId());
            }

            // device registration for messaging verified fine
            return ErrorCodes.NO_ERROR;

        } catch (Exception e) {
            if(e instanceof BackendlessException) {
                if(((BackendlessException) e).getCode().equals(ErrorCodes.BL_ERROR_MSGING_UNKNOWN_DEV)) {
                    // This device is not registered for messaging
                    LogMy.d(TAG,"This device is not registered for messaging: "+Messaging.DEVICE_ID);
                    return ErrorCodes.DEV_NOT_REG_FOR_MSGING;
                }
                LogMy.e(TAG,"BackendlessException in checkMsgDevReg: "+e.toString());
                return AppCommonUtil.getLocalErrorCode((BackendlessException)e);
            }

            LogMy.e(TAG,"Exception in checkMsgDevReg: "+e.toString());
            return ErrorCodes.GENERAL_ERROR;
        }

        /*Backendless.Messaging.getRegistrations(new AsyncCallback<DeviceRegistration>() {
            @Override
            public void handleResponse(DeviceRegistration response) {
                String devId = response.getDeviceId();
                LogMy.d(TAG, "Received device registration data: " + devId);

                try {
                    String curDevId = mInstance.getCustomer().getMsgDevId();
                    if (curDevId == null || curDevId.isEmpty() || !curDevId.equals(devId)) {
                        LogMy.d(TAG, "Trying to update customer object for deviceId: " + response.getDeviceId());
                        String newDevId = CommonServices.getInstance().setMsgDeviceId(mInstance.getCustomer().getMobile_num(), devId);
                        mInstance.getCustomer().setMsgDevId(newDevId);
                    }
                } catch (Exception e) {
                    LogMy.e(TAG,"Failed to update customer object for msgDevId: "+e.toString());
                    if(devId!=null) {
                        // error after device registration - deregister the device
                        try {
                            Backendless.Messaging.unregisterDevice();
                        } catch (Exception ex) {
                            LogMy.e(TAG,"Exception during device deregistration: "+ex.toString());
                            LogMy.e(TAG,"Failed to deregister the device: "+devId);
                        }
                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                LogMy.e(TAG,"Failed to receive Device registration data: "+fault.toString());
            }
        });*/


        /*String newDevId = null;
        try {
            newDevId = Backendless.Messaging.getDeviceRegistration().getDeviceId();
            if(newDevId==null || newDevId.isEmpty()) {
                LogMy.d(TAG, "Msg Device Id not available");
                return;
            }

            LogMy.d(TAG, "Trying to update customer object for deviceId: " + newDevId);
            mInstance.mCustomer.setMsgDevId(newDevId);
            mInstance.mCustomer = Backendless.Persistence.save(mInstance.mCustomer);
            LogMy.d(TAG, "Updated device registration Id: " + mInstance.mCustomer.getMsgDevId());

        } catch (Exception e) {
            LogMy.e(TAG,"Failed to update customer object for msgDevId: "+e.toString());
            if(newDevId!=null) {
                // error after device registration - deregister the device
                try {
                    Backendless.Messaging.unregisterDevice();
                } catch (Exception ex) {
                    LogMy.e(TAG,"Exception during device deregistration: "+ex.toString());
                    LogMy.e(TAG,"Failed to deregister the device: "+newDevId);
                }
            }
        }*/
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

    public boolean isChkMsgDevReg() {
        return mChkMsgDevReg;
    }

    public synchronized void setChkMsgDevReg(boolean chkMsgDevReg) {
        this.mChkMsgDevReg = chkMsgDevReg;
    }

    public List<CustAddress> getAddresses() {
        return mAddresses;
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

        return AppCommonUtil.loadGlobalSettings(MyGlobalSettings.RunMode.appCustomer);
        //return ErrorCodes.NO_ERROR;
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
        // 'addresses' field is not actually stored in DB -a nd only used as transport during login
        if(mCustomer.getAddresses()!=null) {
            mAddresses = mCustomer.getAddresses();
            mCustomer.setAddresses(null);
        } else {
            LogMy.d(TAG,"Customer addresses are null");
        }

        // Set user id for crashlytics
        if(AppConstants.USE_CRASHLYTICS) {
            Crashlytics.setUserIdentifier(mCustomer.getPrivate_id());
        }
    }
}
