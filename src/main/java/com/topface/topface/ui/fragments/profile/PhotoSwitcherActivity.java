package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.framework.imageloader.DefaultImageLoader;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.AlbumPhotos;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.User;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoDeleteRequest;
import com.topface.topface.requests.PhotoMainRequest;
import com.topface.topface.requests.UserRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.UserProfileActivity;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.ui.views.ImageSwitcherLooped;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
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
    public static final String INTENT_PHOTOS_FILLED = "photos_filled";
    public static final String INTENT_PRELOAD_PHOTO = "preload_photo";
    public static final String INTENT_FILL_PROFILE_ON_BACK = "fill_profile_on_back";
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
    private UserProfileLoader mUserProfileLoader;
    private IUserProfileReceiver mUserProfileReceiver = new IUserProfileReceiver() {
        @Override
        public void onReceiveUserProfile(User user) {
            int photosCount = user.photosCount;
            if (photosCount > 0) {
                ArrayList<Photo> arrList = getPhotos(user.photos);
                mPhotoLinks = new Photos();
                mPhotoLinks.addAll(arrList);

                // calc avatar position in photos list
                // user.photo.getPosition is not helping here
                int position = 0;
                if (user.photo != null) {
                    int needId = user.photo.getId();
                    for (int i = 0; i < arrList.size(); i++) {
                        if (arrList.get(i).getId() == needId) {
                            position = i;
                            break;
                        }
                    }
                }
                initViews(position, photosCount);
            } else {
                startUserProfileActivity();
            }
        }
    };

    public static Intent getPhotoSwitcherIntent(int position, int userId, int photosCount, ProfileGridAdapter adapter) {
        return getPhotoSwitcherIntent(position, userId, photosCount, adapter.getData());
    }

    public static  Intent getPhotoSwitcherIntent(int position){
        return getPhotoSwitcherIntent(position,CacheProfile.uid,CacheProfile.photos.size(),CacheProfile.photos);
    }

    public static Intent getPhotoSwitcherIntent(int position, int userId, int photosCount, Photos photos) {
        Intent intent = new Intent(App.getContext(), PhotoSwitcherActivity.class);
        intent.putExtra(INTENT_USER_ID, userId);
        //Если первый элемент - это фейковая фотка, то смещаем позицию показа
        position = photos.get(0).isFake() ? position - 1 : position;
        intent.putExtra(INTENT_ALBUM_POS, position);
        intent.putExtra(INTENT_PHOTOS_COUNT, photosCount);
        intent.putExtra(INTENT_PHOTOS_FILLED, true);
        intent.putParcelableArrayListExtra(INTENT_PHOTOS, photos);
        return intent;
    }

    public static Intent getPhotoSwitcherIntent(int userId, Class callingClass, Photo preloadPhoto, Context context) {
        Intent intent = new Intent(context, PhotoSwitcherActivity.class);
        intent.putExtra(INTENT_USER_ID, userId);
        intent.putExtra(INTENT_FILL_PROFILE_ON_BACK, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        if (preloadPhoto != null) {
            intent.putExtra(INTENT_PRELOAD_PHOTO, preloadPhoto);
        }
        if (callingClass != null) {
            intent.putExtra(AbstractProfileFragment.INTENT_CALLING_FRAGMENT, callingClass.getName());
        }
        return intent;
    }

    public static Intent getPhotoSwitcherIntent(String itemId, int userId, Photo preloadPhoto, Context context) {
        Intent intent = getPhotoSwitcherIntent(userId, preloadPhoto, context);
        if (itemId != null) {
            // for forwarding feed id to profile fragment
            intent.putExtra(AbstractProfileFragment.INTENT_ITEM_ID, itemId);
        }
        return intent;
    }

    public static Intent getPhotoSwitcherIntent(int userId, Photo preloadPhoto, Context context) {
        return getPhotoSwitcherIntent(userId, null, preloadPhoto, context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_photos);
        // Extras
        Intent intent = getIntent();
        mUid = intent.getIntExtra(INTENT_USER_ID, -1);
        if (mUid == -1) {
            Debug.log(this, "Intent param is wrong");
            finish();
            return;
        }
        // Control layout
        mPhotoAlbumControl = (ViewGroup) findViewById(R.id.loPhotoAlbumControl);
        mOwnPhotosControl = (ViewGroup) mPhotoAlbumControl.findViewById(R.id.loBottomPanel);

        Photo preloadPhoto = intent.getParcelableExtra(INTENT_PRELOAD_PHOTO);
        if (preloadPhoto != null) {
            Point size = Utils.getSrceenSize(this);
            String s = preloadPhoto.getSuitableLink(size.x, size.y);
            DefaultImageLoader.getInstance(this).preloadImage(s, null);
        }

        if (intent.getBooleanExtra(INTENT_PHOTOS_FILLED, false)) {
            int photosCount = intent.getIntExtra(INTENT_PHOTOS_COUNT, 0);
            int position = intent.getIntExtra(INTENT_ALBUM_POS, 0);
            ArrayList<Photo> arrList = getPhotos(intent);

            mPhotoLinks = new Photos();
            mPhotoLinks.addAll(arrList);

            initViews(position, photosCount);
        } else {
            mUserProfileLoader = new UserProfileLoader(
                    (RelativeLayout) findViewById(R.id.lockScreen),
                    findViewById(R.id.llvProfileLoading),
                    mUserProfileReceiver,
                    mUid
            );
        }
    }

    private void initViews(int position, int photosCount) {

        int rest = photosCount - mPhotoLinks.size();
        for (int i = 0; i < rest; i++) {
            mPhotoLinks.add(new Photo());
        }

        // Gallery
        // stub is needed, because sometimes(while gallery is waiting for user profile load)
        // ViewPager becomes visible without data
        // and its post init hangs app
        ViewStub stub = (ViewStub) findViewById(R.id.gallery_album_stub);
        stub.inflate();
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
        if (!getIntent().getBooleanExtra(INTENT_PHOTOS_FILLED, false)) {
            mUserProfileLoader.loadUserProfile(this);
        }
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
        Intent intent = getIntent();
        if (intent.getBooleanExtra(INTENT_FILL_PROFILE_ON_BACK, false)) {
            startUserProfileActivity();
        } else {
            super.onBackPressed();
        }
    }

    protected void startUserProfileActivity() {
        ApiResponse lastResponse = mUserProfileLoader.getLastResponse();
        // if profile was not loaded at this moment - we will open UserProfileActivity
        // without chached info
        Intent intent = getIntent();
        String callingClassName = intent.getStringExtra(AbstractProfileFragment.INTENT_CALLING_FRAGMENT);
        String itemId = intent.getStringExtra(AbstractProfileFragment.INTENT_ITEM_ID);

        if (lastResponse != null) {
            startActivity(UserProfileActivity.createIntent(
                            lastResponse,
                            mUid,
                            itemId,
                            callingClassName,
                            UserFormFragment.class.getName(),
                            this)
            );
        } else {
            startActivity(UserProfileActivity.createIntent(
                            mUid,
                            itemId,
                            callingClassName,
                            UserFormFragment.class.getName(),
                            this)
            );
        }
        finish();
    }

    @Override
    protected void onPreFinish() {
        super.onPreFinish();
        Intent intent = getIntent();
        if (intent.getBooleanExtra(INTENT_FILL_PROFILE_ON_BACK, false)) {
            startUserProfileActivity();
        }
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
                removeEmptyPhotos(arrList);
                return arrList;
            } else {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    private ArrayList<Photo> getPhotos(Photos photos) {
        if (photos != null) {
            ArrayList<Photo> arrList = new ArrayList<>();
            arrList.addAll(photos);
            //Удаляем пустые пукнты фотографий
            removeEmptyPhotos(arrList);
            return arrList;
        } else {
            return new ArrayList<>();
        }
    }

    private void removeEmptyPhotos(ArrayList<Photo> arrList) {
        for (int i = 0; i < arrList.size(); i++) {
            Photo photo = arrList.get(i);
            if (photo == null || photo.isFake() || photo.isEmpty()) {
                arrList.remove(i);
            }
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

    public static interface IUserProfileReceiver {
        public void onReceiveUserProfile(User user);
    }

    private class UserProfileLoader {
        private int mLastLoadedProfileId;
        private ApiResponse mLastResponse;
        private RelativeLayout mLockScreen;
        private RetryViewCreator mRetryView;
        private View mLoaderView;
        private IUserProfileReceiver mReceiver = null;
        private int mProfileId;

        public UserProfileLoader(RelativeLayout lockScreen, View loaderView, IUserProfileReceiver receiver, final int profileId) {
            mLockScreen = lockScreen;
            mLoaderView = loaderView;
            mReceiver = receiver;
            mProfileId = profileId;
            mRetryView = new RetryViewCreator.Builder(PhotoSwitcherActivity.this, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadUserProfile(PhotoSwitcherActivity.this);
                }
            }).build();
            mLockScreen.addView(mRetryView.getView());
        }

        private boolean isLoaded(int profileId) {
            return profileId == mLastLoadedProfileId;
        }

        public void loadUserProfile(Context context) {
            if (isLoaded(mProfileId)) return;
            mLockScreen.setVisibility(View.GONE);
            mLoaderView.setVisibility(View.VISIBLE);
            UserRequest userRequest = new UserRequest(mProfileId, context);
            registerRequest(userRequest);
            userRequest.callback(new DataApiHandler<User>() {

                @Override
                protected void success(User user, IApiResponse response) {
                    mLastLoadedProfileId = mProfileId;
                    if (user != null) {
                        mLastResponse = (ApiResponse) response;
                    }
                    if (user == null) {
                        showRetryBtn();
                    } else if (user.banned) {
                        showForBanned();
                    } else if (user.deleted) {
                        showForDeleted();
                    } else {
                        mLoaderView.setVisibility(View.INVISIBLE);
                        setProfile(user);
                    }
                }

                @Override
                protected User parseResponse(ApiResponse response) {
                    return User.parse(mProfileId, response);
                }

                @Override
                public void fail(final int codeError, IApiResponse response) {
                    if (response.isCodeEqual(ErrorCodes.INCORRECT_VALUE, ErrorCodes.USER_NOT_FOUND)) {
                        showForNotExisting();
                    } else {
                        showRetryBtn();
                    }
                }
            }).exec();

        }

        public ApiResponse getLastResponse() {
            return mLastResponse;
        }

        private void setProfile(User user) {
            if (mReceiver != null) {
                mReceiver.onReceiveUserProfile(user);
            }
        }

        private void showLockWithText(String text, boolean onlyMessage) {
            if (mRetryView != null) {
                mLoaderView.setVisibility(View.GONE);
                mLockScreen.setVisibility(View.VISIBLE);
                mRetryView.setText(text);
                mRetryView.showRetryButton(!onlyMessage);
            }
        }

        private void showLockWithText(String text) {
            showLockWithText(text, true);
        }

        private void showForBanned() {
            showLockWithText(getString(R.string.user_baned));
        }

        private void showRetryBtn() {
            showLockWithText(getString(R.string.general_profile_error), false);
        }

        private void showForDeleted() {
            showLockWithText(getString(R.string.user_is_deleted));
        }

        private void showForNotExisting() {
            showLockWithText(getString(R.string.user_does_not_exist), true);
        }
    }
}
