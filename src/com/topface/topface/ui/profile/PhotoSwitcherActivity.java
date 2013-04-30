package com.topface.topface.ui.profile;

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
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.PreloadManager;

import java.util.ArrayList;

public class PhotoSwitcherActivity extends BaseFragmentActivity {

    public static final String INTENT_MORE = "more";
    public static final String INTENT_CLEAR = "clear";
    private TextView mCounter;
    private ViewGroup mPhotoAlbumControl;
    private Photos mPhotoLinks;
    private PreloadManager mPreloadManager;
    private Photos mDeletedPhotos = new Photos();

    public static final String DEFAULT_UPDATE_PHOTOS_INTENT = "com.topface.topface.updatePhotos";

    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_ALBUM_POS = "album_position";
    public static final String INTENT_PHOTOS = "album_photos";
    public static final String INTENT_PHOTOS_COUNT = "photos_count";

    private ImageSwitcher mImageSwitcher;
    private boolean mNeedMore;
    private boolean mCanSendAlbumReq = true;
    private int mUid;
    private int mLoadedCount;
    public static final int DEFAULT_PRELOAD_ALBUM_RANGE = 3;
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

        mLoadedCount = mPhotoLinks.getRealPhotosCount();
        mNeedMore = photosCount > mLoadedCount;
        int rest = photosCount - mPhotoLinks.size();
        for (int i = 0; i < rest; i++) {
            mPhotoLinks.add(new Photo());
        }
        mImageSwitcher.setData(mPhotoLinks);
        mImageSwitcher.setCurrentItem(position, false);

        initControls();
        setCounter(position);
        refreshButtonsState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        deletePhotoRequest();
    }

    private void initControls() {
        // Control layout
        mPhotoAlbumControl = (ViewGroup) findViewById(R.id.loPhotoAlbumControl);
        // - close button
        mPhotoAlbumControl.findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
        if (mUid == CacheProfile.uid) {
            mPhotoAlbumControl.setVisibility(View.GONE);
            // - set avatar button
            mSetAvatarButton = (TextView) mPhotoAlbumControl.findViewById(R.id.btnSetAvatar);
            mSetAvatarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Photo currentPhoto = mPhotoLinks.get(mCurrentPosition);
                    if (currentPhoto.getId() != CacheProfile.photo.getId()) {
                        if (!mDeletedPhotos.contains(currentPhoto)) {
                            setAsMainRequest(currentPhoto);
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
                            mDeletedPhotos.remove(currentPhoto);
                        } else {
                            mDeletedPhotos.add(currentPhoto);
                        }
                        refreshButtonsState();
                    }
                }
            });
            if (mPhotoLinks.size() <= 1) mDeleteButton.setVisibility(View.GONE);
        } else {
            mPhotoAlbumControl.findViewById(R.id.loBottomPanel).setVisibility(View.GONE);
        }
    }

    private void deletePhotoRequest() {
        if (mDeletedPhotos.isEmpty()) return;

        PhotoDeleteRequest request = new PhotoDeleteRequest(this);
        request.photos = mDeletedPhotos.getIdsArray();
        request.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                for (Photo currentPhoto : mDeletedPhotos) {
                    CacheProfile.photos.removeById(currentPhoto.getId());
                }
                LocalBroadcastManager.getInstance(PhotoSwitcherActivity.this).sendBroadcast(new Intent(DEFAULT_UPDATE_PHOTOS_INTENT)
                        .putExtra(INTENT_PHOTOS, CacheProfile.photos)
                        .putExtra(INTENT_MORE, CacheProfile.photos.size() < CacheProfile.totalPhotos-mDeletedPhotos.size())
                        .putExtra(INTENT_CLEAR, true));
                mDeletedPhotos.clear();
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                Toast.makeText(PhotoSwitcherActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
            }
        }).exec();
    }

    private void setAsMainRequest(final Photo currentPhoto) {
        PhotoMainRequest request = new PhotoMainRequest(this);
        request.photoid = currentPhoto.getId();
        registerRequest(request);
        request.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                CacheProfile.photo = currentPhoto;
                sendProfileUpdateBroadcast();
                refreshButtonsState();
                Toast.makeText(PhotoSwitcherActivity.this, R.string.avatar_set_successfully, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                Toast.makeText(PhotoSwitcherActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT)
                        .show();
            }
        }).exec();
    }

    private static void sendProfileUpdateBroadcast() {
        Intent intent = new Intent();
        intent.setAction(ProfileRequest.PROFILE_UPDATE_ACTION);
        LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
    }

    private ArrayList<Photo> getPhotos(Intent intent) {
        ArrayList<Photo> arrList = intent.getExtras().getParcelableArrayList(INTENT_PHOTOS);
        //Удаляем пустые пукнты фотографий
        for (int i = 0; i < arrList.size(); i++) {
            if (arrList.get(i) == null) {
                arrList.remove(i);
            }
        }
        return arrList;
    }

    private void setCounter(int position) {
        if (mPhotoLinks != null) {
            mCurrentPosition = position;
            mCounter.setText((mCurrentPosition + 1) + "/" + mPhotoLinks.size());
        }
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPhotoAlbumControl.setVisibility(mPhotoAlbumControl.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        }
    };


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

    private void refreshButtonsState() {
        if (mUid == CacheProfile.uid && mSetAvatarButton != null) {
            final Photo currentPhoto = mPhotoLinks.get(mCurrentPosition);
            if (mDeletedPhotos.contains(currentPhoto)) {
                mDeleteButton.setVisibility(View.VISIBLE);
                mDeleteButton.setImageResource(R.drawable.ico_restore_photo_selector);
                mSetAvatarButton.setText(R.string.edit_restore);
                mSetAvatarButton.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            } else {
                if(CacheProfile.photo.getId() == currentPhoto.getId()) {
                    mDeleteButton.setVisibility(View.GONE);
                } else {
                    mDeleteButton.setVisibility(View.VISIBLE);
                    mDeleteButton.setImageResource(R.drawable.ico_delete_selector);
                }
                if (currentPhoto.getId() == CacheProfile.photo.getId()) {
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
        request.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Photos newPhotos = Photos.parse(response.jsonResult.optJSONArray("items"));
                mNeedMore = response.jsonResult.optBoolean("more");
                int i = -1;
                for (Photo photo : newPhotos) {
                    data.set(mLoadedCount + i, photo);
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
            public void fail(int codeError, ApiResponse response) {
                mCanSendAlbumReq = true;
            }
        }).exec();
    }
}
