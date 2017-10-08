package in.ezeshop.common;

import com.backendless.exceptions.BackendlessException;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Cashback;
import in.ezeshop.common.database.Cities;
import in.ezeshop.common.database.CustAddress;
import in.ezeshop.common.database.MerchantStats;
import in.ezeshop.common.database.Merchants;
import in.ezeshop.common.database.MyAreas;
import in.ezeshop.common.database.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by adgangwa on 08-10-2016.
 */
public class CommonUtils {

    private static final SimpleDateFormat mSdfOnlyDateFilename = new SimpleDateFormat(CommonConstants.DATE_FORMAT_ONLY_DATE_FILENAME, CommonConstants.DATE_LOCALE);
    private static final SimpleDateFormat mSdfDateMMYYYY = new SimpleDateFormat(CommonConstants.DATE_FORMAT_MMYYYY, CommonConstants.DATE_LOCALE);

    public static boolean txnVerifyReq(Merchants merchant, Transaction txn) {
        //if(txn.getCancelTime()==null) {
            int cl_credit_threshold = (merchant.getCl_credit_limit_for_pin() < 0) ? MyGlobalSettings.getAccAddPinLimit() : merchant.getCl_credit_limit_for_pin();
            int cl_debit_threshold = (merchant.getCl_debit_limit_for_pin() < 0) ? MyGlobalSettings.getAccDebitPinLimit() : merchant.getCl_debit_limit_for_pin();
            //int cb_debit_threshold = (merchant.getCb_debit_limit_for_pin() < 0) ? MyGlobalSettings.getCbDebitPinLimit() : merchant.getCb_debit_limit_for_pin();

            /*int cl_credit_threshold = MyGlobalSettings.getAccAddPinLimit();
            int cl_debit_threshold = MyGlobalSettings.getAccDebitPinLimit();
            int cb_debit_threshold = MyGlobalSettings.getCbDebitPinLimit();*/

            //int higher_debit_threshold = Math.max(cl_debit_threshold, cb_debit_threshold);

            return (txn.getCl_credit() > cl_credit_threshold
                    || txn.getCl_debit() > cl_debit_threshold
                    || txn.getCl_overdraft() > 0
                    //|| txn.getCb_debit() > cb_debit_threshold
                    //|| (txn.getCl_debit() + txn.getCb_debit()) > higher_debit_threshold
            );
        //}
        //return true;
    }

    public static int getAccBalance(Cashback cb) {
        return (cb.getCl_credit()+cb.getCb_credit()+cb.getExtra_cb_credit()-cb.getCl_debit()-cb.getCl_overdraft());
    }

    public static String getHalfVisibleMobileNum(String mobileNum) {
        if(mobileNum.length() != CommonConstants.MOBILE_NUM_LENGTH) {
            return null;
        }
        // build half visible mobile num : 8800xxx535
        char[] mobile = mobileNum.toCharArray();
        mobile[4] = '*';
        mobile[5] = '*';
        mobile[6] = '*';
        return String.valueOf(mobile);
    }

    public static String getHalfVisibleStr(String str) {
        // build half visible userid : XXXXX91535
        StringBuilder halfVisibleUserid = new StringBuilder();
        int hiddenlen = str.length() - (str.length() / 3);
        for(int i=0; i<hiddenlen; i++) {
            halfVisibleUserid.append("x");
        }
        halfVisibleUserid.append(str.substring(hiddenlen));
        return halfVisibleUserid.toString();
    }

    /*
     * Customer Address related
     */
    public static String getCustAddrStrWithName(CustAddress addr) {
        MyAreas  area = addr.getArea();
        if(area==null) {
            return "";
        }
        Cities city = area.getCity();
        if(city==null) {
            return "";
        }

        return addr.getToName()+"\n"
                +getCustAddressStr(addr);
    }
    public static String getCustAddressStr(CustAddress addr) {
        MyAreas  area = addr.getArea();
        if(area==null) {
            return "";
        }
        Cities city = area.getCity();
        if(city==null) {
            return "";
        }

        return addr.getText1()+"\n"
                +area.getAreaName()+", "+city.getCity()+"\n"
                +city.getState()+
                ((area.getPincode()==null||area.getPincode().isEmpty())?(""):(" - "+area.getPincode()))+"\n"
                +"+91-"+addr.getContactNum();
    }

