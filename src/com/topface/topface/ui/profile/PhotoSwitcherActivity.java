package com.topface.topface.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.Photos;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.PreloadManager;

public class PhotoSwitcherActivity extends Activity {

    private TextView mCounter;
    private ViewGroup mHeaderBar;
    private Photos mPhotoLinks;
    private PreloadManager mPreloadManager;

    public static final String INTENT_OWNER = "owner";
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_ALBUM_POS = "album_position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_photos);
        mPreloadManager = new PreloadManager(getApplicationContext());
        // Extras
        Intent intent = getIntent();
        boolean isOwner = intent.getBooleanExtra(INTENT_OWNER, false);
        int position = intent.getIntExtra(INTENT_ALBUM_POS, 0);
        int uid = intent.getIntExtra(INTENT_USER_ID, -1);

        if (uid == -1) {
            Debug.log(this, "Intent param is wrong");
            finish();
            return;
        }

        //Header
        mHeaderBar = (ViewGroup) findViewById(R.id.loHeaderBar);

        // Title Header
        mCounter = ((TextView) findViewById(R.id.tvHeaderTitle));

        // Gallery
        ImageSwitcher imageSwitcher = ((ImageSwitcher) findViewById(R.id.galleryAlbum));
        imageSwitcher.setOnPageChangeListener(mOnPageChangeListener);
        imageSwitcher.setOnClickListener(mOnClickListener);

        mPhotoLinks = isOwner ? CacheProfile.photos : Data.photos;

        imageSwitcher.setData(mPhotoLinks);
        imageSwitcher.setCurrentItem(position, false);

        Button backButton = ((Button) findViewById(R.id.btnNavigationBack));
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
        mCounter.setText((position + 1) + "/" + mPhotoLinks.size());
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
            mPreloadManager.preloadPhoto(mPhotoLinks,position+1);
            setCounter(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };
}
