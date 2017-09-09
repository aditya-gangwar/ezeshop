package in.ezeshop.merchantbase;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import in.ezeshop.appbase.BaseDialog;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.common.MyGlobalSettings;
import in.ezeshop.common.database.Merchants;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.merchantbase.entities.MerchantUser;

import java.text.SimpleDateFormat;

/**
 * Created by adgangwa on 30-07-2016.
 */
public class MerchantDetailsDialog extends BaseDialog {
    private static final String TAG = "MchntApp-MerchantDetailsDialog";

    private final SimpleDateFormat mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.DATE_LOCALE);
    private final SimpleDateFormat mSdfOnlyDate = new SimpleDateFormat(CommonConstants.DATE_FORMAT_ONLY_DATE_DISPLAY, CommonConstants.DATE_LOCALE);

    /*
    private MerchantDetailsDialogIf mCallback;
    public interface MerchantDetailsDialogIf {
        MyRetainedFragment getRetainedFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mCallback = (MerchantDetailsDialogIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement MerchantDetailsDialogIf");
        }
        initDialogView();
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_merchant_details, null);

        bindUiResources(v);
        initDialogView();

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, this)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AppCommonUtil.setDialogTextSize(MerchantDetailsDialog.this, (AlertDialog) dialog);
            }
        });

        return dialog;
    }

    private void initDialogView() {
        MerchantUser merchantUser = MerchantUser.getInstance();
        Merchants merchant = MerchantUser.getInstance().getMerchant();

        if(merchantUser.getDisplayImage()!=null) {
            mDisplayImage.setImageBitmap(merchantUser.getDisplayImage());
        }

        mStoreName.setText(merchant.getName());
        mStoreCategory.setText(merchant.getBuss_category());
        mMerchantId.setText(merchant.getAuto_id());
        mInputContactName.setText(merchant.getContactName());
        mInputMobileNum.setText(merchant.getMobile_num());
        mRegisteredOn.setText(mSdfOnlyDate.format(merchant.getCreated()));
        mExpiringOn.setText(mSdfOnlyDate.format(AppCommonUtil.getExpiryDate(merchant)));

        int status = merchant.getAdmin_status();
        mInputStatus.setText(DbConstants.userStatusDesc[status]);
        if(status != DbConstants.USER_STATUS_ACTIVE) {
            mLayoutStatusDetails.setVisibility(View.VISIBLE);
            mInputStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
            mInputStatusDate.setText(mSdfDateWithTime.format(merchant.getStatus_update_time()));
            mInputReason.setText(merchant.getStatus_reason());

            if(status==DbConstants.USER_STATUS_UNDER_CLOSURE ) {
                mLayoutActivation.setVisibility(View.VISIBLE);
                String msg = "Removal on "+AppCommonUtil.getMchntRemovalDate(merchant.getRemoveReqDate());
                mInputActivation.setText(msg);

            } else if(status==DbConstants.USER_STATUS_LOCKED) {
                mLayoutActivation.setVisibility(View.VISIBLE);
                String detail = "Will be auto unlocked after "+MyGlobalSettings.getAccBlockMins(DbConstants.USER_TYPE_MERCHANT)+" minutes from given time.";
                mInputActivation.setText(detail);

            } else {
                mLayoutActivation.setVisibility(View.GONE);
            }
        } else {
            mLayoutStatusDetails.setVisibility(View.GONE);
        }

        mInputEmail.setText(merchant.getEmail());
        String phone = AppConstants.PHONE_COUNTRY_CODE+merchant.getContactPhone();
        mInputContactNum.setText(phone);

        mAddress.setText(merchant.getAddress().getLine_1());
        mCity.setText(merchant.getAddress().getCity());
        mState.setText(merchant.getAddress().getState());
        mPincode.setText(merchant.getAddress().getPincode());
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

    private ImageView mDisplayImage;
    private EditText mInputMobileNum;
    private EditText mInputContactName;
    private EditText mStoreName;
    private EditText mStoreCategory;

    private EditText mMerchantId;
    private EditText mRegisteredOn;
    private EditText mExpiringOn;

    private EditText mInputStatus;
    private View mLayoutStatusDetails;
    private EditText mInputReason;
    private EditText mInputStatusDate;
    private View mLayoutActivation;
    private EditText mInputActivation;

    private EditText mInputContactNum;
    private EditText mInputEmail;

    private EditText mAddress;
    private EditText mCity;
    private EditText mState;
    private EditText mPincode;


    private void bindUiResources(View v) {

        mDisplayImage = (ImageView) v.findViewById(R.id.display_image);
        mStoreName = (EditText) v.findViewById(R.id.input_store_name);
        mStoreCategory = (EditText) v.findViewById(R.id.input_store_category);

        mMerchantId = (EditText) v.findViewById(R.id.input_merchant_id);
        mInputContactName = (EditText) v.findViewById(R.id.input_contact_name);
        mInputMobileNum = (EditText) v.findViewById(R.id.input_merchant_mobile);
        mRegisteredOn = (EditText) v.findViewById(R.id.input_registered_on);
        mExpiringOn = (EditText) v.findViewById(R.id.input_expiring_on);

        mInputStatus = (EditText) v.findViewById(R.id.input_status);
        mLayoutStatusDetails = v.findViewById(R.id.layout_status_details);
        mInputReason = (EditText) v.findViewById(R.id.input_reason);
        mInputStatusDate = (EditText) v.findViewById(R.id.input_status_date);
        mLayoutActivation = v.findViewById(R.id.layout_activation);
        mInputActivation = (EditText) v.findViewById(R.id.input_activation);

        mInputContactNum = (EditText) v.findViewById(R.id.input_contact_phone);
        mInputEmail = (EditText) v.findViewById(R.id.input_merchant_email);

        mAddress = (EditText) v.findViewById(R.id.input_address);
        mCity = (EditText) v.findViewById(R.id.input_city);
        mState = (EditText) v.findViewById(R.id.input_state);
        mPincode = (EditText) v.findViewById(R.id.input_pincode);
    }
}

