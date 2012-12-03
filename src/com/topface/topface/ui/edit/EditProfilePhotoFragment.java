package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.MainRequest;
import com.topface.topface.requests.PhotoDeleteRequest;
import com.topface.topface.ui.profile.AddPhotoHelper;
import com.topface.topface.ui.profile.ProfilePhotoGridAdapter;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;

import java.util.ArrayList;

public class EditProfilePhotoFragment extends AbstractEditFragment {

    private ArrayList<Photo> mDeleted = new ArrayList<Photo>();

    private ProfilePhotoGridAdapter mPhotoGridAdapter;
    private int mLastSelectedAsMainId;
    private int mSelectedAsMainId;

    private GridView mPhotoGridView;
    private Photos mPhotoLinks;

    private AddPhotoHelper mAddPhotoHelper;

    private ViewFlipper mViewFlipper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedAsMainId = CacheProfile.photo == null ? -1 : CacheProfile.photo.getId();
        mLastSelectedAsMainId = mSelectedAsMainId;

        mPhotoLinks = new Photos();
        mPhotoLinks.add(null);
        if (CacheProfile.photos != null) {
            mPhotoLinks.addAll(CacheProfile.photos);
        }
        mPhotoGridAdapter = new EditProfileGrigAdapter(
                getActivity().getApplicationContext(), mPhotoLinks);

        mAddPhotoHelper = new AddPhotoHelper(this);
        mAddPhotoHelper.setOnResultHandler(mHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_profile_photos, container, false);

        // Navigation bar
        ((TextView) getActivity().findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_title);
        TextView subTitle = (TextView) getActivity().findViewById(R.id.tvNavigationSubtitle);
        subTitle.setVisibility(View.VISIBLE);
        subTitle.setText(R.string.edit_album);

        mRightPrsBar = (ProgressBar) getActivity().findViewById(R.id.prsNavigationRight);

        getActivity().findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
        mBackButton = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setText(R.string.general_edit_button);
        mBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mViewFlipper = (ViewFlipper) root.findViewById(R.id.vfFlipper);

        mPhotoGridView = (GridView) root.findViewById(R.id.fragmentGrid);
        mPhotoGridView.setAdapter(mPhotoGridAdapter);
        mPhotoGridView.setOnItemClickListener(mOnItemClickListener);

        TextView title = (TextView) root.findViewById(R.id.fragmentTitle);
        title.setVisibility(View.GONE);

