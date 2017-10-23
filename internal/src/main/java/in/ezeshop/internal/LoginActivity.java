package in.ezeshop.internal;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import in.ezeshop.appbase.utilities.BackgroundProcessor;
import in.ezeshop.internal.entities.AgentUser;
import in.ezeshop.internal.helper.MyRetainedFragment;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.AppAlarms;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.appbase.utilities.RootUtil;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.ValidationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements
        MyRetainedFragment.RetainedFragmentIf, DialogFragmentWrapper.DialogFragmentWrapperIf,
        PasswdResetDialog.PasswdResetDialogIf {

    private static final String TAG = "AgentApp-LoginActivity";

    private static final String RETAINED_FRAGMENT_TAG = "workLogin";
    private static final String DIALOG_PASSWD_RESET = "dialogPaswdReset";

    public static final String PREF_INSTANCE_ID = "agentInstanceId";

    // permission request codes need to be < 256
    private static final int RC_HANDLE_WRITE_STORAGE_PERM = 10;
    private static final int RC_HANDLE_READ_STORAGE_PERM = 11;
    private static final int RC_HANDLE_CAMERA_PERM = 12;
    private static final int RC_HANDLE_LOCATION_FINE = 13;


    MyRetainedFragment      mWorkFragment;
    private String          mPassword;
    private String          mLoginId;
    private String          mInstanceId;
    boolean                 mProcessingResetPasswd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        // setting off fullscreen mode as 'screen pan on keyboard' doesnt work fine with fullscreen
        // http://stackoverflow.com/questions/7417123/android-how-to-adjust-layout-in-full-screen-mode-when-softkeyboard-is-visible
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);
        // show the keyboard and adjust screen for the same
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // local activity initializations
        bindUiResources();
        makeForgotPasswordLink();

        // Check to see if we have retained the worker fragment.
        FragmentManager fm = getFragmentManager();
        mWorkFragment = (MyRetainedFragment) fm.findFragmentByTag(RETAINED_FRAGMENT_TAG);
        // If not retained (or first time running), we need to create it.
        if (mWorkFragment == null) {
            LogMy.d(TAG, "Creating retained fragment");
            mWorkFragment = new MyRetainedFragment();
            fm.beginTransaction().add(mWorkFragment, RETAINED_FRAGMENT_TAG).commit();
        }

        // Fill old login id
        String oldId = PreferenceManager.getDefaultSharedPreferences(this).getString(AppConstants.PREF_LOGIN_ID, null);
        if( oldId != null) {
            mIdTextRes.setText(oldId);
            mPasswdTextRes.requestFocus();
        }

        // Get instanceId
        mInstanceId = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_INSTANCE_ID, null);
        if( mInstanceId == null) {
            LogMy.d(TAG,"Creating new instance id for agent");
            // first run - generate and store
            // TODO: for production use randomUUID - instead of deviceId
            //mInstanceId = UUID.randomUUID().toString();
            mInstanceId = AppCommonUtil.getDeviceId(this);
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString(PREF_INSTANCE_ID, mInstanceId)
                    .apply();
        }

        mPasswdTextRes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mLoginButton.performClick();
                    return true;
                }
                return false;
            }
        });

        mLoginButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                // check internet connectivity
                int resultCode = AppCommonUtil.isNetworkAvailableAndConnected(LoginActivity.this);
                if (resultCode != ErrorCodes.NO_ERROR) {
                    // Show error notification dialog
                    DialogFragmentWrapper.createNotification(AppConstants.noInternetTitle, AppCommonUtil.getErrorDesc(resultCode), false, true)
                            .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
                } else {
                    if(checkPermissions()) {
                        initOperationData();
                        loginAgent();
                    }
                }
            }
        });
    }

    private boolean checkPermissions() {
        // check external storage permission
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(rc != PackageManager.PERMISSION_GRANTED) {
            requestWriteStoragePermission();
            return false;
        }

        rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(rc != PackageManager.PERMISSION_GRANTED) {
            requestReadStoragePermission();
            return false;
        }

        // check camera permission
        /*rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if(rc != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return false;
        }*/

        rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(rc!=PackageManager.PERMISSION_GRANTED) {
            // request permission
            requestLocationPermission(Manifest.permission.ACCESS_FINE_LOCATION, RC_HANDLE_LOCATION_FINE);
        }

        return true;
    }

    public void requestWriteStoragePermission() {
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_WRITE_STORAGE_PERM);
            return;
        }

        final Activity thisActivity = this;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_WRITE_STORAGE_PERM);
            }
        };

        Snackbar.make(mIdTextRes, R.string.permission_storage_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    public void requestReadStoragePermission() {
        final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_READ_STORAGE_PERM);
            return;
        }

        final Activity thisActivity = this;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_READ_STORAGE_PERM);
            }
        };

        Snackbar.make(mIdTextRes, R.string.permission_storage_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /*public void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mIdTextRes, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }*/

    private void requestLocationPermission(String permission, final int handle) {
        LogMy.w(TAG, "Location permission is not granted. Requesting permission");

        final String[] permissions = new String[]{permission};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            ActivityCompat.requestPermissions(this, permissions, handle);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions, handle);
            }
        };

        final ScrollView scrollview_register = (ScrollView) findViewById(R.id.scrollview_register);
        Snackbar.make(scrollview_register, R.string.permission_location_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_WRITE_STORAGE_PERM &&
                requestCode != RC_HANDLE_CAMERA_PERM &&
                requestCode != RC_HANDLE_READ_STORAGE_PERM &&
                requestCode != RC_HANDLE_LOCATION_FINE) {
            LogMy.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LogMy.d(TAG, "Permission granted: "+requestCode);
            // we have permission, re-trigger the login button
            mLoginButton.performClick();
            return;
        }

        LogMy.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        String msg = null;
        switch (requestCode) {
            case RC_HANDLE_WRITE_STORAGE_PERM:
                msg = getString(R.string.no_write_storage_permission);
                break;
            case RC_HANDLE_READ_STORAGE_PERM:
                msg = getString(R.string.no_write_storage_permission);
                break;
            case RC_HANDLE_CAMERA_PERM:
                msg = getString(R.string.no_camera_permission);
                break;
            case RC_HANDLE_LOCATION_FINE:
                msg = getString(R.string.no_location_permission);
                break;
        }
        DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.noPermissionTitle,
                msg, false, true);
        notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
    }

    private void initOperationData() {
        // instance may have been destroyed due to wrong login / account disabled etc
        AppCommonUtil.hideKeyboard(LoginActivity.this);
        getUiResourceValues();
    }

    public void loginAgent() {
        LogMy.d(TAG, "In loginAgent");

        // validate complete form and mark errors
        if (validate()) {
            // Check if device is rooted
            // Intentionally doing only on button press and not earlier - as we intend to collect user id too
            if(RootUtil.isDeviceRooted()) {
                // Show error notification dialog
                DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppConstants.msgInsecureDevice, false, true)
                        .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
                Map<String,String> params = new HashMap<>();
                params.put("IMEI",AppCommonUtil.getIMEI(this));
                params.put("Manufacturer",AppCommonUtil.getDeviceManufacturer());
                params.put("Model",AppCommonUtil.getDeviceModel());
                params.put("AndroidVersion",AppCommonUtil.getAndroidVersion());
                AppAlarms.deviceRooted(mLoginId, DbConstants.USER_TYPE_AGENT,"loginAgent",params);
            }

            // disable login button
            mLoginButton.setEnabled(false);
            // show progress dialog
            AppCommonUtil.showProgressDialog(this, AppConstants.progressLogin);
            mWorkFragment.loginAgent(mLoginId, mPassword, mInstanceId);
        }
    }

    private boolean validate() {
        boolean valid = true;
        // validate all fields and mark ones with error
        // return false if any invalid
        int errorCode = ValidationHelper.validateInternalUserId(mLoginId);
        if(errorCode!=ErrorCodes.NO_ERROR) {
            valid = false;
            mIdTextRes.setError(AppCommonUtil.getErrorDesc(errorCode));
        }

        errorCode = ValidationHelper.validatePassword(mPassword);
        if(errorCode!=ErrorCodes.NO_ERROR) {
            valid = false;
            mPasswdTextRes.setError(AppCommonUtil.getErrorDesc(errorCode));
        }

        return valid;
    }

    @Override
