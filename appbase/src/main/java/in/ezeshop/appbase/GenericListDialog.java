package in.ezeshop.appbase;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.ezeshop.appbase.constants.AppConstants;
import in.ezeshop.appbase.utilities.AppCommonUtil;
import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.appbase.utilities.OnSingleClickListener;
import in.ezeshop.appbase.utilities.ValidationHelper;
import in.ezeshop.common.constants.ErrorCodes;

/**
 * Created by adgangwa on 28-10-2017.
 */

public class GenericListDialog extends BaseDialog {
    public static final String TAG = "MchntApp-GenericListDialog";

    private static final String ARG_ITEM_ICON = "listItemIcon";
    private static final String ARG_LIST_DATA = "listData";
    private static final String ARG_TITLE = "toolbarTitle";

    public interface GenericListDialogIf {
        void onListItemSelected(int index, String text);
    }

    private TextView mViewTitle;
    private RecyclerView mRecyclerView;
    private GenericListDialogIf mCallback;
    private GenericListDialog.GenericListAdapter mAdapter;

    // Instance state
    int mImgResId;
    ArrayList<String> mItems;
    String mTitleStr;

    public static GenericListDialog getInstance(int iconId, ArrayList<String> items, String title) {
        Bundle args = new Bundle();
        args.putInt(ARG_ITEM_ICON, iconId);
        args.putStringArrayList(ARG_LIST_DATA, items);
        args.putString(ARG_TITLE, title);

        GenericListDialog fragment = new GenericListDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LogMy.d(TAG, "In onCreateDialog");

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_generic_list, null);

        mViewTitle = (TextView) v.findViewById(R.id.label_title);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // return new dialog
        final AlertDialog alertDialog =  new AlertDialog.Builder(getActivity()).setView(v).create();

        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mCallback = (GenericListDialogIf) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement GenericListDialogIf");
        }

        updateUI(getArguments().getInt(ARG_ITEM_ICON),
                getArguments().getStringArrayList(ARG_LIST_DATA),
                getArguments().getString(ARG_TITLE));
    }

    public void updateUI(int iconResId, ArrayList<String> items, String title) {
        mImgResId = iconResId;
        mItems = items;
        mTitleStr = title;

        mViewTitle.setVisibility(View.VISIBLE);
        mViewTitle.setText(title);

        mAdapter = new GenericListAdapter(mImgResId, mItems);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void handleBtnClick(DialogInterface dialog, int which) {
        // do nothing
    }

    @Override
    public boolean handleTouchUp(View v) {
        return false;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    private class ListItemHolder extends RecyclerView.ViewHolder {

        private ImageView mImgListItem;
        private TextView mInputListItem;

        public ListItemHolder(View itemView) {
            super(itemView);
            mImgListItem = (ImageView) itemView.findViewById(R.id.img_listItem);
            mInputListItem = (TextView) itemView.findViewById(R.id.input_listItem);
        }

        public void bindItem(int iconId, String text) {
            if(iconId!=-1 && iconId!=0) {
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
    }

}


