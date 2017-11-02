package in.ezeshop.customerbase;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import id.zelory.compressor.Compressor;
import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.ImageViewActivity;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.CustAddress;
import in.ezeshop.common.database.CustomerOrder;
import in.ezeshop.common.database.Merchants;
import in.ezeshop.customerbase.entities.CustomerUser;
import in.ezeshop.customerbase.helper.MyRetainedFragment;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by adgangwa on 02-10-2017.
 */

public class CreateOrderFragment extends BaseFragment implements
        EasyPermissions.PermissionCallbacks {
    private static final String TAG = "CustApp-CreateOrderFragment";

    private CreateOrderFragment.CreateOrderFragmentIf mCallback;
    private MyRetainedFragment mRetainedFragment;

    private static final int REQ_NOTIFY_ERROR = 4;
    private static final int REQ_NOTIFY_ERROR_EXIT = 5;
    private static final int RC_HANDLE_CAMERA_PERM = 10;

    // Special member variable to identify backstack cases
    private Integer mBackstackFlag;

    // Part of instance state: to be restored in event of fragment recreation
    private String mSelectedAreaId;
    private String mSelectedMchntName;
    private int mFreeDlvryMinAmt;

    // Container Activity must implement this interface
    public interface CreateOrderFragmentIf {
        MyRetainedFragment getRetainedFragment();
        void setToolbarForFrag(int iconResId, String title, String subTitle);
        void onChooseAddress();
        void onChooseMerchant(String areaId);
        void onOrderCreate(String mchntName, int freeDlvryMinAmt);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_order, container, false);
        unbinder = ButterKnife.bind(this, v);

        EasyImage.configuration(getActivity())
                .setImagesFolderName(CommonConstants.BRAND_NAME)
//                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
//                .setCopyPickedImagesToPublicGalleryAppFolder(true)
                .setAllowMultiplePickInGallery(true);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mCallback = (CreateOrderFragment.CreateOrderFragmentIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement CreateOrderFragmentIf");
        }

        mRetainedFragment = mCallback.getRetainedFragment();
        mPrescripImgLytArr = new View[]{mLytPrescripImg1,mLytPrescripImg2,mLytPrescripImg3,mLytPrescripImg4};
        mPrescripImgArr = new ImageView[]{mImgPrescrip1,mImgPrescrip2,mImgPrescrip3,mImgPrescrip4};
        mPrescripImgDelArr = new ImageView[]{mImgPrescripDel1,mImgPrescripDel2,mImgPrescripDel3,mImgPrescripDel4};

        /*
         * Instead of checking for 'savedInstanceState==null', checking
         * for any 'not saved member' value (here, mBackstackFlag)
         * The reason being, that for scenarios wherein fragment was stored in backstack and
         * has come to foreground again - like after pressing 'back' from 'txn confirm fragment'
         * then, the savedInstanceState will be NULL only.
         * In backstack cases, only view is destroyed, while the fragment is saved as it is
         * Thus not even onSaveInstance() gets called.
         *
         * mCashPaidHelper will be null - for both 'fragment create' and 'fragment re-create' scenarios
         * but not for 'backstack' scenarios
         */
        try {
            if(mRetainedFragment.mCustOrder==null) {
                mRetainedFragment.mCustOrder = new CustomerOrder();
            }

            if(savedInstanceState==null) {
                // Either fragment 'create' or 'backstack' case
                if (mBackstackFlag==null) {
                    // fragment create case - initialize member variables
                    //mPrescripsDisabled = false;
                    mBackstackFlag = 123; // dummy memory allocation - to check for backstack scenarios later
                    // On first create set 'default address' as selected address
                    mRetainedFragment.mCustOrder.setAddressId(CustomerUser.getInstance().getCustomer().getDefaultAddressId());;
                } else {
                    // backstack case - no need to initialize member variables
                    // as the same are automatically stored and restored
                    // so - do nothing
                }
            } else {
                // fragment recreate case - restore member variables
                mSelectedAreaId = savedInstanceState.getString("mSelectedAreaId");
                mSelectedMchntName = savedInstanceState.getString("mSelectedMchntName");
                mFreeDlvryMinAmt = savedInstanceState.getInt("mFreeDlvryMinAmt");
            }

            //setup all listeners
            initListeners();
            // Update view - only to be done only after values are restored above
            initDisplay();

        } catch (Exception e) {
            LogMy.e(TAG, "Exception in onActivityCreated", e);
            DialogFragmentWrapper dialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            dialog.setTargetFragment(this, REQ_NOTIFY_ERROR_EXIT);
            dialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            //throw e;
        }
    }

    private void initDisplay() {
        refreshPrescripImgs();
        mInputComments.clearFocus();
        mBtnAddPrescrip.requestFocus();
    }

    private void refreshMerchant() {
        LogMy.d(TAG,"In refreshMerchant");

        String selMchntId = mRetainedFragment.mCustOrder.getMerchantId();
        Merchants selMchnt = null;
        if(selMchntId!=null && !selMchntId.isEmpty() &&
                mSelectedAreaId!=null && !mSelectedAreaId.isEmpty()) {
            // select merchant object from the list
            List<Merchants> mchnts = mRetainedFragment.mAreaToMerchants.get(mSelectedAreaId);
            for (Merchants m : mchnts) {
                if(m.getAuto_id().equals(selMchntId)) {
                    selMchnt = m;
                }
            }
        }
        if(selMchnt!=null) {
            mBtnChangeMchnt.setText("CHANGE");
            mInputMchnt.setText(CommonUtils.getMchntAddressStrWithName(selMchnt));
            mSelectedMchntName = selMchnt.getName();
            mFreeDlvryMinAmt = selMchnt.getFreeDlvrMinAmt();
        } else {
            mBtnChangeMchnt.setText("SELECT");
            mInputMchnt.setText("");
            mSelectedMchntName = null;
            mFreeDlvryMinAmt = -1;
        }
    }

    private void refreshAddress() {
        LogMy.d(TAG,"In refreshAddress");

        // First time - set to default address
        List<CustAddress> allAddress = CustomerUser.getInstance().getAllAddress();

        String selAddress = mRetainedFragment.mCustOrder.getAddressId();
        CustAddress selAddr = null;
        if( !(selAddress==null || allAddress==null || allAddress.isEmpty() || selAddress.isEmpty()) ) {
            for (CustAddress addr : allAddress) {
                if(addr.getId().equals(selAddress)) {
                    selAddr = addr;
                }
            }
        }

        if(selAddr!=null) {
            mBtnChangeAddr.setText("CHANGE");
            mInputAddress.setText(CommonUtils.getCustAddrStrWithName(selAddr));
            mSelectedAreaId = selAddr.getAreaId();
        } else {
            mBtnChangeAddr.setText("SELECT");
            mInputAddress.setText("");
            mSelectedAreaId = "";
        }
    }

    private void refreshPrescripImgs() {
        LogMy.d(TAG,"In refreshPrescripImgs");

        if(mRetainedFragment.mPrescripImgs==null || mRetainedFragment.mPrescripImgs.isEmpty()) {
            mLytPrescripImgs.setVisibility(View.GONE);
        } else {
            mLytPrescripImgs.setVisibility(View.VISIBLE);
            // Make all invisible first
            for(int i=0; i<CommonConstants.MAX_PRESCRIPS_PER_ORDER; i++) {
                mPrescripImgLytArr[i].setVisibility(View.GONE);
                mPrescripImgArr[i].setImageResource(R.drawable.ic_description_black_18dp);
            }

            int indx = 0;
            for (File img :
                    mRetainedFragment.mPrescripImgs) {

                final View imgLyt =mPrescripImgLytArr[indx];
                imgLyt.setVisibility(View.VISIBLE);
                LogMy.d(TAG,"Image file: "+img.getAbsolutePath()+","+img.getPath()+","+Uri.fromFile(img));

                Picasso.Builder builder = new Picasso.Builder(getActivity().getApplicationContext());
                builder.listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        LogMy.e(TAG,"Picasso image load failed: "+uri,exception);
                        imgLyt.setVisibility(View.GONE);
                        AppCommonUtil.toast(getActivity(),"Failed to upload image");
                        // TODO: raise alarm
                    }}
                );
                builder.build().load(Uri.fromFile(img)).fit().centerCrop()
