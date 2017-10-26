package in.ezeshop.customerbase;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import in.ezeshop.appbase.BaseDialog;
import in.ezeshop.appbase.utilities.AppCommonUtil;

/**
 * Created by adgangwa on 25-10-2017.
 */

public class CustOrderSuccessDialog extends BaseDialog {

    private static final String TAG = "MchntApp-CustOrderSuccessDialog";

    private static final String ARG_MCHNT_NAME = "mchntName";
    private static final String ARG_ORDER_ID = "orderId";

    /*private CustOrderSuccessDialogIf mListener;

    public interface CustOrderSuccessDialogIf {
        void onTxnSuccess();
    }*/

    public static CustOrderSuccessDialog newInstance(String merchantName, String orderId) {
        Bundle args = new Bundle();
        args.putString(ARG_MCHNT_NAME, merchantName);
        args.putString(ARG_ORDER_ID, orderId);

        CustOrderSuccessDialog fragment = new CustOrderSuccessDialog();
        fragment.setArguments(args);
        return fragment;
    }


    /*@Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mListener = (CustOrderSuccessDialogIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement CustOrderSuccessDialogIf");
        }
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_cust_order_success, null);

        bindUiResources(v);

        // display values
        mInputMchnt.setText(getArguments().getString(ARG_MCHNT_NAME, ""));
        String text = "Order ID: " + getArguments().getString(ARG_ORDER_ID, "");
        mInputOrderId.setText(text);

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, this)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AppCommonUtil.setDialogTextSize(CustOrderSuccessDialog.this, (AlertDialog) dialog);
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

    /*private void sendResult() {
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
    }*/

    private TextView mInputMchnt;
    private TextView mInputOrderId;

    private void bindUiResources(View v) {
        mInputMchnt = (TextView) v.findViewById(R.id.input_mchnt_name);
        mInputOrderId = (TextView) v.findViewById(R.id.input_orderId);
    }
}
