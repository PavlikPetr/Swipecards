package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.RetryRequestReceiver;
import com.topface.topface.data.*;
import com.topface.topface.data.search.OnUsersListEventsListener;
import com.topface.topface.data.search.SearchUser;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.utils.*;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ViewUsersListFragment<T extends FeedUser> extends BaseFragment {
    public static final int LIMIT = 40;
    public static final int PHOTOS_LIMIT = 5;
    public static final int DEFAULT_PRELOAD_ALBUM_RANGE = 2;

    private ProgressBar mProgressBar;
    private ImageSwitcher mImageSwitcher;
    private ImageButton mRetryBtn;

    private UsersList<T> mUsersList;
    private PreloadManager<SearchUser> mPreloadManager;

    private boolean mUpdateInProcess;
    private int mLoadedCount;
    private boolean mNeedMore;
    private boolean mCanSendAlbumReq = true;
    private T mCurrentUser;
    private RateController mRateController;
    private AtomicBoolean mFragmentPaused = new AtomicBoolean(false);

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPreloadManager != null) {
                mPreloadManager.checkConnectionType(intent.getIntExtra(ConnectionChangeReceiver.CONNECTION_TYPE, 0));
            }
        }
    };
    private FeedItem mLastFeedItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreloadManager = new PreloadManager<SearchUser>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_view_users, null);

        getActionBar(root);
        initActionBar(root);
        inflateTopPanel(root);
        inflateControls(root);
        initImageSwitcher(root);
        initViews(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(RetryRequestReceiver.RETRY_INTENT));
        mFragmentPaused.set(false);
        if (mCurrentUser == null && !getUsersList().isEmpty()) {
            showUser(getUsersList().getCurrentUser());
            unlockControls();
            mRetryBtn.setVisibility(View.GONE);
            getProgressBar().setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        mFragmentPaused.set(true);
    }

    @Override
    protected void onLoadProfile() {
        super.onLoadProfile();
        if (mUsersList == null) {
            UsersList usersList = getUsersList();
            usersList.setOnEmptyListListener(getOnUsersListEventsListener());
        }
        //Показываем последнего пользователя
        if (mCurrentUser == null) {
            T currentUser = getUsersList().getCurrentUser();
            if (currentUser != null) {
                showUser(currentUser);
            } else {
                showNextUser();
            }
        }
    }

    private void inflateTopPanel(View root) {
        Integer resId = getTopPanelLayoutResId();
        if (resId != null) {
            ViewStub topPanelStub = ((ViewStub) root.findViewById(R.id.vsTopPanelContainer));
            topPanelStub.setLayoutResource(R.layout.controls_closing_top_panel);
            final View view = topPanelStub.inflate();
            initTopPanel(view);

        }
    }

    private void inflateControls(View root) {
        Integer resId = getControlsLayoutResId();
        if (resId != null) {
            ViewStub controlsStub = (ViewStub) root.findViewById(R.id.vsControls);
            controlsStub.setLayoutResource(getControlsLayoutResId());
            initControls(controlsStub.inflate());
        }
    }

    protected abstract void initTopPanel(View topPanelView);

    protected abstract void initControls(View controlsView);

    private void initActionBar(View view) {
        ActionBar actionBar = getActionBar(view);
        refreshActionBarTitles(view);
        final Activity activity = getActivity();
        if (activity instanceof NavigationActivity) {
            actionBar.showHomeButton((NavigationActivity) activity);
        }
        initActionBarControls(actionBar);
    }

    protected abstract void initActionBarControls(ActionBar actionbar);

    protected void refreshActionBarTitles(View view) {
        getActionBar(view).setTitleText(getTitle());
        getActionBar(view).setSubTitleText(getSubtitle());
    }

    protected abstract String getTitle();

    protected abstract String getSubtitle();

    private void initImageSwitcher(View root) {
        ImageSwitcher imageSwitcher = getImageSwitcher(root);
        imageSwitcher.setOnPageChangeListener(getOnPageChangedListener());
        imageSwitcher.setOnClickListener(getOnImgeSwitcherClickListener());
        imageSwitcher.setUpdateHandler(getUnlockControlsHandler());
    }

    private ImageSwitcher getImageSwitcher() throws NullPointerException {
        View view = getView();
        if (view != null) {
            return getImageSwitcher(view);
        } else {
            Debug.error("ERROR: root view for getting ImageSwitcher is NULL");
            throw new NullPointerException();
        }
    }

    private ImageSwitcher getImageSwitcher(View view) {
        if (mImageSwitcher == null) {
            mImageSwitcher = ((ImageSwitcher) view.findViewById(R.id.glrDatingAlbum));
        }
        return mImageSwitcher;
    }

    private ImageSwitcher.ImageSwitcherAdapter getImageSwitcherAdapter() {
        return (ImageSwitcher.ImageSwitcherAdapter) getImageSwitcher().getAdapter();
    }

    protected abstract Integer getControlsLayoutResId();

    protected void initViews(View root) {
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsDatingLoading);
        mRetryBtn = (ImageButton) root.findViewById(R.id.btnUpdate);
        mRetryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CacheProfile.isLoaded()) {
                    updateData(false);
                    mRetryBtn.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private ViewPager.OnPageChangeListener getOnPageChangedListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position + DEFAULT_PRELOAD_ALBUM_RANGE == (mLoadedCount - 1)) {
                    if (mNeedMore && mCanSendAlbumReq) {
                        mCanSendAlbumReq = false;
                        sendAlbumRequest();
                    }
                }
                ViewUsersListFragment.this.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        };
    }

    protected abstract void onPageSelected(int position);

    public View.OnClickListener getOnImgeSwitcherClickListener() {
        return null;
    }

    public Handler getUnlockControlsHandler() {
        return null;
    }

    private UsersList<T> getUsersList() {
        if (mUsersList == null) {
            mUsersList = createUsersList();
        }
        return mUsersList;
    }

    protected void clearUsersList() {
        getUsersList().clear();
    }

    protected T getCurrentUser() {
        return mCurrentUser;
    }

    protected String getLastFeedId() {
        if (mLastFeedItem != null)
            return mLastFeedItem.id;
        else
            return null;
    }

    protected abstract UsersList<T> createUsersList();

    protected ProgressBar getProgressBar() {
        return mProgressBar;
    }

    protected void updateData(final boolean isAddition) {
        if (!mUpdateInProcess) {
            onUpdateStart(isAddition);
            ApiRequest request = getUsersListRequest();
            request.callback(new DataApiHandler<UsersList>() {
                private boolean more = true;

                @Override
                protected void success(UsersList data, ApiResponse response) {
                    UsersList.log("load success. Loaded " + data.size() + " users");
                    UsersList<T> usersList = getUsersList();
                    if (mFragmentPaused.get()) {
                        usersList.addAndUpdateSignature(data);
                        mCurrentUser = null;
                        return;
                    }
                    if (data.size() != 0) {
                        getImageSwitcher().setVisibility(View.VISIBLE);
                        usersList.addAndUpdateSignature(data);
                        //если список был пуст, то просто показываем нового пользователя
                        T currentUser = usersList.getCurrentUser();
                        //NOTE: Если в поиске никого нет, то мы показываем следующего юзера
                        //Но нужно учитывать, что такое происходит при смене фильтра не через приложение,
                        //Когда чистится поиск, если фильтр поменялся удаленно,
                        //из-за чего происходит автоматический переход на следующего юзера
                        //От этого эффекта можно избавиться, если заменить на такое условие:
                        //<code>if (!isAddition && mCurrentUser != currentUser || mCurrentUser == null)</code>
                        //Но возникает странный эффект, когда в поиске написано одно, а у юзера другое,
                        //В связи с чем, все работает так как работает
                        if (currentUser != null && mCurrentUser != currentUser) {
                            showUser(currentUser);
                            unlockControls();
                        } else {
                            if (!more) showUser(null);
                        }
                        //Скрываем кнопку отправки повтора
                        mRetryBtn.setVisibility(View.GONE);
                    } else {
                        getProgressBar().setVisibility(View.GONE);
                        if (!more) showUser(null);
                    }
                    onUpdateSuccess(isAddition);
                }

                @SuppressWarnings("unchecked")
                @Override
                protected UsersList parseResponse(ApiResponse response) {
                    Class itemsClass = getItemsClass();
                    if (itemsClass == SearchUser.class) {
                        return new UsersList<SearchUser>(response, itemsClass);
                    } else {
                        if (getFeedUserContainerClass() != null) {
                            FeedListData<FeedItem> items = new FeedListData<FeedItem>(response.getJsonResult(), getFeedUserContainerClass());
                            more = items.more;
                            mLastFeedItem = items.items.isEmpty() ? null : items.items.get(items.items.size() - 1);
                            return new UsersList<FeedUser>(items, itemsClass);
                        } else {
                            return new UsersList<FeedUser>(itemsClass);
                        }
                    }
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        UsersList.log("load error: " + response.message);
                        Toast.makeText(activity, App.getContext().getString(R.string.general_data_error),
                                Toast.LENGTH_SHORT).show();
                    }
                    unlockControls();
                    onUpdateFail(isAddition);
                }

                @Override
                public void always(ApiResponse response) {
                    super.always(response);
                    mUpdateInProcess = false;
                }
            }).exec();
        }
    }

    private void sendAlbumRequest() {
        final UsersList<T> usersList = getUsersList();
        final Photos photos = getImageSwitcherAdapter().getData();

        if (usersList == null)
            return;
        if ((mLoadedCount - 1) >= photos.size())
            return;
        if (photos.get(mLoadedCount - 1) == null)
            return;

        int position = photos.get(mLoadedCount - 1).getPosition() + 1;
        final T currentUser = usersList.getCurrentUser();
        if (currentUser != null) {
            AlbumRequest request = new AlbumRequest(getActivity(), currentUser.id, PHOTOS_LIMIT,
                    position, AlbumRequest.MODE_SEARCH);
            final int uid = currentUser.id;
            request.callback(new ApiHandler() {
                @Override
                public void success(ApiResponse response) {
                    if (uid == usersList.getCurrentUser().id) {
                        Photos newPhotos = Photos.parse(response.jsonResult.optJSONArray("items"));
                        mNeedMore = response.jsonResult.optBoolean("more");
                        int i = 0;
                        for (Photo photo : newPhotos) {
                            if (mLoadedCount + i < photos.size()) {
                                photos.set(mLoadedCount + i, photo);
                                i++;
                            }
                        }

                        if (mImageSwitcher != null && mImageSwitcher.getAdapter() != null) {
                            mImageSwitcher.getAdapter().notifyDataSetChanged();
                        }
                    }
                    mCanSendAlbumReq = true;
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                    mCanSendAlbumReq = true;
                }
            }).exec();
        }
    }

    protected abstract ApiRequest getUsersListRequest();

    public abstract Class getItemsClass();

    @Override
    protected void onUpdateStart(boolean isPushUpdating) {
        lockControls();
        mUpdateInProcess = true;
        if (!isPushUpdating) {
            getProgressBar().setVisibility(View.VISIBLE);
            getImageSwitcher().setVisibility(View.GONE);
        }
        UsersList.log("Update start: " + (isPushUpdating ? "addition" : "replace"));
    }

    @Override
    protected void onUpdateSuccess(boolean isPushUpdating) {
        if (!isPushUpdating) {
            getProgressBar().setVisibility(View.GONE);
        }
    }

    @Override
    protected void onUpdateFail(boolean isPushUpdating) {
        if (!isPushUpdating) {
            getProgressBar().setVisibility(View.GONE);
            mRetryBtn.setVisibility(View.VISIBLE);
        }
    }

    private void setUserPhotos(T currUser) {
        // photos
        mLoadedCount = currUser.photos.getRealPhotosCount();
        mNeedMore = currUser.photosCount > mLoadedCount;
        int rest = currUser.photosCount - currUser.photos.size();

        for (int i = 0; i < rest; i++) {
            currUser.photos.add(new Photo());
        }
    }

    private void fillUserInfo(T currUser) {
        // User Info
        mCurrentUser = currUser;
        if (currUser == null || !isAdded()) {
            return;
        }
        lockControls();
        setUserPhotos(currUser);
        getImageSwitcher().setData(currUser.photos);
        getImageSwitcher().setCurrentItem(0, true);
    }

    protected abstract void lockControls();

    private void showUser(T user) {
        if (user != null) {
            fillUserInfo(user);
            unlockControls();
        }
        mPreloadManager.preloadPhoto(getUsersList());
        if (user == null) {
            onUsersProcessed();
        }
        onShowUser();
    }

    protected abstract void unlockControls();

    protected abstract void onShowUser();

    protected void showNextUser() {
        if (getUsersList().isEnded()) {
            updateData(true);
            return;
        }
        T nextUser = getUsersList().nextUser();
        showUser(nextUser);
    }

    public OnUsersListEventsListener getOnUsersListEventsListener() {
        return new OnUsersListEventsListener() {
            @Override
            public void onEmptyList(UsersList usersList) {
                updateData(false);
            }

            @Override
            public void onPreload(UsersList usersList) {
                updateData(true);
            }

        };
    }

    public Class getFeedUserContainerClass() {
        return null;
    }

    public Integer getTopPanelLayoutResId() {
        return null;
    }

    protected RateController getRateController() {
        if (mRateController == null) {
            mRateController = new RateController(getActivity());
            mRateController.setOnRateControllerListener(getOnRateListener());
        }
        return mRateController;
    }

    public RateController.OnRateControllerListener getOnRateListener() {
        return null;
    }

    protected void onUsersProcessed() {
    }
}
