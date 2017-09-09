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
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.ValidationHelper;

/**
 * Created by adgangwa on 26-04-2016.
 */
public class PinChangeDialog extends BaseDialog {
    public static final String TAG = "CustApp-PinChangeDialog";

    private PinChangeDialogIf mListener;

    public interface PinChangeDialogIf {
        void onPinChangeData(String oldPin, String newPin);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mListener = (PinChangeDialogIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement PinChangeDialogIf");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LogMy.d(TAG, "In onCreateDialog");

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pin_change, null);
        initUiResources(v);

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

                        String currPasswd = inputCurrPasswd.getText().toString();
                        String newPassword = inputNewPasswd.getText().toString();

                        int errorCode = ValidationHelper.validatePin(currPasswd);
                        if(errorCode!= ErrorCodes.NO_ERROR) {
                            inputCurrPasswd.setError(AppCommonUtil.getErrorDesc(errorCode));
                        } else {
                            errorCode = ValidationHelper.validatePin(newPassword);
                            if (errorCode != ErrorCodes.NO_ERROR) {
                                inputNewPasswd.setError(AppCommonUtil.getErrorDesc(errorCode));
                            } else {
                                String newPassword2 = inputNewPasswd2.getText().toString();
                                if (!newPassword.equals(newPassword2)) {
                                    inputNewPasswd2.setError("Does not match with new PIN value.");
                                    errorCode = ErrorCodes.GENERAL_ERROR;
                                }
                            }
                        }

                        if(errorCode==ErrorCodes.NO_ERROR) {
                            mListener.onPinChangeData(currPasswd,newPassword);
                            getDialog().dismiss();
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
        //mListener.onPasswdChangeData(null, null);
    }

    EditText inputCurrPasswd;
    EditText inputNewPasswd;
    EditText inputNewPasswd2;

    private void initUiResources(View view) {
        inputCurrPasswd = (EditText) view.findViewById(in.ezeshop.appbase.R.id.input_current_passwd);
        inputNewPasswd = (EditText) view.findViewById(in.ezeshop.appbase.R.id.input_new_passwd);
        inputNewPasswd2 = (EditText) view.findViewById(in.ezeshop.appbase.R.id.input_new_passwd_2);
    }
}
