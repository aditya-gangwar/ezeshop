package in.ezeshop.merchantbase;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.ColorRes;
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
    private static final int REQ_CONFIRM_OVERDRAFT = 5;
    private static final int REQ_NOTIFY_ERROR = 7;
    private static final int REQ_NOTIFY_ERROR_EXIT = 8;

    private static final String DIALOG_NUM_INPUT = "NumberInput";


    // enabled state
    private static final int STATUS_AUTO = 0;

    // temporary disabled states i.e. can be changed from this screen also
    private static final int STATUS_CLEARED = 11;    // explicitly cleared by user by clicking
    private static final int STATUS_CASH_PAID_NOT_SET = 14;
    private static final int STATUS_NO_BILL_AMT = 15;

    // permanent disabled states i.e. cant be changed from this screen
    private static final int STATUS_DISABLED = 21;
    private static final int STATUS_NO_BALANCE = 22;
    private static final int STATUS_BALANCE_BELOW_LIMIT = 23;
    private static final int STATUS_ACCOUNT_FULL = 24;
    private static final int STATUS_ONLINE_ORDER = 25;


    public static final Map<Integer, String> statusDesc;
    static {
        Map<Integer, String> aMap = new HashMap<>(10);
        aMap.put(STATUS_CASH_PAID_NOT_SET,"Payment Not Set");
        aMap.put(STATUS_NO_BILL_AMT,"No Bill Amount");

        aMap.put(STATUS_DISABLED,"Disabled in Settings");
        aMap.put(STATUS_NO_BALANCE,"No Account Balance");
        aMap.put(STATUS_BALANCE_BELOW_LIMIT,"Balance below "+AppCommonUtil.getAmtStr(MyGlobalSettings.getCbRedeemLimit()));
        aMap.put(STATUS_ACCOUNT_FULL,"Account Limit Reached");
        aMap.put(STATUS_ONLINE_ORDER,"Not allowed for Online Customer order");
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

    private int mAddCashload;
    private int mDelCharges;
    private int mAddCbNormal;
    private int mAddCbExtra;

    private int mCashPaid;
    private int mReturnCash;

    private int mCbEligibleAmt;
    private int mExtraCbEligibleAmt;

    private int mClBalance;
    private int mMinCashToPay;

    private float mCbRate;
    private float mPpCbRate;

    // Part of instance state: to be restored in event of fragment recreation
    private int mAddClStatus;
    private int mDebitClStatus;
    private int mOverdraftStatus;
    private int mAddCbStatus;
    private int mDelChgsStatus;

    private long newBillDialogOpenTime;

    // Container Activity must implement this interface
    public interface CashTransactionFragmentIf {
        MyRetainedFragment getRetainedFragment();
        void onTransactionSubmit(int cashPaid);
        void setDrawerState(boolean isEnabled);
        void restartTxn();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogMy.d(TAG, "In onCreateView");
        View v = inflater.inflate(R.layout.fragment_cash_txn_6, container, false);

        // access to UI elements
        bindUiResources(v);
        //setup all listeners
        initListeners();

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        LogMy.d(TAG, "In onActivityCreated");
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
                    LogMy.d(TAG, "Fragment re-create case");
                    // restore status from stored values
                    mClBalance = savedInstanceState.getInt("mClBalance");
                    mMinCashToPay = savedInstanceState.getInt("mMinCashToPay");

                    setAddCbStatus(savedInstanceState.getInt("mAddCbStatus"));
                    setAddClStatus(savedInstanceState.getInt("mAddClStatus"));
                    setDebitClStatus(savedInstanceState.getInt("mDebitClStatus"));
                    setOverdraftStatus(savedInstanceState.getInt("mOverdraftStatus"));
                    setDelChgsStatus(savedInstanceState.getInt("mDelChgsStatus"));
                }
            } else {
                // these fxs update onscreen view also, so need to be run for backstack scenario too
                setAddCbStatus(mAddCbStatus);
                setAddClStatus(mAddClStatus);
                setDebitClStatus(mDebitClStatus);
                setOverdraftStatus(mOverdraftStatus);
                setDelChgsStatus(mDelChgsStatus);
            }

            // Init view - only to be done after states are set above
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
                    setOverdraft(savedInstanceState.getInt("mOverdraft"));

                    setAddCashload(savedInstanceState.getInt("mAddCashload"));
                    setAddCashback(savedInstanceState.getInt("mAddCbNormal"), savedInstanceState.getInt("mAddCbExtra"));

                    setCashPaid(savedInstanceState.getInt("mCashPaid"));
                    mReturnCash = savedInstanceState.getInt("mReturnCash");

                    mCbEligibleAmt = savedInstanceState.getInt("mCbEligibleAmt");
                    mExtraCbEligibleAmt = savedInstanceState.getInt("mExtraCbEligibleAmt");
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
        int value = mRetainedFragment.mBillAmount;
        AppCommonUtil.showAmtColor(getActivity(), null, mInputBillAmt, value,false);
        mLayoutBillAmt.setAlpha(value==0?0.4f:1.0f);
        mImgBill.setColorFilter(ContextCompat.getColor(getActivity(),
                value==0?R.color.disabled:R.color.primary),
                PorterDuff.Mode.SRC_IN);

        /*if(mRetainedFragment.mBillAmount==0) {
            mInputBillAmt.setText(AppConstants.SYMBOL_RS_0);
            mLayoutBillAmt.setAlpha(0.4f);
        } else {
            mInputBillAmt.setText(AppCommonUtil.getNegativeAmtStr(mRetainedFragment.mBillAmount, true));
            mLayoutBillAmt.setAlpha(1.0f);
        }*/
    }

    private void setAddCashload(int value) {
        if(value!=mAddCashload || value==0) {
            this.mAddCashload = value;
            setAccountAmt();
            // Calling, as 'Prepaid extra cashback' may need to be applied
            calcAndSetAddCb();
        }
    }

    private void setDebitCashload(int value) {
        if(value!=mDebitCashload || value==0) {
            this.mDebitCashload = value;
            setAccountAmt();
        }
    }

    private void setOverdraft(int value) {
        if(value!=mOverdraft) {
            this.mOverdraft = value;
            mInputOverdraft.setText(AppCommonUtil.getNegativeAmtStr(mOverdraft));
        }
    }

    private void setAccountAmt() {
        int value = mAddCashload - mDebitCashload;
        AppCommonUtil.showAmtColor(getActivity(), null, mInputAccount, value, true);

        /*if(mAddCashload>0) {
            mInputAccount.setText(AppCommonUtil.getNegativeAmtStr(mAddCashload, true));
            mInputAccount.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_positive));

        } else if(mDebitCashload>0) {
            mInputAccount.setText(AppCommonUtil.getNegativeAmtStr(mDebitCashload, false));
            mInputAccount.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));

        } else {
            mInputAccount.setText(AppCommonUtil.getAmtStr(0));
            mInputAccount.setTextColor(ContextCompat.getColor(getActivity(), R.color.secondary_text));
        }*/
    }

    private void setAddCashback(int onBill, int onAcc) {
        mAddCbNormal = onBill;
        mAddCbExtra = onAcc;
        int total = mAddCbNormal + mAddCbExtra;
        mInputAddCb.setText(AppCommonUtil.getAmtStr(total));
    }

    private void setCashBalance() {
        LogMy.d(TAG,"In setCashBalance: "+mReturnCash);

        // set text
        String str = "Balance     "+ AppCommonUtil.getSignedAmtStr(mReturnCash);
        mInputToPayCash.setText(str);

        // set color
        @ColorRes int color = mReturnCash==0?R.color.green_positive:R.color.red_negative;
        mInputToPayCash.setTextColor(ContextCompat.getColor(getActivity(), color));
        mDividerInputToPayCash.setBackgroundColor(ContextCompat.getColor(getActivity(), color));
    }

    private void setCashPaid(int value) {
        mCashPaid = value;
    }

    private void calcAndSetAmts(boolean forcedRefresh) {
        LogMy.d(TAG,"Entering calcAndSetAmts: Bill:"+mRetainedFragment.mBillAmount +", cashPaid:"+mCashPaid);
        LogMy.d(TAG,"Amount status: mDebitClStatus:"+ mDebitClStatus +", mAddClStatus:"+mAddClStatus+", mOverdraftStatus:"+ mOverdraftStatus);

        // calculate add/redeem amounts fresh
        // We may have some values set, from earlier calculation
        // Reset those, and calculate all values again, based on new status
        mOverdraft = mDebitCashload = mAddCashload = 0;
        // Calculate both debit and add values
        calcDebitCl();
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
        int effectiveToPay = (mRetainedFragment.mBillAmount + mDelCharges) - mDebitCashload;
        mReturnCash = mCashPaid - (effectiveToPay + mAddCashload);

        // if any change to be returned
        if(mReturnCash > 0) {
            // try to round off, so as mReturnCash becomes 0
            if(mDebitCashload >= mReturnCash) {
                setDebitCashload(mDebitCashload - mReturnCash);
                // calculate values again
                effectiveToPay = (mRetainedFragment.mBillAmount + mDelCharges) - mDebitCashload;
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
            mCashPaidHelper.refreshValues(mMinCashToPay, mCashPaid, (mRetainedFragment.mBillAmount + mDelCharges));
        }
    }


    private void calcDebitCl() {
        int effectiveToPay = 0;
        if(mDebitClStatus ==STATUS_AUTO) {
            // mDebitCashback will always be 0 - but added here just for completeness of formulae
            effectiveToPay = (mRetainedFragment.mBillAmount + mDelCharges) - mDebitCashload;

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

    private void calcAddCl() {
        int effectiveToPay = 0;
        if(mAddClStatus==STATUS_AUTO) {
            //mAddCashload = 0;
            effectiveToPay = (mRetainedFragment.mBillAmount + mDelCharges) - mDebitCashload;
            int addCash = (mCashPaid < effectiveToPay)?0:(mCashPaid - effectiveToPay);

            // check for limit
            int currAccBal = mRetainedFragment.mCurrCashback.getCurrAccBalance();
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
        /*if(mMerchantUser.getMerchant().getCl_overdraft_enable()) {
            // if overdraft allowed - min amount to pay will be 0
            mMinCashToPay = 0;
        } else {*/
            // Min cash to pay = 'Bill amt' - 'all enabled debit amts'
            // If 'any one or both combined enabled debit amount' > 'bill amt', then mMinCashToPay = 0
            mMinCashToPay = mRetainedFragment.mBillAmount + mDelCharges;
            if (mDebitClStatus == STATUS_AUTO) {
                mMinCashToPay = mMinCashToPay - Math.min(mClBalance, mMinCashToPay);
            }
            if(mOverdraftStatus == STATUS_AUTO) {
                mMinCashToPay = mMinCashToPay - Math.min(Math.abs(mOverdraft), MyGlobalSettings.getAccOverdraftLimit());
            }

            if(mMinCashToPay<0) {
                mMinCashToPay = 0;
            }
        //}
        LogMy.d(TAG,"Exiting calcMinCashToPay: "+mMinCashToPay);
    }

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
                    mRetainedFragment.mBillAmount = Integer.parseInt(newBillAmt);
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
                        mCashPaidHelper = new CashPaid2(mMinCashToPay, (mOverdraftStatus==STATUS_AUTO),
                                (mRetainedFragment.mBillAmount + mDelCharges), this, getActivity());
                    }

                    mCashPaidHelper.onCustomAmtEnter(newCashAmt,(mOverdraftStatus==STATUS_AUTO));
                    break;

                case REQ_CONFIRM_OVERDRAFT:
                    if (resultCode != Activity.RESULT_OK) {
                        return;
                    }
                    LogMy.d(TAG, "Received first overdraft confirmation.");
                    setTransactionValues();
                    mCallback.onTransactionSubmit(mCashPaid);
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

        // Important to set all Numerical values (to 0 if not applicable)
        trans.setTotal_billed(mRetainedFragment.mBillAmount);
        trans.setDelCharge(mDelCharges);
        trans.setCl_credit(mAddCashload);
        trans.setCl_debit(mDebitCashload);
        trans.setCl_overdraft(mOverdraft);
        trans.setPaymentAmt(mCashPaid);

        trans.setCb_eligible_amt(mCbEligibleAmt);
        trans.setCb_percent(String.valueOf(mCbRate));
        trans.setCb_credit(mAddCbNormal);

        trans.setExtra_cb_percent(String.valueOf(mPpCbRate));
        trans.setExtracb_eligible_amt(mExtraCbEligibleAmt);
        trans.setExtra_cb_credit(mAddCbExtra);

        trans.setCust_private_id(mRetainedFragment.mCurrCustomer.getPrivateId());
        // If this txn is for an online order - the TxnId will be same as orderId
        // Else backend will generate a new one
        trans.setTrans_id(mRetainedFragment.mOrderIdForBilling);

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

                int i = v.getId();
                if (i==R.id.img_trans_bill_amt || i==R.id.label_trans_bill_amt || i==R.id.input_trans_bill_amt) {
                    // open 'bill amount' for editing
                    long currentClickTime= SystemClock.uptimeMillis();
                    long elapsedTime=currentClickTime-newBillDialogOpenTime;
                    newBillDialogOpenTime=currentClickTime;

                    if(elapsedTime >= AppConstants.MIN_CLICK_INTERVAL) {
                        String amount = mInputBillAmt.getText().toString().replace(AppConstants.SYMBOL_RS, "").replace("+", "").replace(" ", "");
                        startNumInputDialog(REQ_NEW_BILL_AMT, "Bill Amount:", amount, 0, 0);
                    }

                } else if (i == R.id.img_account || i == R.id.label_account || i == R.id.input_account) {

                    String errorStr = null;

                    if(mAddCashload > 0) {
                        // already set for add, clear it on touch
                        setAddClStatus(STATUS_CLEARED);
                        calcAndSetAmts(false);

                    } else if(mDebitCashload > 0) {
                        // already set for debit, clear it on touch
                        // if 'overdraft' set - clear that also - as its not possible to not deduct balance from account and do overdraft only
                        setOverdraftStatus(STATUS_CLEARED);
                        setDebitClStatus(STATUS_CLEARED);
                        calcAndSetAmts(false);

                    } else if(mCashPaid == (mRetainedFragment.mBillAmount + mDelCharges)) {
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
                    if(mOverdraft > 0 || mOverdraftStatus==STATUS_AUTO) {
                        // already set for add, clear it on touch
                        setOverdraftStatus(STATUS_CLEARED);
                        calcAndSetAmts(false);
                    } else {
                        if(mOverdraftStatus==STATUS_CLEARED) {
                            if(mReturnCash==0) {
                                AppCommonUtil.toast(getActivity(), "Balance Already Zero");
                            } else {
                                // activating overdraft means activating cl_debit also
                                // this as before calculating overdraft - any amount in account is need to be debit
                                if(mDebitClStatus==STATUS_CLEARED) {
                                    setDebitClStatus(STATUS_AUTO);
                                }
                                setOverdraftStatus(STATUS_AUTO);
                                calcAndSetAmts(false);
                            }
                        } else  {
                            AppCommonUtil.toast(getActivity(), statusDesc.get(mOverdraftStatus));
                            /*String errorStr = null;
                            switch (mOverdraftStatus) {
                                case STATUS_NO_BILL_AMT:
                                    errorStr = "No Bill Amount";
                                    break;
                                default:
                                    errorStr = statusDesc.get(mOverdraftStatus);
                            }
                            if(errorStr!=null) {
                                AppCommonUtil.toast(getActivity(),errorStr);
                            }*/
                        }
                    }
                } else if (i == R.id.img_delivery_charges || i == R.id.label_delivery_charges || i == R.id.input_delivery_charges) {
                    if(mDelChgsStatus==STATUS_AUTO) {
                        setDelChgsStatus(STATUS_CLEARED);
                    } else if(mDelChgsStatus==STATUS_CLEARED) {
                        if(isDelChgsApplicable()) {
                            setDelChgsStatus(STATUS_AUTO);
                        } else {
                            //AppCommonUtil.toast(getActivity(), "Delivery Charge not applicable");
                            String msg = "Delivery Charges not applicable. 'Bill amount' is more than free delivery bill of "
                                    + AppCommonUtil.getAmtStr(MerchantUser.getInstance().getMerchant().getFreeDlvrMinAmt()) + ".";
                            AppCommonUtil.snackbar(mCoordinatorLayout, msg);
                        }
                    }
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
        LogMy.d(TAG, "In initListeners");
        // can change bill amount
        mImgBill.setOnTouchListener(this);
        mLabelBill.setOnTouchListener(this);
        mInputBillAmt.setOnTouchListener(this);

        mImgDelChgs.setOnTouchListener(this);
        mLabelDelChgs.setOnTouchListener(this);
        mInputDelChgs.setOnTouchListener(this);

        //mLayoutAccount.setOnTouchListener(this);
        mImgAccount.setOnTouchListener(this);
        mLabelAccount.setOnTouchListener(this);
        mInputAccount.setOnTouchListener(this);

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

                    if (mReturnCash != 0) {
                        if (mCashPaid >= 0) {
                            AppCommonUtil.toast(getActivity(), "Balance not 0");
                        } else {
                            AppCommonUtil.toast(getActivity(), "Set Payment done");
                        }

                        return;
                    }

                    // If all 0 - no point going ahead
                    // This may happen, if this txn involves only cashback and
                    // that cashback is less than 1 rupee - which will be rounded of to 0
                    if ((mAddCbNormal + mAddCbExtra) <= 0 && mAddCashload <= 0 && mOverdraft <= 0 && mDebitCashload <= 0) {
                        //AppCommonUtil.toast(getActivity(), "No MyeCash data to process !!");
                        String msg;
                        //if (mRetainedFragment.mBillAmount <= 0) {
                            msg = "All Add/Debit amounts are 0. Nothing to process.";
                        //} else {
                        //    msg = "'Award Cashback' is less than 1 Rupee. Other credit/debit amount are 0.";
                        //}
                        DialogFragmentWrapper dialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, msg, true, true);
                        dialog.setTargetFragment(CashTransactionFragment_2.this, REQ_NOTIFY_ERROR);
                        dialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
                        return;
                    } else {
                        // Balance is 0
                        // Ask for confirmation - if first overdraft for this customer
                        if (mOverdraft>0 && mRetainedFragment.mCurrCashback.getCurrAccOverdraft() <= 0) {
                            DialogFragmentWrapper dialog = DialogFragmentWrapper.createConfirmationDialog(
                                    AppConstants.overdraftConfirmTitle, AppConstants.overdraftConfirmMsg, true, false);
                            dialog.setTargetFragment(CashTransactionFragment_2.this, REQ_CONFIRM_OVERDRAFT);
                            dialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_CONFIRMATION);
                        } else {
                            setTransactionValues();
                            mCallback.onTransactionSubmit(mCashPaid);
                        }
                    }

                    /*if (mCashPaid >= 0) {
                        if (mReturnCash != 0) {
                            AppCommonUtil.toast(getActivity(), "Balance not 0");
                        } else {
                            setTransactionValues();
                            mCallback.onTransactionSubmit(mCashPaid);
                        }
                    } else {
                        AppCommonUtil.toast(getActivity(), "Set Cash Paid");
                    }*/
                } else {
                    AppCommonUtil.toast(getActivity(), "No MyeCash data to process !!");
                }
            }
        });

    }

    private void setDelChgsStatus(int status) {
        LogMy.d(TAG, "In setDelChgsStatus: " + status);
        mDelChgsStatus = status;
        if(mAddCbStatus == STATUS_AUTO) {
            mLayoutDelChgs.setVisibility(View.VISIBLE);
            mLayoutDelChgs.setAlpha(1.0f);
            mImgDelChgs.setColorFilter(ContextCompat.getColor(getActivity(), R.color.primary), PorterDuff.Mode.SRC_IN);
            //mLabelDelChgs.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            //mInputDelChgs.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            mDelCharges = MyGlobalSettings.getDeliveryCharges();

        } else if(mAddCbStatus == STATUS_CLEARED) {
            mLayoutDelChgs.setVisibility(View.VISIBLE);
            mLayoutAddCb.setAlpha(0.5f);
            mImgDelChgs.setColorFilter(ContextCompat.getColor(getActivity(), R.color.disabled), PorterDuff.Mode.SRC_IN);
            //mLabelDelChgs.setTextColor(ContextCompat.getColor(getActivity(), R.color.disabled));
            //mInputDelChgs.setTextColor(ContextCompat.getColor(getActivity(), R.color.disabled));
            mDelCharges = 0;

        } else if(mAddCbStatus == STATUS_DISABLED) {
            mLayoutDelChgs.setVisibility(View.GONE);
            mDelCharges = 0;
        }

        AppCommonUtil.showAmtColor(getActivity(), mLabelDelChgs, mInputDelChgs, mDelCharges,false);
    }

    private void setAddClStatus(int status) {
        LogMy.d(TAG, "In setAddClStatus: " + status);
        mAddClStatus = status;
        showAccountStatus();
    }

    private void setDebitClStatus(int status) {
        LogMy.d(TAG, "In setDebitClStatus: "+status);
        mDebitClStatus = status;
        showAccountStatus();
    }

    private void showAccountStatus() {
        if(mAddClStatus!=STATUS_AUTO && mDebitClStatus!=STATUS_AUTO) {
            mLayoutAccount.setAlpha(0.4f);
            mImgAccount.setColorFilter(ContextCompat.getColor(getActivity(), R.color.disabled), PorterDuff.Mode.SRC_IN);

        } else {
            mLayoutAccount.setAlpha(1.0f);
            mImgAccount.setColorFilter(ContextCompat.getColor(getActivity(), R.color.primary), PorterDuff.Mode.SRC_IN);
        }
    }

    private void setOverdraftStatus(int status) {
        LogMy.d(TAG, "In setOverdraftStatus: "+status);
        mOverdraftStatus = status;

        if(mOverdraftStatus != STATUS_AUTO) {
            mLayoutOverdraft.setAlpha(0.4f);
            mLayoutOverdraft.setBackgroundResource(R.drawable.round_rect_border_disabled);
            //mImgOverdraft.setColorFilter(R.color.disabled);
            mImgOverdraft.setColorFilter(ContextCompat.getColor(getActivity(), R.color.disabled), PorterDuff.Mode.SRC_IN);
            mLabelOverdraft.setTextColor(ContextCompat.getColor(getActivity(), R.color.disabled));
            mInputOverdraft.setTextColor(ContextCompat.getColor(getActivity(), R.color.disabled));
        } else {
            mLayoutOverdraft.setAlpha(1.0f);
            mLayoutOverdraft.setBackgroundResource(R.drawable.round_rect_border_red);
            //mImgOverdraft.setColorFilter(R.color.red_negative);
            mImgOverdraft.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red_negative), PorterDuff.Mode.SRC_IN);
            mLabelOverdraft.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            mInputOverdraft.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
        }
    }

    private void setAddCbStatus(int status) {
        LogMy.d(TAG, "In setAddCbStatus: " + status);
        mAddCbStatus = status;
        if (mAddCbStatus == STATUS_AUTO) {
            mLayoutAddCb.setAlpha(1.0f);
        } else {
            mLayoutAddCb.setAlpha(0.5f);
        }
    }

    private boolean isDelChgsApplicable() {
        return (mRetainedFragment.mBillAmount > 0 &&
                mRetainedFragment.mBillAmount < MerchantUser.getInstance().getMerchant().getFreeDlvrMinAmt());
    }

    /*
     * Calculates 'states' based on parameters which can't be changed from this screen
     * i.e, merchant settings, and account/cashback balance
     * So, this fx. should be called only once - when this fragment is created
     */
    private void initAmtUiStates() {
        LogMy.d(TAG, "In initAmtUiStates");

        // Delivery Charges status
        if(mRetainedFragment.mOrderIdForBilling==null) {
            setDelChgsStatus(STATUS_DISABLED);
        } else {
            if(isDelChgsApplicable()) {
                setDelChgsStatus(STATUS_AUTO);
            } else {
                setDelChgsStatus(STATUS_CLEARED);
            }
        }

        // Init 'add cash' status
        if(mMerchantUser.getMerchant().getCl_add_enable()) {

            if(mRetainedFragment.mCurrCashback.getCurrAccBalance() >= MyGlobalSettings.getCashAccLimit()) {
                setAddClStatus(STATUS_ACCOUNT_FULL);
            } else {
                // by default, dont try add cash
                setAddClStatus(STATUS_CLEARED);
            }
        } else {
            setAddClStatus(STATUS_DISABLED);
        }

        // Init 'debit cash' status
        mClBalance = mRetainedFragment.mCurrCashback.getCurrAccBalance();
        if(mClBalance <= 0) {
            setDebitClStatus(STATUS_NO_BALANCE);
            mClBalance = 0;
        } else {
            // by default, dont try to debit
            setDebitClStatus(STATUS_CLEARED);
        }

        // Init overdraft status
        // Not allowed for online customer orders
        if(mRetainedFragment.mOrderIdForBilling!=null) {
            setOverdraftStatus(STATUS_ONLINE_ORDER);
        } else if(mMerchantUser.getMerchant().getCl_overdraft_enable()) {
            int accBalance = mRetainedFragment.mCurrCashback.getCurrAccBalance();
            if(accBalance<0 && Math.abs(accBalance) >= MyGlobalSettings.getAccOverdraftLimit()) {
                setOverdraftStatus(STATUS_ACCOUNT_FULL);
            } else {
                // by default, dont enable overdraft
                setOverdraftStatus(STATUS_CLEARED);
            }
        } else {
            setOverdraftStatus(STATUS_DISABLED);
        }

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
            mCashPaidHelper = new CashPaid2(mMinCashToPay, (mOverdraftStatus==STATUS_AUTO),
                    (mRetainedFragment.mBillAmount + mDelCharges), this, getActivity());
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

            if(mRetainedFragment.mBillAmount <= 0) {
                newStatus = STATUS_NO_BILL_AMT;
            } else {
                newStatus = (mDebitClStatus == STATUS_AUTO) ? STATUS_AUTO : STATUS_CLEARED;
            }
            setDebitClStatus(newStatus);
        }

        // 'overdraft' status
        if(mOverdraftStatus < STATUS_DISABLED) {
            // not permanently disabled
            int newStatus;

            if(mRetainedFragment.mBillAmount <= 0) {
                newStatus = STATUS_NO_BILL_AMT;
            } else {
                newStatus = (mOverdraftStatus == STATUS_AUTO) ? STATUS_AUTO : STATUS_CLEARED;
            }
            setOverdraftStatus(newStatus);
        }
    }

    // UI Resources data members
    private View mCoordinatorLayout;

    private View mLayoutBillAmt;
    private ImageView mImgBill;
    private View mLabelBill;
    private EditText mInputBillAmt;

    private View mLayoutDelChgs;
    private ImageView mImgDelChgs;
    private EditText mLabelDelChgs;
    private EditText mInputDelChgs;

    private View mLayoutAccount;
    private ImageView mImgAccount;
    private View mLabelAccount;
    private EditText mInputAccount;

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
        LogMy.d(TAG, "In bindUiResources");

        mCoordinatorLayout = v.findViewById(R.id.myCoordinatorLayout);

        mLayoutBillAmt = v.findViewById(R.id.layout_bill_amt);
        mImgBill = (ImageView) v.findViewById(R.id.img_trans_bill_amt);
        mLabelBill = v.findViewById(R.id.label_trans_bill_amt);
        mInputBillAmt = (EditText) v.findViewById(R.id.input_trans_bill_amt);

        mLayoutDelChgs = v.findViewById(R.id.layout_delivery_charges);
        mImgDelChgs = (ImageView) v.findViewById(R.id.img_delivery_charges);
        mLabelDelChgs = (EditText) v.findViewById(R.id.label_delivery_charges);
        mInputDelChgs = (EditText) v.findViewById(R.id.input_delivery_charges);

        mLayoutAccount = v.findViewById(R.id.layout_account);
        mImgAccount = (ImageView) v.findViewById(R.id.img_account);
        mLabelAccount = v.findViewById(R.id.label_account);
        mInputAccount = (EditText) v.findViewById(R.id.input_account);

        mLayoutOverdraft = v.findViewById(R.id.ct_layout_overdraft);
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
        LogMy.d(TAG, "In onResume");
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
        LogMy.d(TAG, "In onPause");
        super.onPause();
        AppCommonUtil.cancelToast();
    }

    @Override
    public void onStop() {
        super.onStop();
        LogMy.d(TAG, "In onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogMy.d(TAG, "In onDestroyView");
    }

    @Override
    public void onStart() {
        super.onStart();
        LogMy.d(TAG, "In onStart");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        LogMy.d(TAG, "In onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putInt("mDebitCashload", mDebitCashload);
        outState.putInt("mOverdraft", mOverdraft);

        outState.putInt("mAddCashload", mAddCashload);
        outState.putInt("mAddCbNormal", mAddCbNormal);
        outState.putInt("mAddCbExtra", mAddCbExtra);

        outState.putInt("mCashPaid", mCashPaid);
        outState.putInt("mReturnCash", mReturnCash);

        outState.putInt("mCbEligibleAmt", mCbEligibleAmt);
        outState.putInt("mExtraCbEligibleAmt", mExtraCbEligibleAmt);

        outState.putInt("mClBalance", mClBalance);
        outState.putInt("mMinCashToPay", mMinCashToPay);

        outState.putInt("mAddCbStatus", mAddCbStatus);
        outState.putInt("mAddClStatus", mAddClStatus);
        outState.putInt("mDebitClStatus", mDebitClStatus);
        outState.putInt("mOverdraftStatus", mOverdraftStatus);

    }
}

    /*private void calcAndSetAddCb() {
        // calculate add cashback
        if(STATUS_DISABLED != mAddCbStatus) {

            boolean cbApply = (mRetainedFragment.mBillAmount > 0);
            boolean cbExtraApply = (mAddCashload >= mMerchantUser.getMerchant().getPrepaidCbMinAmt() && mPpCbRate>0);

            // calculate cashbacks
            int cbEligibleAmt = mRetainedFragment.mBillAmount - mRetainedFragment.mCbExcludedTotal - mDebitCashback;
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

