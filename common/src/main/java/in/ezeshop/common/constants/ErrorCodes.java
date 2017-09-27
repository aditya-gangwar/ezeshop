package in.ezeshop.common.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by adgangwa on 19-02-2016.
 */
public class ErrorCodes {

    /*
     * Use Error Codes always of 3 digits only i.e. from 100 - 999.
     */
    public static final int ERROR_DIGITS = 3;
    public static final int ERROR_MAX_CNT = 999;

    // Non error responses
    public static final int NO_ERROR = 100;
    public static final int OP_SCHEDULED = 102;
    public static final int OTP_GENERATED = 103;

    // Generic error code
    public static final int GENERAL_ERROR = 200;

    // User and account related error codes
    public static final int USER_ALREADY_LOGGED_IN = 500;
    public static final int NO_SUCH_USER = 501;
    public static final int USER_ACC_DISABLED = 502;
    public static final int USER_ACC_LOCKED = 503;
    public static final int FIRST_LOGIN_PENDING = 504;
    public static final int NOT_LOGGED_IN = 505;
    public static final int CUST_NOT_REG_WITH_MCNT = 506;
    public static final int ACC_UNDER_EXPIRY = 507;
    public static final int USER_WRONG_ID_PASSWD = 508;
    public static final int FATAL_ERROR_ACC_DISABLED = 509;
    public static final int USER_ALREADY_REGISTERED = 510;
    public static final int LIMITED_ACCESS_CREDIT_TXN_ONLY = 511;
    public static final int SESSION_TIMEOUT = 512;

    // Membership card errors
    public static final int NO_SUCH_CARD = 530;
    public static final int WRONG_CARD = 531;
    public static final int CARD_ALREADY_IN_USE = 532;
    public static final int CARD_DISABLED = 533;
    public static final int CARD_NOT_REG_WITH_CUST = 534;
    public static final int CARD_WRONG_OWNER_MCHNT = 535;

    // Limit based errors
    public static final int FAILED_ATTEMPT_LIMIT_RCHD = 540;
    public static final int TRUSTED_DEVICE_LIMIT_RCHD = 541;
    public static final int CASH_ACCOUNT_LIMIT_RCHD = 542;
    public static final int ACCOUNT_NOT_ENUF_BALANCE = 543;
    public static final int CB_NOT_ENUF_BALANCE = 544;

    // Format related errors
    public static final int EMPTY_VALUE = 550;
    public static final int INVALID_FORMAT = 551;
    public static final int INVALID_FORMAT_COMMA = 552;
    public static final int INVALID_FORMAT_ZERO = 553;
    public static final int INVALID_LENGTH = 554;
    public static final int INVALID_VALUE = 555;
    public static final int NO_DATA_FOUND = 556;
    public static final int INVALID_PASSWD_FORMAT = 557;

    // Any of sub backend operations failed
    public static final int SEND_SMS_FAILED = 560;
    public static final int OTP_GENERATE_FAILED = 562;

    // Input / Verification data or permissions failure
    public static final int WRONG_INPUT_DATA = 580;
    public static final int WRONG_OTP = 581;
    public static final int WRONG_PIN = 582;
    public static final int VERIFICATION_FAILED_PASSWD = 583;
    public static final int VERIFICATION_FAILED_NAME = 584;
    public static final int VERIFICATION_FAILED_DOB = 585;
    public static final int VERIFICATION_FAILED_MOBILE = 586;
    public static final int OPERATION_NOT_ALLOWED = 587;
    public static final int NOT_TRUSTED_DEVICE = 588;
    public static final int TEMP_PASSWD_EXPIRED = 589;
    public static final int WRONG_USER_TYPE = 590;
    public static final int DEVICE_INSECURE = 591;
    public static final int LOGGED_IN_DEVICE_DELETE = 592;
    public static final int WRNG_PSWD_NOT_TRUSTED_DEV = 593;
    public static final int DEV_NOT_REG_FOR_MSGING = 594;

