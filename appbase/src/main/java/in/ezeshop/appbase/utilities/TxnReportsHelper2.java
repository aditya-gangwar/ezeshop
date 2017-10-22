package in.ezeshop.appbase.utilities;

import android.app.Activity;
import android.content.Context;

import com.backendless.exceptions.BackendlessException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import in.ezeshop.appbase.entities.MyTransaction;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.CsvConverter;
import in.ezeshop.common.DateUtil;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Transaction;

/**
 * Created by adgangwa on 28-10-2016.
 */
public class TxnReportsHelper2 {

    public interface TxnReportsHelper2If {
        void fetchTxnsFromDB(String whereClause);
        void fetchTxnFiles(List<String> missingFiles);
        void onFinalTxnSetAvailable(List<Transaction> allTxns);
    }

    private static final String TAG = "BaseApp-TxnReportsHelper2";

    private Context mContext;
    private TxnReportsHelper2If mCallback;

    //private Date mTxnInDbFrom;
    private Date mFromDate;
    private Date mToDate;
    private String mMerchantId;
    private String mCustomerId;
    private boolean mFetchByMchnt;
    private String mCurPeriodFile;
    private long mCurFileModTime;

    public List<String> mAllFiles = new ArrayList<>();
    public List<String> mMissingFiles = new ArrayList<>();
    public List<Transaction> mTxnsFromCsv = new ArrayList<>();


    public TxnReportsHelper2(Activity callingActivity) {
        mContext = callingActivity;
        mCallback = (TxnReportsHelper2If) callingActivity;
        //mTxnInDbFrom = getTxnInDbStartTime();
    }

    /*public static Date getTxnInDbStartTime() {
        // time after which txns should be in DB
        DateUtil now = new DateUtil(new Date(), TimeZone.getDefault());
        LogMy.d( TAG, "now: "+ String.valueOf(now.getTime().getTime()) );
        // -1 as 'today' is inclusive
        now.removeDays(MyGlobalSettings.getTxnsIntableKeepDays()-1);
        LogMy.d( TAG, "now: "+ String.valueOf(now.getTime().getTime()) );
        return now.toMidnight().getTime();
    }*/

    /*
     * If mFromDate < mTxnInDbFrom (means txns from CSV files required)
     *      fetch required files
     *      (later once all files are available - check if txns from DB also required)
     * Else
     *      fetch txns only from DB
     */
    public void startTxnFetch(Date from, Date to, String merchantId, String customerId, boolean fetchByMchnt) throws Exception{
        mFromDate = from;
        mToDate = to;
        //LogMy.d( TAG, "mTxnInDbFrom: "+ String.valueOf(mTxnInDbFrom.getTime()) );
        mMerchantId = merchantId;
        mCustomerId = customerId;
        mFetchByMchnt = fetchByMchnt;

        // reset all
        mTxnsFromCsv.clear();

        //if(mFromDate.getTime() < mTxnInDbFrom.getTime()) {
            // archived txns from CSV files are required
            if(mFetchByMchnt && (mMerchantId==null || mMerchantId.isEmpty()) ) {
                LogMy.e(TAG,"Merchant ID not available to fetch txn CSV files");
                throw new BackendlessException(String.valueOf(ErrorCodes.GENERAL_ERROR), "Merchant ID not available to fetch txn CSV files");
            }

            // find which txn csv files are not locally available
            if(mFetchByMchnt) {
                getTxnCsvFilePaths();
            } else {
                getCustTxnCsvFilePaths();
            }
            if (mMissingFiles.isEmpty()) {
                // all required txn files are locally available
                // process all files and store applicable CSV records in 'mWorkFragment.mFilteredCsvRecords'
                onAllTxnFilesAvailable(false);
            } else {
                // one or more txn files are not locally available, fetch the same from backend
                // all files will be processed in single go, after fetching missing files
                mCallback.fetchTxnFiles(mMissingFiles);;
            }
        /*} else {
            // txns only from DB required
            mCallback.fetchTxnsFromDB(buildWhereClause());
        }*/
    }

