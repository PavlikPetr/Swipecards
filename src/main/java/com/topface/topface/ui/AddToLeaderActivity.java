package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.AlbumPhotos;
import com.topface.topface.data.Options;
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
import com.topface.topface.ui.views.GridViewWithHeaderAndFooter;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.actionbar.ActionBarTitleSetterDelegate;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;

import java.util.List;

public class AddToLeaderActivity extends BaseFragmentActivity implements View.OnClickListener {

    public final static int ADD_TO_LEADER_ACTIVITY_ID = 1;

    private GridViewWithHeaderAndFooter mGridView;
    private LockerView mLoadingLocker;
    private EditText mEditText;
    private LeadersPhotoGridAdapter mUsePhotosAdapter;

    private Photos usePhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_photoblog);
        mGridView = (GridViewWithHeaderAndFooter) findViewById(R.id.user_photos_grid);
        mLoadingLocker = (LockerView) findViewById(R.id.llvLeaderSending);
        usePhotos = new Photos();
        mGridView.addHeaderView(getHeaderView());
        // add title to actionbar
        new ActionBarTitleSetterDelegate(getSupportActionBar()).setActionBarTitles(R.string.general_photoblog, null);
    }


    private View getHeaderView() {
        View headerView = getLayoutInflater().inflate(R.layout.add_leader_grid_view_header, null);
        mEditText = (EditText) headerView.findViewById(R.id.yourGreetingEditText);
        initButtons(headerView);
        return headerView;
    }

    private void initButtons(View headerView) {
        LinearLayout buttonsLayout = (LinearLayout) headerView.findViewById(R.id.buttonsContainer);
        buttonsLayout.removeAllViews();
        List<Options.LeaderButton> buttons = CacheProfile.getOptions().buyLeaderButtons;
        for (int i = 0; i < buttons.size(); i++) {
            getLayoutInflater().inflate(R.layout.add_leader_button, buttonsLayout);
            // get last added view
            Button buttonCurrent = (Button) buttonsLayout.getChildAt(buttonsLayout.getChildCount() - 1);
            buttonCurrent.setText(buttons.get(i).title);
            buttonCurrent.setTag(i);
            buttonCurrent.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            pressedAddToLeader((int) v.getTag());
        }
    }

    private void pressedAddToLeader(int position) {
        Options.LeaderButton buttonData = CacheProfile.getOptions().buyLeaderButtons.get(position);
        int leadersPrice = buttonData.price * buttonData.photoCount;
        int selectedPhotoId = mUsePhotosAdapter.getSelectedPhotoId();
        if (CacheProfile.money < leadersPrice) {
            startActivity(PurchasesActivity.createBuyingIntent("Leaders", PurchasesFragment.TYPE_LEADERS, leadersPrice));
        } else if (selectedPhotoId != -1) {
            mLoadingLocker.setVisibility(View.VISIBLE);
            new LeaderRequest(selectedPhotoId, AddToLeaderActivity.this, buttonData.photoCount, mEditText.getText().toString(), (long) leadersPrice)
                    .callback(new ApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            setResult(Activity.RESULT_OK, new Intent());
                            Toast.makeText(AddToLeaderActivity.this, R.string.leaders_leader_now, Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            mLoadingLocker.setVisibility(View.GONE);
                            Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                        }
                    }).exec();

        } else {
            Toast.makeText(AddToLeaderActivity.this, R.string.leaders_need_photo, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onLoadProfile() {
        super.onLoadProfile();
        getProfile();
    }

    private void getProfile() {
        updateProfileInfo(CacheProfile.getProfile());
    }

    private void updateProfileInfo(Profile profile) {
        mLoadingLocker.setVisibility(View.VISIBLE);
        final AlbumRequest request = new AlbumRequest(this, profile.uid, AlbumRequest.MODE_LEADER, AlbumLoadController.FOR_GALLERY);
        final RetryViewCreator rv = new RetryViewCreator.Builder(this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                request.exec();
            }
        }).backgroundColor(getResources().getColor(R.color.bg_main)).build();
        rv.setVisibility(View.GONE);
//        mLoadingLocker.addView(rv.getView());

        request.callback(new DataApiHandler<Photos>() {

            @Override
            protected void success(Photos data, IApiResponse response) {
                fillPhotos(data);
                rv.setVisibility(View.VISIBLE);
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
        mGridView.setColumnWidth(mGridView.getGridViewColumnWidth());
        mGridView.setNumColumns(mGridView.getGridViewNumColumns());
        mUsePhotosAdapter = new LeadersPhotoGridAdapter(this, usePhotos, mGridView.getGridViewColumnWidth());
        mGridView.setAdapter(mUsePhotosAdapter);
    }

    private void splitPhotos(Photos photos) {
        usePhotos.clear();
        for (Photo photo : photos) {
            if (photo.canBecomeLeader || CacheProfile.getOptions().minLeadersPercent == 0) {
                usePhotos.add(new Photo(photo));
            }
        }
    }
}
