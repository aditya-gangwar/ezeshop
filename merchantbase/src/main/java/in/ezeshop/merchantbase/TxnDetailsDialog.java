package in.ezeshop.merchantbase;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import in.ezeshop.appbase.BaseDialog;
import in.ezeshop.appbase.entities.MyTransaction;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.database.Transaction;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.merchantbase.helper.MyRetainedFragment;

/**
 * Created by adgangwa on 15-09-2016.
 */
public class TxnDetailsDialog extends BaseDialog {
    private static final String TAG = "MchntApp-TxnDetailsDialog";
    private static final String ARG_POSITION = "argPosition";

    private TxnDetailsDialogIf mCallback;
    private SimpleDateFormat mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.DATE_LOCALE);

    public interface TxnDetailsDialogIf {
        MyRetainedFragment getRetainedFragment();
        void showCustDetails(String internalId);
        //void showTxnImg(int currTxnPos);
        //void cancelTxn(int txnPos);
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
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_txn_details, null);

        bindUiResources(v);

        Dialog dialog = new AlertDialog.Builder(getActivity(),R.style.MyAnimatedDialog)
                .setView(v)
                .setPositiveButton(android.R.string.ok, this)
                //.setNeutralButton("Cancel Txn", this)
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

    @Override
    public void handleBtnClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                dialog.dismiss();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
            /*case DialogInterface.BUTTON_NEUTRAL:
                //Do nothing here because we override this button later to change the close behaviour.
                //However, we still need this because on older versions of Android unless we
                //pass a handler the button doesn't get instantiated
                break;*/
        }
    }

    @Override
    public boolean handleTouchUp(View v) {
        return false;
    }

    private void initDialogView(final int position) {
        LogMy.d(TAG,"Entering initDialogView");
        final Transaction txn = mCallback.getRetainedFragment().mLastFetchTransactions.get(position);

        if(txn != null) {
            mInputMobileNum.setText(Html.fromHtml("<u>"+CommonUtils.getHalfVisibleMobileNum(txn.getCust_mobile())+"</u>"));
            mInputMobileNum.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        mCallback.showCustDetails(txn.getCust_private_id());
                        getDialog().dismiss();
                        return true;
                    }
                    return false;
                }
            });
            mInputTxnTime.setText(mSdfDateWithTime.format(txn.getCreate_time()));

            mInputTotalBill.setText(AppCommonUtil.getAmtStr(txn.getTotal_billed()));
            // set account add/debit amount
            int value = txn.getCl_credit();
            if(value > 0) {
                mInputAcc.setText(AppCommonUtil.getSignedAmtStr(value, true));
                mInputAcc.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_positive));
                mLayoutOverdraft.setVisibility(View.GONE);
            } else {
                value = txn.getCl_debit();
                mInputAcc.setText(AppCommonUtil.getSignedAmtStr(value, false));
                if(value>0) {
                    mInputAcc.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
                } else {
                    mInputAcc.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
                }

                if(txn.getCl_overdraft() > 0) {
                    mLayoutOverdraft.setVisibility(View.VISIBLE);
                    mInputOverdraft.setText(AppCommonUtil.getSignedAmtStr(txn.getCl_overdraft(), false));
                } else {
                    mLayoutOverdraft.setVisibility(View.GONE);
                }
            }

            mInputPayment.setText(AppCommonUtil.getAmtStr(txn.getPaymentAmt()));
            value = txn.getCb_credit() + txn.getExtra_cb_credit();
            mInputCbAward.setText(AppCommonUtil.getAmtStr(value));
            mInputCbDetails.setText(MyTransaction.getCbDetailStr(txn));

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
        LogMy.d(TAG,"Exiting initDialogView");
    }

    /*@Override
    public void onStart()
    {
        LogMy.d(TAG,"Entering onStart");
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point

        final int position = getArguments().getInt(ARG_POSITION, -1);
        final Transaction txn = mCallback.getRetainedFragment().mLastFetchTransactions.get(position);

        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button neutralButton = d.getButton(Dialog.BUTTON_NEUTRAL);

            Date dbTime = TxnReportsHelper.getTxnInDbStartTime();
            LogMy.d( TAG, "dbTime: "+ String.valueOf(dbTime.getTime()) );

//            if(txn.getCreate_time().getTime() > dbTime.getTime() &&
//                    txn.getCancelTime()==null) {
            if(txn.getCreate_time().getTime() > dbTime.getTime()) {
                neutralButton.setEnabled(true);
                neutralButton.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        mCallback.cancelTxn(position);
                        d.dismiss();
                    }
                });
            } else {
                neutralButton.setEnabled(false);
                neutralButton.setOnClickListener(null);
            }
        }
        LogMy.d(TAG,"Exiting onStart");
    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // reset it
        //mCallback.getRetainedFragment().mLastFetchedImage = null;
    }

    //private View mLayoutCancelled;
    //private TextView mInputCancelTime;

    private TextView mInputMobileNum;
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

    /*private View mLayoutCbBill;
    private TextView mInputCbBill;

    //private EditText mInputCustomerId;
    //private TextView mCardUsed;

    private TextView mInputCbAward;
    private TextView mInputCbAward2;
    private View mLayoutCbDetails;
    private TextView mInputCbDetails;

    private TextView mInputCbRedeem;

    private TextView mInputAccAdd;
    private TextView mInputAccDebit;

    private View mLabelAcc;
    private View mLayoutAccDebit;
    private View mLayoutAccAdd;*/

    //private ImageView mTxnImage;

    private void bindUiResources(View v) {

        //mLayoutCancelled = v.findViewById(R.id.layout_cancelled);
        //mInputCancelTime = (TextView) v.findViewById(R.id.input_cancel_time);

        mInputMobileNum = (TextView) v.findViewById(R.id.input_customer_mobile);
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

        /*mLayoutCbBill = v.findViewById(R.id.layout_cb_bill);
        mInputCbBill = (TextView) v.findViewById(R.id.input_cb_bill);

        mInputCustomerId = (EditText) v.findViewById(R.id.input_customer_id);;
        //mCardUsed = (TextView) v.findViewById(R.id.input_card_used);

        mLabelAcc = v.findViewById(R.id.label_acc);
        mLayoutAccDebit = v.findViewById(R.id.layout_acc_debit);
        mLayoutAccAdd = v.findViewById(R.id.layout_acc_add);

        mInputAccAdd = (TextView) v.findViewById(R.id.input_acc_add);
        mInputAccDebit = (TextView) v.findViewById(R.id.input_acc_debit);

        mInputCbAward = (TextView) v.findViewById(R.id.input_cb_award);
        mInputCbAward2 = (TextView) v.findViewById(R.id.input_cb_award2);
        mLayoutCbDetails = v.findViewById(R.id.layout_cb_details);
        mInputCbDetails = (TextView) v.findViewById(R.id.input_cb_details);

        mInputCbRedeem = (TextView) v.findViewById(R.id.input_cb_redeem);

        //mTxnImage = (ImageView) v.findViewById(R.id.txnImage);*/

    }
}

