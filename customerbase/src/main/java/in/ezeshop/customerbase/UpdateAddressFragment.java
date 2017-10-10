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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.entities.MyCities;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Cities;
import in.ezeshop.common.database.CustAddress;
import in.ezeshop.common.database.Areas;
import in.ezeshop.customerbase.entities.CustomerUser;
import in.ezeshop.customerbase.helper.MyRetainedFragment;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * Created by adgangwa on 09-10-2017.
 */

public class UpdateAddressFragment extends BaseFragment {
    private static final String TAG = "CustApp-UpdateAddressFragment";

    private static final int REQ_NOTIFY_ERROR = 1;
    private static final int REQ_NOTIFY_ERROR_EXIT = 3;

    private static final String DIALOG_CITY = "DialogCity";
    private static final int REQUEST_CITY = 10;
    private static final String DIALOG_AREA = "DialogArea";
    private static final int REQUEST_AREA = 12;

    private static final String ARG_ADDR_ID = "argAddrId";

    private UpdateAddressFragment.UpdateAddressFragmentIf mCallback;
    private MyRetainedFragment mRetainedFragment;

    // Special member variable to identify backstack cases
    private Integer mBackstackFlag;

    // Part of instance state: to be restored in event of fragment recreation
    private boolean isModified;

    public interface UpdateAddressFragmentIf {
        MyRetainedFragment getRetainedFragment();
        void setDrawerState(boolean isEnabled);
        void onUpdateAddress(CustAddress addr, boolean setAsDefault);
    }

    public static UpdateAddressFragment getInstance(String addressId) {
        Bundle args = new Bundle();
        args.putString(ARG_ADDR_ID, addressId);
        UpdateAddressFragment fragment = new UpdateAddressFragment();
        fragment.setArguments(args);
        return fragment;
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

            if(savedInstanceState==null) {
                // Either fragment 'create' or 'backstack' case
                if (mBackstackFlag==null) {
                    // fragment create case - initialize member variables
                    isModified = false;
                    mBackstackFlag = 123; // dummy memory allocation - to check for backstack scenarios later
                    // On first create set 'default address' as selected address
                    mRetainedFragment.mSelectedAddrId = CustomerUser.getInstance().getCustomer().getDefaultAddressId();
                } else {
                    // backstack case - no need to initialize member variables
                    // as the same are automatically stored and restored
                    LogMy.d(TAG,"Backstack case");
                }
            } else {
                // fragment recreate case - restore member variables
                isModified = savedInstanceState.getBoolean("isModified");
            }

            //setup all listeners
            initListeners();

            updateUI(getArguments().getString(ARG_ADDR_ID));

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

    private void updateUI(String editAddrId) {
        LogMy.d(TAG, "In updateUI");

        // find address with given id
        CustAddress editAddr = null;
        if(editAddrId!=null) {
            List<CustAddress> allAddress = CustomerUser.getInstance().getCustomer().getAddresses();
            if (!(allAddress == null || allAddress.isEmpty())) {
                for (CustAddress addr :
                        allAddress) {
                    if (addr.getId().equals(editAddrId)) {
                        editAddr = addr;
                        break;
                    }
                }
            }
        }

        if(editAddr!=null) {
            Areas area = editAddr.getArea();
            Cities city = area.getCity();

            mInputCity.setText(city.getCity());
            mInputName.setText(editAddr.getToName());
            mInputContactNum.setText(editAddr.getContactNum());
            mInputAddress.setText(editAddr.getText1());
            mInputArea.setText(area.getAreaName());
            mInputState.setText(city.getState());
            mInputPincode.setText(area.getPincode());

            if(editAddrId.equals(CustomerUser.getInstance().getCustomer().getDefaultAddressId())) {
                mCbxDefaultAddr.setChecked(true);
            } else {
                mCbxDefaultAddr.setChecked(false);
            }

            mBtnUpdateAddr.setText("UPDATE ADDRESS");
        } else {
            mBtnUpdateAddr.setText("ADD ADDRESS");
        }
        mInputOtherArea.setVisibility(View.GONE);

    }

    // Using BaseFragment's onClick method - to avoid double clicks
    @Override
    public void handleBtnClick(View v) {
        if(!mCallback.getRetainedFragment().getResumeOk())
            return;

        try {
            LogMy.d(TAG, "In handleBtnClick: " + v.getId());
            int id = v.getId();

            if(id==mInputCity.getId()) {
                AppCommonUtil.hideKeyboard(getActivity());
                DialogFragmentWrapper dialog = DialogFragmentWrapper.createSingleChoiceDialog(getString(R.string.city_hint),
                        MyCities.getCityValueSet(), -1, true);
                dialog.setTargetFragment(UpdateAddressFragment.this,REQUEST_CITY);
                dialog.show(getFragmentManager(), DIALOG_CITY);

            } else if(id==mInputArea.getId()) {

            } else if(id==mCbxDefaultAddr.getId()) {

            } else if(id==mBtnUpdateAddr.getId()) {

            }

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
        switch (requestCode) {
            case REQUEST_CITY:
                if(resultCode==ErrorCodes.NO_ERROR) {
                    String city = data.getStringExtra(DialogFragmentWrapper.EXTRA_SELECTION);
                    mInputCity.setText(city);
                }
                break;
        }
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
            //updateUI();
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
        outState.putBoolean("isModified", isModified);
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



