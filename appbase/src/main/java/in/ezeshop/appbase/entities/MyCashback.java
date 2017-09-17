package in.ezeshop.appbase.entities;

import android.graphics.Bitmap;

import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.CsvConverter;
import in.ezeshop.common.MyCustomer;
import in.ezeshop.common.MyMerchant;
import in.ezeshop.common.database.Cashback;
import in.ezeshop.appbase.utilities.LogMy;

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
    //public static final int CB_CMP_TYPE_ACC_ADD = 3;
    //public static final int CB_CMP_TYPE_ACC_DEBIT = 4;
    //public static final int CB_CMP_TYPE_CB_BALANCE = 5;
    //public static final int CB_CMP_TYPE_CB_ADD = 6;
    //public static final int CB_CMP_TYPE_CB_DEBIT = 7;
    // Cashback sort by merchant attributes
    public static final int CB_CMP_TYPE_MCHNT_NAME = 8;
    public static final int CB_CMP_TYPE_MCHNT_CITY = 9;

    private Cashback mOldCashback;
    private Cashback mCurrCashback;

    // customer/merchant associated with this cashback object
    // they are provided by the backend in the other_details field as CSV string
    private MyCustomer mCustomer;
    private MyMerchant mMerchant;
    private Bitmap mDpMerchant;

    public MyCashback(Cashback cb, boolean callingUserIsMchnt) {
        init(cb,callingUserIsMchnt);
    }

    public MyCashback(String csvRecord, boolean callingUserIsMchnt) {
        //init(cb,callingUserIsMchnt);
        init(CsvConverter.cbFromCsvStr(csvRecord), callingUserIsMchnt);
    }

    /*
     * Init object values from given CSV string
     * containing both 'cashback' and 'customer/merchant' data in single record
     */
    /*public void init(String csvRecord, boolean callingUserIsMchnt) {
        init(CsvConverter.cbFromCsvStr(csvRecord), callingUserIsMchnt);
    }*/

    private void init(Cashback cb, boolean callingUserIsMchnt) {
        mCurrCashback = cb;
        LogMy.d(TAG,"Init MyCashback, other details: "+mCurrCashback.getOther_details());

        if(callingUserIsMchnt) {
            mCustomer = new MyCustomer();
            mCustomer.init(mCurrCashback.getOther_details());
        } else {
            mMerchant = new MyMerchant();
            mMerchant.init(mCurrCashback.getOther_details());
        }
    }

    // Current cashback operations
    public void setCashback(Cashback currCashback) {
        mOldCashback = mCurrCashback;
        mCurrCashback = currCashback;
    }

    /*
     * Getter methods
     */
    public MyCustomer getCustomer() {
        return mCustomer;
    }

    public MyMerchant getMerchant() {
        return mMerchant;
    }

    public String getMerchantId() {
        return mCurrCashback.getMerchant_id();
    }

    public Bitmap getDpMerchant() {
        return mDpMerchant;
    }

    public void setDpMerchant(Bitmap mDpMerchant) {
        this.mDpMerchant = mDpMerchant;
    }

    /*
     * Current cashback Getter methods
     */
    public Cashback getCurrCashback() {
        return mCurrCashback;
    }
    public int getBillAmt() {
        return mCurrCashback.getTotal_billed();
    }

    public int getCurrAccBalance() {
        return CommonUtils.getAccBalance(mCurrCashback);
    }
    public int getCurrAccTotalAdd() {
        return (mCurrCashback.getCl_credit() + mCurrCashback.getCb_credit() + mCurrCashback.getExtra_cb_credit());
    }
    public int getClCredit() {
        return mCurrCashback.getCl_credit();
    }
    public int getCurrAccTotalCb() {
        return (mCurrCashback.getCb_credit() + mCurrCashback.getExtra_cb_credit());
    }

    public int getCurrAccTotalDebit() {
        return (mCurrCashback.getCl_debit() + mCurrCashback.getCl_overdraft());
    }

    /*public int getClDebit() { return mCurrCashback.getCl_debit(); }
    public int getClOverdraft() { return mCurrCashback.getCl_overdraft(); }*/

    public Date getLastTxnTime() {
        // updateTime will be null if no txn done after registration - use createTime in that case
        return (mCurrCashback.getLastTxnTime()==null ? getCreateTime():mCurrCashback.getLastTxnTime());
    }
    public Date getCreateTime() {
        return mCurrCashback==null?null:mCurrCashback.getCreated();
    }

    /*
     * Old cashback Getter methods
     */
    /*public int getOldCbBalance() {
        return mOldCashback==null?0:(mOldCashback.getCb_credit() - mOldCashback.getCb_debit());
    }*/
    public int getOldClBalance() {
        //return mOldCashback==null?0:(mOldCashback.getCl_credit() - mOldCashback.getCl_debit());
        return CommonUtils.getAccBalance(mOldCashback);
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
            switch (mCompareType) {
                case CB_CMP_TYPE_UPDATE_TIME:
                    return compare(lhs.getLastTxnTime().getTime(), rhs.getLastTxnTime().getTime());
                case CB_CMP_TYPE_BILL_AMT:
                    return compare(lhs.getBillAmt(), rhs.getBillAmt());
                case CB_CMP_TYPE_ACC_BALANCE:
                    return compare(lhs.getCurrAccBalance(), rhs.getCurrAccBalance());
                /*case CB_CMP_TYPE_ACC_ADD:
                    return compare(lhs.getClCredit(), rhs.getClCredit());
                case CB_CMP_TYPE_ACC_DEBIT:
                    return compare(lhs.getClDebit(), rhs.getClDebit());
                case CB_CMP_TYPE_CB_ADD:
                    return compare(lhs.getCbCredit(), rhs.getCbCredit());*/
                /*case CB_CMP_TYPE_CB_DEBIT:
                    return compare(lhs.getCbRedeem(), rhs.getCbRedeem());
                case CB_CMP_TYPE_CB_BALANCE:
                    return compare(lhs.getCurrCbBalance(), rhs.getCurrCbBalance());*/
                case CB_CMP_TYPE_MCHNT_NAME:
                    return compare(lhs.getMerchant().getName(), rhs.getMerchant().getName());
                case CB_CMP_TYPE_MCHNT_CITY:
                    return compare(lhs.getMerchant().getCity(), rhs.getMerchant().getCity());
            }
            return 0;
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
