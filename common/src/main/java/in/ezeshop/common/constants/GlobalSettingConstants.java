package in.ezeshop.common.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by adgangwa on 21-12-2016.
 */
public class GlobalSettingConstants {

    private static final int TOTAL_SETTINGS_CNT = 40;
    /*
     * Keys of all GlobalSettings
     */
    public static final String SETTINGS_MERCHANT_PASSWD_RESET_MINS = "Mchnt_Password_Reset_Mins";
    public static final String SETTINGS_CUSTOMER_PASSWD_RESET_MINS = "Cust_Password_Reset_Mins";
    public static final String SETTINGS_MERCHANT_ACCOUNT_BLOCK_MINS = "Mchnt_Acc_Locked_Mins";
    public static final String SETTINGS_CUSTOMER_ACCOUNT_BLOCK_MINS = "Cust_Acc_Locked_Mins";
    //public static final String SETTINGS_CB_REDEEM_CARD_REQ = "CB_Redeem_Card_Required";
    //public static final String SETTINGS_ACC_DB_CARD_REQ = "AC_Debit_Card_Required";
    public static final String SETTINGS_CL_CREDIT_LIMIT_FOR_PIN = "AC_Credit_Limit_For_PIN";
    public static final String SETTINGS_CL_DEBIT_LIMIT_FOR_PIN = "AC_Debit_Limit_For_PIN";
    public static final String SETTINGS_CB_DEBIT_LIMIT_FOR_PIN = "CB_Debit_Limit_For_PIN";
    public static final String SETTINGS_STATS_NO_REFRESH_MINS = "Mchnt_Stats_Refresh_Mins";
    //public static final String SETTINGS_CUSTOMER_NO_REFRESH_MINS = "Cust_Data_Refresh_Mins";
    public static final String SETTINGS_CUSTOMER_CASH_LIMIT = "AC_Max_Cash_Limit";
    public static final String SETTINGS_CUSTOMER_OVERDRAFT_LIMIT = "AC_Max_Overdraft_Limit";
    public static final String SETTINGS_MCHNT_REMOVAL_EXPIRY_DAYS = "Mchnt_Removal_Expiry_Days";
    public static final String SETTINGS_CUST_ACC_LIMIT_MODE_MINS = "Cust_Limited_Mode_Mins";
    public static final String SETTINGS_WRONG_ATTEMPT_RESET_MINS = "Wrong_Verify_Reset_Mins";
    // Txns older than this will be archived to files
    // This also means that txns older than this :
    // 1) Cannot be cancelled by Merchant
    // 2) Cannot be visible on main screen i.e. can be fetched on per Merchant basis only.
    public static final String SETTINGS_TXNS_INTABLE_KEEP_DAYS = "Txns_Recent_Days";

    public static final String SETTINGS_OPS_KEEP_DAYS = "Service_Req_Keep_Days";
    public static final String SETTINGS_OTP_VALID_MINS = "OTP_Valid_Mins";
    public static final String SETTINGS_MERCHANT_WRONG_ATTEMPT_LIMIT = "Mchnt_Allowed_Wrong_Verify_Attempts";
    public static final String SETTINGS_CUSTOMER_WRONG_ATTEMPT_LIMIT = "Cust_Allowed_Wrong_Verify_Attempts";
    public static final String SETTINGS_MCHNT_RENEW_DURATION = "Mchnt_Renew_Months";
    public static final String SETTINGS_CUST_RENEW_DURATION = "Cust_Renew_Months";
    public static final String SETTINGS_MCHNT_TXN_HISTORY_DAYS = "Mchnt_Txn_History_Days";
    public static final String SETTINGS_CUST_TXN_HISTORY_DAYS = "Cust_Txn_History_Days";
    public static final String SETTINGS_CB_REDEEM_LIMIT = "CB_Redeem_Limit";
    public static final String SETTINGS_SERVICE_DISABLED_UNTIL = "Service_Disabled_Until";
    //public static final String SETTINGS_TXN_IMAGE_CAPTURE_MODE = "Txn_Image_Capture_Mode";
    public static final String SETTINGS_DAILY_DOWNTIME_START_HOUR = "Daily_Downtime_Start_Hour";
    public static final String SETTINGS_DAILY_DOWNTIME_END_HOUR = "Daily_Downtime_End_Hour";
    public static final String SETTINGS_SERVICE_NA_URL = "Service_NotAvailable_URL";
    public static final String SETTINGS_MCHNT_TERMS_URL = "Mchnt_Terms_Url";
    public static final String SETTINGS_CUST_TERMS_URL = "Cust_Terms_Url";
    //public static final String SETTINGS_CUST_CARD_PRICE = "Cust_Card_Price";
    //public static final String SETTINGS_CUST_CARD_MIN_QTY = "Cust_Card_Min_Qty";
    //public static final String SETTINGS_CUST_CARD_MAX_QTY = "Cust_Card_Max_Qty";
    public static final String SETTINGS_CONTACT_US_URL = "ContactUs_Url";
    //public static final String SETTINGS_CB_TXN_VERIFY_TYPE = "Cb_Txn_Verify_Method";
    public static final String SETTINGS_DELIVERY_CHARGES = "Delivery_Charges";
    public static final String SETTINGS_MCHNT_NEW_ORDER_TIMEOUT = "MchntNewOrderTimeout";
    public static final String SETTINGS_MCHNT_ACPTD_ORDER_TIMEOUT = "MchntAcptdOrderTimeout";
    public static final String SETTINGS_MCHNT_DSPTCHD_ORDER_TIMEOUT = "MchntDsptchdOrderTimeout";
    public static final String SETNGS_ORDER_TMOUT_NOTIFY_THRSHLD_PRCENT = "OrderTmoutNotifyThrsholdPrcent";

