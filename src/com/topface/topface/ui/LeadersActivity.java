package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.LeaderRequest;
import com.topface.topface.ui.adapters.LeadersPhotoAdapter;
import com.topface.topface.ui.profile.AddPhotoHelper;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class LeadersActivity extends BaseFragmentActivity {
    private GridView mGridView;
//    private ProgressBar mProgressBar;
    private PhotoSelector mSelectedPhoto = new PhotoSelector();
    private Button mBuyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_leaders_new);
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

//        mProgressBar = (ProgressBar) findViewById(R.id.loader);
        mGridView = (GridView) findViewById(R.id.fragmentGrid);
        mBuyButton = (Button) findViewById(R.id.btnLeadersBuy);

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
                    startActivity(new Intent(getApplicationContext(), BuyingActivity.class));
                } else if (mSelectedPhoto.isSelected()) {
                    new LeaderRequest(mSelectedPhoto.getPhotoId(), LeadersActivity.this)
                            .callback(new ApiHandler() {
                                @Override
                                public void success(ApiResponse response) {
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
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

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedPhoto.select(i, adapterView);
            }
        });
    }

    private void updateProfileInfo(Profile profile) {
        LeadersPhotoAdapter leadersAdapter = new LeadersPhotoAdapter(getApplicationContext(), profile.photos, mSelectedPhoto);
        mGridView.setAdapter(leadersAdapter);
        mSelectedPhoto.selectInitPhoto(profile.photo,profile.photos);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getProfile() {
        updateProfileInfo(CacheProfile.getProfile());

//        ProfileRequest profileRequest = new ProfileRequest(getApplicationContext());
//        profileRequest.part = ProfileRequest.P_ALL;
//        profileRequest.callback(new ApiHandler() {
//            @Override
//            public void success(final ApiResponse response) {
//                post(new Runnable() {
//                    @Override
//                    public void run() {
//                        updateProfileInfo(Profile.parse(response));
////                        mProgressBar.setVisibility(View.GONE);
//                    }
//                });
//            }
//
//            @Override
//            public void fail(int codeError, ApiResponse response) {
//                post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Utils.showErrorMessage(LeadersActivity.this);
////                        mProgressBar.setVisibility(View.GONE);
//                    }
//                });
//            }
//        }).exec();
    }

    public static class PhotoSelector {
        private int mItem;
        private int mPhotoId;

        public void select(int item, AdapterView<?> adapterView) {
            if (item >= 0) {
                //При повторном клике на выбранный элемент, отключаем
                if (item == mItem) {
                    mItem = -1;
                    mPhotoId = -1;
                } else {
                    Photo photo = (Photo) adapterView.getItemAtPosition(item);
                    mItem = item;
                    mPhotoId = photo.getId();
                }
                ((LeadersPhotoAdapter) adapterView.getAdapter()).notifyDataSetChanged();
            }
        }

        public void selectInitPhoto(Photo avatar, Photos photos) {
            for (int i=0; i < photos.size(); i++) {
                if (avatar.getId() == photos.get(i).getId()) {
                    mItem = i;
                    break;
                }
            }
            mPhotoId = avatar.getId();
        }

        public boolean isSelected() {
            return mItem > 0;
        }

        public int getPhotoId() {
            return mPhotoId;
        }

        public int getItemId() {
            return mItem;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            getProfile();
            if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_OK) {
                Toast.makeText(LeadersActivity.this, R.string.photo_add_or, Toast.LENGTH_SHORT).show();
            } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                Toast.makeText(LeadersActivity.this, R.string.photo_add_error, Toast.LENGTH_SHORT).show();
            }
        }
    };

}
