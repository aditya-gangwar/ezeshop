package in.ezeshop.appbase;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.ValidationHelper;

/**
 * Created by adgangwa on 26-04-2016.
 */
public class PasswdChangeDialog extends BaseDialog {
    public static final String TAG = "BaseApp-PasswdChangeDialog";

    private PasswdChangeDialogIf mListener;
    private boolean mShowSecInfo;
    private boolean mInformOnCancel;

    public interface PasswdChangeDialogIf {
        void onPasswdChangeData(String oldPasswd, String newPassword);
    }

    public static PasswdChangeDialog newInstance(boolean showSecInfo, boolean informOnCancel) {
        PasswdChangeDialog fragment = new PasswdChangeDialog();
        fragment.mInformOnCancel = informOnCancel;
        fragment.mShowSecInfo = showSecInfo;
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mListener = (PasswdChangeDialogIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement PasswdChangeDialogIf");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LogMy.d(TAG, "In onCreateDialog");

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_password_change, null);
        initUiResources(v);

        if(savedInstanceState!=null) {
            LogMy.d(TAG,"Restoring dialog state");
            mInformOnCancel = savedInstanceState.getBoolean("mInformOnCancel");
            mShowSecInfo = savedInstanceState.getBoolean("mShowSecInfo");
        }

        if(mShowSecInfo) {
            labelInfo.setVisibility(View.VISIBLE);
        } else {
            labelInfo.setVisibility(View.GONE);
        }

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

                final Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener( new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        AppCommonUtil.hideKeyboard(getDialog());

                        // Validate
                        String currPasswd = inputCurrPasswd.getText().toString();
                        String newPassword = inputNewPasswd.getText().toString();

                        int errorCode = ValidationHelper.validatePassword(currPasswd);
                        if(errorCode!= ErrorCodes.NO_ERROR) {
                            inputCurrPasswd.setError(AppCommonUtil.getErrorDesc(errorCode));
                        } else {
                            errorCode = ValidationHelper.validateNewPassword(newPassword);
                            if (errorCode != ErrorCodes.NO_ERROR) {
                                inputNewPasswd.setError(AppCommonUtil.getErrorDesc(errorCode));
                            } else {
                                String newPassword2 = inputNewPasswd2.getText().toString();
                                if (!newPassword.equals(newPassword2)) {
                                    inputNewPasswd2.setError("Does not match with new password above.");
                                    errorCode = ErrorCodes.GENERAL_ERROR;
                                }
                            }
                        }

                        if(errorCode==ErrorCodes.NO_ERROR) {
                            mListener.onPasswdChangeData(currPasswd, newPassword);
                            getDialog().dismiss();
                        }
                    }
                });

                inputNewPasswd2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            b.performClick();
                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        alertDialog.setCanceledOnTouchOutside(false);
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
                if(mInformOnCancel) {
                    mListener.onPasswdChangeData(null, null);
                }
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
        if(mInformOnCancel) {
            mListener.onPasswdChangeData(null, null);
        }
        //mListener.onPasswdChangeData(null, null);
    }

    EditText labelInfo;
    EditText inputCurrPasswd;
    EditText inputNewPasswd;
    EditText inputNewPasswd2;

    private void initUiResources(View v) {
        labelInfo = (EditText) v.findViewById(R.id.label_security_info);
        inputCurrPasswd = (EditText) v.findViewById(R.id.input_current_passwd);
        inputNewPasswd = (EditText) v.findViewById(R.id.input_new_passwd);
        inputNewPasswd2 = (EditText) v.findViewById(R.id.input_new_passwd_2);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mInformOnCancel", mInformOnCancel);
        outState.putBoolean("mShowSecInfo", mShowSecInfo);
    }
}