    // Misc errors
    public static final int DUPLICATE_ENTRY = 661;
    public static final int DEVICE_ALREADY_REGISTERED = 662;
    public static final int MERCHANT_ID_RANGE_ERROR = 663;
    public static final int NO_INTERNET_CONNECTION = 664;
    public static final int FILE_UPLOAD_FAILED = 665;
    public static final int FILE_NOT_FOUND = 666;
    public static final int SERVICE_GLOBAL_DISABLED = 667;
    public static final int REMOTE_SERVICE_NOT_AVAILABLE = 668;
    public static final int MOBILE_ALREADY_REGISTERED = 669;
    public static final int UNDER_DAILY_DOWNTIME = 670;
    public static final int INTERNET_OK_SERVICE_NOK = 671;
    public static final int MCHNT_ORDER_FREE_CARDS = 672;
    public static final int MCHNT_ORDER_ALLOT_CARDS = 673;

    // Merchnat order errors
    public static final int MO_DEL_INVALID_STATUS = 771;


    // *******************************************************************
    // IT IS MANDATORY THAT ALL ERROR CODES ABOVE ARE ADDED TO BELOW MAP
    // EVEN IF WITH EMPTY STRING
    // *******************************************************************
    public static final Map<Integer, String> appErrorDesc;
    static {
        Map<Integer, String> aMap = new HashMap<>(100);

        aMap.put(NO_ERROR, "All is well");
        aMap.put(OP_SCHEDULED,"");
        aMap.put(OTP_GENERATED,"OTP sent on mobile number. Do the operation again with OTP value.");

        aMap.put(GENERAL_ERROR, "System Error. Please try again later. \n Please contact customer care if issue persists.");

        aMap.put(USER_ALREADY_LOGGED_IN, "User is already logged in (may be from other device). Please logout first.");
        aMap.put(NO_SUCH_USER,"User is not registered. Please register first and then try again.");
        aMap.put(USER_ACC_DISABLED, "User account is disabled. Please contact customer care.");
        aMap.put(USER_ACC_LOCKED,"User account is temporarily locked.");
        aMap.put(FIRST_LOGIN_PENDING, "New user. Please use forgot password link to generate new password.");
        aMap.put(NOT_LOGGED_IN,"You are not logged in, or logged in from other device. \nPlease login again to proceed.");
        aMap.put(CUST_NOT_REG_WITH_MCNT,"Customer has done no transaction with the merchant.");
        aMap.put(ACC_UNDER_EXPIRY,"Account under Expiry duration");
        aMap.put(USER_WRONG_ID_PASSWD, "Wrong User id or Password. \nAccount gets Locked after %s incorrect attempts.");
        aMap.put(FATAL_ERROR_ACC_DISABLED,"User account disabled temporarily by system for safety purpose. You will receive notification from customer care in next 24-48 hours.");
        aMap.put(USER_ALREADY_REGISTERED, "User is already registered");
        aMap.put(LIMITED_ACCESS_CREDIT_TXN_ONLY,"Limited Access. Only 'CREDIT' transactions are allowed.");
        aMap.put(SESSION_TIMEOUT,"Your session has timed out due to inactivity. \nPlease login again to proceed.");

        aMap.put(SEND_SMS_FAILED,"Sorry, but we failed to send SMS to you. Request to please try again later.");
        aMap.put(OTP_GENERATE_FAILED,"Failed to generate OTP. Please try again later.");

        aMap.put(WRONG_INPUT_DATA,"Invalid input data");
        aMap.put(WRONG_OTP,"Wrong OTP value.");
        aMap.put(WRONG_PIN,"Wrong PIN. Account gets Locked after %s incorrect attempts.");
        aMap.put(VERIFICATION_FAILED_NAME,"Wrong Customer Name. Account gets Locked after %s incorrect attempts.");
        aMap.put(VERIFICATION_FAILED_DOB,"Wrong Date of Birth. Account gets Locked after %s incorrect attempts.");
        aMap.put(VERIFICATION_FAILED_MOBILE,"Wrong Mobile Number. Account gets Locked after %s incorrect attempts.");
        aMap.put(VERIFICATION_FAILED_PASSWD,"Wrong password. Account gets Locked after %s incorrect attempts.");
        aMap.put(OPERATION_NOT_ALLOWED,"You do not have permissions for this operation");
        aMap.put(NOT_TRUSTED_DEVICE,"This device is not in trusted device list");
        aMap.put(TEMP_PASSWD_EXPIRED,"Temporary password expired. Please generate new password using 'Forget Password' link on login screen.");
        aMap.put(WRONG_USER_TYPE,"Wrong user type");
        aMap.put(DEVICE_INSECURE,"Your device is not secure. Please install and run application on other device.");
        aMap.put(LOGGED_IN_DEVICE_DELETE,"You cannot delete device from which you are logged in.");
        aMap.put(WRNG_PSWD_NOT_TRUSTED_DEV,"Not Trusted Device. Please enter correct User ID/Password to add in Trusted device list.");
        aMap.put(DEV_NOT_REG_FOR_MSGING,"");

        aMap.put(NO_SUCH_CARD,"Invalid MyeCash Customer Card");
        aMap.put(WRONG_CARD,"Wrong Customer Card Used.");
        aMap.put(CARD_ALREADY_IN_USE,"This card is already registered to customer");
        aMap.put(CARD_DISABLED,"This Membership card is Disabled and cannot be used");
        aMap.put(CARD_NOT_REG_WITH_CUST,"This Membership card is not registered to any Customer");
        aMap.put(CARD_WRONG_OWNER_MCHNT,"This Membership card is not allocated to you as Merchant. Please return to company agent.");

        aMap.put(FAILED_ATTEMPT_LIMIT_RCHD,"Failed attempt limit reached. Account is Locked for next %s minutes.");
        aMap.put(TRUSTED_DEVICE_LIMIT_RCHD,"Trusted device limit reached. To continue, login from any trusted device and delete any from the trusted devices.");
        aMap.put(CASH_ACCOUNT_LIMIT_RCHD,"Cash Account balance more than INR %s. Change 'Cash Paid'.");
        aMap.put(ACCOUNT_NOT_ENUF_BALANCE,"Not enough Cash Account balance available");
        aMap.put(CB_NOT_ENUF_BALANCE,"Not enough Cashback balance available");


        aMap.put(EMPTY_VALUE,"Empty input value");
        aMap.put(INVALID_FORMAT,"Invalid format");
        aMap.put(INVALID_FORMAT_COMMA,"Comma(,) is not allowed.");
        aMap.put(INVALID_FORMAT_ZERO,"Wrong format. 0 is not allowed.");
        aMap.put(INVALID_LENGTH,"Invalid length");
        aMap.put(INVALID_VALUE,"Wrong input values");
        aMap.put(NO_DATA_FOUND,"No data found");
        aMap.put(INVALID_PASSWD_FORMAT,"Password should contain atleast 6 characters, and also any number.");

        aMap.put(DUPLICATE_ENTRY,"Duplicate entry. Data already exists.");
        aMap.put(DEVICE_ALREADY_REGISTERED,"Device already added to other Merchant. \nOne device can be added to only one merchant account.");
        aMap.put(MERCHANT_ID_RANGE_ERROR,"Issue with Merchant ID Range.");
        aMap.put(NO_INTERNET_CONNECTION,"No Internet Connection. \n\nPlease check Internet connectivity and try again.");
        aMap.put(FILE_UPLOAD_FAILED,"Failed to upload the file. Please try again later.");
        aMap.put(FILE_NOT_FOUND,"Requested data not available");
        aMap.put(SERVICE_GLOBAL_DISABLED,"Service under maintenance. Please try after ");
        aMap.put(REMOTE_SERVICE_NOT_AVAILABLE,"MyeCash Server not reachable. Please check Internet connection also.");
        aMap.put(MOBILE_ALREADY_REGISTERED,"Mobile Number is already registered for other user.");
        aMap.put(UNDER_DAILY_DOWNTIME,"Service not available daily between %s:00 and %s:00 hours.");
        aMap.put(INTERNET_OK_SERVICE_NOK,"MyeCash Server not reachable.\n\nSorry for Inconvenience.\n\nPlease try again after 1 hour. Thanks.");
        aMap.put(MCHNT_ORDER_FREE_CARDS,"Please free up allocated cards first");
        aMap.put(MCHNT_ORDER_ALLOT_CARDS,"Please allot all Cards first");

        aMap.put(MO_DEL_INVALID_STATUS,"In Process or Completed Order cannot be deleted.");

        appErrorDesc = Collections.unmodifiableMap(aMap);
    }

