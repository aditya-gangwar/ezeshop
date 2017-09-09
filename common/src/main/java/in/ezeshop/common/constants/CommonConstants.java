package in.ezeshop.common.constants;

import java.util.Locale;

/**
 * This class contains constants relevant to both server code and app code.
 * This class should be exactly same everywhere
 */
public class CommonConstants {

    /*
     * Common Settings
     */
    //public static final boolean IS_PRODUCTION_RELEASE = false;

    /*
     * Backend server settings
     */
    //public static final String APPLICATION_ID = "927292A7-D4D3-7742-FFED-86CED1441100";
    //public static final String ANDROID_SECRET_KEY = "14765681-0A7C-3F4E-FF29-41A948E33500";
    //public static final String VERSION = "v1";

    //public static final String BACKENDLESS_HOST_IP = "35.154.80.2";
    //public static final String BACKENDLESS_HOST = "http://"+BACKENDLESS_HOST_IP+":8080/api";
    //public static final String BULK_API_URL  = BACKENDLESS_HOST+"/"+VERSION+"/data/bulk/";
    //public static String BACKEND_FILE_BASE_URL = BACKENDLESS_HOST+"/"+APPLICATION_ID+"/"+VERSION+"/files/";

    /*
     * Due to some issue in backendless - the errorCode is not correctly transmitted to app
     * from the Backend API - on raising an exception.
     * So, the errorCode is passed in 'errorMsg' field of the Backendless exception.
     * This prefix is added to the message to signal the same.
     */
    //public static final String PREFIX_ERROR_CODE_AS_MSG = "ZZ";

    /*
     * To use int as boolean
     */
    public static final int BOOLEAN_VALUE_FALSE = 0;
    public static final int BOOLEAN_VALUE_TRUE = 2;
    public static final int BOOLEAN_VALUE_INVALID = -1;

    public static final String CUSTOMER_CARE_NUMBER = "+91-9069113198";

    /*
     * Date formats
     */
    public static final String TIMEZONE = "Asia/Kolkata";

    public static final Locale DATE_LOCALE = Locale.ENGLISH;
    // used where only date (without time) is to be shown
    public static final String DATE_FORMAT_ONLY_DATE_DISPLAY = "dd MMM, yy";
    // used to specify 'date with no time' to the backend, like in where clause
    public static final String DATE_FORMAT_ONLY_DATE_BACKEND = "dd-MMM-yyyy";
    // used to specify 'date with no time' in the CSV report generated
    public static final String DATE_FORMAT_ONLY_DATE_CSV = "dd/MM/yyyy";
    public static final String DATE_FORMAT_DDMM = "ddMM";
    public static final String DATE_FORMAT_DDMMYY = "ddMMyy";
    public static final String DATE_FORMAT_MMYYYY = "MMyyyy";
    // used in reports etc where both date and time is to be shown
    public static final String DATE_FORMAT_WITH_TIME = "dd/MM/yyyy HH:mm";
    // date format to be used in filename
    public static final String DATE_FORMAT_ONLY_DATE_FILENAME = "ddMMMyy";
    // date format with time, to be used in filename
    public static final String DATE_FORMAT_WITH_TIME_FILENAME = "ddMMMyy_HH_mm";
    // format to show only time in 12 hr format
    public static final String DATE_FORMAT_ONLY_TIME_12 = "hh:mm a";
    // format to show only time in CSV file
    public static final String DATE_FORMAT_ONLY_TIME_24_CSV = "HH:mm:ss";

    public static final int MILLISECS_IN_HOUR = 3600000;
    public static final int MILLISECS_IN_MINUTE = 60000;

    public static final int SEX_MALE = 11;
    public static final int SEX_FEMALE = 12;

    /*
     * Size, Length and Limits
     */
    public static final int CUSTOMER_INTERNAL_ID_LEN = 6;
    public static final int INTERNAL_USER_ID_LEN = 7;
    public static final int MERCHANT_ID_LEN = 8;
    public static final int MOBILE_NUM_LENGTH = 10;
    public static final int LANDLINE_NUM_LENGTH = 11;
    //public static final int CUSTOMER_CARDID_LEN = 11;
    // DOB in format 'DDMMYYYY'
    public static final int DOB_LEN = 8;
    public static final int TRANSACTION_ID_LEN = 13;
    public static final int PIN_LEN = 4;
    public static final int OTP_LEN = 5;
    public static final int PINCODE_LEN = 6;
    public static final int PASSWORD_MIN_LEN = 6;