    /*
     * Ones defined only in backend as constant values - as not used by App
     * Thus they are more of Backend Constants - but still kept here
     */
    public static final int FAILED_SMS_RETRY_MINS = 30;

    // Below are not part of global settings, but keeping them here
    // as they depend on above 'passwd reset mins' values - keep the values 1/6th of them
    // TODO: Keep the 'backend passwd reset timer' duration (below defined) 1/6th of 'password reset mins' values
    // So, if Cool_off_mins is 60, then timer should run every 10 mins
    public static final int MERCHANT_PASSWORD_RESET_TIMER_INTERVAL = 10;
    public static final int CUSTOMER_PASSWORD_RESET_TIMER_INTERVAL = 10;

    /*
     * Map to Values
     */
    // 'txn_image_capture_mode' global setting values
    public static final int TXN_IMAGE_CAPTURE_ALWAYS = 0;
    // only when 'card is mandatory' based on txn type and amounts
    public static final int TXN_IMAGE_CAPTURE_CARD_REQUIRED = 1;
    public static final int TXN_IMAGE_CAPTURE_NEVER = 2;

    // SETTINGS_CB_TXN_VERIFY_TYPE possible values
    /*public static final int TXN_CB_VERIFY_CARD = 0; // card scan required
    public static final int TXN_CB_VERIFY_PIN = 1; // PIN required
    public static final int TXN_CB_VERIFY_OTP = 2; // OTP required
    public static final int TXN_CB_VERIFY_ANY_ONE = 3; // any one of card, PIN or OTP - ask user for it*/

