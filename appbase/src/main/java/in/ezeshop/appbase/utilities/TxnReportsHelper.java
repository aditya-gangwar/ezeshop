package in.ezeshop.appbase.utilities;

import android.app.Activity;
import android.content.Context;

import com.backendless.exceptions.BackendlessException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import in.ezeshop.appbase.entities.MyTransaction;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.CsvConverter;
import in.ezeshop.common.DateUtil;
import in.ezeshop.common.MyGlobalSettings;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Transaction;

/**
 * Created by adgangwa on 28-10-2016.
 */
public class TxnReportsHelper {

    public interface TxnReportsHelperIf {
        void fetchTxnsFromDB(String whereClause);
        void fetchTxnFiles(List<String> missingFiles);
        void onFinalTxnSetAvailable(List<Transaction> allTxns);
    }

    private static final String TAG = "BaseApp-TxnReportsHelper";

    private Context mContext;
    private TxnReportsHelperIf mCallback;

    private Date mTxnInDbFrom;
    private Date mFromDate;
    private Date mToDate;
    private String mMerchantId;
    private String mCustomerId;

    public List<String> mAllFiles = new ArrayList<>();
    public List<String> mMissingFiles = new ArrayList<>();
    public List<Transaction> mTxnsFromCsv = new ArrayList<>();


    public TxnReportsHelper(Activity callingActivity) {
        mContext = callingActivity;
        mCallback = (TxnReportsHelperIf) callingActivity;
        mTxnInDbFrom = getTxnInDbStartTime();
    }

    public static Date getTxnInDbStartTime() {
        // time after which txns should be in DB
        DateUtil now = new DateUtil(new Date(), TimeZone.getDefault());
        LogMy.d( TAG, "now: "+ String.valueOf(now.getTime().getTime()) );
        // -1 as 'today' is inclusive
        now.removeDays(MyGlobalSettings.getTxnsIntableKeepDays()-1);
        LogMy.d( TAG, "now: "+ String.valueOf(now.getTime().getTime()) );
        return now.toMidnight().getTime();
    }

    /*
     * If mFromDate < mTxnInDbFrom (means txns from CSV files required)
     *      fetch required files
     *      (later once all files are available - check if txns from DB also required)
     * Else
     *      fetch txns only from DB
     */
    public void startTxnFetch(Date from, Date to, String merchantId, String customerId) throws Exception{
        mFromDate = from;
        mToDate = to;
        LogMy.d( TAG, "mTxnInDbFrom: "+ String.valueOf(mTxnInDbFrom.getTime()) );
        mMerchantId = merchantId;
        mCustomerId = customerId;

        // reset all
        mTxnsFromCsv.clear();

        if(mFromDate.getTime() < mTxnInDbFrom.getTime()) {
            // archived txns from CSV files are required
            if(mMerchantId==null || mMerchantId.isEmpty()) {
                LogMy.e(TAG,"Merchant ID not available to fetch txn CSV files");
                throw new BackendlessException(String.valueOf(ErrorCodes.GENERAL_ERROR), "Merchant ID not available to fetch txn CSV files");
            }

            // find which txn csv files are not locally available
            getTxnCsvFilePaths();
            if (mMissingFiles.isEmpty()) {
                // all required txn files are locally available
                // process all files and store applicable CSV records in 'mWorkFragment.mFilteredCsvRecords'
                onAllTxnFilesAvailable(false);
            } else {
                // one or more txn files are not locally available, fetch the same from backend
                // all files will be processed in single go, after fetching missing files
                mCallback.fetchTxnFiles(mMissingFiles);;
            }
        } else {
            // txns only from DB required
            mCallback.fetchTxnsFromDB(buildWhereClause());
        }
    }

    // checks for txn files locally and sets 'mWorkFragment.mAllFiles' and 'mWorkFragment.mMissingFiles' accordingly
    private void getTxnCsvFilePaths() {
        // loop through the dates for which report is required
        // check if file against the date is available locally
        // if not, add in missing file list
        long diff = Math.abs(mTxnInDbFrom.getTime() - mFromDate.getTime());
        int diffDays = (int) (diff / (24 * 60 * 60 * 1000));

        DateUtil txnDay = new DateUtil(mFromDate, TimeZone.getDefault());
        mMissingFiles.clear();
        mAllFiles.clear();

        for(int i=0; i<diffDays; i++) {
            String filename = CommonUtils.getTxnCsvFilename(txnDay.getTime(),mMerchantId);
            mAllFiles.add(filename);

            File file = mContext.getFileStreamPath(filename);
            if(file == null || !file.exists()) {
                // file does not exist
                LogMy.d(TAG,"Missing file: "+filename);
                String filepath = CommonUtils.getMerchantTxnDir(mMerchantId) + CommonConstants.FILE_PATH_SEPERATOR + filename;
                mMissingFiles.add(filepath);
            }
            txnDay.addDays(1);
        }
    }

    // this function is called for further processing,
    // when all CSV txn files for old days are available locally
    public void onAllTxnFilesAvailable(boolean remoteCsvTxnFileNotFound) throws Exception {
        // process CSV files to extract applicable CSV records
        processTxnFiles();
        // check if records from DB table are to be fetched too
        if( mToDate.getTime() >= mTxnInDbFrom.getTime() ||
                remoteCsvTxnFileNotFound ) {
            mCallback.fetchTxnsFromDB(buildWhereClause());
        } else {
            // no DB table records to be fetched
            onDbTxnsAvailable(null);
        }
    }

