package com.topface.topface.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.AlbumPhotos;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.Options;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.experiments.FeedScreensIntent;
import com.topface.topface.requests.AddPhotoFeedRequest;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.adapters.LeadersPhotoGridAdapter;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.dialogs.TakePhotoDialog;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.IPhotoTakerWithDialog;
import com.topface.topface.utils.PhotoTaker;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.actionbar.ActionBarTitleSetterDelegate;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;

import org.json.JSONException;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

public class AddToLeaderActivity extends BaseFragmentActivity implements View.OnClickListener {

    public final static int ADD_TO_LEADER_ACTIVITY_ID = 1;
    @Inject
    TopfaceAppState mAppState;
    private static final String PHOTOS = "PHOTOS";
    private static final String POSITION = "POSITION";
    private static final String SELECTED_POSITION = "SELECTED_POSITION";
    private static final String ALREADY_SHOWN = "ALREADY_SHOWN";
    private static final int MAX_SYMBOL_COUNT = 120;
    private int mCoins;
    private Action1<BalanceData> mBalanceAction = new Action1<BalanceData>() {
        @Override
        public void call(BalanceData balanceData) {
            mCoins = balanceData.money;
        }
    };
    private Subscription mBalanceSubscription;
    private int mPosition;
    private int mSelectedPosition;
    private boolean mIsPhotoDialogShown;
    private GridViewWithHeaderAndFooter mGridView;
    private LockerView mLoadingLocker;
    private EditText mEditText;
    private View mGridFooterView;
    private LeadersPhotoGridAdapter mUsePhotosAdapter;
    private AddPhotoHelper mAddPhotoHelper;
    private TakePhotoDialog takePhotoDialog;
    private IPhotoTakerWithDialog mPhotoTaker;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AddPhotoHelper.handlePhotoMessage(msg);
        }
    };
    Photos mPhotos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(this).inject(this);
        mBalanceSubscription = mAppState.getObservable(BalanceData.class).subscribe(mBalanceAction);
        mGridFooterView = createGridViewFooter();
        mGridView = (GridViewWithHeaderAndFooter) findViewById(R.id.user_photos_grid);
        addFooterView();
        mLoadingLocker = (LockerView) findViewById(R.id.llvLeaderSending);
        if (savedInstanceState != null) {
            mPhotos = JsonUtils.fromJson(savedInstanceState.getString(PHOTOS), Photos.class);
            mPosition = savedInstanceState.getInt(POSITION, 0);
            mSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION, 0);
            mIsPhotoDialogShown = savedInstanceState.getBoolean(ALREADY_SHOWN);
        }
        mGridView.addHeaderView(getHeaderView());
        // add title to actionbar
        new ActionBarTitleSetterDelegate(getSupportActionBar()).setActionBarTitles(R.string.general_photoblog, null);
        // init grid view and create adapter
        initPhotosGrid(mPosition, mSelectedPosition);
        mPhotoTaker = new PhotoTaker(initAddPhotoHelper(), this);
        takePhotoDialog = (TakePhotoDialog) getSupportFragmentManager().findFragmentByTag(TakePhotoDialog.TAG);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.ac_photoblog;
    }

    @Override
    public void onResume() {
        super.onResume();
        showPhotoHelper();
    }

    private void showPhotoHelper() {
        showPhotoHelper(getString(R.string.no_photo_take_photo), !mIsPhotoDialogShown);
    }

    private void showPhotoHelper(String message, boolean isNeedShow) {
        if (takePhotoDialog != null) {
            takePhotoDialog.setPhotoTaker(mPhotoTaker);
        }
        if (isNeedShow) {
            mAddPhotoHelper = initAddPhotoHelper();
            if (CacheProfile.getProfile().photo == null && takePhotoDialog == null) {
                mAddPhotoHelper.showTakePhotoDialog(mPhotoTaker, null, message);
                mIsPhotoDialogShown = true;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBalanceSubscription.unsubscribe();
    }

    @Override
    protected void onProfileUpdated() {
        super.onProfileUpdated();
        mPhotos = CacheProfile.getProfile().photos;
        LeadersPhotoGridAdapter adapter = getAdapter();
        adapter.setData(mPhotos, false);
        setSeletedPosition(0, CacheProfile.getProfile().photos.isEmpty() ? 0 : CacheProfile.getProfile().photos.get(0).getId());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case AddPhotoHelper.GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY_WITH_DIALOG:
                case AddPhotoHelper.GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA_WITH_DIALOG:
                    if (mAddPhotoHelper != null) {
                        mAddPhotoHelper.showTakePhotoDialog(mPhotoTaker, mAddPhotoHelper.processActivityResult(requestCode, resultCode, data, false));
                        mIsPhotoDialogShown = true;
                    }
                    break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putString(PHOTOS, getAdapter().getAdapterData().toJson().toString());
        } catch (JSONException e) {
            Debug.error(e);
        }
        outState.putInt(POSITION, mPosition);
        outState.putInt(SELECTED_POSITION, mSelectedPosition);
        outState.putBoolean(ALREADY_SHOWN, mIsPhotoDialogShown);
    }

    private View getHeaderView() {
        View headerView = getLayoutInflater().inflate(R.layout.add_leader_grid_view_header, null);
        mEditText = (EditText) headerView.findViewById(R.id.yourGreetingEditText);
        // set max symbol count for input status
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_SYMBOL_COUNT)});
        initButtons(headerView);
        return headerView;
    }

    private void initButtons(View headerView) {
        LinearLayout buttonsLayout = (LinearLayout) headerView.findViewById(R.id.buttonsContainer);
        buttonsLayout.removeAllViews();
        List<Options.LeaderButton> buttons = getOptions().buyLeaderButtons;
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

    @Override
    protected void onPause() {
        if (mEditText != null) {
            Utils.hideSoftKeyboard(this, mEditText);
        }
        mPosition = mGridView.getFirstVisiblePosition();
        mSelectedPosition = getAdapter().getSelectedPhotoId();
        mIsPhotoDialogShown = true;
        super.onPause();
    }

    private void pressedAddToLeader(int position) {
        final Options.LeaderButton buttonData = getOptions().buyLeaderButtons.get(position);
        int selectedPhotoId = getAdapter().getSelectedPhotoId();
        if (getAdapter().getCount() > 0) {
            if (mCoins < buttonData.price) {
                showPurchasesFragment(buttonData.price);
            } else if (selectedPhotoId > LeadersPhotoGridAdapter.EMPTY_SELECTED_ID) {
                mLoadingLocker.setVisibility(View.VISIBLE);
                new AddPhotoFeedRequest(selectedPhotoId, AddToLeaderActivity.this, buttonData.photoCount, mEditText.getText().toString(), (long) buttonData.price)
                        .callback(new ApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                setResult(Activity.RESULT_OK, new Intent());
                                finish();
                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                                mLoadingLocker.setVisibility(View.GONE);
                                if (codeError == ErrorCodes.PAYMENT) {
                                    showPurchasesFragment(buttonData.price);
                                } else {
                                    Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).exec();

            } else {
                Toast.makeText(App.getContext(), R.string.leaders_need_photo, Toast.LENGTH_SHORT).show();
            }
        } else {
            showPhotoHelper(getString(R.string.add_photo_before), true);
        }
    }

    private void showPurchasesFragment(int price) {
        Debug.error("money price " + price);
        startActivity(PurchasesActivity.createBuyingIntent(this.getLocalClassName()
                , PurchasesFragment.TYPE_LEADERS, price, getOptions().topfaceOfferwallRedirect));
    }


    private LeadersPhotoGridAdapter getAdapter() {
        if (mUsePhotosAdapter == null) {
            mUsePhotosAdapter = createAdapter();
        }
        return mUsePhotosAdapter;
    }

    private void initPhotosGrid(final int position, final int selectedPosition) {
        mGridView.post(new Runnable() {
            @Override
            public void run() {
                LeadersPhotoGridAdapter adapter = getAdapter();
                mGridView.setAdapter(adapter);
                mGridView.setOnScrollListener(adapter);
                setSeletedPosition(position, selectedPosition);
            }
        });
    }

    private void setSeletedPosition(int position, int selectedPosition) {
        mGridView.setSelection(position);
        LeadersPhotoGridAdapter adapter = getAdapter();
        if (adapter != null && adapter.getPhotoLinks().size() > 0 && selectedPosition != 0) {
            adapter.setSelectedPhotoId(selectedPosition);
        }
    }

    private Photos getPhotoLinks() {
        Photos photoLinks = new Photos();
        photoLinks.clear();
        if (CacheProfile.getProfile().photos != null) {
            photoLinks.addAll(CacheProfile.getProfile().photos);
        }
        return checkPhotos(photoLinks);
    }

    private Photos checkPhotos(Photos photos) {
        Photos result = new Photos();
        result.clear();
        for (Photo photo : photos) {
            if (photo.canBecomeLeader) {
                result.add(new Photo(photo));
            }
        }
        return result;
    }


    private LeadersPhotoGridAdapter createAdapter() {
        Photos photos = getPhotoLinks();
        return new LeadersPhotoGridAdapter(this.getApplicationContext(),
                photos,
                photos.size(),
                mGridView.getGridViewColumnWidth(), new LoadingListAdapter.Updater() {
            @Override
            public void onUpdate() {
                sendAlbumRequest();
            }
        });
    }

    private void sendAlbumRequest() {
        Photos photoLinks = getAdapter().getAdapterData();
        if (photoLinks == null || photoLinks.size() < 2) {
            return;
        }
        mGridFooterView.setVisibility(View.VISIBLE);
        Photo photo = getAdapter().getItem(photoLinks.size() - 2);
        int position = photo.getPosition();
        AlbumRequest request = new AlbumRequest(
                this.getApplicationContext(),
                CacheProfile.getProfile().uid,
                position + 1,
                AlbumRequest.MODE_ALBUM,
                AlbumLoadController.FOR_GALLERY,
                true
        );
        request.callback(new DataApiHandler<AlbumPhotos>() {

            @Override
            protected void success(AlbumPhotos data, IApiResponse response) {
                if (getAdapter() != null) {
                    getAdapter().addPhotos(data, data.more, false);
                }
            }

            @Override
            protected AlbumPhotos parseResponse(ApiResponse response) {
                return new AlbumPhotos(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Utils.showErrorMessage();
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                mGridFooterView.setVisibility(View.GONE);
            }
        }).exec();
    }

    private void addFooterView() {
        if (mGridView != null) {
            if (mGridView.getFooterViewCount() == 0) {
                mGridView.addFooterView(mGridFooterView);
            }
            mGridFooterView.setVisibility(View.GONE);
        }
    }

    private View createGridViewFooter() {
        return ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.gridview_footer_progress_bar, null, false);
    }

    private AddPhotoHelper initAddPhotoHelper() {
        if (mAddPhotoHelper == null) {
            mAddPhotoHelper = new AddPhotoHelper(this);
        }
        mAddPhotoHelper.setOnResultHandler(mHandler);
        return mAddPhotoHelper;
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        FeedScreensIntent.equipPhotoFeedIntent(intent);
        return intent;
    }
}
