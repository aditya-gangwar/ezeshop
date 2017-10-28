package in.ezeshop.appbase.entities;

import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.CsvConverter;
import in.ezeshop.common.MyCustomer;
import in.ezeshop.common.database.Cashback;
import in.ezeshop.common.database.Merchants;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by adgangwa on 07-05-2016.
 */
public class MyCashback {
    private static final String TAG = "BaseApp-MyCashback";

    // Cashback sort parameter types
    public static final int CB_CMP_TYPE_UPDATE_TIME = 0;
    public static final int CB_CMP_TYPE_BILL_AMT = 1;
    public static final int CB_CMP_TYPE_ACC_BALANCE = 2;
    // Cashback sort by merchant attributes
    public static final int CB_CMP_TYPE_MCHNT_NAME = 8;
    public static final int CB_CMP_TYPE_CB_RATE = 3;

    //private Cashback mOldCashback;

    // Any of below can be 'null'
    // so should be handles so in the class

    // if null means relationship between merchant - customer dont exist yet
    private Cashback mCashback;
    // One of below will usually be null - depending upon which app this class is used by
    // can be null - when this class is used by merchant app - as merchant object is same (part of MerchantUser class)
    private Merchants mMerchant;
    // can be null - when this class is used by customer app - as customer object is same (part of CustomerUser class)
    private MyCustomer mCustomer;

    // store account balance of old cashback - in case of cashback object replacement
    private int mOldAccBalance;

    /* Constructors */
    public MyCashback(Cashback cb) {
        init(cb);
    }

    public MyCashback(String csvRecord) {
        init(CsvConverter.cbFromCsvStr(csvRecord));
    }

    public MyCashback(Merchants mchnt) {
        mCashback = null;
        mMerchant = mchnt;
        mCustomer = null;
    }

    /*
     * Init object values from given CSV string
     * containing both 'cashback' and 'customer/merchant' data in single record
     */
    private void init(Cashback cb) {
        //mOldCashback = mCashback;
        mCashback = cb;
        if(cb.getMerchantNIDB()!=null) {
            mMerchant = cb.getMerchantNIDB();
        }
        if(mCashback.getOther_details()!=null && !mCashback.getOther_details().isEmpty()) {
            mCustomer = new MyCustomer();
            mCustomer.init(mCashback.getOther_details());
        }
    }

    // Current cashback operations
    public void resetOnlyCashback(Cashback currCashback) {
        //mOldCashback = mCashback;
        mOldAccBalance = CommonUtils.getAccBalance(mCashback);
        mCashback = currCashback;
    }

    /*
     * Getter methods
     */
    public MyCustomer getCustomer() {
        return mCustomer;
    }

    public Merchants getMerchant() {
        return mMerchant;
    }

    public String getMerchantId() {
        return mCashback.getMerchant_id();
    }

    /*
     * Current cashback Getter methods
     */
    public boolean isAccDataAvailable() {
        return mCashback!=null;
    }
    public int getCurrAccBalance() {
        return CommonUtils.getAccBalance(mCashback);
    }
    public int getCurrAccTotalAdd() {
        return mCashback ==null?0:(mCashback.getCl_credit() + mCashback.getCb_credit() + mCashback.getExtra_cb_credit());
    }
    public int getClCredit() {
        return mCashback ==null?0: mCashback.getCl_credit();
    }
    public int getCurrAccTotalCb() {
        return mCashback ==null?0:(mCashback.getCb_credit() + mCashback.getExtra_cb_credit());
    }

    public int getCurrAccTotalDebit() {
        return mCashback ==null?0:(mCashback.getCl_debit() + mCashback.getCl_overdraft());
    }

    public int getCurrAccOverdraft() {
        return mCashback ==null?0: mCashback.getCl_overdraft();
    }

    public int getBillAmt() {
        return mCashback ==null?0: mCashback.getTotal_billed();
    }
    public int getCurrClDebit() { return mCashback ==null?0: mCashback.getCl_debit(); }

    public Date getLastTxnTime() {
        // updateTime will be null if no txn done after registration - use createTime in that case
        return mCashback ==null?null:(mCashback.getLastTxnTime()==null ? getCreateTime(): mCashback.getLastTxnTime());
    }
    public Date getCreateTime() {
        return mCashback ==null?null: mCashback.getCreated();
    }

    /*
     * Old cashback Getter methods
     */
    public int getOldClBalance() {
        //return mOldCashback==null?0:CommonUtils.getAccBalance(mOldCashback);
        return mOldAccBalance;
    }

    /*
     * comparator functions for sorting
     */
    public static class MyCashbackComparator implements Comparator<MyCashback> {

        int mCompareType;
        public MyCashbackComparator(int compareType) {
            mCompareType = compareType;
        }

        @Override
        public int compare(MyCashback lhs, MyCashback rhs) {
            // TODO: Handle null x or y values
            try {
                switch (mCompareType) {
                    case CB_CMP_TYPE_UPDATE_TIME:
                        return compare(lhs.getLastTxnTime().getTime(), rhs.getLastTxnTime().getTime());
                    case CB_CMP_TYPE_BILL_AMT:
                        return compare(lhs.getBillAmt(), rhs.getBillAmt());
                    case CB_CMP_TYPE_ACC_BALANCE:
                        return compare(lhs.getCurrAccBalance(), rhs.getCurrAccBalance());
                    case CB_CMP_TYPE_MCHNT_NAME:
                        return compare(lhs.getMerchant().getName(), rhs.getMerchant().getName());
                    case CB_CMP_TYPE_CB_RATE:
                        return compare(lhs.getMerchant().getCb_rate(), rhs.getMerchant().getCb_rate());
                    default:
                        return 0;
                }
            } catch (Exception ex) {
                // ignore exception
                LogMy.e(TAG,"Exception in MyCashbackComparator:compare",ex);
                return 0;
            }
        }
        private static int compare(long a, long b) {
            return a < b ? -1
                    : a > b ? 1
                    : 0;
        }
        private static int compare(int a, int b) {
            return a < b ? -1
                    : a > b ? 1
                    : 0;
        }
        private static int compare(String a, String b) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(a, b);
            return (res != 0) ? res : a.compareTo(b);
        }
    }
}
