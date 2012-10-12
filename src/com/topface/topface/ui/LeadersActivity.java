package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.android.apps.analytics.easytracking.EasyTracker;
import com.google.android.apps.analytics.easytracking.TrackedActivity;
import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Album;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.LeaderRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.adapters.LeadersAlbumAdapter;
import com.topface.topface.ui.blocks.HorizontalListView;
import com.topface.topface.ui.profile.AddPhotoHelper;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

import java.util.LinkedList;

public class LeadersActivity extends TrackedActivity {
    private HorizontalListView mListView;
    private ProgressBar mProgressBar;
    private ViewGroup mLeadersContent;
    private PhotoSelector mSelectedPhoto = new PhotoSelector();
    private Button mBuyButton;
    private AddPhotoHelper mAddPhotoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_leaders);
        ((TextView) findViewById(R.id.tvHeaderTitle)).setText(R.string.leaders_go_date);

        mProgressBar = (ProgressBar) findViewById(R.id.loader);
        mLeadersContent = (ViewGroup) findViewById(R.id.leadersContent);
        mListView = (HorizontalListView) findViewById(R.id.photoAlbum);
        mBuyButton = (Button) findViewById(R.id.btnLeadersBuy);
        mAddPhotoHelper = new AddPhotoHelper(LeadersActivity.this, this);
        mAddPhotoHelper.setOnResultHandler(mHandler);

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
                    EasyTracker.getTracker().trackEvent("Purchase", "PageBecomeLeader", "", 0);
                    startActivity(new Intent(getApplicationContext(), BuyingActivity.class));
                } else if (mSelectedPhoto.isSelected()) {
                    new LeaderRequest(mSelectedPhoto.getPhotoId(), LeadersActivity.this)
                            .callback(new ApiHandler() {
                                @Override
                                public void success(ApiResponse response) throws NullPointerException {
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            EasyTracker.getTracker().trackEvent("BecomeLeader", "", "", 0);
                                            Toast.makeText(LeadersActivity.this, R.string.leaders_leader_now, Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                                }

                                @Override
                                public void fail(int codeError, ApiResponse response) throws NullPointerException {
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(LeadersActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).exec();

                } else {
                    Toast.makeText(LeadersActivity.this, R.string.need_select_photo, Toast.LENGTH_SHORT).show();
                    sendStat("NeedSelectPhotoError", null);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    sendStat("AddPhoto", null);
                    mAddPhotoHelper.addPhoto();
                } else {
                    sendStat("SelectPhoto", null);
                    mSelectedPhoto.select(i, adapterView);
                }
            }
        });
    }

    private void updateProfileInfo(Profile profile) {
        Debug.log(profile.toString());
        LinkedList<Album> photoList = new LinkedList<Album>();
        for (Album album : profile.albums) {
            if (!album.ero) {
                photoList.add(album);
            }
        }

        LeadersAlbumAdapter leadersAdapter = new LeadersAlbumAdapter(getApplicationContext(), photoList, mSelectedPhoto);
        mListView.setAdapter(leadersAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAddPhotoHelper.checkActivityResult(requestCode, resultCode, data);
    }

    private void getProfile() {
        ProfileRequest profileRequest = new ProfileRequest(getApplicationContext());
        profileRequest.part = ProfileRequest.P_ALL;
        profileRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        updateProfileInfo(Profile.parse(response));
                        mProgressBar.setVisibility(View.GONE);
                        mLeadersContent.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showErrorMessage(LeadersActivity.this);
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).exec();
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
                    Album album = (Album) adapterView.getItemAtPosition(item);
                    mItem = item;
                    mPhotoId = album.id;
                }
                ((LeadersAlbumAdapter) adapterView.getAdapter()).notifyDataSetChanged();
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

    private void sendStat(String action, String label, int value) {
        action = action == null ? "" : action;
        label = label == null ? "" : label;
        EasyTracker.getTracker().trackEvent("Leaders", action, label, value);
    }

    private void sendStat(String action, String label) {
        action = action == null ? "" : action;
        label = label == null ? "" : label;
        EasyTracker.getTracker().trackEvent("Leaders", action, label, 0);
    }
}
