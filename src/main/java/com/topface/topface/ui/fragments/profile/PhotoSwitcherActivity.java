package com.topface.topface.ui.fragments.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.Static;
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
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.PreloadManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class PhotoSwitcherActivity extends BaseFragmentActivity {

    public static final String INTENT_MORE = "more";
    public static final String INTENT_CLEAR = "clear";
    public static final String DEFAULT_UPDATE_PHOTOS_INTENT = "com.topface.topface.updatePhotos";
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_ALBUM_POS = "album_position";
    public static final String INTENT_PHOTOS = "album_photos";
    public static final String INTENT_PHOTOS_COUNT = "photos_count";
    public static final String CONTROL_VISIBILITY = "CONTROL_VISIBILITY";
    public static final String OWN_PHOTOS_CONTROL_VISIBILITY = "OWN_PHOTOS_CONTROL_VISIBILITY";
    public static final String DELETED_PHOTOS = "DELETED_PHOTOS";
    public static final int DEFAULT_PRELOAD_ALBUM_RANGE = 3;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPhotoAlbumControl != null) {
                mPhotoAlbumControl.setVisibility(
                        mPhotoAlbumControl.getVisibility() == View.GONE ?
                                View.VISIBLE :
                                View.GONE
                );
                mPhotoAlbumControlVisibility = mPhotoAlbumControl.getVisibility();
                mOwnPhotosControlVisibility = mOwnPhotosControl.getVisibility();
            }
        }
    };
    private TextView mCounter;
    private ViewGroup mPhotoAlbumControl;
    private ViewGroup mOwnPhotosControl;
    private int mPhotoAlbumControlVisibility = View.GONE;
    private int mOwnPhotosControlVisibility = View.GONE;
    private Photos mPhotoLinks;
    private PreloadManager mPreloadManager;
    private Photos mDeletedPhotos = new Photos();
    private ImageSwitcher mImageSwitcher;
    private boolean mNeedMore;
    private boolean mCanSendAlbumReq = true;
    private int mUid;
    private int mLoadedCount;
    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            mPreloadManager.preloadPhoto(mPhotoLinks, position + 1);
            setCounter(position);
            refreshButtonsState();
            if (position + DEFAULT_PRELOAD_ALBUM_RANGE == mLoadedCount) {
                final Photos data = ((ImageSwitcher.ImageSwitcherAdapter) mImageSwitcher.getAdapter()).getData();
                if (mNeedMore) {
                    mImageSwitcher.getAdapter().notifyDataSetChanged();
                    if (mCanSendAlbumReq) {
                        mCanSendAlbumReq = false;
                        sendAlbumRequest(data);
                    }
                }
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };
    private int mCurrentPosition = 0;
    private TextView mSetAvatarButton;
    private ImageButton mDeleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_photos);
        mPreloadManager = new PreloadManager();
        // Extras
        Intent intent = getIntent();
        int photosCount = intent.getIntExtra(INTENT_PHOTOS_COUNT, 0);
        int position = intent.getIntExtra(INTENT_ALBUM_POS, 0);
        mUid = intent.getIntExtra(INTENT_USER_ID, -1);
        ArrayList<Photo> arrList = getPhotos(intent);

        mPhotoLinks = new Photos();
        mPhotoLinks.addAll(arrList);

        mNeedMore = photosCount > mPhotoLinks.size();

        if (mUid == -1) {
            Debug.log(this, "Intent param is wrong");
            finish();
            return;
        }

        // Title Header
        mCounter = ((TextView) findViewById(R.id.tvPhotoCounter));

        // Gallery
        mImageSwitcher = ((ImageSwitcher) findViewById(R.id.galleryAlbum));
        mImageSwitcher.setOnPageChangeListener(mOnPageChangeListener);
        mImageSwitcher.setOnClickListener(mOnClickListener);

        // Control layout
        mPhotoAlbumControl = (ViewGroup) findViewById(R.id.loPhotoAlbumControl);
        mOwnPhotosControl = (ViewGroup) mPhotoAlbumControl.findViewById(R.id.loBottomPanel);

        // - close button
        mPhotoAlbumControl.findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePhotoRequest();
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        mLoadedCount = mPhotoLinks.getRealPhotosCount();
        mNeedMore = photosCount > mLoadedCount;
        int rest = photosCount - mPhotoLinks.size();
        for (int i = 0; i < rest; i++) {
            mPhotoLinks.add(new Photo());
        }
        mImageSwitcher.setData(mPhotoLinks);
        mImageSwitcher.setCurrentItem(position, false);

        setCounter(position);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPhotoAlbumControl.setVisibility(mPhotoAlbumControlVisibility);
    }

    @Override
    protected void onLoadProfile() {
        super.onLoadProfile();
        initControls();
        refreshButtonsState();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CONTROL_VISIBILITY, mPhotoAlbumControl.getVisibility());
        outState.putInt(OWN_PHOTOS_CONTROL_VISIBILITY, mOwnPhotosControl.getVisibility());
        try {
            outState.putString(DELETED_PHOTOS, mDeletedPhotos.toJson().toString());
        } catch (JSONException e) {
            Debug.error(e);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPhotoAlbumControlVisibility = savedInstanceState.getInt(CONTROL_VISIBILITY, View.GONE);
        mOwnPhotosControlVisibility = savedInstanceState.getInt(OWN_PHOTOS_CONTROL_VISIBILITY, View.GONE);
        try {
            mDeletedPhotos = new Photos(new JSONArray(savedInstanceState.getString(DELETED_PHOTOS)));
        } catch (JSONException e) {
            Debug.error(e);
        }
    }

    @Override
    public void onBackPressed() {
        deletePhotoRequest();
        super.onBackPressed();
    }

    private void initControls() {
        if (mUid == CacheProfile.uid) {
            // - set avatar button
            mSetAvatarButton = (TextView) mPhotoAlbumControl.findViewById(R.id.btnSetAvatar);
            mSetAvatarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Photo currentPhoto = mPhotoLinks.get(mCurrentPosition);
                    if (CacheProfile.photo != null && currentPhoto != null && currentPhoto.getId() != CacheProfile.photo.getId()) {
                        if (!mDeletedPhotos.contains(currentPhoto)) {
                            setAsMainRequest(currentPhoto);
                        } else {
                            mDeletedPhotos.remove(currentPhoto);
                            refreshButtonsState();
                        }
                    }
                }
            });
            // - delete button
            mDeleteButton = (ImageButton) mPhotoAlbumControl.findViewById(R.id.btnDelete);
            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Photo currentPhoto = mPhotoLinks.get(mCurrentPosition);
                    if (currentPhoto != null) {
                        if (mDeletedPhotos.contains(currentPhoto)) {
                            mDeletedPhotos.removeById(currentPhoto.getId());
                        } else {
                            mDeletedPhotos.add(currentPhoto);
                        }
                        refreshButtonsState();
                    }
                }
            });
            if (mPhotoLinks.size() <= 1) mDeleteButton.setVisibility(View.GONE);
        } else {
            mPhotoAlbumControl.findViewById(R.id.loBottomPanel).setVisibility(mOwnPhotosControlVisibility);
        }
    }

    private void deletePhotoRequest() {
        if (mDeletedPhotos.isEmpty()) return;

        PhotoDeleteRequest request = new PhotoDeleteRequest(this);
        request.photos = mDeletedPhotos.getIdsArray();
        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                for (Photo currentPhoto : mDeletedPhotos) {
                    CacheProfile.photos.removeById(currentPhoto.getId());
                }
                LocalBroadcastManager.getInstance(PhotoSwitcherActivity.this).sendBroadcast(new Intent(DEFAULT_UPDATE_PHOTOS_INTENT)
                        .putExtra(INTENT_PHOTOS, CacheProfile.photos)
                        .putExtra(INTENT_MORE, CacheProfile.photos.size() < CacheProfile.totalPhotos - mDeletedPhotos.size())
                        .putExtra(INTENT_CLEAR, true));
                mDeletedPhotos.clear();
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Toast.makeText(PhotoSwitcherActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
            }
        }).exec();
    }

    private void setAsMainRequest(final Photo currentPhoto) {
        PhotoMainRequest request = new PhotoMainRequest(this);
        request.photoId = currentPhoto.getId();
        registerRequest(request);
        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                CacheProfile.photo = currentPhoto;
                CacheProfile.sendUpdateProfileBroadcast();
                refreshButtonsState();
                Toast.makeText(PhotoSwitcherActivity.this, R.string.avatar_set_successfully, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Toast.makeText(PhotoSwitcherActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT)
                        .show();
            }
        }).exec();
    }

    private ArrayList<Photo> getPhotos(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            ArrayList<Photo> arrList = extras.getParcelableArrayList(INTENT_PHOTOS);
            //Удаляем пустые пукнты фотографий
            if (arrList != null) {
                for (int i = 0; i < arrList.size(); i++) {
                    if (arrList.get(i) == null || arrList.get(i).isFake()) {
                        arrList.remove(i);
                    }
                }
                return arrList;
            } else {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    private void setCounter(int position) {
        int photosLinksSize = mPhotoLinks.size();
        if (mPhotoLinks != null) {
            mCurrentPosition = position < photosLinksSize ? position : photosLinksSize - 1;
            mCounter.setText((mCurrentPosition + 1) + "/" + photosLinksSize);
        }
    }

    private void refreshButtonsState() {
        if (mUid == CacheProfile.uid && mSetAvatarButton != null && mPhotoLinks != null && mPhotoLinks.size() > mCurrentPosition) {
            final Photo currentPhoto = mPhotoLinks.get(mCurrentPosition);
            if (mDeletedPhotos.contains(currentPhoto)) {
                mDeleteButton.setVisibility(View.VISIBLE);
                mDeleteButton.setImageResource(R.drawable.ico_restore_photo_selector);
                mSetAvatarButton.setText(R.string.edit_restore);
                mSetAvatarButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                if (CacheProfile.photo != null && CacheProfile.photo.getId() == currentPhoto.getId()) {
                    mDeleteButton.setVisibility(View.GONE);
                } else {
                    mDeleteButton.setVisibility(View.VISIBLE);
                    mDeleteButton.setImageResource(R.drawable.ico_delete_selector);
                }
                if (CacheProfile.photo != null && currentPhoto.getId() == CacheProfile.photo.getId()) {
                    mSetAvatarButton.setText(R.string.your_avatar);
                    mSetAvatarButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_selected_selector, 0, 0, 0);
                } else {
                    mSetAvatarButton.setText(R.string.on_avatar);
                    mSetAvatarButton.setCompoundDrawablesWithIntrinsicBounds(CacheProfile.sex == Static.BOY ? R.drawable.ico_avatar_man_selector : R.drawable.ico_avatar_woman_selector, 0, 0, 0);
                }
            }
        }
    }

    private void sendAlbumRequest(final Photos data) {
        int position = data.get(mLoadedCount - 2).getPosition() + 1;
        AlbumRequest request = new AlbumRequest(this, mUid, AlbumRequest.DEFAULT_PHOTOS_LIMIT, position, AlbumRequest.MODE_SEARCH);
        request.callback(new DataApiHandler<AlbumPhotos>() {

            @Override
            protected void success(AlbumPhotos newPhotos, IApiResponse response) {
                mNeedMore = newPhotos.more;
                int i = -1;
                for (Photo photo : newPhotos) {
                    mPhotoLinks.set(mLoadedCount + i, photo);
                    i++;
                }
                mLoadedCount += newPhotos.size();
                mCanSendAlbumReq = true;

                if (mImageSwitcher != null) {
                    mImageSwitcher.getAdapter().notifyDataSetChanged();
                    LocalBroadcastManager.getInstance(PhotoSwitcherActivity.this).sendBroadcast(new Intent(DEFAULT_UPDATE_PHOTOS_INTENT)
                            .putExtra(INTENT_PHOTOS, newPhotos)
                            .putExtra(INTENT_MORE, mNeedMore));
                }
            }

            @Override
            protected AlbumPhotos parseResponse(ApiResponse response) {
                return new AlbumPhotos(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                mCanSendAlbumReq = true;
            }
        }).exec();
    }
}
