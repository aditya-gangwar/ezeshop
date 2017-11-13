package in.ezeshop.merchantbase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Transaction;
import in.ezeshop.merchantbase.helper.MyRetainedFragment;

/**
 * Created by adgangwa on 01-11-2017.
 */

public class PendingOrderListFrag extends BaseFragment {
    private static final String TAG = "MchntApp-PendingOrderListFrag";

    private static final int REQ_NOTIFY_ERROR = 1;

    private SimpleDateFormat mSdfDateWithTime;

    private RecyclerView mRecyclerNewOrders;
    private RecyclerView mRecyclerAccptdOrders;
    private RecyclerView mRecyclerDsptchdOrders;

    private TextView mInfoNoNewOrders;
    private TextView mInfoNoAccptdOrders;
    private TextView mInfoNoDsptchdOrders;

    private MyRetainedFragment mRetainedFragment;
    private PendingOrderListFrag.PendingOrderListFragIf mCallback;
    // instance state - store and restore
    //private int mSelectedSortType;

    public interface PendingOrderListFragIf {
        void setToolbarForFrag(int iconResId, String title, String subTitle);
        MyRetainedFragment getRetainedFragment();
        void showOrderDetailed(String orderId);
    }

    /*public static PendingOrderListFrag getInstance(Date startTime, Date endTime) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_START_TIME, startTime);
        args.putSerializable(ARG_END_TIME, endTime);

        PendingOrderListFrag fragment = new PendingOrderListFrag();
        fragment.setArguments(args);
        return fragment;
    }*/

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogMy.d(TAG, "In onActivityCreated");

