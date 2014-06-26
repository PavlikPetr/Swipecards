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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoDeleteRequest;
import com.topface.topface.requests.PhotoMainRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.fragments.profile.ProfilePhotoGridAdapter;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.CacheProfile;

import java.util.ArrayList;

public class EditProfilePhotoFragment extends AbstractEditFragment {

    private ArrayList<Photo> mDeleted = new ArrayList<>();

    private EditProfileGridAdapter mPhotoGridAdapter;
    private int mLastSelectedAsMainId;
    private int mSelectedAsMainId;

    private GridView mPhotoGridView;
    private Photos mPhotoLinks;

    private AddPhotoHelper mAddPhotoHelper;

    private ViewFlipper mViewFlipper;
    private View mLoadingLocker;
    private boolean mOperationsFinished = true;
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                mViewFlipper.setDisplayedChild(1);
            }
        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mViewFlipper.setDisplayedChild(0);
            Activity activity = getActivity();
            if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_OK) {
                final Photo photo = (Photo) msg.obj;
                if (CacheProfile.photos.isEmpty()) {
                    mPhotoLinks.addFirst(photo);
                    CacheProfile.photo = photo;
                    mLastSelectedAsMainId = photo.getId();
                    CacheProfile.photos.addFirst(photo);
                    mPhotoGridAdapter.addFirst(photo);
                    CacheProfile.sendUpdateProfileBroadcast();
                    PhotoMainRequest request = new PhotoMainRequest(getActivity());
                    request.photoId = photo.getId();
                    request.callback(new SimpleApiHandler()).exec();
                } else {
                    CacheProfile.photos.addFirst(photo);
                    mPhotoGridAdapter.addFirst(photo);
                }

                if (activity != null) {
                    Toast.makeText(activity, R.string.photo_add_or, Toast.LENGTH_SHORT).show();
                    activity.setResult(Activity.RESULT_OK);
                }
            } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR && activity != null) {
                Toast.makeText(activity, R.string.photo_add_error, Toast.LENGTH_SHORT).show();
            }
        }
    };

    public EditProfilePhotoFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedAsMainId = CacheProfile.photo == null ? -1 : CacheProfile.photo.getId();
        mLastSelectedAsMainId = mSelectedAsMainId;
        CacheProfile.sendUpdateProfileBroadcast();
        mPhotoLinks = new Photos();
        mPhotoLinks.add(null);
        if (CacheProfile.photos != null) {
            mPhotoLinks.addAll(CacheProfile.photos);
        }
        mPhotoGridAdapter = new EditProfileGridAdapter(
                getActivity().getApplicationContext(), mPhotoLinks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_profile_photos, container, false);

        mLoadingLocker = root.findViewById(R.id.fppLocker);
        mAddPhotoHelper = new AddPhotoHelper(this, mLoadingLocker);
        mAddPhotoHelper.setOnResultHandler(mHandler);

        mViewFlipper = (ViewFlipper) root.findViewById(R.id.vfFlipper);

        mPhotoGridView = (GridView) root.findViewById(R.id.usedGrid);
        mPhotoGridView.setAdapter(mPhotoGridAdapter);
        mPhotoGridView.setOnItemClickListener(mOnItemClickListener);

        TextView title = (TextView) root.findViewById(R.id.usedTitle);
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

    @Override
    protected void saveChanges(final Handler handler) {
        if (hasChanges()) {
            prepareRequestSend();
            mLoadingLocker.setVisibility(View.VISIBLE);
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
                    public void success(IApiResponse response) {
                        CacheProfile.photos.removeAll(mDeleted);
                        mDeleted.clear();
                        CacheProfile.sendUpdateProfileBroadcast();
                        finishOperations(handler);

                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
//                        finishOperations(handler);
                        warnEditingFailed(handler);
                    }


                    @Override
                    public void always(IApiResponse response) {
                        super.always(response);
                        if (mLoadingLocker != null) {
                            mLoadingLocker.setVisibility(View.GONE);
                        }
                    }
                }).exec();
            }

            if (mSelectedAsMainId != mLastSelectedAsMainId) {
                PhotoMainRequest setAsPhotoMainRequest = new PhotoMainRequest(getActivity());
                registerRequest(setAsPhotoMainRequest);
                setAsPhotoMainRequest.photoId = mLastSelectedAsMainId;
                setAsPhotoMainRequest.callback(new ApiHandler() {

                    @Override
                    public void success(IApiResponse response) {
                        CacheProfile.photo = mPhotoLinks.getByPhotoId(mLastSelectedAsMainId);
                        mSelectedAsMainId = mLastSelectedAsMainId;
                        CacheProfile.sendUpdateProfileBroadcast();
                        finishOperations(handler);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        if (getActivity() != null) {
                            if (codeError == ErrorCodes.NON_EXIST_PHOTO_ERROR) {
                                Photo removedPhoto = mPhotoLinks.getByPhotoId(mLastSelectedAsMainId);
                                mPhotoGridAdapter.getData().remove(removedPhoto);
                                mPhotoGridAdapter.notifyDataSetChanged();
                                if (CacheProfile.photos.contains(removedPhoto)) {
                                    CacheProfile.photos.remove(removedPhoto);
                                    Toast.makeText(App.getContext(), R.string.general_photo_deleted, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        warnEditingFailed(handler);
                    }

                    @Override
                    public void always(IApiResponse response) {
                        super.always(response);
                        if (mLoadingLocker != null) {
                            mLoadingLocker.setVisibility(View.GONE);
                        }
                    }
                }).exec();
            } else {
                mSelectedAsMainId = mLastSelectedAsMainId;
                finishOperations(handler);
            }

        } else {
            handler.sendEmptyMessage(0);
        }
    }

    private synchronized void finishOperations(Handler handler) {
        getActivity().setResult(Activity.RESULT_OK);
        if (mOperationsFinished) {
            finishRequestSend();
            handler.sendEmptyMessage(0);
        } else {
            mOperationsFinished = true;
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
        mAddPhotoHelper.processActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.edit_title);
    }

    @Override
    protected String getSubtitle() {
        return getString(R.string.edit_album);
    }

    class EditProfileGridAdapter extends ProfilePhotoGridAdapter {

        public EditProfileGridAdapter(Context context,
                                      Photos photoLinks) {
            super(context, photoLinks);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            final Photo item = getItem(position);
            int type = getItemViewType(position);

            if (convertView == null) {
                if (type == T_ADD_BTN) {
                    convertView = mInflater.inflate(R.layout.item_user_gallery_add_btn, null, false);
                    return convertView;
                }
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
                if (type == T_ADD_BTN) return convertView;
                holder = (ViewHolder) convertView.getTag();
            }

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
}
