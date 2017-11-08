package in.ezeshop.merchantbase;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import id.zelory.compressor.Compressor;
import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.ImageViewActivity;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.entities.MyTransaction;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Merchants;
import in.ezeshop.common.database.Transaction;
import in.ezeshop.merchantbase.entities.MerchantUser;
import in.ezeshop.merchantbase.helper.MyRetainedFragment;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by adgangwa on 04-11-2016.
 */
public class TxnConfirmFragment extends BaseFragment implements
        EasyPermissions.PermissionCallbacks {
    private static final String TAG = "MchntApp-TxnConfirmFragment";
    //private static final String ARG_CASH_PAID = "cashPaid";

    private static final int RC_HANDLE_CAMERA_PERM = 10;

    private Merchants mMerchant;
    private TxnConfirmFragmentIf mCallback;
    private MyRetainedFragment mRetainedFragment;

    // Container Activity must implement this interface
    public interface TxnConfirmFragmentIf {
        MyRetainedFragment getRetainedFragment();
        void onTransactionConfirm();
        void setDrawerState(boolean isEnabled);
    }

    public static TxnConfirmFragment getInstance(int cashPaid) {
        //Bundle args = new Bundle();
        //args.putInt(ARG_CASH_PAID, cashPaid);
        TxnConfirmFragment fragment = new TxnConfirmFragment();
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_txn_confirm3, container, false);
        unbinder = ButterKnife.bind(this, v);

        try {
            bindUiResources(v);

            mInputBillNum.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction()==MotionEvent.ACTION_UP) {
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    }
                    return false;
                }
            });

        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            getActivity().onBackPressed();
        }

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mCallback = (TxnConfirmFragmentIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement TxnConfirmFragmentIf");
        }

        try {
            mMerchant = MerchantUser.getInstance().getMerchant();
            mRetainedFragment = mCallback.getRetainedFragment();

            mPrescripImgLytArr = new View[]{mLytPrescripImg1,mLytPrescripImg2,mLytPrescripImg3,mLytPrescripImg4};
            mPrescripImgArr = new ImageView[]{mImgPrescrip1,mImgPrescrip2,mImgPrescrip3,mImgPrescrip4};
            mPrescripImgDelArr = new ImageView[]{mImgPrescripDel1,mImgPrescripDel2,mImgPrescripDel3,mImgPrescripDel4};

            initListeners();
            displayTransactionValues();

        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            getActivity().onBackPressed();
        }
    }

    // Not using BaseFragment's onTouch
    // as we dont want 'double touch' check against these buttons
    @Override
    public boolean handleTouchUp(View v) {
        return false;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(!mCallback.getRetainedFragment().getResumeOk())
            return true;

        try {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                LogMy.d(TAG, "In onTouch: " + v.getId());

                int id = v.getId();
                if (id==R.id.img_precrips_1 || id==R.id.img_precrips_2 || id==R.id.img_precrips_3 || id==R.id.img_precrips_4) {
                    File img = mRetainedFragment.mBillImgs.get(getImgIndex(id));
                    LogMy.d(TAG,"Image file: "+img.getAbsolutePath()+","+img.getPath()+","+Uri.fromFile(img));

                    // Show appropriate uploaded prescription preview
                    Intent intent = new Intent(getActivity(), ImageViewActivity.class );
                    intent.putExtra(ImageViewActivity.INTENT_EXTRA_URI, Uri.fromFile(img));
                    startActivity(intent);

                } else if (id == R.id.img_precrips_del_1 || id == R.id.img_precrips_del_2 || id == R.id.img_precrips_del_3 || id == R.id.img_precrips_del_4) {
                    // Delete clicked uploaded prescription
                    mRetainedFragment.mBillImgs.remove(getImgIndex(id));
                    refreshBillImgs();

                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in onTouch", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }

        return true;
    }

    private int getImgIndex(int id) {
        for(int i=0; i<CommonConstants.MAX_PRESCRIPS_PER_ORDER; i++) {
            if(mPrescripImgDelArr[i].getId()==id || mPrescripImgArr[i].getId()==id) {
                return i;
            }
        }
        LogMy.e(TAG,"In getImgIndex: Invalid id: "+id);
        return -1;
    }

    @Override
    public void handleBtnClick(View v) {
        if(v.getId()==R.id.btn_txn_confirm) {
            if(!mCallback.getRetainedFragment().getResumeOk())
                return;

            // check if invoice num is mandatory
            if (mMerchant.getInvoiceNumAsk() &&
                    !mMerchant.getInvoiceNumOptional() &&
                    mInputBillNum.getText().toString().isEmpty()) {
                mInputBillNum.setError("Enter Linked Invoice Number");
            } else {
                AppCommonUtil.hideKeyboard(getActivity());
                Transaction curTxn = mCallback.getRetainedFragment().mCurrTransaction.getTransaction();
                curTxn.setInvoiceNum(mInputBillNum.getText().toString());
                //curTxn.setComments(mInputComments.getText().toString());
                mCallback.onTransactionConfirm();
            }

        } else if (v.getId() == mBtnAddPrescrip.getId()) {
            handleImgUploadReq();
        }
    }

    private void displayTransactionValues() {

        Transaction curTransaction = mRetainedFragment.mCurrTransaction.getTransaction();

        AppCommonUtil.showAmtColor(getActivity(), null, mInputBillAmt, curTransaction.getTotal_billed(),false);
        if(curTransaction.getTrans_id()==null) {
            // this txn is not for online order
            mLayoutDelChgs.setVisibility(View.GONE);
        } else {
            mLayoutDelChgs.setVisibility(View.VISIBLE);
            AppCommonUtil.showAmtColor(getActivity(), null, mInputDelChgs, curTransaction.getDelCharge(),false);
        }

        // set account add/debit amount
        int value = curTransaction.getCl_credit() - curTransaction.getCl_debit();
        AppCommonUtil.showAmtColor(getActivity(), null, mInputAcc, value, false);

        // show/hide overdraft data
        if(curTransaction.getCl_overdraft() > 0) {
            mLayoutOverdraft.setVisibility(View.VISIBLE);
            mInputOverdraft.setText(AppCommonUtil.getNegativeAmtStr(curTransaction.getCl_overdraft()));
        } else {
            mLayoutOverdraft.setVisibility(View.GONE);
        }

        AppCommonUtil.showAmtColor(getActivity(), null, mInputPayment, curTransaction.getPaymentAmt(),false);

        value = curTransaction.getCb_credit() + curTransaction.getExtra_cb_credit();
        AppCommonUtil.showAmtColor(getActivity(), null, mInputAddCb, value,false);

        mInputCbDetails.setText(MyTransaction.getCbDetailStr(curTransaction,false));

        /* More Details */
        if(curTransaction.getTrans_id()==null || curTransaction.getTrans_id().isEmpty()) {
            // this txn is not for online order
            mLayoutOrderId.setVisibility(View.GONE);
        } else {
            mLayoutOrderId.setVisibility(View.VISIBLE);
            mInputOrderId.setText(curTransaction.getTrans_id());
        }
        // Bill Number
        if(mMerchant.getInvoiceNumAsk()) {
            mLayoutExtraDetails.setVisibility(View.VISIBLE);
            mLayoutBillNum.setVisibility(View.VISIBLE);
            if(mMerchant.getInvoiceNumOnlyNumbers()) {
                mInputBillNum.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            }
        } else {
            mLayoutExtraDetails.setVisibility(View.GONE);
            mLayoutBillNum.setVisibility(View.GONE);
        }
    }

    private void refreshBillImgs() {
        LogMy.d(TAG,"In refreshBillImgs");

        if(mRetainedFragment.mBillImgs ==null || mRetainedFragment.mBillImgs.isEmpty()) {
            mLytPrescripImgs.setVisibility(View.GONE);
        } else {
            mLytPrescripImgs.setVisibility(View.VISIBLE);
            // Make all invisible first
            for(int i = 0; i< CommonConstants.MAX_PRESCRIPS_PER_ORDER; i++) {
                mPrescripImgLytArr[i].setVisibility(View.GONE);
                mPrescripImgArr[i].setImageResource(R.drawable.ic_description_black_18dp);
            }

            int indx = 0;
            for (File img :
                    mRetainedFragment.mBillImgs) {

                final View imgLyt =mPrescripImgLytArr[indx];
                imgLyt.setVisibility(View.VISIBLE);
                LogMy.d(TAG,"Image file: "+img.getAbsolutePath()+","+img.getPath()+","+ Uri.fromFile(img));

                mPrescripImgArr[indx].setImageURI(Uri.fromFile(img));
                mPrescripImgArr[indx].setScaleType(ImageView.ScaleType.CENTER_CROP);
                indx++;
            }
        }
    }

    private void handleImgUploadReq() {
        // check for camera permission
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            // Already have permission, do the thing
            if(mRetainedFragment.mBillImgs ==null || mRetainedFragment.mBillImgs.size()<4) {
                // show chooser (camera/gallery) to upload prescription
                EasyImage.openChooserWithGallery(this, "Select Image From ?", 0);
            } else {
                String str = "Max "+CommonConstants.MAX_PRESCRIPS_PER_ORDER+" Bill images allowed";
                AppCommonUtil.toast(getActivity(), str);
            }

        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "Camera permission is required to take photo of the Bill",
                    RC_HANDLE_CAMERA_PERM, perms);
        }
    }

    /*
     * Permission handling related overrides
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        handleImgUploadReq();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        LogMy.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // Check if user selected - never ask again
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // Never ask again selected
            new AppSettingsDialog.Builder(this)
                    .setRationale("Cannot upload Bill images without Camera permission. \n\nPress 'Ok' and select 'Permissions' on the Settings screen to provide Camera permission.")
                    .build().show();
        } else {
            // Denied - show rationale for the same
            DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.noPermissionTitle,
                    "Cannot upload Bill images without Camera permission.", false, true);
            notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogMy.d(TAG, "In onActivityResult :" + requestCode + ", " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                AppCommonUtil.cancelProgressDialog(true);
                LogMy.e(TAG,"Error returned by Image picker. Image Source: "+source,e);
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                AppCommonUtil.showProgressDialog(getActivity(),AppConstants.progressDefault);
                // Append picked image(s) to the store
                // Do check for maximum prescriptions per order
                int ignored = 0;
                int newImgs = 0;
                for (File img : imageFiles) {
                    if(!img.exists()) {
                        LogMy.wtf(TAG,"Picked image file does not exist");
                        AppCommonUtil.cancelProgressDialog(true);
                        AppCommonUtil.toast(getActivity(),"Image upload failed");
                        return;
                    }
                    if(mRetainedFragment.mBillImgs ==null) {
                        mRetainedFragment.mBillImgs = new ArrayList<>(CommonConstants.MAX_PRESCRIPS_PER_ORDER);
                    }
                    if(mRetainedFragment.mBillImgs.size() < CommonConstants.MAX_PRESCRIPS_PER_ORDER) {
                        try {
                            File compressedImage = new Compressor(getActivity())
                                    .setMaxWidth(AppConstants.IMG_PRESCRIP_MAX_WIDTH)
                                    .setMaxHeight(AppConstants.IMG_PRESCRIP_MAX_HEIGHT)
                                    .setQuality(AppConstants.IMG_PRESCRIP_COMPRESS_RATIO)
                                    .setCompressFormat(AppCommonUtil.getImgCompressFormat())
                                    .compressToFile(img);
                            LogMy.d(TAG, "Image compress: " + (img.length() / 1024) + ", " + (compressedImage.length() / 1024));

                            LogMy.d(TAG,"Before rename: "+compressedImage.getAbsolutePath());
                            File newFile = new File(compressedImage.getParent(),
                                    CommonUtils.getCustPrescripFilename(CustomerUser.getInstance().getCustomer().getPrivate_id()));
                            if( compressedImage.renameTo(newFile) ) {
                                // file rename success
                                LogMy.d(TAG,"After rename: "+newFile.getAbsolutePath());

                                if(newFile.exists()) {
                                    mRetainedFragment.mBillImgs.add(newFile);
                                    if (compressedImage.exists()) {
                                        getActivity().deleteFile(compressedImage.getName());
                                    }
                                } else {
                                    LogMy.wtf(TAG, "onImagesPicked: New file after rename does not exist");
                                    mRetainedFragment.mBillImgs.add(compressedImage);
                                }
                            } else {
                                mRetainedFragment.mBillImgs.add(compressedImage);
                            }

                        } catch(Exception e) {
                            LogMy.e(TAG,"Failed to compress image",e);
                            mRetainedFragment.mBillImgs.add(img);
                        }
                        newImgs++;
                    } else {
                        ignored++;
                    }
                }
                if(newImgs>0) {
                    refreshBillImgs();
                }

                AppCommonUtil.cancelProgressDialog(true);
                if(ignored>0) {
                    String str = ignored+" images ignored, as upto "+ CommonConstants.MAX_PRESCRIPS_PER_ORDER +" prescriptions are allowed per order.";
                    DialogFragmentWrapper.createNotification(AppConstants.generalInfoTitle,
                            str, true, false).show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
                }
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(getActivity());
                    if (photoFile != null) photoFile.delete();
                }
            }
        });

        try {
            switch (requestCode) {
                case AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE:
                    // User returned from app settings screen
                    // Check for permissions again - to see if user gave the permissions or not
                    String[] perms = {Manifest.permission.CAMERA};
                    if (EasyPermissions.hasPermissions(getActivity(), perms)) {
                        handleImgUploadReq();
                    }
                    // doing nothing, if permission still not given
                    // will anyways be tried again - if the user presses add button again
                    break;
            }
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in onActivityResult", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    private void initListeners() {

        mImgPrescrip1.setOnTouchListener(this);
        mImgPrescrip2.setOnTouchListener(this);
        mImgPrescrip3.setOnTouchListener(this);
        mImgPrescrip4.setOnTouchListener(this);

        mImgPrescripDel1.setOnTouchListener(this);
        mImgPrescripDel2.setOnTouchListener(this);
        mImgPrescripDel3.setOnTouchListener(this);
        mImgPrescripDel4.setOnTouchListener(this);

        // Avoid double clicks on the button
        mBtnConfirm.setOnClickListener(this);
        mBtnAddPrescrip.setOnClickListener(this);

    }



    private EditText mInputBillAmt;
    private LinearLayout mLayoutDelChgs;
    private EditText mInputDelChgs;
    private LinearLayout mLayoutAcc;
    private EditText mInputAcc;
    private LinearLayout mLayoutOverdraft;
    private EditText mInputOverdraft;

    private EditText mInputPayment;
    private EditText mInputAddCb;
    private EditText mInputCbDetails;

    private View mLayoutExtraDetails;
    private LinearLayout mLayoutOrderId;
    private EditText mInputOrderId;

    private View mLayoutBillNum;
    private EditText mInputBillNum;

    private AppCompatButton mBtnConfirm;

    // Ignore 'presscrip' in variable names
    // assume that 'bill' - its copy-paste
    @BindView(R2.id.layout_prescrip_imgs) View mLytPrescripImgs;

    @BindView(R2.id.layout_img_prescrips_1) View mLytPrescripImg1;
    @BindView(R2.id.img_precrips_1) ImageView mImgPrescrip1;
    @BindView(R2.id.img_precrips_del_1) ImageView mImgPrescripDel1;
    @BindView(R2.id.layout_img_prescrips_2) View mLytPrescripImg2;
    @BindView(R2.id.img_precrips_2) ImageView mImgPrescrip2;
    @BindView(R2.id.img_precrips_del_2) ImageView mImgPrescripDel2;
    @BindView(R2.id.layout_img_prescrips_3) View mLytPrescripImg3;
    @BindView(R2.id.img_precrips_3) ImageView mImgPrescrip3;
    @BindView(R2.id.img_precrips_del_3) ImageView mImgPrescripDel3;
    @BindView(R2.id.layout_img_prescrips_4) View mLytPrescripImg4;
    @BindView(R2.id.img_precrips_4) ImageView mImgPrescrip4;
    @BindView(R2.id.img_precrips_del_4) ImageView mImgPrescripDel4;

    @BindView(R2.id.layout_addPrescrips) View mLytAddPrescrip;
    @BindView(R2.id.btn_addPrescrips) TextView mBtnAddPrescrip;

    View mPrescripImgLytArr[];
    ImageView mPrescripImgArr[];
    ImageView mPrescripImgDelArr[];

    private Unbinder unbinder;


    private void bindUiResources(View v) {
        mInputBillAmt = (EditText) v.findViewById(R.id.input_bill_amt);
        mLayoutDelChgs = (LinearLayout) v.findViewById(R.id.layout_delCharges);
        mInputDelChgs = (EditText) v.findViewById(R.id.input_delCharges);
        mLayoutAcc = (LinearLayout) v.findViewById(R.id.layout_account);
        mInputAcc = (EditText) v.findViewById(R.id.input_account);
        mLayoutOverdraft = (LinearLayout) v.findViewById(R.id.layout_overdraft);
        mInputOverdraft = (EditText) v.findViewById(R.id.input_overdraft);

        mInputPayment = (EditText) v.findViewById(R.id.input_cash_paid);
        mInputAddCb = (EditText) v.findViewById(R.id.input_add_cb);
        mInputCbDetails = (EditText) v.findViewById(R.id.input_cb_details);

        mLayoutExtraDetails = v.findViewById(R.id.layout_extra_details);
        mLayoutOrderId = (LinearLayout) v.findViewById(R.id.layout_orderId);
        mInputOrderId = (EditText) v.findViewById(R.id.input_orderId);
        mLayoutBillNum = v.findViewById(R.id.layout_invoice_num);
        mInputBillNum = (EditText) v.findViewById(R.id.input_invoice_num);

        mBtnConfirm = (AppCompatButton) v.findViewById(R.id.btn_txn_confirm);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCallback.setDrawerState(false);
        mCallback.getRetainedFragment().setResumeOk(true);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
