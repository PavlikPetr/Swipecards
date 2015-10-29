package com.topface.topface.ui;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.actionbar.ActionBarTitleSetterDelegate;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class InviteVkFriendsActivity extends BaseFragmentActivity {
    private final static int ITEMS_COUNT_BEFORE_END = 5;
    private final static int MAX_RE_REQUEST_COUNT = 3;

    private final static String VK_FRIENDS_LIST_DATA = "vk_friends_list_data";
    private final static String VK_FRIENDS_BUTTONS_STATE_DATA = "vk_friends_buttons_state_data";
    private final static String VK_FRIENDS_GETTING_EXTRA = "vk_friends_getting_extra";

    private final static String USER_ID_VK_PARAM = "user_id";

    private VKRequest mFriendsRequest;
    private int mAvailableFriendsCount = 0;
    private VKFriendsAdapter mAdapter;
    private boolean mIsGettingExtraFriends = false;
    private View mFooterView;

    @Bind(R.id.vkFriendsList)
    ListView mListView;
    @Bind(R.id.mainProgressBar)
    ProgressBar mProgress;

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setMainProgressVisibility(true);
        new ActionBarTitleSetterDelegate(getSupportActionBar()).setActionBarTitles(R.string.vk_profile_invite_friends_title, null);
        mFooterView = getLayoutInflater().inflate(R.layout.gridview_footer_progress_bar, null);
        mListView.addFooterView(mFooterView);
        mAdapter = new VKFriendsAdapter(new ArrayList<VKApiUser>());
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int totalItemsWithoutHeadersAndFooters = totalItemCount - mListView.getHeaderViewsCount() - mListView.getFooterViewsCount();
                if (totalItemsWithoutHeadersAndFooters - (firstVisibleItem + visibleItemCount) <= ITEMS_COUNT_BEFORE_END
                        && !mIsGettingExtraFriends
                        && (totalItemsWithoutHeadersAndFooters == 0 || totalItemsWithoutHeadersAndFooters < mAvailableFriendsCount)) {
                    mIsGettingExtraFriends = true;
                    loadNewPackData();
                }
            }
        });
        mAdapter.setInviteClickListener(new VKFriendsAdapter.InViteClickListener() {
            @Override
            public void onClick(int id) {
                VKRequest inviteRequest = new VKRequest("apps.sendRequest");
                inviteRequest.addExtraParameter(USER_ID_VK_PARAM, id);
                inviteRequest.attempts = 0;
                inviteRequest.executeWithListener(mVkInviteListener);
            }
        });
    }

    private void loadNewPackData() {
        mFriendsRequest = getVkFriendsRequest(mAdapter.getCount());
        mFriendsRequest.executeWithListener(mFriendsListener);
    }

    private VKRequest.VKRequestListener mVkInviteListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            if (mAdapter != null) {
                mAdapter.setButtonState((Integer) response.request.getPreparedParameters().get(USER_ID_VK_PARAM), false);
            }
            Toast.makeText(App.getContext(), R.string.vk_friends_success_invite, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(VKError error) {
            if (mAdapter != null) {
                mAdapter.setButtonState((Integer) error.request.getPreparedParameters().get(USER_ID_VK_PARAM), true);
            }
            Toast.makeText(App.getContext(), R.string.general_data_error, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected int getContentLayout() {
        return R.layout.ac_invite_vk_friends;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFriendsRequest != null) {
            mFriendsRequest.cancel();
        }
    }

    private VKRequest getVkFriendsRequest(int offset) {
        VKRequest friendsRequest = new VKRequest("apps.getFriendsList");
        friendsRequest.attempts = MAX_RE_REQUEST_COUNT;
        friendsRequest.addExtraParameters(VKParameters.from(VKApiConst.FIELDS, "photo_200", VKApiConst.COUNT, 30, VKApiConst.EXTENDED, 1));
        if (offset > 0) {
            friendsRequest.addExtraParameter(VKApiConst.OFFSET, mAdapter.getCount());
        }
        return friendsRequest;
    }

    private VKRequest.VKRequestListener mFriendsListener = new VKRequest.VKRequestListener() {
        @Override
        public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
            super.onProgress(progressType, bytesLoaded, bytesTotal);
            showProgress(true);
        }

        @Override
        public void onComplete(VKResponse response) {
            super.onComplete(response);
            showProgress(false);
            try {
                JSONObject responseJSON = response.json.getJSONObject("response");
                setAdapterData(JsonUtils.fromJson(responseJSON.getJSONArray("items").toString(), new TypeToken<List<VKApiUser>>() {
                }));
                mAvailableFriendsCount = responseJSON.getInt("count");
            } catch (JSONException e) {
                Debug.error(e);
            }
            mIsGettingExtraFriends = false;
        }

        @Override
        public void onError(VKError error) {
            super.onError(error);
            showProgress(false);
            mIsGettingExtraFriends = false;
        }
    };

    private static class VKFriendsAdapter extends BaseAdapter {

        List<VKApiUser> mFriendsList;
        private InViteClickListener mInViteClickListener;
        private ConcurrentHashMap<Integer, Boolean> mButtonsStateList = new ConcurrentHashMap<>();

        VKFriendsAdapter(List<VKApiUser> friends) {
            mFriendsList = friends;
            fillButtonsState();
        }

        VKFriendsAdapter(List<VKApiUser> friends, ConcurrentHashMap<Integer, Boolean> buttonsStateList) {
            mFriendsList = friends;
            mButtonsStateList = buttonsStateList;
        }

        private void fillButtonsState() {
            for (VKApiUser user : mFriendsList) {
                mButtonsStateList.put(user.getId(), true);
            }
        }

        private boolean isButtonEnabled(int userId) {
            if (mButtonsStateList != null && mButtonsStateList.containsKey(userId)) {
                return mButtonsStateList.get(userId);
            }
            return true;
        }

        public void setButtonState(int userId, boolean isEnabled) {
            if (mButtonsStateList != null && mButtonsStateList.containsKey(userId)) {
                mButtonsStateList.replace(userId, isEnabled);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return mFriendsList != null ? mFriendsList.size() : 0;
        }

        @Override
        public VKApiUser getItem(int position) {
            return mFriendsList != null ? mFriendsList.get(position) : null;
        }

        public void addFriends(List<VKApiUser> friends) {
            mFriendsList.addAll(friends);
            fillButtonsState();
        }

        public List<VKApiUser> getAllData() {
            return mFriendsList;
        }

        public ConcurrentHashMap<Integer, Boolean> getButtonsStateList() {
            return mButtonsStateList;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.invite_vk_friends_cell, parent, false);
                holder = new ViewHolder();
                holder.photo = (ImageViewRemote) convertView.findViewById(R.id.vkFriendAvatar);
                holder.name = (TextView) convertView.findViewById(R.id.vkFriendName);
                holder.invite = (Button) convertView.findViewById(R.id.vkFrienInviteButton);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (holder != null) {
                initHolder(holder, getItem(position));
            }
            return convertView;
        }

        private void initHolder(ViewHolder holder, final VKApiUser friend) {
            holder.photo.setRemoteSrc(friend.photo_200);
            holder.name.setText(friend.first_name.concat(" ").concat(friend.last_name));
            boolean isEnabled = isButtonEnabled(friend.getId());
            holder.invite.setEnabled(isEnabled);
            holder.invite.setText(isEnabled ? R.string.invite_friend_button : R.string.invitation_sended_button);
            holder.invite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setButtonState(friend.getId(), false);
                    if (mInViteClickListener != null) {
                        mInViteClickListener.onClick(friend.getId());
                    }
                }
            });
        }

        public interface InViteClickListener {
            void onClick(int id);
        }

        public void setInviteClickListener(InViteClickListener inviteClickListener) {
            mInViteClickListener = inviteClickListener;
        }

        class ViewHolder {
            ImageViewRemote photo;
            TextView name;
            Button invite;
        }
    }

    private void setMainProgressVisibility(boolean isVisible) {
        mProgress.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        mListView.setVisibility(!isVisible ? View.VISIBLE : View.GONE);
    }

    private void setAdapterData(List<VKApiUser> friends) {
        mAdapter.addFriends(friends);
        mAdapter.notifyDataSetChanged();
        setMainProgressVisibility(false);
    }

    private void showProgress(boolean visibility) {
        if (mAdapter == null || mAdapter.getCount() == 0 && mFooterView != null) {
            mFooterView.setVisibility(visibility ? View.VISIBLE : View.GONE);
        }
    }
}
