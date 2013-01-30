package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.LeaderRequest;
import com.topface.topface.ui.adapters.LeadersPhotoAdapter;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import java.util.LinkedList;

public class LeadersActivity extends BaseFragmentActivity {
    private com.example.gridlayout.GridLayout mGridView;
    private com.example.gridlayout.GridLayout mUselessGridView;
    private LockerView mLoadingLocker;
    private PhotoSelector mSelectedPhoto = new PhotoSelector();
    private Button mBuyButton;

    private Photos usePhotos;
    private Photos uselessPhotos;

    private LinkedList<LeadersPhoto> mLeadersPhotos = new LinkedList<LeadersPhoto>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_leaders_layout);
        ((TextView) findViewById(R.id.tvNavigationTitle)).setText(R.string.leaders_go_date);
        findViewById(R.id.btnNavigationHome).setVisibility(View.INVISIBLE);
        View backButton = findViewById(R.id.btnNavigationBack);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        usePhotos = new Photos();
        uselessPhotos = new Photos();

//        mProgressBar = (ProgressBar) findViewById(R.id.loader);
        mGridView = (com.example.gridlayout.GridLayout) findViewById(R.id.usedGrid);
        mUselessGridView = (com.example.gridlayout.GridLayout) findViewById(R.id.unusedGrid);
        mBuyButton = (Button) findViewById(R.id.btnLeadersBuy);
        mLoadingLocker = (LockerView) findViewById(R.id.llvLeaderSending);

        setListeners();
        getProfile();
        setPrice();
    }

    private void setPrice() {
        int leadersPrice = CacheProfile.getOptions().price_leader;
        mBuyButton.setText(
                Utils.getQuantityString(
                        R.plurals.leaders_price,
                        leadersPrice,
                        leadersPrice
                )
        );
    }

    private void setListeners() {
        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CacheProfile.money < CacheProfile.getOptions().price_leader) {
                    Intent intent = new Intent(LeadersActivity.this, ContainerActivity.class);
                    intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUYING_FRAGMENT);
                    startActivity(intent);
                } else if (mSelectedPhoto.isSelected()) {
                    mLoadingLocker.setVisibility(View.VISIBLE);
                    new LeaderRequest(mSelectedPhoto.getPhotoId(), LeadersActivity.this)
                            .callback(new ApiHandler() {
                                @Override
                                public void success(ApiResponse response) {
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mLoadingLocker.setVisibility(View.GONE);
                                            Toast.makeText(LeadersActivity.this, R.string.leaders_leader_now, Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                                }

                                @Override
                                public void fail(int codeError, ApiResponse response) {
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mLoadingLocker.setVisibility(View.GONE);
                                            Toast.makeText(LeadersActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).exec();

                } else {
                    Toast.makeText(LeadersActivity.this, R.string.leaders_need_photo, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateProfileInfo(Profile profile) {
        splitPhotos(profile.photos);
        for (final Photo photo : usePhotos) {
            View view = getLayoutInflater().inflate(R.layout.leaders_photo_item, null);
            ImageViewRemote ivr = (ImageViewRemote) view.findViewById(R.id.ivLeadPhoto);
            final ImageView mask = (ImageView) view.findViewById(R.id.lpiMask);
            mLeadersPhotos.add(new LeadersPhoto(photo, mask));
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mSelectedPhoto != null) {
                        mSelectedPhoto.select(mask, photo);
                    }
                }
            });
            mGridView.addView(view);
            ivr.setPhoto(photo);
        }

        for (Photo photo : uselessPhotos) {
            View view = getLayoutInflater().inflate(R.layout.leaders_photo_unused_item, null);
            ImageViewRemote ivr = (ImageViewRemote) view.findViewById(R.id.ivLeadPhoto);
            TextView tv = (TextView) view.findViewById(R.id.lpuRating);
            tv.setText(getString(R.string.default_percent_equation, photo.mLiked));
            mUselessGridView.addView(view);
            ivr.setPhoto(photo);
        }
        if (mSelectedPhoto != null) {
            mSelectedPhoto.select(mLeadersPhotos.get(0).view, mLeadersPhotos.get(0).photo);
        }
    }

    private void splitPhotos(Photos photos) {
        for(Photo photo : photos) {
            if(photo.mLiked >= 25) {
                usePhotos.add(new Photo(photo));
            } else {
                uselessPhotos.add(new Photo(photo));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getProfile() {
        updateProfileInfo(CacheProfile.getProfile());
    }

    public static class LeadersPhoto {
        public ImageView view;
        public Photo photo;

        public LeadersPhoto(Photo photo, ImageView view) {
            this.photo = photo;
            this.view = view;
        }
    }

    public static class PhotoSelector {
        private ImageView mItem;
        private int mPhotoId;

        public void select(ImageView item, Photo photo) {
            if (item != null) {
                //При повторном клике на выбранный элемент, отключаем
                if (item.equals(mItem)) {
                    mItem.setImageResource(R.drawable.mask_normal_photo);
                    mItem = null;
                    mPhotoId = -1;

                } else {
                    if(mItem != null) {
                        mItem.setImageResource(R.drawable.mask_normal_photo);
                    }
                    item.setImageResource(R.drawable.mask_selected_photo);
                    mItem = item;
                    mPhotoId = photo.getId();
                }

            }
        }

        public boolean isSelected() {
            return mItem != null;
        }

        public int getPhotoId() {
            return mPhotoId;
        }

        public ImageView getItem() {
            return mItem;
        }
    }
}
