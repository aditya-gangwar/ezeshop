package in.ezeshop.customerbase;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.entities.MyCashback;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Merchants;
import in.ezeshop.customerbase.helper.MyRetainedFragment;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * Created by adgangwa on 28-10-2017.
 */

public class MchntDetailsFragCustApp extends BaseFragment {
    private static final String TAG = "CustApp-MchntDetailsFragCustApp";

    private static final String ARG_GETTXNS_BTN = "getTxnsBtn";
    private static final String DIALOG_CALL_NUMBER = "DialogCallNumber";

    private static final int REQ_NOTIFY_ERROR = 1;
    private static final int REQ_NOTIFY_ERROR_EXIT = 5;
    private static final int REQUEST_CALL_NUMBER = 10;

    public interface MchntDetailsFragCustAppIf {
        MyRetainedFragment getRetainedFragment();
        void setToolbarForFrag(int iconResId, String title, String subTitle);
        void getMchntTxns(String id, String name);
    }

    private SimpleDateFormat mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.MY_LOCALE);

    private MyRetainedFragment mRetainedFragment;
    private MchntDetailsFragCustAppIf mCallback;

    private String mMerchantId;
    List<String> mMchntNumbers = new ArrayList<>(10);

    public static MchntDetailsFragCustApp newInstance(boolean showGetTxnsBtn) {
        LogMy.d(TAG, "Creating new MchntDetailsFragCustApp instance: ");
        Bundle args = new Bundle();
        args.putBoolean(ARG_GETTXNS_BTN, showGetTxnsBtn);

        MchntDetailsFragCustApp fragment = new MchntDetailsFragCustApp();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_mchnt_details_for_cust, container, false);
        bindUiResources(v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogMy.d(TAG, "In onActivityCreated");

        try {
            mCallback = (MchntDetailsFragCustApp.MchntDetailsFragCustAppIf) getActivity();
            mRetainedFragment = mCallback.getRetainedFragment();

            //setup all listeners
            initListeners();

            updateUI(mCallback.getRetainedFragment().mSelectCashback,
                    getArguments().getBoolean(ARG_GETTXNS_BTN,true));

        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement MchntDetailsFragCustAppIf");
        } catch(Exception e) {
            LogMy.e(TAG, "Exception in onActivityCreated", e);
            // unexpected exception - show error
            DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            notDialog.setTargetFragment(this,REQ_NOTIFY_ERROR);
            notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    private void updateUI(MyCashback data, boolean showTxnBtn) {
        LogMy.d(TAG, "In updateUI");
        if(data==null) {
            return;
        }

        Merchants merchant = data.getMerchant();

        if(merchant != null) {
            mMerchantId = merchant.getAuto_id();

            if (merchant.getAdmin_status() == DbConstants.USER_STATUS_UNDER_CLOSURE) {
                mLayoutExpNotice.setVisibility(View.VISIBLE);
                mInputExpNotice.setText(String.format(getString(R.string.mchnt_remove_notice_to_cust),
                        AppCommonUtil.getMchntRemovalDate(merchant.getRemoveReqDate())));
            } else {
                mLayoutExpNotice.setVisibility(View.GONE);
            }

            /*if(cb.getDpMerchant()!=null) {
                mImgMerchant.setImageBitmap(cb.getDpMerchant());
            }*/
            Bitmap dp = AppCommonUtil.getLocalBitmap(getActivity(),
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

            String phone = AppConstants.PHONE_COUNTRY_CODE + "-" + merchant.getContactPhone();
            mMchntNumbers.add(phone);

            if(merchant.getContactPhone2()!=null && !merchant.getContactPhone2().isEmpty()) {
                phone = phone + ", " + AppConstants.PHONE_COUNTRY_CODE + "-" + merchant.getContactPhone2();
                mMchntNumbers.add(AppConstants.PHONE_COUNTRY_CODE + "-" + merchant.getContactPhone2());
            }
            mInputContactPhone.setText(phone);

            mAddress.setText(CommonUtils.getMchntAddressStr(merchant));
        } else {
            LogMy.wtf(TAG, "updateUI: Merchant object is null !!");
            return;
        }

        if(data.isAccDataAvailable()) {
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
            showTxnBtn = false;
        }

        if(showTxnBtn) {
           mBtnGetTxns.setVisibility(View.VISIBLE);
        } else {
            mBtnGetTxns.setVisibility(View.GONE);
        }
    }

    // Using BaseFragment's onClick method - to avoid double clicks
    @Override
    public void handleBtnClick(View v) {
        if(!mCallback.getRetainedFragment().getResumeOk())
            return;

        try {
            LogMy.d(TAG, "In handleBtnClick: " + v.getId());

            int id = v.getId();
            if (id == mBtnCall.getId()) {

                if(mMchntNumbers.size() > 1) {
                    DialogFragmentWrapper dialog = DialogFragmentWrapper.createSingleChoiceDialog("Select Number to Call",
                            mMchntNumbers.toArray(new String[mMchntNumbers.size()]), 0, true);
                    dialog.setTargetFragment(this,REQUEST_CALL_NUMBER);
                    dialog.show(getFragmentManager(), DIALOG_CALL_NUMBER);
                } else {
                    dialNumber(mMchntNumbers.get(0));
                }

            } else if (id==mBtnGetTxns.getId()) {
                mCallback.getMchntTxns(mMerchantId, mName.getText().toString());
            }
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in handleBtnClick", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    // 'num' is in format +91-<number>
    private void dialNumber(String num) {
        String callNum = num.replace("-","");
        LogMy.d(TAG,"dialNumber: "+num+", "+callNum);
        Intent i = new Intent(Intent.ACTION_DIAL);
        String p = "tel:" + callNum;
        i.setData(Uri.parse(p));
        startActivity(i);
    }

    // Not using BaseFragment's onTouch
    // as we dont want 'double touch' check against these buttons
    @Override
    public boolean handleTouchUp(View v) {
        return false; // do nothing
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogMy.d(TAG, "In onActivityResult :" + requestCode + ", " + resultCode);
        if(resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CALL_NUMBER:
                String number = data.getStringExtra(DialogFragmentWrapper.EXTRA_SELECTION);
                dialNumber(number);
                break;
        }
    }

    private void initListeners() {
        mBtnCall.setOnClickListener(this);
        mBtnGetTxns.setOnClickListener(this);
    }

    // UI Resources data members
    private View mLayoutExpNotice;
    private EditText mInputExpNotice;
    private ImageView mImgMerchant;
    private EditText mName;
    private EditText mCbRate;
    private EditText mPpCbDetails;
    private EditText mInputContactPhone;
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

    private AppCompatButton mBtnCall;
    private AppCompatButton mBtnGetTxns;

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
        mAddress = (EditText) v.findViewById(R.id.input_address);

        mLayoutExpNotice = v.findViewById(R.id.layout_expiry_notice);
        mInputExpNotice = (EditText) v.findViewById(R.id.input_expiry_notice);

        mBtnCall = (AppCompatButton) v.findViewById(R.id.btn_call);
        mBtnGetTxns = (AppCompatButton) v.findViewById(R.id.btn_txns);

    }

    @Override
    public void onResume() {
        super.onResume();
        mCallback.setToolbarForFrag(-1,"Merchant Details",null);

        try {
            // intentionally called from onResume
            // to get addresses automatically updated in case address add/edit
            //updateUI();
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in onResume", e);
            DialogFragmentWrapper dialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            dialog.setTargetFragment(this, REQ_NOTIFY_ERROR_EXIT);
            dialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            //throw e;
        }

        mCallback.getRetainedFragment().setResumeOk(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        AppCommonUtil.cancelToast();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        LogMy.d(TAG, "In onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        //unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        // Clear any configuration that was done!
        EasyImage.clearConfiguration(getActivity());
        super.onDestroy();
    }

}



