package in.ezeshop.customerbase;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.SortTxnDialog;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.entities.MyTransaction;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.MyGlobalSettings;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Customers;
import in.ezeshop.common.database.Transaction;
import in.ezeshop.customerbase.entities.CustomerUser;
import in.ezeshop.customerbase.helper.MyRetainedFragment;

/**
 * Created by adgangwa on 07-04-2016.
 */
public class TxnListFragment extends BaseFragment {
    private static final String TAG = "CustApp-TxnListFragment";

    private static final String CSV_REPORT_HEADER_1 = ",,MyeCash Customer Statement,,,,,,,,,,,,,";
    // Merchant Id.,<id>,,,,,,,,,
    private static final String CSV_REPORT_HEADER_2 = "\"=\"\"Customer Id.\"\"\",%s,,,,,,,,,,,,,";
    // Card Num.,<card id>,,,,,,,,,
    //private static final String CSV_REPORT_HEADER_3 = "\"=\"\"Member Card#\"\"\",%s,,,,,,,,,,,,,";
    // ,,,,,,Period,From <start date> to <end date>,,,
    private static final String CSV_REPORT_HEADER_4 = ",,,,,,,Period,From %s to %s,,";
    // ,,,,,,Currency,INR,,,
    private static final String CSV_REPORT_HEADER_5 = ",,,,,,,Currency,INR,,";
    private static final String CSV_REPORT_HEADER_6 = ",,,,,,,,,,";
    private static final String CSV_REPORT_HEADER_7 = ",,,,,,,,,,";
    //private static final String CSV_HEADER = "Sl. No.,Date,Time,Transaction Id,Merchant Id,Merchant Name,Bill Amount,Cashback Add,Cashback Debit,Cashback Rate,Account Debit,Account Add,Extra Cashback Add,Extra Cashback Rate,Cancel Time,Comments";
    //private static final String CSV_HEADER_NO_ACC = "Sl. No.,Date,Time,Transaction Id,Merchant Id,Merchant Name,Bill Amount,Cashback Add,Cashback Debit,Cashback Rate,Cancel Time,Comments";
    //private static final String CSV_HEADER = "Sl. No.,Date,Time,Transaction Id,Merchant Id,Merchant Name,Bill Amount,Cashback Add,Cashback Debit,Cashback Rate,Account Debit,Account Add,Extra Cashback Add,Extra Cashback Rate,Comments";
    //private static final String CSV_HEADER_NO_ACC = "Sl. No.,Date,Time,Transaction Id,Merchant Id,Merchant Name,Bill Amount,Cashback Add,Cashback Debit,Cashback Rate,Comments";
    private static final String CSV_HEADER = "Sl. No.,Date,Time,Merchant,Bill Amount,Account Add,Account Debit,Overdraft,Cashback,Cashback Details,Transaction Id,Linked Invoice";

    // 5+10+10+10+10+10+10+5+5+5+5 = 85
    private static final int CSV_RECORD_MAX_CHARS = 100;
    //TODO: change this to 100 in production
    private static final int CSV_LINES_BUFFER = 5;

    private static final String ARG_START_TIME = "startTime";
    private static final String ARG_END_TIME = "endTime";
    private static final String ARG_FILTER_MCHNT = "filterMchnt";

    private static final int REQ_NOTIFY_ERROR = 1;
    private static final int REQ_SORT_TXN_TYPES = 2;

    private static final String DIALOG_SORT_TXN_TYPES = "dialogSortTxn";
    private static final String DIALOG_TXN_DETAILS = "dialogTxnDetails";

    private SimpleDateFormat mSdfDateWithTime;
    private SimpleDateFormat mSdfOnlyDateCSV;
    private SimpleDateFormat mSdfOnlyTimeCSV;
    private SimpleDateFormat mSdfOnlyDate;

    //private EditText mFilterMchnt;
    //private EditText mFilterDuration;
    private RecyclerView mTxnRecyclerView;
    private EditText mHeaderTime;
    private EditText mHeaderBill;
    private EditText mHeaderCb;
    private EditText mHeaderAcc;
    private EditText mInfoOldTxns;

