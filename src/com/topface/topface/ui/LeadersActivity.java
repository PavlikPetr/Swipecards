package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.LeaderRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.adapters.LeadersPhotoAdapter;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class LeadersActivity extends BaseFragmentActivity {
    private GridView mGridView;
    private LockerView mLoadingLocker;
    private PhotoSelector mSelectedPhoto = new PhotoSelector();
    private Button mBuyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_leaders);
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
                    startActivity(new Intent(getApplicationContext(), BuyingActivity.class));
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

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    mAddPhotoHelper.addPhoto();
                } else {
                    mSelectedPhoto.select(i, adapterView);
                }
            }
        });
    }

    private void updateProfileInfo(Profile profile) {
        LeadersPhotoAdapter leadersAdapter = new LeadersPhotoAdapter(getApplicationContext(), profile.photos, mSelectedPhoto);
        mListView.setAdapter(leadersAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAddPhotoHelper.checkActivityResult(requestCode, resultCode, data);
    }

    private void getProfile() {
        updateProfileInfo(CacheProfile.getProfile());
    }

    public static class PhotoSelector {
        private int mItem;
        private int mPhotoId;

        public void select(int item, AdapterView<?> adapterView) {
            if (item > 0) {
                //При повторном клике на выбранный элемент, отключаем
                if (item == mItem) {
                    mItem = 0;
                    mPhotoId = 0;
                } else {
                    Photo photo = (Photo) adapterView.getItemAtPosition(item);
                    mItem = item;
                    mPhotoId = photo.getId();
                }
                ((LeadersPhotoAdapter) adapterView.getAdapter()).notifyDataSetChanged();
            }
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
}
