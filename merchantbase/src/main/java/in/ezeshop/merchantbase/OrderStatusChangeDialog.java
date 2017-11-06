package in.ezeshop.merchantbase;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import in.ezeshop.appbase.BaseDialog;
import in.ezeshop.appbase.GenericListSearchFrag;
import in.ezeshop.appbase.entities.MyAreas;
import in.ezeshop.appbase.entities.MyCashback;
import in.ezeshop.appbase.entities.MyCities;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.DateUtil;
import in.ezeshop.common.MyCustomer;
import in.ezeshop.common.MyGlobalSettings;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.merchantbase.helper.MyRetainedFragment;

/**
 * Created by adgangwa on 05-11-2017.
 */

public class OrderStatusChangeDialog extends BaseDialog {
    private static final String TAG = "MchntApp-OrderStatusChangeDialog";
    private static final String ARG_ORDER_ID = "argOderId";
    private static final String ARG_ORDER_STATUS = "argOderStatus";
    private static final String ARG_CALL_BY_FRAG = "argCallByFrag";

    private static final String DIALOG_CANCEL_REASON = "DialogCancelReason";
    private static final int REQUEST_CANCEL_REASON = 12;

    private OrderStatusChangeDialogIf mCallback;

    public static final int REQ_ORDER_STATUS_CHG = 11;
    public static final String DIALOG_ORDER_STATUS_CHG = "DialogOrderStatusChg";

    public interface OrderStatusChangeDialogIf {
        //MyRetainedFragment getRetainedFragment();
        void cancelOrder(String orderId, String reason);
        void acceptOrder(String orderId);
    }

    public static OrderStatusChangeDialog newInstance(String orderId, String orderStatus, boolean callByFrag) {
        LogMy.d(TAG, "Creating new OrderStatusChangeDialog instance: "+orderId);
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        args.putString(ARG_ORDER_STATUS, orderStatus);
        args.putBoolean(ARG_CALL_BY_FRAG, callByFrag);

        OrderStatusChangeDialog fragment = new OrderStatusChangeDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            if(getArguments().getBoolean(ARG_CALL_BY_FRAG)) {
                mCallback = (OrderStatusChangeDialogIf) getTargetFragment();
            } else {
                mCallback = (OrderStatusChangeDialogIf) getActivity();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OrderStatusChangeDialogIf");
        }

        initListeners();
        if(savedInstanceState==null) {
            initDialogView();
        }
    }

    private void initDialogView() {
        mInputOrderId.setText(getArguments().getString(ARG_ORDER_ID));
        mInputOrderStatus.setText(getArguments().getString(ARG_ORDER_STATUS));

        mLytCancelDetails.setVisibility(View.GONE);
        mInfoProcessOrder.setVisibility(View.GONE);
    }

    private void initListeners() {
        mRadioBtnCancel.setOnClickListener(this);
        mRadioBtnNext.setOnClickListener(this);
        mBtnCancelReason.setOnClickListener(this);
        mBtnProcess.setOnClickListener(this);
    }

    @Override
    public void handleBtnClick(View v) {
        int id = v.getId();

        if(id == mRadioBtnCancel.getId()) {
            mRadioBtnCancel.setChecked(true);
            mRadioBtnNext.setChecked(false);

            mLytCancelDetails.setVisibility(View.VISIBLE);
            mInfoProcessOrder.setVisibility(View.GONE);
            //mBtnProcess.setText("CONFIRM");

        } else if(id == mRadioBtnNext.getId()) {
            mRadioBtnNext.setChecked(true);
            mRadioBtnCancel.setChecked(false);

            mLytCancelDetails.setVisibility(View.GONE);
            mInfoProcessOrder.setVisibility(View.VISIBLE);
            //mBtnProcess.setText("CONTINUE");

        } else if(id == mBtnCancelReason.getId()) {
            AppCommonUtil.hideKeyboard(getActivity());

            DialogFragmentWrapper dialog = DialogFragmentWrapper.createSingleChoiceDialog("Select Cancel Reason",
                    getResources().getStringArray(R.array.newOrderCancelReasons), -1, true);
            dialog.setTargetFragment(OrderStatusChangeDialog.this, REQUEST_CANCEL_REASON);
            dialog.show(getFragmentManager(), DIALOG_CANCEL_REASON);

        } else if(id == mBtnProcess.getId()) {

            if(mRadioBtnCancel.isChecked()) {
                // Check reason is provided
                if(mBtnCancelReason.getText()==null || mBtnCancelReason.getText().toString().isEmpty()) {
                    mBtnCancelReason.setError("Select Cancel Reason");
                    //AppCommonUtil.toast(getActivity(), "Select Cancel Reason");
                } else {
                    mCallback.cancelOrder(mInputOrderId.getText().toString(), mBtnCancelReason.getText().toString());
                    getDialog().dismiss();
                }

            } else if(mRadioBtnNext.isChecked()) {
                mCallback.acceptOrder(mInputOrderId.getText().toString());
                getDialog().dismiss();

            } else {
                AppCommonUtil.toast(getActivity(), "Select Action to Process Order");
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogMy.d(TAG, "In onActivityResult :" + requestCode + ", " + resultCode);
        if(resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CANCEL_REASON:
                String str = data.getStringExtra(DialogFragmentWrapper.EXTRA_SELECTION);
                mBtnCancelReason.setText(str);
                break;
        }
    }

    @Override
    public void handleDialogBtnClick(DialogInterface dialog, int which) {
        // do nothing
    }

    @Override
    public boolean handleTouchUp(View v) {
        return false;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_order_status_change, null);

        bindUiResources(v);
        if(savedInstanceState!=null) {
            //LogMy.d(TAG,"Restoring");
            //mCustMobile = savedInstanceState.getString("mCustMobile");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(v);

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AppCommonUtil.setDialogTextSize(OrderStatusChangeDialog.this, (AlertDialog) dialog);
            }
        });

        return dialog;
    }

    private TextView mInputOrderId;
    private TextView mInputOrderStatus;
    private RadioButton mRadioBtnCancel;
    private RadioButton mRadioBtnNext;
    private View mLytCancelDetails;
    private EditText mBtnCancelReason;
    private TextView mInfoProcessOrder;
    private AppCompatButton mBtnProcess;


    private void bindUiResources(View v) {

        mInputOrderId = (TextView) v.findViewById(R.id.input_orderId);
        mInputOrderStatus = (TextView) v.findViewById(R.id.input_status);
        mRadioBtnCancel = (RadioButton) v.findViewById(R.id.radioBtnCancel);
        mRadioBtnNext = (RadioButton) v.findViewById(R.id.radioBtnNext);
        mLytCancelDetails = v.findViewById(R.id.layout_cancelDetails);
        mBtnCancelReason = (EditText) v.findViewById(R.id.input_cancelReason);
        mInfoProcessOrder = (TextView) v.findViewById(R.id.input_processInfo);
        mBtnProcess = (AppCompatButton) v.findViewById(R.id.btn_process);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putString("mCustMobile", mCustMobile);
    }
}

