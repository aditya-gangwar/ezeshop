package in.ezeshop.merchantbase;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import in.ezeshop.appbase.*;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.entities.MyTransaction;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.common.MyGlobalSettings;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Transaction;
import in.ezeshop.merchantbase.entities.MerchantUser;
import in.ezeshop.merchantbase.helper.CashPaid2;
import in.ezeshop.merchantbase.helper.MyRetainedFragment;

/**
 * Created by adgangwa on 25-02-2017.
 */

public class CashTransactionFragment_2 extends BaseFragment implements
        CashPaid2.CashPaid2If {
    private static final String TAG = "MchntApp-CashTransactionFragment";

    private static final int REQ_NEW_BILL_AMT = 3;
    private static final int REQ_CASH_PAID_AMT = 4;
    private static final int REQ_NOTIFY_ERROR = 7;
    private static final int REQ_NOTIFY_ERROR_EXIT = 8;
    //public static final int RC_BARCODE_CAPTURE_CASH_TXN = 9009;

    private static final String DIALOG_NUM_INPUT = "NumberInput";


    // enabled state
    private static final int STATUS_AUTO = 0;

    // temporary disabled states i.e. can be changed from this screen also
    private static final int STATUS_CLEARED = 11;    // explicitly cleared by user by clicking
    //private static final int STATUS_QR_CARD_NOT_USED = 13;
    private static final int STATUS_CASH_PAID_NOT_SET = 14;
    private static final int STATUS_NO_BILL_AMT = 15;

    // permanent disabled states i.e. cant be changed from this screen
    private static final int STATUS_DISABLED = 21;
    private static final int STATUS_NO_BALANCE = 22;
    private static final int STATUS_BALANCE_BELOW_LIMIT = 23;
    private static final int STATUS_ACCOUNT_FULL = 24;
    //private static final int STATUS_CARD_NOT_ACTIVE = 25;


    public static final Map<Integer, String> statusDesc;
    static {
        Map<Integer, String> aMap = new HashMap<>(20);
        //aMap.put(STATUS_QR_CARD_NOT_USED,"Customer Card Required for Debit");
        aMap.put(STATUS_CASH_PAID_NOT_SET,"Cash Paid Not Set");
        aMap.put(STATUS_NO_BILL_AMT,"No Bill Amount for Debit");

        aMap.put(STATUS_DISABLED,"Disabled in Settings");
        aMap.put(STATUS_NO_BALANCE,"No Balance for Debit");
        aMap.put(STATUS_BALANCE_BELOW_LIMIT,"Balance below "+AppCommonUtil.getAmtStr(MyGlobalSettings.getCbRedeemLimit()));
        aMap.put(STATUS_ACCOUNT_FULL,"Account Cash Limit Reached");
        //aMap.put(STATUS_CARD_NOT_ACTIVE,"Customer Card not Active");
        statusDesc = Collections.unmodifiableMap(aMap);
    }


    private CashTransactionFragment_2.CashTransactionFragmentIf mCallback;
    private MyRetainedFragment mRetainedFragment;
    private MerchantUser mMerchantUser;
    private CashPaid2 mCashPaidHelper;

    // These members are not necessarily required to be stored as part of fragment state
    // As, either they represent values on screen, or can be calculated again.
    // But, at this advance stage, it is easier to save-restore them - instead of changing code otherwise
    private int mDebitCashload;
    private int mOverdraft;
    //private int mDebitCashback;
    private int mAddCashload;
    private int mAddCbNormal;
    private int mAddCbExtra;
    private int mCashPaid;
    private int mReturnCash;

    private int mCbEligibleAmt;
    private int mExtraCbEligibleAmt;

    private int mClBalance;
    //private int mCbBalance;
    private int mMinCashToPay;

    private float mCbRate;
    private float mPpCbRate;

    // Part of instance state: to be restored in event of fragment recreation
    private int mAddClStatus;
    private int mDebitClStatus;
    private int mOverdraftStatus;
    private int mAddCbStatus;
    //private int mDebitCbStatus;
    //private int cardScanAmtId;
    private long newBillDialogOpenTime;

    // Container Activity must implement this interface
    public interface CashTransactionFragmentIf {
        MyRetainedFragment getRetainedFragment();
        void onTransactionSubmit(int cashPaid);
        void setDrawerState(boolean isEnabled);
        void restartTxn();
        void onViewOrderList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cash_txn_5, container, false);

        // access to UI elements
        bindUiResources(v);
        //setup all listeners
        initListeners();

        //mLayoutErrorDetails.setVisibility(View.GONE);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mCallback = (CashTransactionFragment_2.CashTransactionFragmentIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement CashTransactionFragmentIf");
        }

        mRetainedFragment = mCallback.getRetainedFragment();
        mMerchantUser = MerchantUser.getInstance();

        /*
         * Instead of checking for 'savedInstanceState==null', checking
         * for any 'not saved member' value (here, mCashPaidHelper)
         * The reason being, that for scenarios wherein fragment was stored in backstack and
         * has come to foreground again - like after pressing 'back' from 'txn confirm fragment'
         * then, the savedInstanceState will be NULL only.
         * In backstack cases, only view is destroyed, while the fragment is saved as it is
         * Thus not even onSaveInstance() gets called.
         *
         * mCashPaidHelper will be null - for both 'fragment create' and 'fragment re-create' scenarios
         * but not for 'backstack' scenarios
         */
        try {
            boolean isBackstackCase = false;
            if (mCashPaidHelper != null) {
                isBackstackCase = true;
            }

            mCbRate = Float.parseFloat(mMerchantUser.getMerchant().getCb_rate());
            mPpCbRate = Float.parseFloat(mMerchantUser.getMerchant().getPrepaidCbRate());

            if (!isBackstackCase) {
                // either of fragment 'create' or 'recreate' scenarios
                if (savedInstanceState == null) {
                    // fragment create case
                    initAmtUiStates();
                } else {
                    // fragment re-create case
                    LogMy.d(TAG, "Fragment re-create case");
                    // restore status from stored values
                    mClBalance = savedInstanceState.getInt("mClBalance");
                    mCbBalance = savedInstanceState.getInt("mCbBalance");
                    mMinCashToPay = savedInstanceState.getInt("mMinCashToPay");

                    setAddCbStatus(savedInstanceState.getInt("mAddCbStatus"));
                    setAddClStatus(savedInstanceState.getInt("mAddClStatus"));
                    setDebitClStatus(savedInstanceState.getInt("mDebitClStatus"));
                    //setDebitCbStatus(savedInstanceState.getInt("mDebitCbStatus"));
                    setOverdraftStatus(savedInstanceState.getInt("mOverdraft"));
                    //mDebitCbOnPriority = savedInstanceState.getBoolean("mDebitCbOnPriority");
                }
            } else {
                // these fxs update onscreen view also, so need to be run for backstack scenario too
                setAddCbStatus(mAddCbStatus);
                setAddClStatus(mAddClStatus);
                setDebitClStatus(mDebitClStatus);
                setOverdraftStatus(mOverdraft);
                //setDebitCbStatus(mDebitCbStatus);
            }

            // Init view - only to be done after states are set above
            //initAmtUiVisibility(false);
            //initCashUiVisibility(false);
            if(mCashPaidHelper!=null) {
                mCashPaidHelper.initView(getView());
            }
            displayInputBillAmt();
            calcAndSetAddCb();

            if (!isBackstackCase) {
                if (savedInstanceState == null) {
                    calcAndSetAmts(false);

                } else {
                    // restore earlier calculated values
                    setDebitCashload(savedInstanceState.getInt("mDebitCashload"));
                    setRedeemCashback(savedInstanceState.getInt("mDebitCashback"));
                    setAddCashload(savedInstanceState.getInt("mAddCashload"));
                    setAddCashback(savedInstanceState.getInt("mAddCbNormal"),
                            savedInstanceState.getInt("mAddCbExtra"));
                    setCashPaid(savedInstanceState.getInt("mCashPaid"));

                    mReturnCash = savedInstanceState.getInt("mReturnCash");
                }
            }

        } catch (Exception e) {
            LogMy.e(TAG, "Exception in CustomerTransactionFragment:onActivityCreated", e);
            DialogFragmentWrapper dialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            dialog.setTargetFragment(this, REQ_NOTIFY_ERROR_EXIT);
            dialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            //throw e;
        }
    }

    private void displayInputBillAmt() {
        mInputBillAmt.setText(AppCommonUtil.getSignedAmtStr(mRetainedFragment.mBillTotal, true));
    }

    private void setAddCashload(int value) {
        if(value!=mAddCashload || value==0) {
            this.mAddCashload = value;
            setAccountAmt();
            // Calling, as 'Prepaid extra cashback' may need to be applied
            calcAndSetAddCb();
        }
        /*if(value>0) {
            mInputAccount.setText(AppCommonUtil.getSignedAmtStr(mAddCashload, true));
            mInputAccount.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_positive));
            // Calling, as 'Prepaid extra cashback' may need to be applied
            calcAndSetAddCb();
        }*/
    }

    private void setDebitCashload(int value) {
        if(value!=mDebitCashload || value==0) {
            this.mDebitCashload = value;
            setAccountAmt();
        }
        /*if(value>0) {
            mInputAccount.setText(AppCommonUtil.getSignedAmtStr(mDebitCashload, false));
            mInputAccount.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
        }*/
    }

    private void setOverdraft(int value) {
        if(value!=mOverdraft) {
            this.mOverdraft = value;
            mInputOverdraft.setText(AppCommonUtil.getSignedAmtStr(mOverdraft, false));
        }
    }

    private void setAccountAmt() {
        if(mAddCashload>0) {
            mInputAccount.setText(AppCommonUtil.getSignedAmtStr(mAddCashload, true));
            mInputAccount.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_positive));

        } else if(mDebitCashload>0) {
            mInputAccount.setText(AppCommonUtil.getSignedAmtStr(mDebitCashload, false));
            mInputAccount.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));

        } else {
            mInputAccount.setText(AppCommonUtil.getAmtStr(0));
            mInputAccount.setTextColor(ContextCompat.getColor(getActivity(), R.color.secondary_text));
        }
    }

    /*private void setRedeemCashback(int value) {
        //if(value!=mDebitCashback || value==0) {
            this.mDebitCashback = value;
            if(mDebitCashback==0) {
                mInputDebitCb.setText(AppCommonUtil.getAmtStr(mDebitCashback));
                mInputDebitCb.setTextColor(ContextCompat.getColor(getActivity(), R.color.secondary_text));
            } else {
                mInputDebitCb.setText(AppCommonUtil.getSignedAmtStr(mDebitCashback, false));
                mInputDebitCb.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
            }
            // recalculate cashback
            calcAndSetAddCb();
        //}
    }*/

    private void setAddCashback(int onBill, int onAcc) {
        mAddCbNormal = onBill;
        mAddCbExtra = onAcc;
        int total = mAddCbNormal + mAddCbExtra;
        mInputAddCb.setText(AppCommonUtil.getAmtStr(total));
    }

    private void setCashBalance() {
        LogMy.d(TAG,"In setCashBalance: "+mReturnCash);
        if(mReturnCash > 0) {
            String str = "Balance     "+ AppCommonUtil.getSignedAmtStr(mReturnCash, false);
            mInputToPayCash.setText(str);
            mInputToPayCash.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
            mDividerInputToPayCash.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red_negative));

        } else if(mReturnCash == 0) {
            String str = "Balance      "+AppConstants.SYMBOL_RS+" 0";
            mInputToPayCash.setText(str);
            mInputToPayCash.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_positive));
            mDividerInputToPayCash.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green_positive));

        } else {
            String str = "Balance      "+ AppCommonUtil.getSignedAmtStr(Math.abs(mReturnCash), true);
            mInputToPayCash.setText(str);
            mInputToPayCash.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
            mDividerInputToPayCash.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
        }
    }

    private void setCashPaid(int value) {
        mCashPaid = value;
    }

    private void calcAndSetAmts(boolean forcedRefresh) {
        LogMy.d(TAG,"Entering calcAndSetAmts: Bill:"+mRetainedFragment.mBillTotal+", cashPaid:"+mCashPaid);
        LogMy.d(TAG,"Amount status: mDebitClStatus:"+ mDebitClStatus +", mAddClStatus:"+mAddClStatus+", mOverdraftStatus:"+ mOverdraftStatus);

        // calculate add/redeem amounts fresh
        // We may have some values set, from earlier calculation
        // Reset those, and calculate all values again, based on new status
        mOverdraft = mDebitCashload = mAddCashload = 0;
        // Calculate both debit and add values
        calcDebitCl();
        //calcRedeemCb();
        calcAddCl();

        // Merge 'redeem cashload' and 'add cashload' values
        if(mDebitCashload > mAddCashload && mAddCashload > 0) {
            setDebitCashload(mDebitCashload - mAddCashload);
            setAddCashload(0);
        } else if(mAddCashload > mDebitCashload && mDebitCashload > 0) {
            setAddCashload(mAddCashload - mDebitCashload);
            setDebitCashload(0);
        }
        LogMy.d(TAG,"After merge cashload: "+mAddCashload+", "+ mDebitCashload);

        // calculate cash to pay
        int effectiveToPay = mRetainedFragment.mBillTotal - mDebitCashload;
        mReturnCash = mCashPaid - (effectiveToPay + mAddCashload);

        // if any change to be returned
        if(mReturnCash > 0) {
            // try to round off, so as mReturnCash becomes 0
            if(mDebitCashload >= mReturnCash) {
                setDebitCashload(mDebitCashload - mReturnCash);
                // calculate values again
                effectiveToPay = mRetainedFragment.mBillTotal - mDebitCashload;
                mReturnCash = mCashPaid - (effectiveToPay + mAddCashload);
            }
        }

        // if any amount to collect - try for overdraft
        if(mReturnCash < 0) {
            if(mOverdraftStatus==STATUS_AUTO) {
                setOverdraft(Math.abs(mReturnCash));
                mReturnCash = Math.abs(mReturnCash) - mOverdraft; //should be 0 always
            } else {
                setOverdraft(0);
            }
        }

        setCashBalance();

        // re-calculate states based on 'new value' and old state
        if(mAddCashload==0 && mAddClStatus==STATUS_AUTO) {
            setAddClStatus(STATUS_CLEARED);
        }
        if(mDebitCashload ==0 && mDebitClStatus ==STATUS_AUTO) {
            setDebitClStatus(STATUS_CLEARED);
        }
        if(mOverdraft ==0 && mOverdraftStatus ==STATUS_AUTO) {
            setOverdraftStatus(STATUS_CLEARED);
        }

        int oldMinCash = mMinCashToPay;
        calcMinCashToPay();
        if(oldMinCash!=mMinCashToPay || forcedRefresh) {
            mCashPaidHelper.refreshValues(mMinCashToPay, mCashPaid, mRetainedFragment.mBillTotal);
        }
    }


    private void calcDebitCl() {
        int effectiveToPay = 0;
        if(mDebitClStatus ==STATUS_AUTO) {
            // mDebitCashback will always be 0 - but added here just for completeness of formulae
            effectiveToPay = mRetainedFragment.mBillTotal - mDebitCashload;

            if( mCashPaid > effectiveToPay ) {
                // no point of any redeem, if cashPaid is already more than the amount
                setDebitCashload(0);
            } else {
                setDebitCashload(Math.min(mClBalance, (effectiveToPay - mCashPaid)));
            }
        } else if(mDebitClStatus < STATUS_DISABLED){
            // temporary disabled case
            setDebitCashload(0);
        }
        LogMy.d(TAG,"mDebitCashload: "+ mDebitCashload +", "+effectiveToPay);
    }

    /*private void calcRedeemCb() {
        int effectiveToPay = 0;
        if(mDebitCbStatus ==STATUS_AUTO) {
            //mDebitCashback = 0;
            effectiveToPay = mRetainedFragment.mBillTotal - mDebitCashback - mDebitCashload;

            if( mCashPaid > effectiveToPay ) {
                //setRedeemCashback(Math.min(mCbBalance, effectiveToPay));
                setRedeemCashback(0);
            } else {
                setRedeemCashback(Math.min(mCbBalance, (effectiveToPay - mCashPaid)));
            }
        //} else if (mDebitCbStatus ==STATUS_CLEARED || mDebitClStatus ==STATUS_NO_BILL_AMT) {
        } else if (mDebitCbStatus < STATUS_DISABLED) {
            // temporary disabled case
            setRedeemCashback(0);
        }
        LogMy.d(TAG,"mDebitCashback: "+ mDebitCashback +", "+effectiveToPay);
    }*/

    private void calcAddCl() {
        int effectiveToPay = 0;
        if(mAddClStatus==STATUS_AUTO) {
            //mAddCashload = 0;
            effectiveToPay = mRetainedFragment.mBillTotal - mDebitCashload;
            int addCash = (mCashPaid < effectiveToPay)?0:(mCashPaid - effectiveToPay);

            // check for limit
            int currAccBal = mRetainedFragment.mCurrCashback.getCurrClBalance();
            if( (currAccBal + addCash) > MyGlobalSettings.getCashAccLimit()) {
                // old balance + new will cross the account cash limit
                // update addCash value accordingly
                addCash = MyGlobalSettings.getCashAccLimit() - currAccBal;
            }
            setAddCashload(addCash);

        } else if (mAddClStatus < STATUS_DISABLED) {
            setAddCashload(0);
        }
        LogMy.d(TAG,"mAddCashload: "+mAddCashload+", "+effectiveToPay);
    }

    private void calcMinCashToPay() {
        // Min cash to pay = 'Bill amt' - 'all enabled debit amts'
        // If 'any one or both combined enabled debit amount' > 'bill amt', then mMinCashToPay = 0
        mMinCashToPay = mRetainedFragment.mBillTotal;
        if(mDebitClStatus==STATUS_AUTO) {
            mMinCashToPay = mMinCashToPay - Math.min(mClBalance, mMinCashToPay);
        }
        /*if(mDebitCbStatus ==STATUS_AUTO) {
            mMinCashToPay = mMinCashToPay - Math.min(mCbBalance, mMinCashToPay);
        }*/
        LogMy.d(TAG,"Exiting calcMinCashToPay: "+mMinCashToPay);
    }

    /*private void calcAndSetAddCb() {
        // calculate add cashback
        if(STATUS_DISABLED != mAddCbStatus) {

            boolean cbApply = (mRetainedFragment.mBillTotal > 0);
            boolean cbExtraApply = (mAddCashload >= mMerchantUser.getMerchant().getPrepaidCbMinAmt() && mPpCbRate>0);

            // calculate cashbacks
            int cbEligibleAmt = mRetainedFragment.mBillTotal - mRetainedFragment.mCbExcludedTotal - mDebitCashback;
            if(cbApply) {
                mAddCbNormal = (int) (cbEligibleAmt * mCbRate) / 100;
            } else {
                mAddCbNormal = 0;
            }
            if(cbExtraApply) {
                mAddCbExtra = (int) (mAddCashload * mPpCbRate) / 100;
            } else {
                mAddCbExtra = 0;
            }
            setAddCashback(mAddCbNormal, mAddCbExtra);
            LogMy.d(TAG, "mAddCbNormal: " + mAddCbNormal +", mAddCbExtra: "+ mAddCbExtra);

            // show cashback details
            String str = "";
            if(cbApply && cbExtraApply) {
                // both CB appl - show only rates - else string will be too long to display in single line
                str = "* Cashback  @ "+mCbRate+"% + "+mPpCbRate+"%";
            } else if(cbApply) {
                str = "* Cashback  @ "+mCbRate+"% of  "+ AppCommonUtil.getAmtStr(cbEligibleAmt);
            } else if(cbExtraApply) {
                str = "* Cashback  @ "+mPpCbRate+"% of  "+ AppCommonUtil.getAmtStr(mAddCashload);
            } else {
                // no cashback applies
                str = "* Cashback  ";
            }
            mLabelAddCb.setText(str);
        }
    }*/

    private void calcAndSetAddCb() {
        // calculate add cashback
        if(STATUS_DISABLED != mAddCbStatus) {

            boolean cbApply = (mCashPaid > 0);
            boolean cbExtraApply = (mAddCashload >= mMerchantUser.getMerchant().getPrepaidCbMinAmt() && mPpCbRate>0);

            // calculate cashbacks
            mCbEligibleAmt = mExtraCbEligibleAmt = mAddCbExtra = mAddCbNormal = 0;

            if(cbApply && cbExtraApply) {
                // both normal and extraCb apply
                mCbEligibleAmt = mCashPaid - mAddCashload;
                mExtraCbEligibleAmt = mAddCashload;
            } else {
                // only one of normal and extraCb apply
                if(cbApply) {
                    mCbEligibleAmt = mCashPaid;
                } else if(cbExtraApply) {
                    // mCashPaid == mAddCashload
                    mExtraCbEligibleAmt = mAddCashload;
                } else {
                    // I shouldn't be here
                }
            }

            mAddCbNormal = (int) (mCbEligibleAmt * mCbRate) / 100;
            mAddCbExtra = (int) (mExtraCbEligibleAmt * mPpCbRate) / 100;
            setAddCashback(mAddCbNormal, mAddCbExtra);

            LogMy.d(TAG, "mAddCbNormal: " + mAddCbNormal +", mAddCbExtra: "+ mAddCbExtra);
        }
    }

    private boolean billAmtEditAllowed() {
        return true;
        // To avoid inconsistancy, editing allowed only if:
        // 0) No item added
        // 1) single item in order &
        //  1a) that item has single quantity and
        //  1b) it is not cashback excluded case
        // basically, only when merchant have probably entered final order cost only
        /*if(mRetainedFragment.mOrderItems == null ||
                mRetainedFragment.mOrderItems.size() == 0) {
            return true;
        } else {
            if(mRetainedFragment.mOrderItems.size() == 1 &&
                    mRetainedFragment.mOrderItems.get(0).getQuantity() == 1 &&
                    mRetainedFragment.mCbExcludedTotal==0) {
                return true;
            } else {
                return false;
            }
        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogMy.d(TAG, "In onActivityResult :" + requestCode + ", " + resultCode);

        try {
            switch (requestCode) {
                case REQ_NEW_BILL_AMT:
                    if (resultCode != Activity.RESULT_OK) {
                        return;
                    }
                    LogMy.d(TAG, "Received new bill amount.");
                    String newBillAmt = (String) data.getSerializableExtra(NumberInputDialog.EXTRA_INPUT_HUMBER);
                    mRetainedFragment.mBillTotal = Integer.parseInt(newBillAmt);
                    displayInputBillAmt();
                    updateAmtUiStates();

                    // re-calculate all amounts
                    calcAndSetAmts(false);
                    calcAndSetAddCb();
                    break;

                case REQ_CASH_PAID_AMT:
                    if (resultCode != Activity.RESULT_OK) {
                        return;
                    }
                    LogMy.d(TAG, "Received new cash paid amount.");
                    String newCashAmt = (String) data.getSerializableExtra(NumberInputDialog.EXTRA_INPUT_HUMBER);
                    if(mCashPaidHelper==null) {
                        mCashPaidHelper = new CashPaid2(mMinCashToPay, mRetainedFragment.mBillTotal, this, getActivity());
                    }
                    mCashPaidHelper.onCustomAmtEnter(newCashAmt);

                    break;

                case REQ_NOTIFY_ERROR:
                    //mCallback.restartTxn();
                    // do nothing
                    break;

                case REQ_NOTIFY_ERROR_EXIT:
                    //getActivity().onBackPressed();
                    mCallback.restartTxn();
                    break;
            }
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    @Override
    public void onAmountEnterFinal(int value, boolean clearCase) {
        LogMy.d(TAG,"In onAmountEnterFinal: "+value);

        setCashPaid(value);
        updateAmtUiStates();
        calcAndSetAmts(false);
    }

    @Override
    public void collectCustomAmount(String curValue, int minValue) {
        startNumInputDialog(REQ_CASH_PAID_AMT, "Cash Paid:", curValue, minValue, 0);
    }

    private void setTransactionValues() {
        LogMy.d(TAG, "In setTransactionValues");
        Transaction trans = new Transaction();
        // Set only the amounts
        trans.setTotal_billed(mRetainedFragment.mBillTotal);
        trans.setCb_eligible_amt(mCbEligibleAmt);
        trans.setExtracb_eligible_amt(mExtraCbEligibleAmt);

        //trans.setCb_billed(mRetainedFragment.mBillTotal - mRetainedFragment.mCbExcludedTotal);
        trans.setCl_credit(mAddCashload);
        trans.setCb_credit(mAddCbNormal);
        trans.setExtra_cb_credit(mAddCbExtra);

        trans.setCl_debit(mDebitCashload);
        trans.setCl_overdraft(mOverdraft);

        trans.setCb_percent(String.valueOf(mCbRate));
        trans.setExtra_cb_percent(String.valueOf(mPpCbRate));

        trans.setCust_private_id(mRetainedFragment.mCurrCustomer.getPrivateId());

        mRetainedFragment.mCurrTransaction = new MyTransaction(trans);
    }

    // Not using BaseFragment's onTouch
    // as we dont want 'double touch' check against these buttons
    @Override
    public boolean handleTouchUp(View v) {
        return false; // do nothing
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(!mCallback.getRetainedFragment().getResumeOk())
            return true;

        try {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                LogMy.d(TAG, "In onTouch: " + v.getId());

                //mLayoutErrorDetails.setVisibility(View.GONE);

                int i = v.getId();
                if (i==R.id.img_trans_bill_amt || i==R.id.label_trans_bill_amt || i==R.id.input_trans_bill_amt) {
                    // open 'bill amount' for editing
                    long currentClickTime= SystemClock.uptimeMillis();
                    long elapsedTime=currentClickTime-newBillDialogOpenTime;
                    newBillDialogOpenTime=currentClickTime;

                    if(elapsedTime >= AppConstants.MIN_CLICK_INTERVAL) {
                        if (billAmtEditAllowed()) {
                            String amount = mInputBillAmt.getText().toString().replace(AppConstants.SYMBOL_RS, "").replace("+", "").replace(" ", "");
                            startNumInputDialog(REQ_NEW_BILL_AMT, "Bill Amount:", amount, 0, 0);
                        } else {
                            mCallback.onViewOrderList();
                        }
                    }

                } else if (i == R.id.img_account || i == R.id.label_account || i == R.id.input_account) {

                    String errorStr = null;

                    if(mAddCashload > 0) {
                        // already set for add, clear it on touch
                        setAddClStatus(STATUS_CLEARED);
                        calcAndSetAmts(false);

                    } else if(mDebitCashload > 0) {
                        // already set for debit, clear it on touch
                        setDebitClStatus(STATUS_CLEARED);
                        calcAndSetAmts(false);

                    } else if(mCashPaid == mRetainedFragment.mBillTotal) {
                        AppCommonUtil.toast(getActivity(), "Full Payment selected already");

                    } else{
                        if(mCashPaid > 0 && mCashPaid >= mMinCashToPay) {
                            // add case
                            if(mAddClStatus==STATUS_CLEARED) {
                                setAddClStatus(STATUS_AUTO);
                                calcAndSetAmts(false);
                            } else if(mDebitClStatus!=STATUS_AUTO) {
                                switch (mDebitClStatus) {
                                    case STATUS_DISABLED:
                                        errorStr = "Enable Add Cash in Settings";
                                        break;
                                    case STATUS_ACCOUNT_FULL:
                                        errorStr = "Add Cash Account Limit of "+MyGlobalSettings.getCashAccLimit()+" Reached";
                                        break;
                                    case STATUS_CASH_PAID_NOT_SET:
                                        errorStr = "To Add, Set Cash Paid";
                                        break;
                                    default:
                                        errorStr = statusDesc.get(mAddClStatus);
                                }
                            }
                        } else {
                            // debit case
                            if(mDebitClStatus==STATUS_CLEARED) {
                                if(mReturnCash==0) {
                                    AppCommonUtil.toast(getActivity(), "Balance Already Zero");
                                } else {
                                    setDebitClStatus(STATUS_AUTO);
                                    calcAndSetAmts(false);
                                }
                            } else if(mDebitClStatus!=STATUS_AUTO) {
                                switch (mDebitClStatus) {
                                    case STATUS_NO_BALANCE:
                                        errorStr = "No Balance for Debit";
                                        break;
                                    case STATUS_NO_BILL_AMT:
                                        errorStr = "No Bill Amount for Debit";
                                        break;
                                    default:
                                        errorStr = statusDesc.get(mDebitClStatus);
                                }
                            }
                        }

                        if(errorStr!=null) {
                            //mLayoutErrorDetails.setVisibility(View.VISIBLE);
                            //mLabelErrorDetails.setText(errorStr);
                            AppCommonUtil.toast(getActivity(),errorStr);
                        }
                    }

                } else if (i == R.id.layout_cashback_add || i == R.id.label_cashback_add || i == R.id.input_cashback_add) {

                    // clicking has no impact on 'add cb' status - so not calling updateAmtUiStates() and calcAndSetAmts()

                    String errorStr = null;
                    if(mAddCbStatus==STATUS_AUTO) {
                        if(mCbRate<=0) {
                            errorStr = "Cashback            : 0%";
                        } else if(mCashPaid==0) {
                            errorStr = "Cashback            : Payment not selected yet";
                        } else {
                            //int cbEligibleAmt = mRetainedFragment.mBillTotal - mRetainedFragment.mCbExcludedTotal - mDebitCashback;
                            errorStr = "Cashback            : " + AppCommonUtil.getAmtStr(mAddCbNormal) +
                            " @ "+mCbRate+"% of "+AppCommonUtil.getAmtStr(mCbEligibleAmt);
                        }

                        if(mPpCbRate<=0) {
                            errorStr = errorStr + "\nExtra Cashback  : 0%";
                        } else if(mAddCashload <= mMerchantUser.getMerchant().getPrepaidCbMinAmt()) {
                            errorStr = errorStr + "\nExtra Cashback  : Account Deposit less than "+
                                    AppCommonUtil.getAmtStr(mMerchantUser.getMerchant().getPrepaidCbMinAmt());
                        } else {
                            errorStr = errorStr + "\nExtra Cashback  : " + AppCommonUtil.getAmtStr(mAddCbExtra) +
                                    " @ "+mPpCbRate+"% of "+AppCommonUtil.getAmtStr(mExtraCbEligibleAmt);
                        }

                        AppCommonUtil.toast(getActivity(), errorStr);

                    } else {
                        AppCommonUtil.toast(getActivity(), statusDesc.get(mAddClStatus));
                    }

                } else if (i == R.id.ct_layout_overdraft || i == R.id.ct_label_overdraft || i == R.id.ct_input_overdraft) {

                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in CashTxnFragment:onTouch", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }

        return true;
    }

    /*private void askCardScan(String msg) {
        Snackbar mySnackbar = Snackbar.make(mCoordinatorLayout, msg, Snackbar.LENGTH_LONG);
        mySnackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.green_positive));
        mySnackbar.setAction("SCAN", new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                LogMy.d(TAG, "Card Scan confirmation");
                Intent intent = new Intent(getActivity(), BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                mRetainedFragment.mCardImageFilename = CommonConstants.PREFIX_TXN_IMG_FILE_NAME+Long.toString(System.currentTimeMillis())+"."+CommonConstants.PHOTO_FILE_FORMAT;
                intent.putExtra(BarcodeCaptureActivity.ImageFileName,mRetainedFragment.mCardImageFilename);
                startActivityForResult(intent, RC_BARCODE_CAPTURE_CASH_TXN);
            }
        });
        mySnackbar.show();
    }*/

    // Not using BaseFragment's onClick method
    @Override
    public void handleBtnClick(View v) {
        // do nothing
    }

    private void startNumInputDialog(int reqCode, String label, String curValue, int minValue, int maxValue) {
        FragmentManager manager = getFragmentManager();
        NumberInputDialog dialog = NumberInputDialog.newInstance(label, curValue, true, minValue, maxValue);
        dialog.setTargetFragment(this, reqCode);
        dialog.show(manager, DIALOG_NUM_INPUT);
    }

    private void initListeners() {
        //mLayoutErrorDetails.setOnTouchListener(this);
        //mLabelErrorDetails.setOnTouchListener(this);

        // can change bill amount
        mImgBill.setOnTouchListener(this);
        mLabelBill.setOnTouchListener(this);
        mInputBillAmt.setOnTouchListener(this);

        //mLayoutAccount.setOnTouchListener(this);
        mImgAccount.setOnTouchListener(this);
        mLabelAccount.setOnTouchListener(this);
        mInputAccount.setOnTouchListener(this);

        /*mLayoutDebitCb.setOnTouchListener(this);
        mImgDebitCb.setOnTouchListener(this);
        mLabelDebitCb.setOnTouchListener(this);
        mInputDebitCb.setOnTouchListener(this);*/

        mLabelOverdraft.setOnTouchListener(this);
        mImgOverdraft.setOnTouchListener(this);
        mLabelOverdraft.setOnTouchListener(this);
        mInputOverdraft.setOnTouchListener(this);

        mLayoutAddCb.setOnTouchListener(this);
        mLabelAddCb.setOnTouchListener(this);
        mInputAddCb.setOnTouchListener(this);

        mInputToPayCash.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                LogMy.d(TAG, "Clicked Process txn button");
                if (mAddClStatus == STATUS_AUTO ||
                        mDebitClStatus == STATUS_AUTO ||
                        mAddCbStatus == STATUS_AUTO ||
                        mOverdraft == STATUS_AUTO) {
                    // If all 0 - no point going ahead
                    // This may happen, if this txn involves only cashback and
                    // that cashback is less than 1 rupee - which will be rounded of to 0
                    if ((mAddCbNormal + mAddCbExtra) <= 0 && mAddCashload <= 0 && mOverdraft <= 0 && mDebitCashload <= 0) {
                        //AppCommonUtil.toast(getActivity(), "No MyeCash data to process !!");
                        String msg;
                        if (mRetainedFragment.mBillTotal <= 0) {
                            msg = "All Credit/Debit amounts are 0, for both Cashback and Account";
                        } else {
                            msg = "'Award Cashback' is less than 1 Rupee. Other credit/debit amount are 0.";
                        }
                        DialogFragmentWrapper dialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, msg, true, true);
                        dialog.setTargetFragment(CashTransactionFragment_2.this, REQ_NOTIFY_ERROR);
                        dialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
                        return;
                    }
                    //if (getCashPaidError() == null) {
                    if (mCashPaid >= 0) {
                        if (mReturnCash != 0) {
                            AppCommonUtil.toast(getActivity(), "Balance not 0 yet");
                        } else {
                            setTransactionValues();
                            mCallback.onTransactionSubmit(mCashPaid);
                        }
                    } else {
                        AppCommonUtil.toast(getActivity(), "Set Cash Paid");
                    }
                } else {
                    AppCommonUtil.toast(getActivity(), "No MyeCash data to process !!");
                }
            }
        });

    }

    private void setAddClStatus(int status) {
        LogMy.d(TAG, "In setAddClStatus: " + status);
        //if(status!=mAddClStatus) {
            mAddClStatus = status;
            showAccountStatus();
        //}
    }

    private void setDebitClStatus(int status) {
        LogMy.d(TAG, "In setDebitClStatus: "+status);
        //if(status!=mDebitClStatus) {
            mDebitClStatus = status;
            showAccountStatus();
        //}
    }

    private void showAccountStatus() {
        /*if(mAddClStatus==STATUS_DISABLED && mDebitClStatus==STATUS_NO_BALANCE) {
            // Account facility is not available in FREE account - so it should come here in that case
            mLayoutAccount.setVisibility(View.GONE);

        } else*/ if(mAddClStatus!=STATUS_AUTO && mDebitClStatus!=STATUS_AUTO) {
            mLayoutAccount.setAlpha(0.4f);
            mImgAccount.setColorFilter(ContextCompat.getColor(getActivity(), R.color.disabled), PorterDuff.Mode.SRC_IN);
            //mLabelAccount.setEnabled(false);
            //mInputAccount.setEnabled(false);

        } else {
            mLayoutAccount.setAlpha(1.0f);
            mImgAccount.setColorFilter(ContextCompat.getColor(getActivity(), R.color.primary), PorterDuff.Mode.SRC_IN);
            //mLabelAccount.setEnabled(true);
            //mInputAccount.setEnabled(true);
        }
    }

    private void setOverdraftStatus(int status) {
        LogMy.d(TAG, "In setOverdraftStatus: "+status);
        mOverdraftStatus = status;

        if(mOverdraftStatus != STATUS_AUTO) {
            mLayoutOverdraft.setAlpha(0.5f);
            mLayoutOverdraft.setBackgroundResource(R.drawable.round_rect_border_disabled);
            mImgOverdraft.setColorFilter(R.color.disabled, PorterDuff.Mode.SRC_IN);
            mLabelOverdraft.setTextColor(ContextCompat.getColor(getActivity(), R.color.disabled));
            mInputOverdraft.setTextColor(ContextCompat.getColor(getActivity(), R.color.disabled));
        } else {
            mLayoutOverdraft.setAlpha(1.0f);
            mLayoutOverdraft.setBackgroundResource(R.drawable.round_rect_border_red);
            mImgOverdraft.setColorFilter(R.color.red_negative, PorterDuff.Mode.SRC_IN);
            mLabelOverdraft.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary));
            mInputOverdraft.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
        }
    }

    private void setAddCbStatus(int status) {
        LogMy.d(TAG, "In setAddCbStatus: " + status);
        //if(status!=mAddCbStatus) {
            mAddCbStatus = status;
            if (mAddCbStatus == STATUS_AUTO) {
                mLayoutAddCb.setAlpha(1.0f);
                //mLabelAddCb.setEnabled(true);
                //mInputAddCb.setEnabled(true);
            } else {
                mLayoutAddCb.setAlpha(0.5f);
                //mLabelAddCb.setEnabled(false);
                //mInputAddCb.setEnabled(false);
            }
        //}
    }

    /*
     * Calculates 'states' based on parameters which can't be changed from this screen
     * i.e, merchant settings, and account/cashback balance
     * So, this fx. should be called only once - when this fragment is created
     */
    private void initAmtUiStates() {
        LogMy.d(TAG, "In initAmtUiStates");

        // Init 'add cash' status
        if(mMerchantUser.getMerchant().getCl_add_enable()) {

            if(mRetainedFragment.mCurrCashback.getCurrClBalance() >= MyGlobalSettings.getCashAccLimit()) {
                setAddClStatus(STATUS_ACCOUNT_FULL);
            } else {
                // by default, dont try add cash
                setAddClStatus(STATUS_CLEARED);
            }
        } else {
            setAddClStatus(STATUS_DISABLED);
        }

        // Init 'debit cash' status
        mClBalance = mRetainedFragment.mCurrCashback.getCurrClBalance();
        /*if(!isCardStatusOk() && MyGlobalSettings.getCardReqAccDebit()) {
            setDebitClStatus(STATUS_CARD_NOT_ACTIVE);
        } else*/ if(mClBalance <= 0) {
            setDebitClStatus(STATUS_NO_BALANCE);
        } else {
            // by default, debit if available
            setDebitClStatus(STATUS_AUTO);
        }

        // Init overdraft status
        if(mMerchantUser.getMerchant().getCl_overdraft_enable()) {

            if(Math.abs(mRetainedFragment.mCurrCashback.getCurrClBalance()) >= MyGlobalSettings.getAccOverdraftLimit()) {
                setOverdraftStatus(STATUS_ACCOUNT_FULL);
            } else {
                // by default, dont enable overdraft
                setOverdraftStatus(STATUS_CLEARED);
            }
        } else {
            setOverdraftStatus(STATUS_DISABLED);
        }

        // Init 'debit cashback' status
        /*mCbBalance = mRetainedFragment.mCurrCashback.getCurrCbBalance();
        if(mCbBalance<= 0) {
            setDebitCbStatus(STATUS_NO_BALANCE);
        } else if( mCbBalance < MyGlobalSettings.getCbRedeemLimit()) {
            setDebitCbStatus(STATUS_BALANCE_BELOW_LIMIT);
        } else {
            // by default, dont try cb debit
            setDebitCbStatus(STATUS_CLEARED);
        }*/

        // Init 'add cashback' status
        if( (mCbRate > 0) ||
                (mPpCbRate > 0 && mMerchantUser.getMerchant().getCl_add_enable()) ) {
            setAddCbStatus(STATUS_AUTO);
        } else {
            setAddCbStatus(STATUS_DISABLED);
        }

        // Cash Paid section
        if(mCashPaidHelper==null) {
            calcMinCashToPay();
            mCashPaidHelper = new CashPaid2(mMinCashToPay, mRetainedFragment.mBillTotal, this, getActivity());
        }
        mCashPaidHelper.initView(getView());
    }

    /*
     * Updates 'states' based on parameters that can be changed from this screen also
     * i.e. Bill amount, cash paid, and card scan
     * So, this fx. should be called, each time any of above parameters change
     */
    private void updateAmtUiStates() {
        LogMy.d(TAG, "In updateAmtUiStates");

        // 'add cash' status
        if(mAddClStatus < STATUS_DISABLED) {
            // not permanently disabled
            int newStatus;
            // first check if need to be temporary disabled
            if(mCashPaid == 0) {
                newStatus = STATUS_CASH_PAID_NOT_SET;
            } else {
                newStatus = (mAddClStatus==STATUS_AUTO) ? STATUS_AUTO : STATUS_CLEARED;
            }
            setAddClStatus(newStatus);
        }

        // 'debit cash' status
        if(mDebitClStatus < STATUS_DISABLED) {
            // not permanently disabled
            int newStatus;

            // first check if need to be temporary disabled
            /*if(!isCardPresented() && MyGlobalSettings.getCardReqAccDebit()) {
                newStatus = STATUS_QR_CARD_NOT_USED;
            } else*/ if(mRetainedFragment.mBillTotal <= 0) {
                newStatus = STATUS_NO_BILL_AMT;
            } else {
                // if here, means no need for temporary disable
                /*if(cardScanAmtId==R.id.input_account) {
                    // special case of being called after card scan initiated by touch
                    newStatus = STATUS_AUTO;
                    cardScanAmtId = 0;
                } else {*/
                    // preserve overall status
                    newStatus = (mDebitClStatus == STATUS_AUTO) ? STATUS_AUTO : STATUS_CLEARED;
                //}
            }
            setDebitClStatus(newStatus);
        }

        // 'debit cashback' status
        if(mDebitCbStatus < STATUS_DISABLED) {
            // not permanently disabled
            int newStatus;

            // first check if need to be temporary disabled
            /*if(!isCardPresented() && MyGlobalSettings.getCardReqAccDebit()) {
                newStatus = STATUS_QR_CARD_NOT_USED;
            } else*/ if(mRetainedFragment.mBillTotal <= 0) {
                newStatus = STATUS_NO_BILL_AMT;
            } else {
                // if here, means no need for temporary disable
                /*if(cardScanAmtId==R.id.input_cashback) {
                    // special case of being called after card scan initiated by touch
                    newStatus = STATUS_AUTO;
                    cardScanAmtId = 0;
                } else {*/
                    // preserve overall status
                    newStatus = (mDebitCbStatus == STATUS_AUTO) ? STATUS_AUTO : STATUS_CLEARED;
                //}
            }
            setDebitCbStatus(newStatus);
        }

        // 'add cashback' status
        // no impact
        /*if(mAddClStatus < STATUS_DISABLED) {
            // not permanently disabled
            // means atleast one type of'cashback' i enabled in settings (normal or extra)

            if(mCbRate>0 && mRetainedFragment.mBillTotal<=0) {
                setAddCbStatus(STATUS_NO_BILL_AMT);
            }

            if( (mRetainedFragment.mBillTotal <= 0) &&
                    (mPpCbRate <= 0 || mMerchantUser.getMerchant().getCl_add_enable()) ) {
                setAddCbStatus(STATUS_NO_BILL_AMT);
            }
        }*/
    }

    // UI Resources data members
    private View mCoordinatorLayout;
    //private View mLayoutErrorDetails;
    //private EditText mLabelErrorDetails;

    private ImageView mImgBill;
    private View mLabelBill;
    private EditText mInputBillAmt;

    private View mLayoutAccount;
    private ImageView mImgAccount;
    private View mLabelAccount;
    private EditText mInputAccount;

    /*private View mLayoutDebitCb;
    private ImageView mImgDebitCb;
    private View mLabelDebitCb;
    private EditText mInputDebitCb;*/

    private View mLayoutOverdraft;
    private ImageView mImgOverdraft;
    private EditText mLabelOverdraft;
    private EditText mInputOverdraft;

    private View mLayoutAddCb;
    private EditText mLabelAddCb;
    private EditText mInputAddCb;

    private View mDividerInputToPayCash;
    private AppCompatButton mInputToPayCash;

    private void bindUiResources(View v) {

        mCoordinatorLayout = v.findViewById(R.id.myCoordinatorLayout);

        //mLayoutErrorDetails = v.findViewById(R.id.layout_error_detail);
        //mLabelErrorDetails = (EditText)v.findViewById(R.id.label_error_detail);

        mImgBill = (ImageView) v.findViewById(R.id.img_trans_bill_amt);
        mLabelBill = v.findViewById(R.id.label_trans_bill_amt);
        mInputBillAmt = (EditText) v.findViewById(R.id.input_trans_bill_amt);

        mLayoutAccount = v.findViewById(R.id.layout_account);
        mImgAccount = (ImageView) v.findViewById(R.id.img_account);
        mLabelAccount = v.findViewById(R.id.label_account);
        mInputAccount = (EditText) v.findViewById(R.id.input_account);

        /*mLayoutDebitCb = v.findViewById(R.id.layout_cb);
        mImgDebitCb = (ImageView) v.findViewById(R.id.img_cashback);
        mLabelDebitCb = v.findViewById(R.id.label_cashback);
        mInputDebitCb = (EditText) v.findViewById(R.id.input_cashback);*/

        mLayoutOverdraft = v.findViewById(R.id.layout_overdraft);
        mImgOverdraft = (ImageView) v.findViewById(R.id.ct_img_overdraft);
        mLabelOverdraft = (EditText) v.findViewById(R.id.ct_label_overdraft);
        mInputOverdraft = (EditText) v.findViewById(R.id.ct_input_overdraft);

        mLayoutAddCb = v.findViewById(R.id.layout_cashback_add);
        mLabelAddCb = (EditText) v.findViewById(R.id.label_cashback_add);
        mInputAddCb = (EditText) v.findViewById(R.id.input_cashback_add);

        mDividerInputToPayCash = v.findViewById(R.id.divider_btn_collect_cash);
        mInputToPayCash = (AppCompatButton) v.findViewById(R.id.btn_collect_cash);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCallback.setDrawerState(false);

        try {
            displayInputBillAmt();
            // re-calculate all amounts
            updateAmtUiStates();
            calcAndSetAmts(true);
            calcAndSetAddCb();
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in CustomerTransactionFragment:onResume", e);
            DialogFragmentWrapper dialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            dialog.setTargetFragment(this, REQ_NOTIFY_ERROR_EXIT);
            dialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            //throw e;
        }

        mCallback.getRetainedFragment().setResumeOk(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        AppCommonUtil.cancelToast();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        LogMy.d(TAG, "In onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putInt("mClBalance", mClBalance);
        //outState.putInt("mCbBalance", mCbBalance);
        outState.putInt("mMinCashToPay", mMinCashToPay);

        outState.putInt("mAddCbStatus", mAddCbStatus);
        outState.putInt("mAddClStatus", mAddClStatus);
        outState.putInt("mDebitClStatus", mDebitClStatus);
        //outState.putInt("mDebitCbStatus", mDebitCbStatus);
        //outState.putBoolean("mDebitCbOnPriority", mDebitCbOnPriority);

        outState.putInt("mDebitCashload", mDebitCashload);
        //outState.putInt("mDebitCashback", mDebitCashback);
        outState.putInt("mAddCashload", mAddCashload);
        outState.putInt("mAddCbNormal", mAddCbNormal);
        outState.putInt("mAddCbExtra", mAddCbExtra);
        outState.putInt("mCashPaid", mCashPaid);
        outState.putInt("mReturnCash", mReturnCash);
    }
}