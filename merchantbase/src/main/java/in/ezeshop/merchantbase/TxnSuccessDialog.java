package in.ezeshop.merchantbase;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import in.ezeshop.appbase.BaseDialog;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.CommonUtils;

/**
 * Created by adgangwa on 24-04-2016.
 */
public class TxnSuccessDialog extends BaseDialog {

    private static final String TAG = "MchntApp-TxnSuccessDialog";

    private static final String ARG_MOBILE_NUM = "mobile_num";
    private static final String ARG_TXN_ID = "txnId";
    private static final String ARG_CL_BALANCE = "cl_balance";
    //private static final String ARG_CB_BALANCE = "cb_balance";
    private static final String ARG_CL_BALANCE_OLD = "cl_balance_old";
    //private static final String ARG_CB_BALANCE_OLD = "cb_balance_old";

    private TxnSuccessDialogIf mListener;

    public interface TxnSuccessDialogIf {
        void onTxnSuccess();
    }

    public static TxnSuccessDialog newInstance(String custId, String txnId, int clBalance, int clBalanceOld) {
        Bundle args = new Bundle();
        args.putString(ARG_MOBILE_NUM, custId);
        args.putString(ARG_TXN_ID, txnId);
        args.putInt(ARG_CL_BALANCE, clBalance);
        //args.putInt(ARG_CB_BALANCE, cbBalance);
        args.putInt(ARG_CL_BALANCE_OLD, clBalanceOld);
        //args.putInt(ARG_CB_BALANCE_OLD, cbBalanceOld);

        TxnSuccessDialog fragment = new TxnSuccessDialog();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mListener = (TxnSuccessDialogIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement TxnSuccessDialogIf");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String mobileNum = getArguments().getString(ARG_MOBILE_NUM, null);
        String txnId = getArguments().getString(ARG_TXN_ID, null);
        int clbalance = getArguments().getInt(ARG_CL_BALANCE);
        //int cbBalance = getArguments().getInt(ARG_CB_BALANCE);
        int clbalanceOld = getArguments().getInt(ARG_CL_BALANCE_OLD);
        //int cbBalanceOld = getArguments().getInt(ARG_CB_BALANCE_OLD);

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_txn_success, null);

        bindUiResources(v);
        // display values
        String cust = "Customer: "+ CommonUtils.getHalfVisibleMobileNum(mobileNum);
        mInputCustomer.setText(cust);
        if(txnId!=null) {
            String txt = "Txn ID: " + txnId;
            mInputTxnId.setText(txt);
        }
        //mInputCbBalance.setText(AppCommonUtil.getAmtStr(cbBalance));
        //mInputCbBalanceOld.setText(AppCommonUtil.getAmtStr(cbBalanceOld));

        /*if(clbalance==0 && clbalanceOld==0) {
            mLayoutAccNew.setVisibility(View.GONE);
            mLayoutAccOld.setVisibility(View.GONE);
        } else {
            mInputCashBalance.setText(AppCommonUtil.getAmtStr(clbalance));
            mInputCashBalanceOld.setText(AppCommonUtil.getAmtStr(clbalanceOld));
        }*/

        AppCommonUtil.showAmtColor(getActivity(), null, mInputCashBalance, clbalance, false);
        /*if(clbalance < 0) {
            mInputCashBalance.setText(AppCommonUtil.getNegativeAmtStr(clbalance,false));
            mInputCashBalance.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
        } else {
            mInputCashBalance.setText(AppCommonUtil.getNegativeAmtStr(clbalance,true));
            mInputCashBalance.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_positive));
        }*/

        AppCommonUtil.showAmtSigned(getActivity(), null, mInputCashBalanceOld, clbalanceOld, false);
        /*if(clbalanceOld < 0) {
            mInputCashBalanceOld.setText(AppCommonUtil.getNegativeAmtStr(clbalanceOld,false));
        } else {
            mInputCashBalanceOld.setText(AppCommonUtil.getNegativeAmtStr(clbalanceOld,true));
        }*/

        //displayTransactionValues();

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, this)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AppCommonUtil.setDialogTextSize(TxnSuccessDialog.this, (AlertDialog) dialog);
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
            case DialogInterface.BUTTON_NEUTRAL:
                break;
        }
    }

    @Override
    public boolean handleTouchUp(View v) {
        return false;
    }

    private void sendResult() {
        LogMy.d(TAG, "In sendResult");
        if (mListener != null) {
            mListener.onTxnSuccess();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        sendResult();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        sendResult();
    }

    private EditText mInputCustomer;
    private EditText mInputTxnId;
    private EditText mInputCashBalance;
    //private EditText mInputCbBalance;
    private EditText mInputCashBalanceOld;
    //private EditText mInputCbBalanceOld;

    //private View mLayoutAccNew;
    //private View mLayoutAccOld;

    private void bindUiResources(View v) {
        mInputCustomer = (EditText) v.findViewById(R.id.input_cust_id);

        mInputTxnId = (EditText) v.findViewById(R.id.input_txn_id);
        mInputCashBalance = (EditText) v.findViewById(R.id.input_acc_new);
        //mInputCbBalance = (EditText) v.findViewById(R.id.input_cb_balance);

        mInputCashBalanceOld = (EditText) v.findViewById(R.id.input_acc_old);
        //mInputCbBalanceOld = (EditText) v.findViewById(R.id.input_cb_balance_old);

        //mLayoutAccNew = v.findViewById(R.id.layout_acc_new);
        //mLayoutAccOld = v.findViewById(R.id.layout_acc_old);
    }
}