package com.topface.topface.ui;

import android.os.Bundle;
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
import com.topface.topface.statistics.InvitesStatistics;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.FlurryManager;
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

import static com.topface.topface.statistics.InvitesStatistics.PLC_VK_INVITES;

public class InviteVkFriendsActivity extends BaseFragmentActivity {
    private final static int ITEMS_COUNT_BEFORE_END = 5;
    private final static int MAX_RE_REQUEST_COUNT = 3;

    private final static String VK_FRIENDS_LIST_DATA = "vk_friends_list_data";
    private final static String VK_FRIENDS_BUTTONS_STATE_DATA = "vk_friends_buttons_state_data";
    private final static String VK_FRIENDS_GETTING_EXTRA = "vk_friends_getting_extra";
    private final static String VK_FRIENDS_LIST_SCROLL_POSITION = "vk_friends_list_scroll_position";
    private final static String VK_FRIENDS_AVAILABLE_COUNT = "vk_friends_available_count";
    private final static String VK_OFFSET_VALUE = "vk_offset_value";
    private static final String PAGE_NAME = "vkinvites";
    private static final int VK_ERROR_ACCESS_DENIED = 15;

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
    @Bind(R.id.noOneFriendLoaded)
    TextView mNoFriendsTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        new ActionBarTitleSetterDelegate(getSupportActionBar()).setActionBarTitles(R.string.vk_profile_invite_friends_title, null);
        mFooterView = getLayoutInflater().inflate(R.layout.gridview_footer_progress_bar, null);
        mListView.addFooterView(mFooterView);
        int position = 0;
        if (savedInstanceState != null) {
            mAdapter = new VKFriendsAdapter(parseFriendsList(savedInstanceState.getString(VK_FRIENDS_LIST_DATA)), (ConcurrentHashMap<Integer, Boolean>) savedInstanceState.getSerializable(VK_FRIENDS_BUTTONS_STATE_DATA));
            mIsGettingExtraFriends = savedInstanceState.getBoolean(VK_FRIENDS_GETTING_EXTRA);
            position = savedInstanceState.getInt(VK_FRIENDS_LIST_SCROLL_POSITION);
            mAvailableFriendsCount = savedInstanceState.getInt(VK_FRIENDS_AVAILABLE_COUNT);
            int offsetValue = savedInstanceState.getInt(VK_OFFSET_VALUE, 0);
            if (offsetValue != 0) {
                mAdapter.setOffsetValue(offsetValue);
            }

        } else {
            mAdapter = new VKFriendsAdapter(new ArrayList<VKApiUser>());
        }
        mListView.setAdapter(mAdapter);
        setMainProgressVisibility(mAdapter == null || mAdapter.getCount() == 0);
        showProgress(false);
        mListView.scrollTo(0, position);
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
                InvitesStatistics.sendInviteBtnClickAction(PLC_VK_INVITES);
                VKRequest inviteRequest = new VKRequest("apps.sendRequest");
                inviteRequest.addExtraParameter(USER_ID_VK_PARAM, id);
                inviteRequest.addExtraParameter("type", "invite");
                inviteRequest.attempts = 0;
                inviteRequest.executeWithListener(mVkInviteListener);
            }
        });
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    private void loadNewPackData() {
        showProgress(true);
        mFriendsRequest = getVkFriendsRequest(mAdapter.getOffsetValue());
        mFriendsRequest.executeWithListener(mFriendsListener);
    }

    private VKRequest.VKRequestListener mVkInviteListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            InvitesStatistics.sendSuccessInviteResponseAction(PLC_VK_INVITES);
            FlurryManager.getInstance().sendInviteEvent(FlurryManager.VK_INVITES, 1);
            if (mAdapter != null) {
                mAdapter.setButtonState((Integer) response.request.getPreparedParameters().get(USER_ID_VK_PARAM), false);
            }
            Toast.makeText(App.getContext(), R.string.vk_friends_success_invite, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(VKError error) {
            InvitesStatistics.sendFailedInviteResponseAction(PLC_VK_INVITES, error != null ? error.errorCode : null);
            int userId = getUSerIDFromVkError(error);
            if (userId != 0) {
                mAdapter.setButtonState(userId, true);
            }
            if (error != null && error.errorCode == VKError.VK_API_ERROR && error.apiError.errorCode == VK_ERROR_ACCESS_DENIED && mAdapter != null && userId != 0) {
                mAvailableFriendsCount = mAvailableFriendsCount - (mAdapter.removeUserById(userId) ? 1 : 0);
            }
            if (error != null && error.errorCode != VKError.VK_CANCELED) {
                Toast.makeText(App.getContext(), error.apiError.errorCode != VK_ERROR_ACCESS_DENIED ? R.string.general_data_error : R.string.vk_profile_invite_friends_error, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private int getUSerIDFromVkError(VKError error) {
        if (error != null && error.request != null && error.request.getMethodParameters() != null && error.request.getMethodParameters().get(USER_ID_VK_PARAM) != null) {
            return (int) error.request.getMethodParameters().get(USER_ID_VK_PARAM);
        }
        return 0;
    }

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
        outState.putString(VK_FRIENDS_LIST_DATA, packFriendsList());
        outState.putSerializable(VK_FRIENDS_BUTTONS_STATE_DATA, mAdapter != null ? mAdapter.getButtonsStateList() : new ConcurrentHashMap<Integer, Boolean>());
        outState.putBoolean(VK_FRIENDS_GETTING_EXTRA, mIsGettingExtraFriends);
        outState.putInt(VK_FRIENDS_LIST_SCROLL_POSITION, mListView != null ? mListView.getScrollY() : 0);
        outState.putInt(VK_FRIENDS_AVAILABLE_COUNT, mAvailableFriendsCount);
        outState.putInt(VK_OFFSET_VALUE, mAdapter != null ? mAdapter.getOffsetValue() : 0);
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
            friendsRequest.addExtraParameter(VKApiConst.OFFSET, offset);
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
                setAdapterData(parseFriendsList(responseJSON.getJSONArray("items").toString()));
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

    private List<VKApiUser> parseFriendsList(String resp) {
        return JsonUtils.fromJson(resp, new TypeToken<List<VKApiUser>>() {
        });
    }

    private String packFriendsList() {
        return packFriendsList(mAdapter != null ? mAdapter.getAllData() : new ArrayList<VKApiUser>());
    }

    private String packFriendsList(List<VKApiUser> friends) {
        return JsonUtils.toJson(friends);
    }

    private static class VKFriendsAdapter extends BaseAdapter {

        List<VKApiUser> mFriendsList;
        private InViteClickListener mInViteClickListener;
        private ConcurrentHashMap<Integer, Boolean> mButtonsStateList = new ConcurrentHashMap<>();
        private int mOffset;
        private int mDeletedCount;

        VKFriendsAdapter(List<VKApiUser> friends) {
            this(friends, null);
        }

        VKFriendsAdapter(List<VKApiUser> friends, ConcurrentHashMap<Integer, Boolean> buttonsStateList) {
            mFriendsList = friends != null ? friends : new ArrayList<VKApiUser>();
            setOffsetValue(mFriendsList.size());
            if (buttonsStateList != null) {
                mButtonsStateList = buttonsStateList;
            } else {
                fillButtonsState();
            }
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
            setOffsetValue(getOffsetValue() + friends.size());
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

        public int getOffsetValue() {
            return mOffset;
        }

        public void setOffsetValue(int count) {
            mOffset = count;
        }

        public boolean removeUserById(int id) {
            for (int i = 0; i < mFriendsList.size(); i++) {
                if (mFriendsList.get(i).getId() == id) {
                    mFriendsList.remove(i);
                    mDeletedCount++;
                    return true;
                }
            }
            return false;
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
        setNoFriendsTitleVisibility(!isVisible && mAdapter != null && mAdapter.getCount() == 0);
    }

    private void setAdapterData(List<VKApiUser> friends) {
        mAdapter.addFriends(friends);
        mAdapter.notifyDataSetChanged();
        setMainProgressVisibility(false);
    }

    private void showProgress(boolean visibility) {
        if (mFooterView != null) {
            mFooterView.setVisibility(visibility && mAdapter != null && mAdapter.getCount() != 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void setNoFriendsTitleVisibility(boolean isVisible) {
        mNoFriendsTitle.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        mListView.setVisibility(!isVisible ? View.VISIBLE : View.GONE);
    }
}
