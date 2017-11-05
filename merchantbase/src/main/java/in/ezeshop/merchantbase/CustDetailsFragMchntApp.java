package in.ezeshop.merchantbase;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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

import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.GenericListDialog;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.entities.MyCashback;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.DateUtil;
import in.ezeshop.common.MyCustomer;
import in.ezeshop.common.MyGlobalSettings;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Customers;
import in.ezeshop.common.database.Merchants;
import in.ezeshop.merchantbase.helper.MyRetainedFragment;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * Created by adgangwa on 05-11-2017.
 */

public class CustDetailsFragMchntApp extends BaseFragment {
    private static final String TAG = "CustApp-CustDetailsFragMchntApp";

    private static final String ARG_CB_POSITION = "cbPosition";
    private static final String ARG_GETTXNS_BTN = "getTxnsBtn";
    //private static final String DIALOG_CALL_NUMBER = "DialogCallNumber";

    private static final int REQ_NOTIFY_ERROR = 1;
    private static final int REQ_NOTIFY_ERROR_EXIT = 5;

    public interface CustDetailsFragMchntAppIf {
        MyRetainedFragment getRetainedFragment();
        void setToolbarForFrag(int iconResId, String title, String subTitle);
        void getCustTxns(String id);
    }

    private SimpleDateFormat mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.MY_LOCALE);

    private CustDetailsFragMchntAppIf mCallback;

    private String mCustMobile;

    public static CustDetailsFragMchntApp newInstance(int position, boolean showGetTxnsBtn) {
        LogMy.d(TAG, "Creating new CustDetailsFragMchntApp instance: ");
        Bundle args = new Bundle();
        args.putInt(ARG_CB_POSITION, position);
        args.putBoolean(ARG_GETTXNS_BTN, showGetTxnsBtn);

        CustDetailsFragMchntApp fragment = new CustDetailsFragMchntApp();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_customer_details, container, false);
        bindUiResources(v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogMy.d(TAG, "In onActivityCreated");

        try {
            mCallback = (CustDetailsFragMchntAppIf) getActivity();

            //setup all listeners
            initListeners();

            MyCashback cb = mCallback.getRetainedFragment().mCurrCashback;
            int position = getArguments().getInt(ARG_CB_POSITION, -1);
            if(position>=0) {
                cb = mCallback.getRetainedFragment().mLastFetchCashbacks.get(position);
                mCustMobile = cb.getCustomer().getMobileNum();
                //cust = cb.getCustomer();
            }

            updateUI(cb, getArguments().getBoolean(ARG_GETTXNS_BTN,true));

        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement CustDetailsFragMchntAppIf");
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

        MyCustomer cust = data.getCustomer();

        if(cust != null) {
            mInputCustName.setText(cust.getName());
            mInputMobileNum.setText(cust.getMobileNum());

            int status = cust.getStatus();
            mInputStatus.setText(DbConstants.userStatusDesc[status]);
            if(status != DbConstants.USER_STATUS_ACTIVE) {
                mLayoutStatusDetails.setVisibility(View.VISIBLE);
                mInputStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
                mInputStatusDate.setText(cust.getStatusUpdateTime());
                mInputReason.setText(cust.getStatusReason());

                if(status==DbConstants.USER_STATUS_LOCKED) {
                    mInputStatusDetails.setVisibility(View.VISIBLE);
                    DateUtil time = new DateUtil(cust.getStatusUpdateDate());
                    time.addMinutes(MyGlobalSettings.getAccBlockMins(DbConstants.USER_TYPE_CUSTOMER));
                    String detail = "Will be Unlocked at "+mSdfDateWithTime.format(time.getTime());
                    mInputStatusDetails.setText(detail);

                } else if(status==DbConstants.USER_STATUS_LIMITED_CREDIT_ONLY) {
                    mInputStatusDetails.setVisibility(View.VISIBLE);
                    DateUtil time = new DateUtil(cust.getStatusUpdateDate());
                    time.addMinutes(MyGlobalSettings.getCustAccLimitModeMins());
                    String detail = "Will be Active again at "+mSdfDateWithTime.format(time.getTime());
                    mInputStatusDetails.setText(detail);

                } else {
                    mInputStatusDetails.setVisibility(View.GONE);
                }
            } else {
                mLayoutStatusDetails.setVisibility(View.GONE);
            }

        } else {
            LogMy.wtf(TAG, "updateUI: Customer object is null !!");
            return;
        }

        if(data.isAccDataAvailable()) {
            // Customer is member to this merchant
            mNonMemberInfo.setVisibility(View.GONE);
            mLytAccDetails.setVisibility(View.VISIBLE);

            if(data.getLastTxnTime()!=null) {
                mLastUsedHere.setText(mSdfDateWithTime.format(data.getLastTxnTime()));
            } else {
                mLastUsedHere.setText("-");
            }
            if(data.getCreateTime()!=null) {
                mFirstUsedHere.setText(mSdfDateWithTime.format(data.getCreateTime()));
            } else {
                mFirstUsedHere.setText("-");
            }

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
            LogMy.d(TAG, "In handleDialogBtnClick: " + v.getId());

            int id = v.getId();
            if (id == mBtnCall.getId()) {
                dialNumber(mCustMobile);

            } else if (id==mBtnGetTxns.getId()) {
                mCallback.getCustTxns(mCustMobile);
            }
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in handleDialogBtnClick", e);
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
        /*switch (requestCode) {
            case REQUEST_CALL_NUMBER:
                String number = data.getStringExtra(DialogFragmentWrapper.EXTRA_SELECTION);
                dialNumber(number);
                break;
        }*/
    }

    private void initListeners() {
        mBtnCall.setOnClickListener(this);
        mBtnGetTxns.setOnClickListener(this);
    }

    private TextView mInputCustName;
    private TextView mInputMobileNum;
    private TextView mInputStatus;
    private View mLayoutStatusDetails;
    private TextView mInputReason;
    private TextView mInputStatusDate;
    private TextView mInputStatusDetails;

    private View mNonMemberInfo;
    private View mLytAccDetails;

    private TextView mLastUsedHere;
    private TextView mFirstUsedHere;
    private TextView mInputTotalBill;
    private TextView mInputAccBalance;
    private TextView mInputAccTotalAdd;
    private TextView mInputAccAddCb;
    private TextView mInputAccDeposit;
    private TextView mInputAccTotalDebit;

    private AppCompatButton mBtnCall;
    private AppCompatButton mBtnGetTxns;

    private void bindUiResources(View v) {

        mInputCustName = (TextView) v.findViewById(R.id.input_customer_name);
        mInputMobileNum = (TextView) v.findViewById(R.id.input_customer_mobile);
        mInputStatus = (TextView) v.findViewById(R.id.input_status);
        mLayoutStatusDetails = v.findViewById(R.id.layout_status_details);
        mInputReason = (TextView) v.findViewById(R.id.input_reason);
        mInputStatusDate = (TextView) v.findViewById(R.id.input_status_date);
        mInputStatusDetails = (TextView) v.findViewById(R.id.input_activation);

        mNonMemberInfo = v.findViewById(R.id.layout_nonMemberInfo);
        mLytAccDetails = v.findViewById(R.id.layout_accDetails);

        mLastUsedHere = (TextView) v.findViewById(R.id.input_cust_last_activity);;
        mFirstUsedHere = (TextView) v.findViewById(R.id.input_cust_register_on);;
        mInputTotalBill = (TextView) v.findViewById(R.id.input_total_bill);
        mInputAccBalance = (TextView) v.findViewById(R.id.input_acc_balance);
        mInputAccTotalAdd = (TextView) v.findViewById(R.id.input_acc_add);
        mInputAccAddCb = (TextView) v.findViewById(R.id.input_cb);
        mInputAccDeposit = (TextView) v.findViewById(R.id.input_acc_deposit);
        mInputAccTotalDebit = (TextView) v.findViewById(R.id.input_acc_debit);

        mBtnCall = (AppCompatButton) v.findViewById(R.id.btn_call);
        mBtnGetTxns = (AppCompatButton) v.findViewById(R.id.btn_txns);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mCustMobile", mCustMobile);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCallback.setToolbarForFrag(-1,"Customer Details",null);

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

    @Override public void onDestroyView() {
        super.onDestroyView();
        //unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

