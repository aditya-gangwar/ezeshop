package in.ezeshop.customerbase;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import in.ezeshop.appbase.BaseDialog;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.common.MyGlobalSettings;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.ValidationHelper;

/**
 * Created by adgangwa on 27-09-2016.
 */
public class PinResetDialog extends BaseDialog {
    public static final String TAG = "CustApp-PinResetDialog";

    private PinResetDialogIf mListener;

    public interface PinResetDialogIf {
        void onPinResetData(String secret);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mListener = (PinResetDialogIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement PinResetDialogIf");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LogMy.d(TAG, "In onCreateDialog");

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pin_reset, null);
        initUiResources(v);

        mLabelInfo1.setText(String.format(getString(R.string.cust_pin_reset_info), MyGlobalSettings.getCustPasswdResetMins().toString()));

        // return new dialog
        final AlertDialog alertDialog =  new AlertDialog.Builder(getActivity()).setView(v)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
        if(AppConstants.BLOCK_SCREEN_CAPTURE) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        AppCommonUtil.hideKeyboard(getDialog());
                        String value = mSecret.getText().toString();
                        int error = ValidationHelper.validateDob(value);
                        if(error == ErrorCodes.NO_ERROR) {
                            mListener.onPinResetData(value);
                            getDialog().dismiss();
                        } else {
                            mSecret.setError(AppCommonUtil.getErrorDesc(error));
                        }
                    }
                });
            }
        });

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        return alertDialog;
    }

    @Override
    public void handleDialogBtnClick(DialogInterface dialog, int which) {
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

    /*@Override
    public void onClick(DialogInterface dialog, int which) {
        //Do nothing here because we override this button in OnShowListener to change the close behaviour.
        //However, we still need this because on older versions of Android unless we
        //pass a handler the button doesn't get instantiated
    }*/

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    private EditText mSecret;
    private EditText mLabelInfo1;

    private void initUiResources(View v) {
        mSecret = (EditText) v.findViewById(R.id.input_secret_1);
        mLabelInfo1 = (EditText) v.findViewById(R.id.label_info_1);
    }
}

