package in.ezeshop.internal;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import in.ezeshop.appbase.utilities.BackgroundProcessor;
import in.ezeshop.internal.entities.AgentUser;
import in.ezeshop.appbase.entities.MyBusinessCategories;
import in.ezeshop.appbase.entities.MyCities;
import in.ezeshop.internal.helper.MyRetainedFragment;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.DbConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Address;
import in.ezeshop.common.database.Cities;
import in.ezeshop.common.database.Merchants;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.ValidationHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;


public class RegisterMerchantActivity extends AppCompatActivity
        implements MyRetainedFragment.RetainedFragmentIf,
        DialogFragmentWrapper.DialogFragmentWrapperIf,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String TAG = "AgentApp-RegMerchantActivity";

    private static final int REQUEST_LOAD_IMAGE = 0;

    // Tags of dialogues created by this activity
    private static final String DIALOG_CATEGORY = "DialogCategory";
    private static final String DIALOG_CITY = "DialogCity";
    private static final String DIALOG_NO_INTERNET = "DialogNoInternet";
    private static final String DIALOG_REG_MERCHANT = "DialogRegMerchant";
    private static final String DIALOG_REG_SUCCESS = "DialogRegSuccess";
    private static final String DIALOG_REG_FAILED = "DialogRegFailure";
    private static final String DIALOG_BACK_BUTTON = "dialogBackButton";

    private static final String RETAINED_FRAGMENT_TAG = "workLogin";

    MyRetainedFragment mWorkFragment;
    //private Merchants mMerchant;
    private Address mAddress;

    private File        mPhotoFile;
    private boolean     mImageUploaded;
    //private boolean     mTermsAgreed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_merchant);

        // gets handlers to screen resources
        bindUiResources();

        // check internet connectivity
        int resultCode = AppCommonUtil.isNetworkAvailableAndConnected(RegisterMerchantActivity.this);
        if ( resultCode != ErrorCodes.NO_ERROR) {
            // Show error notification dialog
            DialogFragmentWrapper.createNotification(AppConstants.noInternetTitle, AppCommonUtil.getErrorDesc(resultCode), false, true)
                    .show(getFragmentManager(), DIALOG_NO_INTERNET);
            return;
        }

        // fetch 'business category' and 'cities' value sets asynchronously
        MyCities.initSync();
        MyBusinessCategories.init();

        // Location related
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        // Initialize retained fragment - used for registration background thread
        // Check to see if we have retained the worker fragment.
        FragmentManager fm = getFragmentManager();
        mWorkFragment = (MyRetainedFragment)fm.findFragmentByTag(RETAINED_FRAGMENT_TAG);
        // If not retained (or first time running), we need to create it.
        if (mWorkFragment == null) {
            LogMy.d(TAG,"Creating new retained fragment");
            mWorkFragment = new MyRetainedFragment();
            fm.beginTransaction().add(mWorkFragment, RETAINED_FRAGMENT_TAG).commit();
        }

        // init data members
        //mMerchant = new Merchants();
        mAddress = new Address();
        mWorkFragment.mCurrMerchant = new Merchants();

        // Image upload handling
        mImageUploaded=false;
        final Intent pickImageIntent = prepareImageUpload();
        mImageUploadBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                startActivityForResult(pickImageIntent, REQUEST_LOAD_IMAGE);
            }
        });

        // check for location permission
        /*int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(rc==PackageManager.PERMISSION_GRANTED) {
            mGoogleApiClient.connect();
        } else {
            // request permission
            requestLocationPermission(Manifest.permission.ACCESS_FINE_LOCATION, RC_HANDLE_LOCATION_FINE);
        }*/

        // setup choice handlers
        initChoiceCategories();
        initChoiceCities();

        // Register button listener
        mRegisterButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                // get all input values
                getUiResourceValues();
                // register merchant
                registerMerchant();
            }
        });
    }

    @Override
    public void onDialogResult(String tag, int indexOrResultCode, ArrayList<Integer> selectedItemsIndexList) {
        switch(tag) {
            case DIALOG_NO_INTERNET:
                finish();
                break;
            case DIALOG_REG_MERCHANT:
                mRegisterButton.setEnabled(false);
                AppCommonUtil.showProgressDialog(this, AppConstants.progressRegisterMerchant);
                mWorkFragment.registerMerchant(mPhotoFile);
                break;
            case DIALOG_REG_SUCCESS:
                //MerchantUser.reset();
                finish();
                break;
            case DIALOG_CATEGORY:
                String category = String.valueOf(MyBusinessCategories.getCategoryValueSet()[indexOrResultCode]);
                mWorkFragment.mCurrMerchant.setBuss_category(MyBusinessCategories.getCategoryWithName(category).getCategory_name());
                mCategoryTextRes.setText(category);
                mCategoryTextRes.setError(null);
                break;
            case DIALOG_CITY:
                String city = String.valueOf(MyCities.getCityValueSet()[indexOrResultCode]);
                Cities cityObj = MyCities.getCityWithName(city);
                //mAddress.setCity(cityObj.getCity());
                //mAddress.setState(cityObj.getState());
                mCityTextRes.setText(city);
                mCityTextRes.setError(null);
                mStateTextRes.setText(cityObj.getState());
                mStateTextRes.setError(null);
                break;
            case DIALOG_BACK_BUTTON:
                finish();
                break;
        }
    }

    private void registerMerchant() {
        if (validate()) {
            // set location
            mAddress.setLatitude(currentLatitude);
            mAddress.setLongitude(currentLongitude);

            mWorkFragment.mCurrMerchant.setAddress(mAddress);
            // confirm for registration
            DialogFragmentWrapper.createConfirmationDialog(AppConstants.regConfirmTitle, AppConstants.regConfirmMsg, false, false)
                    .show(getFragmentManager(), DIALOG_REG_MERCHANT);
        }
    }

    private Intent prepareImageUpload() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageIntent.setType("image/*");
        pickImageIntent.putExtra("crop", "true");

        Float imgWidth = getResources().getDimension(R.dimen.register_image_width);
        Float imgHeight = getResources().getDimension(R.dimen.register_image_height);

        int cropWidth = 2*imgWidth.intValue();
        int cropHeight = 2*imgHeight.intValue();

        // Crop size as twice to that of display size
        pickImageIntent.putExtra("outputX", cropWidth);
        pickImageIntent.putExtra("outputY", cropHeight);
        pickImageIntent.putExtra("aspectX", 1);
        pickImageIntent.putExtra("aspectY", 1);
        pickImageIntent.putExtra("scale", true);
        //pickImageIntent.putExtra("outputFormat", CommonConstants.PHOTO_FILE_FORMAT);
        //pickImageIntent.putExtra("outputFormat", Bitmap.CompressFormat.WEBP.toString());
        //pickImageIntent.putExtra("outputFormat", AppCommonUtil.getImgCompressFormat().toString());

        /*String tmpDpFilename = "IMG_" + System.currentTimeMillis() + "." + CommonConstants.PHOTO_FILE_FORMAT;
        mPhotoFile = AppCommonUtil.createLocalImageFile(this, tmpDpFilename);
        PackageManager packageManager = this.getPackageManager();

        boolean canTakePhoto = (mPhotoFile != null) && pickImageIntent.resolveActivity(packageManager) != null;
        mImageUploadBtn.setEnabled(canTakePhoto);
        if (canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            LogMy.d(TAG,"URI: "+uri);
            pickImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }*/

        return pickImageIntent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOAD_IMAGE) {
            if (resultCode == RESULT_OK) {
                try {
                    final InputStream ist = getContentResolver().openInputStream(data.getData());
                    final Bitmap bitmap = BitmapFactory.decodeStream(ist);
                    mImageUploadBtn.setImageBitmap(bitmap);
                    mImageUploaded = true;
                    String tmpDpFilename = "IMG_" + System.currentTimeMillis() + "." + CommonConstants.PHOTO_FILE_FORMAT;
                    AppCommonUtil.compressBmpAndStore(this, bitmap, tmpDpFilename);
                    mPhotoFile = getFileStreamPath(tmpDpFilename);

                } catch (Exception e) {
                    LogMy.d(TAG, "Failed to update display image: " + e.toString());
                }
                //updateDisplayImage(mPhotoFile);
            } else {
                LogMy.d(TAG, "Image crop return failure, Result code : "+resultCode);
            }
        }
    }

    private void updateDisplayImage(File image) {
        try {
            FileInputStream in =  new FileInputStream(image);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            Bitmap userImage = BitmapFactory.decodeStream(in, null, options);
            mImageUploadBtn.setImageBitmap(userImage);
            mImageUploaded = true;
        } catch (Exception e) {
            LogMy.d(TAG, "Failed to update display image: " + e.toString());
        }
    }

    @Override
