package com.topface.topface.ui.fragments.profile.photoswitcher.view;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import com.topface.framework.JsonUtils;
import com.topface.framework.imageloader.DefaultImageLoader;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.AlbumPhotos;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.data.User;
import com.topface.topface.databinding.AcPhotosBinding;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoDeleteRequest;
import com.topface.topface.requests.PhotoMainRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.UserProfileActivity;
import com.topface.topface.ui.adapters.BasePhotoRecyclerViewAdapter;
import com.topface.topface.ui.fragments.profile.AbstractProfileFragment;
import com.topface.topface.ui.fragments.profile.photoswitcher.IUploadAlbumPhotos;
import com.topface.topface.ui.fragments.profile.photoswitcher.IUserProfileReceiver;
import com.topface.topface.ui.fragments.profile.photoswitcher.PhotosManager;
import com.topface.topface.ui.fragments.profile.photoswitcher.UserProfileLoader;
import com.topface.topface.ui.fragments.profile.photoswitcher.viewModel.PhotoSwitcherViewModel;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.ui.views.ImageSwitcherLooped;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.PreloadManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;

import org.json.JSONException;

import java.util.ArrayList;

import javax.inject.Inject;

public class PhotoSwitcherActivity extends BaseFragmentActivity {

    public static final String ADD_NEW_GIFT = "add_new_gift";
    public static final String DEFAULT_UPDATE_PHOTOS_INTENT = "com.topface.topface.updatePhotos";
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_ALBUM_POS = "album_position";
    public static final String INTENT_PHOTOS = "album_photos";
    public static final String INTENT_GIFT = "user_gift";
    public static final String INTENT_PHOTOS_COUNT = "photos_count";
    public static final String INTENT_PHOTOS_FILLED = "photos_filled";
    public static final String INTENT_PRELOAD_PHOTO = "preload_photo";
    public static final String INTENT_FILL_PROFILE_ON_BACK = "fill_profile_on_back";
    public static final String CONTROL_VISIBILITY = "CONTROL_VISIBILITY";
    public static final String OWN_PHOTOS_CONTROL_VISIBILITY = "OWN_PHOTOS_CONTROL_VISIBILITY";
    public static final String DELETED_PHOTOS = "DELETED_PHOTOS";
    public static final int DEFAULT_PRELOAD_ALBUM_RANGE = 3;

    private static final String PHOTO_COUNTER_TEMPLATE = "%d/%d";

