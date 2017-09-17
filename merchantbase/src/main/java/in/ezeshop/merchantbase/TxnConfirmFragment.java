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
    //private static final String ARG_CASH_PAID = "cashPaid";

    private Merchants mMerchant;
    private TxnConfirmFragmentIf mCallback;

    // Container Activity must implement this interface
    public interface TxnConfirmFragmentIf {
        MyRetainedFragment getRetainedFragment();
        void onTransactionConfirm();
        void setDrawerState(boolean isEnabled);
    }

    public static TxnConfirmFragment getInstance(int cashPaid) {
        //Bundle args = new Bundle();
        //args.putInt(ARG_CASH_PAID, cashPaid);
        TxnConfirmFragment fragment = new TxnConfirmFragment();
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_txn_confirm, container, false);

        try {
            bindUiResources(v);

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
        //int cashPaid = getArguments().getInt(ARG_CASH_PAID);

        int value = curTransaction.getTotal_billed();
        mInputBillAmt.setText(AppCommonUtil.getSignedAmtStr(value, true));

        // set account add/debit amount
        value = curTransaction.getCl_credit();
        if(value > 0) {
            mInputAcc.setText(AppCommonUtil.getSignedAmtStr(value, true));
            mInputAcc.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_positive));
        } else {
            value = curTransaction.getCl_debit();
            mInputAcc.setText(AppCommonUtil.getSignedAmtStr(value, false));
            if(value>0) {
                mInputAcc.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
            } else {
                mInputAcc.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            }

            if(curTransaction.getCl_overdraft() > 0) {
                mLayoutOverdraft.setVisibility(View.VISIBLE);
                mInputOverdraft.setText(AppCommonUtil.getSignedAmtStr(curTransaction.getCl_overdraft(), false));
            } else {
                mLayoutOverdraft.setVisibility(View.GONE);
            }
        }

        mInputPayment.setText(AppCommonUtil.getAmtStr(curTransaction.getPaymentAmt()));
        value = curTransaction.getCb_credit() + curTransaction.getExtra_cb_credit();
        mInputAddCb.setText(AppCommonUtil.getAmtStr(value));
        mInputCbDetails.setText(MyTransaction.getCbDetailStr(curTransaction));

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

    private EditText mInputBillAmt;
    private EditText mInputAcc;
    private LinearLayout mLayoutOverdraft;
    private EditText mInputOverdraft;

    private EditText mInputPayment;
    private EditText mInputAddCb;
    private EditText mInputCbDetails;

    private View mLayoutExtraDetails;
    private View mLayoutInvoiceNum;
    private EditText mInputInvoiceNum;

    private AppCompatButton mBtnConfirm;

    private void bindUiResources(View v) {
        mInputBillAmt = (EditText) v.findViewById(R.id.input_bill_amt);
        mInputAcc = (EditText) v.findViewById(R.id.input_account);
        mLayoutOverdraft = (LinearLayout) v.findViewById(R.id.layout_overdraft);
        mInputOverdraft = (EditText) v.findViewById(R.id.input_overdraft);

        mInputPayment = (EditText) v.findViewById(R.id.input_cash_paid);
        mInputAddCb = (EditText) v.findViewById(R.id.input_add_cb);
        mInputCbDetails = (EditText) v.findViewById(R.id.input_cb_details);

        mLayoutExtraDetails = v.findViewById(R.id.layout_extra_details);
        mLayoutInvoiceNum = v.findViewById(R.id.layout_invoice_num);
        mInputInvoiceNum = (EditText) v.findViewById(R.id.input_invoice_num);

        mBtnConfirm = (AppCompatButton) v.findViewById(R.id.btn_txn_confirm);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCallback.setDrawerState(false);
        mCallback.getRetainedFragment().setResumeOk(true);
    }

}