    public static final Map<String, String> valuesGlobalSettings;
    static {
        Map<String, String> aMap = new HashMap<>(TOTAL_SETTINGS_CNT);
        aMap.put(SETTINGS_MERCHANT_PASSWD_RESET_MINS,"10");
        aMap.put(SETTINGS_CUSTOMER_PASSWD_RESET_MINS,"10");
        aMap.put(SETTINGS_MERCHANT_ACCOUNT_BLOCK_MINS,"60");
        aMap.put(SETTINGS_CUSTOMER_ACCOUNT_BLOCK_MINS,"60");
        aMap.put(SETTINGS_CUST_ACC_LIMIT_MODE_MINS,"60");
        aMap.put(SETTINGS_STATS_NO_REFRESH_MINS,"240");
        //aMap.put(SETTINGS_CUSTOMER_NO_REFRESH_MINS,"240");
        aMap.put(SETTINGS_WRONG_ATTEMPT_RESET_MINS,"120");

        //aMap.put(SETTINGS_CB_REDEEM_CARD_REQ,"false");
        //aMap.put(SETTINGS_ACC_DB_CARD_REQ,"false");
        // Keeping this same as 'SETTINGS_CUSTOMER_CASH_LIMIT'
        // which effectively means - PIN will never be required for credit
        aMap.put(SETTINGS_CB_REDEEM_LIMIT,"200");
        aMap.put(SETTINGS_CUSTOMER_CASH_LIMIT,"4000");
        aMap.put(SETTINGS_CUSTOMER_OVERDRAFT_LIMIT,"4000");
        aMap.put(SETTINGS_CL_CREDIT_LIMIT_FOR_PIN,"4000");
        aMap.put(SETTINGS_CL_DEBIT_LIMIT_FOR_PIN,"0");
        aMap.put(SETTINGS_CB_DEBIT_LIMIT_FOR_PIN,"0");

        aMap.put(SETTINGS_MCHNT_REMOVAL_EXPIRY_DAYS,"30");
        aMap.put(SETTINGS_TXNS_INTABLE_KEEP_DAYS,"7");
        aMap.put(SETTINGS_OPS_KEEP_DAYS,"90");
        aMap.put(SETTINGS_OTP_VALID_MINS,"15");
        aMap.put(SETTINGS_MERCHANT_WRONG_ATTEMPT_LIMIT,"5");
        aMap.put(SETTINGS_CUSTOMER_WRONG_ATTEMPT_LIMIT,"5");
        aMap.put(SETTINGS_MCHNT_RENEW_DURATION,"12");
        aMap.put(SETTINGS_CUST_RENEW_DURATION,"12");
        aMap.put(SETTINGS_MCHNT_TXN_HISTORY_DAYS,"90");
        aMap.put(SETTINGS_CUST_TXN_HISTORY_DAYS,"90");
        aMap.put(SETTINGS_SERVICE_DISABLED_UNTIL,null);
        //aMap.put(SETTINGS_TXN_IMAGE_CAPTURE_MODE,String.valueOf(TXN_IMAGE_CAPTURE_CARD_REQUIRED));
        aMap.put(SETTINGS_DAILY_DOWNTIME_START_HOUR,"0");
        aMap.put(SETTINGS_DAILY_DOWNTIME_END_HOUR,"0");
        //URL to which App will redirect if backend is not available
        aMap.put(SETTINGS_SERVICE_NA_URL,"http://www.myecash.in/back-soon");
        aMap.put(SETTINGS_MCHNT_TERMS_URL, "http://www.myecash.in/terms");
        aMap.put(SETTINGS_CUST_TERMS_URL, "http://www.myecash.in/terms#2");
        //aMap.put(SETTINGS_CUST_CARD_PRICE, "35");
        //aMap.put(SETTINGS_CUST_CARD_MIN_QTY, "10");
        //aMap.put(SETTINGS_CUST_CARD_MAX_QTY, "50");
        aMap.put(SETTINGS_CONTACT_US_URL, "http://www.myecash.in/contact-us");
        aMap.put(SETTINGS_DELIVERY_CHARGES, "30");
        aMap.put(SETTINGS_MCHNT_NEW_ORDER_TIMEOUT,"2");
        aMap.put(SETTINGS_MCHNT_ACPTD_ORDER_TIMEOUT,"6");
        aMap.put(SETTINGS_MCHNT_DSPTCHD_ORDER_TIMEOUT,"12");
        aMap.put(SETNGS_ORDER_TMOUT_NOTIFY_THRSHLD_PRCENT,"20");
        valuesGlobalSettings = Collections.unmodifiableMap(aMap);
    }
    /*static {
        Map<String, String> aMap = new HashMap<>(TOTAL_SETTINGS_CNT);
        aMap.put(SETTINGS_MERCHANT_PASSWD_RESET_MINS,"7");
        aMap.put(SETTINGS_CUSTOMER_PASSWD_RESET_MINS,"5");
        aMap.put(SETTINGS_MERCHANT_ACCOUNT_BLOCK_MINS,"6");
        aMap.put(SETTINGS_CUSTOMER_ACCOUNT_BLOCK_MINS,"15");
        aMap.put(SETTINGS_CUST_ACC_LIMIT_MODE_MINS,"5");
        aMap.put(SETTINGS_STATS_NO_REFRESH_MINS,"8");
        aMap.put(SETTINGS_CUSTOMER_NO_REFRESH_MINS,"5");
        aMap.put(SETTINGS_WRONG_ATTEMPT_RESET_MINS,"120");

        aMap.put(SETTINGS_CB_REDEEM_CARD_REQ,"true");
        aMap.put(SETTINGS_ACC_DB_CARD_REQ,"true");
        aMap.put(SETTINGS_CL_CREDIT_LIMIT_FOR_PIN,"500");
        aMap.put(SETTINGS_CL_DEBIT_LIMIT_FOR_PIN,"45");
        aMap.put(SETTINGS_CB_DEBIT_LIMIT_FOR_PIN,"25");
        aMap.put(SETTINGS_CB_REDEEM_LIMIT,"100");
        aMap.put(SETTINGS_CUSTOMER_CASH_LIMIT,"6000");

        aMap.put(SETTINGS_MCHNT_REMOVAL_EXPIRY_DAYS,"30");
        aMap.put(SETTINGS_TXNS_INTABLE_KEEP_DAYS,"10");
        aMap.put(SETTINGS_OPS_KEEP_DAYS,"2");
        aMap.put(SETTINGS_OTP_VALID_MINS,"5");
        aMap.put(SETTINGS_MERCHANT_WRONG_ATTEMPT_LIMIT,"8");
        aMap.put(SETTINGS_CUSTOMER_WRONG_ATTEMPT_LIMIT,"8");
        aMap.put(SETTINGS_MCHNT_RENEW_DURATION,"12");
        aMap.put(SETTINGS_CUST_RENEW_DURATION,"12");
        aMap.put(SETTINGS_MCHNT_TXN_HISTORY_DAYS,"2");
        aMap.put(SETTINGS_CUST_TXN_HISTORY_DAYS,"2");
        aMap.put(SETTINGS_SERVICE_DISABLED_UNTIL,null);
        aMap.put(SETTINGS_TXN_IMAGE_CAPTURE_MODE,String.valueOf(TXN_IMAGE_CAPTURE_CARD_REQUIRED));
        aMap.put(SETTINGS_DAILY_DOWNTIME_START_HOUR,"5");
        aMap.put(SETTINGS_DAILY_DOWNTIME_END_HOUR,"6");
        //URL to which App will redirect if backend is not available
        aMap.put(SETTINGS_SERVICE_NA_URL,"http://www.whysotechnologies.com/backsoon");
        valuesGlobalSettings = Collections.unmodifiableMap(aMap);
    }*/