    // These are defined by backendless
    // These are mapped to appropriate local error codes
    public static final String BL_ERROR_ENTITY_WITH_ID_NOT_FOUND = "1000";
    public static final String BL_ERROR_NO_PERMISSIONS = "1011";
    public static final String BL_ERROR_NO_PERMISSIONS_1 = "1012";
    public static final String BL_ERROR_NO_PERMISSIONS_2 = "1013";
    public static final String BL_ERROR_NO_PERMISSIONS_3 = "1014";
    public static final String BL_ERROR_NO_PERMISSIONS_4 = "1134";
    public static final String BL_ERROR_NO_PERMISSIONS_5 = "2000";
    public static final String BL_ERROR_NO_PERMISSIONS_6 = "2003";
    public static final String BL_ERROR_NO_DATA_FOUND = "1009";
    public static final String BL_ERROR_NO_DATA_FOUND_1 = "1033";
    public static final String BL_ERROR_NO_DATA_FOUND_2 = "1034";
    public static final String BL_ERROR_NO_DATA_FOUND_3 = "1035";
    public static final String BL_ERROR_DUPLICATE_ENTRY = "1155";
    public static final String BL_ERROR_DUPLICATE_ENTRY_1 = "1101";
    public static final String BL_ERROR_DUPLICATE_ENTRY_2 = "8001";
    public static final String BL_ERROR_REGISTER_DUPLICATE = "3033";
    public static final String BL_ERROR_LOGIN_DISABLED = "3000";
    public static final String BL_ERROR_ALREADY_LOGGOED_IN = "3002";
    public static final String BL_ERROR_INVALID_ID_PASSWD = "3003";
    public static final String BL_ERROR_EMPTY_ID_PASSWD = "3006";
    public static final String BL_ERROR_ACCOUNT_LOCKED = "3036";
    public static final String BL_ERROR_MULTIPLE_LOGIN_LIMIT = "3044";
    public static final String BL_ERROR_SESSION_TIMEOUT_URL = "3048";
    public static final String BL_ERROR_SESSION_TIMEOUT = "3091";
    public static final String BL_ERROR_MSGING_UNKNOWN_DEV = "5000";