    /*
     * Methods to get file paths (in backend) for various types of user files
     */
    public static String getMerchantTxnDir(String merchantId) {
        // merchant directory: merchants/<first 3 chars of merchant id>/<first 5 chars of merchant id>/<merchant id>/
        return CommonConstants.MERCHANT_TXN_ROOT_DIR +
                merchantId.substring(0,3) + CommonConstants.FILE_PATH_SEPERATOR +
                merchantId.substring(0,5) + CommonConstants.FILE_PATH_SEPERATOR +
                merchantId;
    }

    public static String getCustomerTxnDir(String customerId) {
        // customer directory: customers/<first 2 chars of customer id>/first 4 chars of customer id>/<customer id>/
        return CommonConstants.CUSTOMER_TXN_ROOT_DIR +
                customerId.substring(0,2) + CommonConstants.FILE_PATH_SEPERATOR +
                customerId.substring(0,4) + CommonConstants.FILE_PATH_SEPERATOR +
                customerId;
    }

    public static String getMerchantCustFilePath(String merchantId) {
        // File name: customers_<merchant_id>.csv
        return CommonConstants.MERCHANT_CUST_DATA_ROOT_DIR +
                CommonConstants.MERCHANT_CUST_DATA_FILE_PREFIX+merchantId+CommonConstants.CSV_FILE_EXT;
    }

    public static String getTxnCsvFilename(Date date, String merchantId) {
        // File name: txns_<merchant_id>_<ddMMMyy>.csv
        mSdfOnlyDateFilename.setTimeZone(TimeZone.getTimeZone(CommonConstants.TIMEZONE));
        return CommonConstants.MERCHANT_TXN_FILE_PREFIX + merchantId + "_" + mSdfOnlyDateFilename.format(date) + CommonConstants.CSV_FILE_EXT;
    }

    public static String getTxnCsvFilename(String month, String year, String merchantId) {
        // File name: txns_<merchant_id>_<MMyyyy>.csv
        return CommonConstants.MERCHANT_TXN_FILE_PREFIX + merchantId + "_" + month + year + CommonConstants.CSV_FILE_EXT;
    }

    public static String getCustTxnCsvFilename(String year, String customerId) {
        // File name: txns_<customer_id>_<yyyy>.csv
        return CommonConstants.CUSTOMER_TXN_FILE_PREFIX + customerId + "_" + year + CommonConstants.CSV_FILE_EXT;
    }

    /*public static String getTxnImgDir(String merchantId) {
        // merchant directory: merchants/<first 3 chars of merchant id>/<next 2 chars of merchant id>/<merchant id>/
        return CommonConstants.MERCHANT_TXN_IMAGE_ROOT_DIR +
                merchantId.substring(0,3) + CommonConstants.FILE_PATH_SEPERATOR +
                merchantId.substring(0,5) + CommonConstants.FILE_PATH_SEPERATOR +
                merchantId;
    }*/

    public static String getTxnImgDir(Date date) {
        mSdfOnlyDateFilename.setTimeZone(TimeZone.getTimeZone(CommonConstants.TIMEZONE));
        return CommonConstants.MERCHANT_TXN_IMAGE_ROOT_DIR + mSdfOnlyDateFilename.format(date);
    }

    public static boolean mchntStatsRefreshReq(MerchantStats stats) {
        boolean retValue = true;
        long now = (new Date()).getTime();
        long updateTime = (stats.getUpdated()==null) ?
                stats.getCreated().getTime() :
                stats.getUpdated().getTime();

        long timeDiff = now - updateTime;
        long noRefreshDuration = MyGlobalSettings.getMchntDashBNoRefreshMins()*CommonConstants.MILLISECS_IN_MINUTE;

        if( timeDiff <= noRefreshDuration ) {
            retValue = false;
        }
        return retValue;
    }

    public static int getCustomerIdType(String id) {
        switch (id.length()) {
            case CommonConstants.MOBILE_NUM_LENGTH:
                return CommonConstants.ID_TYPE_MOBILE;
            /*case CommonConstants.CUSTOMER_CARDID_LEN:
                return CommonConstants.ID_TYPE_CARD;*/
            case CommonConstants.CUSTOMER_INTERNAL_ID_LEN:
                return CommonConstants.ID_TYPE_AUTO;
            default:
                /*if(id.startsWith(CommonConstants.MEMBER_CARD_ID_PREFIX)) {
                    return CommonConstants.ID_TYPE_CARD;
                }*/
                throw new BackendlessException(String.valueOf(ErrorCodes.WRONG_INPUT_DATA), "Invalid customer ID: "+id);
        }
    }

    public static int roundUpTo(int i, int v){
        return Math.round(i/v) * v;
    }
}