        root.findViewById(R.id.btnAddPhotoAlbum).setOnClickListener(mAddPhotoHelper.getAddPhotoClickListener());
        root.findViewById(R.id.btnAddPhotoCamera).setOnClickListener(mAddPhotoHelper.getAddPhotoClickListener());
        root.findViewById(R.id.btnCancel).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mViewFlipper.setDisplayedChild(0);
            }
        });

        return root;
    }

    @Override
    protected boolean hasChanges() {
        return (mSelectedAsMainId != mLastSelectedAsMainId) || !mDeleted.isEmpty();
    }

    private boolean mOperationsFinished = true;

    @Override
    protected void saveChanges(final Handler handler) {
        if (hasChanges()) {
            prepareRequestSend();

            if (!mDeleted.isEmpty()) {
                mOperationsFinished = false;
                PhotoDeleteRequest deleteRequest = new PhotoDeleteRequest(getActivity());
                registerRequest(deleteRequest);

                int[] photoIds = new int[mDeleted.size()];
                for (int i = 0; i < photoIds.length; i++) {
                    photoIds[i] = mDeleted.get(i).getId();
                }
                deleteRequest.photos = photoIds;

                deleteRequest.callback(new ApiHandler() {

                    @Override
                    public void success(ApiResponse response) throws NullPointerException {
                        CacheProfile.photos.removeAll(mDeleted);
                        mDeleted.clear();
                        finishOperations(handler);
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) throws NullPointerException {
                        finishOperations(handler);
                    }
                }).exec();
            }

            if (mSelectedAsMainId != mLastSelectedAsMainId) {
                MainRequest setAsMainRequest = new MainRequest(getActivity());
                registerRequest(setAsMainRequest);
                setAsMainRequest.photoid = mLastSelectedAsMainId;
                setAsMainRequest.callback(new ApiHandler() {

                    @Override
                    public void success(ApiResponse response) throws NullPointerException {
                        CacheProfile.photo = mPhotoLinks.getByPhotoId(mLastSelectedAsMainId);
                        getActivity().setResult(Activity.RESULT_OK);
                        mSelectedAsMainId = mLastSelectedAsMainId;
                        finishOperations(handler);
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) throws NullPointerException {
                        getActivity().setResult(Activity.RESULT_CANCELED);
                        finishOperations(handler);

                    }
                }).exec();
            } else {
                getActivity().setResult(Activity.RESULT_OK);
                mSelectedAsMainId = mLastSelectedAsMainId;
                finishOperations(handler);
            }

        } else {
            handler.sendEmptyMessage(0);
        }
    }

    private synchronized void finishOperations(Handler handler) {
        if (mOperationsFinished) {
            finishRequestSend();
            handler.sendEmptyMessage(0);
        } else {
            mOperationsFinished = true;
        }
    }

    class EditProfileGrigAdapter extends ProfilePhotoGridAdapter {

        public EditProfileGrigAdapter(Context context,
                                      Photos photoLinks) {
            super(context, photoLinks);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            final Photo item = getItem(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_edit_user_gallery, null, false);
                holder = new ViewHolder();
                holder.photo = (ImageViewRemote) convertView.findViewById(R.id.ivPhoto);
                holder.mBtnSetAsMain = (Button) convertView.findViewById(R.id.btnSetAsMain);
                holder.mBtnDelete = (Button) convertView.findViewById(R.id.btnDeletePhoto);
                holder.mBtnRestore = (Button) convertView.findViewById(R.id.btnRestorePhoto);
                holder.mBtnSelectedAsMain = (Button) convertView.findViewById(R.id.btnSetAsMainSelected);
                holder.mShadow = (ImageView) convertView.findViewById(R.id.ivShadow);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (getItemViewType(position) == T_ADD_BTN) {
                holder.photo.setBackgroundResource(R.drawable.profile_add_photo_selector);
                holder.mBtnSetAsMain.setVisibility(View.INVISIBLE);
                holder.mBtnDelete.setVisibility(View.INVISIBLE);
                holder.mBtnSelectedAsMain.setVisibility(View.INVISIBLE);
                holder.mShadow.setVisibility(View.INVISIBLE);
            } else {
                final int itemId = item.getId();
                holder.photo.setPhoto(item);
                if (mDeleted.contains(item)) {
                    holder.mBtnDelete.setVisibility(View.INVISIBLE);
                    holder.mBtnSetAsMain.setVisibility(View.INVISIBLE);
                    holder.mBtnRestore.setVisibility(View.VISIBLE);
                    holder.mBtnSelectedAsMain.setVisibility(View.INVISIBLE);
                    holder.mBtnRestore.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            mDeleted.remove(item);
                            notifyDataSetChanged();
                        }
                    });
                } else {
                    holder.mBtnRestore.setVisibility(View.INVISIBLE);

                    holder.mBtnDelete.setVisibility(View.VISIBLE);
                    holder.mBtnDelete.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (itemId == mLastSelectedAsMainId) {
                                mLastSelectedAsMainId = mSelectedAsMainId;
                            }
                            mDeleted.add(item);
                            notifyDataSetChanged();
                        }
                    });

                    if (mLastSelectedAsMainId == itemId) {
                        holder.mBtnSetAsMain.setVisibility(View.INVISIBLE);
                        holder.mBtnSelectedAsMain.setVisibility(View.VISIBLE);
                        holder.mBtnDelete.setVisibility(View.GONE);
                    } else {
                        holder.mBtnSelectedAsMain.setVisibility(View.INVISIBLE);
                        holder.mBtnSetAsMain.setVisibility(View.VISIBLE);
                        holder.mBtnSetAsMain.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mLastSelectedAsMainId = itemId;
                                notifyDataSetChanged();
                            }
                        });
                    }
                }
            }

            return convertView;
        }

        class ViewHolder {
            ImageViewRemote photo;
            ImageView mShadow;
            Button mBtnDelete;
            Button mBtnSetAsMain;
            Button mBtnRestore;
            Button mBtnSelectedAsMain;
        }
    }

    @Override
    protected void lockUi() {
        mPhotoGridView.setEnabled(false);
    }

    @Override
    protected void unlockUi() {
        mPhotoGridView.setEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAddPhotoHelper.checkActivityResult(requestCode, resultCode, data);
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                mViewFlipper.setDisplayedChild(1);
                return;
            }
            Data.photos = CacheProfile.photos;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mViewFlipper.setDisplayedChild(0);
            if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_OK) {
                Photo photo = (Photo) msg.obj;

                CacheProfile.photos.addFirst(photo);
                mPhotoLinks.add(1, photo);

                mPhotoGridAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), R.string.photo_add_or, Toast.LENGTH_SHORT).show();
            } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                Toast.makeText(getActivity(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
            }
        }
    };
}
