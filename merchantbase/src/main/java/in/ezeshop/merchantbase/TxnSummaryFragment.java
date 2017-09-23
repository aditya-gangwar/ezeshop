package in.ezeshop.merchantbase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.merchantbase.helper.MyRetainedFragment;

/**
 * Created by adgangwa on 08-06-2016.
 */
public class TxnSummaryFragment extends BaseFragment {
    private static final String TAG = "MchntApp-TxnSummaryFragment";

    private static final String ARG_SUMMARY = "summary";
    private static final String ARG_FRM_DATE = "fromDate";
    private static final String ARG_TO_DATE = "toDate";

    public interface TxnSummaryFragmentIf {
        MyRetainedFragment getRetainedFragment();
        void setToolbarTitle(String title);
        void showTxnDetails();
    }

    private TxnSummaryFragmentIf mCallback;

    public static TxnSummaryFragment newInstance(int[] summary, String fromDate, String toDate) {
        Bundle args = new Bundle();
        args.putIntArray(ARG_SUMMARY, summary);
        args.putString(ARG_FRM_DATE, fromDate);
        args.putString(ARG_TO_DATE, toDate);
        TxnSummaryFragment fragment = new TxnSummaryFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogMy.d(TAG, "In onActivityCreated");

        try {
            mCallback = (TxnSummaryFragmentIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement TxnSummaryFragmentIf");
        }
        mCallback.setToolbarTitle("Summary");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogMy.d(TAG, "In onCreateView");
        View v = inflater.inflate(R.layout.fragment_txn_report_summary, container, false);

        try {
            // access to UI elements
            bindUiResources(v);

            detailsButton.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    if(!mCallback.getRetainedFragment().getResumeOk())
                        return;

                    mCallback.showTxnDetails();
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

    private void updateUi() {
        String txt = getArguments().getString(ARG_FRM_DATE,"")+"   -   "+getArguments().getString(ARG_TO_DATE,"");
        dates.setText(txt);

        int[] summary = getArguments().getIntArray(ARG_SUMMARY);

        input_values[AppConstants.INDEX_TXN_COUNT].setText(String.valueOf(summary[AppConstants.INDEX_TXN_COUNT]));
        input_values[AppConstants.INDEX_OVERDRAFT_TXN_COUNT].setText(String.valueOf(summary[AppConstants.INDEX_OVERDRAFT_TXN_COUNT]));

        input_values[AppConstants.INDEX_BILL_AMOUNT].setText(AppCommonUtil.getSignedAmtStr(summary[AppConstants.INDEX_BILL_AMOUNT]));
        input_values[AppConstants.INDEX_CASHBACK].setText(AppCommonUtil.getSignedAmtStr(summary[AppConstants.INDEX_CASHBACK]));
        input_values[AppConstants.INDEX_ADD_ACCOUNT].setText(AppCommonUtil.getSignedAmtStr(summary[AppConstants.INDEX_ADD_ACCOUNT]));
        input_values[AppConstants.INDEX_DEBIT_ACCOUNT].setText(AppCommonUtil.getNegativeAmtStr(summary[AppConstants.INDEX_DEBIT_ACCOUNT]));
        if(summary[AppConstants.INDEX_OVERDRAFT] > 0) {
            layoutOverdraft.setVisibility(View.VISIBLE);
            input_values[AppConstants.INDEX_OVERDRAFT].setText(AppCommonUtil.getNegativeAmtStr(summary[AppConstants.INDEX_OVERDRAFT]));
        } else {
            layoutOverdraft.setVisibility(View.GONE);
        }
    }

    EditText input_values[] = new EditText[AppConstants.INDEX_SUMMARY_MAX_VALUE];
    EditText dates;
    AppCompatButton detailsButton;
    //View layoutAcc;
    View layoutOverdraft;

    protected void bindUiResources(View view) {
        dates = (EditText) view.findViewById(R.id.txnlist_filter_duration);
        input_values[AppConstants.INDEX_TXN_COUNT] = (EditText) view.findViewById(R.id.input_trans_count);
        input_values[AppConstants.INDEX_OVERDRAFT_TXN_COUNT] = (EditText) view.findViewById(R.id.input_overdraft_count);
        input_values[AppConstants.INDEX_BILL_AMOUNT] = (EditText) view.findViewById(R.id.input_trans_bill_amt);
        input_values[AppConstants.INDEX_CASHBACK] = (EditText) view.findViewById(R.id.input_trans_add_cb);
        input_values[AppConstants.INDEX_ADD_ACCOUNT] = (EditText) view.findViewById(R.id.input_trans_add_account);
        input_values[AppConstants.INDEX_DEBIT_ACCOUNT] = (EditText) view.findViewById(R.id.input_trans_debit_account);
        input_values[AppConstants.INDEX_OVERDRAFT] = (EditText) view.findViewById(R.id.input_trans_overdraft);

        detailsButton = (AppCompatButton) view.findViewById(R.id.details_btn);
        //layoutAcc = view.findViewById(R.id.layout_account);
        layoutOverdraft = view.findViewById(R.id.layout_trans_overdraft);
    }

    @Override
    public void onResume() {
        LogMy.d(TAG, "In onResume");
        super.onResume();
        // update values
        try {
            updateUi();
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in TxnSummaryFragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            getActivity().onBackPressed();
        }
        mCallback.getRetainedFragment().setResumeOk(true);
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
}

