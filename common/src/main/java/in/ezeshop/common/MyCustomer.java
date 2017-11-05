package in.ezeshop.common;

import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.database.Customers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by adgangwa on 02-06-2016.
 */
public class MyCustomer {
    private static final String TAG = "MyCustomer";

    SimpleDateFormat mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.MY_LOCALE);

    /*
     * Index of various parameters in CustomerDetails CSV records (rcvd as part of cashback object)
     * Format:
     * <private id>,<mobile_num>,<<name>>,<<first login ok>>,<<cust create time>>
     * <acc_status>,<acc_status_reason>,<acc_status_update_time>,<<admin remarks>>
     *     -- membership card data follows --
     * <card_id>,<card_status>,<card_status_update_time>
     * Records with double bracket '<<>>' are only sent to 'customer care' users
     */
    private static int CUST_CSV_PRIVATE_ID = 0;
    private static int CUST_CSV_NAME = 1;
    private static int CUST_CSV_MOBILE_NUM = 2;
    private static int CUST_CSV_ACC_STATUS = 3;
    private static int CUST_CSV_STATUS_REASON = 4;
    private static int CUST_CSV_STATUS_UPDATE_TIME = 5;
    private static int CUST_CSV_TOTAL_FIELDS = 6;

    // Total size of above fields = 50+10*9
    private static final int CUST_CSV_MAX_SIZE = 200;
    private static final String CUST_CSV_DELIM = ":";

    // Customer properties
    private String mPrivateId;
    private String mName;
    private String mMobileNum;
    private int mStatus;
    private String mStatusUpdateTime;
    private Date mStatusUpdateDate;
    // optional properties
    private String mStatusReason;

    // Init from CSV string
    public void init(String customerDetailsInCsvFormat) {
        if(customerDetailsInCsvFormat== null || customerDetailsInCsvFormat.isEmpty()) {
            return;
        }

        mSdfDateWithTime.setTimeZone(TimeZone.getTimeZone(CommonConstants.TIMEZONE));

        String[] csvFields = customerDetailsInCsvFormat.split(CUST_CSV_DELIM, -1);
        mPrivateId = csvFields[CUST_CSV_PRIVATE_ID];
        mName = csvFields[CUST_CSV_NAME];
        mMobileNum = csvFields[CUST_CSV_MOBILE_NUM];
        mStatus = Integer.parseInt(csvFields[CUST_CSV_ACC_STATUS]);
        mStatusUpdateDate = new Date(Long.parseLong(csvFields[CUST_CSV_STATUS_UPDATE_TIME]));
        mStatusUpdateTime = mSdfDateWithTime.format(mStatusUpdateDate);


        if(!csvFields[CUST_CSV_STATUS_REASON].isEmpty()) {
            mStatusReason = csvFields[CUST_CSV_STATUS_REASON];
        } else {
            mStatusReason = null;
        }
    }

    // Convert to CSV string
    public static String toCsvString(Customers customer) {

        //CustomerCards card = customer.getMembership_card();
        String[] csvFields = new String[CUST_CSV_TOTAL_FIELDS];

        csvFields[CUST_CSV_PRIVATE_ID] = customer.getPrivate_id() ;
        csvFields[CUST_CSV_NAME] = customer.getName() ;
        csvFields[CUST_CSV_MOBILE_NUM] = CommonUtils.getHalfVisibleMobileNum(customer.getMobile_num());
        csvFields[CUST_CSV_ACC_STATUS] = String.valueOf(customer.getAdmin_status()) ;
        csvFields[CUST_CSV_STATUS_REASON] = customer.getStatus_reason();
        csvFields[CUST_CSV_STATUS_UPDATE_TIME] = String.valueOf(customer.getStatus_update_time().getTime()) ;

        // join the fields in single CSV string
        StringBuilder sb = new StringBuilder(CUST_CSV_MAX_SIZE);
        for(int i=0; i<CUST_CSV_TOTAL_FIELDS; i++) {
            sb.append(csvFields[i]).append(CUST_CSV_DELIM);
        }

        return sb.toString();
    }

    /*
     * Getter methods
     */
    public String getPrivateId() {
        return mPrivateId;
    }

    public String getName() {
        return mName;
    }

    public String getMobileNum() {
        return mMobileNum;
    }

    public int getStatus() {
        return mStatus;
    }

    public String getStatusReason() {
        return mStatusReason;
    }

    public String getStatusUpdateTime() {
        return mStatusUpdateTime;
    }

    public Date getStatusUpdateDate() {
        return mStatusUpdateDate;
    }

    /*public String getCardId() {
        return mCardId;
    }

    public int getCardStatus() {
        return mCardStatus;
    }

    public String getCardStatusUpdateTime() {
        return mCardStatusUpdateTime;
    }*/
}