    private static final String PAGE_NAME = "photoswitcher";
    private static final int ANIMATION_TIME = 200;
    @Inject
    TopfaceAppState appState;
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
    private ViewGroup mPhotoAlbumControl;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setPhotoAlbumControlVisibility(mPhotoAlbumControl != null
                    && mPhotoAlbumControl.getVisibility() != View.VISIBLE
                    ? View.VISIBLE
                    : View.GONE, true);
        }
    };
    private ViewGroup mOwnPhotosControl;
    private int mPhotoAlbumControlVisibility = View.VISIBLE;
    private int mOwnPhotosControlVisibility = View.GONE;
    private Photos mPhotoLinks;
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
    private Photos mDeletedPhotos = new Photos();
    private ImageSwitcherLooped mImageSwitcher;
    private int mUid;
    private PhotosManager mPhotosManager = new PhotosManager(new IUploadAlbumPhotos() {
        @Override
        public void sendRequest(int position) {
            sendAlbumRequest(position);
        }
    });
    private TranslateAnimation mCurrentAnimation;
    private TranslateAnimation mAnimationHide = null;
    private TranslateAnimation mAnimationShow = null;
    private Animation.AnimationListener mAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mCurrentAnimation == mAnimationShow) {
                mPhotoAlbumControl.setVisibility(View.VISIBLE);
            } else if (mCurrentAnimation == mAnimationHide) {
                mPhotoAlbumControl.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };
    private int mCurrentPosition = 0;
    private UserProfileLoader mUserProfileLoader;
    private AcPhotosBinding mBinding;
    private PhotoSwitcherViewModel mViewModel;

    public static Intent getPhotoSwitcherIntent(ArrayList<Gift> gifts, int position, int userId, int photosCount, BasePhotoRecyclerViewAdapter adapter) {
        return getPhotoSwitcherIntent(gifts, position, userId, photosCount, adapter.getPhotos());
    }

    public static Intent getPhotoSwitcherIntent(ArrayList<Gift> gifts, int position, int userId, int photosCount, Photos photos) {
        Intent intent = new Intent(App.getContext(), PhotoSwitcherActivity.class);
        intent.putExtra(INTENT_USER_ID, userId);
        // если позиция невалидная смещаем до последней в "колоде" хуяк-хуяк и в продакшн
        intent.putExtra(INTENT_ALBUM_POS, position >= photosCount ? photosCount - 1 : position);
        intent.putExtra(INTENT_PHOTOS_COUNT, photosCount);
        intent.putExtra(INTENT_PHOTOS_FILLED, true);
        intent.putParcelableArrayListExtra(INTENT_PHOTOS, photos);
        intent.putParcelableArrayListExtra(INTENT_GIFT, gifts);
        return intent;
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setHasContent(false);
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, getContentLayout());
        App.from(getApplicationContext()).inject(this);
        mViewModel = new PhotoSwitcherViewModel(mBinding, this);
        mBinding.setViewModel(mViewModel);
        overridePendingTransition(R.anim.fade_in, 0);
        // Extras
        Intent intent = getIntent();
        mUid = intent.getIntExtra(INTENT_USER_ID, -1);
        // Control layout
        mPhotoAlbumControl = mBinding.loPhotoAlbumControl;
        mOwnPhotosControl = mBinding.loBottomPanel;

        Photo preloadPhoto = intent.getParcelableExtra(INTENT_PRELOAD_PHOTO);
        if (preloadPhoto != null) {
            Point size = Utils.getSrceenSize(this);
            String s = preloadPhoto.getSuitableLink(size.x, size.y);
            if (PreloadManager.isPreloadAllowed()) {
                DefaultImageLoader.getInstance(this).preloadImage(s, null);
            }
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
                    mBinding.lockScreen,
                    mBinding.llvProfileLoading,
                    mUserProfileReceiver,
                    mUid
            );
        }
    }

    private void sendAlbumRequest(int position) {
        AlbumRequest request = new AlbumRequest(this, mUid, position, AlbumRequest.MODE_ALBUM, AlbumLoadController.FOR_PREVIEW);
        request.callback(new DataApiHandler<AlbumPhotos>() {

            @Override
            protected void success(AlbumPhotos newPhotos, IApiResponse response) {
                for (Photo photo : newPhotos) {
                    mPhotoLinks.set(photo.getPosition(), photo);
                }
                /*
                Записываем в кэш только в том случае если фоточки не имеют фейков, в противном случае
                в провили будут пустые итем на месте фоточек, и перестанет работать автодагрузка так как фактически
                из-за фейков нечего будет подгружать. По этому дабы не писать кучу оверхеда для такой исключительной ситуации
                "теряем" объекты загруженные в этой активити
                 */
                if (mUid == App.from(PhotoSwitcherActivity.this).getProfile().uid && !isContainsFakePhoto(mPhotoLinks)) {
                    Profile profile = App.from(PhotoSwitcherActivity.this).getProfile();
                    profile.photos = mPhotoLinks;
                    appState.setData(profile);
                }

                if (mImageSwitcher != null) {
                    mImageSwitcher.getAdapter().notifyDataSetChanged();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mViewModel != null) {
            mViewModel.release();
        }
        if (mUserProfileLoader != null) {
            mUserProfileLoader.release();
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.ac_photos;
    }

    private void setPhotoAlbumControlVisibility(int state, boolean isAnimated) {
        ActionBar actionbar = getSupportActionBar();
        if (state != View.VISIBLE) {
            mPhotoAlbumControlVisibility = View.GONE;
            mOwnPhotosControlVisibility = mOwnPhotosControl.getVisibility();
            if (actionbar != null) {
                actionbar.hide();
            }
            if (isAnimated) {
                animateHidePhotoAlbumControlAction();
            } else {
                hidePhotoAlbumControlAction();
            }
        } else {
            mPhotoAlbumControlVisibility = View.VISIBLE;
            mOwnPhotosControlVisibility = mOwnPhotosControl.getVisibility();
            if (actionbar != null) {
                actionbar.show();
            }
            if (isAnimated) {
                animateShowPhotoAlbumControlAction();
            } else {
                showPhotoAlbumControlAction();
            }
        }
    }

    private void initViews(int position, int photosCount) {
        if (mPhotoLinks.size() == 0) {
            finish();
            return;
        }
        int rest = photosCount - mPhotoLinks.size();
        for (int i = 0; i < rest; i++) {
            mPhotoLinks.add(Photo.createFakePhoto());
        }
        // Gallery
        // stub is needed, because sometimes(while gallery is waiting for user profile load)
        // ViewPager becomes visible without data
        // and its post init hangs app
        mBinding.galleryAlbumStub.getViewStub().inflate();
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
        // show control without animation
        setPhotoAlbumControlVisibility(mPhotoAlbumControlVisibility, false);
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
    public void onSaveInstanceState(Bundle outState) {
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
        mDeletedPhotos = JsonUtils.fromJson(savedInstanceState.getString(DELETED_PHOTOS), Photos.class);
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
        String itemId = intent.getStringExtra(AbstractProfileFragment.INTENT_ITEM_ID);
        startActivity(UserProfileActivity.createIntent(lastResponse != null ? lastResponse : null, null,
                mUid, itemId, false, true, null, null));
        finish();
    }

    @Override
    protected boolean onPreFinish() {
        deletePhotoRequest();
        Intent intent = getIntent();
        if (intent.getBooleanExtra(INTENT_FILL_PROFILE_ON_BACK, false)) {
            startUserProfileActivity();
        }
        return super.onPreFinish();
    }

    private int calcRealPosition(int position, int realItemsAmount) {
        return position % realItemsAmount;
    }

    private void initControls() {
        final Profile profile = App.from(this).getProfile();
        if (mUid == profile.uid) {
            // - set avatar button
            mViewModel.setOnAvatarButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Photo currentPhoto = mPhotoLinks.get(mCurrentPosition);
                    if (profile.photo == null || (currentPhoto != null
                            && currentPhoto.getId() != profile.photo.getId())) {
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
            mViewModel.setOnDeleteButtonClickListener(new View.OnClickListener() {
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
            if (mPhotoLinks.size() <= 1) {
                mViewModel.setTrashVisibility(false);
            }
            mOwnPhotosControlVisibility = View.VISIBLE;
        } else {
            mOwnPhotosControlVisibility = View.GONE;
            mOwnPhotosControl.setVisibility(mOwnPhotosControlVisibility);
        }
    }

    public void deletePhotoRequest() {
        final Profile profile = App.from(this).getProfile();
        if (mDeletedPhotos.isEmpty()) return;
        final Photos photos = (Photos) profile.photos.clone();
        final int totalPhotos = profile.photosCount;
        for (Photo currentPhoto : mDeletedPhotos) {
            profile.photos.removeById(currentPhoto.getId());
        }
        profile.photosCount -= mDeletedPhotos.size();
        int decrementPositionBy = 0;
        for (Photo deleted : mDeletedPhotos) {
            if (profile.photo != null && deleted.position < profile.photo.position
                    && profile.photo.position > 0) {
                decrementPositionBy--;
            }
        }
        final int avatarPosition = decrementPositionBy * (-1);
        CacheProfile.incrementPhotoPosition(this, decrementPositionBy, false);

        PhotoDeleteRequest request = new PhotoDeleteRequest(this);
        request.photos = mDeletedPhotos.getIdsArray();
        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                mDeletedPhotos.clear();
                //удалили фоточки обнивить профиль
                appState.setData(profile);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_SHORT);
                profile.photos = photos;
                profile.photosCount = totalPhotos;
                CacheProfile.incrementPhotoPosition(PhotoSwitcherActivity.this, avatarPosition, false);
                LocalBroadcastManager.getInstance(PhotoSwitcherActivity.this).sendBroadcast(new Intent(DEFAULT_UPDATE_PHOTOS_INTENT));
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
                Profile profile = App.from(PhotoSwitcherActivity.this).getProfile();
                profile.photo = currentPhoto;
                appState.setData(profile);
                CacheProfile.sendUpdateProfileBroadcast();
                refreshButtonsState();
                Utils.showToastNotification(R.string.avatar_set_successfully, Toast.LENGTH_SHORT);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                switch (codeError) {
                    // если пользователь пытается поставить на аватарку фото, которое было удалено модератором
                    case ErrorCodes.NON_EXIST_PHOTO_ERROR:
                        Utils.showToastNotification(R.string.general_non_exist_photo_error, Toast.LENGTH_SHORT);
                        CacheProfile.sendUpdateProfileBroadcast();
                        finish();
                        break;
                    case ErrorCodes.CODE_CANNOT_SET_PHOTO_AS_MAIN:
                        Utils.showCantSetPhotoAsMainToast(response);
                        break;
                    default:
                        Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_SHORT);
                        break;
                }
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
            actionBarView.setArrowUpView(String.format(App.getCurrentLocale(), PHOTO_COUNTER_TEMPLATE, mCurrentPosition + 1, photosLinksSize));
        }
    }

    private void refreshButtonsState() {
        Profile profile = App.from(this).getProfile();
        if (mUid == profile.uid && mPhotoLinks != null && mPhotoLinks.size() > mCurrentPosition) {
            final Photo currentPhoto = mPhotoLinks.get(mCurrentPosition);
            if (mDeletedPhotos.contains(currentPhoto)) {
                mViewModel.setTrashEnable(true);
                mViewModel.setTrashVisibility(true);
                mViewModel.setTrashSrc(R.drawable.ico_restore_photo_selector);
                mViewModel.setAvatarVisibility(false);
                mViewModel.setButtonText(R.string.album_photo_deleted);
                //TODO
//                mViewModel.setButtonText(R.string.edit_restore);
            } else {
                mViewModel.setAvatarVisibility(true);
                mViewModel.setTrashVisibility(true);
                mViewModel.setTrashSrc(R.drawable.album_delete_button_selector);
                boolean isMainPhoto = profile.photo != null && currentPhoto.getId() == profile.photo.getId();
                mViewModel.setAvatarEnable(!isMainPhoto);
                mViewModel.setTrashEnable(!isMainPhoto);
                mViewModel.setButtonText(isMainPhoto ? R.string.album_main_photo : 0);
                //TODO
//                mViewModel.setButtonText(isMainPhoto ? R.string.your_avatar : R.string.on_avatar);
            }
        }
    }

    private boolean isContainsFakePhoto(Photos photos) {
        for (Photo photo : photos) {
            if (photo.isFake()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GiftsActivity.INTENT_REQUEST_GIFT:
                if (resultCode == Activity.RESULT_OK) {
                    FeedGift feedGift = getFeedGiftFromIntent(data);
                    if (feedGift != null) {
                        if (mViewModel != null) {
                            mViewModel.showGift(feedGift.gift.link);
                        }
                        Intent intent = new Intent(ADD_NEW_GIFT);
                        intent.putExtra(INTENT_GIFT, feedGift);
                        LocalBroadcastManager.getInstance(this)
                                .sendBroadcast(intent);
                    }
                }
                break;
        }
    }

    private FeedGift getFeedGiftFromIntent(Intent data) {
        FeedGift feedGift = null;
        Bundle extras = data.getExtras();
        if (extras != null) {
            SendGiftAnswer sendGiftAnswer = extras.getParcelable(GiftsActivity.INTENT_SEND_GIFT_ANSWER);
            if (sendGiftAnswer != null) {
                feedGift = new FeedGift();
                feedGift.gift = new Gift(
                        Integer.parseInt(sendGiftAnswer.history.id),
                        0,
                        Gift.PROFILE, sendGiftAnswer.history.link);
            }
        }
        return feedGift;
    }

    private void hidePhotoAlbumControlAction() {
        if (mPhotoAlbumControl != null) {
            mPhotoAlbumControl.setVisibility(View.INVISIBLE);
        }
    }

    private void showPhotoAlbumControlAction() {
        if (mPhotoAlbumControl != null) {
            mPhotoAlbumControl.setVisibility(View.VISIBLE);
        }
    }

    private boolean animateHidePhotoAlbumControlAction() {
        if (mPhotoAlbumControl != null) {
            if (mAnimationHide == null && mPhotoAlbumControl.getMeasuredHeight() != 0) {
                mAnimationHide = new TranslateAnimation(
                        0,
                        0,
                        Utils.getSrceenSize(this).y - mPhotoAlbumControl.getMeasuredHeight(),
                        Utils.getSrceenSize(this).y);
            }
            if (mAnimationHide != null) {
                mCurrentAnimation = mAnimationHide;
                startAnimation(mCurrentAnimation);
            } else {
                hidePhotoAlbumControlAction();
            }
            return true;
        }
        return false;
    }

    private boolean animateShowPhotoAlbumControlAction() {
        if (mPhotoAlbumControl != null) {
            if (mAnimationShow == null && mPhotoAlbumControl.getMeasuredHeight() != 0) {
                mAnimationShow = new TranslateAnimation(
                        0,
                        0,
                        Utils.getSrceenSize(this).y,
                        Utils.getSrceenSize(this).y - mPhotoAlbumControl.getMeasuredHeight());
            }
            if (mAnimationShow != null) {
                mCurrentAnimation = mAnimationShow;
                startAnimation(mCurrentAnimation);
            } else {
                showPhotoAlbumControlAction();
            }
            return true;
        }
        return false;
    }

    private void startAnimation(TranslateAnimation animation) {
        animation.setDuration(ANIMATION_TIME);
        animation.setAnimationListener(mAnimationListener);
        mPhotoAlbumControl.startAnimation(animation);
    }
}