    private MyRetainedFragment mRetainedFragment;
    private TxnListFragmentIf mCallback;
    private Date mStartTime;
    private Date mEndTime;
    //private Boolean mForSingleMchnt;
    // instance state - store and restore
    private int mSelectedSortType;

    public interface TxnListFragmentIf {
        void setToolbarTitle(String title);
        MyRetainedFragment getRetainedFragment();
    }

    public static TxnListFragment getInstance(Date startTime, Date endTime, String mchntName) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_START_TIME, startTime);
        args.putSerializable(ARG_END_TIME, endTime);
        if(mchntName!=null) {
            args.putString(ARG_FILTER_MCHNT, mchntName);
        }

        TxnListFragment fragment = new TxnListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_txn_list_custapp, container, false);

        //mFilterMchnt = (EditText) view.findViewById(R.id.txnlist_filter_mchnt);
        //mFilterDuration = (EditText) view.findViewById(R.id.txnlist_filter_duration);

        mTxnRecyclerView = (RecyclerView) view.findViewById(R.id.txn_recycler_view);
        mTxnRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mHeaderTime = (EditText) view.findViewById(R.id.txnlist_header_time);
        mHeaderBill = (EditText) view.findViewById(R.id.txnlist_header_bill);
        mHeaderCb = (EditText) view.findViewById(R.id.txnlist_header_cb);
        mHeaderAcc = (EditText) view.findViewById(R.id.txnlist_header_acc);

        mInfoOldTxns = (EditText) view.findViewById(R.id.info_old_txns);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogMy.d(TAG, "In onActivityCreated");

        try {
            mCallback = (TxnListFragmentIf) getActivity();

            mRetainedFragment = mCallback.getRetainedFragment();

            mSdfDateWithTime = new SimpleDateFormat(CommonConstants.DATE_FORMAT_WITH_TIME, CommonConstants.DATE_LOCALE);
            mSdfOnlyDateCSV = new SimpleDateFormat(CommonConstants.DATE_FORMAT_ONLY_DATE_CSV, CommonConstants.DATE_LOCALE);
            mSdfOnlyTimeCSV = new SimpleDateFormat(CommonConstants.DATE_FORMAT_ONLY_TIME_24_CSV, CommonConstants.DATE_LOCALE);
            mSdfOnlyDate = new SimpleDateFormat(CommonConstants.DATE_FORMAT_ONLY_DATE_DISPLAY, CommonConstants.DATE_LOCALE);
            //updateUI();

            // get arguments and store in instance
            mStartTime = (Date)getArguments().getSerializable(ARG_START_TIME);
            mEndTime = (Date)getArguments().getSerializable(ARG_END_TIME);

            // show filters
            /*String filterMchnt = getArguments().getString(ARG_FILTER_MCHNT);
            if(filterMchnt!=null) {
                // Txns are for particular merchant only
                mFilterMchnt.setVisibility(View.VISIBLE);
                mFilterMchnt.setText(filterMchnt);
                mForSingleMchnt = true;
                mInfoOldTxns.setVisibility(View.GONE);
                mHeaderMchnt.setVisibility(View.GONE);

                String durationFilter = "From: "+mSdfOnlyDate.format(mStartTime)+
                        "  -  To: "+mSdfOnlyDate.format(mEndTime);
                mFilterDuration.setText(durationFilter);

            } else {
                mFilterMchnt.setVisibility(View.GONE);
                mForSingleMchnt = false;
                mInfoOldTxns.setVisibility(View.VISIBLE);
                mHeaderMchnt.setVisibility(View.VISIBLE);

                String durationFilter = "Duration: Last "+ MyGlobalSettings.getTxnsIntableKeepDays()+" days";
                mFilterDuration.setText(durationFilter);
            }*/

            int sortType = SortTxnDialog.TXN_SORT_DATE_TIME;
            if(savedInstanceState!=null) {
                sortType = savedInstanceState.getInt("mSelectedSortType");
            }
            sortTxnList(sortType);

        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement TxnListFragmentIf");
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            getActivity().onBackPressed();
        }

        setHasOptionsMenu(true);
    }

    private void sortTxnList(int sortType) {
        LogMy.d(TAG,"In sortTxnList : "+sortType+", "+mSelectedSortType);
        switch (sortType) {
            case SortTxnDialog.TXN_SORT_DATE_TIME:
                Collections.sort(mRetainedFragment.mLastFetchTransactions, new MyTransaction.TxnDateComparator());
                break;
            case SortTxnDialog.TXN_SORT_bILL_AMT:
                Collections.sort(mRetainedFragment.mLastFetchTransactions, new MyTransaction.TxnBillComparator());
                break;
            case SortTxnDialog.TXN_SORT_CB_AWARD:
                Collections.sort(mRetainedFragment.mLastFetchTransactions, new MyTransaction.TxnCbAwardComparator());
                break;
            case SortTxnDialog.TXN_SORT_ACC_AMT:
                Collections.sort(mRetainedFragment.mLastFetchTransactions, new MyTransaction.TxnAccAmtComparator());
                break;
        }
        // Make it in decreasing order
        Collections.reverse(mRetainedFragment.mLastFetchTransactions);

        // Remove arrow as per old sort type
        switch (mSelectedSortType) {
            case SortTxnDialog.TXN_SORT_DATE_TIME:
                mHeaderTime.setText(R.string.txnlist_header_datetime);
                mHeaderTime.setTypeface(null, Typeface.NORMAL);
                break;
            case SortTxnDialog.TXN_SORT_bILL_AMT:
                mHeaderBill.setText(R.string.txnlist_header_bill);
                mHeaderBill.setTypeface(null, Typeface.NORMAL);
                break;
            case SortTxnDialog.TXN_SORT_CB_AWARD:
                mHeaderCb.setText(R.string.txnlist_header_cb);
                mHeaderCb.setTypeface(null, Typeface.NORMAL);
                break;
            case SortTxnDialog.TXN_SORT_ACC_AMT:
                mHeaderAcc.setText(R.string.txnlist_header_acc);
                mHeaderAcc.setTypeface(null, Typeface.NORMAL);
                break;
        }

        // Add arrow in header as per new sort type
        String text = null;
        switch (sortType) {
            case SortTxnDialog.TXN_SORT_DATE_TIME:
                text = AppConstants.SYMBOL_DOWN_ARROW + getString(R.string.txnlist_header_datetime);
                mHeaderTime.setText(text);
                mHeaderTime.setTypeface(null, Typeface.BOLD);
                break;
            case SortTxnDialog.TXN_SORT_bILL_AMT:
                text = AppConstants.SYMBOL_DOWN_ARROW + getString(R.string.txnlist_header_bill);
                mHeaderBill.setText(text);
                mHeaderBill.setTypeface(null, Typeface.BOLD);
                break;
            case SortTxnDialog.TXN_SORT_CB_AWARD:
                text = AppConstants.SYMBOL_DOWN_ARROW + getString(R.string.txnlist_header_cb);
                mHeaderCb.setText(text);
                mHeaderCb.setTypeface(null, Typeface.BOLD);
                break;
            case SortTxnDialog.TXN_SORT_ACC_AMT:
                text = AppConstants.SYMBOL_DOWN_ARROW + getString(R.string.txnlist_header_acc);
                mHeaderAcc.setText(text);
                mHeaderAcc.setTypeface(null, Typeface.BOLD);
                break;
        }

        // store existing sortType
        mSelectedSortType = sortType;
    }

    public void showDetailedDialog(int pos) {
        TxnDetailsDialog dialog = TxnDetailsDialog.newInstance(pos);
        dialog.show(getFragmentManager(), DIALOG_TXN_DETAILS);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.txn_list_menu_custapp, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!mCallback.getRetainedFragment().getResumeOk())
            return true;
        //mActiveMenuItemId = item.getItemId();
        try {
            int i = item.getItemId();
            if (i == R.id.action_download) {
                downloadReport();
            } else if (i == R.id.action_email) {
                emailReport();
            } else if (i == R.id.action_sort) {
                //SortTxnDialog dialog = SortTxnDialog.newInstance(mSelectedSortType, anyAccTxn());
                SortTxnDialog dialog = SortTxnDialog.newInstance(mSelectedSortType);
                dialog.setTargetFragment(this, REQ_SORT_TXN_TYPES);
                dialog.show(getFragmentManager(), DIALOG_SORT_TXN_TYPES);
            }
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }

        return super.onOptionsItemSelected(item);
    }

    /*private boolean anyAccTxn() {
        // loop and check if there's any txn with acc credit/debit
        boolean accFigures = false;
        for (Transaction txn :
                mRetainedFragment.mLastFetchTransactions) {
            if(txn.getCl_debit()!=0 || txn.getCl_credit()!=0) {
                accFigures = true;
                break;
            }
        }
        return accFigures;
    }*/

    private void downloadReport() {
        File file = createCsvReport();
        if(file!=null) {
            // register with download manager, so as can be seen by clicking 'Downloads' icon
            DownloadManager manager = (DownloadManager) getActivity().getSystemService(AppCompatActivity.DOWNLOAD_SERVICE);

            SimpleDateFormat ddMM = new SimpleDateFormat(CommonConstants.DATE_FORMAT_DDMM, CommonConstants.DATE_LOCALE);
            String startDate = ddMM.format(mStartTime);
            String endDate = ddMM.format(mEndTime);

            String fileName = "MyeCash_Statement_"+startDate+"_"+endDate+CommonConstants.CSV_FILE_EXT;
            long fileid = manager.addCompletedDownload(fileName, "MyeCash transactions statement",
                    true, "text/plain", file.getAbsolutePath(), file.length(), true);
            AppCommonUtil.toast(getActivity(),"Download Complete");
        }
    }

    private void emailReport() {
        File csvFile = createCsvReport();
        if(csvFile != null) {
            // create intent for email
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            // The intent does not have a URI, so declare the "text/plain" MIME type
            emailIntent.setType("text/csv");
            /*String emailId = MerchantUser.getInstance().getMerchant().getEmail();
            if(emailId!=null && emailId.length()>0) {
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {emailId}); // recipients
            }*/
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MyeCash Customer Statement");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Dear Sir - \n\nPlease find attached the requested MyeCash transaction statement.\n\nThanks.\n\nRegards,\nMyeCash Team.");
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(csvFile));

            // check if there's activity available for the intent
            PackageManager packageManager = getActivity().getPackageManager();
            List activities = packageManager.queryIntentActivities(emailIntent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            boolean isIntentSafe = activities.size() > 0;

            if(isIntentSafe) {
                startActivity(emailIntent);
            } else {
                DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle,
                        "No Email App like Gmail is installed on this mobile to send the email. \nPlease install any and try again. You can also download the file.", true, true);
                notDialog.setTargetFragment(this,REQ_NOTIFY_ERROR);
                notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            }
        }
    }

    private File createCsvReport() {
        LogMy.d(TAG,"In createCsvReport");

        File file = null;
        try {
            long currentTimeInSecs = Math.abs(System.currentTimeMillis() / 1000);
            String fileName = AppConstants.FILE_PREFIX_TXN_LIST + String.valueOf(currentTimeInSecs) + CommonConstants.CSV_FILE_EXT;

            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            file = new File(dir, fileName);
            FileOutputStream stream = new FileOutputStream(file, false);

            // +10 to cover for headers
            StringBuilder sb = new StringBuilder(CSV_RECORD_MAX_CHARS*(CSV_LINES_BUFFER+10));
            String startDate = mSdfOnlyDateCSV.format(mStartTime);
            String endDate = mSdfOnlyDateCSV.format(mEndTime);

            // append all headers
            Customers user = CustomerUser.getInstance().getCustomer();
            sb.append(CSV_REPORT_HEADER_1).append(CommonConstants.NEWLINE_SEP);
            sb.append(String.format(CSV_REPORT_HEADER_2,user.getMobile_num())).append(CommonConstants.NEWLINE_SEP);
            /*sb.append(String.format(CSV_REPORT_HEADER_3,
                    (user.getMembership_card()==null)?"":user.getMembership_card().getCardNum()))
                    .append(CommonConstants.NEWLINE_SEP);*/
            sb.append(String.format(CSV_REPORT_HEADER_4,startDate,endDate)).append(CommonConstants.NEWLINE_SEP);
            sb.append(CSV_REPORT_HEADER_5).append(CommonConstants.NEWLINE_SEP);
            sb.append(CSV_REPORT_HEADER_6).append(CommonConstants.NEWLINE_SEP);
            sb.append(CSV_REPORT_HEADER_7).append(CommonConstants.NEWLINE_SEP);

            /*boolean showAccFields = anyAccTxn();
            if(showAccFields) {
                sb.append(CSV_HEADER).append(CommonConstants.NEWLINE_SEP);
            } else {
                sb.append(CSV_HEADER_NO_ACC).append(CommonConstants.NEWLINE_SEP);
            }*/

            int billTotal = 0;
            int accDebitTotal = 0;
            int accOverdraftTotal = 0;
            int accCreditTotal = 0;
            //int cbRedeemTotal = 0;
            int cbAwardTotal = 0;

            int cnt = mRetainedFragment.mLastFetchTransactions.size();
            for(int i=0; i<cnt; i++) {
                if(sb==null) {
                    // +1 for buffer
                    sb = new StringBuilder(CSV_RECORD_MAX_CHARS*(CSV_LINES_BUFFER+1));
                }

                // Append CSV record for this txn
                sb.append(i+1).append(CommonConstants.CSV_DELIMETER);

                Transaction txn = mRetainedFragment.mLastFetchTransactions.get(i);
                sb.append(mSdfOnlyDateCSV.format(txn.getCreate_time())).append(CommonConstants.CSV_DELIMETER);
                sb.append(mSdfOnlyTimeCSV.format(txn.getCreate_time())).append(CommonConstants.CSV_DELIMETER);
                sb.append(txn.getMerchant_name()).append(CommonConstants.CSV_DELIMETER);

                if(txn.getTotal_billed() > 0) {
                    sb.append(txn.getTotal_billed()).append(CommonConstants.CSV_DELIMETER);
                    billTotal = billTotal + txn.getTotal_billed();
                } else {
                    sb.append("0").append(CommonConstants.CSV_DELIMETER);
                }

                // account details
                if (txn.getCl_credit() > 0) {
                    sb.append(txn.getCl_credit()).append(CommonConstants.CSV_DELIMETER);
                    accCreditTotal = accCreditTotal + txn.getCl_credit();
                } else {
                    sb.append("0").append(CommonConstants.CSV_DELIMETER);
                }
                if (txn.getCl_debit() > 0) {
                    sb.append(txn.getCl_debit()).append(CommonConstants.CSV_DELIMETER);
                    accDebitTotal = accDebitTotal + txn.getCl_debit();
                } else {
                    sb.append("0").append(CommonConstants.CSV_DELIMETER);
                }
                if(txn.getCl_overdraft()>0) {
                    sb.append(txn.getCl_overdraft()).append(CommonConstants.CSV_DELIMETER);
                    accOverdraftTotal = accOverdraftTotal + txn.getCl_overdraft();
                } else {
                    sb.append("0").append(CommonConstants.CSV_DELIMETER);
                }

                // cashback details
                int totalCb = txn.getCb_credit() + txn.getExtra_cb_credit();
                if(totalCb > 0) {
                    sb.append(totalCb).append(CommonConstants.CSV_DELIMETER);
                    cbAwardTotal = cbAwardTotal + totalCb;
                } else {
                    sb.append("0").append(CommonConstants.CSV_DELIMETER);
                }
                sb.append(MyTransaction.getCbDetailStr(txn,true)).append(CommonConstants.CSV_DELIMETER);

                // other details
                sb.append(txn.getTrans_id()).append(CommonConstants.CSV_DELIMETER);
                if(txn.getInvoiceNum()==null) {
                    sb.append(CommonConstants.CSV_DELIMETER);
                } else {
                    sb.append(txn.getInvoiceNum()).append(CommonConstants.CSV_DELIMETER);
                }

                sb.append(CommonConstants.NEWLINE_SEP);

                // Write every 100 records in one go to the file
                if(i%CSV_LINES_BUFFER == 0) {
                    stream.write(sb.toString().getBytes());
                    LogMy.d(TAG,"Written "+String.valueOf(i+1)+"records to "+file.getAbsolutePath());
                    sb = null;
                }
            }

            // write totals line
            if(sb==null) {
                sb = new StringBuilder(CSV_RECORD_MAX_CHARS);
            }
            sb.append("Total").append(CommonConstants.CSV_DELIMETER);
            sb.append(CommonConstants.CSV_DELIMETER).append(CommonConstants.CSV_DELIMETER).append(CommonConstants.CSV_DELIMETER);
            sb.append(billTotal).append(CommonConstants.CSV_DELIMETER);
            sb.append(accCreditTotal).append(CommonConstants.CSV_DELIMETER);
            sb.append(accDebitTotal).append(CommonConstants.CSV_DELIMETER);
            sb.append(accOverdraftTotal).append(CommonConstants.CSV_DELIMETER);
            sb.append(cbAwardTotal).append(CommonConstants.CSV_DELIMETER);
            sb.append(CommonConstants.CSV_DELIMETER).append(CommonConstants.CSV_DELIMETER).append(CommonConstants.NEWLINE_SEP);

            // write remaining records
            if(sb!=null) {
                stream.write(sb.toString().getBytes());
                LogMy.d(TAG,"Written pending records to "+file.getAbsolutePath());
                sb = null;
            }

            stream.close();

        } catch(Exception e) {
            LogMy.e(TAG,"exception in createCsvReport: "+e.toString());
            if(file!=null) {
                // delete it
                file.delete();
            }

            DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            notDialog.setTargetFragment(this,REQ_NOTIFY_ERROR);
            notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);

            return null;
        }

        return file;
    }

    public void sortNupdateUI() {
        //mTxnRecyclerView.setAdapter(new TxnAdapter(mRetainedFragment.mLastFetchTransactions, mForSingleMchnt));
        //mCallback.setToolbarTitle(mRetainedFragment.mLastFetchTransactions.size() + " Transactions");
        sortTxnList(mSelectedSortType);
        updateUI();
    }

    private void updateUI() {
        mTxnRecyclerView.setAdapter(new TxnAdapter(mRetainedFragment.mLastFetchTransactions));
        mCallback.setToolbarTitle(mRetainedFragment.mLastFetchTransactions.size() + " Transactions");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogMy.d(TAG, "In onActivityResult :" + requestCode + ", " + resultCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        try {
            if (requestCode == REQ_SORT_TXN_TYPES) {
                int sortType = data.getIntExtra(SortTxnDialog.EXTRA_SELECTION, SortTxnDialog.TXN_SORT_DATE_TIME);
                sortTxnList(sortType);
                updateUI();
            }
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
        try {
            updateUI();
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            getActivity().onBackPressed();
        }
        mCallback.getRetainedFragment().setResumeOk(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mSelectedSortType", mSelectedSortType);
    }


    private class TxnHolder extends RecyclerView.ViewHolder {

        private Transaction mTxn;

        public TextView mDatetime;
        public TextView mMchntName;

        public TextView mBillAmount;
        public TextView mCashbackAward;
        public TextView mAccountAmt;
        public View mImgOverdraft;

        public TextView mLabelBill;
        public TextView mLabelCb;
        public TextView mLabelAcc;

        public TxnHolder(View itemView) {
            super(itemView);

            mDatetime = (TextView) itemView.findViewById(R.id.txn_time);
            mMchntName = (TextView) itemView.findViewById(R.id.txn_mchnt_name);
            mBillAmount = (TextView) itemView.findViewById(R.id.bill_input);
            mAccountAmt = (TextView) itemView.findViewById(R.id.acc_input);
            mImgOverdraft = itemView.findViewById(R.id.txn_ic_overdraft);
            mCashbackAward = (TextView) itemView.findViewById(R.id.txn_cashback_award);

            mLabelBill = (TextView) itemView.findViewById(R.id.txn_label_bill);
            mLabelCb = (TextView) itemView.findViewById(R.id.txn_label_cb);
            mLabelAcc = (TextView) itemView.findViewById(R.id.txn_label_acc);
        }

//        public void bindTxn(Transaction txn, boolean forSingleMchnt) {
        public void bindTxn(Transaction txn) {
            mTxn = txn;

            mDatetime.setText(mSdfDateWithTime.format(mTxn.getCreate_time()));
            /*if(forSingleMchnt) {
                mMchntName.setVisibility(View.GONE);
            } else {*/
                mMchntName.setVisibility(View.VISIBLE);
                mMchntName.setText(mTxn.getMerchant_name());
            //}

            AppCommonUtil.showAmt(getActivity(), mLabelBill, mBillAmount, mTxn.getTotal_billed(),true);
            AppCommonUtil.showAmt(getActivity(), mLabelCb, mCashbackAward, (mTxn.getCb_credit()+mTxn.getExtra_cb_credit()), true);

            // set account add/debit amount
            int value = txn.getCl_credit() - txn.getCl_debit() - txn.getCl_overdraft();
            AppCommonUtil.showAmtColor(getActivity(), mLabelAcc, mAccountAmt, value, true);

            // show/hide overdraft icon
            mImgOverdraft.setVisibility( value>0?View.GONE:View.VISIBLE);
        }
    }

    private class TxnAdapter extends RecyclerView.Adapter<TxnHolder> {
        private List<Transaction> mTxns;
        //private boolean mForSingleMchnt;

        private int selected_position = -1;
        private View.OnClickListener mListener;

//        public TxnAdapter(List<Transaction> txns, boolean forSingleMchnt) {
        public TxnAdapter(List<Transaction> txns) {
            mTxns = txns;
            //mForSingleMchnt = forSingleMchnt;
            mListener = new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    if(!mCallback.getRetainedFragment().getResumeOk())
                        return;

                    LogMy.d(TAG,"In onSingleClick of txn list item");
                    int pos = mTxnRecyclerView.getChildAdapterPosition(v);

                    // Updating old as well as new positions
                    notifyItemChanged(selected_position);
                    selected_position = pos;
                    notifyItemChanged(selected_position);

                    if (pos >= 0 && pos < getItemCount()) {
                        showDetailedDialog(pos);
                    } else {
                        LogMy.e(TAG,"Invalid position in onClickListener of txn list item: "+pos);
                    }
                }
            };
        }

        @Override
        public TxnHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.txn_itemview_custapp_2, parent, false);
            //LogMy.d(TAG,"Root view: "+view.getId());
            //view.setOnClickListener(mListener);
            return new TxnHolder(view);
        }
        @Override
        public void onBindViewHolder(TxnHolder holder, int position) {
            Transaction txn = mTxns.get(position);
            if(selected_position == position){
                // Here I am just highlighting the background
                holder.itemView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.list_highlight));
            }else{
                holder.itemView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            }

            holder.itemView.setOnClickListener(mListener);
            //holder.bindTxn(txn, mForSingleMchnt);
            holder.bindTxn(txn);
        }
        @Override
        public int getItemCount() {
             return mTxns.size();
        }
    }
}
