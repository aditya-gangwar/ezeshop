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
 * Created by adgangwa on 26-04-2016.
 */
public class PasswdResetDialogCustApp extends BaseDialog {
    public static final String TAG = "CustApp-PasswdResetDialog";

    private PasswdResetDialogIf mListener;

    public interface PasswdResetDialogIf {
        void onPasswdResetData(String secret1);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mListener = (PasswdResetDialogIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement PasswdResetDialogIf");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LogMy.d(TAG, "In onCreateDialog");

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pwd_reset_cust, null);
        initUiResources(v);

        String txt = String.format(getActivity().getString(R.string.reset_passwd_info),
                MyGlobalSettings.getCustPasswdResetMins().toString());
        mInfo.setText(txt);

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
                        String value = mInputSecret.getText().toString();
                        int error = ValidationHelper.validateDob(value);
                        if(error == ErrorCodes.NO_ERROR) {
                            mListener.onPasswdResetData(value);
                            getDialog().dismiss();
                        } else {
                            mInputSecret.setError(AppCommonUtil.getErrorDesc(error));
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
    public void handleBtnClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                //Do nothing here because we override this button in OnShowListener to change the close behaviour.
                //However, we still need this because on older versions of Android unless we
                //pass a handler the button doesn't get instantiated
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                AppCommonUtil.hideKeyboard(getDialog());
                mListener.onPasswdResetData(null);
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
        mListener.onPasswdResetData(null);
    }

    private EditText mInputSecret;
    private EditText mInfo;

    private void initUiResources(View v) {
        mInputSecret = (EditText) v.findViewById(R.id.input_secret_1);
        mInfo = (EditText) v.findViewById(R.id.labelInfo);
    }
}