    //public static final int MIN_CARD_ORDER_QTY = 10;

    public static final int MAX_DEVICES_PER_MERCHANT = 3;
    public static final int DB_QUERY_PAGE_SIZE = 100;

    /*
     * Backend path constants
     */
    public static final String FILE_PATH_SEPERATOR = "/";
    public static final String MERCHANT_ROOT_DIR = "merchants"+ CommonConstants.FILE_PATH_SEPERATOR;
    public static final String CUSTOMER_ROOT_DIR = "customers"+ CommonConstants.FILE_PATH_SEPERATOR;
    public static final String MERCHANT_TXN_ROOT_DIR = MERCHANT_ROOT_DIR+"txnCsvFiles"+ CommonConstants.FILE_PATH_SEPERATOR;
    public static final String CUSTOMER_TXN_ROOT_DIR = CUSTOMER_ROOT_DIR+"txnCsvFiles"+ CommonConstants.FILE_PATH_SEPERATOR;
    public static final String MERCHANT_DISPLAY_IMAGES_DIR = MERCHANT_ROOT_DIR+"displayImages"+ CommonConstants.FILE_PATH_SEPERATOR;
    public static final String MERCHANT_LOGGING_ROOT_DIR = "logging"+ CommonConstants.FILE_PATH_SEPERATOR;
    public static final String MERCHANT_CUST_DATA_ROOT_DIR = MERCHANT_ROOT_DIR+"customerData"+ CommonConstants.FILE_PATH_SEPERATOR;
    public static final String MERCHANT_TXN_IMAGE_ROOT_DIR = MERCHANT_ROOT_DIR+"txnImages"+ CommonConstants.FILE_PATH_SEPERATOR;

    public static final String MERCHANT_TXN_FILE_PREFIX = "txns_";
    public static final String CUSTOMER_TXN_FILE_PREFIX = "txns_";
    public static final String MERCHANT_CUST_DATA_FILE_PREFIX = "customers_";
    public static final String CASHBACK_DATA_FILE_PREFIX = "cashback_";
    public static final String PREFIX_TXN_IMG_FILE_NAME = "txnImg_";
    public static final String PREFIX_TXN_CANCEL_IMG_FILE_NAME = "txnCancel_";

    public static final String CSV_DELIMETER = ",";
    public static final String SPECIAL_DELIMETER = ";";
    public static final String CSV_FILE_EXT = ".csv";
    public static final String NEWLINE_SEP = "\n";

    /*
     * Prefixes
     */
    public static final String PREFIX_AGENT_ID = "1";
    public static final String PREFIX_CC_ID = "2";
    public static final String PREFIX_CCNT_ID = "3";
    public static final String TRANSACTION_ID_PREFIX = "TX";
    //public static final String MEMBER_CARD_ID_PREFIX = "mmc";
    public static final String MEMBER_BARCODE_ID_PREFIX = "a7";
    //public static final String MCHNT_ORDER_ID_PREFIX = "MO";

    // Customer id type to fetch record
    public static final int ID_TYPE_MOBILE = 0;
    //public static final int ID_TYPE_CARD = 1;
    public static final int ID_TYPE_AUTO = 2;

    /*
     * Other common constants
     */
    // number to character mapping
    public static final String numToChar[] = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    // File format for photos
    // Supported Values: "webp", "jpeg", "png"
    public static final String PHOTO_FILE_FORMAT = "webp";
    // Bulk actions for Member Cards
    /*public static final String CARDS_UPLOAD_TO_POOL = "cardsUploadToPool";
    //public static final String CARDS_ALLOT_TO_AGENT = "cardsAllotToAgent";
    public static final String CARDS_ALLOT_TO_MCHNT = "cardsAllotToMchnt";
    public static final String CARDS_RETURN_BY_MCHNT = "cardsReturnByMchnt";
    //public static final String CARDS_RETURN_BY_AGENT = "cardsReturnByAgent";*/

    public static final String DUMMY_CITY_NAME = "DummyCity";
}
