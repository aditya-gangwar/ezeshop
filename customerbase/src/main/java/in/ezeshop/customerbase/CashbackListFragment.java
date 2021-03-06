package in.ezeshop.customerbase;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.appbase.entities.MyCashback;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.database.Merchants;
import in.ezeshop.customerbase.helper.MyRetainedFragment;

/**
 * Created by adgangwa on 09-09-2016.
 */
public class CashbackListFragment extends BaseFragment {
    private static final String TAG = "CustApp-CashbackListFragment";

    private static final String DIALOG_MERCHANT_DETAILS = "dialogMerchantDetails";
    private static final String DIALOG_SORT_CUST_TYPES = "dialogSortCust";

    private static final int REQ_NOTIFY_ERROR = 1;
    private static final int REQ_SORT_CUST_TYPES = 2;

    private SimpleDateFormat mSdfDateWithTime;
    private MyRetainedFragment mRetainedFragment;
    private CashbackListFragmentIf mCallback;

    public interface CashbackListFragmentIf {
        MyRetainedFragment getRetainedFragment();
        boolean refreshMchntList();
        void showMchntDetails(MyCashback data);
        void setDrawerState(boolean isEnabled);
    }

    private RecyclerView mRecyclerView;
    private EditText mTbSubhead1Text1;
    //private EditText mUpdated;
    //private EditText mUpdatedDetail;
    private List<MyCashback> mMyCbs;

    // instance state - store and restore
    private int mSelectedSortType;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogMy.d(TAG, "In onActivityCreated");

        try {
            mCallback = (CashbackListFragmentIf) getActivity();
            mRetainedFragment = mCallback.getRetainedFragment();

            mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.MY_LOCALE);

            // As the data is in Map - which cant be sorted
            // so create a local list from Map in retained fragment
            // note - this is not copy, as both list and Map will point to same MyCashback objects
            mMyCbs = new ArrayList<>(mRetainedFragment.mCashbacks.values());

            int sortType = MyCashback.CB_CMP_TYPE_UPDATE_TIME;
            if(savedInstanceState!=null) {
                sortType = savedInstanceState.getInt("mSelectedSortType");
            }
            sortList(sortType);

            // update time
            //mUpdated.setText(mSdfDateWithTime.format(mRetainedFragment.mCbsUpdateTime));

