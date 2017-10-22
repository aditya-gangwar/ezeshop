package in.ezeshop.customerbase;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import in.ezeshop.appbase.BaseDialog;
import in.ezeshop.appbase.entities.MyTransaction;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.database.Transaction;
import in.ezeshop.customerbase.helper.MyRetainedFragment;

/**
 * Created by adgangwa on 15-09-2016.
 */
public class TxnDetailsDialog extends BaseDialog {
    private static final String TAG = "CustApp-TxnDetailsDialog";
    private static final String ARG_POSITION = "argPosition";

    private TxnDetailsDialogIf mCallback;
    private SimpleDateFormat mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.MY_LOCALE);

    public interface TxnDetailsDialogIf {
        MyRetainedFragment getRetainedFragment();
        void showMchntDetails(String mchntId);
        //void showTxnImg(int currTxnPos);
    }

    public static TxnDetailsDialog newInstance(int position) {
        LogMy.d(TAG, "Creating new TxnDetailsDialog instance: "+position);
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);

        TxnDetailsDialog fragment = new TxnDetailsDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mCallback = (TxnDetailsDialogIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement TxnDetailsDialogIf");
        }

        int position = getArguments().getInt(ARG_POSITION, -1);
        initDialogView(position);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_txn_details_custapp_2, null);

        bindUiResources(v);

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, this)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AppCommonUtil.setDialogTextSize(TxnDetailsDialog.this, (AlertDialog) dialog);
            }
        });

        return dialog;
    }

    private void initDialogView(final int position) {
        final Transaction txn = mCallback.getRetainedFragment().mLastFetchTransactions.get(position);

        if(txn != null) {
            mInputMerchant.setText(Html.fromHtml("<u>"+ txn.getMerchant_name()+"</u>"));
            mInputMerchant.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        mCallback.showMchntDetails(txn.getMerchant_id());
                        getDialog().dismiss();
                        return true;
                    }
                    return false;
                }
            });
            mInputTxnTime.setText(mSdfDateWithTime.format(txn.getCreate_time()));

            //mInputTotalBill.setText(AppCommonUtil.getAmtStr(txn.getTotal_billed()));
            AppCommonUtil.showAmtColor(getActivity(), null, mInputTotalBill, txn.getTotal_billed(),false);

            // set account add/debit amount
            int value = txn.getCl_credit() - txn.getCl_debit() - txn.getCl_overdraft();
            AppCommonUtil.showAmtColor(getActivity(), null, mInputAcc, value, false);

            // show/hide overdraft layout
            if(txn.getCl_overdraft() > 0) {
                mLayoutOverdraft.setVisibility(View.VISIBLE);
                mInputOverdraft.setText(AppCommonUtil.getNegativeAmtStr(txn.getCl_overdraft()));
            } else {
                mLayoutOverdraft.setVisibility(View.GONE);
            }

            // set account add/debit amount
            /*int value = txn.getCl_credit();
            if(value > 0) {
                mInputAcc.setText(AppCommonUtil.getNegativeAmtStr(value, true));
                mInputAcc.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_positive));
                mLayoutOverdraft.setVisibility(View.GONE);
            } else {
                value = txn.getCl_debit();
                mInputAcc.setText(AppCommonUtil.getNegativeAmtStr(value, false));
                if(value>0) {
                    mInputAcc.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
                } else {
                    mInputAcc.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
                }

                if(txn.getCl_overdraft() > 0) {
                    mLayoutOverdraft.setVisibility(View.VISIBLE);
                    mInputOverdraft.setText(AppCommonUtil.getNegativeAmtStr(txn.getCl_overdraft(), false));
                } else {
                    mLayoutOverdraft.setVisibility(View.GONE);
                }
            }*/

            //mInputPayment.setText(AppCommonUtil.getAmtStr(txn.getPaymentAmt()));
            AppCommonUtil.showAmtColor(getActivity(), null, mInputPayment, txn.getPaymentAmt(),false);

            value = txn.getCb_credit() + txn.getExtra_cb_credit();
            //mInputCbAward.setText(AppCommonUtil.getAmtStr(value));
            AppCommonUtil.showAmtColor(getActivity(), null, mInputCbAward, value,false);

            mInputCbDetails.setText(MyTransaction.getCbDetailStr(txn,false));

            mInputTxnId.setText(txn.getTrans_id());
            mPinUsed.setText(txn.getCpin());

            if(txn.getInvoiceNum()==null || txn.getInvoiceNum().isEmpty()) {
                mLayoutInvNum.setVisibility(View.GONE);
            } else {
                mLayoutInvNum.setVisibility(View.VISIBLE);
                mInvoiceNum.setText(txn.getInvoiceNum());
            }

        } else {
            LogMy.wtf(TAG, "Txn object is null !!");
            getDialog().dismiss();
        }
    }

    @Override
    public void handleBtnClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                //Do nothing here because we override this button in OnShowListener to change the close behaviour.
                //However, we still need this because on older versions of Android unless we
                //pass a handler the button doesn't get instantiated
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                break;
        }
    }

    @Override
    public boolean handleTouchUp(View v) {
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // reset it
        //mCallback.getRetainedFragment().mLastFetchedImage = null;
    }

    //private View mLayoutCancelled;
    //private EditText mInputCancelTime;

    private EditText mInputMerchant;
    private TextView mInputTxnTime;

    private TextView mInputTotalBill;
    private TextView mInputAcc;
    private View mLayoutOverdraft;
    private TextView mInputOverdraft;

    private TextView mInputPayment;
    private TextView mInputCbAward;
    private TextView mInputCbDetails;

    private TextView mInputTxnId;
    private TextView mPinUsed;
    private View mLayoutInvNum;
    private TextView mInvoiceNum;


    /*private EditText mInputTxnId;
    private EditText mInputTxnTime;

    private View mLayoutInvNum;
    private EditText mInvoiceNum;

    //private EditText mCardUsed;
    private EditText mPinUsed;

    private EditText mInputTotalBill;
    private View mLayoutCbBill;
    private EditText mInputCbBill;

    private EditText mInputCbAward;
    private EditText mInputCbAward2;
    private View mLayoutCbDetails;
    private EditText mInputCbDetails;

    private EditText mInputCbRedeem;

    private EditText mInputAccAdd;
    private EditText mInputAccDebit;

    private View mLabelAcc;
    private View mLayoutAccDebit;
    private View mLayoutAccAdd;

    private EditText mInputMerchant;
    private EditText mInputMchntId;*/


    private void bindUiResources(View v) {

        mInputMerchant = (EditText) v.findViewById(R.id.input_merchant_name);
        mInputTxnTime = (TextView) v.findViewById(R.id.input_txn_time);

        mInputTotalBill = (TextView) v.findViewById(R.id.input_total_bill);
        mInputAcc = (TextView) v.findViewById(R.id.input_account);
        mLayoutOverdraft = v.findViewById(R.id.layout_overdraft);
        mInputOverdraft = (TextView) v.findViewById(R.id.input_overdraft);

        mInputPayment = (TextView) v.findViewById(R.id.input_payment);
        mInputCbAward = (TextView) v.findViewById(R.id.input_add_cb);
        mInputCbDetails = (TextView) v.findViewById(R.id.input_cb_details);

        mInputTxnId = (TextView) v.findViewById(R.id.input_txn_id);
        mPinUsed = (TextView) v.findViewById(R.id.input_pin_used);
        mLayoutInvNum = v.findViewById(R.id.layout_invoice_num);
        mInvoiceNum = (TextView) v.findViewById(R.id.input_invoice_num);

        //mLayoutCancelled = v.findViewById(R.id.layout_cancelled);
        //mInputCancelTime = (EditText) v.findViewById(R.id.input_cancel_time);

        /*mInputTxnId = (EditText) v.findViewById(R.id.input_txn_id);
        mInputTxnTime = (EditText) v.findViewById(R.id.input_txn_time);

        mLayoutInvNum = v.findViewById(R.id.layout_invoice_num);
        mInvoiceNum = (EditText) v.findViewById(R.id.input_invoice_num);

        //mCardUsed = (EditText) v.findViewById(R.id.input_card_used);
        mPinUsed = (EditText) v.findViewById(R.id.input_pin_used);

        mInputTotalBill = (EditText) v.findViewById(R.id.input_total_bill);
        mLayoutCbBill = v.findViewById(R.id.layout_cb_bill);
        mInputCbBill = (EditText) v.findViewById(R.id.input_cb_bill);

        mInputAccAdd = (EditText) v.findViewById(R.id.input_acc_add);
        mInputAccDebit = (EditText) v.findViewById(R.id.input_acc_debit);

        mLabelAcc = v.findViewById(R.id.label_acc);
        mLayoutAccDebit = v.findViewById(R.id.layout_acc_debit);
        mLayoutAccAdd = v.findViewById(R.id.layout_acc_add);

        mInputCbAward = (EditText) v.findViewById(R.id.input_cb_award);
        mInputCbAward2 = (EditText) v.findViewById(R.id.input_cb_award2);
        mLayoutCbDetails = v.findViewById(R.id.layout_cb_details);
        mInputCbDetails = (EditText) v.findViewById(R.id.input_cb_details);

        mInputCbRedeem = (EditText) v.findViewById(R.id.input_cb_redeem);

        mInputMerchant = (EditText) v.findViewById(R.id.input_merchant_name);
        mInputMchntId = (EditText) v.findViewById(R.id.input_merchant_id);*/

    }
}