    // checks for txn files locally and sets 'mWorkFragment.mAllFiles' and 'mWorkFragment.mMissingFiles' accordingly
    private void getTxnCsvFilePaths() {
        // assuming 1 file per month
        // loop from 'from date month' to 'to date month' - checking if corresponding file is locally available or not

        // +1 as getMonth() starts with 0 for Jan
        int fromMonth = (new DateUtil(mFromDate, TimeZone.getDefault())).getMonth()+1;
        int toMonth = (new DateUtil(mToDate, TimeZone.getDefault())).getMonth()+1;
        int fromYear = (new DateUtil(mFromDate, TimeZone.getDefault())).getYear();
        int toYear = (new DateUtil(mToDate, TimeZone.getDefault())).getYear();
        //int currDay = (new DateUtil(new Date(), TimeZone.getDefault())).getDayOfMonth();

        mMissingFiles.clear();
        mAllFiles.clear();

        SimpleDateFormat formatter1=new SimpleDateFormat("dd/MM/yyyy");

        // build names of all required CSV files
        for(int y=fromYear; y<=toYear; y++) {
            int startMonth = (y==fromYear)?fromMonth:1;
            int endMonth = (y==toYear)?toMonth:12;
            String yearStr = String.format("%04d", y);

            for (int m = startMonth; m <= endMonth; m++) {
                String monthStr = String.format("%02d", m);

                String filename = CommonUtils.getTxnCsvFilename(monthStr, yearStr, mMerchantId);
                mAllFiles.add(filename);

                File file = mContext.getFileStreamPath(filename);
                if(file == null || !file.exists()) {
                    // file does not exist locally
                    LogMy.d(TAG,"Missing file: "+filename);
                    String filepath = CommonUtils.getMerchantTxnDir(mMerchantId) + CommonConstants.FILE_PATH_SEPERATOR + filename;
                    mMissingFiles.add(filepath);
                } else {
                    // check if local copy is latest or not
                    boolean latestCopy = true;
                    Date reqFileUpdateTime = null;

                    try {
                        if (y == toYear && m == toMonth) {
                            // current month file
                            // file update should be after requested 'to' time
                            mCurPeriodFile = filename;
                            reqFileUpdateTime = mToDate;
                            //reqFileUpdateTime = (new DateUtil(new Date(), TimeZone.getDefault())).toMidnight().getTime();
                        } else {
                            // old month file
                            // file update should be after next month's first day midnight
                            String nextDayDate = null;
                            if (m != 12) {
                                nextDayDate = "01/" + String.format("%02d", m + 1) + "/" + yearStr;
                            } else {
                                // Dec month - next month is 1 and add year also
                                nextDayDate = "01/01/" + String.format("%04d", y + 1);
                            }
                            reqFileUpdateTime = formatter1.parse(nextDayDate);
                        }

                        long fileModifyTime = getCsvModifyTime(file);
                        //if(file.lastModified() < reqFileUpdateTime.getTime()) {
                        if(fileModifyTime < (reqFileUpdateTime.getTime()/1000)) {
                            LogMy.d(TAG,"Local file modified time less than required: "+filename+","+fileModifyTime+","+(reqFileUpdateTime.getTime()/1000));
                            latestCopy = false;
                        }
                    } catch (Exception e) {
                        // Add to missing file list
                        LogMy.e(TAG,"Exception in TxnReportsHelper2, adding to missing file list: "+filename);
                        latestCopy = false;
                    }

                    if(!latestCopy) {
                        String filepath = CommonUtils.getMerchantTxnDir(mMerchantId) + CommonConstants.FILE_PATH_SEPERATOR + filename;
                        mMissingFiles.add(filepath);
                    }
                }
            }
        }

        LogMy.d(TAG,"mAllFiles: "+mAllFiles.toString());
        LogMy.d(TAG,"mMissingFiles: "+mMissingFiles.toString());

        // loop through the dates for which report is required
        // check if file against the date is available locally
        // if not, add in missing file list
        /*long diff = Math.abs(mTxnInDbFrom.getTime() - mFromDate.getTime());
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
        }*/
    }

