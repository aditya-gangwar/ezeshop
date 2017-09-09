package in.ezeshop.internal;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import in.ezeshop.internal.entities.AgentUser;
import in.ezeshop.internal.helper.MyRetainedFragment;
import in.ezeshop.appbase.PasswdChangeDialog;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.common.DateUtil;
import in.ezeshop.common.MyGlobalSettings;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.customerbase.CashbackActivityCust;
import in.ezeshop.customerbase.entities.CustomerUser;
import in.ezeshop.merchantbase.CashbackActivity;
import in.ezeshop.merchantbase.entities.MerchantUser;

import java.util.ArrayList;

/**
 * Created by adgangwa on 18-07-2016.
 */
public class ActionsActivity extends AppCompatActivity implements
        MyRetainedFragment.RetainedFragmentIf, DialogFragmentWrapper.DialogFragmentWrapperIf,
        ActionsFragment.ActionsFragmentIf, PasswdChangeDialog.PasswdChangeDialogIf,
        SearchMerchantDialog.SearchMerchantDialogIf, MerchantDetailsFragment.MerchantDetailsFragmentIf,
        DisableMchntDialog.DisableMchntDialogIf, SearchCustomerDialog.SearchCustomerDialogIf,
        CustomerDetailsFragment.CustomerDetailsFragmentIf, DisableCustDialog.DisableCustDialogIf,
        GlobalSettingsListFrag.GlobalSettingsListFragIf
{

    private static final String TAG = "AgentApp-ActionsActivity";
    private static final String RETAINED_FRAGMENT = "retainedFragActions";
    private static final String ACTIONS_FRAGMENT = "actionsFragment";
    private static final String MCHNT_DETAILS_FRAGMENT = "mchntDetailsFragment";
    private static final String CUST_DETAILS_FRAGMENT = "custDetailsFragment";
    //private static final String SEARCH_ORDERS_FRAGMENT = "searchOrdersFragment";
    //private static final String CARD_DETAILS_FRAGMENT = "cardDetailsFragment";
    private static final String SETTINGS_LIST_FRAGMENT = "settingsListFragment";
    //private static final String CARDS_ACTIONS_LIST_FRAGMENT = "cardsActionListFragment";
    //private static final String MCHNT_ORDERS_FRAGMENT = "MchntOrdersFragment";
    //private static final String ORDER_DETAILS_FRAGMENT = "orderDetailsFragment";
    //private static final String ORDER_STATUS_FRAGMENT = "orderStatusFragment";

    private static final String DIALOG_BACK_BUTTON = "dialogBackButton";
    private static final String DIALOG_CHANGE_PASSWORD = "dialogChangePassword";
    private static final String DIALOG_SEARCH_MCHNT = "searchMchnt";
    private static final String DIALOG_SEARCH_CUSTOMER = "searchCustomer";
    //private static final String DIALOG_SEARCH_CARD = "searchCard";
    //private static final String DIALOG_CARD_LIST = "dialogCardList";
    private static final String DIALOG_DEL_DUMMY_DATA = "dialogDelDummyData";

    private static final String DIALOG_SESSION_TIMEOUT = "dialogSessionTimeout";

    // this will never be null, as it only gets destroyed with cashback activity itself
    private ActionsFragment mActionsFragment;
    //private CardsActionListFrag mCardsActionFragment;

    private FragmentManager mFragMgr;
    private MyRetainedFragment mWorkFragment;
    //private AgentUser mUser;

    boolean mExitAfterLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions);

        //mUser = AgentUser.getInstance();

        if(savedInstanceState==null) {
            initToolbar(true);
        }

        // Initialize retained fragment before other fragments
        // Check to see if we have retained the worker fragment.
        mFragMgr = getFragmentManager();
        mWorkFragment = (MyRetainedFragment)mFragMgr.findFragmentByTag(RETAINED_FRAGMENT);
        // If not retained (or first time running), we need to create it.
        if (mWorkFragment == null) {
            LogMy.d(TAG, "Creating retained fragment instance");
            mWorkFragment = new MyRetainedFragment();
            mFragMgr.beginTransaction().add(mWorkFragment, RETAINED_FRAGMENT).commit();
        }

        startActionsFragment();
    }

    private void initToolbar(boolean isNewActivity) {
        LogMy.d(TAG, "In initToolbar");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actions);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);

        if(isNewActivity) {
            getSupportActionBar().setTitle("Home");
        }
    }

    @Override
    public void onBgProcessResponse(int errorCode, int operation) {
        // Session timeout case - show dialog and logout - irrespective of invoked operation
        if(errorCode==ErrorCodes.SESSION_TIMEOUT || errorCode==ErrorCodes.NOT_LOGGED_IN) {
            AppCommonUtil.cancelProgressDialog(true);
            DialogFragmentWrapper.createNotification(AppConstants.notLoggedInTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                    .show(mFragMgr, DIALOG_SESSION_TIMEOUT);
            return;
        }

        try {
            switch(operation) {
                case MyRetainedFragment.REQUEST_LOGOUT:
                    AppCommonUtil.cancelProgressDialog(true);
                    AgentUser.reset();
                    //Start Login Activity
                    if(!mExitAfterLogout) {
                        Intent intent = new Intent( this, LoginActivity.class );
                        // clear this activity from backstack
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    finish();
                    break;

                case MyRetainedFragment.REQUEST_CHANGE_PASSWD:
                    AppCommonUtil.cancelProgressDialog(true);
                    if(errorCode==ErrorCodes.NO_ERROR) {
                        DialogFragmentWrapper.createNotification(AppConstants.pwdChangeSuccessTitle, AppConstants.pwdChangeSuccessMsg, false, false)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                        logoutAgent();
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;

                case MyRetainedFragment.REQUEST_SEARCH_MERCHANT:
                    AppCommonUtil.cancelProgressDialog(true);
                    if(errorCode==ErrorCodes.NO_ERROR) {
                        mWorkFragment.mCurrMerchant = MerchantUser.getInstance().getMerchant();
                        startMchntDetailsFragment();
                        // show merchant details
                        /*
                        mWorkFragment.mCurrMerchant = MerchantUser.getInstance().getMerchant();
                        MerchantDetailsFragment dialog = new MerchantDetailsFragment();
                        dialog.show(getFragmentManager(), DIALOG_MCHNT_DETAILS);*/
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;

                case MyRetainedFragment.REQUEST_DISABLE_MERCHANT:
                    AppCommonUtil.cancelProgressDialog(true);
                    if(errorCode==ErrorCodes.NO_ERROR) {
                        DialogFragmentWrapper.createNotification(AppConstants.defaultSuccessTitle, AppConstants.merchantDisableSuccessMsg, false, false)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;

                case MyRetainedFragment.REQUEST_SEARCH_CUSTOMER:
                    AppCommonUtil.cancelProgressDialog(true);
                    if(errorCode==ErrorCodes.NO_ERROR) {
                        mWorkFragment.mCurrCustomer = CustomerUser.getInstance().getCustomer();
                        startCustDetailsFragment();
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;

                case MyRetainedFragment.REQUEST_DISABLE_CUSTOMER:
                    AppCommonUtil.cancelProgressDialog(true);
                    if(errorCode==ErrorCodes.NO_ERROR) {
                        DialogFragmentWrapper.createNotification(AppConstants.defaultSuccessTitle, AppConstants.customerDisableSuccessMsg, false, false)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;

                case MyRetainedFragment.REQUEST_LIMIT_CUST_ACC:
                    AppCommonUtil.cancelProgressDialog(true);
                    if(errorCode==ErrorCodes.NO_ERROR) {
                        String msg = String.format(AppConstants.customerLimitedSuccessMsg, MyGlobalSettings.getCustAccLimitModeMins());
                        DialogFragmentWrapper.createNotification(AppConstants.defaultSuccessTitle, msg, false, false)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;

                /*case MyRetainedFragment.REQUEST_SEARCH_CARD:
                    AppCommonUtil.cancelProgressDialog(true);
                    if(errorCode==ErrorCodes.NO_ERROR) {
                        startCardDetailsFragment();
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;

                case MyRetainedFragment.REQUEST_ACTION_CARDS:
                    AppCommonUtil.cancelProgressDialog(true);
                    if(errorCode==ErrorCodes.NO_ERROR) {
                        if(!mWorkFragment.cardNumFetched) {
                            mWorkFragment.cardNumFetched = true;
                        }
                        // null shud work fine - as its expected that the fragment will be still open
                        // and it will have 'actio' stored in its arguments
                        startCardActionListFrag(null);
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;

                case MyRetainedFragment.REQUEST_DISABLE_CUST_CARD:
                    AppCommonUtil.cancelProgressDialog(true);
                    if(errorCode==ErrorCodes.NO_ERROR) {
                        DialogFragmentWrapper.createNotification(AppConstants.defaultSuccessTitle, AppConstants.custCardDisableSuccessMsg, false, false)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;*/

                /*case MyRetainedFragment.REQUEST_SEARCH_MCHNT_ORDER:
                    AppCommonUtil.cancelProgressDialog(true);
                    if(errorCode==ErrorCodes.NO_ERROR) {
                        startMchntOrdersFragment();
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;

                case MyRetainedFragment.REQUEST_FETCH_ALLOTTED_CARDS:
                    AppCommonUtil.cancelProgressDialog(true);
                    if(errorCode==ErrorCodes.NO_ERROR) {
                        CardListDialog dialog = new CardListDialog();
                        dialog.show(getFragmentManager(), DIALOG_CARD_LIST);
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;

                case MyRetainedFragment.REQUEST_CHANGE_ORDER_STATUS:
                    AppCommonUtil.cancelProgressDialog(true);
                    if(errorCode==ErrorCodes.NO_ERROR) {
                        DialogFragmentWrapper.createNotification(AppConstants.defaultSuccessTitle, "Order Updated Successfully", false, false)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;*/

                case MyRetainedFragment.REQUEST_DEL_DUMMY_DATA:
                    AppCommonUtil.cancelProgressDialog(true);
                    if(errorCode==ErrorCodes.NO_ERROR) {
                        DialogFragmentWrapper.createNotification(AppConstants.defaultSuccessTitle, "Dummy Data Deleted Successfully", false, false)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    } else {
                        DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                                .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
                    }
                    break;
            }
        } catch (Exception e) {
            AppCommonUtil.cancelProgressDialog(true);
            LogMy.e(TAG, "Exception in CashbackActivity:onBgProcessResponse: "+operation+": "+errorCode, e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), false, true)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    @Override
    public void onDialogResult(String tag, int indexOrResultCode, ArrayList<Integer> selectedItemsIndexList) {
        LogMy.d(TAG,"In onDialogResult: "+tag);
        if (tag.equals(DIALOG_BACK_BUTTON)) {
            mExitAfterLogout = true;
            // delete all app memory data
            /*if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                if(!((ActivityManager)getSystemService(ACTIVITY_SERVICE))
                        .clearApplicationUserData()) {
                    LogMy.w(TAG,"Failed to clear application user data");
                }
            } else {
                LogMy.e(TAG,"Not clearing cache data - as API level is below 19.");
            }*/
            // delete all internal files every 10 days
            DateUtil now = new DateUtil();
            if(now.getDayOfMonth()%10==0) {
                AppCommonUtil.delAllInternalFiles(this);
            }
            logoutAgent();
        }  else if (tag.equals(DIALOG_SESSION_TIMEOUT)) {
            mExitAfterLogout = false;
            logoutAgent();
        } else if(tag.equals(DIALOG_DEL_DUMMY_DATA)) {
            AppCommonUtil.showProgressDialog(this, "Deleting ...");
            mWorkFragment.delDummyData();
        }
    }

    private void logoutAgent() {
        int resultCode = AppCommonUtil.isNetworkAvailableAndConnected(this);
        if ( resultCode != ErrorCodes.NO_ERROR) {
            // Show error notification dialog
            DialogFragmentWrapper.createNotification(AppConstants.noInternetTitle, AppCommonUtil.getErrorDesc(resultCode), false, true)
                    .show(mFragMgr, DialogFragmentWrapper.DIALOG_NOTIFICATION);
            //TODO: do handling here
        } else {
            // show progress dialog
            AppCommonUtil.showProgressDialog(this, AppConstants.progressLogout);
            mWorkFragment.logoutAgent();
        }
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        LogMy.d(TAG, "In onBackPressed: " + count);

        /*
        if (count == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStackImmediate();
        }*/

        if (mActionsFragment.isVisible()) {
            DialogFragmentWrapper.createConfirmationDialog(AppConstants.exitGenTitle, AppConstants.exitAppMsg, false, false)
                    .show(mFragMgr, DIALOG_BACK_BUTTON);
        } else {
            mFragMgr.popBackStackImmediate();
            if(mActionsFragment.isVisible()) {
                LogMy.d(TAG,"Actions fragment visible");
                //getReadyForNewTransaction();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.agent_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * react to the user tapping the back/up icon in the action bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                onBackPressed();
                break;
            case R.id.action_change_passwd:
                PasswdChangeDialog dialog = new PasswdChangeDialog();
                dialog.show(getFragmentManager(), DIALOG_CHANGE_PASSWORD);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public MyRetainedFragment getRetainedFragment() {
        return mWorkFragment;
    }

    @Override
    public void onPasswdChangeData(String oldPasswd, String newPassword) {
        AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
        mWorkFragment.changePassword(oldPasswd, newPassword);
    }

    @Override
    public void disableMerchant(String ticketId, String reason, String remarks) {
        AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
        mWorkFragment.disableMerchant(ticketId, reason, remarks);
    }

    @Override
    public void disableCustomer(boolean isLtdMode, String ticketId, String reason, String remarks) {
        AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
        mWorkFragment.disableCustomer(isLtdMode, ticketId, reason, remarks);
    }

    /*@Override
    public void disableCard(String ticketId, String reason, String remarks) {
        AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
        mWorkFragment.disableCustCard(ticketId, reason, remarks);
    }*/

    @Override
    public void launchMchntApp() {
        // pseudo login done - launch cashback activity
        //Start Cashback Activity
        Intent intent = new Intent( this, CashbackActivity.class );
        intent.putExtra(CashbackActivity.INTENT_EXTRA_USER_TOKEN, AgentUser.getInstance().getUserToken());
        // clear Login activity from backstack
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void launchCustApp() {
        // pseudo login done - launch cashback activity
        //Start Cashback Activity
        Intent intent = new Intent( this, CashbackActivityCust.class );
        intent.putExtra(CashbackActivityCust.INTENT_EXTRA_USER_TOKEN, AgentUser.getInstance().getUserToken());
        // clear Login activity from backstack
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onActionBtnClick(String action) {
        LogMy.d(TAG,"In onActionBtnClick: "+action);
        switch(action) {
            case ActionsFragment.MERCHANT_REGISTER:
                startMerchantRegisterActivity();
                break;
            case ActionsFragment.MERCHANT_SEARCH:
                SearchMerchantDialog dialog = new SearchMerchantDialog();
                dialog.show(getFragmentManager(), DIALOG_SEARCH_MCHNT);
                break;
            case ActionsFragment.CUSTOMER_SEARCH:
                SearchCustomerDialog custDialog = new SearchCustomerDialog();
                custDialog.show(getFragmentManager(), DIALOG_SEARCH_CUSTOMER);
                break;
            /*case ActionsFragment.MCHNT_ORDER_SEARCH:
                // clear previous values
                if(mWorkFragment.mSelectedStatus==null) {
                    mWorkFragment.mSelectedStatus = new ArrayList<>();
                } else {
                    mWorkFragment.mSelectedStatus.clear();
                }
                mWorkFragment.mMchntOrderId = null;
                mWorkFragment.mMchntIdForOrder = null;
                mWorkFragment.mCurrOrder =  null;

                startSearchOrdersFragment();
                break;
            case ActionsFragment.CARDS_SEARCH:
                SearchCardDialog cardDialog = new SearchCardDialog();
                cardDialog.show(getFragmentManager(), DIALOG_SEARCH_CARD);
                break;
            case ActionsFragment.CARDS_UPLOAD:
            ///case ActionsFragment.CARDS_ALLOT_AGENT:
            //case ActionsFragment.CARDS_ALLOT_MCHNT:
            //case ActionsFragment.CARDS_RETURN_MCHNT:
            //case ActionsFragment.CARDS_RETURN_AGENT:
                // initialize relevant data members first
                if(mWorkFragment.mLastCardsForAction==null) {
                    mWorkFragment.mLastCardsForAction = new ArrayList<>();
                } else {
                    mWorkFragment.mLastCardsForAction.clear();
                }
                mWorkFragment.cardNumFetched = false;
                startCardActionListFrag(action);
                break;*/
            case ActionsFragment.OTHER_GLOBAL_SETTINGS:
                startSettingsListFrag();
                break;
            case ActionsFragment.OTHER_CLEAR_DUMMY_DATA:
                DialogFragmentWrapper.createConfirmationDialog("Confirm Clear Data", "Are you sure to Delete Data for Dummy Merchant ?", false, false)
                        .show(mFragMgr, DIALOG_DEL_DUMMY_DATA);
                break;

        }
    }

    /*@Override
    public void startOrderStatusChange() {
        startOrderStatusChangeFrag();
    }

    @Override
    public void changeOrderStatus(MerchantOrders updatedOrder) {
        AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
        mWorkFragment.changeOrderStatus(updatedOrder);
    }

    @Override
    public void showOrderDetails(int pos) {
        mWorkFragment.mCurrOrder = mWorkFragment.mLastFetchMchntOrders.get(pos);
        startOrderDetailsFrag();
    }*/

    /*@Override
    public void allocateCards(boolean removeCase) {
        if(mWorkFragment.mLastCardsForAction==null) {
            mWorkFragment.mLastCardsForAction = new ArrayList<>();
        } else {
            mWorkFragment.mLastCardsForAction.clear();
        }
        mWorkFragment.cardNumFetched = false;

        if(removeCase) {
            startCardActionListFrag(ActionsFragment.CARDS_RETURN_MCHNT);
        } else {
            startCardActionListFrag(ActionsFragment.CARDS_ALLOT_MCHNT);
        }
    }

    @Override
    public void searchMchntOrder() {
        AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
        mWorkFragment.searchMchntOrder();
    }

    @Override
    public void execActionForCards(String cards, String action) {
        AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
        mWorkFragment.execActionForCards(cards, ActionsFragment.cardsActionCodeMap.get(action));
    }

    @Override
    public void fetchAllottedCards(String orderId) {
        AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
        mWorkFragment.fetchAllottedCards(orderId);
    }

    @Override
    public void showCardDetails(String cardId) {
        onCardInputData(cardId);
    }*/

    @Override
    public void onInputData(String value, boolean searchById) {
        if(value!=null && !value.isEmpty()) {
            AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
            mWorkFragment.mCurrMerchant = null;
            mWorkFragment.searchMerchant(value, searchById);
        }
    }

    @Override
    public void onCustInputData(String value, boolean searchById) {
        if(value!=null && !value.isEmpty()) {
            AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
            mWorkFragment.mCurrCustomer = null;
            mWorkFragment.searchCustomer(value, searchById);
        }
    }

    /*@Override
    public void onCardInputData(String id) {
        if(id!=null && !id.isEmpty()) {
            AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
            mWorkFragment.mCurrMemberCard = null;
            mWorkFragment.searchMemberCard(id);
        }
    }*/



    /*
     * Activity and Fragment start fxs
     */
    private void startActionsFragment() {
        if (mFragMgr.findFragmentByTag(ACTIONS_FRAGMENT) == null) {
            //setDrawerState(false);
            mActionsFragment = new ActionsFragment();
            mFragMgr.beginTransaction()
                    .add(R.id.fragment_container, mActionsFragment, ACTIONS_FRAGMENT)
                    .addToBackStack(ACTIONS_FRAGMENT)
                    .commit();
        }
    }

    private void startMchntDetailsFragment() {
        if (mFragMgr.findFragmentByTag(MCHNT_DETAILS_FRAGMENT) == null) {
            LogMy.d(TAG, "Creating new mchnt details fragment");
            Fragment fragment = new MerchantDetailsFragment();
            FragmentTransaction transaction = mFragMgr.beginTransaction();

            // Add over the existing fragment
            transaction.replace(R.id.fragment_container, fragment, MCHNT_DETAILS_FRAGMENT);
            transaction.addToBackStack(MCHNT_DETAILS_FRAGMENT);

            // Commit the transaction
            transaction.commit();
        }
    }

    private void startCustDetailsFragment() {
        if (mFragMgr.findFragmentByTag(CUST_DETAILS_FRAGMENT) == null) {
            LogMy.d(TAG, "Creating new customer details fragment");
            Fragment fragment = new CustomerDetailsFragment();
            FragmentTransaction transaction = mFragMgr.beginTransaction();

            // Add over the existing fragment
            transaction.replace(R.id.fragment_container, fragment, CUST_DETAILS_FRAGMENT);
            transaction.addToBackStack(CUST_DETAILS_FRAGMENT);

            // Commit the transaction
            transaction.commit();
        }
    }

    /*private void startSearchOrdersFragment() {
        if (mFragMgr.findFragmentByTag(SEARCH_ORDERS_FRAGMENT) == null) {
            Fragment fragment = new SearchMchntOrderFrag();
            FragmentTransaction transaction = mFragMgr.beginTransaction();

            // Add over the existing fragment
            transaction.replace(R.id.fragment_container, fragment, SEARCH_ORDERS_FRAGMENT);
            transaction.addToBackStack(SEARCH_ORDERS_FRAGMENT);

            // Commit the transaction
            transaction.commit();
        }
    }*/

    /*private void startCardDetailsFragment() {
        if (mFragMgr.findFragmentByTag(CARD_DETAILS_FRAGMENT) == null) {
            LogMy.d(TAG, "Creating new card details fragment");
            Fragment fragment = new CardDetailsFragment();
            FragmentTransaction transaction = mFragMgr.beginTransaction();

            // Add over the existing fragment
            transaction.replace(R.id.fragment_container, fragment, CARD_DETAILS_FRAGMENT);
            transaction.addToBackStack(CARD_DETAILS_FRAGMENT);

            // Commit the transaction
            transaction.commit();
        }
    }*/

    private void startSettingsListFrag() {
        if (mFragMgr.findFragmentByTag(SETTINGS_LIST_FRAGMENT) == null) {
            LogMy.d(TAG, "Creating new setiings list fragment");
            Fragment fragment = new GlobalSettingsListFrag();
            FragmentTransaction transaction = mFragMgr.beginTransaction();

            // Add over the existing fragment
            transaction.replace(R.id.fragment_container, fragment, SETTINGS_LIST_FRAGMENT);
            transaction.addToBackStack(SETTINGS_LIST_FRAGMENT);

            // Commit the transaction
            transaction.commit();
        }
    }

    /*private void startCardActionListFrag(String action) {

        mCardsActionFragment = (CardsActionListFrag) mFragMgr.findFragmentByTag(CARDS_ACTIONS_LIST_FRAGMENT);
        if ( mCardsActionFragment == null) {
            LogMy.d(TAG, "Creating new card action list fragment");
            mCardsActionFragment = CardsActionListFrag.getInstance(action);
            FragmentTransaction transaction = mFragMgr.beginTransaction();

            // Add over the existing fragment
            transaction.replace(R.id.fragment_container, mCardsActionFragment, CARDS_ACTIONS_LIST_FRAGMENT);
            transaction.addToBackStack(CARDS_ACTIONS_LIST_FRAGMENT);

            // Commit the transaction
            transaction.commit();

        } else if(mCardsActionFragment.isVisible()){
            LogMy.d(TAG,"CardsActionListFrag already available and visible");
            mCardsActionFragment.updateUI();
        }
    }

    private void startMchntOrdersFragment() {
        if (mFragMgr.findFragmentByTag(MCHNT_ORDERS_FRAGMENT) == null) {

            Fragment fragment = new MchntOrderListFragInternal();
            FragmentTransaction transaction = mFragMgr.beginTransaction();

            // Add over the existing fragment
            transaction.replace(R.id.fragment_container, fragment, MCHNT_ORDERS_FRAGMENT);
            transaction.addToBackStack(MCHNT_ORDERS_FRAGMENT);

            // Commit the transaction
            transaction.commit();
        }
    }

    private void startOrderDetailsFrag() {
        if (mFragMgr.findFragmentByTag(ORDER_DETAILS_FRAGMENT) == null) {
            //Fragment fragment = OrderDetailsFrag.getInstance(orderID);
            Fragment fragment = new OrderDetailsFrag();
            FragmentTransaction transaction = mFragMgr.beginTransaction();

            // Add over the existing fragment
            transaction.replace(R.id.fragment_container, fragment, ORDER_DETAILS_FRAGMENT);
            transaction.addToBackStack(ORDER_DETAILS_FRAGMENT);

            // Commit the transaction
            transaction.commit();
        }
    }

    private void startOrderStatusChangeFrag() {
        if (mFragMgr.findFragmentByTag(ORDER_STATUS_FRAGMENT) == null) {
            //Fragment fragment = OrderDetailsFrag.getInstance(orderID);
            Fragment fragment = new OrderStatusChangeFrag();
            FragmentTransaction transaction = mFragMgr.beginTransaction();

            // Add over the existing fragment
            transaction.replace(R.id.fragment_container, fragment, ORDER_STATUS_FRAGMENT);
            transaction.addToBackStack(ORDER_STATUS_FRAGMENT);

            // Commit the transaction
            transaction.commit();
        }
    }*/



    private void startMerchantRegisterActivity() {
        Intent registrationIntent = new Intent( this, RegisterMerchantActivity.class );
        startActivity(registrationIntent);
    }

    @Override
    public void onBgThreadCreated() {
        // nothing to do
    }

    @Override
    protected void onResume() {
        LogMy.d(TAG, "In onResume");
        super.onResume();
        if(getFragmentManager().getBackStackEntryCount()==0) {
            // no fragment in backstack - so flag wont get set by any fragment - so set it here
            // though this shud never happen - as CashbackActivity always have a fragment
            mWorkFragment.setResumeOk(true);
        }
        if(AppCommonUtil.getProgressDialogMsg()!=null) {
            AppCommonUtil.showProgressDialog(this, AppCommonUtil.getProgressDialogMsg());
        }
        AppCommonUtil.setUserType(DbConstants.USER_TYPE_CC);
    }

    @Override
    protected void onPause() {
        LogMy.d(TAG,"In onPause: ");
        super.onPause();
        // no need to do this in each fragment - as activity onPause will always get called
        mWorkFragment.setResumeOk(false);
        AppCommonUtil.cancelProgressDialog(false);
    }

}

