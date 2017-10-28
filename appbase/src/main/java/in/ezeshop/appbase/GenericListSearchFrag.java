package in.ezeshop.appbase;

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

import java.util.ArrayList;
import java.util.List;

import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.DialogFragmentWrapper;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.common.constants.ErrorCodes;

/**
 * Created by adgangwa on 18-10-2017.
 */

public class GenericListSearchFrag extends BaseFragment
        implements SearchView.OnQueryTextListener {
    private static final String TAG = "BaseApp-GenericListFragment";

    public static final String EXTRA_SELECTION = "extraSelected";
    public static final String EXTRA_SELECTION_INDEX = "extraSelectedIndex";

    private static final String ARG_ITEM_ICON = "listItemIcon";
    private static final String ARG_LIST_DATA = "listData";
    private static final String ARG_TITLE = "toolbarTitle";

    private static final int REQ_NOTIFY_ERROR = 1;

    // Special member variable to identify backstack cases
    private Integer mBackstackFlag;

    private TextView mViewTitle;
    private RecyclerView mRecyclerView;
    private GenericListFragmentIf mCallback;
    private GenericListAdapter mAdapter;

    // Instance state
    int mImgResId;
    ArrayList<String> mItems;
    String mTitleStr;

    public interface GenericListFragmentIf {
        void setToolbarForFrag(int iconResId, String title, String subTitle);
        void onListItemSelected(int index, String text);
    }

    public static GenericListSearchFrag getInstance(int iconId, ArrayList<String> items, String toolbarTitle) {
        Bundle args = new Bundle();
        args.putInt(ARG_ITEM_ICON, iconId);
        args.putStringArrayList(ARG_LIST_DATA, items);
        args.putString(ARG_TITLE, toolbarTitle);

        GenericListSearchFrag fragment = new GenericListSearchFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogMy.d(TAG, "In onActivityCreated");

        try {
            mCallback = (GenericListFragmentIf) getActivity();
            // title is used in case of Dialog and not fragment
            mViewTitle.setVisibility(View.GONE);

            if(savedInstanceState==null) {
                // Either fragment 'create' or 'backstack' case
                if (mBackstackFlag==null) {
                    // fragment create case - show data
                    updateUI(getArguments().getInt(ARG_ITEM_ICON),
                            getArguments().getStringArrayList(ARG_LIST_DATA),
                            getArguments().getString(ARG_TITLE));
                    mBackstackFlag = 123; // dummy memory allocation - to check for backstack scenarios later
                } else {
                    // backstack case - no need to initialize member variables
                    // as the same are automatically stored and restored
                    // so - do nothing
                }
            } else {
                // fragment re-create case
                // restore instance state
                // as adapter instance does not get stored - so need to recreate the same to show data on ui
                updateUI(savedInstanceState.getInt("mImgResId"),
                        savedInstanceState.getStringArrayList("mItems"),
                        savedInstanceState.getString("mTitleStr"));
            }

        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement GenericListFragmentIf");

        } catch (Exception e) {
            LogMy.e(TAG, "Exception in Fragment: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
            getActivity().onBackPressed();
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generic_list, container, false);

        mViewTitle = (TextView) view.findViewById(R.id.label_title);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.generic_list_menu, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        MenuItemCompat.setOnActionExpandListener(item,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when collapsed
                        mAdapter.setFilter(mItems);
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
        final List<String> filteredItems = filter(mItems, newText);

        mAdapter.setFilter(filteredItems);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private List<String> filter(List<String> items, String query) {
        query = query.toLowerCase();
        final List<String> filteredItems = new ArrayList<>();
        for (String item : items) {
            if (item.toLowerCase().contains(query)) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int i = item.getItemId();

        } catch (Exception e) {
            LogMy.e(TAG, "Exception in onOptionsItemSelected: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
        return super.onOptionsItemSelected(item);
    }*/

    public void updateUI(int iconResId, ArrayList<String> items, String title) {
        mImgResId = iconResId;
        mItems = items;
        mTitleStr = title;

        mAdapter = new GenericListAdapter(mImgResId, mItems);
        mRecyclerView.setAdapter(mAdapter);
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogMy.d(TAG, "In onActivityResult :" + requestCode + ", " + resultCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        try {
        } catch (Exception e) {
            LogMy.e(TAG, "Exception in onActivityResult: ", e);
            DialogFragmentWrapper.createNotification(AppConstants.generalFailureTitle, AppCommonUtil.getErrorDesc(ErrorCodes.GENERAL_ERROR), true, true)
                    .show(getFragmentManager(), DialogFragmentWrapper.DIALOG_NOTIFICATION);
        }
    }*/

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
        mCallback.setToolbarForFrag(-1, mTitleStr,null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mImgResId", mImgResId);
        outState.putStringArrayList("mItems", mItems);
        outState.putString("mTitleStr", mTitleStr);
    }

    private class ListItemHolder extends RecyclerView.ViewHolder {

        private View mLytListItem;
        private ImageView mImgListItem;
        private TextView mInputListItem;

        public ListItemHolder(View itemView) {
            super(itemView);
            mLytListItem = itemView.findViewById(R.id.layout_listItem);
            mImgListItem = (ImageView) itemView.findViewById(R.id.img_listItem);
            mInputListItem = (TextView) itemView.findViewById(R.id.input_listItem);
        }

        public void bindItem(int iconId, String text) {
            if(iconId!=-1) {
                mImgListItem.setVisibility(View.VISIBLE);
                mImgListItem.setImageResource(iconId);
            } else {
                mImgListItem.setVisibility(View.GONE);
            }
            mInputListItem.setText(text);
        }
    }

    private class GenericListAdapter extends RecyclerView.Adapter<ListItemHolder> {
        private List<String> mItems;
        private int mIconResId;
        private View.OnClickListener mListener;

        public GenericListAdapter(int iconResId, List<String> items) {
            mIconResId = iconResId;
            mItems = items;

            mListener = new OnSingleClickListener() {
                final List<String> data = mItems;

                @Override
                public void onSingleClick(View v) {
                    //if(!mCallback.getRetainedFragment().getResumeOk())
                        //return;

                    LogMy.d(TAG,"In onSingleClick of txn list item");
                    int pos = mRecyclerView.getChildAdapterPosition(v);

                    if (pos >= 0 && pos < getItemCount()) {
                        mCallback.onListItemSelected(pos, data.get(pos));
                    } else {
                        LogMy.wtf(TAG,"Invalid position in onClickListener of Generic list item: "+pos);
                    }
                }
            };
        }

        @Override
        public ListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.generic_list_item, parent, false);
            return new ListItemHolder(view);
        }
        @Override
        public void onBindViewHolder(ListItemHolder holder, int position) {
            String text = mItems.get(position);
            holder.itemView.setOnClickListener(mListener);
            holder.bindItem(mIconResId, text);
        }
        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public void setFilter(List<String> items) {
            mItems = new ArrayList<>();
            mItems.addAll(items);
            notifyDataSetChanged();
        }
    }
}
