package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
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

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.AlbumPhotos;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoDeleteRequest;
import com.topface.topface.requests.PhotoMainRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.fragments.profile.PhotoSwitcherActivity;
import com.topface.topface.ui.fragments.profile.ProfilePhotoGridAdapter;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class EditProfilePhotoFragment extends AbstractEditFragment {

    private static final String PHOTOS = "PHOTOS";
    private static final String POSITION = "POSITION";
    private static final String FLIPPER_VISIBLE_CHILD = "FLIPPER_VISIBLE_CHILD";

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

                //Увеличиваем общее количество фотографий юзера
                CacheProfile.totalPhotos += 1;

                mPhotoLinks.addFirst(photo);
                Toast.makeText(App.getContext(), R.string.photo_add_or, Toast.LENGTH_SHORT).show();
                if (activity == null) {
                    Intent intent = new Intent(CacheProfile.PROFILE_UPDATE_ACTION);
                    LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
                } else {
                    activity.setResult(Activity.RESULT_OK);
                }
            } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                Toast.makeText(App.getContext(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
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
        if (CacheProfile.photos != null) {
            mPhotoLinks.addAll(CacheProfile.photos);
        }
        mPhotoGridAdapter = new EditProfileGridAdapter(
                getActivity().getApplicationContext(), mPhotoLinks, CacheProfile.totalPhotos, new LoadingListAdapter.Updater() {
            @Override
            public void onUpdate() {
                sendAlbumRequest();
            }
        });
    }

    private void sendAlbumRequest() {
        if (mPhotoLinks == null || mPhotoLinks.size() < 2 || !mPhotoGridAdapter.getLastItem().isFake()) {
            return;
        }
        Photo photo = mPhotoGridAdapter.getItem(mPhotoLinks.size() - 2);
        int position = photo.getPosition();
        AlbumRequest request = new AlbumRequest(
                getActivity(),
                CacheProfile.uid,
                position + 1,
                AlbumRequest.MODE_ALBUM,
                AlbumLoadController.FOR_GALLERY
        );
        request.callback(new DataApiHandler<AlbumPhotos>() {

            @Override
            protected void success(AlbumPhotos data, IApiResponse response) {
                if (mPhotoGridAdapter != null) {

                    mPhotoGridAdapter.addPhotos(data, data.more, false);
                }
            }

            @Override
            protected AlbumPhotos parseResponse(ApiResponse response) {
                return new AlbumPhotos(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Utils.showErrorMessage();
            }
        }).exec();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_profile_photos, container, false);

        mLoadingLocker = root.findViewById(R.id.fppLocker);
        mAddPhotoHelper = new AddPhotoHelper(this, mLoadingLocker);
        mAddPhotoHelper.setOnResultHandler(mHandler);

        mViewFlipper = (ViewFlipper) root.findViewById(R.id.vfFlipper);

        int position = 0;
        if (savedInstanceState != null) {
            try {
                mPhotoGridAdapter.setData(new Photos(
                        new JSONArray(savedInstanceState.getString(PHOTOS))), false, false);
            } catch (JSONException e) {
                Debug.error(e);
            }
            position = savedInstanceState.getInt(POSITION, 0);
            mViewFlipper.setDisplayedChild(savedInstanceState.getInt(FLIPPER_VISIBLE_CHILD, 0));
        }

        mPhotoGridView = (GridView) root.findViewById(R.id.usedGrid);
        mPhotoGridView.setSelection(position);
        mPhotoGridView.setAdapter(mPhotoGridAdapter);
        mPhotoGridView.setOnItemClickListener(mOnItemClickListener);
        mPhotoGridView.setOnScrollListener(mPhotoGridAdapter);

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
    public void onResume() {
        if (mPhotoGridAdapter.getPhotoLinks() == null || mPhotoGridAdapter.getPhotoLinks().size() == 0) {
            mPhotoGridAdapter.updateData();
            mPhotoGridAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putString(PHOTOS, mPhotoGridAdapter.getAdaprerData().toJson().toString());
        } catch (JSONException e) {
            Debug.error(e);
        }
        outState.putInt(POSITION, mPhotoGridView.getFirstVisiblePosition());
        outState.putInt(FLIPPER_VISIBLE_CHILD, mViewFlipper.getDisplayedChild());
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
                        CacheProfile.totalPhotos -= mDeleted.size();
                        mDeleted.clear();
                        CacheProfile.sendUpdateProfileBroadcast();

                        Intent intent = new Intent(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT);
                        intent.putExtra(PhotoSwitcherActivity.INTENT_PHOTOS, CacheProfile.photos);
                        intent.putExtra(PhotoSwitcherActivity.INTENT_CLEAR, true);
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

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
                        CacheProfile.photo = mPhotoGridAdapter.getPhotoLinks().getPhotoById(mLastSelectedAsMainId);
                        mSelectedAsMainId = mLastSelectedAsMainId;
                        CacheProfile.sendUpdateProfileBroadcast();
                        finishOperations(handler);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        if (getActivity() != null) {
                            if (codeError == ErrorCodes.NON_EXIST_PHOTO_ERROR) {
                                Photo removedPhoto = mPhotoLinks.getPhotoById(mLastSelectedAsMainId);
                                mPhotoGridAdapter.getAdaprerData().remove(removedPhoto);
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
                                      Photos photoLinks, int photoSize, LoadingListAdapter.Updater listener) {
            super(context, photoLinks, photoSize, listener);
        }

        @Override
        protected boolean isAddPhotoButtonEnabled() {
            return true;
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
            if (item.isFake()) {
                holder.mBtnDelete.setVisibility(View.INVISIBLE);
                holder.mBtnSetAsMain.setVisibility(View.INVISIBLE);
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
