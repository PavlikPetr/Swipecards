package com.topface.topface.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.InputFilter;
import android.util.Log;
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
import com.topface.topface.data.Profile;
import com.topface.topface.data.experiments.FeedScreensIntent;
import com.topface.topface.requests.AddPhotoFeedRequest;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.statistics.TakePhotoStatistics;
import com.topface.topface.ui.adapters.LeadersRecyclerViewAdapter;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.dialogs.TakePhotoPopup;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.FlurryManager;
import com.topface.topface.utils.RxUtils;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.actionbar.ActionBarTitleSetterDelegate;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;

import static com.topface.topface.utils.FlurryManager.GET_LEAD;

public class AddToLeaderActivity extends BaseFragmentActivity implements View.OnClickListener {

    public final static int ADD_TO_LEADER_ACTIVITY_ID = 1;
    @Inject
    TopfaceAppState mAppState;
    private static final String PHOTOS = "photos";
    private static final String SELECTED_POSITION = "selected_position";
    private static final String ALREADY_SHOWN = "already_shown";
    private static final String PAGE_NAME = "adtoleader";
    private static final String GREETING_TEXT = "greeting_text";
    private static final String IS_KEY_BOARD_SHOW = "is_key_board_show";

    private static final int MAX_SYMBOL_COUNT = 120;
    private int mCoins;
    private Action1<BalanceData> mBalanceAction = new Action1<BalanceData>() {
        @Override
        public void call(BalanceData balanceData) {
            mCoins = balanceData.money;
        }
    };
    private int mSelectedPosition;
    private boolean mIsPhotoDialogShown;
    private LeadersRecyclerViewAdapter mUsePhotosAdapter;
    private AddPhotoHelper mAddPhotoHelper;
    private EditText mEditText;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AddPhotoHelper.handlePhotoMessage(msg);
        }
    };
    Photos mPhotos = null;
    private String mGreetingText = Utils.EMPTY;
    private boolean mIsKeyBoardShown;
    private CompositeSubscription mSubscriptions = new CompositeSubscription();

    @Bind(R.id.user_photos_grid)
    RecyclerView mRecyclerView;
    @Bind(R.id.llvLeaderSending)
    LockerView mLoadingLocker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        App.get().inject(this);
        mSubscriptions.add(mAppState.getObservable(BalanceData.class).subscribe(mBalanceAction));
        if (savedInstanceState != null) {
            mPhotos = JsonUtils.fromJson(savedInstanceState.getString(PHOTOS), Photos.class);
            mSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION, 0);
            mIsPhotoDialogShown = savedInstanceState.getBoolean(ALREADY_SHOWN);
            mGreetingText = savedInstanceState.getString(GREETING_TEXT);
            mIsKeyBoardShown = savedInstanceState.getBoolean(IS_KEY_BOARD_SHOW, false);
        }
        // add title to actionbar
        new ActionBarTitleSetterDelegate(getSupportActionBar()).setActionBarTitles(R.string.general_photoblog, null);
        // init grid view and create adapter
        initPhotosGrid(mSelectedPosition);
        initAddPhotoHelper();
        mSubscriptions.add(mAppState.getObservable(Profile.class).subscribe(new Action1<Profile>() {
            @Override
            public void call(Profile profile) {
                handlePhotos(profile);
            }
        }));
    }

    private void handlePhotos(final @NotNull Profile profile) {
        if (profile.photos.size() == 0) {
            getAdapter().getAdapterData().clear();
            getAdapter().getAdapterData().add(Photo.createFakePhoto());
            getAdapter().notifyDataSetChanged();
            return;
        }
        Observable.just(profile)
                .flatMap(new Func1<Profile, Observable<Photo>>() {
                    @Override
                    public Observable<Photo> call(Profile profile) {
                        return Observable.from(profile.photos);
                    }
                })
                .filter(new Func1<Photo, Boolean>() {
                    @Override
                    public Boolean call(Photo photo) {
                        return !getAdapter().getAdapterData().contains(photo);
                    }
                })
                .reduce(new Photos(), new Func2<Photos, Photo, Photos>() {
                    @Override
                    public Photos call(Photos photos, Photo photo) {
                        photos.add(photo);
                        return photos;
                    }
                }).subscribe(new RxUtils.ShortSubscription<Photos>() {
            @Override
            public void onNext(Photos photos) {
                getAdapter().addPhotos(getPhotoLinks(photos), profile.photos.size() < profile.photosCount, false, true);
            }
        });
    }

    @Override
    protected int getContentLayout() {
        return R.layout.ac_photoblog;
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    public void onResume() {
        super.onResume();
        showPhotoHelper();
    }

    private void showPhotoHelper() {
        showPhotoHelper(!mIsPhotoDialogShown);
    }

    private void showPhotoHelper(boolean isNeedShow) {
        if (isNeedShow) {
            if (!App.getConfig().getUserConfig().isUserAvatarAvailable() && App.get().getProfile().photo == null) {
                TakePhotoPopup.newInstance(TakePhotoStatistics.PLC_ADD_TO_LEADER).show(getSupportFragmentManager(), TakePhotoPopup.TAG);
                mIsPhotoDialogShown = true;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxUtils.safeUnsubscribe(mSubscriptions);
        if (mAddPhotoHelper != null) {
            mAddPhotoHelper.releaseHelper();
        }
        ButterKnife.unbind(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mAddPhotoHelper != null) {
            mAddPhotoHelper.processActivityResult(requestCode, resultCode, data);
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
        outState.putInt(SELECTED_POSITION, mSelectedPosition);
        outState.putBoolean(ALREADY_SHOWN, mIsPhotoDialogShown);
        outState.putString(GREETING_TEXT, mEditText != null ? mEditText.getText().toString() : Utils.EMPTY);
        outState.putBoolean(IS_KEY_BOARD_SHOW, mEditText != null && mEditText.hasFocus());
    }

    private View createHeaderView() {
        View headerView = getLayoutInflater().inflate(R.layout.add_leader_grid_view_header, null);
        mEditText = (EditText) headerView.findViewById(R.id.yourGreetingEditText);
        mEditText.setText(mGreetingText);
        if (mIsKeyBoardShown) {
            mEditText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.showSoftKeyboard(AddToLeaderActivity.this.getApplicationContext(), mEditText);
                    mEditText.setSelection(mGreetingText.length());
                }
            }, 200);
        }
        // set max symbol count for input status
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_SYMBOL_COUNT)});
        initButtons(headerView);
        return headerView;
    }

    private void initButtons(View headerView) {
        LinearLayout buttonsLayout = (LinearLayout) headerView.findViewById(R.id.buttonsContainer);
        buttonsLayout.removeAllViews();
        List<Options.LeaderButton> buttons = App.from(this).getOptions().buyLeaderButtons;
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
        mSelectedPosition = getAdapter().getSelectedPhotoId();
        mIsPhotoDialogShown = true;
        super.onPause();
    }

    private void pressedAddToLeader(int position) {
        final Options.LeaderButton buttonData = App.from(this).getOptions().buyLeaderButtons.get(position);
        int selectedPhotoId = getAdapter().getSelectedPhotoId();
        if (getAdapter().getItemCount() > 1) {
            if (mCoins < buttonData.price) {
                showPurchasesFragment(buttonData.price);
            } else if (selectedPhotoId > LeadersRecyclerViewAdapter.EMPTY_SELECTED_ID) {
                mLoadingLocker.setVisibility(View.VISIBLE);
                new AddPhotoFeedRequest(selectedPhotoId, AddToLeaderActivity.this, buttonData.photoCount, mEditText.getText().toString(), (long) buttonData.price)
                        .callback(new ApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                FlurryManager.getInstance().sendSpendCoinsEvent(buttonData.price, GET_LEAD);
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
            showPhotoHelper(true);
        }
    }

    private void showPurchasesFragment(int price) {
        Debug.error("money price " + price);
        startActivity(PurchasesActivity.createBuyingIntent(this.getLocalClassName()
                , PurchasesFragment.TYPE_LEADERS, price, App.from(this).getOptions().topfaceOfferwallRedirect));
    }


    private LeadersRecyclerViewAdapter getAdapter() {
        if (mUsePhotosAdapter == null) {
            mUsePhotosAdapter = createAdapter();
        }
        return mUsePhotosAdapter;
    }

    private void initPhotosGrid(final int selectedPosition) {
        int spanCount = getResources().getInteger(R.integer.add_to_leader_column_count);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(getAdapter());
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                setSelectedPosition(selectedPosition);
            }
        });
    }


    private void setSelectedPosition(int selectedPosition) {
        LeadersRecyclerViewAdapter adapter = getAdapter();
        if (adapter != null && !adapter.getAdapterData().isEmpty() && selectedPosition != 0) {
            adapter.setSelectedPhotoId(selectedPosition);
        }
    }

    private Photos getPhotoLinks(Photos photos) {
        Photos photoLinks = new Photos();
        photoLinks.clear();
        if (photos != null) {
            photoLinks.addAll(photos);
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


    private LeadersRecyclerViewAdapter createAdapter() {
        Photos photos = getPhotoLinks(App.get().getProfile().photos);
        return (LeadersRecyclerViewAdapter) new LeadersRecyclerViewAdapter(
                photos,
                photos.size(), new LoadingListAdapter.Updater() {
            @Override
            public void onUpdate() {
                sendAlbumRequest();
            }
        })
                .setHeader(createHeaderView(), false)
                .setFooter(createFooterView(), false);
    }

    private void sendAlbumRequest() {
        Photos photoLinks = getAdapter().getAdapterData();
        if (photoLinks == null || photoLinks.size() < 2) {
            return;
        }
        Photo photo = getAdapter().getItem(photoLinks.size() - 2);
        int position = photo.getPosition();
        AlbumRequest request = new AlbumRequest(
                this.getApplicationContext(),
                App.from(this).getProfile().uid,
                position + 1,
                AlbumRequest.MODE_ALBUM,
                AlbumLoadController.FOR_GALLERY,
                true
        );
        request.callback(new DataApiHandler<AlbumPhotos>() {

            @Override
            protected void success(AlbumPhotos data, IApiResponse response) {
                if (getAdapter() != null) {
                    getAdapter().addPhotos(data, data.more, false, false);
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

        }).exec();
    }

    private View createFooterView() {
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
