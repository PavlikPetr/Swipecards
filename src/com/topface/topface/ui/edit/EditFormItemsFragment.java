package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.QuestionaryRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;

public class EditFormItemsFragment extends AbstractEditFragment {

    private static int mTitleId;
    private static int mDataId;
    private static final String ARG_TAG_TITLE_ID = "title_id";
    private static final String ARG_TAG_DATA_ID = "data_id";
    private static final String ARG_TAG_DATA = "data";
    private String mData;
    private FormInfo mFormInfo;
    private static int mSeletedDataId;
    private static Profile mProfile;

    private ListView mListView;
    private FormCheckingDataAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            mTitleId = getArguments().getInt(ARG_TAG_TITLE_ID);
            mDataId = getArguments().getInt(ARG_TAG_DATA_ID);
            mSeletedDataId = mDataId;
            mData = getArguments().getString(ARG_TAG_DATA);
        } else {
            mTitleId = FormItem.NO_RESOURCE_ID;
            mDataId = FormItem.NO_RESOURCE_ID;
            mSeletedDataId = mDataId;
            mData = Static.EMPTY;
            mProfile = CacheProfile.getProfile();
        }

        mProfile = CacheProfile.getProfile();

        mFormInfo = new FormInfo(getActivity(), mProfile);

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ac_edit_with_listview, container,
                false);

        // Navigation bar
        ActionBar actionBar = getActionBar(root);
        actionBar.setTitleText(getString(R.string.edit_title));
        String formItemTitle = mFormInfo.getFormTitle(mTitleId);
        actionBar.setSubTitleText(formItemTitle);

        actionBar.showBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mRightPrsBar = actionBar.getRightProgressBar();

        // List
        mListView = (ListView) root.findViewById(R.id.lvList);

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.item_edit_profile_form_header,
                mListView, false);
        ((TextView) header.findViewById(R.id.tvTitle)).setText(formItemTitle);
        mListView.addHeaderView(header);

        mAdapter = new FormCheckingDataAdapter(getActivity().getApplicationContext(),
                mFormInfo.getEntriesByTitleId(mTitleId, new String[]{mData}),
                mFormInfo.getIdsByTitleId(mTitleId), mSeletedDataId);
        mListView.setAdapter(mAdapter);
        return root;
    }

    private void setSelectedId(int id) {
        mSeletedDataId = id;
        refreshSaveState();
    }

    @Override
    protected void saveChanges(final Handler handler) {
        if (hasChanges()) {
            for (int i = 0; i < CacheProfile.forms.size(); i++) {
                if (CacheProfile.forms.get(i).titleId == mTitleId) {
                    final FormItem item = CacheProfile.forms.get(i);
                    FormItem newItem;
                    newItem = new FormItem(item.titleId, mSeletedDataId, FormItem.DATA);

                    mFormInfo.fillFormItem(newItem);

                    prepareRequestSend();
                    QuestionaryRequest request = mFormInfo.getFormRequest(newItem);
                    registerRequest(request);
                    request.callback(new ApiHandler() {

                        @Override
                        public void success(ApiResponse response) {
                            item.dataId = mSeletedDataId;
                            mFormInfo.fillFormItem(item);
                            getActivity().setResult(Activity.RESULT_OK);
                            mDataId = mSeletedDataId;
                            finishRequestSend();
                            handler.sendEmptyMessage(0);
                        }

                        @Override
                        public void fail(int codeError, ApiResponse response) {
                            Activity activity = getActivity();
                            if (activity != null) {
                                getActivity().setResult(Activity.RESULT_CANCELED);
                                finishRequestSend();
                                handler.sendEmptyMessage(0);
                            }
                        }
                    }).exec();
                    break;
                }
            }
        } else {
            handler.sendEmptyMessage(0);
        }
    }

    @Override
    public boolean hasChanges() {
        return mDataId != mSeletedDataId;
    }

    private class FormCheckingDataAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private String[] mListData;
        private int[] mIds;
        private int mLastSelected;

        public FormCheckingDataAdapter(Context context, String[] data, int[] ids, int selectedId) {
            mInflater = LayoutInflater.from(context);
            mListData = data;
            mIds = ids;
            mLastSelected = getSelectedIndex(selectedId);
        }

        private int getSelectedIndex(int selectedId) {
            for (int i = 0; i < mIds.length; i++) {
                if (mIds[i] == selectedId) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getCount() {
            return mListData.length;
        }

        @Override
        public String getItem(int position) {
            return mListData[position];
        }

        @Override
        public long getItemId(int position) {
            return mIds[position];
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.item_edit_form_check, null, false);
                holder.mTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                holder.mBackground = (ImageView) convertView.findViewById(R.id.ivEditBackground);
                holder.mCheck = (ImageView) convertView.findViewById(R.id.ivCheck);


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            if (position == 0) {
                holder.mBackground.setImageDrawable(getResources().getDrawable(
                        R.drawable.edit_big_btn_top_selector));
            } else if (position == getCount() - 1) {
                holder.mBackground.setImageDrawable(getResources().getDrawable(
                        R.drawable.edit_big_btn_bottom_selector));
            } else {
                holder.mBackground.setImageDrawable(getResources().getDrawable(
                        R.drawable.edit_big_btn_middle_selector));
            }

            if (mLastSelected == position) {
                holder.mCheck.setVisibility(View.VISIBLE);
            } else {
                holder.mCheck.setVisibility(View.INVISIBLE);
            }

            holder.mTitle.setText(getItem(position));

            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLastSelected = position;
                    setSelectedId((int) getItemId(position));
                    notifyDataSetChanged();
                }
            });

            convertView.setEnabled(mListView.isEnabled());
            return convertView;
        }

        class ViewHolder {
            TextView mTitle;
            ImageView mBackground;
            ImageView mCheck;
        }
    }

    @Override
    protected void lockUi() {
        mListView.setEnabled(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void unlockUi() {
        mListView.setEnabled(true);
    }

    public static EditFormItemsFragment newInstance(int titleId, int dataId, String data) {
        EditFormItemsFragment fragment = new EditFormItemsFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_TAG_TITLE_ID, titleId);
        args.putInt(ARG_TAG_DATA_ID, dataId);
        args.putString(ARG_TAG_DATA, data);
        fragment.setArguments(args);

        return fragment;
    }
}