/*package in.ezeshop.appbase.entities;

import java.util.Comparator;

import in.ezeshop.common.database.Merchants;

public class MerchantWrapper {
    private static final String TAG = "BaseApp-MyMerchant";

    // Cashback sort parameter types
    public static final int MCHNT_CMP_TYPE_ACC_BALANCE = 1;
    public static final int MCHNT_CMP_TYPE_MCHNT_NAME = 2;
    public static final int MCHNT_CMP_TYPE_CB_RATE = 3;

    private Merchants mMerchant;
    private String mCustPrivId;
    private int mAccBalance;

    public MerchantWrapper(Merchants mchnt, String custId, int accBalance) {
        mMerchant = mchnt;
        mCustPrivId = custId;
        mAccBalance = accBalance;
    }

    public Merchants getmMerchant() {
        return mMerchant;
    }

    public void setmMerchant(Merchants mMerchant) {
        this.mMerchant = mMerchant;
    }

    public String getmCustPrivId() {
        return mCustPrivId;
    }

    public void setmCustPrivId(String mCustPrivId) {
        this.mCustPrivId = mCustPrivId;
    }

    public int getmAccBalance() {
        return mAccBalance;
    }

    public void setmAccBalance(int mAccBalance) {
        this.mAccBalance = mAccBalance;
    }

    public static class MerchantComparator implements Comparator<MerchantWrapper> {

        int mCompareType;
        public MerchantComparator(int compareType) {
            mCompareType = compareType;
        }

        @Override
        public int compare(MerchantWrapper lhs, MerchantWrapper rhs) {
            // TODO: Handle null x or y values
            switch (mCompareType) {
                case MCHNT_CMP_TYPE_ACC_BALANCE:
                    return compare(lhs.mAccBalance, rhs.mAccBalance);
                case MCHNT_CMP_TYPE_MCHNT_NAME:
                    return compare(lhs.mMerchant.getName(), rhs.mMerchant.getName());
                case MCHNT_CMP_TYPE_CB_RATE:
                    return compare(lhs.mMerchant.getCb_rate(), rhs.mMerchant.getCb_rate());
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
*/