//                        .placeholder(R.drawable.ic_description_black_18dp)
//                        .error(R.drawable.ic_clear_black_24dp)
                        .into(mPrescripImgArr[indx]);
                indx++;
            }
        }
    }

    // Not using BaseFragment's onTouch
    // as we dont want 'double touch' check against these buttons
    @Override
    public boolean handleTouchUp(View v) {
        return false; // do nothing
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
                    File img = mRetainedFragment.mPrescripImgs.get(getImgIndex(id));
                    LogMy.d(TAG,"Image file: "+img.getAbsolutePath()+","+img.getPath()+","+Uri.fromFile(img));

                    // Show appropriate uploaded prescription preview
                    Intent intent = new Intent(getActivity(), ImageViewActivity.class );
                    intent.putExtra(ImageViewActivity.INTENT_EXTRA_URI, Uri.fromFile(img));
                    startActivity(intent);

                } else if (id == R.id.img_precrips_del_1 || id == R.id.img_precrips_del_2 || id == R.id.img_precrips_del_3 || id == R.id.img_precrips_del_4) {
                    // Delete clicked uploaded prescription
                    mRetainedFragment.mPrescripImgs.remove(getImgIndex(id));
                    refreshPrescripImgs();

                } /*else if (id == R.id.checkbox_noPrescrips || id == R.id.label_noPrescrips) {
                    // Toggle Prescription upload
                }*/ else {
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

    // Using BaseFragment's onClick method - to avoid double clicks
    @Override
    public void handleBtnClick(View v) {
        if(!mCallback.getRetainedFragment().getResumeOk())
            return;

        try {
            LogMy.d(TAG, "In handleBtnClick: " + v.getId());

            int i = v.getId();
            if (i == mBtnAddPrescrip.getId()) {
                handleImgUploadReq();

            } else if (i == mBtnChangeAddr.getId()) {
                // Show 'choose address' fragment
                mCallback.onChooseAddress();

            } else if (i == mBtnChangeMchnt.getId()) {
                // delivery address should be set
                if(mInputAddress.getText().toString().isEmpty() ||
                        mRetainedFragment.mCustOrder.getAddressId()==null) {
                    AppCommonUtil.toast(getActivity(), "Set Delivery Address");
                } else {
                    // show 'choose merchant' fragment
                    mCallback.onChooseMerchant(mSelectedAreaId);
                }

            } else if (i == mBtnConfirm.getId()) {
                // process order
                if(validate()) {
                    mRetainedFragment.mCustOrder.setCustComments(mInputComments.getText().toString());
                    mCallback.onOrderCreate(mSelectedMchntName, mFreeDlvryMinAmt);
                }
            }
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in handleBtnClick", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    private boolean validate() {
        boolean allok = true;

        // either of prescription or comments are required
        if(mInputComments.getText().toString().isEmpty()) {
            if(mRetainedFragment.mPrescripImgs.size()<1) {
                allok = false;
                DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle,
                        "Add either a medicine Prescription or enter item names in 'Comments' section", true, true)
                        .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            }
        }

        // both delivery address and merchant is mandatory
        if(mInputAddress.getText().toString().isEmpty() ||
                mRetainedFragment.mCustOrder.getAddressId()==null) {
            allok = false;
            mInputAddress.setError("Delivery address missing");
            AppCommonUtil.toast(getActivity(), "Delivery address missing");
        }

        if(mInputMchnt.getText().toString().isEmpty() ||
                mRetainedFragment.mCustOrder.getMerchantId()==null) {
            mInputMchnt.setError("Delivery address missing");
            if(allok) {
                allok = false;
                AppCommonUtil.toast(getActivity(), "Select Merchant for order");
            }
        }

        return  allok;
    }

    private void handleImgUploadReq() {
        // check for camera permission
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            // Already have permission, do the thing
            if(mRetainedFragment.mPrescripImgs==null || mRetainedFragment.mPrescripImgs.size()<4) {
                // show chooser (camera/gallery) to upload prescription
                EasyImage.openChooserWithGallery(this, "Select Image From ?", 0);
            } else {
                String str = "Max "+CommonConstants.MAX_PRESCRIPS_PER_ORDER+" prescriptions allowed";
                AppCommonUtil.toast(getActivity(), str);
            }

        } else {
            List<String> permList = new ArrayList<>(Arrays.asList(perms));
            if (EasyPermissions.somePermissionPermanentlyDenied(this, permList)) {
                // Permanently denied
                new AppSettingsDialog.Builder(this)
                        .setRationale("Camera permission is required to add prescriptions image !")
                        .build().show();
            } else {
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(this, "Need permission to take photo of the Prescription",
                        RC_HANDLE_CAMERA_PERM, perms);
            }
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
        // doing nothing - will anyways be tried again - if the user presses add button again
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
                    if(mRetainedFragment.mPrescripImgs==null) {
                        mRetainedFragment.mPrescripImgs = new ArrayList<>(CommonConstants.MAX_PRESCRIPS_PER_ORDER);
                    }
                    if(mRetainedFragment.mPrescripImgs.size() < CommonConstants.MAX_PRESCRIPS_PER_ORDER) {
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
                                    mRetainedFragment.mPrescripImgs.add(newFile);
                                    if (compressedImage.exists()) {
                                        getActivity().deleteFile(compressedImage.getName());
                                    }
                                } else {
                                    LogMy.wtf(TAG, "onImagesPicked: New file after rename does not exist");
                                    mRetainedFragment.mPrescripImgs.add(compressedImage);
                                }
                            } else {
                                mRetainedFragment.mPrescripImgs.add(compressedImage);
                            }

                        } catch(Exception e) {
                            LogMy.e(TAG,"Failed to compress image",e);
                            mRetainedFragment.mPrescripImgs.add(img);
                        }
                        newImgs++;
                    } else {
                        ignored++;
                    }
                }
                if(newImgs>0) {
                    refreshPrescripImgs();
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

                case REQ_NOTIFY_ERROR:
                    //mCallback.restartTxn();
                    // do nothing
                    break;

                case REQ_NOTIFY_ERROR_EXIT:
                    //getActivity().onBackPressed();
                    //mCallback.restartTxn();
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

        //mCbxNoPrescrips.setOnTouchListener(this);
        //mLabelNoPrescrips.setOnTouchListener(this);

        // Avoid double clicks on the button
        mBtnAddPrescrip.setOnClickListener(this);
        mBtnChangeAddr.setOnClickListener(this);
        mBtnChangeMchnt.setOnClickListener(this);
        mBtnConfirm.setOnClickListener(this);
    }

    // UI Resources data members
    @BindView(R2.id.btn_confirm_order) AppCompatButton mBtnConfirm;
    @BindView(R2.id.layout_prescrip_imgs) View mLytPrescripImgs;
    //@BindView(R2.id.input_prescripInfo) TextView mInputPrecripsInfo;

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

    @BindView(R2.id.input_comments) EditText mInputComments;
    //@BindView(R2.id.checkbox_noPrescrips) ImageView mCbxNoPrescrips;
    //@BindView(R2.id.label_noPrescrips) TextView mLabelNoPrescrips;

    //@BindViews({R2.id.layout_prescrip_imgs, R2.id.layout_addPrescrips })
    //List<View> allPrescripViews;

    @BindView(R2.id.cardBtn_dlvrAddres) TextView mBtnChangeAddr;
    @BindView(R2.id.input_dlvrAddres) TextView mInputAddress;

    @BindView(R2.id.cardBtn_orderMchnt) TextView mBtnChangeMchnt;
    @BindView(R2.id.input_orderMchnt) TextView mInputMchnt;

    View mPrescripImgLytArr[];
    ImageView mPrescripImgArr[];
    ImageView mPrescripImgDelArr[];

    private Unbinder unbinder;

    @Override
    public void onResume() {
        super.onResume();
        mCallback.setToolbarForFrag(-1,"Create Order",null);

        try {
            // intentionally calling it from here - as the source data can be modfied outside of this fragment too
            refreshAddress();
            refreshMerchant();
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
        outState.putString("mSelectedAreaId", mSelectedAreaId);
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

