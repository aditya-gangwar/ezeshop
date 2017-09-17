package in.ezeshop.merchantbase;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.ValidationHelper;

/**
 * Created by adgangwa on 30-04-2016.
 */
public class TxnPinInputDialog extends DialogFragment
        implements View.OnClickListener {

    private static final String TAG = "MchntApp-TxnPinInputDialog";
    private static final String ARG_CASH_CREDIT = "cashCredit";
    private static final String ARG_CASH_DEBIT = "cashDebit";
    //private static final String ARG_CASHBACK_DEBIT = "cashbackDebit";
    private static final String ARG_OVERDRAFT = "accOverdraft";
    //private static final String ARG_CANCEL_TXNID = "cancelTxnId";
    private static final String ARG_ASK_OTP = "mAskOtp";

    //private static Integer[] keys = {0,1,2,3,4,5,6,7,8,9};

    private TxnPinInputDialogIf mCallback;
    private String mPin;
    private boolean mAskOtp;

    public interface TxnPinInputDialogIf {
        void onTxnPin(String pin, String tag);
    }


    public static TxnPinInputDialog newInstance(int cashCredit, int cashDebit, int overdraft, boolean argAskOtp) {
        Bundle args = new Bundle();
        args.putInt(ARG_CASH_CREDIT, cashCredit);
        args.putInt(ARG_CASH_DEBIT, cashDebit);
        //args.putInt(ARG_CASHBACK_DEBIT, cashbackDebit);
        args.putInt(ARG_OVERDRAFT, overdraft);
        /*if(cancelTxnId!=null && !cancelTxnId.isEmpty()) {
            args.putString(ARG_CANCEL_TXNID, cancelTxnId);
        }*/
        args.putBoolean(ARG_ASK_OTP, argAskOtp);

        TxnPinInputDialog fragment = new TxnPinInputDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mCallback = (TxnPinInputDialogIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement TxnPinInputDialogIf");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_txn_pin_input, null, false);

        bindUiResources(v);
        if(savedInstanceState!=null) {
            LogMy.d(TAG,"Restoring");
            mPin = savedInstanceState.getString("mPin");
        } else {
            mPin = "";
        }

        // set values
        /*String cancelTxnId = getArguments().getString(ARG_CANCEL_TXNID);
        if(cancelTxnId!=null) {
            mLayoutAmts.setVisibility(View.GONE);
            String msg = "Cancel Transaction "+cancelTxnId+" ?";
            mLabelInfo.setText(msg);

        } else {*/
            int cashCredit = getArguments().getInt(ARG_CASH_CREDIT);
            int cashDebit = getArguments().getInt(ARG_CASH_DEBIT);
            int overdraft = getArguments().getInt(ARG_OVERDRAFT);
            //int cashbackDebit = getArguments().getInt(ARG_CASHBACK_DEBIT);

            mAskOtp = getArguments().getBoolean(ARG_ASK_OTP);
            if(mAskOtp) {
                mTitle.setText("Enter OTP");
                mInputSecretPin.setHint("Enter OTP");
            }

            if (cashCredit > 0) {
                mInputCashAmount.setText(AppCommonUtil.getSignedAmtStr(cashCredit, true));
                mInputCashAmount.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_positive));
            } else if (cashDebit > 0) {
                mInputCashAmount.setText(AppCommonUtil.getSignedAmtStr(cashDebit, false));
                mInputCashAmount.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
            } else {
                mLayoutAcc.setVisibility(View.GONE);
            }

            if(overdraft > 0) {
                mLayoutOverdraft.setVisibility(View.VISIBLE);
                mInputOverdraft.setText(AppCommonUtil.getSignedAmtStr(overdraft, false));
            } else {
                mLayoutOverdraft.setVisibility(View.GONE);
            }

            /*if (cashbackDebit > 0) {
                mInputCashbackAmount.setText(AppCommonUtil.getSignedAmtStr(cashbackDebit, false));
                mInputCashAmount.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_negative));
            } else {
                mLayoutCashbackAmount.setVisibility(View.GONE);
            }*/
        //}

        initKeyboard();

        /*Dialog dialog =  new AlertDialog.Builder(getActivity(), R.style.WrapEverythingDialog)
                .setView(v)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing here because we override this button later to change the close behaviour.
                                //However, we still need this because on older versions of Android unless we
                                //pass a handler the button doesn't get instantiated
                            }
                        })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AppCommonUtil.setDialogTextSize(TxnPinInputDialog.this, (AlertDialog) dialog);
            }
        });*/

        Dialog dialog =  new AlertDialog.Builder(getActivity(), R.style.WrapEverythingDialog).setView(v).create();
        //dialog.setTitle("Enter PIN");
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(AppConstants.BLOCK_SCREEN_CAPTURE) {
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AppCommonUtil.setDialogTextSize(TxnPinInputDialog.this, (AlertDialog) dialog);
                //((AlertDialog)dialog).getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        return dialog;
    }

    /*@Override
    public void handleBtnClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                //Do nothing
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
    }*/

    /*@Override
    public void onStart() {
        // This MUST be called first! Otherwise the view tweaking will not be present in the displayed Dialog (most likely overriden)
        super.onStart();

        //int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
        //getDialog().getWindow().setLayout(R.dimen.keyboard_full_width_dialog_less, ViewGroup.LayoutParams.WRAP_CONTENT);

        //forceWrapContent(getDialog().findViewById(R.id.label_title));
    }

    protected void forceWrapContent(View v) {
        // Start with the provided view
        View current = v;

        // Travel up the tree until fail, modifying the LayoutParams
        do {
            // Get the parent
            ViewParent parent = current.getParent();

            // Check if the parent exists
            if (parent != null) {
                // Get the view
                try {
                    current = (View) parent;
                } catch (ClassCastException e) {
                    // This will happen when at the top view, it cannot be cast to a View
                    break;
                }

                // Modify the layout
                current.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        } while (current.getParent() != null);

        // Request a layout to be re-done
        current.requestLayout();
    }*/

    /*@Override
    public void onStart()
    {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean wantToCloseDialog = true;

                    LogMy.d(TAG, "Clicked Ok");
                    String pinOrOtp = mInputSecretPin.getText().toString();

                    int errorCode = ValidationHelper.validatePin(pinOrOtp);
                    if (errorCode == ErrorCodes.NO_ERROR) {
                        mCallback.onTxnPin(pinOrOtp, getTag());
                    } else {
                        mInputSecretPin.setError(AppCommonUtil.getErrorDesc(errorCode));
                        wantToCloseDialog = false;
                    }

                    if (wantToCloseDialog)
                        d.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }*/

    @Override
    public void onClick(View v) {
        int vId = v.getId();

        String curStr = mInputSecretPin.getText().toString();

        if (vId == R.id.input_kb_bs) {
            LogMy.d(TAG,"Clicked BS");
            if (curStr.length() > 0) {
                mInputSecretPin.setText("");
                mInputSecretPin.append(curStr, 0, (curStr.length() - 1));
                mPin = mPin.substring(0,(mPin.length()-1));
            }

        } else if (vId == R.id.input_kb_0 || vId == R.id.input_kb_1 || vId == R.id.input_kb_2 || vId == R.id.input_kb_3 || vId == R.id.input_kb_4 || vId == R.id.input_kb_5 || vId == R.id.input_kb_6 || vId == R.id.input_kb_7 || vId == R.id.input_kb_8 || vId == R.id.input_kb_9) {
            LogMy.d(TAG,"Clicked Num key");

            AppCompatButton key = (AppCompatButton) v;
            if(mAskOtp && mPin.length() < CommonConstants.OTP_LEN) {
                mPin = mPin + key.getText();
                mInputSecretPin.append(key.getText());

            } else if(!mAskOtp && mPin.length() < CommonConstants.PIN_LEN) {
                mPin = mPin + key.getText();
                mInputSecretPin.append("*");
            }
        }
    }

    private EditText mTitle;
    private View mLayoutAcc;
    private EditText mInputCashAmount;
    private View mLayoutOverdraft;
    private EditText mInputOverdraft;
    //private EditText mInputCashbackAmount;
    //private LinearLayout mLayoutCashAmount;
    //private LinearLayout mLayoutCashbackAmount;
    //private EditText mLabelInfo;
    private EditText mInputSecretPin;

    private AppCompatButton mKeys[];
    private AppCompatButton mKeyOk;
    private AppCompatImageButton mKeyBspace;

    private void bindUiResources(View v) {
        mTitle = (EditText) v.findViewById(R.id.label_title);
        //mLayoutAmts = v.findViewById(R.id.layout_amounts);

        mLayoutAcc = v.findViewById(R.id.layout_account);;
        mInputCashAmount = (EditText) v.findViewById(R.id.input_account);
        mLayoutOverdraft = v.findViewById(R.id.layout_overdraft);;
        mInputOverdraft = (EditText) v.findViewById(R.id.input_overdraft);
        /*mInputCashbackAmount = (EditText) v.findViewById(R.id.input_cashback_amount);
        mLayoutCashAmount = (LinearLayout) v.findViewById(R.id.layout_cash_amount);
        mLayoutCashbackAmount = (LinearLayout) v.findViewById(R.id.layout_cashback_amount);*/

        //mLabelInfo = (EditText) v.findViewById(R.id.label_information);
        mInputSecretPin = (EditText) v.findViewById(R.id.input_secret_pin);

        mKeys = new AppCompatButton[10];
        mKeys[0] = (AppCompatButton) v.findViewById(R.id.input_kb_0);
        mKeys[1] = (AppCompatButton) v.findViewById(R.id.input_kb_1);
        mKeys[2] = (AppCompatButton) v.findViewById(R.id.input_kb_2);
        mKeys[3] = (AppCompatButton) v.findViewById(R.id.input_kb_3);
        mKeys[4] = (AppCompatButton) v.findViewById(R.id.input_kb_4);
        mKeys[5] = (AppCompatButton) v.findViewById(R.id.input_kb_5);
        mKeys[6] = (AppCompatButton) v.findViewById(R.id.input_kb_6);
        mKeys[7] = (AppCompatButton) v.findViewById(R.id.input_kb_7);
        mKeys[8] = (AppCompatButton) v.findViewById(R.id.input_kb_8);
        mKeys[9] = (AppCompatButton) v.findViewById(R.id.input_kb_9);
        mKeyBspace = (AppCompatImageButton) v.findViewById(R.id.input_kb_bs);
        mKeyOk = (AppCompatButton) v.findViewById(R.id.input_kb_ok);
    }

    private void initKeyboard() {
        // set text randomly for the keys
        //Collections.shuffle(Arrays.asList(keys));

        for(int i=0; i<10; i++) {
            mKeys[i].setOnClickListener(this);
            //mKeys[i].setText(String.valueOf(keys[i]));
        }

        mKeyBspace.setOnClickListener(this);
        mKeyOk.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                // Seperate click handler - to avoid double click of OK button
                LogMy.d(TAG,"Clicked OK");
                //mInputSecretPin.setText("");
                Boolean wantToCloseDialog = true;
                //String pin = mInputSecretPin.getText().toString();

                int errorCode;
                if(mAskOtp) {
                    errorCode = ValidationHelper.validateOtp(mPin);
                } else {
                    errorCode = ValidationHelper.validatePin(mPin);
                }
                if (errorCode == ErrorCodes.NO_ERROR) {
                    mCallback.onTxnPin(mPin, getTag());
                } else {
                    mInputSecretPin.setError(AppCommonUtil.getErrorDesc(errorCode));
                    wantToCloseDialog = false;
                }

                if (wantToCloseDialog)
                    getDialog().dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        // Store access variables for window and blank point
        int width = getResources().getDimensionPixelSize(R.dimen.keyboard_full_width_dialog_less);
        //int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
        getDialog().getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        /*
        Window window = getDialog().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((int) (size.x * 0.75), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);*/

        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mPin", mPin);
    }

}

