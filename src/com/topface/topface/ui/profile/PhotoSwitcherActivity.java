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
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.PreloadManager;

import java.util.ArrayList;

public class PhotoSwitcherActivity extends Activity {

    public static final String INTENT_MORE = "more";
    private TextView mCounter;
    private ViewGroup mHeaderBar;
    private Photos mPhotoLinks;
    private PreloadManager mPreloadManager;

    public static final String DEFAULT_UPDATE_PHOTOS_INTENT = "com.topface.topface.updatePhotos";

    public static final String INTENT_OWNER = "owner";
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_ALBUM_POS = "album_position";
    public static final String INTENT_PHOTOS = "album_photos";
    public static final String INTENT_PHOTOS_COUNT = "photos_count";

    private ImageSwitcher mImageSwitcher;
    private boolean mNeedMore;
    private boolean mCanSendAlbumReq = true;
    private int mUid;
    private int photosCount;
    private int mLoadedCount;
    public static final int DEFAULT_PRELOAD_ALBUM_RANGE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_photos);
        mPreloadManager = new PreloadManager();
        // Extras
        Intent intent = getIntent();
        photosCount = intent.getIntExtra(INTENT_PHOTOS_COUNT, 0);
        int position = intent.getIntExtra(INTENT_ALBUM_POS, 0);
        mUid = intent.getIntExtra(INTENT_USER_ID, -1);
        ArrayList<Photo> arrList = intent.getExtras().getParcelableArrayList(INTENT_PHOTOS);
        mPhotoLinks = new Photos();
        mPhotoLinks.addAll(arrList);

        mNeedMore = photosCount > mPhotoLinks.size();

        if (mUid == -1) {
            Debug.log(this, "Intent param is wrong");
            finish();
            return;
        }

        //Header
        mHeaderBar = (ViewGroup) findViewById(R.id.loHeaderBar);
        mHeaderBar.setVisibility(View.INVISIBLE);

        // Title Header
        mCounter = ((TextView) findViewById(R.id.tvHeaderTitle));

        // Gallery
        mImageSwitcher = ((ImageSwitcher) findViewById(R.id.galleryAlbum));
        mImageSwitcher.setOnPageChangeListener(mOnPageChangeListener);
        mImageSwitcher.setOnClickListener(mOnClickListener);

//        mPhotoLinks = isOwner ? CacheProfile.photos : Data.photos;

        mLoadedCount = mPhotoLinks.getRealPhotosCount();
        mNeedMore = photosCount > mLoadedCount;
        int rest = photosCount - mPhotoLinks.size();

        for (int i = 0; i < rest; i++) {
            mPhotoLinks.add(new Photo());
        }

        mImageSwitcher.setData(mPhotoLinks);
        mImageSwitcher.setCurrentItem(position, false);

        ImageButton backButton = ((ImageButton) findViewById(R.id.btnNavigationBack));
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        setCounter(position);
    }

    private void setCounter(int position) {
        if (mPhotoLinks != null) {
            mCounter.setText((position + 1) + "/" + mPhotoLinks.size());
        }
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mHeaderBar.setVisibility(mHeaderBar.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
        }
    };


    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            mPreloadManager.preloadPhoto(mPhotoLinks, position + 1);
            setCounter(position);

            if (position + DEFAULT_PRELOAD_ALBUM_RANGE == mLoadedCount) {
                final Photos data = ((ImageSwitcher.ImageSwitcherAdapter)mImageSwitcher.getAdapter()).getData();

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

    private void sendAlbumRequest(final Photos data) {

        int id = data.get(mLoadedCount - 2).getId();
        AlbumRequest request = new AlbumRequest(this, mUid, AlbumRequest.DEFAULT_PHOTOS_LIMIT, id, AlbumRequest.MODE_SEARCH);
        request.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Photos newPhotos = Photos.parse(response.jsonResult.optJSONArray("items"));
                mNeedMore = response.jsonResult.optBoolean("more");
                int i = -1;
                for(Photo photo : newPhotos) {
                    data.set(mLoadedCount + i, photo);
                    i++;
                }
                mLoadedCount += newPhotos.size();
                mCanSendAlbumReq = true;

                if(mImageSwitcher != null) {
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