    // checks for txn files locally and sets 'mWorkFragment.mAllFiles' and 'mWorkFragment.mMissingFiles' accordingly
    private void getCustTxnCsvFilePaths() {
        // assuming 1 file per year
        // loop from 'from date year' to 'to date year' - checking if corresponding file is locally available or not

        int fromYear = (new DateUtil(mFromDate, TimeZone.getDefault())).getYear();
        int toYear = (new DateUtil(mToDate, TimeZone.getDefault())).getYear();

        mMissingFiles.clear();
        mAllFiles.clear();

        SimpleDateFormat formatter1=new SimpleDateFormat("dd/MM/yyyy");

        // build names of all required CSV files
        for(int y=fromYear; y<=toYear; y++) {
            String yearStr = String.format("%04d", y);

            String filename = CommonUtils.getCustTxnCsvFilename(yearStr, mCustomerId);
            mAllFiles.add(filename);

            File file = mContext.getFileStreamPath(filename);
            if(file == null || !file.exists()) {
                // file does not exist locally
                LogMy.d(TAG,"Missing file: "+filename);
                String filepath = CommonUtils.getCustomerTxnDir(mCustomerId) + CommonConstants.FILE_PATH_SEPERATOR + filename;
                mMissingFiles.add(filepath);
            } else {
                // check if local copy is latest or not
                boolean latestCopy = true;
                Date reqFileUpdateTime = null;

                try {
                    if (y == toYear) {
                        // current year file
                        // file update should be after requested 'to' time
                        mCurPeriodFile = filename;
                        reqFileUpdateTime = mToDate;
                        //reqFileUpdateTime = (new DateUtil(new Date(), TimeZone.getDefault())).toMidnight().getTime();
                    } else {
                        // old year file
                        // file update should be after next year's first day midnight

                        String nextDayDate = "01/01" + String.format("%04d", y + 1);
                        reqFileUpdateTime = formatter1.parse(nextDayDate);
                    }

                    long fileModifyTime = getCsvModifyTime(file);
                    //if(file.lastModified() < reqFileUpdateTime.getTime()) {
                    if(fileModifyTime < (reqFileUpdateTime.getTime()/1000)) {
                        LogMy.d(TAG,"Local file modified time less than required: "+filename+","+fileModifyTime+","+(reqFileUpdateTime.getTime()/1000));
                        latestCopy = false;
                    }
                } catch (Exception e) {
                    // Add to missing file list
                    LogMy.e(TAG,"Exception in TxnReportsHelper2, adding to missing file list: "+filename);
                    latestCopy = false;
                }

                if(!latestCopy) {
                    String filepath = CommonUtils.getCustomerTxnDir(mCustomerId) + CommonConstants.FILE_PATH_SEPERATOR + filename;
                    mMissingFiles.add(filepath);
                }
            }
        }

        LogMy.d(TAG,"mAllFiles: "+mAllFiles.toString());
        LogMy.d(TAG,"mMissingFiles: "+mMissingFiles.toString());
    }

    private long getCsvModifyTime(File file) {
        long modifyTime = 0;
        try {
            FileInputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            int lineCnt = 0;
            String receiveString = "";
            while ( (receiveString = reader.readLine()) != null ) {
                // ignore empty lines
                if(receiveString.trim().isEmpty() || receiveString.equals(CommonConstants.NEWLINE_SEP)) {
                    LogMy.d(TAG, "Read empty line");
                    continue;
                }

                //LogMy.d(TAG,"Read line: "+receiveString);
                if(lineCnt==0) {
                    modifyTime = Long.parseLong(receiveString);
                } else {
                    break;
                }
                lineCnt++;
            }
            is.close();

        } catch (Exception e) {
            LogMy.e(TAG,"Failed to read modify time from CSV file"+file.getAbsolutePath(),e);
            modifyTime = 0;
        }
        return modifyTime;
    }