    // Map from backendless error codes to local error codes
    public static final Map<String, Integer> backendToLocalErrorCode;
    static {
        Map<String, Integer> map = new HashMap<>(50);

        // backendless expected error codes
        map.put(BL_ERROR_LOGIN_DISABLED, USER_ACC_DISABLED);
        map.put(BL_ERROR_ALREADY_LOGGOED_IN, USER_ALREADY_LOGGED_IN);
        map.put(BL_ERROR_MULTIPLE_LOGIN_LIMIT, USER_ALREADY_LOGGED_IN);
        map.put(BL_ERROR_INVALID_ID_PASSWD, USER_WRONG_ID_PASSWD);
        map.put(BL_ERROR_EMPTY_ID_PASSWD, USER_WRONG_ID_PASSWD);
        map.put(BL_ERROR_ACCOUNT_LOCKED, USER_ACC_LOCKED);
        map.put(BL_ERROR_DUPLICATE_ENTRY, DUPLICATE_ENTRY);
        map.put(BL_ERROR_DUPLICATE_ENTRY_1, DUPLICATE_ENTRY);
        map.put(BL_ERROR_DUPLICATE_ENTRY_2, DUPLICATE_ENTRY);
        map.put(BL_ERROR_REGISTER_DUPLICATE, USER_ALREADY_REGISTERED);
        map.put(BL_ERROR_NO_PERMISSIONS, OPERATION_NOT_ALLOWED);
        map.put(BL_ERROR_NO_PERMISSIONS_1, OPERATION_NOT_ALLOWED);
        map.put(BL_ERROR_NO_PERMISSIONS_2, OPERATION_NOT_ALLOWED);
        map.put(BL_ERROR_NO_PERMISSIONS_3, OPERATION_NOT_ALLOWED);
        map.put(BL_ERROR_NO_PERMISSIONS_4, OPERATION_NOT_ALLOWED);
        map.put(BL_ERROR_NO_PERMISSIONS_5, OPERATION_NOT_ALLOWED);
        map.put(BL_ERROR_NO_PERMISSIONS_6, OPERATION_NOT_ALLOWED);
        map.put(BL_ERROR_NO_DATA_FOUND, NO_DATA_FOUND);
        map.put(BL_ERROR_NO_DATA_FOUND_1, NO_DATA_FOUND);
        map.put(BL_ERROR_NO_DATA_FOUND_2, NO_DATA_FOUND);
        map.put(BL_ERROR_NO_DATA_FOUND_3, NO_DATA_FOUND);
        map.put(BL_ERROR_SESSION_TIMEOUT_URL, SESSION_TIMEOUT);
        map.put(BL_ERROR_SESSION_TIMEOUT, SESSION_TIMEOUT);
        map.put(BL_ERROR_MSGING_UNKNOWN_DEV, DEV_NOT_REG_FOR_MSGING);

        backendToLocalErrorCode = Collections.unmodifiableMap(map);
    }


