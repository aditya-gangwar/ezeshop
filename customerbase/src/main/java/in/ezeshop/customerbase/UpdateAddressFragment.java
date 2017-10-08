package in.ezeshop.customerbase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.CustAddress;
import in.ezeshop.customerbase.entities.CustomerUser;
import in.ezeshop.customerbase.helper.MyRetainedFragment;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * Created by adgangwa on 09-10-2017.
 */

public class UpdateAddressFragment extends BaseFragment {
    private static final String TAG = "CustApp-UpdateAddressFragment";

    private static final int REQ_NOTIFY_ERROR = 1;
    private static final int REQ_NOTIFY_ERROR_EXIT = 5;

    private MyRetainedFragment mRetainedFragment;
    private UpdateAddressFragment.UpdateAddressFragmentIf mCallback;

    public interface UpdateAddressFragmentIf {
        MyRetainedFragment getRetainedFragment();
        void setDrawerState(boolean isEnabled);
        void onUpdateAddress(String addrId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_address, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogMy.d(TAG, "In onActivityCreated");

        try {
            mCallback = (UpdateAddressFragment.UpdateAddressFragmentIf) getActivity();
            mRetainedFragment = mCallback.getRetainedFragment();

            //setup all listeners
            initListeners();

        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement UpdateAddressFragmentIf");
        } catch(Exception e) {
            LogMy.e(TAG, "Exception is UpdateAddressFragment:onActivityCreated", e);
            // unexpected exception - show error
            DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            notDialog.setTargetFragment(this,REQ_NOTIFY_ERROR);
            notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    private void updateUI() {
        LogMy.d(TAG, "In updateUI");
    }

    // Using BaseFragment's onClick method - to avoid double clicks
    @Override
    public void handleBtnClick(View v) {
        if(!mCallback.getRetainedFragment().getResumeOk())
            return;

        try {
            LogMy.d(TAG, "In handleBtnClick: " + v.getId());
            int id = v.getId();

        } catch (Exception e) {
            LogMy.e(TAG, "Exception in ChooseAddressFrag:onClick", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
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
    }

    private void initListeners() {
        mBtnUpdateAddr.setOnClickListener(this);
        mInputCity.setOnClickListener(this);
        mInputArea.setOnClickListener(this);
        mCbxDefaultAddr.setOnClickListener(this);
    }

    // UI Resources data members
    @BindView(R2.id.btn_addAddress) AppCompatButton mBtnUpdateAddr;
    @BindView(R2.id.inputCity) TextInputEditText mInputCity;
    @BindView(R2.id.inputName) TextInputEditText mInputName;
    @BindView(R2.id.inputMobileNum) TextInputEditText mInputContactNum;
    @BindView(R2.id.inputAddrLine1) TextInputEditText mInputAddress;
    @BindView(R2.id.inputArea) TextInputEditText mInputArea;
    @BindView(R2.id.inputOtherArea) TextInputEditText mInputOtherArea;
    @BindView(R2.id.inputState) TextInputEditText mInputState;
    @BindView(R2.id.inputPincode) TextInputEditText mInputPincode;
    @BindView(R2.id.cbx_defaultAddress) CheckBox mCbxDefaultAddr;

    private Unbinder unbinder;

    @Override
    public void onResume() {
        super.onResume();
        mCallback.setDrawerState(false);

        try {
            updateUI();
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in CustomerTransactionFragment:onResume", e);
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
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        // Clear any configuration that was done!
        EasyImage.clearConfiguration(getActivity());
        super.onDestroy();
    }

}