//    public void onBgProcessResponse(int errorCode, int operation) {
    public void onBgProcessResponse(int errorCode, BackgroundProcessor.MessageBgJob opData) {
        LogMy.d(TAG, "In onBgProcessResponse");

        if(opData.requestCode== MyRetainedFragment.REQUEST_REGISTER_MERCHANT) {
            AppCommonUtil.cancelProgressDialog(true);
            if(errorCode==ErrorCodes.NO_ERROR) {
                //mMerchant = mMerchantUser.getMerchant();
                mRegisterButton.setEnabled(true);

                // Return response to parent activity (login) and finish this activity
                //setResult(RESULT_OK, null);

                // Show dialog
                String msg = String.format(AppConstants.mchntRegSuccessMsg, AgentUser.getInstance().mLastRegMerchantId);
                DialogFragmentWrapper.createNotification(AppConstants.defaultSuccessTitle, msg, false, false)
                        .show(getFragmentManager(), DIALOG_REG_SUCCESS);

            } else {
                mRegisterButton.setEnabled(true);
                // Show error notification dialog
                DialogFragmentWrapper.createNotification(AppConstants.regFailureTitle, AppCommonUtil.getErrorDesc(errorCode), false, true)
                        .show(getFragmentManager(), DIALOG_REG_FAILED);
            }
        }
    }

    private boolean validate() {
        boolean valid = true;
        int errorCode = ErrorCodes.NO_ERROR;

        StringBuilder sb = new StringBuilder();
        sb.append("Correct following: ");

        if(mRegFormNum.getText().toString().isEmpty()) {
            valid = false;
            mBrandNameTextRes.setError(AppCommonUtil.getErrorDesc(errorCode));
            sb.append("Form Number; ");
        }

        // validate all fields and mark ones with error
        // return false if any invalid
        if(!mImageUploaded) {
            valid = false;
            errorCode=ErrorCodes.GENERAL_ERROR;
            sb.append("Display image; ");
        }

        errorCode = ValidationHelper.validateName(mBrandNameTextRes.getText().toString());
        if( errorCode != ErrorCodes.NO_ERROR ) {
            valid = false;
            mBrandNameTextRes.setError(AppCommonUtil.getErrorDesc(errorCode));
            sb.append("Brand name; ");
        }

        if(mWorkFragment.mCurrMerchant.getBuss_category() == null) {
            mCategoryTextRes.setError("Select business category");
            valid = false;
            sb.append("Business category; ");
        } else {
            mCategoryTextRes.setError(null);
        }

        String mobile = mMobileNoTextRes.getText().toString();
        errorCode = ValidationHelper.validateMobileNo(mobile);
        if( errorCode != ErrorCodes.NO_ERROR ) {
            valid = false;
            mMobileNoTextRes.setError(AppCommonUtil.getErrorDesc(errorCode));
            sb.append("Mobile number; ");
        } else if(!mMobileNoConfirm.getText().toString().equals(mobile)) {
            valid = false;
            mMobileNoTextRes.setError("Doesn't match");
            sb.append("Confirm Mobile number; ");
        }

        errorCode = ValidationHelper.validateName(mContactNameRes.getText().toString());
        if( errorCode != ErrorCodes.NO_ERROR ) {
            valid = false;
            mContactNameRes.setError(AppCommonUtil.getErrorDesc(errorCode));
            sb.append("Contact name; ");
        }

        errorCode = ValidationHelper.validateMobileNo(mContactNumRes.getText().toString());
        if( errorCode != ErrorCodes.NO_ERROR ) {
            valid = false;
            mContactNumRes.setError(AppCommonUtil.getErrorDesc(errorCode));
            sb.append("Contact number; ");
        }

        if(mEmailTextRes.getText()!=null) {
            errorCode = ValidationHelper.validateEmail(mEmailTextRes.getText().toString());
            if (errorCode != ErrorCodes.NO_ERROR) {
                valid = false;
                mEmailTextRes.setError(AppCommonUtil.getErrorDesc(errorCode));
                sb.append("Email; ");
            }
        }

        errorCode = ValidationHelper.validateDob(mDoBTextRes.getText().toString());
        if( errorCode != ErrorCodes.NO_ERROR ) {
            valid = false;
            mDoBTextRes.setError(AppCommonUtil.getErrorDesc(errorCode));
            sb.append("DoB; ");
        }

        if(mAddress.getAreaNIDB() == null) {
            mCityTextRes.setError("Select city");
            valid = false;
            sb.append("Address city; ");
        } else {
            mCityTextRes.setError(null);
        }

        errorCode = ValidationHelper.validateAddress(mAddressTextRes1.getText().toString());
        if( errorCode != ErrorCodes.NO_ERROR ) {
            valid = false;
            mAddressTextRes1.setError(AppCommonUtil.getErrorDesc(errorCode));
            sb.append("Address; ");
        }

        /*errorCode = ValidationHelper.validatePincode(mPincodeRes.getText().toString());
        if( errorCode != ErrorCodes.NO_ERROR ) {
            valid = false;
            mPincodeRes.setError(AppCommonUtil.getErrorDesc(errorCode));
            sb.append("Pincode; ");
        }*/

        /*if(currentLatitude==0 || currentLongitude==0) {
            valid = false;
            sb.append("Location");
        }*/

        if(!valid) {
            Toast.makeText(getBaseContext(), sb.toString(), Toast.LENGTH_LONG).show();
        }
        return valid;
    }

    private void initChoiceCategories() {
        mCategoryTextRes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                FragmentManager fragManager = getFragmentManager();
                if ((event.getAction() == MotionEvent.ACTION_UP) && (fragManager.findFragmentByTag(DIALOG_CATEGORY) == null)) {
                    //LogMy.d(TAG, "In onTouch");
                    AppCommonUtil.hideKeyboard(RegisterMerchantActivity.this);
                    DialogFragmentWrapper dialog = DialogFragmentWrapper.createSingleChoiceDialog(getString(R.string.category_hint), MyBusinessCategories.getCategoryValueSet(),-1, false);
                    dialog.show(fragManager, DIALOG_CATEGORY);
                    return true;
                }
                return false;
            }
        });
        mCategoryTextRes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                FragmentManager fragManager = getFragmentManager();
                if (hasFocus && (fragManager.findFragmentByTag(DIALOG_CATEGORY) == null)) {
                    //LogMy.d(TAG, "In onFocusChange");
                    AppCommonUtil.hideKeyboard(RegisterMerchantActivity.this);
                    DialogFragmentWrapper dialog = DialogFragmentWrapper.createSingleChoiceDialog(getString(R.string.category_hint), MyBusinessCategories.getCategoryValueSet(),-1, false);
                    dialog.show(fragManager, DIALOG_CATEGORY);
                }
            }
        });
    }

    private void initChoiceCities() {
        mCityTextRes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                FragmentManager fragManager = getFragmentManager();
                if ((event.getAction() == MotionEvent.ACTION_UP) && (fragManager.findFragmentByTag(DIALOG_CITY) == null)) {
                    //LogMy.d(TAG, "In onTouch");
                    AppCommonUtil.hideKeyboard(RegisterMerchantActivity.this);
                    DialogFragmentWrapper dialog = DialogFragmentWrapper.createSingleChoiceDialog(getString(R.string.city_hint), MyCities.getCityValueSet(),-1, false);
                    dialog.show(fragManager, DIALOG_CITY);
                    return true;
                }
                return false;
            }
        });
        mCityTextRes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                FragmentManager fragManager = getFragmentManager();
                if (hasFocus && (fragManager.findFragmentByTag(DIALOG_CITY) == null)) {
                    //LogMy.d(TAG, "In onFocusChange");
                    AppCommonUtil.hideKeyboard(RegisterMerchantActivity.this);
                    DialogFragmentWrapper dialog = DialogFragmentWrapper.createSingleChoiceDialog(getString(R.string.city_hint), MyCities.getCityValueSet(),-1, false);
                    dialog.show(fragManager, DIALOG_CITY);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DialogFragmentWrapper.createConfirmationDialog(AppConstants.exitGenTitle, AppConstants.exitRegActivityMsg, false, false).show(getFragmentManager(), DIALOG_BACK_BUTTON);
    }

    // ui resources
    private EditText    mRegFormNum;

    private ImageView   mImageUploadBtn;
    private EditText    mBrandNameTextRes;
    private EditText    mCategoryTextRes;
    private EditText    mMobileNoTextRes;
    private EditText    mMobileNoConfirm;

    private EditText    mContactNameRes;
    private EditText    mContactNumRes;
    private EditText    mEmailTextRes;
    private EditText    mDoBTextRes;
    private EditText    mCityTextRes;
    private EditText    mStateTextRes;
    private EditText    mAddressTextRes1;
    //private EditText    mPincodeRes;
    private Button      mRegisterButton;

    private void bindUiResources() {
        mRegFormNum = (EditText) findViewById(R.id.input_formNum);

        mImageUploadBtn     = (ImageView) findViewById(R.id.btn_upload_image);
        mBrandNameTextRes   = (EditText) findViewById(R.id.input_merchant_name);
        mCategoryTextRes    = (EditText) findViewById(R.id.edittext_category);
        mMobileNoTextRes    = (EditText) findViewById(R.id.input_merchant_mobile);
        mMobileNoConfirm    = (EditText) findViewById(R.id.input_mobile_confirm);

        mContactNameRes       = (EditText) findViewById(R.id.input_contact_name);
        mContactNumRes       = (EditText) findViewById(R.id.input_contact_num);
        mEmailTextRes       = (EditText) findViewById(R.id.input_merchant_email);
        mDoBTextRes       = (EditText) findViewById(R.id.input_merchant_dob);
        mCityTextRes        = (EditText) findViewById(R.id.edittext_city);
        mStateTextRes       = (EditText) findViewById(R.id.edittext_state);
        mAddressTextRes1     = (EditText) findViewById(R.id.input_address_1);
        //mPincodeRes     = (EditText) findViewById(R.id.input_pincode);
        //mTermsCheckBox      = (CheckBox) findViewById(R.id.checkBox_terms);
        mRegisterButton     = (Button) findViewById(R.id.btn_register);
        //mLoginLink          = (TextView) findViewById(R.id.link_login);
        //mTermsLink          = (TextView) findViewById(R.id.link_terms);
    }

    private void getUiResourceValues() {
        mWorkFragment.mCurrMerchant.setRegFormNum(mRegFormNum.getText().toString());
        mWorkFragment.mCurrMerchant.setName(mBrandNameTextRes.getText().toString());
        mWorkFragment.mCurrMerchant.setMobile_num(mMobileNoTextRes.getText().toString());
        mWorkFragment.mCurrMerchant.setContactName(mContactNameRes.getText().toString());
        mWorkFragment.mCurrMerchant.setContactPhone(mContactNumRes.getText().toString());
        mWorkFragment.mCurrMerchant.setEmail(mEmailTextRes.getText().toString());
        mWorkFragment.mCurrMerchant.setDob(mDoBTextRes.getText().toString());
        mAddress.setLine_1(mAddressTextRes1.getText().toString());
        //mAddress.setPincode(mPincodeRes.getText().toString());
        //mTermsAgreed = mTermsCheckBox.isChecked();
    }

    // Location related callbacks

    @Override
    protected void onResume() {
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
        //Now lets connect to the API
        //mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogMy.d(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        AppCommonUtil.cancelProgressDialog(false);
        mWorkFragment.setResumeOk(false);
    }

    //Define a request code to send to Google Play services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_LOCATION_FINE = 3;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;

    @Override
    public void onConnected(Bundle bundle) {
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(rc==PackageManager.PERMISSION_GRANTED) {
            // Create the LocationRequest object
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000); // 1 second, in milliseconds


            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                //If everything went fine lets get latitude and longitude
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();

                LogMy.d(TAG, currentLatitude + " WORKS " + currentLongitude);
            }
            return;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Do nothing
    }

    @Override
    public void onLocationChanged(Location location) {
        // Do nothing
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                LogMy.e(TAG, "Exception in onConnectionFailed: " + e.toString());
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            LogMy.e(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

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

    /*@Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_LOCATION_FINE) {
            LogMy.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LogMy.d(TAG, "Location permission granted - initialize the camera source");
            mGoogleApiClient.connect();
            return;
        }

        LogMy.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Location permission !!")
                .setMessage(R.string.no_location_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }*/

    @Override
    public void onBgThreadCreated() {
        // do nothing
    }
}
