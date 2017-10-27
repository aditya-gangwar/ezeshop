package in.ezeshop.customerbase;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.ezeshop.appbase.BaseFragment;
import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.entities.MyCashback;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.common.CommonUtils;
import in.ezeshop.common.MyGlobalSettings;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Merchants;
import in.ezeshop.customerbase.helper.MyRetainedFragment;

/**
 * Created by adgangwa on 20-10-2017.
 */

public class ChooseMerchantFrag extends BaseFragment
        implements SearchView.OnQueryTextListener {
    private static final String TAG = "CustApp-ChooseMerchantFrag";

    private static final String DIALOG_SORT_MCHNT_TYPES = "dialogSortMchnt";
    private static final String DIALOG_MERCHANT_DETAILS = "dialogMerchantDetails";

    private static final String ARG_AREA_ID = "areaId";
    private static final String ARG_TOOLBAR_TITLE = "toolbarTitle";

    private static final int REQ_NOTIFY_ERROR = 1;
    private static final int REQ_SORT_MCHNT_TYPES = 2;

    private MyRetainedFragment mRetainedFragment;
    private ChooseMerchantFrag.ChooseMerchantFragIf mCallback;
    private MchntAdapter mAdapter;

    public interface ChooseMerchantFragIf {
        MyRetainedFragment getRetainedFragment();
        void setToolbarForFrag(int iconResId, String title, String subTitle);
        void showMchntDetails(MyCashback data);
        void onSelectMerchant(String mchntId);
    }

    private RecyclerView mRecyclerView;
    //private List<MerchantWrapper> mData;
    private List<MyCashback> mData;

    // instance state - store and restore
    private int mSelectedSortType;


    public static ChooseMerchantFrag getInstance(String areaId) {
        Bundle args = new Bundle();
        args.putString(ARG_AREA_ID, areaId);

        ChooseMerchantFrag fragment = new ChooseMerchantFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogMy.d(TAG, "In onActivityCreated");

        try {
            mCallback = (ChooseMerchantFrag.ChooseMerchantFragIf) getActivity();
            mRetainedFragment = mCallback.getRetainedFragment();

            if(savedInstanceState!=null) {
                mSelectedSortType = savedInstanceState.getInt("mSelectedSortType");
            } else {
                mSelectedSortType= MyCashback.CB_CMP_TYPE_ACC_BALANCE;
            }
            refreshData();

            setHasOptionsMenu(true);

        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement ChooseMerchantFragIf");
        } catch(Exception e) {
            LogMy.e(TAG, "Exception is ChooseMerchantFrag:onActivityCreated", e);
            // unexpected exception - show error
            DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            notDialog.setTargetFragment(this,REQ_NOTIFY_ERROR);
            notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            getActivity().onBackPressed();
        }
    }

    private void sortList(int sortType) {
        if(mData !=null) {
            Collections.sort(mData, new MyCashback.MyCashbackComparator(sortType));

            if (sortType != MyCashback.CB_CMP_TYPE_MCHNT_NAME) {
                // Make it in decreasing order - if not string comparison
                Collections.reverse(mData);
            }
            // store existing sortType
            mSelectedSortType = sortType;
        } else {
            LogMy.wtf(TAG,"In sortList: mData is null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_mchnt_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyView_myMchntListOrder);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    private void updateUI() {
        if(mData !=null) {
            mAdapter = new MchntAdapter(mData);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            LogMy.e(TAG,"In updateUI: mData is null");
        }
    }

    public void refreshData() {
        if(mRetainedFragment.mAreaToMerchants!=null) {
            List<Merchants> mchnts = mRetainedFragment.mAreaToMerchants.get(getArguments().getString(ARG_AREA_ID));
            mData = new ArrayList<>(mchnts.size());
            for (Merchants m :
                    mchnts) {
                // Check existing Cashback list - to find account balance
                MyCashback cb = null;
                if(mRetainedFragment.mCashbacks!=null) {
                    cb = mRetainedFragment.mCashbacks.get(m.getAuto_id());
                }
                if(cb!=null) {
                    // already available - add the same to data
                    mData.add(cb);
                } else {
                    // create new object with Merchants object
                    mData.add(new MyCashback(m));
                }
                //int accBal = cb==null?0:cb.getCurrAccBalance();
                //mData.add(new MerchantWrapper(m,null,accBal));
            }

            sortList(mSelectedSortType);
            updateUI();
        } else {
            LogMy.e(TAG,"In refreshData: mCashbacks is null");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogMy.d(TAG, "In onActivityResult :" + requestCode + ", " + resultCode);
        if(resultCode != Activity.RESULT_OK) {
            return;
        }
        try {
            if (requestCode == REQ_SORT_MCHNT_TYPES) {
                int sortType = data.getIntExtra(SortMchntDialog.EXTRA_SELECTION, MyCashback.CB_CMP_TYPE_ACC_BALANCE);
                sortList(sortType);
                updateUI();
            }
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCallback.setToolbarForFrag(-1,"Choose Merchant",null);
        try {
            //updateUI();
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            throw e;
            //getActivity().onBackPressed();
        }
        mCallback.getRetainedFragment().setResumeOk(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mSelectedSortType", mSelectedSortType);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.order_mchnt_list_menu, menu);

        final MenuItem item = menu.findItem(in.ezeshop.appbase.R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        MenuItemCompat.setOnActionExpandListener(item,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when collapsed
                        mAdapter.setFilter(mData);
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // Do something when expanded
                        return true; // Return true to expand action view
                    }
                });
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<MyCashback> filteredItems = filter(mData, newText);

        mAdapter.setFilter(filteredItems);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private List<MyCashback> filter(List<MyCashback> items, String query) {
        query = query.toLowerCase();
        final List<MyCashback> filteredItems = new ArrayList<>();
        for (MyCashback item : items) {
            if ( item.getMerchant().getName().toLowerCase().contains(query) ||
                    item.getMerchant().getAddress().getLine_1().toLowerCase().contains(query)) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!mCallback.getRetainedFragment().getResumeOk())
            return true;

        try {
            int i = item.getItemId();
            if (i == R.id.action_sort) {
                sortMerchantList();

            }

        } catch(Exception e) {
            LogMy.e(TAG, "Exception is onOptionsItemSelected", e);
            // unexpected exception - show error
            DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            notDialog.setTargetFragment(this,REQ_NOTIFY_ERROR);
            notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortMerchantList() {
        try {
            OrderSortMchntDialog dialog = OrderSortMchntDialog.newInstance(mSelectedSortType);
            dialog.setTargetFragment(this, REQ_SORT_MCHNT_TYPES);
            dialog.show(getFragmentManager(), DIALOG_SORT_MCHNT_TYPES);

        } catch(Exception e) {
            LogMy.e(TAG, "Exception in sortMerchantList", e);
            // unexpected exception - show error
            DialogFragmentWrapper notDialog = DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true);
            notDialog.setTargetFragment(this,REQ_NOTIFY_ERROR);
            notDialog.show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
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

    private class MchntHolder extends RecyclerView.ViewHolder {

        private MyCashback mMchnt;

        private TextView mMerchantName;
        private TextView mBtnDEtails;

        private ImageView mMerchantDp;
        private TextView mAddress;
        private TextView mDeliveryInfo;
        private TextView mAccBalance;
        private TextView mCbRate;

        public MchntHolder(View itemView) {
            super(itemView);

            mMerchantName = (TextView) itemView.findViewById(R.id.cardTitle_mchntName);
            mBtnDEtails = (TextView) itemView.findViewById(R.id.cardBtn_details);
            mMerchantDp = (ImageView) itemView.findViewById(R.id.img_merchant);
            mAddress = (TextView) itemView.findViewById(R.id.input_mchntAddress);
            mDeliveryInfo = (TextView) itemView.findViewById(R.id.input_delChargeInfo);
            mAccBalance = (TextView) itemView.findViewById(R.id.input_accBalance);
            mCbRate = (TextView)  itemView.findViewById(R.id.input_cbRate);
        }

        public void bindCb(MyCashback mchnt) {
            mMchnt = mchnt;
            Merchants merchant = mchnt.getMerchant();

            mMerchantName.setText(merchant.getName());

            Bitmap dp = getMchntDp(merchant.getDisplayImage());
            if(dp!=null) {
                mMerchantDp.setImageBitmap(dp);
            }

            mAddress.setText(CommonUtils.getMchntAddrStrShort(merchant));

            String delInfo;
            if(merchant.getFreeDlvrMinAmt()>0) {
                delInfo = String.format(getString(R.string.deliveryChargeInfo),
                        MyGlobalSettings.getDeliveryCharges().toString(),
                        merchant.getFreeDlvrMinAmt().toString());
            } else {
                delInfo = "Delivery is FREE";
            }
            mDeliveryInfo.setText(delInfo);

            AppCommonUtil.showAmtColor(getActivity(),null,mAccBalance,mchnt.getCurrAccBalance(),false);
            String cbStr = "@"+merchant.getCb_rate()+"%";
            mCbRate.setText(cbStr);

            mBtnDEtails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Show merchant details dialog
                    mCallback.showMchntDetails(mMchnt);
                    /*mRetainedFragment.mSelectCashback = mMchnt;
                    MchntDetailsDialogCustApp dialog = MchntDetailsDialogCustApp.newInstance(mMchnt.isAccDataAvailable());
                    dialog.show(getFragmentManager(), DIALOG_MERCHANT_DETAILS);*/
                }
            });
        }

        private Bitmap getMchntDp(String filename) {
            if(filename!=null) {
                File file = getActivity().getFileStreamPath(filename);
                if(file!=null) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    if(bitmap==null) {
                        LogMy.e(TAG,"Not able to decode mchnt dp file: "+file.getName());
                    } else {
                        LogMy.d(TAG,"Decoded file as bitmap: "+file.getPath());
                        // convert to round image
                        /*float radiusInDp = getResources().getDimension(R.dimen.dp_item_image_width);
                        float radiusInPixels = AppCommonUtil.dipToPixels(getActivity(), radiusInDp);

                        Bitmap scaledImg = Bitmap.createScaledBitmap(bitmap,(int)radiusInPixels,(int)radiusInPixels,true);
                        Bitmap roundImage = AppCommonUtil.getCircleBitmap(scaledImg);
                        return roundImage;*/
                        return bitmap;
                    }
                } else {
                    LogMy.e(TAG,"Mchnt Dp file not available locally: "+filename);
                }
            }
            return null;
        }


    }

    private class MchntAdapter extends RecyclerView.Adapter<MchntHolder> {

        private List<MyCashback> mItems;
        private View.OnClickListener mListener;

        public MchntAdapter(List<MyCashback> mchnts) {
            mItems = mchnts;
            mListener = new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    LogMy.d(TAG,"In onClickListener of merchant list item");

                    if(!mCallback.getRetainedFragment().getResumeOk())
                        return;

                    int pos = mRecyclerView.getChildAdapterPosition(v);
                    if (pos >= 0 && pos < getItemCount()) {
                        mCallback.onSelectMerchant(mItems.get(pos).getMerchant().getAuto_id());
                    } else {
                        LogMy.e(TAG,"Invalid position in onClickListener of customer list item: "+pos);
                    }
                }
            };
        }

        @Override
        public MchntHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.order_merchant_itemview, parent, false);
            return new MchntHolder(view);
        }

        @Override
        public void onBindViewHolder(MchntHolder holder, int position) {
            MyCashback m = mItems.get(position);
            holder.itemView.setOnClickListener(mListener);
            holder.bindCb(m);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public void setFilter(List<MyCashback> items) {
            mItems = new ArrayList<>();
            mItems.addAll(items);
            notifyDataSetChanged();
        }
    }
}

