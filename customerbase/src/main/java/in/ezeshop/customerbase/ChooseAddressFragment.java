package in.ezeshop.customerbase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ChooseAddressFragment extends BaseFragment {
    private static final String TAG = "CustApp-ChooseAddressFragment";

    private static final int REQ_NOTIFY_ERROR = 1;
    private static final int REQ_NOTIFY_ERROR_EXIT = 5;

    private MyRetainedFragment mRetainedFragment;
    private ChooseAddressFragment.ChooseAddressFragmentIf mCallback;

    public interface ChooseAddressFragmentIf {
        MyRetainedFragment getRetainedFragment();
        void setToolbarForFrag(int iconResId, String title, String subTitle);
        void onSelectAddress(CustAddress addr);
        void onAddAddress();
        void onEditAddress(String addrId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_choose_address, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogMy.d(TAG, "In onActivityCreated");

        try {
            mCallback = (ChooseAddressFragment.ChooseAddressFragmentIf) getActivity();
            mRetainedFragment = mCallback.getRetainedFragment();

            mCardViewArr = new View[]{mCardViewAddr1,mCardViewAddr2,mCardViewAddr3,mCardViewAddr4};
            mBtnEditArr = new TextView[]{mBtnEditAddress1,mBtnEditAddress2,mBtnEditAddress3,mBtnEditAddress4};
            mLytAddressArr = new View[]{mLytAddress1,mLytAddress2,mLytAddress3,mLytAddress4};
            mInputNameArr = new TextView[]{mInputToName1,mInputToName2,mInputToName3,mInputToName4};
            mInputAddrArr = new TextView[]{mInputAddress1,mInputAddress2,mInputAddress3,mInputAddress4};

            //setup all listeners
            initListeners();

        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement ChooseAddressFragmentIf");
        } catch(Exception e) {
            LogMy.e(TAG, "Exception in onActivityCreated", e);
            // unexpected exception - show error
            DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            notDialog.setTargetFragment(this,REQ_NOTIFY_ERROR);
            notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    private void updateUI() {
        LogMy.d(TAG, "In updateUI");
        // show all address in CustomerUser address storage
        List<CustAddress> addresses = CustomerUser.getInstance().getAllAddress();

        int i=0;
        for (CustAddress addr : addresses) {
            mCardViewArr[i].setVisibility(View.VISIBLE);
            mInputNameArr[i].setText(addr.getToName());
            mInputAddrArr[i].setText(CommonUtils.getCustAddressStr(addr));
            i++;
        }
        // hide remaining
        for(;i<CommonConstants.MAX_ADDRESS_PER_CUSTOMER;i++) {
            mCardViewArr[i].setVisibility(View.GONE);
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
            if (id == mBtnAddAddress.getId()) {
                // check if 4 already added
                if(CustomerUser.getInstance().getAllAddress().size() < CommonConstants.MAX_ADDRESS_PER_CUSTOMER) {
                    mCallback.onAddAddress();
                } else {
                    AppCommonUtil.toast(getActivity(),"Max 4 Address allowed");
                }

            } else if (id==R.id.cardBtn_editAddress1 || id==R.id.cardBtn_editAddress2 || id==R.id.cardBtn_editAddress3 || id==R.id.cardBtn_editAddress4) {
                // any one address is to be edited
                int idx = getAddrIndex(id);
                LogMy.d(TAG,"Edit address. Idx: "+idx);
                mCallback.onEditAddress(CustomerUser.getInstance().getAllAddress().get(idx).getId());

            } else if (id==R.id.lyt_address1 || id==R.id.lyt_address2 || id==R.id.lyt_address3 || id==R.id.lyt_address4) {
                // any one address is selected
                int idx = getAddrIndex(id);
                LogMy.d(TAG,"Selected address. Idx: "+idx);
                //mCallback.onSelectAddress(CustomerUser.getInstance().getAllAddress().get(idx).getId());
                mCallback.onSelectAddress(CustomerUser.getInstance().getAllAddress().get(idx));
            }
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in handleDialogBtnClick", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    private int getAddrIndex(int id) {
        for(int i=0; i<CommonConstants.MAX_ADDRESS_PER_CUSTOMER; i++) {
            if(mBtnEditArr[i].getId()==id || mLytAddressArr[i].getId()==id) {
                return i;
            }
        }

        LogMy.e(TAG,"In getAddrIndex: Invalid id: "+id);
        return -1;
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
        mBtnAddAddress.setOnClickListener(this);

        // set for all addresses
        for(int i=0; i< CommonConstants.MAX_ADDRESS_PER_CUSTOMER; i++) {
            mLytAddressArr[i].setOnClickListener(this);
            mBtnEditArr[i].setOnClickListener(this);
        }
    }

    // UI Resources data members
    @BindView(R2.id.btn_addAddress) TextView mBtnAddAddress;

    @BindView(R2.id.cardview_address1) View mCardViewAddr1;
    @BindView(R2.id.cardBtn_editAddress1) TextView mBtnEditAddress1;
    @BindView(R2.id.lyt_address1) View mLytAddress1;
    @BindView(R2.id.cardTitle_toName1) TextView mInputToName1;
    @BindView(R2.id.input_address1) TextView mInputAddress1;

    @BindView(R2.id.cardview_address2) View mCardViewAddr2;
    @BindView(R2.id.cardBtn_editAddress2) TextView mBtnEditAddress2;
    @BindView(R2.id.lyt_address2) View mLytAddress2;
    @BindView(R2.id.cardTitle_toName2) TextView mInputToName2;
    @BindView(R2.id.input_address2) TextView mInputAddress2;

    @BindView(R2.id.cardview_address3) View mCardViewAddr3;
    @BindView(R2.id.cardBtn_editAddress3) TextView mBtnEditAddress3;
    @BindView(R2.id.lyt_address3) View mLytAddress3;
    @BindView(R2.id.cardTitle_toName3) TextView mInputToName3;
    @BindView(R2.id.input_address3) TextView mInputAddress3;

    @BindView(R2.id.cardview_address4) View mCardViewAddr4;
    @BindView(R2.id.cardBtn_editAddress4) TextView mBtnEditAddress4;
    @BindView(R2.id.lyt_address4) View mLytAddress4;
    @BindView(R2.id.cardTitle_toName4) TextView mInputToName4;
    @BindView(R2.id.input_address4) TextView mInputAddress4;

    View mCardViewArr[];
    TextView mBtnEditArr[];
    View mLytAddressArr[];
    TextView mInputNameArr[];
    TextView mInputAddrArr[];

    private Unbinder unbinder;

    @Override
    public void onResume() {
        super.onResume();
        mCallback.setToolbarForFrag(-1,"Select Address",null);

        try {
            // intentionally called from onResume
            // to get addresses automatically updated in case address add/edit
            updateUI();
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
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        // Clear any configuration that was done!
        EasyImage.clearConfiguration(getActivity());
        super.onDestroy();
    }

}