    // this function is called for further processing,
    // when all CSV txn files for old days are available locally
    public void onAllTxnFilesAvailable(boolean remoteCsvTxnFileNotFound) throws Exception {
        // process CSV files to extract applicable CSV records
        processTxnFiles();

        // For now - always check for txns in DB
        //mCallback.fetchTxnsFromDB(buildWhereClause());

        // check if records from DB table are to be fetched too
        //if( mToDate.getTime() >= mTxnInDbFrom.getTime() || remoteCsvTxnFileNotFound ) {
        if( (mToDate.getTime()/1000) >= mCurFileModTime || remoteCsvTxnFileNotFound ) {
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

        whereClause.append(" AND archived = false");

        // customer and merchant id
        /*if(mCustomerId.length() == CommonConstants.MOBILE_NUM_LENGTH) {
            whereClause.append(" AND customer_id = '").append(mCustomerId).append("'");
        } else if(mCustomerId.length() == CommonConstants.CUSTOMER_INTERNAL_ID_LEN) {
            whereClause.append(" AND cust_private_id = '").append(mCustomerId).append("'");
        }*/
        if(mCustomerId!=null && !mCustomerId.isEmpty()) {
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

        //boolean isCustomerFilter = (mCustomerId != null && mCustomerId.length() > 0);

        boolean isFilter;
        if(mFetchByMchnt) {
            isFilter = (mCustomerId != null && mCustomerId.length() > 0);
        } else {
            isFilter = (mMerchantId != null && mMerchantId.length() > 0);
        }

        FileInputStream is;
        BufferedReader bfReader;

        for(int i=0; i<mAllFiles.size(); i++) {
            try {
                //byte[] bytes = AppCommonUtil.fileAsByteArray(mContext, mAllFiles.get(i));
                //LogMy.d(TAG,"Encoded "+mAllFiles.get(i)+": "+new String(bytes));
                //byte[] decodedBytes = Base64.decode(bytes, Base64.DEFAULT);
                //LogMy.d(TAG,"Decoded "+mAllFiles.get(i)+": "+new String(decodedBytes));

                //InputStream is = new ByteArrayInputStream(bytes);
                //BufferedReader bfReader = new BufferedReader(new InputStreamReader(is));

                is = mContext.openFileInput(mAllFiles.get(i));
                bfReader = new BufferedReader(new InputStreamReader(is));

                int lineCnt = 0;
                String receiveString = "";
                while ( (receiveString = bfReader.readLine()) != null ) {
                    // ignore empty lines
                    if(receiveString.trim().isEmpty() || receiveString.equals(CommonConstants.NEWLINE_SEP)) {
                        LogMy.d(TAG, "Read empty line");
                        continue;
                    }

                    //LogMy.d(TAG,"Read line: "+receiveString);
                    // first line is modify time
                    if(lineCnt!=0) {
                        processTxnCsvRecord(receiveString, isFilter);
                    } else {
                        if(mCurPeriodFile.equals(mAllFiles.get(i))) {
                            // store current month file's modify time
                            try {
                                mCurFileModTime = Long.parseLong(receiveString);
                            } catch (Exception e) {
                                LogMy.e(TAG,"Failed to read modify time from CSV file"+mAllFiles.get(i),e);
                                mCurFileModTime = 0;
                            }
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

    private void processTxnCsvRecord(String csvString, boolean isFilter)  throws ParseException {
        Transaction txn = CsvConverter.txnFromCsvStr(csvString);

        if( txn.getCreate_time().getTime() > mFromDate.getTime()
                && txn.getCreate_time().getTime() < mToDate.getTime()) {
            if(isFilter) {
                if( (mFetchByMchnt && mCustomerId.equals(txn.getCust_private_id()))
                || (!mFetchByMchnt && mMerchantId.equals(txn.getMerchant_id())) ) {
                    mTxnsFromCsv.add(txn);
                }
            } else {
                mTxnsFromCsv.add(txn);
            }
        }

        /*if( txn.getCreate_time().getTime() > mFromDate.getTime()
                && txn.getCreate_time().getTime() < mToDate.getTime()
                && (!isCustomerFilter || mCustomerId.equals(txn.getCust_private_id()))
                ) {
            mTxnsFromCsv.add(txn);
        }*/

        /*if( !isCustomerFilter ||
                mCustomerId.equals(txn.getCust_mobile()) ||
                mCustomerId.equals(txn.getCust_private_id()) ) {

            mTxnsFromCsv.add(txn);
        }*/
    }
}
