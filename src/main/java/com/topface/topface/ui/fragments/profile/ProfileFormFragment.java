package com.topface.topface.ui.fragments.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.topface.topface.R;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormItem;

public class ProfileFormFragment extends ProfileInnerFragment {

    private static final String FORM_ITEMS = "FORM_ITEMS";
    private static final String POSITION = "POSITION";

    private ListView mFormListView;

    View.OnClickListener mOnFillClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Object formItem = view.getTag();
            if (formItem instanceof FormItem) {
                FormItem item = (FormItem) formItem;

                if (item.type == FormItem.STATUS) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), EditContainerActivity.class);
                    startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_STATUS);
                } else if (item.dataId == FormItem.NO_RESOURCE_ID) {
                    Intent intent = new Intent(getActivity().getApplicationContext(),
                            EditContainerActivity.class);
                    intent.putExtra(EditContainerActivity.INTENT_FORM_TITLE_ID, item.titleId);
                    intent.putExtra(EditContainerActivity.INTENT_FORM_DATA_ID, item.dataId);
                    intent.putExtra(EditContainerActivity.INTENT_FORM_DATA, item.value);
                    startActivityForResult(intent,
                            EditContainerActivity.INTENT_EDIT_INPUT_FORM_ITEM);
                } else {
                    Intent intent = new Intent(getActivity().getApplicationContext(),
                            EditContainerActivity.class);
                    intent.putExtra(EditContainerActivity.INTENT_FORM_TITLE_ID, item.titleId);
                    intent.putExtra(EditContainerActivity.INTENT_FORM_DATA_ID, item.dataId);
                    intent.putExtra(EditContainerActivity.INTENT_FORM_DATA, item.value);
                    startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_FORM_ITEM);
                }
            }
        }
    };
    private ProfileFormListAdapter mProfileFormListAdapter;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mProfileFormListAdapter != null) {
                mProfileFormListAdapter.refillData();
                mProfileFormListAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProfileFormListAdapter = new ProfileFormListAdapter(getActivity());
        mProfileFormListAdapter.setOnFillListener(mOnFillClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_form, container, false);
        mFormListView = (ListView) root.findViewById(R.id.fragmentFormList);
        mFormListView.setAdapter(mProfileFormListAdapter);

        View titleLayout = root.findViewById(R.id.loUserTitle);
        titleLayout.setVisibility(View.GONE);
        (root.findViewById(R.id.ivDivider)).setVisibility(View.GONE);

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            mProfileFormListAdapter.restoreState(savedInstanceState.getParcelableArrayList(FORM_ITEMS));
            mProfileFormListAdapter.notifyDataSetChanged();
            mFormListView.setSelection(savedInstanceState.getInt(POSITION, 0));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(FORM_ITEMS, mProfileFormListAdapter.saveState());
        outState.putInt(POSITION, mFormListView.getFirstVisiblePosition());
    }
}
