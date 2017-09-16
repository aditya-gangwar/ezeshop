package in.ezeshop.merchantbase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.entities.MyTransaction;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Merchants;
import in.ezeshop.common.database.Transaction;
import in.ezeshop.merchantbase.entities.MerchantUser;
import in.ezeshop.merchantbase.helper.MyRetainedFragment;

/**
 * Created by adgangwa on 04-11-2016.
 */
public class TxnConfirmFragment extends BaseFragment {
    private static final String TAG = "MchntApp-TxnConfirmFragment";
    private static final String ARG_CASH_PAID = "cashPaid";

    private Merchants mMerchant;
    private TxnConfirmFragmentIf mCallback;

    // Container Activity must implement this interface
    public interface TxnConfirmFragmentIf {
        MyRetainedFragment getRetainedFragment();
        void onTransactionConfirm();
        void setDrawerState(boolean isEnabled);
    }

    public static TxnConfirmFragment getInstance(int cashPaid) {
        Bundle args = new Bundle();
        args.putInt(ARG_CASH_PAID, cashPaid);
        TxnConfirmFragment fragment = new TxnConfirmFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_txn_confirm, container, false);

        try {
            bindUiResources(v);

            //mInputInvoiceNum.setOnTouchListener(this);
            mInputInvoiceNum.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction()==MotionEvent.ACTION_UP) {
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    }
                    return false;
                }
            });

            mBtnConfirm.setOnClickListener(this);
            /*mBtnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!mCallback.getRetainedFragment().getResumeOk())
                        return;

                    // check if invoice num is mandatory
                    if (mMerchant.isInvoiceNumAsk() &&
                            !mMerchant.isInvoiceNumOptional() &&
                            mInputInvoiceNum.getText().toString().isEmpty()) {
                        mInputInvoiceNum.setError("Enter Linked Invoice Number");
                    } else {
                        AppCommonUtil.hideKeyboard(getActivity());
                        Transaction curTxn = mCallback.getRetainedFragment().mCurrTransaction.getTransaction();
                        curTxn.setInvoiceNum(mInputInvoiceNum.getText().toString());
                        //curTxn.setComments(mInputComments.getText().toString());
                        mCallback.onTransactionConfirm();
                    }
                }
            });*/
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            getActivity().onBackPressed();
        }

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mCallback = (TxnConfirmFragmentIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement TxnConfirmFragmentIf");
        }

        try {
            mMerchant = MerchantUser.getInstance().getMerchant();
            displayTransactionValues();
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            getActivity().onBackPressed();
        }
    }

    @Override
    public boolean handleTouchUp(View v) {
        /*if(v.getId()==R.id.input_invoice_num) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }*/
        return false;
    }

    @Override
    public void handleBtnClick(View v) {
        if(v.getId()==R.id.btn_txn_confirm) {
            if(!mCallback.getRetainedFragment().getResumeOk())
                return;

            // check if invoice num is mandatory
            if (mMerchant.isInvoiceNumAsk() &&
                    !mMerchant.isInvoiceNumOptional() &&
                    mInputInvoiceNum.getText().toString().isEmpty()) {
                mInputInvoiceNum.setError("Enter Linked Invoice Number");
            } else {
                AppCommonUtil.hideKeyboard(getActivity());
                Transaction curTxn = mCallback.getRetainedFragment().mCurrTransaction.getTransaction();
                curTxn.setInvoiceNum(mInputInvoiceNum.getText().toString());
                //curTxn.setComments(mInputComments.getText().toString());
                mCallback.onTransactionConfirm();
            }
        }
    }

    private void displayTransactionValues() {

        Transaction curTransaction = mCallback.getRetainedFragment().mCurrTransaction.getTransaction();
        int cashPaid = getArguments().getInt(ARG_CASH_PAID);

        //int total = 0;
        //mInputCustomer.setText(curTransaction.getCustomer_id());

        //int toPay = 0;
        //int returnCash = 0;
        //boolean anyDebit = false;

        int value = curTransaction.getTotal_billed();
        mInputBillAmt.setText(AppCommonUtil.getSignedAmtStr(value, true));

        value = curTransaction.getCl_credit();
        if(value > 0) {
            mInputAcc.setText(AppCommonUtil.getSignedAmtStr(value, true));
            mInputAcc.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_positive));
        } else {
            mInputAcc.setText(AppCommonUtil.getSignedAmtStr(value, false));
            mInputAcc.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));

            if(curTransaction.getCl_overdraft() > 0) {
                mLayoutOverdraft.setVisibility(View.VISIBLE);
                mInputOverdraft.setText(AppCommonUtil.getSignedAmtStr(curTransaction.getCl_overdraft(), false));
            } else {
                mLayoutOverdraft.setVisibility(View.GONE);
            }
        }

        mInputPayment.setText(AppCommonUtil.getAmtStr(cashPaid));

        // Bill amount
        /*int value = curTransaction.getTotal_billed();
        if(value <= 0) {
            mLayoutBillAmt.setVisibility(View.GONE);
            // no point of any debit when no bill amount
            mLayoutDebitCl.setVisibility(View.GONE);
            mLayoutDebitCb.setVisibility(View.GONE);
        } else {
            mInputBillAmt.setText(AppCommonUtil.getSignedAmtStr(value, true));
            toPay = toPay + value;

            value = curTransaction.getCl_debit();
            if(value <= 0) {
                mLayoutDebitCl.setVisibility(View.GONE);
            } else {
                mInputDebitCl.setText(AppCommonUtil.getSignedAmtStr(value, false));
                toPay = toPay - value;
                anyDebit = true;
            }

            value = curTransaction.getCb_debit();
            if(value <= 0) {
                mLayoutDebitCb.setVisibility(View.GONE);
            } else {
                mInputDebitCb.setText(AppCommonUtil.getSignedAmtStr(value, false));
                toPay = toPay - value;
                anyDebit = true;
            }
        }

        if(anyDebit) {
            mInputToPay.setText(AppCommonUtil.getSignedAmtStr(toPay, true));
        } else {
            mLayoutToPay.setVisibility(View.GONE);
            mDividerToPay.setVisibility(View.GONE);
        }

        //always show cash paid - as -ve though
        mInputCashPaid.setText(AppCommonUtil.getSignedAmtStr(cashPaid, false));

        value = curTransaction.getCl_credit();
        if(value <= 0) {
            mLayoutAddCl.setVisibility(View.GONE);
            mLayoutBalance.setVisibility(View.GONE);
            mDividerBalance.setVisibility(View.GONE);

            returnCash = cashPaid - toPay;
        } else {
            int balance = cashPaid - toPay;
            if(balance>0) {
                // check for 'bill amount' again - this to avoid un-necessary showing Blanace row for 'put Add CL' txns
                if(curTransaction.getTotal_billed() > 0) {
                    mLayoutBalance.setVisibility(View.VISIBLE);
                    mDividerBalance.setVisibility(View.VISIBLE);
                    mInputBalance.setText(AppCommonUtil.getSignedAmtStr(balance, false));
                } else {
                    mLayoutBalance.setVisibility(View.GONE);
                    mDividerBalance.setVisibility(View.GONE);
                }
                mInputAddCl.setText(AppCommonUtil.getSignedAmtStr(value, true));
                returnCash = balance - value;
            } else {
                LogMy.wtf(TAG, "Doubtful state: "+value+","+cashPaid+","+toPay);
            }
        }

        if(returnCash > 0) {
            // I shudn't be here
            mLabelReturnCash.setText("RETURN");
            mInputReturnCash.setText(AppCommonUtil.getSignedAmtStr(returnCash, false));
            mInputReturnCash.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
            mLayoutReturnCash.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.bg_light_pink));
        } else {
            mLabelReturnCash.setText("BALANCE");
            mInputReturnCash.setText(AppCommonUtil.getSignedAmtStr(Math.abs(returnCash), true));
            mLayoutReturnCash.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.bg_light_grey));
        }*/

        // add cashback
        //value = curTransaction.getCb_credit();
        value = curTransaction.getCb_credit() + curTransaction.getExtra_cb_credit();
        /*if(value == 0) {
            mLayoutAddCb.setVisibility(View.GONE);
            mLayoutCbDetails.setVisibility(View.GONE);
        } else {
            mLayoutAddCb.setVisibility(View.VISIBLE);
            mLayoutCbDetails.setVisibility(View.VISIBLE);*/

            mInputAddCb.setText(AppCommonUtil.getAmtStr(value));
            mInputCbDetails.setText(MyTransaction.getCbDetailStr(curTransaction));
        //}

        if(mMerchant.isInvoiceNumAsk()) {
            mLayoutExtraDetails.setVisibility(View.VISIBLE);
            mLayoutInvoiceNum.setVisibility(View.VISIBLE);
            if(mMerchant.isInvoiceNumOnlyNumbers()) {
                mInputInvoiceNum.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            }
        } else {
            mLayoutExtraDetails.setVisibility(View.GONE);
            mLayoutInvoiceNum.setVisibility(View.GONE);
        }
    }

    //private EditText mInputCustomer;

    private EditText mInputBillAmt;
    private EditText mInputAcc;

    private LinearLayout mLayoutOverdraft;
    private EditText mInputOverdraft;
    private EditText mInputPayment;

    /*private LinearLayout mLayoutBillAmt;
    private EditText mInputBillAmt;

    private LinearLayout mLayoutDebitCl;
    private EditText mInputDebitCl;

    private LinearLayout mLayoutDebitCb;
    private EditText mInputDebitCb;

    private View mDividerToPay;
    private LinearLayout mLayoutToPay;
    private EditText mInputToPay;

    private LinearLayout mLayoutCashpaid;
    private EditText mInputCashPaid;

    private View mDividerBalance;
    private LinearLayout mLayoutBalance;
    private EditText mInputBalance;

    private LinearLayout mLayoutAddCl;
    private EditText mInputAddCl;

    private LinearLayout mLayoutReturnCash;
    private EditText mLabelReturnCash;
    private EditText mInputReturnCash;*/

    //private LinearLayout mLayoutAddCb;
    private EditText mInputAddCb;

    //private View mLayoutCbDetails;
    private EditText mInputCbDetails;

    private View mLayoutExtraDetails;
    private View mLayoutInvoiceNum;
    private EditText mInputInvoiceNum;
    //private EditText mInputComments;

    private AppCompatButton mBtnConfirm;

    private void bindUiResources(View v) {
        //mInputCustomer = (EditText) v.findViewById(R.id.input_customer);

        //mLayoutBillAmt = (LinearLayout) v.findViewById(R.id.layout_bill_amt);
        mInputBillAmt = (EditText) v.findViewById(R.id.input_bill_amt);
        //mSpaceBillAmt = (Space) v.findViewById(R.id.space_bill_amt);

        mInputAcc = (EditText) v.findViewById(R.id.input_account);
        mLayoutOverdraft = (LinearLayout) v.findViewById(R.id.layout_overdraft);
        mInputOverdraft = (EditText) v.findViewById(R.id.input_overdraft);
        mInputPayment = (EditText) v.findViewById(R.id.input_cash_paid);

        /*mLayoutDebitCl = (LinearLayout) v.findViewById(R.id.layout_debit_account);
        mInputDebitCl = (EditText) v.findViewById(R.id.input_debit_account);

        mLayoutDebitCb = (LinearLayout) v.findViewById(R.id.layout_redeem_cb);
        mInputDebitCb = (EditText) v.findViewById(R.id.input_redeem_cb);

        mDividerToPay = v.findViewById(R.id.divider_to_pay);
        mLayoutToPay = (LinearLayout) v.findViewById(R.id.layout_to_pay);
        mInputToPay = (EditText) v.findViewById(R.id.input_to_pay);

        mLayoutCashpaid = (LinearLayout) v.findViewById(R.id.layout_cash_paid);
        mInputCashPaid = (EditText) v.findViewById(R.id.input_cash_paid);

        mDividerBalance = v.findViewById(R.id.divider_balance);
        mLayoutBalance = (LinearLayout) v.findViewById(R.id.layout_balance);
        mInputBalance = (EditText) v.findViewById(R.id.input_balance);

        mLayoutAddCl = (LinearLayout) v.findViewById(R.id.layout_add_account);
        mInputAddCl = (EditText) v.findViewById(R.id.input_add_account);

        mLayoutReturnCash = (LinearLayout) v.findViewById(R.id.layout_return_cash);
        mLabelReturnCash = (EditText) v.findViewById(R.id.label_cash_to_pay);
        mInputReturnCash = (EditText) v.findViewById(R.id.input_return_cash);*/

        //mLayoutAddCb = (LinearLayout) v.findViewById(R.id.layout_add_cb);
        mInputAddCb = (EditText) v.findViewById(R.id.input_add_cb);

        //mLayoutCbDetails = v.findViewById(R.id.layout_cb_details);
        mInputCbDetails = (EditText) v.findViewById(R.id.input_cb_details);

        mLayoutExtraDetails = v.findViewById(R.id.layout_extra_details);
        mLayoutInvoiceNum = v.findViewById(R.id.layout_invoice_num);
        mInputInvoiceNum = (EditText) v.findViewById(R.id.input_invoice_num);
        //mInputComments = (EditText) v.findViewById(R.id.input_comments);

        mBtnConfirm = (AppCompatButton) v.findViewById(R.id.btn_txn_confirm);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCallback.setDrawerState(false);
        mCallback.getRetainedFragment().setResumeOk(true);
    }

}
