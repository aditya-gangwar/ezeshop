package in.ezeshop.internal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.text.SimpleDateFormat;

import in.ezeshop.internal.entities.AgentUser;
import in.ezeshop.internal.helper.MyRetainedFragment;
import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.common.database.Customers;

/**
 * Created by adgangwa on 29-11-2016.
 */
public class CustomerDetailsFragment extends BaseFragment {
    private static final String TAG = "AgentApp-CustomerDetailsFragment";

    private static final String DIALOG_DISABLE_CUST = "disableCustomer";
    //private static final String DIALOG_DISABLE_CARD = "disableCard";

    private final SimpleDateFormat mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.DATE_LOCALE);
    private SimpleDateFormat mSdfDateOnly = new SimpleDateFormat(CommonConstants.DATE_FORMAT_ONLY_DATE_DISPLAY, CommonConstants.DATE_LOCALE);

    public interface CustomerDetailsFragmentIf {
        MyRetainedFragment getRetainedFragment();
        void launchCustApp();
    }
    private CustomerDetailsFragmentIf mCallback;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mCallback = (CustomerDetailsFragmentIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement CustomerDetailsFragmentIf");
        }
        initDialogView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogMy.d(TAG, "In onCreateView");
        View v = inflater.inflate(R.layout.frag_cust_details_internal, container, false);

        // access to UI elements
        bindUiResources(v);
        //setup buttons
        if(AgentUser.getInstance().getUserType() == DbConstants.USER_TYPE_AGENT) {
            mAccDisable.setVisibility(View.GONE);
            mAccLimited.setVisibility(View.GONE);
            mLaunchApp.setVisibility(View.GONE);
            //mCardDisable.setVisibility(View.GONE);

        } else if(AgentUser.getInstance().getUserType() == DbConstants.USER_TYPE_CC) {
            mAccDisable.setOnClickListener(this);
            mAccLimited.setOnClickListener(this);
            mLaunchApp.setOnClickListener(this);
            //mCardDisable.setOnClickListener(this);
        }

        return v;
    }

    private void initDialogView() {
        Customers customer = mCallback.getRetainedFragment().mCurrCustomer;
        //CustomerCards card = customer.getMembership_card();

        mName.setText(customer.getName());
        mInternalId.setText(customer.getPrivate_id());
        mInputMobileNum.setText(customer.getMobile_num());
        mFirstLogin.setText(customer.getFirst_login_ok().toString());
        if(customer.getFirst_login_ok()) {
            mFirstLogin.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
        }
        mRegisteredOn.setText(mSdfDateOnly.format(customer.getRegDate()));
        //mExpiringOn.setText(mSdfDateOnly.format(AppCommonUtil.getExpiryDate(customer)));

        int status = customer.getAdmin_status();
        mInputStatus.setText(DbConstants.userStatusDesc[status]);
        if(status != DbConstants.USER_STATUS_ACTIVE) {
            mInputStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
        }
        mInputStatusDate.setText(mSdfDateWithTime.format(customer.getStatus_update_time()));
        mInputReason.setText(customer.getStatus_reason());

        /*if(card!=null) {
            mInputQrCard.setText(card.getCardNum());
            mInputCardStatus.setText(DbConstants.cardStatusDescInternal[card.getStatus()]);
            if (card.getStatus() != DbConstants.CUSTOMER_CARD_STATUS_ACTIVE) {
                mInputCardStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
            }
            mCardStatusDate.setText(mSdfDateWithTime.format(card.getStatus_update_time()));
            mCardStatusReason.setText(card.getStatus_reason());
        } else {
            mInputQrCard.setText("");
            mLayoutCardDetails.setVisibility(View.GONE);
        }*/

        if( status!=DbConstants.USER_STATUS_ACTIVE && (mAccDisable.getVisibility()==View.VISIBLE) ) {
            mAccDisable.setEnabled(false);
            mAccDisable.setOnClickListener(null);
            mAccDisable.setAlpha(0.4f);
        }

        if( status!=DbConstants.USER_STATUS_ACTIVE && (mAccLimited.getVisibility()==View.VISIBLE) ) {
            mAccLimited.setEnabled(false);
            mAccLimited.setOnClickListener(null);
            mAccLimited.setAlpha(0.4f);
        }

        /*if( card!=null && card.getStatus() != DbConstants.CUSTOMER_CARD_STATUS_ACTIVE && (mCardDisable.getVisibility()==View.VISIBLE) ) {
            mCardDisable.setEnabled(false);
            mCardDisable.setOnClickListener(null);
            mCardDisable.setAlpha(0.4f);
        }*/
    }

    @Override
    public boolean handleTouchUp(View v) {
        // do nothing
        return false;
    }

    @Override
    public void handleBtnClick(View v) {
        switch (v.getId()) {
            case R.id.btn_acc_status:
                LogMy.d(TAG,"Clicked change acc status button.");
                // show disable dialog
                DisableCustDialog dialog = DisableCustDialog.getInstance(false);
                dialog.show(getFragmentManager(), DIALOG_DISABLE_CUST);
                break;

            case R.id.btn_launch_app:
                LogMy.d(TAG,"Clicked launch merchant app button.");
                mCallback.launchCustApp();
                break;

            case R.id.btn_acc_limited:
                LogMy.d(TAG,"Clicked change acc status button.");
                // show disable dialog
                dialog = DisableCustDialog.getInstance(true);
                dialog.show(getFragmentManager(), DIALOG_DISABLE_CUST);
                break;

            /*case R.id.btn_disable_card:
                LogMy.d(TAG,"Clicked disable card button.");
                // show disable dialog
                DisableCardDialog dialogCard = new DisableCardDialog();
                dialogCard.show(getFragmentManager(), DIALOG_DISABLE_CARD);
                break;*/
        }
    }

    /*@Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_acc_status:
                LogMy.d(TAG,"Clicked change acc status button.");
                // show disable dialog
                DisableCustDialog dialog = DisableCustDialog.getInstance(false);
                dialog.show(getFragmentManager(), DIALOG_DISABLE_CUST);
                break;

            case R.id.btn_launch_app:
                LogMy.d(TAG,"Clicked launch merchant app button.");
                mCallback.launchCustApp();
                break;

            case R.id.btn_acc_limited:
                LogMy.d(TAG,"Clicked change acc status button.");
                // show disable dialog
                dialog = DisableCustDialog.getInstance(true);
                dialog.show(getFragmentManager(), DIALOG_DISABLE_CUST);
                break;

            case R.id.btn_disable_card:
                LogMy.d(TAG,"Clicked disable card button.");
                // show disable dialog
                DisableCardDialog dialogCard = new DisableCardDialog();
                dialogCard.show(getFragmentManager(), DIALOG_DISABLE_CARD);
                break;
        }
    }*/

    private EditText mName;
    private EditText mInternalId;
    private EditText mInputMobileNum;
    private EditText mFirstLogin;
    private EditText mRegisteredOn;
    //private EditText mExpiringOn;

    private EditText mInputStatus;
    private EditText mInputStatusDate;
    private EditText mInputReason;
    //private EditText mInputRemarks;

    /*private EditText mInputQrCard;
    private View mLayoutCardDetails;
    private EditText mInputCardStatus;
    private EditText mCardStatusDate;
    private EditText mCardStatusReason;*/

    private AppCompatButton mAccDisable;
    private AppCompatButton mAccLimited;
    //private AppCompatButton mCardDisable;
    private AppCompatButton mLaunchApp;


    private void bindUiResources(View v) {
        mName = (EditText) v.findViewById(R.id.input_cust_name);
        mInternalId = (EditText) v.findViewById(R.id.input_cust_id);

        mInputMobileNum = (EditText) v.findViewById(R.id.input_customer_mobile);
        mFirstLogin = (EditText) v.findViewById(R.id.input_first_login);
        mRegisteredOn = (EditText) v.findViewById(R.id.input_registered_on);
        //mExpiringOn = (EditText) v.findViewById(R.id.input_expiring_on);

        mInputStatus = (EditText) v.findViewById(R.id.input_status);
        mInputStatusDate = (EditText) v.findViewById(R.id.input_status_date);
        mInputReason = (EditText) v.findViewById(R.id.input_status_reason);
        //mInputRemarks = (EditText) v.findViewById(R.id.input_status_remarks);

        /*mInputQrCard = (EditText) v.findViewById(R.id.input_card_id);
        mLayoutCardDetails = v.findViewById(R.id.layout_card_details);
        mInputCardStatus = (EditText) v.findViewById(R.id.input_card_status);
        mCardStatusDate = (EditText) v.findViewById(R.id.input_card_status_date);
        mCardStatusReason = (EditText) v.findViewById(R.id.input_card_status_reason);*/

        mAccDisable = (AppCompatButton) v.findViewById(R.id.btn_acc_status);
        mAccLimited = (AppCompatButton) v.findViewById(R.id.btn_acc_limited);
        //mCardDisable = (AppCompatButton) v.findViewById(R.id.btn_disable_card);
        mLaunchApp = (AppCompatButton) v.findViewById(R.id.btn_launch_app);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCallback.getRetainedFragment().setResumeOk(true);
    }

}
