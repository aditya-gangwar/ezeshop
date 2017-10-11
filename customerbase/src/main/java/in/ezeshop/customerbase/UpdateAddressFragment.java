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
import in.ezeshop.appbase.entities.MyAreas;
import in.ezeshop.appbase.entities.MyCities;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.ValidationHelper;
import in.ezeshop.common.constants.CommonConstants;
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
    private String mEditAddressId;
    private boolean mIsModified;

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
                    mIsModified = false;
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
                mIsModified = savedInstanceState.getBoolean("mIsModified");
                mEditAddressId = savedInstanceState.getString("mEditAddressId");
            }

            //setup all listeners
            initListeners();

            mEditAddressId = getArguments().getString(ARG_ADDR_ID);
            updateUI();

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

        // find address with given id
        CustAddress editAddr = null;
        if(mEditAddressId!=null) {
            editAddr = CustomerUser.getInstance().getAddress(mEditAddressId);
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

            if(mEditAddressId.equals(CustomerUser.getInstance().getCustomer().getDefaultAddressId())) {
                mCbxDefaultAddr.setChecked(true);
            } else {
                mCbxDefaultAddr.setChecked(false);
            }

            mBtnUpdateAddr.setText("UPDATE ADDRESS");
        } else {
            mBtnUpdateAddr.setText("ADD ADDRESS");
        }
        mCbxDefaultAddr.setChecked(false);
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
                AppCommonUtil.hideKeyboard(getActivity());

                String city = mInputCity.getText().toString();
                if(city.isEmpty()) {
                    AppCommonUtil.toast(getActivity(),"Select City");
                    mInputCity.requestFocus();

                } else {
                    DialogFragmentWrapper dialog = DialogFragmentWrapper.createSingleChoiceDialog(getString(R.string.area_hint),
                            MyAreas.getAreaNameList(city), -1, true);
                    dialog.setTargetFragment(UpdateAddressFragment.this, REQUEST_AREA);
                    dialog.show(getFragmentManager(), DIALOG_AREA);
                }


            } else if(id==mCbxAreaNotListed.getId()) {
                if(mCbxAreaNotListed.isChecked()) {
                    mInputOtherArea.setVisibility(View.VISIBLE);
                    // make area non-editable
                    mInputArea.setAlpha(0.4f);
                    mInputAddress.setClickable(false);
                    mInputAddress.setFocusable(false);
                    // make pincode editable
                    mInputPincode.setClickable(true);
                    mInputPincode.setFocusable(true);
                } else {
                    mInputOtherArea.setVisibility(View.GONE);
                    // make area selectable
                    mInputArea.setAlpha(1.0f);
                    mInputAddress.setClickable(true);
                    mInputAddress.setFocusable(true);
                    // make pincode non-editable
                    mInputPincode.setClickable(false);
                    mInputPincode.setFocusable(false);
                }

            } else if(id==mCbxDefaultAddr.getId()) {
                // do nothing

            } else if(id==mBtnUpdateAddr.getId()) {
                // validate fields
                if(validate()) {
                    CustAddress addr = createCustAddress();
                    // Check if edit or add case
                    boolean update = true;
                    if(mEditAddressId!=null || !mEditAddressId.isEmpty()) {
                        // Edit address case
                        // Check if anything modified
                        if(!AppCommonUtil.areCustAddressEqual(CustomerUser.getInstance().getAddress(mEditAddressId),addr)) {
                            // something got modified
                            addr.setId(mEditAddressId); // to identify to update existing address
                        } else {
                            AppCommonUtil.toast(getActivity(),"No Change");
                            update = false;
                        }
                    }

                    if(update) {
                        mCallback.onUpdateAddress(addr,mCbxDefaultAddr.isChecked());
                    }

                } else {
                    AppCommonUtil.toast(getActivity(),"Fields Missing");
                }
            }

        } catch (Exception e) {
            LogMy.e(TAG, "Exception in ChooseAddressFrag:onClick", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    private CustAddress createCustAddress() {
        // First create CustAddress object from field values
        CustAddress addr = new CustAddress();
        addr.setToName(mInputName.getText().toString());
        addr.setContactNum(mInputContactNum.getText().toString());

        Areas area = null;
        if(mCbxAreaNotListed.isChecked()) {
            area = new Areas();
            area.setValidated(false);
            area.setAreaName(mInputOtherArea.getText().toString());
            area.setPincode(mInputPincode.getText().toString());
            Cities city = MyCities.getCityWithName(mInputCity.getText().toString());
            area.setCity(city);
        } else {
            area = MyAreas.getAreaObject(mInputCity.getText().toString(), mInputArea.getText().toString());
        }
        addr.setArea(area);
        addr.setText1(mInputAddress.getText().toString());
        addr.setCustPrivateId(CustomerUser.getInstance().getCustomer().getPrivate_id());

        return addr;
    }

    private boolean validate() {
        boolean allok = true;
        if(mInputName.getText().toString().isEmpty()) {
            mInputName.setError("Enter Name");
            allok = false;
        }
        int status = ValidationHelper.validateMobileNo(mInputContactNum.getText().toString());
        if(status!=ErrorCodes.NO_ERROR) {
            mInputContactNum.setError(AppCommonUtil.getErrorDesc(status));
            allok = false;
        }
        if(mInputCity.getText().toString().isEmpty()) {
            mInputCity.setError("Select City");
            allok = false;
        }
        if(mCbxAreaNotListed.isChecked()) {
            if (mInputOtherArea.getText().toString().isEmpty()) {
                mInputOtherArea.setError("Enter Area Name");
                allok = false;
            }
            status = ValidationHelper.validatePincode(mInputPincode.getText().toString());
            if(status!=ErrorCodes.NO_ERROR) {
                mInputPincode.setError(AppCommonUtil.getErrorDesc(status));
                allok = false;
            }
        } else {
            if (mInputArea.getText().toString().isEmpty()) {
                mInputArea.setError("Select Area");
                allok = false;
            }
        }
        if(mInputAddress.getText().toString().isEmpty()) {
            mInputAddress.setError("Enter Address");
            allok = false;
        }
        return allok;
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
                    mInputState.setText(MyCities.getCityWithName(city).getState());
                }
                break;
            case REQUEST_AREA:
                if(resultCode==ErrorCodes.NO_ERROR) {
                    String areaStr = data.getStringExtra(DialogFragmentWrapper.EXTRA_SELECTION);
                    mInputArea.setText(areaStr);
                    Areas area = MyAreas.getAreaObject(mInputCity.getText().toString(), areaStr);
                    mInputPincode.setText(area.getPincode()==null?"":area.getPincode());
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
    @BindView(R2.id.cbx_areaNotListed) CheckBox mCbxAreaNotListed;
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
        outState.putBoolean("mIsModified", mIsModified);
        outState.putString("mEditAddressId", mEditAddressId);
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