    // returns final list of transactions
    public void onDbTxnsAvailable(List<Transaction> fetchedDbTxns) {

        // all txns available now - either from files or DB or both.

        // make 'mWorkFragment.mLastFetchTransactions' refer to final list of txns
        if( !mTxnsFromCsv.isEmpty() &&
                fetchedDbTxns != null &&
                !fetchedDbTxns.isEmpty() ) {
            LogMy.d(TAG,"Merging records from CSV and DB: "+mTxnsFromCsv.size()+", "+fetchedDbTxns.size());
            // merge if both type of records available
            fetchedDbTxns.addAll(mTxnsFromCsv);

        } else if( fetchedDbTxns == null || fetchedDbTxns.isEmpty()) {
            LogMy.d(TAG,"Only records from CSV available: "+mTxnsFromCsv.size());
            fetchedDbTxns = mTxnsFromCsv;

        } else {
            LogMy.d(TAG,"Only records from DB available: "+fetchedDbTxns.size());
        }

        if(fetchedDbTxns!=null && !fetchedDbTxns.isEmpty()) {
            // Remove duplicates - this may happen due to some error in backend archive process
            // which can result same txns to be present in both the DB table and CSV file
            // A simpler way is to override equals() and hashcode() methods of Transaction class
            // however didn't want to loose the ability to export 'Transaction' class from Backendless and use as it is
            fetchedDbTxns = new ArrayList<>(MyTransaction.removeDuplicateTxns(fetchedDbTxns));
        }

        mCallback.onFinalTxnSetAvailable(fetchedDbTxns);
    }

    private String buildWhereClause() {
        StringBuilder whereClause = new StringBuilder();

        whereClause.append("create_time >= '").append(mFromDate.getTime()).append("'");
        // we used '<' and not '<='
        whereClause.append(" AND create_time < '").append(mToDate.getTime()).append("'");

        //whereClause.append(" AND archived = false");

        // customer and merchant id
        if(mCustomerId.length() == CommonConstants.MOBILE_NUM_LENGTH) {
            whereClause.append(" AND customer_id = '").append(mCustomerId).append("'");
        } else if(mCustomerId.length() == CommonConstants.CUSTOMER_INTERNAL_ID_LEN) {
            whereClause.append(" AND cust_private_id = '").append(mCustomerId).append("'");
        }
        if(mMerchantId!=null && !mMerchantId.isEmpty()) {
            whereClause.append(" AND merchant_id = '").append(mMerchantId).append("'");
        }

        LogMy.d(TAG,"whereClause: "+whereClause.toString());

        return whereClause.toString();
    }

    // process all files in 'mAllFiles' and add applicable CSV records in mWorkFragment.mFilteredCsvRecords
    private void processTxnFiles() throws Exception {
        mTxnsFromCsv.clear();

        boolean isCustomerFilter = (mCustomerId != null && mCustomerId.length() > 0);

        for(int i=0; i<mAllFiles.size(); i++) {
            try {
                byte[] bytes = AppCommonUtil.fileAsByteArray(mContext, mAllFiles.get(i));
                //LogMy.d(TAG,"Encoded "+mAllFiles.get(i)+": "+new String(bytes));
                //byte[] decodedBytes = Base64.decode(bytes, Base64.DEFAULT);
                //LogMy.d(TAG,"Decoded "+mAllFiles.get(i)+": "+new String(decodedBytes));
                //InputStream is = new ByteArrayInputStream(decodedBytes);
                InputStream is = new ByteArrayInputStream(bytes);
                BufferedReader bfReader = new BufferedReader(new InputStreamReader(is));

                int lineCnt = 0;
                String receiveString = "";
                while ( (receiveString = bfReader.readLine()) != null ) {
                    LogMy.d(TAG,"Read line: "+receiveString);
                    // Line 0 is header - ignore the same
                    if(lineCnt!=0) {
                        // ignore empty lines
                        if(receiveString.trim().isEmpty() || receiveString.equals(CommonConstants.NEWLINE_SEP)) {
                            LogMy.d(TAG, "Read empty line");
                        } else {
                            processTxnCsvRecord(receiveString, isCustomerFilter);
                        }
                    }
                    lineCnt++;
                }
                is.close();
                LogMy.d(TAG,"Processed "+lineCnt+" lines from "+mAllFiles.get(i));

            } catch (FileNotFoundException fnf) {
                // ignore it - if the file already in 'missing list'
                boolean fileAlreadyMissing = false;
                String missingFile = mAllFiles.get(i);
                for (String curVal : mMissingFiles){
                    if (curVal.endsWith(missingFile)){
                        fileAlreadyMissing = true;
                    }
                }
                if(fileAlreadyMissing) {
                    // file not found in backend also - so ignore the exception
                    LogMy.i(TAG,"File not found locally, but is not found in backend also: "+missingFile);
                } else {
                    LogMy.e(TAG,"Txn CSV file not found locally: "+missingFile);
                    throw fnf;
                }
            } catch (Exception e) {
                // Any other exception can be due to corrupted file
                // delete this file and throw exception - failure in 1 file means complete failure
                LogMy.e(TAG,"Exception while reading txn csv file: "+mAllFiles.get(i),e);
                mContext.deleteFile(mAllFiles.get(i));
                throw e;
            }
        }
    }

    private void processTxnCsvRecord(String csvString, boolean isCustomerFilter)  throws ParseException {
        Transaction txn = CsvConverter.txnFromCsvStr(csvString);

        if( !isCustomerFilter ||
                mCustomerId.equals(txn.getCust_mobile()) ||
                mCustomerId.equals(txn.getCust_private_id()) ) {

            mTxnsFromCsv.add(txn);
        }
    }

    public Date getTxnInDbFrom() {
        return mTxnInDbFrom;
    }
}
