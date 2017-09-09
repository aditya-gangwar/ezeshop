package in.ezeshop.merchantbase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.appbase.utilities.ValidationHelper;
import in.ezeshop.merchantbase.helper.MyRetainedFragment;

/**
 * Created by adgangwa on 07-06-2016.
 */
public class MobileChangePreference extends DialogPreference {
    private static final String TAG = "MchntApp-MobileChangePreference";

    public interface MobileChangePreferenceIf {
        void changeMobileNumOk(String verifyParam, String newMobile);
        void changeMobileNumOtp(String otp);
        //void changeMobileNumReset(boolean showMobilePref);
        //MerchantOps getMobileChangeMerchantOp();
        MyRetainedFragment getRetainedFragment();
    }

    private MobileChangePreferenceIf mCallback;

    View layoutDob1;
    EditText labelDob2;
    EditText labelNewMobile;
    EditText labelNewMobile2;
    EditText labelNewOtp;

    EditText inputDob;
    EditText inputNewMobile;
    EditText inputNewMobile2;
    EditText inputNewOtp;

    public MobileChangePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCallback = (CashbackActivity)context;
        setDialogLayoutResource(R.layout.dialog_mobile_change);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    /*
    public void show() {
        showDialog(null);
    }*/

    @Override
    protected void onBindDialogView (View view) {
        layoutDob1 = view.findViewById(R.id.layout_dob);
        labelDob2 = (EditText) view.findViewById(R.id.label_dob2);
        labelNewMobile = (EditText) view.findViewById(R.id.label_new_mobile);
        labelNewMobile2 = (EditText) view.findViewById(R.id.label_new_mobile2);
        labelNewOtp = (EditText) view.findViewById(R.id.label_otp);

        inputDob = (EditText) view.findViewById(R.id.input_dob);
        inputNewMobile = (EditText) view.findViewById(R.id.input_new_mobile);
        inputNewMobile2 = (EditText) view.findViewById(R.id.input_new_mobile2);
        inputNewOtp = (EditText) view.findViewById(R.id.input_otp);

        super.onBindDialogView(view);
    }

    @Override
    protected void showDialog(Bundle bundle) {
        super.showDialog(bundle);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        /*
        ((AlertDialog) getDialog()).setButton(DialogInterface.BUTTON_NEUTRAL, "Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCallback.changeMobileNumReset();
                dialog.dismiss();
            }
        });*/

        setParams();

        Button pos = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        pos.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (validate()) {
                    // OTP is only visible in second run
                    String otp = null;
                    if (inputNewOtp.getVisibility() == View.VISIBLE) {
                        otp = inputNewOtp.getText().toString();
                        mCallback.changeMobileNumOtp(otp);

                    } else {
                        if(!inputNewMobile2.getText().toString().equals(inputNewMobile.getText().toString())) {
                            inputNewMobile2.setError("Value do not match with above");
                            return;
                        }
                        // check old and new numbers are not same
                        if(mCallback.getRetainedFragment().mMerchantUser.getMerchant().getMobile_num().equals(inputNewMobile.getText().toString())) {
                            inputNewMobile.setError("Same as current registered number.");
                            return;
                        }

                        mCallback.changeMobileNumOk(
                                inputDob.getText().toString(),
                                inputNewMobile.getText().toString());
                    }
                    getDialog().dismiss();
                }
            }
        });
    }

    private void setParams() {
        //MerchantOps op = mCallback.getMobileChangeMerchantOp();
        String verifyParam = mCallback.getRetainedFragment().mVerifyParamMobileChange;
        String newMobile = mCallback.getRetainedFragment().mNewMobileNum;

        //if(op == null || !op.getOp_status().equals(DbConstants.MERCHANT_OP_STATUS_OTP_GENERATED)) {
        if(verifyParam==null || newMobile==null) {
            // first run, otp not generated yet
            // disable OTP and ask for parameters
            labelNewOtp.setText("OTP will be sent on the New mobile number for verification");
            inputNewOtp.setVisibility(View.GONE);
        } else {
            // second run, OTP generated
            // disable and show parameter values and ask for otp
            layoutDob1.setAlpha(0.4f);
            labelDob2.setEnabled(false);
            inputDob.setEnabled(false);

            labelNewMobile.setEnabled(false);
            inputNewMobile.setText(newMobile);
            inputNewMobile.setEnabled(false);

            labelNewMobile2.setEnabled(false);
            inputNewMobile2.setText(newMobile);
            inputNewMobile2.setEnabled(false);
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
        super.onPrepareDialogBuilder(builder);
        builder.setNeutralButton("Restart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //mCallback.changeMobileNumReset(true);
                //dialog.dismiss();

                MyRetainedFragment retainedFrag = mCallback.getRetainedFragment();
                retainedFrag.mNewMobileNum = null;
                retainedFrag.mVerifyParamMobileChange = null;
                retainedFrag.mOtpMobileChange = null;

                setParams();
            }
        });
        builder.setTitle(null);
    }

    /*@Override
    public void onClick(View v) {
        if (validate()) {
            // OTP is only visible in second run
            String otp = null;
            if (inputNewOtp.getVisibility() == View.VISIBLE) {
                otp = inputNewOtp.getText().toString();
                mCallback.changeMobileNumOtp(otp);

            } else {
                if(!inputNewMobile2.getText().toString().equals(inputNewMobile.getText().toString())) {
                    inputNewMobile2.setError("Value do not match with above");
                    return;
                }
                // check old and new numbers are not same
                if(mCallback.getRetainedFragment().mMerchantUser.getMerchant().getMobile_num().equals(inputNewMobile.getText().toString())) {
                    inputNewMobile.setError("Same as current registered number.");
                    return;
                }

                mCallback.changeMobileNumOk(
                        inputDob.getText().toString(),
                        inputNewMobile.getText().toString());
            }
            getDialog().dismiss();
        }
    }*/

    private boolean validate() {
        boolean retValue = true;
        int errorCode;

        if(inputDob.isEnabled()) {
            errorCode = ValidationHelper.validateDob(inputDob.getText().toString());
            if(errorCode != ErrorCodes.NO_ERROR) {
                inputDob.setError(AppCommonUtil.getErrorDesc(errorCode));
                retValue = false;
            }
        }

        if( inputNewMobile.isEnabled()) {
            errorCode = ValidationHelper.validateMobileNo(inputNewMobile.getText().toString());
            if(errorCode != ErrorCodes.NO_ERROR) {
                inputNewMobile.setError(AppCommonUtil.getErrorDesc(errorCode));
                retValue = false;
            }
        }

        if(inputNewOtp.getVisibility() == View.VISIBLE) {
            errorCode = ValidationHelper.validateOtp(inputNewOtp.getText().toString());
            if(errorCode != ErrorCodes.NO_ERROR) {
                inputNewOtp.setError(AppCommonUtil.getErrorDesc(errorCode));
                retValue = false;
            }
        }

        return retValue;
    }

}
