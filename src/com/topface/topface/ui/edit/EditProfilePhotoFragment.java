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
import android.widget.AdapterView.OnItemClickListener;
import com.topface.topface.R;
import com.topface.topface.data.Photos;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.MainRequest;
import com.topface.topface.ui.profile.ProfilePhotoGridAdapter;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;

public class EditProfilePhotoFragment extends AbstractEditFragment implements OnItemClickListener {

    private ProfilePhotoGridAdapter mPhotoGridAdapter;
    private int mLastSelectedId;
    private int mSelectedId;

    private GridView mPhotoGridView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedId = CacheProfile.photo.getId();
        mLastSelectedId = mSelectedId;
        mPhotoGridAdapter = new EditProfileDrigAdapter(
                getActivity().getApplicationContext(), CacheProfile.photos);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_grid, container, false);

        // Navigation bar
        ((TextView) getActivity().findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_title);
        TextView subTitle = (TextView) getActivity().findViewById(R.id.tvNavigationSubtitle);
        subTitle.setVisibility(View.VISIBLE);
        subTitle.setText(R.string.edit_profile_photo);

        mSaveButton = (Button) getActivity().findViewById(R.id.btnNavigationRightWithText);
        mSaveButton.setText(getResources().getString(R.string.navigation_save));
        mSaveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveChanges(null);
            }
        });
        mRightPrsBar = (ProgressBar) getActivity().findViewById(R.id.prsNavigationRight);

        getActivity().findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
        mBackButton = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setText(R.string.navigation_edit);
        mBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mPhotoGridView = (GridView) root.findViewById(R.id.fragmentGrid);
        mPhotoGridView.setNumColumns(3);
        mPhotoGridView.setAdapter(mPhotoGridAdapter);
        mPhotoGridView.setOnItemClickListener(this);

        TextView title = (TextView) root.findViewById(R.id.fragmentTitle);
        title.setVisibility(View.INVISIBLE);

        return root;
    }

    @Override
    protected boolean hasChanges() {
        return mSelectedId != mLastSelectedId;
    }

    @Override
    protected void saveChanges(Handler handler) {
        prepareRequestSend();
        MainRequest request = new MainRequest(getActivity().getApplicationContext());
        registerRequest(request);
        request.photoid = mSelectedId;
        request.callback(new ApiHandler() {

            @Override
            public void success(ApiResponse response) throws NullPointerException {
                //TODO: Обновлять текущую автарку пользователя
                getActivity().setResult(Activity.RESULT_OK);
                mSelectedId = mLastSelectedId;
                finishRequestSend();
            }

            @Override
            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                getActivity().setResult(Activity.RESULT_CANCELED);
                finishRequestSend();
            }
        }).exec();
    }

    class EditProfileDrigAdapter extends ProfilePhotoGridAdapter {

        public EditProfileDrigAdapter(Context context,
                                      Photos photoLinks) {
            super(context, photoLinks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_user_gallery, null, false);
                holder = new ViewHolder();
                holder.photo = (ImageViewRemote) convertView.findViewById(R.id.ivPhoto);
                holder.selector = (ImageView) convertView.findViewById(R.id.ivSelector);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.photo.setPhoto(getItem(position));
            if (mLastSelectedId == position) {
                holder.selector.setVisibility(View.VISIBLE);
            } else {
                holder.selector.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        class ViewHolder {
            ImageViewRemote photo;
            ImageView selector;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        mLastSelectedId = position;
        mPhotoGridAdapter.notifyDataSetChanged();
        refreshSaveState();
    }

    @Override
    protected void lockUi() {
        mPhotoGridView.setEnabled(false);
    }

    @Override
    protected void unlockUi() {
        mPhotoGridView.setEnabled(true);
    }


}
