package com.topface.topface.ui.fragments;

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
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.PreloadManager;

public abstract class ViewUsersListFragment<T extends FeedUser> extends BaseFragment implements View.OnClickListener {
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


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPreloadManager != null) {
                mPreloadManager.checkConnectionType(intent.getIntExtra(ConnectionChangeReceiver.CONNECTION_TYPE, 0));
            }
        }
    };

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
        initControls(root);
        initImageSwitcher();
        initViews(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(RetryRequestReceiver.RETRY_INTENT));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
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

    private void initControls(View root) {
        ViewStub controlsStub = (ViewStub) root.findViewById(R.id.vsControls);
        Integer resId = getControlsLayoutResId();
        if (resId != null) {
            controlsStub.setLayoutResource(getControlsLayoutResId());
            controlsStub.inflate();
        }
    }

    private void initImageSwitcher() {
        ImageSwitcher imageSwitcher = getImageSwitcher();
        imageSwitcher.setOnPageChangeListener(getOnPageChangedListener());
        imageSwitcher.setOnClickListener(getOnImgeSwitcherClickListener());
        imageSwitcher.setUpdateHandler(getUnlockControlsHandler());
    }

    private ImageSwitcher getImageSwitcher() {
        if (mImageSwitcher == null) {
            mImageSwitcher = ((ImageSwitcher) getView().findViewById(R.id.glrDatingAlbum));
        }
        return mImageSwitcher;
    }

    private ImageSwitcher.ImageSwitcherAdapter getImageSwitcherAdapter() {
        ImageSwitcher.ImageSwitcherAdapter adapter = (ImageSwitcher.ImageSwitcherAdapter) getImageSwitcher().getAdapter();
        return adapter;
    }

    protected abstract Integer getControlsLayoutResId();

    protected void initViews(View root) {
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsDatingLoading);
        mRetryBtn = (ImageButton) root.findViewById(R.id.btnUpdate);
        mRetryBtn.setOnClickListener(this);
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

    protected abstract UsersList<T> createUsersList();

    protected ProgressBar getProgressBar() {
        return mProgressBar;
    }

    private void updateData(final boolean isAddition) {
        if (!mUpdateInProcess) {
            onUpdateStart(isAddition);
            getUsersListRequest().callback(new DataApiHandler<UsersList>() {

                @Override
                protected void success(UsersList data, ApiResponse response) {
                    UsersList.log("load success. Loaded " + data.size() + " users");
                    UsersList<T> usersList = getUsersList();
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
                        }
                        //Скрываем кнопку отправки повтора
                        mRetryBtn.setVisibility(View.GONE);
                    } else {
                        getProgressBar().setVisibility(View.GONE);
                    }
                    onUpdateSuccess(isAddition);
                }

                @Override
                protected UsersList parseResponse(ApiResponse response) {
                    if (getItemsClass() == SearchUser.class) {
                        return new UsersList(response, getItemsClass());
                    } else {
                        FeedListData<FeedItem> items = new FeedListData<FeedItem>(response.getJsonResult(),FeedItem.class);
                        return new UsersList(items, getItemsClass());
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
            });
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
        onShowUser();
    }

    protected abstract void unlockControls();

    protected abstract void onShowUser();

    private void showNextUser() {
        showUser(getUsersList().nextUser());
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

    @Override
    public void onClick(View view) {
        if (!CacheProfile.isLoaded()) {
            return;
        }
        switch (view.getId()) {
            case R.id.btnUpdate:
                updateData(false);
                mRetryBtn.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }
}
