package in.ezeshop.merchantbase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.List;

import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.MerchantOps;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.merchantbase.helper.MyRetainedFragment;

/**
 * Created by adgangwa on 18-09-2016.
 */
public class MerchantOpListFrag extends BaseFragment {
    private static final String TAG = "MchntApp-MerchantOpListFrag";

    private static final int REQ_NOTIFY_ERROR = 1;

    private SimpleDateFormat mSdfDateWithTime;

    private RecyclerView mRecyclerView;
    private MyRetainedFragment mRetainedFragment;
    private MerchantOpListFragIf mCallback;

    public interface MerchantOpListFragIf {
        MyRetainedFragment getRetainedFragment();
        void setDrawerState(boolean isEnabled);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogMy.d(TAG, "In onActivityCreated");

        try {
            mCallback = (MerchantOpListFragIf) getActivity();
            mRetainedFragment = mCallback.getRetainedFragment();

            mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.MY_LOCALE);

        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement MerchantOpListFragIf");
        } catch(Exception e) {
            LogMy.e(TAG, "Exception is MerchantOpListFrag:onActivityCreated", e);
            // unexpected exception - show error
            DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            notDialog.setTargetFragment(this,REQ_NOTIFY_ERROR);
            notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            getActivity().onBackPressed();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mchnt_op_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.mchntOp_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
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

    private void updateUI() {
        if(mRetainedFragment.mLastFetchMchntOps!=null) {
            mRecyclerView.setAdapter(new ListAdapter(mRetainedFragment.mLastFetchMchntOps));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogMy.d(TAG, "In onActivityResult :" + requestCode + ", " + resultCode);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mCallback.setDrawerState(false);
            updateUI();
            mCallback.getRetainedFragment().setResumeOk(true);

        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            mCallback.getRetainedFragment().setResumeOk(true);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            getActivity().onBackPressed();
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        private MerchantOps mOp;

        public EditText mOpName;
        public EditText mOpTime;
        public EditText mOpInitBy;
        public EditText mOpStatus;
        public EditText mOpParams;
        public EditText mOpTicketId;
        public EditText mOpReason;
        public EditText mOpRemarks;

        public ItemHolder(View itemView) {
            super(itemView);

            mOpName = (EditText) itemView.findViewById(R.id.input_opname);
            mOpTime = (EditText) itemView.findViewById(R.id.input_op_time);
            mOpInitBy = (EditText) itemView.findViewById(R.id.input_init_by);
            mOpStatus = (EditText) itemView.findViewById(R.id.input_status);
            mOpParams = (EditText) itemView.findViewById(R.id.input_params);
            mOpTicketId = (EditText) itemView.findViewById(R.id.input_ticketId);
            mOpReason = (EditText) itemView.findViewById(R.id.input_reason);
            mOpRemarks = (EditText) itemView.findViewById(R.id.input_remarks);
        }

        public void bindOp(MerchantOps op) {
            mOp = op;

            mOpName.setText(op.getOp_code());
            mOpTime.setText(mSdfDateWithTime.format(op.getCreated()));
            String initBy = op.getInitiatedBy();
            if(op.getInitiatedVia()!=null && !op.getInitiatedVia().isEmpty()) {
                initBy = initBy+" Via "+op.getInitiatedVia();
            }
            mOpInitBy.setText(initBy);

            // Optional parameters
            if(op.getExtra_op_params()!=null && !op.getExtra_op_params().isEmpty()) {
                mOpParams.setText(op.getExtra_op_params());
            } else {
                mOpParams.setVisibility(View.GONE);
            }

            // ticket num and remarks are to be shown to customer care user only
            if(!mCallback.getRetainedFragment().mMerchantUser.isPseudoLoggedIn()) {
                mOpTicketId.setVisibility(View.GONE);
                mOpRemarks.setVisibility(View.GONE);
                mOpReason.setVisibility(View.GONE);
                mOpStatus.setVisibility(View.GONE);
            } else {
                String statusStr = "Status: "+op.getOp_status();
                mOpStatus.setText(statusStr);

                if(op.getTicketNum()!=null && !op.getTicketNum().isEmpty()) {
                    String ticketStr = "Ticket ID: "+op.getTicketNum();
                    mOpTicketId.setText(ticketStr);
                } else {
                    mOpTicketId.setVisibility(View.GONE);
                }

                if(op.getRemarks()!=null && !op.getRemarks().isEmpty()) {
                    String str = "Remarks: "+op.getRemarks();
                    mOpRemarks.setText(str);
                } else {
                    mOpRemarks.setVisibility(View.GONE);
                }

                if(op.getReason()!=null && !op.getReason().isEmpty()) {
                    String str = "Reason: "+op.getReason();
                    mOpReason.setText(str);
                } else {
                    mOpReason.setVisibility(View.GONE);
                }
            }
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ItemHolder> {
        private List<MerchantOps> mOps;
        //private View.OnClickListener mListener;

        public ListAdapter(List<MerchantOps> ops) {
            mOps = ops;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.merchant_op_itemview, parent, false);
            //view.setOnClickListener(mListener);
            return new ItemHolder(view);
        }
        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            MerchantOps op = mOps.get(position);
            holder.bindOp(op);
        }
        @Override
        public int getItemCount() {
            return mOps.size();
        }
    }
}