        try {
            mCallback = (PendingOrderListFrag.PendingOrderListFragIf) getActivity();

            mRetainedFragment = mCallback.getRetainedFragment();
            mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.MY_LOCALE);
            //updateUI();

        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement PendingOrderListFragIf");

        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            getActivity().onBackPressed();
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_orders, container, false);

        mInfoNoNewOrders = (TextView) view.findViewById(R.id.no_newOrders);
        mRecyclerNewOrders = (RecyclerView) view.findViewById(R.id.recycler_newOrders);
        mRecyclerNewOrders.setLayoutManager(new LinearLayoutManager(getActivity()));

        mInfoNoAccptdOrders = (TextView) view.findViewById(R.id.no_acceptedOrders);
        mRecyclerAccptdOrders = (RecyclerView) view.findViewById(R.id.recycler_acceptedOrders);
        mRecyclerAccptdOrders.setLayoutManager(new LinearLayoutManager(getActivity()));

        mInfoNoDsptchdOrders = (TextView) view.findViewById(R.id.no_dispatchedOrders);
        mRecyclerDsptchdOrders = (RecyclerView) view.findViewById(R.id.recycler_dispatchedOrders);
        mRecyclerDsptchdOrders.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    private void updateUI() {
        mCallback.setToolbarForFrag(-1,"Pending Online Orders",null);

        List<Transaction> newOrders = null;
        List<Transaction> accptdOrders = null;
        List<Transaction> dsptchdOrders = null;

        for (Transaction txn :
                mRetainedFragment.mPendingCustOrders.values()) {
            if (txn.getCustOrder().getCurrStatus().equals(DbConstants.CUSTOMER_ORDER_STATUS.New.toString())) {
                if(newOrders==null) {
                    newOrders = new ArrayList<>(mRetainedFragment.mPendingCustOrders.size());
                }
                newOrders.add(txn);

            } else if (txn.getCustOrder().getCurrStatus().equals(DbConstants.CUSTOMER_ORDER_STATUS.Accepted.toString())) {
                if(accptdOrders==null) {
                    accptdOrders = new ArrayList<>(mRetainedFragment.mPendingCustOrders.size());
                }
                accptdOrders.add(txn);

            } else if (txn.getCustOrder().getCurrStatus().equals(DbConstants.CUSTOMER_ORDER_STATUS.Dispatched.toString())) {
                if(dsptchdOrders==null) {
                    dsptchdOrders = new ArrayList<>(mRetainedFragment.mPendingCustOrders.size());
                }
                dsptchdOrders.add(txn);
            }
        }

        if(newOrders==null) {
            mInfoNoNewOrders.setVisibility(View.VISIBLE);
            mRecyclerNewOrders.setVisibility(View.GONE);
        } else {
            mInfoNoNewOrders.setVisibility(View.GONE);
            mRecyclerNewOrders.setVisibility(View.VISIBLE);
            mRecyclerNewOrders.setAdapter(new CustOrderAdapter(newOrders));
        }
        if(accptdOrders==null) {
            mInfoNoAccptdOrders.setVisibility(View.VISIBLE);
            mRecyclerAccptdOrders.setVisibility(View.GONE);
        } else {
            mInfoNoAccptdOrders.setVisibility(View.GONE);
            mRecyclerAccptdOrders.setVisibility(View.VISIBLE);
            mRecyclerAccptdOrders.setAdapter(new CustOrderAdapter(accptdOrders));
        }
        if(dsptchdOrders==null) {
            mInfoNoDsptchdOrders.setVisibility(View.VISIBLE);
            mRecyclerDsptchdOrders.setVisibility(View.GONE);
        } else {
            mInfoNoDsptchdOrders.setVisibility(View.GONE);
            mRecyclerDsptchdOrders.setVisibility(View.VISIBLE);
            mRecyclerDsptchdOrders.setAdapter(new CustOrderAdapter(dsptchdOrders));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogMy.d(TAG, "In onActivityResult :" + requestCode + ", " + resultCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        try {
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
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

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        mCallback.getRetainedFragment().setResumeOk(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putInt("mSelectedSortType", mSelectedSortType);
    }

    private class OrderHolder extends RecyclerView.ViewHolder {

        private Transaction mOrder;

        private View mLytExpiring;
        private TextView mInputExpiring;
        private TextView mInputCustName;
        private TextView mInputBillAmt;
        private TextView mDatetime;
        private TextView mInputAddress;
        private View mLytAgent;
        private TextView mInputAgentName;

        public OrderHolder(View itemView) {
            super(itemView);

            mLytExpiring = itemView.findViewById(R.id.lyt_expiring);
            mInputExpiring = (TextView) itemView.findViewById(R.id.input_expiring);
            mInputCustName = (TextView) itemView.findViewById(R.id.input_custName);
            mInputBillAmt = (TextView) itemView.findViewById(R.id.input_billAmt);
            mDatetime = (TextView) itemView.findViewById(R.id.input_orderTime);
            mInputAddress = (TextView) itemView.findViewById(R.id.input_orderAddr);
            mLytAgent = itemView.findViewById(R.id.layout_agent);
            mInputAgentName = (TextView) itemView.findViewById(R.id.input_agentName);
        }

        public void bindTxn(Transaction txn) {
            mOrder = txn;

            // Check if about to expire
            long minsToExpire = CommonUtils.isOrderExpiring(txn.getCustOrder());
            if(minsToExpire > 0) {
                // show order expiry notification
                mLytExpiring.setVisibility(View.VISIBLE);
                mInputExpiring.setText( String.format(getActivity().getString(R.string.orderExpiryBanner), String.valueOf(minsToExpire)));
            } else {
                mLytExpiring.setVisibility(View.GONE);
            }

            mInputCustName.setText(txn.getCustOrder().getCustName());
            mDatetime.setText(mSdfDateWithTime.format(txn.getCreate_time()));
            mInputAddress.setText(CommonUtils.getDlvryAddrStrShort(txn.getCustOrder()) );

            AppCommonUtil.showAmtColor(getActivity(), null, mInputBillAmt, txn.getPaymentAmt(), false);
            /*if(txn.getTxn()==null) {
                mInputBillAmt.setVisibility(View.GONE);
            } else {
                mInputBillAmt.setVisibility(View.VISIBLE);
                AppCommonUtil.showAmtColor(getActivity(), null, mInputBillAmt, txn.getTxn().getPaymentAmt(), false);
            }*/
            //ToDo: Add agent details
            mLytAgent.setVisibility(View.GONE);
        }
    }

    private class CustOrderAdapter extends RecyclerView.Adapter<OrderHolder> {
        private List<Transaction> mCustOrders;
        private int selected_position = -1;
        private View.OnClickListener mListener;

        public CustOrderAdapter(List<Transaction> txns) {
            mCustOrders = txns;
            mListener = new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    if(!mCallback.getRetainedFragment().getResumeOk())
                        return;

                    LogMy.d(TAG,"In onSingleClick of txn list item");
                    int pos = mRecyclerNewOrders.getChildAdapterPosition(v);

                    // Updating old as well as new positions
                    notifyItemChanged(selected_position);
                    selected_position = pos;
                    notifyItemChanged(selected_position);

                    if (pos >= 0 && pos < getItemCount()) {
                        mCallback.showOrderDetailed(mCustOrders.get(pos).getTrans_id());
                    } else {
                        LogMy.e(TAG,"Invalid position in onClickListener of txn list item: "+pos);
                    }
                }
            };
        }

        @Override
        public OrderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.itemview_pending_order, parent, false);
            //LogMy.d(TAG,"Root view: "+view.getId());
            //view.setOnClickListener(mListener);
            return new OrderHolder(view);
        }
        @Override
        public void onBindViewHolder(OrderHolder holder, int position) {
            Transaction txn = mCustOrders.get(position);
            if(selected_position == position){
                // Here I am just highlighting the background
                holder.itemView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.list_highlight));
            }else{
                holder.itemView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            }

            holder.itemView.setOnClickListener(mListener);
            holder.bindTxn(txn);
        }
        @Override
        public int getItemCount() {
            return mCustOrders.size();
        }
    }
}

