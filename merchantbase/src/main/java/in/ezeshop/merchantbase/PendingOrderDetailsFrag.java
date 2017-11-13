package in.ezeshop.merchantbase;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.backendless.exceptions.BackendlessException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.GenericListDialog;
import in.ezeshop.appbase.ImageViewActivity;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.CustomerOrder;
import in.ezeshop.common.database.Prescriptions;
import in.ezeshop.common.database.Transaction;
import in.ezeshop.merchantbase.helper.MyRetainedFragment;

/**
 * Created by adgangwa on 02-11-2017.
 */

public class PendingOrderDetailsFrag extends BaseFragment
        implements GenericListDialog.GenericListDialogIf, OrderStatusChangeDialog.OrderStatusChangeDialogIf {
    private static final String TAG = "MchntApp-PendingOrderDetailsFrag";

    private PendingOrderDetailsFrag.PendingOrderDetailsFragIf mCallback;
    //private MyRetainedFragment mRetainedFragment;
    private SimpleDateFormat mSdfDateWithTime;

    private static final int REQ_NOTIFY_ERROR = 4;
    private static final int REQ_NOTIFY_ERROR_EXIT = 5;

    private static final String DIALOG_CALL_NUMBER = "DialogCallNumber";
    private static final String ARG_ORDER_ID = "argOrderId";

    // Special member variable to identify backstack cases
    private Integer mBackstackFlag;
    private File[] mPrescripImgs = new File[CommonConstants.MAX_PRESCRIPS_PER_ORDER];
    private ArrayList<String> mCallNumbers = new ArrayList<>(10);
    private ArrayList<String> mCallNumDisplay = new ArrayList<>(10);

    private String mOrderId;
    private String mOrderCurrStatus;

    // Part of instance state: to be restored in event of fragment recreation
    private String mCancelReason;

    // Container Activity must implement this interface
    public interface PendingOrderDetailsFragIf {
        MyRetainedFragment getRetainedFragment();
        void setToolbarForFrag(int iconResId, String title, String subTitle);
        void showCustomerDetails(String custMobile);
        void cancelOrder(String orderId, String cancelReason);
        void acceptOrder(String orderId);
    }

    public static PendingOrderDetailsFrag getInstance(String orderId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDER_ID, orderId);

        PendingOrderDetailsFrag fragment = new PendingOrderDetailsFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_order_detail_mchntapp, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mCallback = (PendingOrderDetailsFragIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement PendingOrderDetailsFragIf");
        }

        //mRetainedFragment = mCallback.getRetainedFragment();
        mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.MY_LOCALE);

        mPrescripImgArr = new ImageView[]{mImgPrescrip1,mImgPrescrip2,mImgPrescrip3,mImgPrescrip4};

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
            if(savedInstanceState==null) {
                // Either fragment 'create' or 'backstack' case
                if (mBackstackFlag==null) {
                    // fragment create case - initialize member variables
                    //mPrescripsDisabled = false;
                    mBackstackFlag = 123; // dummy memory allocation - to check for backstack scenarios later
                } else {
                    // backstack case - no need to initialize member variables
                    // as the same are automatically stored and restored
                    // so - do nothing
                }
            } else {
                // fragment recreate case - restore member variables
                mCancelReason = savedInstanceState.getString("mCancelReason");
            }

            //setup all listeners
            initListeners();
            // Update view - only to be done only after values are restored above
            initDisplay(mCallback.getRetainedFragment().mPendingCustOrders.get(getArguments().getString(ARG_ORDER_ID)));

        } catch (Exception e) {
            LogMy.e(TAG, "Exception in onActivityCreated", e);
            DialogFragmentWrapper dialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            dialog.setTargetFragment(this, REQ_NOTIFY_ERROR_EXIT);
            dialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            throw e;
        }
    }

    private void initDisplay(Transaction txn) {
        //CustomerOrder order = mRetainedFragment.mSelCustOrder;
        if(txn==null) {
            LogMy.wtf(TAG,"initDisplay: Order object is null");
            throw new BackendlessException(String.valueOf(ErrorCodes.GENERAL_ERROR), "Order object is null");
        }

        mOrderId = txn.getTrans_id();
        mOrderCurrStatus = txn.getCustOrder().getCurrStatus();

        // Customer details
        mInputCustName.setText(txn.getCustOrder().getCustName());
        mInputCustMobile.setText(txn.getCust_mobile());
        mInputAddress.setText(CommonUtils.getCustAddrStrWithName(txn.getCustOrder()));

        // Order details
        mInputOrderId.setText(txn.getTrans_id());
        refreshPrescripImgs(txn.getCustOrder());
        if(txn.getCustOrder().getCustComments()==null || txn.getCustOrder().getCustComments().isEmpty()) {
            mInputCommentsInfo.setVisibility(View.GONE);
            mInputComments.setVisibility(View.GONE);
        } else {
            mInputCommentsInfo.setVisibility(View.VISIBLE);
            mInputComments.setVisibility(View.VISIBLE);
            mInputComments.setText(txn.getCustOrder().getCustComments());
        }

        // Order Status details
        refOrderStatusDetails(txn.getCustOrder());
        // Billing details
        refreshBillingDetails(txn);

        // Call numbers
        prepareCallNumbers(txn);
    }

    private void prepareCallNumbers(Transaction txn) {
        mCallNumbers.clear();
        mCallNumDisplay.clear();

        mCallNumbers.add(txn.getCust_mobile());
        mCallNumDisplay.add("Account Number ("+txn.getCustOrder().getCustName()+") : "
                +AppConstants.PHONE_COUNTRY_CODE_DISPLAY+txn.getCust_mobile());

        if(txn.getCustOrder().getDelvryContactNum()!=null &&
                !txn.getCustOrder().getDelvryContactNum().equals(txn.getCust_mobile())) {
            mCallNumbers.add(txn.getCustOrder().getDelvryContactNum());
            mCallNumDisplay.add("Delivery Address Number ("+txn.getCustOrder().getDelvryToName()+") : "
                    +AppConstants.PHONE_COUNTRY_CODE_DISPLAY+txn.getCustOrder().getDelvryContactNum());
        }

        // todo: Add Delivery agent number
    }

    private void refreshBillingDetails(Transaction txn) {
        mLytBilling.setVisibility(View.GONE);
    }

    private void refOrderStatusDetails(CustomerOrder order) {

        if(order.getCreateTime()!=null) {
            //mImgOrderStatusNew.setImageDrawable(AppCommonUtil.getTintedDrawable(this, , tintColor));
            mImgOrderStatusNew.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green_positive));
            mDivOrderStatusNew.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green_positive));
            mLabelStatusNew.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            mTimeStatusNew.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            mTimeStatusNew.setText(mSdfDateWithTime.format(order.getCreateTime()));
        } else {
            // I shouldn't be here
            LogMy.wtf(TAG,"refOrderStatusDetails: Order create time is null");
        }

        boolean isCancelled = order.getCurrStatus().equals(DbConstants.CUSTOMER_ORDER_STATUS.Cancelled.toString());

        // Init view for Accepted status
        mImgOrderStatusAcptd.setVisibility(View.VISIBLE);
        mDivOrderStatusAcptd.setVisibility(View.VISIBLE);
        mSpaceStatusAccptd.setVisibility(View.VISIBLE);
        mLytStatusAccptd.setVisibility(View.VISIBLE);

        if(order.getAcceptTime()!=null) {
            mImgOrderStatusAcptd.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green_positive));
            mDivOrderStatusAcptd.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green_positive));
            mLabelStatusAccptd.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            mTimeStatusAccptd.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            mTimeStatusAccptd.setText(mSdfDateWithTime.format(order.getAcceptTime()));

        } else {
            if(isCancelled) {
                mImgOrderStatusAcptd.setVisibility(View.GONE);
                mDivOrderStatusAcptd.setVisibility(View.GONE);
                mSpaceStatusAccptd.setVisibility(View.GONE);
                mLytStatusAccptd.setVisibility(View.GONE);

            } else {
                mImgOrderStatusAcptd.setColorFilter(ContextCompat.getColor(getActivity(), R.color.disabled));
                mDivOrderStatusAcptd.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.disabled));
                mLabelStatusAccptd.setTextColor(ContextCompat.getColor(getActivity(), R.color.disabled));
                mTimeStatusAccptd.setTextColor(ContextCompat.getColor(getActivity(), R.color.disabled));
                mTimeStatusAccptd.setText("");
            }
        }

        // Init view for Dispatched status
        mImgOrderStatusDsptchd.setVisibility(View.VISIBLE);
        mDivOrderStatusDsptchd.setVisibility(View.VISIBLE);
        mSpaceStatusDsptchd.setVisibility(View.VISIBLE);
        mLytStatusDsptchd.setVisibility(View.VISIBLE);

        if(order.getDispatchTime()!=null) {
            mImgOrderStatusDsptchd.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green_positive));
            mDivOrderStatusDsptchd.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green_positive));
            mLabelStatusDsptchd.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            mTimeStatusDsptchd.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            mTimeStatusDsptchd.setText(mSdfDateWithTime.format(order.getDispatchTime()));
        } else {
            if(isCancelled) {
                mImgOrderStatusDsptchd.setVisibility(View.GONE);
                mDivOrderStatusDsptchd.setVisibility(View.GONE);
                mSpaceStatusDsptchd.setVisibility(View.GONE);
                mLytStatusDsptchd.setVisibility(View.GONE);

            } else {
                mImgOrderStatusDsptchd.setColorFilter(ContextCompat.getColor(getActivity(), R.color.disabled));
                mDivOrderStatusDsptchd.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.disabled));
                mLabelStatusDsptchd.setTextColor(ContextCompat.getColor(getActivity(), R.color.disabled));
                mTimeStatusDsptchd.setTextColor(ContextCompat.getColor(getActivity(), R.color.disabled));
                mTimeStatusDsptchd.setText("");
            }
        }

        // Init view for Delivered status
        mImgOrderStatusDlvrd.setVisibility(View.VISIBLE);
        mLytStatusDsptchd.setVisibility(View.VISIBLE);

        if(order.getDeliverTime()!=null) {
            mImgOrderStatusDlvrd.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green_positive));
            mLabelStatusDlvrd.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            mTimeStatusDlvrd.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            mTimeStatusDlvrd.setText(mSdfDateWithTime.format(order.getDeliverTime()));
        } else {
            if(isCancelled) {
                mImgOrderStatusDlvrd.setVisibility(View.GONE);
                mLytStatusDsptchd.setVisibility(View.GONE);

            } else {
                mImgOrderStatusDlvrd.setColorFilter(ContextCompat.getColor(getActivity(), R.color.disabled));
                mLabelStatusDlvrd.setTextColor(ContextCompat.getColor(getActivity(), R.color.disabled));
                mTimeStatusDlvrd.setTextColor(ContextCompat.getColor(getActivity(), R.color.disabled));
                mTimeStatusDlvrd.setText("");
            }
        }

        if(isCancelled) {
            mImgOrderStatusCancel.setVisibility(View.VISIBLE);
            mLytStatusCancel.setVisibility(View.VISIBLE);

            mImgOrderStatusCancel.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green_positive));
            mLabelStatusCancel.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            mTimeStatusCancel.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
            mTimeStatusCancel.setText(mSdfDateWithTime.format(order.getDeliverTime()));
        } else {
            mImgOrderStatusCancel.setVisibility(View.GONE);
            mLytStatusCancel.setVisibility(View.GONE);
        }

    }

    private void refreshPrescripImgs(CustomerOrder order) {
        LogMy.d(TAG, "In refreshPrescripImgs");

        if(order.getPrescrips()==null || order.getPrescrips().size()==0) {
            mInputPrescripInfo.setText("* No prescriptions are available");
            mLytPrescripImgs.setVisibility(View.GONE);
        } else {
            mInputPrescripInfo.setText("Prescriptions");
            mLytPrescripImgs.setVisibility(View.VISIBLE);

            // show prescription image previews

            // Make all invisible first
            int cnt = order.getPrescrips().size();
            for(int i=0; i<CommonConstants.MAX_PRESCRIPS_PER_ORDER; i++) {
                if(i<cnt) {
                    mPrescripImgArr[i].setVisibility(View.VISIBLE);
                } else {
                    mPrescripImgArr[i].setVisibility(View.GONE);
                }
            }

            // Add backendless headers to the request
            /*OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {

                    Request.Builder builder = chain.request().newBuilder();
                    for( String key : HeadersManager.getInstance().getHeaders().keySet() ) {
                        builder.addHeader(key, HeadersManager.getInstance().getHeaders().get(key));
                    }
                    return chain.proceed(builder.build());
                }
            });

            // Make picasso builder object with above HttpClient
            Picasso.Builder builder = new Picasso.Builder(getActivity().getApplicationContext());
            builder.downloader(new OkHttpDownloader(okHttpClient));
            // add listener to picasso builder
            builder.listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    LogMy.e(TAG,"Picasso image load failed: "+uri,exception);
                    //imgView.setVisibility(View.GONE);
                    AppCommonUtil.toast(getActivity(),"Failed to upload image");
                }}
            );*/

            int indx = 0;
            for (Prescriptions item :
                    order.getPrescrips()) {
                String filename = Uri.parse(item.getUrl()).getLastPathSegment();
                File file = getActivity().getFileStreamPath(filename);
                if(file == null || !file.exists()) {
                    // I shouldn't be here
                    LogMy.wtf(TAG,"refreshPrescripImgs: Prescription image not available locally: "+item.getUrl());
                } else {
                    mPrescripImgArr[indx].setImageURI(Uri.fromFile(file));
                    mPrescripImgArr[indx].setScaleType(ImageView.ScaleType.CENTER_CROP);
                    mPrescripImgs[indx] = file;
                }
                    //final View imgView = mPrescripImgArr[indx];
                //imgView.setVisibility(View.VISIBLE);
                //LogMy.d(TAG,"Image file URL: "+item.getUrl());
                /*builder.build().load(item.getUrl()).fit().centerCrop()
                        .placeholder(R.drawable.ic_description_black_18dp)
                        .error(R.drawable.ic_clear_black_24dp)
                        .into(mPrescripImgArr[indx]);*/

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
                    File img = mPrescripImgs[getImgIndex(id)];
                    LogMy.d(TAG,"Image file: "+img.getAbsolutePath()+","+img.getPath()+","+Uri.fromFile(img));

                    // Show appropriate uploaded prescription preview
                    Intent intent = new Intent(getActivity(), ImageViewActivity.class );
                    intent.putExtra(ImageViewActivity.INTENT_EXTRA_URI, Uri.fromFile(img));
                    startActivity(intent);

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
            if(mPrescripImgArr[i].getId()==id || mPrescripImgArr[i].getId()==id) {
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
            LogMy.d(TAG, "In handleDialogBtnClick: " + v.getId());

            int i = v.getId();
            if (i == mBtnCall.getId()) {
                if(mCallNumbers.size() > 1) {
                    DialogFragment dialog = GenericListDialog.getInstance(R.drawable.ic_call_black_18dp, mCallNumDisplay, "Select Number to Dial", true);
                    dialog.setTargetFragment(this, GenericListDialog.REQ_GENERIC_LIST);
                    dialog.show(getFragmentManager(), DIALOG_CALL_NUMBER);
                } else {
                    dialNumber(mCallNumbers.get(0));
                }

            } else if (i == mBtnChangeStatus.getId()) {
                DialogFragment dialog = OrderStatusChangeDialog.newInstance(mOrderId, mOrderCurrStatus, true);
                dialog.setTargetFragment(this, OrderStatusChangeDialog.REQ_ORDER_STATUS_CHG);
                dialog.show(getFragmentManager(), OrderStatusChangeDialog.DIALOG_ORDER_STATUS_CHG);

            } else if(i == mBtnCustDetails.getId()) {
                mCallback.showCustomerDetails(mInputCustMobile.getText().toString());
            }

        } catch (Exception e) {
            LogMy.e(TAG, "Exception in handleDialogBtnClick", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    @Override
    public void cancelOrder(String orderId, String reason) {
        if(orderId.equals(mOrderId)) {
            mCancelReason = reason;
            // ask for confirmation
            String msg = "Are you sure to Cancel Order with ID# "+orderId;
            DialogFragment dialog = DialogFragmentWrapper.createConfirmationDialog("Confirm Cancellation", msg, true, false);
            dialog.setTargetFragment(this, DialogFragmentWrapper.REQUEST_DIALOG_CONFIRMATION);
            dialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_CONFIRMATION);
        } else {
            LogMy.wtf(TAG,"cancelOrder: The order id is not same: "+orderId+", "+mOrderId);
        }
    }

    @Override
    public void acceptOrder(String orderId) {
        if(orderId.equals(mOrderId)) {
            mCallback.acceptOrder(mOrderId);
        } else {
            LogMy.wtf(TAG,"acceptOrder: The order id is not same: "+orderId+", "+mOrderId);
        }
    }

    private void dialNumber(String num) {
        String callNum;
        if(num.startsWith(AppConstants.PHONE_COUNTRY_CODE)) {
            if(num.startsWith(AppConstants.PHONE_COUNTRY_CODE_DISPLAY)) {
                callNum = num.replace("-","");
            } else {
                callNum = num;
            }
        } else {
            callNum = AppConstants.PHONE_COUNTRY_CODE+num;
        }

        LogMy.d(TAG,"dialNumber: "+num+", "+callNum);
        Intent i = new Intent(Intent.ACTION_DIAL);
        String p = "tel:" + callNum;
        i.setData(Uri.parse(p));
        startActivity(i);
    }

    @Override
    public void onListItemSelected(int index, String text) {
        LogMy.d(TAG, "In onListItemSelected :" + index + ", " + text);
        dialNumber(mCallNumbers.get(index));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogMy.d(TAG, "In onActivityResult :" + requestCode + ", " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!= Activity.RESULT_OK) {
            return;
        }

        try {
            switch (requestCode) {
                case DialogFragmentWrapper.REQUEST_DIALOG_CONFIRMATION:
                    // only cancellation confirmation is requested from this fragment
                    mCallback.cancelOrder(mOrderId, mCancelReason);
                    break;
                case REQ_NOTIFY_ERROR:
                    // do nothing
                    break;
                case REQ_NOTIFY_ERROR_EXIT:
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

        // Avoid double clicks on the button
        mBtnCustDetails.setOnClickListener(this);
        mBtnCall.setOnClickListener(this);
        mBtnChangeStatus.setOnClickListener(this);
    }

    // UI Resources data members
    @BindView(R2.id.cardBtn_custDetails) TextView mBtnCustDetails;
    @BindView(R2.id.input_custName) TextView mInputCustName;
    @BindView(R2.id.input_custMobile) TextView mInputCustMobile;
    @BindView(R2.id.input_dlvrAddres) TextView mInputAddress;
    @BindView(R2.id.input_orderId) TextView mInputOrderId;

    // Order details members
    @BindView(R2.id.input_prescripInfo) TextView mInputPrescripInfo;
    @BindView(R2.id.layout_prescrip_imgs) View mLytPrescripImgs;
    @BindView(R2.id.img_precrips_1) ImageView mImgPrescrip1;
    @BindView(R2.id.img_precrips_2) ImageView mImgPrescrip2;
    @BindView(R2.id.img_precrips_3) ImageView mImgPrescrip3;
    @BindView(R2.id.img_precrips_4) ImageView mImgPrescrip4;
    @BindView(R2.id.input_commentsInfo1) TextView mInputCommentsInfo;
    @BindView(R2.id.input_comments) TextView mInputComments;

    // Order status details member
    @BindView(R2.id.img_orderStatusNew) ImageView mImgOrderStatusNew;
    @BindView(R2.id.div_orderStatusNew) View mDivOrderStatusNew;
    @BindView(R2.id.img_orderStatusAccptd) ImageView mImgOrderStatusAcptd;
    @BindView(R2.id.div_orderStatusAcptd) View mDivOrderStatusAcptd;
    @BindView(R2.id.img_orderStatusDsptchd) ImageView mImgOrderStatusDsptchd;
    @BindView(R2.id.div_orderStatusDsptchd) View mDivOrderStatusDsptchd;
    @BindView(R2.id.img_orderStatusDlvrd) ImageView mImgOrderStatusDlvrd;
    //@BindView(R2.id.div_orderStatusDlvrd) View mDivOrderStatusDlvrd;
    @BindView(R2.id.img_orderStatusCancel) ImageView mImgOrderStatusCancel;
    // status new
    @BindView(R2.id.label_orderStatusNew) TextView mLabelStatusNew;
    @BindView(R2.id.time_statusNew) TextView mTimeStatusNew;
    // status accepted
    @BindView(R2.id.space_orderStatusAccptd) View mSpaceStatusAccptd;
    @BindView(R2.id.lyt_orderStatusAccptd) View mLytStatusAccptd;
    @BindView(R2.id.label_orderStatusAccptd) TextView mLabelStatusAccptd;
    @BindView(R2.id.time_statusAccptd) TextView mTimeStatusAccptd;
    // status dispatched
    @BindView(R2.id.space_orderStatusDspchd) View mSpaceStatusDsptchd;
    @BindView(R2.id.lyt_orderStatusDspchd) View mLytStatusDsptchd;
    @BindView(R2.id.label_orderStatusDspchd) TextView mLabelStatusDsptchd;
    @BindView(R2.id.time_statusDsptchd) TextView mTimeStatusDsptchd;
    // status delivered
    @BindView(R2.id.space_orderStatusDlvrd) View mSpaceStatusDlvrd;
    @BindView(R2.id.lyt_orderStatusDlvrd) View mLytStatusDlvrd;
    @BindView(R2.id.label_orderStatusDlvrd) TextView mLabelStatusDlvrd;
    @BindView(R2.id.time_statusDlvrd) TextView mTimeStatusDlvrd;
    // status cancelled
    //@BindView(R2.id.space_orderStatusCancel) TextView mSpaceStatusCancel;
    @BindView(R2.id.lyt_orderStatusCancel) View mLytStatusCancel;
    @BindView(R2.id.label_orderStatusCancel) TextView mLabelStatusCancel;
    @BindView(R2.id.time_statusCancel) TextView mTimeStatusCancel;

    // Billing details members
    @BindView(R2.id.cardview_billing) View mLytBilling;

    @BindView(R2.id.btn_call) AppCompatButton mBtnCall;
    @BindView(R2.id.btn_changeStatus) AppCompatButton mBtnChangeStatus;

    ImageView mPrescripImgArr[];
    private Unbinder unbinder;

    @Override
    public void onResume() {
        super.onResume();
        mCallback.setToolbarForFrag(-1,"Create Order",null);

        try {

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
        outState.putString("mCancelReason", mCancelReason);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        // Clear any configuration that was done!
        //EasyImage.clearConfiguration(getActivity());
        super.onDestroy();
    }
}