            setHasOptionsMenu(true);

        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement CashbackListFragmentIf");
        } catch(Exception e) {
            LogMy.e(TAG, "Exception is CashbackListFragment:onActivityCreated", e);
            // unexpected exception - show error
            DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            notDialog.setTargetFragment(this,REQ_NOTIFY_ERROR);
            notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            getActivity().onBackPressed();
        }
    }

    private void sortList(int sortType) {
        if(mMyCbs!=null) {
            Collections.sort(mMyCbs, new MyCashback.MyCashbackComparator(sortType));

            if (sortType != MyCashback.CB_CMP_TYPE_MCHNT_NAME) {
                // Make it in decreasing order - if not string comparison
                Collections.reverse(mMyCbs);
            }
            // store existing sortType
            mSelectedSortType = sortType;
        } else {
            LogMy.e(TAG,"In sortList: mCashbacks is null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merchant_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.cust_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTbSubhead1Text1 = (EditText) view.findViewById(R.id.tb_curr_account) ;

        //mUpdated = (EditText) view.findViewById(R.id.input_updated_time);
        //mUpdatedDetail = (EditText) view.findViewById(R.id.updated_time_details);

        return view;
    }

    private void updateUI() {
        if(mMyCbs!=null) {

            AppCommonUtil.showAmtColor(getActivity(),null,mTbSubhead1Text1,mRetainedFragment.stats.getClBalance(),false);

            mRecyclerView.setAdapter(new CbAdapter(mMyCbs));

            /*CbAdapter adapter = (CbAdapter) mRecyclerView.getAdapter();
            if(adapter==null) {
                LogMy.d(TAG, "Adaptor not set yet");
                mRecyclerView.setAdapter(new CbAdapter(mMyCbs));
            } else {
                adapter.refresh(mMyCbs);
            }*/

            // update time
            //mUpdated.setText(mSdfDateWithTime.format(mRetainedFragment.mCbsUpdateTime));

            /*mRecyclerView.removeAllViews();
            mRecyclerView.setAdapter(new CbAdapter(mMyCbs));
            mRecyclerView.invalidate();
            mRecyclerView.getAdapter().notifyDataSetChanged();*/
        } else {
            LogMy.e(TAG,"In updateUI: mCashbacks is null");
        }
    }

    public void refreshData() {
        if(mRetainedFragment.mCashbacks!=null) {
            mMyCbs = new ArrayList<>(mRetainedFragment.mCashbacks.values());
            for(MyCashback cb:mMyCbs) {
                LogMy.d(TAG,"In refreshData: "+cb.getMerchantId()+", "+cb.getClCredit());
            }
            sortList(mSelectedSortType);
            updateUI();
        } else {
            LogMy.e(TAG,"In refreshData: mCashbacks is null");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogMy.d(TAG, "In onActivityResult :" + requestCode + ", " + resultCode);
        if(resultCode != Activity.RESULT_OK) {
            return;
        }
        try {
            if (requestCode == REQ_SORT_CUST_TYPES) {
                int sortType = data.getIntExtra(SortMchntDialog.EXTRA_SELECTION, MyCashback.CB_CMP_TYPE_UPDATE_TIME);
                sortList(sortType);
                updateUI();
            }
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCallback.setDrawerState(true);
        try {
            updateUI();
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            throw e;
            //getActivity().onBackPressed();
        }
        mCallback.getRetainedFragment().setResumeOk(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mSelectedSortType", mSelectedSortType);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.merchant_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!mCallback.getRetainedFragment().getResumeOk())
            return true;

        try {
            int i = item.getItemId();
            if (i == R.id.action_sort) {
                sortMerchantList();

            } else if(i == R.id.action_refresh) {
                mCallback.refreshMchntList();
                /*if(!mCallback.refreshMchntList()) {
                    String msg = null;
                    if(MyGlobalSettings.getCustNoRefreshMins()==(24*60)) {
                        // 24 is treated as special case as 'once in a day'
                        msg = "Refresh is allowed once a day.";
                    } else {
                        //msg = "Refresh allowed once every "+String.valueOf(MyGlobalSettings.getCustNoRefreshMins())+" minutes";
                        int hours = Math.round(MyGlobalSettings.getMchntDashBNoRefreshMins()/60);
                        msg = "Refresh allowed once every "+hours+" hours.";
                    }

                    msg = msg+"\n\n"+"* Last Updated: "+mSdfDateWithTime.format(mRetainedFragment.mCbsUpdateTime);

                    DialogFragmentWrapper dialog = DialogFragmentWrapper.createNotification(AppConstants.generalInfoTitle, msg, true, false);
                    dialog.setTargetFragment(this, REQ_NOTIFY_ERROR);
                    dialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_CONFIRMATION);
                }*/
            }

        } catch(Exception e) {
            LogMy.e(TAG, "Exception is CashbackListFragment:onOptionsItemSelected", e);
            // unexpected exception - show error
            DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            notDialog.setTargetFragment(this,REQ_NOTIFY_ERROR);
            notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortMerchantList() {
        try {
            // loop and check if there's any txn with acc credit/debit
            boolean accFigures = false;
            for (MyCashback cb : mMyCbs) {
                if(cb.getClCredit()!=0 || cb.getCurrClDebit()!=0) {
                    accFigures = true;
                }
            }

            SortMchntDialog dialog = SortMchntDialog.newInstance(mSelectedSortType, accFigures);
            dialog.setTargetFragment(this, REQ_SORT_CUST_TYPES);
            dialog.show(getFragmentManager(), DIALOG_SORT_CUST_TYPES);

        } catch(Exception e) {
            LogMy.e(TAG, "Exception in sortMerchantList", e);
            // unexpected exception - show error
            DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            notDialog.setTargetFragment(this,REQ_NOTIFY_ERROR);
            notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    @Override
    public boolean handleTouchUp(View v) {
        // do nothing
        return false;
    }

    @Override
    public void handleBtnClick(View v) {
        // do nothing
    }

    private class CbHolder extends RecyclerView.ViewHolder {

        private MyCashback mCb;

        //private View mCardView;
        //private View mLayoutMchntItem;
        private ImageView mMerchantDp;
        private TextView mMerchantName;
        private View mMchntStatusAlert;
        private TextView mAreaNdCity;
        private TextView mLastTxnTime;
        private TextView mAccBalance;
        private ImageView mAccImage;
        //private EditText mCbBalance;
        //private View mLayoutAcc;
        //public View mLayoutRoot;

        public CbHolder(View itemView) {
            super(itemView);

            //mCardView = itemView.findViewById(R.id.card_view);
            //mLayoutMchntItem = itemView.findViewById(R.id.layout_mchnt_item);
            mMerchantDp = (ImageView) itemView.findViewById(R.id.img_merchant);
            mMerchantName = (TextView) itemView.findViewById(R.id.input_mchnt_name);
            mMchntStatusAlert = itemView.findViewById(R.id.icon_mchnt_status_alert);
            mAreaNdCity = (TextView) itemView.findViewById(R.id.mchnt_area_city);
            mLastTxnTime = (TextView) itemView.findViewById(R.id.input_last_txn);
            mAccBalance = (TextView) itemView.findViewById(R.id.input_acc_bal);
            mAccImage = (ImageView)  itemView.findViewById(R.id.icon_account);
            //mCbBalance = (EditText) itemView.findViewById(R.id.input_cb_bal);
            //mLayoutAcc = itemView.findViewById(R.id.layout_acc);

            //mLayoutRoot = itemView.findViewById(R.id.layout_root);

            /*mCardView.setOnTouchListener(this);
            mLayoutMchntItem.setOnTouchListener(this);
            mMerchantDp.setOnTouchListener(this);
            mMerchantName.setOnTouchListener(this);
            mAreaNdCity.setOnTouchListener(this);
            mLastTxnTime.setOnTouchListener(this);
            mAccBalance.setOnTouchListener(this);*/
            //mCbBalance.setOnTouchListener(this);
        }

        /*@Override
        public boolean onTouch(View v, MotionEvent event) {
            try {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if(!mCallback.getRetainedFragment().getResumeOk())
                        return true;

                    LogMy.d(TAG, "In onTouch: " + v.getId());

                    // getRootView was not working, so manually finding root view
                    // depending upon views on which listener is set
                    View rootView = null;
                    if (v.getId() == mCardView.getId()) {
                        rootView = (View) v.getParent();
                        LogMy.d(TAG, "Clicked first level view " + rootView.getId());

                    } else if (v.getId() == mLayoutMchntItem.getId()) {
                        rootView = (View) v.getParent().getParent();
                        LogMy.d(TAG, "Clicked first-a level view " + rootView.getId());

                    } else if (v.getId() == mMerchantDp.getId()) {
                        rootView = (View) v.getParent().getParent().getParent();
                        LogMy.d(TAG, "Clicked 2nd level view " + rootView.getId());

                    } else if (v.getId() == mAreaNdCity.getId() || v.getId() == mLastTxnTime.getId()) {
                        rootView = (View) v.getParent().getParent().getParent().getParent().getParent();
                        LogMy.d(TAG, "Clicked 3rd level view " + rootView.getId());

                    } else {
                        rootView = (View) v.getParent().getParent().getParent().getParent().getParent().getParent();
                        LogMy.d(TAG, "Clicked 4th level view " + rootView.getId());
                    }

                    rootView.performClick();
                }
            } catch (Exception e) {
                LogMy.e(TAG, "Exception in Fragment: ", e);
                DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                        .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            }
            return true;
        }*/

        public void bindCb(MyCashback cb) {
            mCb = cb;
            Merchants merchant = mCb.getMerchant();

            Bitmap dp = AppCommonUtil.getLocalBitmap(getActivity(),
                    merchant.getDisplayImage(), getResources().getDimension(R.dimen.dp_item_image_width));
            if(dp!=null) {
                //mCb.setDpMerchant(dp);
                mMerchantDp.setImageBitmap(dp);
            }

            mMerchantName.setText(merchant.getName());
            if(merchant.getAdmin_status()== DbConstants.USER_STATUS_UNDER_CLOSURE) {
                mMchntStatusAlert.setVisibility(View.VISIBLE);
            } else {
                mMchntStatusAlert.setVisibility(View.GONE);
            }
            String txt = merchant.getAddress().getAreaNIDB().getAreaName()+", "+merchant.getAddress().getAreaNIDB().getCity().getCity();
            mAreaNdCity.setText(txt);
            //txt = "Last: "+mSdfDateWithTime.format(cb.getLastTxnTime());
            txt = mSdfDateWithTime.format(cb.getLastTxnTime());
            mLastTxnTime.setText(txt);

            AppCommonUtil.showAmtColor(getActivity(),null,mAccBalance,mCb.getCurrAccBalance(),false);
        }

    }

    private class CbAdapter extends RecyclerView.Adapter<CbHolder> {
        private List<MyCashback> mCbs;

        private int selected_position = -1;
        private View.OnClickListener mListener;
        //private View.OnTouchListener mTouchListener;

        public CbAdapter(List<MyCashback> cbs) {
            mCbs = cbs;
            mListener = new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    LogMy.d(TAG,"In onClickListener of merchant list item");

                    if(!mCallback.getRetainedFragment().getResumeOk())
                        return;

                    int pos = mRecyclerView.getChildAdapterPosition(v);

                    // Updating old as well as new positions
                    notifyItemChanged(selected_position);
                    selected_position = pos;
                    notifyItemChanged(selected_position);

                    if (pos >= 0 && pos < getItemCount()) {
                        // show detailed dialog
                        //MchntDetailsDialogCustApp dialog = MchntDetailsDialogCustApp.newInstance(mCbs.get(pos).getMerchantId(),true);
                        mCallback.showMchntDetails(mCbs.get(pos));
                        /*mRetainedFragment.mSelectCashback = mCbs.get(pos);
                        MchntDetailsDialogCustApp dialog = MchntDetailsDialogCustApp.newInstance(mRetainedFragment.mSelectCashback.isAccDataAvailable());
                        dialog.show(getFragmentManager(), DIALOG_MERCHANT_DETAILS);*/
                    } else {
                        LogMy.e(TAG,"Invalid position in onClickListener of customer list item: "+pos);
                    }
                }
            };

            /*mTouchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    LogMy.d(TAG,"In OnTouchListener of merchant list item");

                    if(!mCallback.getRetainedFragment().getResumeOk())
                        return true;

                    int pos = mRecyclerView.getChildAdapterPosition(v);

                    // Updating old as well as new positions
                    notifyItemChanged(selected_position);
                    selected_position = pos;
                    notifyItemChanged(selected_position);

                    if (pos >= 0 && pos < getItemCount()) {
                        // show detailed dialog
                        MchntDetailsDialogCustApp dialog = MchntDetailsDialogCustApp.newInstance(mCbs.get(pos).getMerchantId(),true);
                        dialog.show(getFragmentManager(), DIALOG_MERCHANT_DETAILS);
                    } else {
                        LogMy.e(TAG,"Invalid position in OnTouchListener of merchant list item: "+pos);
                    }

                    return true;
                }
            };*/
        }

        @Override
        public CbHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.merchant_itemview_2, parent, false);
            //view.setOnClickListener(mListener);
            return new CbHolder(view);
        }

        @Override
        public void onBindViewHolder(CbHolder holder, int position) {
            MyCashback cb = mCbs.get(position);

            /*if(selected_position == position){
                // Here I am just highlighting the background
                holder.itemView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.list_highlight));
            }else{
                holder.itemView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            }*/

            //holder.mLayoutRoot.setOnClickListener(mListener);
            holder.itemView.setOnClickListener(mListener);
            //holder.itemView.setOnTouchListener(mTouchListener);
            holder.bindCb(cb);
        }

        @Override
        public int getItemCount() {
            return mCbs.size();
        }

        /*public void refresh(List<MyCashback> cbs) {
            mCbs = cbs;
            notifyDataSetChanged();
        }*/
    }
}
