package com.topface.topface.ui.fragments.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
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
import com.topface.topface.ui.views.ImageSwitcherLooped;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

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
                if (mPhotoAlbumControlVisibility == View.VISIBLE) {
                    getSupportActionBar().show();
                } else {
                    getSupportActionBar().hide();
                }
            }
        }
    };
    private ViewGroup mPhotoAlbumControl;
    private ViewGroup mOwnPhotosControl;
    private int mPhotoAlbumControlVisibility = View.VISIBLE;
    private int mOwnPhotosControlVisibility = View.GONE;
    private Photos mPhotoLinks;
    private Photos mDeletedPhotos = new Photos();
    private ImageSwitcherLooped mImageSwitcher;
    private int mUid;
    private PhotosManager mPhotosManager = new PhotosManager();
    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            int realPosition = calcRealPosition(position, mPhotoLinks.size());
            setCounter(realPosition);
            refreshButtonsState();

            mPhotosManager.check(((ImageSwitcher.ImageSwitcherAdapter) mImageSwitcher.getAdapter()).getData(),
                    realPosition);
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

    public static Intent getPhotoSwitcherIntent(int position, int userId, int photosCount, ProfileGridAdapter adapter) {
        Intent intent = new Intent(App.getContext(), PhotoSwitcherActivity.class);
        intent.putExtra(INTENT_USER_ID, userId);
        //Если первый элемент - это фейковая фотка, то смещаем позицию показа
        position = adapter.getItem(0).isFake() ? position - 1 : position;
        intent.putExtra(INTENT_ALBUM_POS, position);
        intent.putExtra(INTENT_PHOTOS_COUNT, photosCount);
        intent.putParcelableArrayListExtra(INTENT_PHOTOS, adapter.getPhotos());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_photos);
        // Extras
        Intent intent = getIntent();
        int photosCount = intent.getIntExtra(INTENT_PHOTOS_COUNT, 0);
        int position = intent.getIntExtra(INTENT_ALBUM_POS, 0);
        mUid = intent.getIntExtra(INTENT_USER_ID, -1);
        ArrayList<Photo> arrList = getPhotos(intent);

        mPhotoLinks = new Photos();
        mPhotoLinks.addAll(arrList);

        if (mUid == -1) {
            Debug.log(this, "Intent param is wrong");
            finish();
            return;
        }

        // Control layout
        mPhotoAlbumControl = (ViewGroup) findViewById(R.id.loPhotoAlbumControl);
        mOwnPhotosControl = (ViewGroup) mPhotoAlbumControl.findViewById(R.id.loBottomPanel);

        int rest = photosCount - mPhotoLinks.size();
        for (int i = 0; i < rest; i++) {
            mPhotoLinks.add(new Photo());
        }

        // Gallery
        mImageSwitcher = ((ImageSwitcherLooped) findViewById(R.id.galleryAlbum));
        mImageSwitcher.setOnPageChangeListener(mOnPageChangeListener);
        mImageSwitcher.setOnClickListener(mOnClickListener);
        mImageSwitcher.setData(mPhotoLinks);
        mImageSwitcher.setCurrentItem((ImageSwitcherLooped.ITEMS_HALF / mPhotoLinks.size() * mPhotoLinks.size()) + position, false);

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
        if (mPhotoAlbumControl != null) {
            outState.putInt(CONTROL_VISIBILITY, mPhotoAlbumControl.getVisibility());
            outState.putInt(OWN_PHOTOS_CONTROL_VISIBILITY, mOwnPhotosControl.getVisibility());
        }
        try {
            if (mDeletedPhotos != null) {
                outState.putString(DELETED_PHOTOS, mDeletedPhotos.toJson().toString());
            }
        } catch (JSONException e) {
            Debug.error(e);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
            case android.R.id.home:
                deletePhotoRequest();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private int calcRealPosition(int position, int realItemsAmount) {
        return position % realItemsAmount;
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
                    if (currentPhoto != null && mDeletedPhotos != null) {
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
            mOwnPhotosControlVisibility = View.VISIBLE;
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
                CacheProfile.totalPhotos -= mDeletedPhotos.size();
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
        if (mPhotoLinks != null) {
            int photosLinksSize = mPhotoLinks.size();
            mCurrentPosition = position < photosLinksSize ? position : photosLinksSize - 1;
            getTitleSetter().setActionBarTitles((mCurrentPosition + 1) + "/" + photosLinksSize, null);
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

    private void sendAlbumRequest(int position) {
        AlbumRequest request = new AlbumRequest(this, mUid, position, AlbumRequest.MODE_SEARCH, AlbumLoadController.FOR_PREVIEW);
        request.callback(new DataApiHandler<AlbumPhotos>() {

            @Override
            protected void success(AlbumPhotos newPhotos, IApiResponse response) {
                for (Photo photo : newPhotos) {
                    mPhotoLinks.set(photo.getPosition(), photo);
                }

                if (mImageSwitcher != null) {
                    mImageSwitcher.getAdapter().notifyDataSetChanged();
                    LocalBroadcastManager.getInstance(PhotoSwitcherActivity.this).sendBroadcast(new Intent(DEFAULT_UPDATE_PHOTOS_INTENT)
                            .putExtra(INTENT_PHOTOS, newPhotos)
                            .putExtra(INTENT_MORE, newPhotos.more));
                }
            }

            @Override
            protected AlbumPhotos parseResponse(ApiResponse response) {
                return new AlbumPhotos(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
            }
        }).exec();
    }

    private class PhotosManager {
        private int mLimit;

        public PhotosManager() {
            LoadController loadController = new AlbumLoadController(AlbumLoadController.FOR_PREVIEW);
            mLimit = loadController.getItemsLimitByConnectionType();
        }

        /**
         * sends request for photo data load, if need
         *
         * @param photos   array of photos
         * @param position _real_ index of current photo in this array
         */
        public void check(final Photos photos, final int position) {
            int indexToLeft = calcRightIndex(photos, position - DEFAULT_PRELOAD_ALBUM_RANGE);
            if (photos.get(indexToLeft).isFake()) {
                sendAlbumRequest(calcRightIndex(photos, position - mLimit));
            }
            int indexToRight = calcRightIndex(photos, position + DEFAULT_PRELOAD_ALBUM_RANGE);
            if (photos.get(indexToRight).isFake()) {
                sendAlbumRequest(calcRightIndex(photos, position));
            }
        }

        /**
         * converts some index (negative for example) to fit in array size
         *
         * @param photos source array
         * @param index  some index to fit
         * @return correct index, fitted in array size
         */
        private int calcRightIndex(final Photos photos, final int index) {
            if (index < 0) {
                int res = index;
                while (res < 0) res += photos.size();
                return res;
            } else if (index >= photos.size()) {
                int res = index;
                while (res >= photos.size()) res -= photos.size();
                return res;
            }
            return index;
        }
    }

    @Override
    protected void initActionBar(ActionBar actionBar) {
        super.initActionBar(actionBar);
        if (actionBar != null) {
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(android.R.color.transparent);
        }
    }
}
