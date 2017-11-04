/*package in.ezeshop.customerbase;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.ezeshop.appbase.BaseDialog;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.AppAlarms;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.appbase.entities.MyCashback;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.common.database.Merchants;
import in.ezeshop.customerbase.entities.CustomerUser;
import in.ezeshop.customerbase.helper.MyRetainedFragment;

public class MchntDetailsDialogCustApp extends BaseDialog {
    private static final String TAG = "CustApp-MerchantDetailsDialog";
    private static final String ARG_GETTXNS_BTN = "getTxnsBtn";
    private static final String ARG_CB_MCHNTID = "mchntId";

    private MerchantDetailsDialogIf mCallback;
    private SimpleDateFormat mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.MY_LOCALE);

    public interface MerchantDetailsDialogIf {
        MyRetainedFragment getRetainedFragment();
        void getMchntTxns(String id, String name);
    }

    private String mMerchantId;

    public static MchntDetailsDialogCustApp newInstance(boolean showGetTxnsBtn) {
        LogMy.d(TAG, "Creating new MerchantDetailsDialog instance: ");
        Bundle args = new Bundle();
        args.putBoolean(ARG_GETTXNS_BTN, showGetTxnsBtn);
        //args.putString(ARG_CB_MCHNTID, merchantId);

        MchntDetailsDialogCustApp fragment = new MchntDetailsDialogCustApp();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mCallback = (MerchantDetailsDialogIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement MerchantDetailsDialogIf");
        }*/

        /*mMerchantId = getArguments().getString(ARG_CB_MCHNTID, null);
        if(savedInstanceState!=null) {
            LogMy.d(TAG,"Restoring");
            mMerchantId = savedInstanceState.getString("mMerchantId");
        }*/

        /*MyCashback accData = null;
        Merchants mchnt = null;

        if(mMerchantId==null || mMerchantId.isEmpty()
                || mCallback.getRetainedFragment().mCashbacks==null
                || mCallback.getRetainedFragment().mCashbacks.get(mMerchantId)==null ) {
            // Only show merchant details - customer account details (cashback object) is not available
            mchnt = mCallback.getRetainedFragment().mSelMerchant;
        } else {
            accData = mCallback.getRetainedFragment().mCashbacks.get(mMerchantId);
            mchnt = accData.getMerchant();
        }*/

        /*mBtnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        initDialogView(mCallback.getRetainedFragment().mSelectCashback);

        if(mCallback.getRetainedFragment().mSelectCashback==null) {
            // I shouldn't be here
            //raise alarm
            Map<String,String> params = new HashMap<>();
            params.put("CustomerId", CustomerUser.getInstance().getCustomer().getPrivate_id());
            params.put("Message","Merchant object is not available");
            AppAlarms.wtf(CustomerUser.getInstance().getCustomer().getPrivate_id(),
                    DbConstants.USER_TYPE_CUSTOMER,"MerchantDetailsDialog:onActivityCreated",params);

            AppCommonUtil.toast(getActivity(), "Error. Please try again later.");
            // dismiss dialog
            getDialog().dismiss();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.frag_mchnt_details_for_cust, null);

        bindUiResources(v);
        boolean showGetTxns = getArguments().getBoolean(ARG_GETTXNS_BTN, true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, this);

        if(showGetTxns) {
            builder.setNeutralButton("Get Orders", this);
        }

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AppCommonUtil.setDialogTextSize(MchntDetailsDialogCustApp.this, (AlertDialog) dialog);
            }
        });

        return dialog;
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
                if(mNonMemberInfo.getVisibility()!=View.VISIBLE) {
                    mCallback.getMchntTxns(mMerchantId, mName.getText().toString());
                    dialog.dismiss();
                } else {
                    AppCommonUtil.toast(getActivity(), "No Orders");
                }
                break;
        }
    }

    @Override
    public boolean handleTouchUp(View v) {
        return false;
    }

    private void initDialogView(MyCashback data) {
        if(data==null) {
            return;
        }

        Merchants merchant = data.getMerchant();
        mMerchantId = data.getMerchantId();

        if(merchant != null) {
            if (merchant.getAdmin_status() == DbConstants.USER_STATUS_UNDER_CLOSURE) {
                mLayoutExpNotice.setVisibility(View.VISIBLE);
                mInputExpNotice.setText(String.format(getString(R.string.mchnt_remove_notice_to_cust),
                        AppCommonUtil.getMchntRemovalDate(merchant.getRemoveReqDate())));
            } else {
                mLayoutExpNotice.setVisibility(View.GONE);
            }*/

            /*if(cb.getDpMerchant()!=null) {
                mImgMerchant.setImageBitmap(cb.getDpMerchant());
            }*/
            /*Bitmap dp = AppCommonUtil.getLocalBitmap(getActivity(),
                    merchant.getDisplayImage(), getResources().getDimension(R.dimen.dp_item_image_width));
            if (dp != null) {
                mImgMerchant.setImageBitmap(dp);
            }

            mName.setText(merchant.getName());
            String textCbRate = "";
            if(Integer.valueOf(merchant.getPrepaidCbRate()) <= 0) {
                textCbRate = merchant.getCb_rate() + "%";
                mPpCbDetails.setVisibility(View.GONE);
            } else {
                textCbRate = merchant.getCb_rate()+"% + "+merchant.getPrepaidCbRate()+"% *";
                mPpCbDetails.setVisibility(View.VISIBLE);
                String ppCbDetails = "* "+merchant.getPrepaidCbRate()+"% when Money added > "+AppCommonUtil.getAmtStr(merchant.getPrepaidCbMinAmt());
                mPpCbDetails.setText(ppCbDetails);
            }
            mCbRate.setText(textCbRate);

            String phone = AppConstants.PHONE_COUNTRY_CODE_DISPLAY+merchant.getContactPhone();
            if(merchant.getContactPhone2()!=null && !merchant.getContactPhone2().isEmpty()) {
                phone = phone + ", " + AppConstants.PHONE_COUNTRY_CODE_DISPLAY+merchant.getContactPhone2();
            }
            mInputContactPhone.setText(phone);

            mAddress.setText(CommonUtils.getMchntAddressStr(merchant));
        } else {
            LogMy.e(TAG, "Merchant object is null !!");
        }

        if(data!=null) {
            // Customer is member to this merchant
            mNonMemberInfo.setVisibility(View.GONE);
            mLytAccDetails.setVisibility(View.VISIBLE);

            Date time = data.getLastTxnTime();
            if(time==null) {
                time = data.getCreateTime();
            }
            mLastTxnTime.setText(mSdfDateWithTime.format(time));

            AppCommonUtil.showAmt(getActivity(), null, mInputTotalBill, data.getBillAmt(),false);

            AppCommonUtil.showAmtColor(getActivity(), null, mInputAccBalance, data.getCurrAccBalance(), false);
            AppCommonUtil.showAmtSigned(getActivity(), null, mInputAccTotalAdd, data.getCurrAccTotalAdd(), false);

            AppCommonUtil.showAmt(getActivity(), null, mInputAccAddCb, data.getCurrAccTotalCb(), true);
            AppCommonUtil.showAmt(getActivity(), null, mInputAccDeposit, data.getClCredit(), true);

            AppCommonUtil.showAmtSigned(getActivity(), null, mInputAccTotalDebit, (data.getCurrAccTotalDebit()*-1), false);

        } else {
            // Customer is not a member to this merchant
            mNonMemberInfo.setVisibility(View.VISIBLE);
            mLytAccDetails.setVisibility(View.GONE);
        }
    }

    private View mLayoutExpNotice;
    private EditText mInputExpNotice;
    private ImageView mImgMerchant;
    private EditText mName;
    private EditText mCbRate;
    private EditText mPpCbDetails;
    private EditText mInputContactPhone;
    private TextView mBtnCall;
    private EditText mAddress;

    private View mNonMemberInfo;
    private View mLytAccDetails;

    private EditText mLastTxnTime;
    private EditText mInputTotalBill;

    private EditText mInputAccBalance;
    private EditText mInputAccTotalAdd;
    private TextView mInputAccAddCb;
    private TextView mInputAccDeposit;
    private EditText mInputAccTotalDebit;

    private void bindUiResources(View v) {

        mImgMerchant = (ImageView) v.findViewById(R.id.img_merchant);

        mName = (EditText) v.findViewById(R.id.input_brand_name);
        mCbRate = (EditText) v.findViewById(R.id.input_cb_rate);
        mPpCbDetails = (EditText) v.findViewById(R.id.input_pp_cb_details);

        mNonMemberInfo = v.findViewById(R.id.layout_nonMemberInfo);
        mLytAccDetails = v.findViewById(R.id.layout_accDetails);

        mLastTxnTime = (EditText) v.findViewById(R.id.input_last_txn_time);;
        mInputTotalBill = (EditText) v.findViewById(R.id.input_total_bill);

        mInputAccBalance = (EditText) v.findViewById(R.id.input_acc_balance);
        mInputAccTotalAdd = (EditText) v.findViewById(R.id.input_acc_add);
        mInputAccAddCb = (TextView) v.findViewById(R.id.input_cb);
        mInputAccDeposit = (TextView) v.findViewById(R.id.input_acc_deposit);
        mInputAccTotalDebit = (EditText) v.findViewById(R.id.input_acc_debit);

        mInputContactPhone = (EditText) v.findViewById(R.id.input_contactNum);
        mBtnCall = (TextView) v.findViewById(R.id.btn_call);
        //mLytContactPhone2 = v.findViewById(R.id.lyt_contactNum2);
        //mInputContactPhone2 = (EditText) v.findViewById(R.id.input_contactNum2);
        mAddress = (EditText) v.findViewById(R.id.input_address);

        mLayoutExpNotice = v.findViewById(R.id.layout_expiry_notice);
        mInputExpNotice = (EditText) v.findViewById(R.id.input_expiry_notice);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putString("mMerchantId", mMerchantId);
    }
}*/