//    public void onBgProcessResponse(int errorCode, int operation) {
    public void onBgProcessResponse(int errorCode, BackgroundProcessor.MessageBgJob opData) {
        LogMy.d(TAG, "In onBgProcessResponse");

        if(opData.requestCode==MyRetainedFragment.REQUEST_LOGIN)
        {
            AppCommonUtil.cancelProgressDialog(true);
            if(errorCode == ErrorCodes.NO_ERROR) {
                onLoginSuccess();
            } else {
                mLoginButton.setEnabled(true);
                // Show error notification dialog
                DialogFragmentWrapper.createNotification(AppConstants.loginFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                        .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            }
        } else if(opData.requestCode== MyRetainedFragment.REQUEST_GENERATE_PWD) {
            AppCommonUtil.cancelProgressDialog(true);
            if(errorCode == ErrorCodes.NO_ERROR) {
                // Show success notification dialog
                DialogFragmentWrapper.createNotification(AppConstants.pwdGenerateSuccessTitle, AppConstants.genericPwdGenerateSuccessMsg, false, false)
                        .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            } else {
                // Show error notification dialog
                DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                        .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            }
            mProcessingResetPasswd = false;
        }
    }

    private void makeForgotPasswordLink()
    {
        mProcessingResetPasswd = false;
        SpannableString forgotPrompt = new SpannableString( getString( R.string.Forgot_passwd_label ) );

        ClickableSpan clickableSpan = new ClickableSpan()
        {
            @Override
            public void onClick( View widget )
            {
                if(!mProcessingResetPasswd) {
                    mProcessingResetPasswd = true;
                    // read and set values
                    initOperationData();
                    // validate
                    int errorCode = ValidationHelper.validateInternalUserId(mLoginId);
                    if (errorCode == ErrorCodes.NO_ERROR) {
                        // Ask for confirmation and the PIN too
                        PasswdResetDialog dialog = new PasswdResetDialog();
                        dialog.show(getFragmentManager(), DIALOG_PASSWD_RESET);
                    } else {
                        mIdTextRes.setError(AppCommonUtil.getErrorDesc(errorCode));
                        mProcessingResetPasswd = false;
                    }
                } else {
                    AppCommonUtil.toast(LoginActivity.this, "Already in progress. Please wait.");
                }
            }
        };

        String linkText = getString( R.string.Forgot_passwd_link );
        int linkStartIndex = forgotPrompt.toString().indexOf( linkText );
        int linkEndIndex = linkStartIndex + linkText.length();
        forgotPrompt.setSpan(clickableSpan, linkStartIndex, linkEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView promptView = (TextView) findViewById( R.id.link_forgot_passwd );
        promptView.setText(forgotPrompt);
        promptView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onPasswdResetData(String secret1) {
        if(secret1!=null) {
            // show progress dialog
            AppCommonUtil.showProgressDialog(this, AppConstants.progressDefault);
            mWorkFragment.generatePassword(mLoginId, secret1);
        } else {
            mProcessingResetPasswd = false;
        }
    }

    /**
     * Sends a request for registration to RegistrationActivity,
     * expects for result in onActivityResult.
     */
    private void onLoginSuccess() {
        LogMy.d(TAG, "In onLoginSuccess");
        // Store latest succesfull login userid to preferences
        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this)
                .edit()
                .putString(AppConstants.PREF_LOGIN_ID, AgentUser.getInstance().getUser_id())
                .apply();

        // turn on fullscreen mode, which was set off in OnCreate
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        //Start Cashback Activity
        Intent intent = new Intent( this, ActionsActivity.class );
        // clear Login activity from backstack
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void getUiResourceValues() {
        mLoginId = mIdTextRes.getText().toString();
        mPassword = mPasswdTextRes.getText().toString();
    }

    // ui resources
    private EditText    mIdTextRes;
    private EditText    mPasswdTextRes;
    private Button      mLoginButton;

    private void bindUiResources() {
        mIdTextRes = (EditText) findViewById(R.id.input_user_id);
        mPasswdTextRes = (EditText) findViewById(R.id.input_password);
        mLoginButton = (Button) findViewById(R.id.btn_login);
    }

    @Override
    public void onDialogResult(String tag, int position, ArrayList<Integer> selectedItemsIndexList) {
        // empty callback - nothing to do
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(AppCommonUtil.getProgressDialogMsg()!=null) {
            AppCommonUtil.showProgressDialog(this, AppCommonUtil.getProgressDialogMsg());
        }
        if(AppConstants.BLOCK_SCREEN_CAPTURE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        AppCommonUtil.setUserType(DbConstants.USER_TYPE_CC);
        mWorkFragment.setResumeOk(true);
    }

    @Override
    protected void onPause() {
        LogMy.d(TAG,"In onPause: ");
        super.onPause();
        AppCommonUtil.cancelProgressDialog(false);
        mWorkFragment.setResumeOk(false);
    }

    @Override
    public void onBgThreadCreated() {
        // nothing to do
    }
}
