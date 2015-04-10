package com.topface.topface.ui.fragments.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
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
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SendGiftAnswer;
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
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.UserProfileActivity;
import com.topface.topface.ui.fragments.OwnAvatarFragment;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.ui.views.ImageSwitcherLooped;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.PreloadManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class PhotoSwitcherActivity extends BaseFragmentActivity {

    public static final String ADD_NEW_GIFT = "add_new_gift";

    public static final String INTENT_MORE = "more";
    public static final String INTENT_CLEAR = "clear";
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
    private static final int ANIMATION_TIME = 200;
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
            if (mPhotoAlbumControl != null) {
                setPhotoAlbumControlVisibility(mPhotoAlbumControl.getVisibility() == View.GONE ||
                        mPhotoAlbumControl.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.GONE, true);
            }
        }
    };
    private ViewGroup mOwnPhotosControl;
    private ImageViewRemote mGiftImage;
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
    private String mUserGiftLink;
    private PhotosManager mPhotosManager = new PhotosManager();
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
    private TextView mSetAvatarButton;
    private ImageButton mDeleteButton;
    private UserProfileLoader mUserProfileLoader;

    public static Intent getPhotoSwitcherIntent(Profile.Gifts gifts, int position, int userId, int photosCount, ProfileGridAdapter adapter) {
        return getPhotoSwitcherIntent(gifts, position, userId, photosCount, adapter.getPhotos());
    }

    public static Intent getPhotoSwitcherIntent(Profile.Gifts gifts, int position, int userId, int photosCount, Photos photos) {
        Intent intent = new Intent(App.getContext(), PhotoSwitcherActivity.class);
        intent.putExtra(INTENT_USER_ID, userId);
        //Если первый элемент - это фейковая фотка, то смещаем позицию показа
        intent.putExtra(INTENT_ALBUM_POS, position);
        intent.putExtra(INTENT_PHOTOS_COUNT, photosCount);
        intent.putExtra(INTENT_PHOTOS_FILLED, true);
        intent.putParcelableArrayListExtra(INTENT_PHOTOS, photos);
        intent.putParcelableArrayListExtra(INTENT_GIFT, gifts);
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
            if (PreloadManager.isPreloadAllowed()) {
                DefaultImageLoader.getInstance(this).preloadImage(s, null);
            }
        }
        extractUserGifts(intent);
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

    private void extractUserGifts(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            ArrayList<Gift> array = extras.getParcelableArrayList(INTENT_GIFT);
            if (array != null && array.size() > 0) {
                mUserGiftLink = array.get(0).link;
            }
        }
    }

    private void setPhotoAlbumControlVisibility(int state, boolean isAnimated) {
        if (state == View.GONE || state == View.INVISIBLE) {
            mPhotoAlbumControlVisibility = View.GONE;
            mOwnPhotosControlVisibility = mOwnPhotosControl.getVisibility();
            getSupportActionBar().hide();
            if (isAnimated) {
                animateHidePhotoAlbumControlAction();
            } else {
                hidePhotoAlbumControlAction();
            }
        } else {
            mPhotoAlbumControlVisibility = View.VISIBLE;
            mOwnPhotosControlVisibility = mOwnPhotosControl.getVisibility();
            getSupportActionBar().show();
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
        initGiftImage();
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
                            this)
            );
        } else {
            startActivity(UserProfileActivity.createIntent(
                            mUid,
                            itemId,
                            callingClassName,
                            this)
            );
        }
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

    private void initGiftImage() {
        mGiftImage = (ImageViewRemote) mPhotoAlbumControl.findViewById(R.id.loGiftImage);
        mGiftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        GiftsActivity.getSendGiftIntent(PhotoSwitcherActivity.this, mUid),
                        GiftsActivity.INTENT_REQUEST_GIFT
                );
            }
        });
        if (mUid == CacheProfile.uid) {
            mGiftImage.setVisibility(View.GONE);
        } else {
            mGiftImage.setVisibility(View.VISIBLE);
            showGiftImage();
        }
    }

    private void showGiftImage(String link) {
        mUserGiftLink = link;
        showGiftImage();
    }

    private void showGiftImage() {
        if (mGiftImage != null) {
            if (mUserGiftLink != null) {
                // show last added gift
                // at first drop background
                // after set image
                mGiftImage.setBackgroundResource(0);
                mGiftImage.setRemoteSrc(mUserGiftLink);
            } else {
                // show default image when user haven't any gifts yet
                // at first drop image
                // after set background
                mGiftImage.setImageDrawable(null);
                mGiftImage.setBackgroundResource(R.drawable.ic_gift);
            }
        }
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
            mOwnPhotosControlVisibility = View.GONE;
            mPhotoAlbumControl.findViewById(R.id.loBottomPanel).setVisibility(mOwnPhotosControlVisibility);
        }
    }

    public void deletePhotoRequest() {
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

                int decrementAvaPos = 0;
                for (Photo deleted : mDeletedPhotos) {
                    if (deleted.position < CacheProfile.photo.position) {
                        decrementAvaPos++;
                    }
                }

                LocalBroadcastManager.getInstance(PhotoSwitcherActivity.this).sendBroadcast(new Intent(DEFAULT_UPDATE_PHOTOS_INTENT)
                        .putExtra(INTENT_PHOTOS, CacheProfile.photos)
                        .putExtra(INTENT_MORE, CacheProfile.photos.size() < CacheProfile.totalPhotos - mDeletedPhotos.size())
                        .putExtra(INTENT_CLEAR, true)
                        .putExtra(OwnAvatarFragment.DECREMENT_AVATAR_POSITION, decrementAvaPos));
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
                switch (codeError) {
                    // если пользователь пытается поставить на аватарку фото, которое было удалено модератором
                    case ErrorCodes.NON_EXIST_PHOTO_ERROR:
                        Toast.makeText(PhotoSwitcherActivity.this, R.string.general_non_exist_photo_error, Toast.LENGTH_SHORT)
                                .show();
                        CacheProfile.sendUpdateProfileBroadcast();
                        finish();
                        break;
                    default:
                        Toast.makeText(PhotoSwitcherActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT)
                                .show();
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
            actionBarView.setArrowUpView((mCurrentPosition + 1) + "/" + photosLinksSize);
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
                    mSetAvatarButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_selected, 0, 0, 0);
                } else {
                    mSetAvatarButton.setText(R.string.on_avatar);
                    mSetAvatarButton.setCompoundDrawablesWithIntrinsicBounds(CacheProfile.sex == Static.BOY ? R.drawable.ico_avatar_man_selector : R.drawable.ico_avatar_woman_selector, 0, 0, 0);
                }
            }
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

                if (mImageSwitcher != null) {
                    mImageSwitcher.getAdapter().notifyDataSetChanged();
                    LocalBroadcastManager.getInstance(PhotoSwitcherActivity.this).sendBroadcast(new Intent(DEFAULT_UPDATE_PHOTOS_INTENT)
                            .putExtra(INTENT_PHOTOS, newPhotos)
                            .putExtra(INTENT_MORE, newPhotos.more)
                            .putExtra(INTENT_CLEAR, true));
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GiftsActivity.INTENT_REQUEST_GIFT:
                if (resultCode == Activity.RESULT_OK) {
                    FeedGift feedGift = getFeedGiftFromIntent(data);
                    if (feedGift != null) {
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
                showGiftImage(sendGiftAnswer.history.link);
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


    public interface IUserProfileReceiver {
        void onReceiveUserProfile(User user);
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
                while (res < 0) {
                    res += photos.size();
                }
                return res;
            } else if (index >= photos.size()) {
                int res = index;
                while (res >= photos.size()) {
                    res -= 1;
                }
                return res;
            }
            return index;
        }
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
                    return new User(mProfileId, response);
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
