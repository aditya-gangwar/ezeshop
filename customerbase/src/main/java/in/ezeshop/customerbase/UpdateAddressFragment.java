package in.ezeshop.customerbase;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.GenericListFragment;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.entities.MyAreas;
import in.ezeshop.appbase.entities.MyCities;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.ValidationHelper;
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
    private String mSelectedArea;

    // Special member variable to identify backstack cases
    private Integer mBackstackFlag;

    // Part of instance state: to be restored in event of fragment recreation
    private String mEditAddressId;
    //private boolean mIsModified;

    public interface UpdateAddressFragmentIf {
        MyRetainedFragment getRetainedFragment();
        void setToolbarForFrag(int iconResId, String title, String subTitle);
        void onUpdateAddress(CustAddress addr, boolean setAsDefault);
        void askSingleChoice(ArrayList<String> items, String title, Fragment callback, int requestCode);
        void startBgJob(int requestCode, String callingFragTag,
                        String argStr1, String argStr2, String argStr3, Long argLong1, Boolean argBool1);
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

            //setup all listeners
            initListeners();

            if(savedInstanceState==null) {
                // Either fragment 'create' or 'backstack' case
                if (mBackstackFlag==null) {
                    // fragment create case - initialize member variables
                    //mIsModified = false;
                    mBackstackFlag = 123; // dummy memory allocation - to check for backstack scenarios later
                    mEditAddressId = getArguments().getString(ARG_ADDR_ID);
                    // called only during creation to avoid losing edited fields
                    initUi();
                } else {
                    // backstack case - no need to initialize member variables
                    // as the same are automatically stored and restored
                    LogMy.d(TAG,"Backstack case");
                }
            } else {
                // fragment recreate case - restore member variables
                //mIsModified = savedInstanceState.getBoolean("mIsModified");
                mEditAddressId = savedInstanceState.getString("mEditAddressId");
            }

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

    private void initUi() {
        LogMy.d(TAG, "In initUi");

        // find address with given id
        // mEditAddressId will be null for add scenarios
        CustAddress editAddr = null;
        if(mEditAddressId!=null) {
            editAddr = CustomerUser.getInstance().getAddress(mEditAddressId);
        }

        if(editAddr!=null) {
            Areas area = editAddr.getAreaNIDB();
            Cities city = area.getCity();

            mInputCity.setText(city.getCity());
            mInputName.setText(editAddr.getToName());
            mInputContactNum.setText(editAddr.getContactNum());
            mInputAddress.setText(editAddr.getText1());
            mInputArea.setText(area.getAreaName());
            /*if(area.getValidated()) {
                mInputArea.setText(area.getAreaName());
                mCbxAreaNotListed.setChecked(false);
                mLytOtherArea.setVisibility(View.GONE);
            } else {
                // area earlier added as 'other area' and not validated yet
                mCbxAreaNotListed.setChecked(true);
                mCbxAreaNotListed.callOnClick(); // to do the processing associated with checkbox
                mInputOtherArea.setText(area.getAreaName());
            }*/
            mInputState.setText(city.getState());
            //mInputPincode.setText(area.getPincode());

            if(mEditAddressId.equals(CustomerUser.getInstance().getCustomer().getDefaultAddressId())) {
                mCbxDefaultAddr.setChecked(true);
            } else {
                mCbxDefaultAddr.setChecked(false);
            }

            mBtnUpdateAddr.setText("UPDATE ADDRESS");
            /*if(editAddr.getId().equals(CustomerUser.getInstance().getCustomer().getDefaultAddressId())) {
                mCbxDefaultAddr.setChecked(true);
            } else {
                mCbxDefaultAddr.setChecked(false);
            }*/
        } else {
            mBtnUpdateAddr.setText("ADD ADDRESS");
            // fill some fields from customer details
            mInputName.setText(CustomerUser.getInstance().getCustomer().getName());
            mInputContactNum.setText(CustomerUser.getInstance().getCustomer().getMobile_num());

            if(!CustomerUser.getInstance().getCustomer().getCity().isEmpty()) {
                Cities city = MyCities.getCityWithName(CustomerUser.getInstance().getCustomer().getCity());
                if(city!=null) {
                    mInputCity.setText(city.getCity());
                    mInputState.setText(city.getState());
                }
            }
            //mCbxAreaNotListed.setChecked(false);
            //mLytOtherArea.setVisibility(View.GONE);
        }
        mCbxDefaultAddr.setChecked(false);

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
                DialogFragmentWrapper dialog = DialogFragmentWrapper.createSingleChoiceDialog("Select City",
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
                    ArrayList<String> areaNameList = MyAreas.getAreaNameList(city);
                    if(areaNameList==null || areaNameList.isEmpty()) {
                        // fetch areas from DB
                        mCallback.startBgJob(MyRetainedFragment.REQUEST_FETCH_AREAS, getTag(),
                                city,null,null,null,null);
                    } else {
                        mSelectedArea = null;
                        mCallback.askSingleChoice(MyAreas.getAreaNameList(city), "Select Area", UpdateAddressFragment.this, REQUEST_AREA);
                    }
                    /*DialogFragmentWrapper dialog = DialogFragmentWrapper.createSingleChoiceDialog(getString(R.string.area_hint),
                            MyAreas.getAreaNameList(city), -1, true);
                    dialog.setTargetFragment(UpdateAddressFragment.this, REQUEST_AREA);
                    dialog.show(getFragmentManager(), DIALOG_AREA);*/
                }


            } /*else if(id==mCbxAreaNotListed.getId()) {
                if(mCbxAreaNotListed.isChecked()) {
                    LogMy.d(TAG, "Area not listed checkbox: Checked");
                    //mLytOtherArea.setVisibility(View.VISIBLE);
                    // make area non-editable
                    mLayoutArea.setAlpha(0.4f);
                    mInputArea.setOnClickListener(null);
                    mInputArea.setError(null);
                } else {
                    LogMy.d(TAG, "Area not listed checkbox: Not Checked");
                    //mLytOtherArea.setVisibility(View.GONE);
                    // make area selectable
                    mLayoutArea.setAlpha(1.0f);
                    mInputArea.setOnClickListener(this);
                }

            } */else if(id==mCbxDefaultAddr.getId()) {
                // do nothing

            } else if(id==mBtnUpdateAddr.getId()) {
                // validate fields
                if(validate()) {
                    CustAddress addr = createCustAddress();
                    // Check if edit or add case
                    boolean update = true;
                    if(mEditAddressId!=null && !mEditAddressId.isEmpty()) {
                        // Edit address case
                        // Check if anything modified
                        // ToDo: the 'areCustAddressEqual' function does not check for 'default Address' modification yet
                        CustAddress editAddr = CustomerUser.getInstance().getAddress(mEditAddressId);
                        if(!AppCommonUtil.areCustAddressEqual(editAddr,addr)) {
                            // something got modified
                            addr.setId(editAddr.getId()); // to identify to update existing address
                            //addr.getAreaNIDB().setValidated(editAddr.getAreaNIDB().getValidated());
                        } else {
                            AppCommonUtil.toast(getActivity(),"No Change");
                            update = false;
                        }
                    }

                    if(update) {
                        mCallback.onUpdateAddress(addr,mCbxDefaultAddr.isChecked());
                    }

                } else {
                    AppCommonUtil.toast(getActivity(),"Data Incorrect");
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
        area = MyAreas.getAreaObject(mInputCity.getText().toString(), mInputArea.getText().toString());
        /*if(mCbxAreaNotListed.isChecked()) {

            if(mEditAddressId!=null) {
                // Address edit scenario
                CustAddress editAddr = CustomerUser.getInstance().getAddress(mEditAddressId);
                Areas oldArea = editAddr.getAreaNIDB();

                // if area not changed - use existing area object
                if(oldArea.getAreaName().equals(mInputOtherArea.getText().toString()) &&
                        oldArea.getCity().getCity().equals(mInputCity.getText().toString())) {
                    area = oldArea;
                }
            }

            if(area==null) {
                // Either new address case, or
                // OtherArea is also added/edited
                area = new Areas();
                area.setValidated(false);
                area.setAreaName(mInputOtherArea.getText().toString());
                //area.setPincode(mInputPincode.getText().toString());
                Cities city = MyCities.getCityWithName(mInputCity.getText().toString());
                area.setCity(city);
            }

        } else {
            area = MyAreas.getAreaObject(mInputCity.getText().toString(), mInputArea.getText().toString());
        }*/

        addr.setAreaNIDB(area);
        addr.setText1(mInputAddress.getText().toString());
        addr.setCustPrivateId(CustomerUser.getInstance().getCustomer().getPrivate_id());

        return addr;
    }

    private boolean validate() {
        boolean allok = true;
        if(mInputName.getText().toString().isEmpty()) {
            mInputName.setError("Enter Name");
            allok = false;
        } else {
            mInputName.setError(null);
        }

        int status = ValidationHelper.validateMobileNo(mInputContactNum.getText().toString());
        if(status!=ErrorCodes.NO_ERROR) {
            mInputContactNum.setError(AppCommonUtil.getErrorDesc(status));
            allok = false;
        } else {
            mInputContactNum.setError(null);
        }

        if(mInputCity.getText().toString().isEmpty()) {
            mInputCity.setError("Select City");
            allok = false;
        } else {
            mInputCity.setError(null);
        }
        /*if(mCbxAreaNotListed.isChecked()) {
            if (mInputOtherArea.getText().toString().isEmpty()) {
                mInputOtherArea.setError("Enter Area Name");
                allok = false;
            } else {
                mInputOtherArea.setError(null);
            }
        } else {*/
            if (mInputArea.getText().toString().isEmpty()) {
                mInputArea.setError("Select Area");
                allok = false;
            } else {
                mInputArea.setError(null);
            }
        //}

        if(mInputAddress.getText().toString().isEmpty()) {
            mInputAddress.setError("Enter Address");
            allok = false;
        } else {
            mInputAddress.setError(null);
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
        if(resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CITY:
                String city = data.getStringExtra(DialogFragmentWrapper.EXTRA_SELECTION);
                String curValue = mInputCity.getText().toString();
                if(!curValue.equals(city)) {
                    mInputCity.setText(city);
                    mInputState.setText(MyCities.getCityWithName(city).getState());
                    mInputArea.setText("");
                }
                break;
            case REQUEST_AREA:
                String areaStr = data.getStringExtra(GenericListFragment.EXTRA_SELECTION);
                //mInputArea.setText(areaStr);
                // Cant set here directly - as mInputArea will be null currently
                LogMy.d(TAG,"Setting mSelectedArea: "+areaStr);
                mSelectedArea = areaStr;
                //Areas area = MyAreas.getAreaObject(mInputCity.getText().toString(), areaStr);
                //mInputPincode.setText(area.getPincode()==null?"":area.getPincode());
                break;

            case MyRetainedFragment.REQUEST_FETCH_AREAS:
                // areas fetched successfully, ask user to select
                String cityStr = mInputCity.getText().toString();
                if(cityStr.isEmpty()) {
                    AppCommonUtil.toast(getActivity(),"Select City");
                    mInputCity.requestFocus();

                } else {
                    ArrayList<String> areaNameList = MyAreas.getAreaNameList(cityStr);
                    if(areaNameList==null || areaNameList.isEmpty()) {
                        // no areas fetched from DB too - raise alarm
                        LogMy.wtf(TAG,"No areas fetched from DB for city: "+cityStr);
                        AppCommonUtil.toast(getActivity(), "No Areas Available");
                    } else {
                        mCallback.askSingleChoice(MyAreas.getAreaNameList(cityStr), "Select Area", UpdateAddressFragment.this, REQUEST_AREA);
                    }
                }
        }
    }

    private void initListeners() {
        mBtnUpdateAddr.setOnClickListener(this);
        mInputCity.setOnClickListener(this);
        mInputArea.setOnClickListener(this);
        mCbxDefaultAddr.setOnClickListener(this);
        //mCbxAreaNotListed.setOnClickListener(this);
    }

    // UI Resources data members
    @BindView(R2.id.btn_addAddress) AppCompatButton mBtnUpdateAddr;
    @BindView(R2.id.inputCity) TextInputEditText mInputCity;
    @BindView(R2.id.inputName) TextInputEditText mInputName;
    @BindView(R2.id.inputMobileNum) TextInputEditText mInputContactNum;
    @BindView(R2.id.inputAddrLine1) TextInputEditText mInputAddress;
    @BindView(R2.id.layoutArea) View mLayoutArea;
    @BindView(R2.id.inputArea) TextInputEditText mInputArea;
    //@BindView(R2.id.cbx_areaNotListed) CheckBox mCbxAreaNotListed;
    //@BindView(R2.id.layoutOtherArea) View mLytOtherArea;
    //@BindView(R2.id.inputOtherArea) TextInputEditText mInputOtherArea;
    @BindView(R2.id.inputState) TextInputEditText mInputState;
    //@BindView(R2.id.inputPincode) TextInputEditText mInputPincode;
    @BindView(R2.id.cbx_defaultAddress) CheckBox mCbxDefaultAddr;

    private Unbinder unbinder;

    @Override
    public void onResume() {
        super.onResume();

        try {
            //initUi();
            if(mEditAddressId!=null) {
                mCallback.setToolbarForFrag(-1, "Edit Address", null);
            } else {
                mCallback.setToolbarForFrag(-1, "Add New Address", null);
            }
            // As 'mSelectedArea' may get set outside this fragment - by askSingleChoice
            if(mSelectedArea!=null) {
                mInputArea.setText(mSelectedArea);
            }
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
        //outState.putBoolean("mIsModified", mIsModified);
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



