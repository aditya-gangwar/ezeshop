package in.ezeshop.customerbase;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import in.ezeshop.appbase.BaseDialog;
import in.ezeshop.appbase.entities.MyCashback;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.OnSingleClickListener;

/**
 * Created by adgangwa on 21-10-2017.
 */

public class OrderSortMchntDialog extends BaseDialog {
    public static final String TAG = "CustApp-OrderSortMchntDialog";

    public static final String ARG_SELECTED = "argSelected";
    public static final String EXTRA_SELECTION = "extraSelected";

    public static OrderSortMchntDialog newInstance(int selectedSortType) {
        LogMy.d(TAG, "Creating new OrderSortMchntDialog instance: "+selectedSortType);
        Bundle args = new Bundle();
        args.putInt(ARG_SELECTED, selectedSortType);

        OrderSortMchntDialog fragment = new OrderSortMchntDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set selection
        int selected = getArguments().getInt(ARG_SELECTED);
        LogMy.d(TAG,"Setting selection to "+selected);
        switch (selected) {
            case MyCashback.CB_CMP_TYPE_MCHNT_NAME:
                mSortCustRadioGroup.check(mMchntName.getId());
                break;
            case MyCashback.CB_CMP_TYPE_CB_RATE:
                mSortCustRadioGroup.check(mCbRate.getId());
                break;
            case MyCashback.CB_CMP_TYPE_ACC_BALANCE:
                mSortCustRadioGroup.check(mBalanceAcc.getId());
                break;
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LogMy.d(TAG, "In onCreateDialog");

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_order_sort_mchnt, null);
        initUiResources(v);

        // return new dialog
        final AlertDialog alertDialog =  new AlertDialog.Builder(getActivity()).setView(v)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AppCommonUtil.setDialogTextSize(OrderSortMchntDialog.this, (AlertDialog) dialog);

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {

                        int selectedId = mSortCustRadioGroup.getCheckedRadioButtonId();
                        int selectedSortType = MyCashback.CB_CMP_TYPE_ACC_BALANCE;

                        if (selectedId == R.id.cbRate) {
                            selectedSortType = MyCashback.CB_CMP_TYPE_CB_RATE;

                        } else if (selectedId == R.id.mchntName) {
                            selectedSortType = MyCashback.CB_CMP_TYPE_MCHNT_NAME;

                        } else if (selectedId == R.id.balanceAcc) {
                            selectedSortType = MyCashback.CB_CMP_TYPE_ACC_BALANCE;

                        }

                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_SELECTION,selectedSortType);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);

                        getDialog().dismiss();
                    }
                });
            }
        });

        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    @Override
    public void handleBtnClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                //Do nothing here because we override this button in OnShowListener to change the close behaviour.
                //However, we still need this because on older versions of Android unless we
                //pass a handler the button doesn't get instantiated
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                break;
        }
    }

    @Override
    public boolean handleTouchUp(View v) {
        return false;
    }

    /*@Override
    public void onClick(DialogInterface dialog, int which) {
        //Do nothing here because we override this button in OnShowListener to change the close behaviour.
        //However, we still need this because on older versions of Android unless we
        //pass a handler the button doesn't get instantiated
    }*/

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    private RadioGroup mSortCustRadioGroup;
    private RadioButton mMchntName;
    private RadioButton mCbRate;
    private RadioButton mBalanceAcc;

    private void initUiResources(View v) {
        mSortCustRadioGroup = (RadioGroup) v.findViewById(R.id.custSortRadioGroup);
        mMchntName = (RadioButton) v.findViewById(R.id.mchntName);
        mCbRate = (RadioButton) v.findViewById(R.id.cbRate);
        mBalanceAcc = (RadioButton) v.findViewById(R.id.balanceAcc);
    }
}