    /*
     * Map to Type of Description
     */
    public static final Map<String, String> descGlobalSettings;
    static {
        Map<String, String> aMap = new HashMap<>(TOTAL_SETTINGS_CNT);
        aMap.put(SETTINGS_MERCHANT_PASSWD_RESET_MINS, "Merchant: Minutes after which new password is sent on reset.");
        aMap.put(SETTINGS_CUSTOMER_PASSWD_RESET_MINS, "Customer: Minutes after which new password is sent on reset.");
        aMap.put(SETTINGS_MERCHANT_ACCOUNT_BLOCK_MINS, "Merchant: Minutes for which account is kept Locked. Enabled automatically after this.");
        aMap.put(SETTINGS_CUSTOMER_ACCOUNT_BLOCK_MINS, "Merchant: Minutes for which account is kept Locked. Enabled automatically after this.");
        //aMap.put(SETTINGS_CB_REDEEM_CARD_REQ, "If Customer Card Mandatory to be scanned, for Cashback redeem ?");
        //aMap.put(SETTINGS_ACC_DB_CARD_REQ, "If Customer Card Mandatory to be scanned, for Account debit ?");
        aMap.put(SETTINGS_CL_CREDIT_LIMIT_FOR_PIN, "Account Credit: Amount over which Customer PIN is asked during txn.");
        aMap.put(SETTINGS_CL_DEBIT_LIMIT_FOR_PIN, "Account Debit: Amount over which Customer PIN is asked during txn.");
        aMap.put(SETTINGS_CB_DEBIT_LIMIT_FOR_PIN, "Cashback Redeem: Amount over which Customer PIN is asked during txn.");
        aMap.put(SETTINGS_STATS_NO_REFRESH_MINS, "Dashboard and Customer List in 'Merchant App' will be refreshed only once in this duration.");
        //aMap.put(SETTINGS_CUSTOMER_NO_REFRESH_MINS, "Merchant List in the 'Customer App' will be refreshed only once in this duration.");
        aMap.put(SETTINGS_CUSTOMER_CASH_LIMIT, "Customer: Maximum amount that can be kept in any account of single merchant.");
        aMap.put(SETTINGS_CUSTOMER_OVERDRAFT_LIMIT, "Customer: Maximum amount that can be overdraft from any account of single merchant.");
        aMap.put(SETTINGS_MCHNT_REMOVAL_EXPIRY_DAYS, "Notice Period for Merchant, if he decides to leave MyeCash program. No Credit Txns will be allowed in this duration.");
        aMap.put(SETTINGS_CUST_ACC_LIMIT_MODE_MINS, "Customer: Minutes for which account is kept in Limited Mode. Enabled automatically after this.");
        aMap.put(SETTINGS_WRONG_ATTEMPT_RESET_MINS, "Number of minutes after which 'wrong attempts' count is reset.");
        aMap.put(SETTINGS_TXNS_INTABLE_KEEP_DAYS, "Transactions can be cancelled within this many days.");
        aMap.put(SETTINGS_OPS_KEEP_DAYS, "Service Requests for any account older than these many days are Purged.");
        aMap.put(SETTINGS_OTP_VALID_MINS, "Time for which any sent OTP is valid.");
        aMap.put(SETTINGS_MERCHANT_WRONG_ATTEMPT_LIMIT, "Merchant: Number of wrong repeated verifications allowed. Account gets Locked after this.");
        aMap.put(SETTINGS_CUSTOMER_WRONG_ATTEMPT_LIMIT, "Customer: Number of wrong repeated verifications allowed. Account gets Locked after this.");
        aMap.put(SETTINGS_MCHNT_RENEW_DURATION, null);
        aMap.put(SETTINGS_CUST_RENEW_DURATION, null);
        aMap.put(SETTINGS_MCHNT_TXN_HISTORY_DAYS, "Merchant: Txns older than this cannot be viewed in App.");
        aMap.put(SETTINGS_CUST_TXN_HISTORY_DAYS, "Customer: Txns older than this cannot be viewed in App, for any particular merchant.");
        aMap.put(SETTINGS_CB_REDEEM_LIMIT, "Customer: Minimum Cashback amount for particular merchant, after which only it can be redeemed.");
        aMap.put(SETTINGS_SERVICE_DISABLED_UNTIL, "Exact time till which Service is Disabled.");
        //aMap.put(SETTINGS_TXN_IMAGE_CAPTURE_MODE, null);
        aMap.put(SETTINGS_DAILY_DOWNTIME_START_HOUR, "Start Hour for Daily service downtime (24 hour format).");
        aMap.put(SETTINGS_DAILY_DOWNTIME_END_HOUR, "End Hour for Daily service downtime (24 hour format).");
        aMap.put(SETTINGS_SERVICE_NA_URL, null);
        aMap.put(SETTINGS_MCHNT_TERMS_URL, null);
        aMap.put(SETTINGS_CUST_TERMS_URL, null);

        aMap.put(SETTINGS_DELIVERY_CHARGES, "Delivery charges for online order to merchants");
        aMap.put(SETTINGS_MCHNT_NEW_ORDER_TIMEOUT,"Duration in hours after which, the 'New' order if not accepted by Merchant, will be automatically get Cancelled");
        aMap.put(SETTINGS_MCHNT_ACPTD_ORDER_TIMEOUT,"Duration in hours after which, the 'Accepted' order if not dispatched by Merchant, will be automatically get Cancelled");
        aMap.put(SETTINGS_MCHNT_DSPTCHD_ORDER_TIMEOUT,"Duration in hours, after which the 'Dispatched' order if not delivered by Merchant, will be assumed to be delivered and get closed.");
        aMap.put(SETNGS_ORDER_TMOUT_NOTIFY_THRSHLD_PRCENT,null);
        //aMap.put(SETTINGS_CUST_CARD_PRICE, null);
        //aMap.put(SETTINGS_CUST_CARD_MIN_QTY, null);
        //aMap.put(SETTINGS_CUST_CARD_MAX_QTY, null);
        //aMap.put(SETTINGS_CONTACT_US_URL, null);
        descGlobalSettings = Collections.unmodifiableMap(aMap);
    }

