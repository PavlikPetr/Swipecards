package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.gridlayout.GridLayout;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import java.util.LinkedList;

public class LeadersActivity extends BaseFragmentActivity {
    private com.topface.topface.ui.gridlayout.GridLayout mGridView;
    private GridLayout mUselessGridView;
    private LockerView mLoadingLocker;
    private PhotoSelector mSelectedPhoto = new PhotoSelector();
    private Button mBuyButton;

    private Photos usePhotos;
    private Photos uselessPhotos;


    private LinkedList<LeadersPhoto> mLeadersPhotos = new LinkedList<LeadersPhoto>();
    private TextView mUselessTitle;
    private RelativeLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_leaders_layout);

        getSupportActionBar().setTitle(getString(R.string.leaders_go_date));

        mContainer = (RelativeLayout) findViewById(R.id.leadersCont);

        usePhotos = new Photos();
        uselessPhotos = new Photos();

//        mProgressBar = (ProgressBar) findViewById(R.id.loader);
        mGridView = (GridLayout) findViewById(R.id.usedGrid);
        mUselessGridView = (GridLayout) findViewById(R.id.unusedGrid);
        mBuyButton = (Button) findViewById(R.id.btnLeadersBuy);
        mLoadingLocker = (LockerView) findViewById(R.id.llvLeaderSending);
        mUselessTitle = (TextView) findViewById(R.id.unusedTitle);
        mUselessTitle.setText(String.format(getString(R.string.leaders_pick_condition), CacheProfile.getOptions().minLeadersPercent));
        if (CacheProfile.getOptions().minLeadersPercent == 0) {
            mUselessTitle.setVisibility(View.GONE);
        } else {
            mUselessTitle.setVisibility(View.VISIBLE);
        }


        setListeners();
        getProfile();
        setPrice();
    }

    private void setPrice() {
        int leadersPrice = CacheProfile.getOptions().priceLeader;
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
                if (CacheProfile.money < CacheProfile.getOptions().priceLeader) {
                    startActivity(ContainerActivity.getBuyingIntent("Leaders"));
                } else if (mSelectedPhoto.isSelected()) {
                    mLoadingLocker.setVisibility(View.VISIBLE);
                    new LeaderRequest(mSelectedPhoto.getPhotoId(), LeadersActivity.this)
                            .callback(new ApiHandler() {
                                @Override
                                public void success(IApiResponse response) {
                                    mLoadingLocker.setVisibility(View.GONE);
                                    Toast.makeText(LeadersActivity.this, R.string.leaders_leader_now, Toast.LENGTH_SHORT).show();
                                    //Обновляем число монет
                                    CacheProfile.money = response.getJsonResult().optInt("money", CacheProfile.money);
                                    LocalBroadcastManager.getInstance(LeadersActivity.this)
                                            .sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
                                    finish();
                                }

                                @Override
                                public void fail(int codeError, IApiResponse response) {
                                    mLoadingLocker.setVisibility(View.GONE);
                                    Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                                }
                            }).exec();

                } else {
                    Toast.makeText(LeadersActivity.this, R.string.leaders_need_photo, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateProfileInfo(Profile profile) {
        mLoadingLocker.setVisibility(View.VISIBLE);
        final AlbumRequest request = new AlbumRequest(this, profile.uid, AlbumRequest.DEFAULT_PHOTOS_LIMIT, AlbumRequest.MODE_LEADER);


        final RetryViewCreator rv = RetryViewCreator.createDefaultRetryView(this, new OnClickListener() {
            @Override
            public void onClick(View view) {
                request.exec();
            }
        });
        rv.setVisibility(View.GONE);
        mContainer.addView(rv.getView());

        mUselessTitle.setVisibility(View.GONE);
        request.callback(new DataApiHandler<Photos>() {

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                if (mLoadingLocker != null) {
                    mLoadingLocker.setVisibility(View.GONE);
                }
            }

            @Override
            protected void success(Photos data, IApiResponse response) {
                fillPhotos(data);
                mLoadingLocker.setVisibility(View.GONE);
                rv.setVisibility(View.GONE);
//                mUselessTitle.setVisibility(View.VISIBLE);
            }

            @Override
            protected Photos parseResponse(ApiResponse response) {
                return Photos.parse(response.getJsonResult().optJSONArray("items"));
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                mLoadingLocker.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);

            }
        }).exec();

    }

    private void fillPhotos(Photos photos) {
        splitPhotos(photos);
        for (final Photo photo : usePhotos) {
            View view = getLayoutInflater().inflate(R.layout.leaders_photo_item, null);
            ImageViewRemote ivr = (ImageViewRemote) view.findViewById(R.id.ivLeadPhoto);
            final ImageView mask = (ImageView) view.findViewById(R.id.lpiMask);
            mLeadersPhotos.add(new LeadersPhoto(photo, mask));
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSelectedPhoto != null) {
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

        if (uselessPhotos.size() == 0) {
            mUselessTitle.setVisibility(View.GONE);
        }

        if (mSelectedPhoto != null) {
            if (mLeadersPhotos.size() > 0) {
                mSelectedPhoto.select(mLeadersPhotos.get(0).view, mLeadersPhotos.get(0).photo);
            }
        }
    }

    private void splitPhotos(Photos photos) {
        for (Photo photo : photos) {
            if (photo.canBecomeLeader || CacheProfile.getOptions().minLeadersPercent == 0) {
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
                    if (mItem != null) {
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
