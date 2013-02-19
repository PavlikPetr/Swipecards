package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.http.ProfileBackgrounds;
import com.topface.topface.utils.http.ProfileBackgrounds.BackgroundItem;
import com.topface.topface.utils.http.ProfileBackgrounds.ResourceBackgroundItem;

import java.util.LinkedList;

public class EditBackgroundFragment extends AbstractEditFragment {

    private int mSelectedId;
    private ListView mBackgroundImagesListView;
    private BackgroundImagesAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ac_edit_with_listview, container, false);

        mSelectedId = CacheProfile.background_id;

        // Navigation bar
        ((TextView) getActivity().findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_title);
        TextView subTitle = (TextView) getActivity().findViewById(R.id.tvNavigationSubtitle);
        subTitle.setVisibility(View.VISIBLE);
        subTitle.setText(R.string.edit_bg_photo);

        getActivity().findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
        Button btnBack = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setText(R.string.general_edit_button);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mRightPrsBar = (ProgressBar) getActivity().findViewById(R.id.prsNavigationRight);

        // List
        mBackgroundImagesListView = (ListView) root.findViewById(R.id.lvList);
        mAdapter = new BackgroundImagesAdapter(getActivity().getApplicationContext(), getBackgroundImagesList());
        mBackgroundImagesListView.setAdapter(mAdapter);

        return root;
    }

    private LinkedList<BackgroundItem> getBackgroundImagesList() {
        return ProfileBackgrounds.getBackgroundItems(getActivity().getApplicationContext(),
                CacheProfile.background_id);
    }

    private void setSelectedBackground(BackgroundItem item) {
        if (item instanceof ResourceBackgroundItem) {
            mSelectedId = ((ResourceBackgroundItem) item).getId();
            refreshSaveState();
        }
    }

    @Override
    protected void saveChanges(final Handler handler) {
        if (hasChanges()) {
            prepareRequestSend();

            SettingsRequest request = new SettingsRequest(getActivity());
            registerRequest(request);
            request.background = mSelectedId;
            request.callback(new ApiHandler() {

                @Override
                public void success(ApiResponse response) {
                    CacheProfile.background_id = mSelectedId;
                    getActivity().setResult(Activity.RESULT_OK);
                    finishRequestSend();
                    handler.sendEmptyMessage(0);
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                    finishRequestSend();
                }
            }).exec();
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED);
            handler.sendEmptyMessage(0);
        }

    }

    @Override
    protected boolean hasChanges() {
        return CacheProfile.background_id != mSelectedId;
    }

    class BackgroundImagesAdapter extends BaseAdapter {

        private LinkedList<BackgroundItem> mData;
        private LayoutInflater mInflater;
        private int mSelectedIndex;

        public BackgroundImagesAdapter(Context context, LinkedList<BackgroundItem> data) {
            mData = data;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public BackgroundItem getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            final BackgroundItem item = getItem(position);

            if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.item_edit_background_photo, null, false);
                holder.mImageView = (ImageView) convertView.findViewById(R.id.ivBackgroundImage);
                holder.mFrameImageView = (ImageView) convertView.findViewById(R.id.ivBackgroundImageFrame);
                holder.mSelected = (ViewGroup) convertView.findViewById(R.id.loSelected);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            LayoutParams params = holder.mImageView.getLayoutParams();
            params.height = holder.mFrameImageView.getDrawable().getIntrinsicHeight() - 2;
            params.width = holder.mFrameImageView.getDrawable().getIntrinsicWidth() - 2;
            holder.mImageView.setImageBitmap(getItem(position).getBitmap());

            if (item.isSelected()) {
                holder.mSelected.setVisibility(View.VISIBLE);
                convertView.setOnClickListener(null);
                mSelectedIndex = position;
            } else {
                holder.mSelected.setVisibility(View.GONE);
                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (item.isForVip()) {
                            if (CacheProfile.premium) {
                                mData.get(mSelectedIndex).setSelected(false);
                                item.setSelected(true);
                                setSelectedBackground(item);
                                notifyDataSetChanged();
                            } else {
                                Intent intent = new Intent(getActivity().getApplicationContext(), ContainerActivity.class);
                                startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                            }
                        } else {
                            mData.get(mSelectedIndex).setSelected(false);
                            item.setSelected(true);
                            setSelectedBackground(item);
                            notifyDataSetChanged();
                        }
                    }
                });
            }

            convertView.setEnabled(mBackgroundImagesListView.isEnabled());
            return convertView;
        }

        class ViewHolder {
            ImageView mImageView;
            ImageView mFrameImageView;
            ViewGroup mSelected;
        }
    }

    @Override
    protected void lockUi() {
        mBackgroundImagesListView.setEnabled(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void unlockUi() {
        mBackgroundImagesListView.setEnabled(true);
    }
}