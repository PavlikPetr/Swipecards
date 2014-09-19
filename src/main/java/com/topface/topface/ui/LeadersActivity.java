package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.AlbumPhotos;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.LeaderRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.adapters.LeadersPhotoGridAdapter;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;

public class LeadersActivity extends BaseFragmentActivity {
    private GridView mGridView;
    private GridView mUselessGridView;
    private View mLoadingLocker;
    private Button mBuyButton;

    private Photos usePhotos;
    private Photos uselessPhotos;

    private LeadersPhotoGridAdapter mUsePhotosAdapter;
    private LeadersPhotoGridAdapter mUselessPhotosAdapter;

    private TextView mUselessTitle;
    private RelativeLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_leaders_layout);

        getSupportActionBar().setTitle(getString(R.string.leaders_go_date));

        mContainer = (RelativeLayout) findViewById(R.id.leadersCont);

        usePhotos = new Photos();
        uselessPhotos = new Photos();

//        mProgressBar = (ProgressBar) findViewById(R.id.loader);
        mGridView = (GridView) findViewById(R.id.useful_photos_grid);
        mUselessGridView = (GridView) findViewById(R.id.useless_photos_grid);
        mBuyButton = (Button) findViewById(R.id.btnLeadersBuy);
        mLoadingLocker = findViewById(R.id.llvLeaderSending);
        mUselessTitle = (TextView) findViewById(R.id.useless_photos_title_text);
        mUselessTitle.setText(String.format(getString(R.string.leaders_pick_condition), CacheProfile.getOptions().minLeadersPercent));
        if (CacheProfile.getOptions().minLeadersPercent == 0) {
            mUselessTitle.setVisibility(View.GONE);
        } else {
            mUselessTitle.setVisibility(View.VISIBLE);
        }
        setListeners();
        getProfile();
        setPrice(CacheProfile.getOptions().priceLeader);
    }

    private void setPrice(int price) {
        mBuyButton.setText(
                Utils.getQuantityString(
                        R.plurals.leaders_price,
                        price,
                        price
                )
        );
    }

    private void setListeners() {
        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int leadersPrice = CacheProfile.getOptions().priceLeader;
                int selectedPhotoId = mUsePhotosAdapter.getSelectedPhotoId();
                if (CacheProfile.money < leadersPrice) {
                    startActivity(PurchasesActivity.createBuyingIntent("Leaders", PurchasesFragment.TYPE_LEADERS, leadersPrice));
                } else if (selectedPhotoId != -1) {
                    mLoadingLocker.setVisibility(View.VISIBLE);
                    new LeaderRequest(selectedPhotoId, LeadersActivity.this)
                            .callback(new ApiHandler() {
                                @Override
                                public void success(IApiResponse response) {
                                    Toast.makeText(LeadersActivity.this, R.string.leaders_leader_now, Toast.LENGTH_SHORT).show();
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
        final AlbumRequest request = new AlbumRequest(this, profile.uid, AlbumRequest.MODE_LEADER, AlbumLoadController.FOR_GALLERY);
        final RetryViewCreator rv = RetryViewCreator.createDefaultRetryView(this, new OnClickListener() {
            @Override
            public void onClick(View view) {
                request.exec();
            }
        }, getResources().getColor(R.color.bg_main));
        rv.setVisibility(View.GONE);
        mContainer.addView(rv.getView());

        mUselessTitle.setVisibility(View.GONE);
        request.callback(new DataApiHandler<Photos>() {

            @Override
            protected void success(Photos data, IApiResponse response) {
                fillPhotos(data);
                rv.setVisibility(View.GONE);
            }

            @Override
            protected Photos parseResponse(ApiResponse response) {
                return new AlbumPhotos(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                rv.setVisibility(View.VISIBLE);
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                if (mLoadingLocker != null) {
                    mLoadingLocker.setVisibility(View.GONE);
                }
            }
        }).exec();

    }

    private void fillPhotos(Photos photos) {
        splitPhotos(photos);
        for (final Photo photo : usePhotos) {
            View view = getLayoutInflater().inflate(R.layout.leaders_photo_item, null);
            ImageViewRemote ivr = (ImageViewRemote) view.findViewById(R.id.ivLeadPhoto);
            ivr.setPhoto(photo);
        }
        mUsePhotosAdapter = new LeadersPhotoGridAdapter(this, usePhotos);
        mGridView.setAdapter(mUsePhotosAdapter);

        for (Photo photo : uselessPhotos) {
            View view = getLayoutInflater().inflate(R.layout.leaders_photo_unused_item, null);
            ImageViewRemote ivr = (ImageViewRemote) view.findViewById(R.id.ivLeadPhoto);
            TextView tv = (TextView) view.findViewById(R.id.lpuRating);
            tv.setText(getString(R.string.default_percent_equation, photo.mLiked));
            ivr.setPhoto(photo);
        }
        mUselessPhotosAdapter = new LeadersPhotoGridAdapter(this, uselessPhotos);
        mUselessGridView.setAdapter(mUselessPhotosAdapter);

        if (uselessPhotos.size() == 0) {
            mUselessTitle.setVisibility(View.GONE);
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
}
