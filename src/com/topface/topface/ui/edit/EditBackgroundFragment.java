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
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.http.ProfileBackgrounds;
import com.topface.topface.utils.http.ProfileBackgrounds.BackgroundItem;
import com.topface.topface.utils.http.ProfileBackgrounds.ResourceBackgroundItem;

import java.util.LinkedList;
import java.util.List;

public class EditBackgroundFragment extends AbstractEditFragment {

    private int mSelectedId;
    private int mSelectedIndex;
    private ListView mBackgroundImagesListView;
    private BackgroundImagesAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ac_edit_with_listview, container, false);

        mSelectedId = CacheProfile.background_id;

        // Navigation bar
        ActionBar actionBar = getActionBar(root);
        actionBar.setTitleText(getString(R.string.edit_title));
        actionBar.setSubTitleText(getString(R.string.edit_bg_photo));

        actionBar.showBackButton(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mRightPrsBar = actionBar.getRightProgressBar();

        // List
        mBackgroundImagesListView = (ListView) root.findViewById(R.id.lvList);
        mAdapter = new BackgroundImagesAdapter(getActivity().getApplicationContext(), getBackgroundImagesList());
        mBackgroundImagesListView.setAdapter(mAdapter);

        return root;
    }

    private List<BackgroundItem> getBackgroundImagesList() {
        List<BackgroundItem> result = ProfileBackgrounds.getBackgroundItems(getActivity().getApplicationContext(),
                CacheProfile.background_id);
        for (int i=0;i<result.size();i++) {
            if (result.get(i) instanceof ResourceBackgroundItem) {
                ResourceBackgroundItem item = (ResourceBackgroundItem) result.get(i);
                if (item instanceof ResourceBackgroundItem) {
                    if (mSelectedId == item.getId()) {
                        mSelectedIndex = i;
                    }
                }
            }


        }
        return result;
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

        private List<BackgroundItem> mData;
        private LayoutInflater mInflater;

        public BackgroundImagesAdapter(Context context, List<BackgroundItem> data) {
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

            if (mSelectedIndex == position) {
                holder.mSelected.setVisibility(View.VISIBLE);
                convertView.setOnClickListener(null);
            } else {
                holder.mSelected.setVisibility(View.GONE);
                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (item.isForVip()) {
                            if (CacheProfile.premium) {
                                select(item,position);
                                notifyDataSetChanged();
                            } else {
                                Intent intent = new Intent(getActivity().getApplicationContext(), ContainerActivity.class);
                                startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                            }
                        } else {
                            select(item, position);
                            notifyDataSetChanged();
                        }
                    }
                });
            }

            convertView.setEnabled(mBackgroundImagesListView.isEnabled());
            return convertView;
        }

        private void select(BackgroundItem item,int position) {
            mSelectedIndex = position;
            setSelectedBackground(item);
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