    /*
     * Map to Type of Values
     */
    // 'value_datatype' column values
    public static final int DATATYPE_INT = 1;
    public static final int DATATYPE_BOOLEAN = 2;
    public static final int DATATYPE_STRING = 3;
    public static final int DATATYPE_DATE = 4;

    public static final Map<String, Integer> valueTypesGlobalSettings;
    static {
        Map<String, Integer> aMap = new HashMap<>(TOTAL_SETTINGS_CNT);
        aMap.put(SETTINGS_MERCHANT_PASSWD_RESET_MINS, DATATYPE_INT);
        aMap.put(SETTINGS_CUSTOMER_PASSWD_RESET_MINS, DATATYPE_INT);
        aMap.put(SETTINGS_MERCHANT_ACCOUNT_BLOCK_MINS, DATATYPE_INT);
        aMap.put(SETTINGS_CUSTOMER_ACCOUNT_BLOCK_MINS, DATATYPE_INT);
        //aMap.put(SETTINGS_CB_REDEEM_CARD_REQ, DATATYPE_BOOLEAN);
        //aMap.put(SETTINGS_ACC_DB_CARD_REQ, DATATYPE_BOOLEAN);
        aMap.put(SETTINGS_CL_CREDIT_LIMIT_FOR_PIN, DATATYPE_INT);
        aMap.put(SETTINGS_CL_DEBIT_LIMIT_FOR_PIN, DATATYPE_INT);
        aMap.put(SETTINGS_CB_DEBIT_LIMIT_FOR_PIN, DATATYPE_INT);
        aMap.put(SETTINGS_STATS_NO_REFRESH_MINS, DATATYPE_INT);
        aMap.put(SETTINGS_CUSTOMER_CASH_LIMIT, DATATYPE_INT);
        aMap.put(SETTINGS_CUSTOMER_OVERDRAFT_LIMIT, DATATYPE_INT);
        aMap.put(SETTINGS_MCHNT_REMOVAL_EXPIRY_DAYS, DATATYPE_INT);
        aMap.put(SETTINGS_CUST_ACC_LIMIT_MODE_MINS, DATATYPE_INT);
        aMap.put(SETTINGS_WRONG_ATTEMPT_RESET_MINS, DATATYPE_INT);
        aMap.put(SETTINGS_TXNS_INTABLE_KEEP_DAYS, DATATYPE_INT);
        aMap.put(SETTINGS_OPS_KEEP_DAYS, DATATYPE_INT);
        aMap.put(SETTINGS_OTP_VALID_MINS, DATATYPE_INT);
        aMap.put(SETTINGS_MERCHANT_WRONG_ATTEMPT_LIMIT, DATATYPE_INT);
        aMap.put(SETTINGS_CUSTOMER_WRONG_ATTEMPT_LIMIT, DATATYPE_INT);
        aMap.put(SETTINGS_MCHNT_RENEW_DURATION, DATATYPE_INT);
        aMap.put(SETTINGS_CUST_RENEW_DURATION, DATATYPE_INT);
        aMap.put(SETTINGS_MCHNT_TXN_HISTORY_DAYS, DATATYPE_INT);
        aMap.put(SETTINGS_CUST_TXN_HISTORY_DAYS, DATATYPE_INT);
        //aMap.put(SETTINGS_CUSTOMER_NO_REFRESH_MINS, DATATYPE_INT);
        aMap.put(SETTINGS_CB_REDEEM_LIMIT, DATATYPE_INT);
        aMap.put(SETTINGS_SERVICE_DISABLED_UNTIL, DATATYPE_DATE);
        //aMap.put(SETTINGS_TXN_IMAGE_CAPTURE_MODE, DATATYPE_INT);
        aMap.put(SETTINGS_DAILY_DOWNTIME_START_HOUR, DATATYPE_INT);
        aMap.put(SETTINGS_DAILY_DOWNTIME_END_HOUR, DATATYPE_INT);
        aMap.put(SETTINGS_SERVICE_NA_URL, DATATYPE_STRING);
        aMap.put(SETTINGS_MCHNT_TERMS_URL, DATATYPE_STRING);
        aMap.put(SETTINGS_CUST_TERMS_URL, DATATYPE_STRING);
        //aMap.put(SETTINGS_CUST_CARD_PRICE, DATATYPE_INT);
        //aMap.put(SETTINGS_CUST_CARD_MIN_QTY, DATATYPE_INT);
        //aMap.put(SETTINGS_CUST_CARD_MAX_QTY, DATATYPE_INT);
        aMap.put(SETTINGS_CONTACT_US_URL, DATATYPE_STRING);
        aMap.put(SETTINGS_DELIVERY_CHARGES, DATATYPE_INT);
        aMap.put(SETTINGS_MCHNT_NEW_ORDER_TIMEOUT,DATATYPE_INT);
        aMap.put(SETTINGS_MCHNT_ACPTD_ORDER_TIMEOUT,DATATYPE_INT);
        aMap.put(SETTINGS_MCHNT_DSPTCHD_ORDER_TIMEOUT,DATATYPE_INT);
        aMap.put(SETNGS_ORDER_TMOUT_NOTIFY_THRSHLD_PRCENT,DATATYPE_INT);
        valueTypesGlobalSettings = Collections.unmodifiableMap(aMap);
    }
}