    // *******************************************************************
    // IT IS MANDATORY THAT ALL ERROR CODES ABOVE ARE ADDED TO BELOW MAP
    // *******************************************************************
    public static final Map<String, String> appErrorNames;
    static {
        Map<String, String> aMap = new HashMap<>(200);

        aMap.put(String.valueOf(NO_ERROR), "NO_ERROR");
        aMap.put(String.valueOf(OP_SCHEDULED),"OP_SCHEDULED");
        aMap.put(String.valueOf(OTP_GENERATED),"OTP_GENERATED");

        aMap.put(String.valueOf(GENERAL_ERROR),"GENERAL_ERROR");

        aMap.put(String.valueOf(USER_ALREADY_LOGGED_IN),"USER_ALREADY_LOGGED_IN");
        aMap.put(String.valueOf(NO_SUCH_USER),"NO_SUCH_USER");
        aMap.put(String.valueOf(USER_ACC_DISABLED),"USER_ACC_DISABLED");
        aMap.put(String.valueOf(USER_ACC_LOCKED),"USER_ACC_LOCKED");
        aMap.put(String.valueOf(FIRST_LOGIN_PENDING),"FIRST_LOGIN_PENDING");
        aMap.put(String.valueOf(NOT_LOGGED_IN),"NOT_LOGGED_IN");
        aMap.put(String.valueOf(CUST_NOT_REG_WITH_MCNT),"CUST_NOT_REG_WITH_MCNT");
        aMap.put(String.valueOf(ACC_UNDER_EXPIRY),"ACC_UNDER_EXPIRY");
        aMap.put(String.valueOf(USER_WRONG_ID_PASSWD),"USER_WRONG_ID_PASSWD");
        aMap.put(String.valueOf(FATAL_ERROR_ACC_DISABLED),"FATAL_ERROR_ACC_DISABLED");
        aMap.put(String.valueOf(USER_ALREADY_REGISTERED),"USER_ALREADY_REGISTERED");
        aMap.put(String.valueOf(LIMITED_ACCESS_CREDIT_TXN_ONLY),"LIMITED_ACCESS_CREDIT_TXN_ONLY");
        aMap.put(String.valueOf(SESSION_TIMEOUT),"SESSION_TIMEOUT");

        aMap.put(String.valueOf(SEND_SMS_FAILED),"SEND_SMS_FAILED");
        aMap.put(String.valueOf(OTP_GENERATE_FAILED),"OTP_GENERATE_FAILED");

        aMap.put(String.valueOf(WRONG_INPUT_DATA),"WRONG_INPUT_DATA");
        aMap.put(String.valueOf(WRONG_OTP),"WRONG_OTP");
        aMap.put(String.valueOf(WRONG_PIN),"WRONG_PIN");
        aMap.put(String.valueOf(VERIFICATION_FAILED_NAME),"VERIFICATION_FAILED_NAME");
        aMap.put(String.valueOf(VERIFICATION_FAILED_DOB),"VERIFICATION_FAILED_DOB");
        aMap.put(String.valueOf(VERIFICATION_FAILED_MOBILE),"VERIFICATION_FAILED_MOBILE");
        aMap.put(String.valueOf(VERIFICATION_FAILED_PASSWD),"VERIFICATION_FAILED_PASSWD");
        aMap.put(String.valueOf(OPERATION_NOT_ALLOWED),"OPERATION_NOT_ALLOWED");
        aMap.put(String.valueOf(NOT_TRUSTED_DEVICE),"NOT_TRUSTED_DEVICE");
        aMap.put(String.valueOf(TEMP_PASSWD_EXPIRED),"TEMP_PASSWD_EXPIRED");
        aMap.put(String.valueOf(WRONG_USER_TYPE),"WRONG_USER_TYPE");
        aMap.put(String.valueOf(DEVICE_INSECURE),"DEVICE_INSECURE");
        aMap.put(String.valueOf(LOGGED_IN_DEVICE_DELETE),"LOGGED_IN_DEVICE_DELETE");
        aMap.put(String.valueOf(WRNG_PSWD_NOT_TRUSTED_DEV),"WRNG_PSWD_NOT_TRUSTED_DEV");
        aMap.put(String.valueOf(DEV_NOT_REG_FOR_MSGING),"DEV_NOT_REG_FOR_MSGING");

        aMap.put(String.valueOf(NO_SUCH_CARD),"NO_SUCH_CARD");
        aMap.put(String.valueOf(WRONG_CARD),"WRONG_CARD");
        aMap.put(String.valueOf(CARD_ALREADY_IN_USE),"CARD_ALREADY_IN_USE");
        aMap.put(String.valueOf(CARD_DISABLED),"CARD_DISABLED");
        aMap.put(String.valueOf(CARD_NOT_REG_WITH_CUST),"CARD_NOT_REG_WITH_CUST");
        aMap.put(String.valueOf(CARD_WRONG_OWNER_MCHNT),"CARD_WRONG_OWNER_MCHNT");

        aMap.put(String.valueOf(FAILED_ATTEMPT_LIMIT_RCHD),"FAILED_ATTEMPT_LIMIT_RCHD");
        aMap.put(String.valueOf(TRUSTED_DEVICE_LIMIT_RCHD),"TRUSTED_DEVICE_LIMIT_RCHD");
        aMap.put(String.valueOf(CASH_ACCOUNT_LIMIT_RCHD),"CASH_ACCOUNT_LIMIT_RCHD");
        aMap.put(String.valueOf(ACCOUNT_NOT_ENUF_BALANCE),"ACCOUNT_NOT_ENUF_BALANCE");
        aMap.put(String.valueOf(CB_NOT_ENUF_BALANCE),"CB_NOT_ENUF_BALANCE");


        aMap.put(String.valueOf(EMPTY_VALUE),"EMPTY_VALUE");
        aMap.put(String.valueOf(INVALID_FORMAT),"INVALID_FORMAT");
        aMap.put(String.valueOf(INVALID_FORMAT_COMMA),"INVALID_FORMAT_COMMA");
        aMap.put(String.valueOf(INVALID_FORMAT_ZERO),"INVALID_FORMAT_ZERO");
        aMap.put(String.valueOf(INVALID_LENGTH),"INVALID_LENGTH");
        aMap.put(String.valueOf(INVALID_VALUE),"INVALID_VALUE");
        aMap.put(String.valueOf(NO_DATA_FOUND),"NO_DATA_FOUND");
        aMap.put(String.valueOf(INVALID_PASSWD_FORMAT),"INVALID_PASSWD_FORMAT");

        aMap.put(String.valueOf(DUPLICATE_ENTRY),"DUPLICATE_ENTRY");
        aMap.put(String.valueOf(DEVICE_ALREADY_REGISTERED),"DEVICE_ALREADY_REGISTERED");
        aMap.put(String.valueOf(MERCHANT_ID_RANGE_ERROR),"MERCHANT_ID_RANGE_ERROR");
        aMap.put(String.valueOf(NO_INTERNET_CONNECTION),"NO_INTERNET_CONNECTION");
        aMap.put(String.valueOf(FILE_UPLOAD_FAILED),"FILE_UPLOAD_FAILED");
        aMap.put(String.valueOf(FILE_NOT_FOUND),"FILE_NOT_FOUND");
        aMap.put(String.valueOf(SERVICE_GLOBAL_DISABLED),"SERVICE_GLOBAL_DISABLED");
        aMap.put(String.valueOf(REMOTE_SERVICE_NOT_AVAILABLE),"REMOTE_SERVICE_NOT_AVAILABLE");
        aMap.put(String.valueOf(MOBILE_ALREADY_REGISTERED),"MOBILE_ALREADY_REGISTERED");
        aMap.put(String.valueOf(UNDER_DAILY_DOWNTIME),"UNDER_DAILY_DOWNTIME");
        aMap.put(String.valueOf(INTERNET_OK_SERVICE_NOK),"INTERNET_OK_SERVICE_NOK");
        aMap.put(String.valueOf(MCHNT_ORDER_FREE_CARDS),"MCHNT_ORDER_FREE_CARDS");
        aMap.put(String.valueOf(MCHNT_ORDER_ALLOT_CARDS),"MCHNT_ORDER_ALLOT_CARDS");

        aMap.put(String.valueOf(MO_DEL_INVALID_STATUS),"MO_DEL_INVALID_STATUS");

        aMap.put(BL_ERROR_LOGIN_DISABLED, BL_ERROR_LOGIN_DISABLED);
        aMap.put(BL_ERROR_ALREADY_LOGGOED_IN, BL_ERROR_ALREADY_LOGGOED_IN);
        aMap.put(BL_ERROR_MULTIPLE_LOGIN_LIMIT, BL_ERROR_MULTIPLE_LOGIN_LIMIT);
        aMap.put(BL_ERROR_INVALID_ID_PASSWD, BL_ERROR_INVALID_ID_PASSWD);
        aMap.put(BL_ERROR_EMPTY_ID_PASSWD, BL_ERROR_EMPTY_ID_PASSWD);
        aMap.put(BL_ERROR_ACCOUNT_LOCKED, BL_ERROR_ACCOUNT_LOCKED);
        aMap.put(BL_ERROR_DUPLICATE_ENTRY, BL_ERROR_DUPLICATE_ENTRY);
        aMap.put(BL_ERROR_DUPLICATE_ENTRY_1, BL_ERROR_DUPLICATE_ENTRY_1);
        aMap.put(BL_ERROR_DUPLICATE_ENTRY_2, BL_ERROR_DUPLICATE_ENTRY_2);
        aMap.put(BL_ERROR_REGISTER_DUPLICATE, BL_ERROR_REGISTER_DUPLICATE);
        aMap.put(BL_ERROR_NO_PERMISSIONS, BL_ERROR_NO_PERMISSIONS);
        aMap.put(BL_ERROR_NO_PERMISSIONS_1, BL_ERROR_NO_PERMISSIONS_1);
        aMap.put(BL_ERROR_NO_PERMISSIONS_2, BL_ERROR_NO_PERMISSIONS_2);
        aMap.put(BL_ERROR_NO_PERMISSIONS_3, BL_ERROR_NO_PERMISSIONS_3);
        aMap.put(BL_ERROR_NO_PERMISSIONS_4, BL_ERROR_NO_PERMISSIONS_4);
        aMap.put(BL_ERROR_NO_PERMISSIONS_5, BL_ERROR_NO_PERMISSIONS_5);
        aMap.put(BL_ERROR_NO_PERMISSIONS_6, BL_ERROR_NO_PERMISSIONS_6);
        aMap.put(BL_ERROR_NO_DATA_FOUND, BL_ERROR_NO_DATA_FOUND);
        aMap.put(BL_ERROR_NO_DATA_FOUND_1, BL_ERROR_NO_DATA_FOUND_1);
        aMap.put(BL_ERROR_NO_DATA_FOUND_2, BL_ERROR_NO_DATA_FOUND_2);
        aMap.put(BL_ERROR_NO_DATA_FOUND_3, BL_ERROR_NO_DATA_FOUND_3);
        aMap.put(BL_ERROR_SESSION_TIMEOUT_URL, BL_ERROR_SESSION_TIMEOUT_URL);
        aMap.put(BL_ERROR_SESSION_TIMEOUT, BL_ERROR_SESSION_TIMEOUT);
        aMap.put(BL_ERROR_MSGING_UNKNOWN_DEV, BL_ERROR_MSGING_UNKNOWN_DEV);

        appErrorNames = Collections.unmodifiableMap(aMap);
    }


}
