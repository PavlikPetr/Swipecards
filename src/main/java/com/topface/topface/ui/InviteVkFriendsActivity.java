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

import butterknife.Bind;
import butterknife.ButterKnife;

public class InviteVkFriendsActivity extends BaseFragmentActivity {
    private final static int ITEMS_COUNT_BEFORE_END = 5;

    private VKRequest mFriendsRequest;
    private int mAvailableFriendsCount;
    private VKFriendsAdapter mAdapter;

    @Bind(R.id.vkFriendsList)
    ListView mListView;
    @Bind(R.id.mainProgressBar)
    ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setMainProgressVisibility(true);
        new ActionBarTitleSetterDelegate(getSupportActionBar()).setActionBarTitles(R.string.vk_profile_button_invite_friends, null);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount - (firstVisibleItem + visibleItemCount) <= ITEMS_COUNT_BEFORE_END && totalItemCount > 0) {
                    loadNewPackData();
                }
            }
        });
        mAdapter = new VKFriendsAdapter(new ArrayList<VKApiUser>());
        mListView.setAdapter(mAdapter);
        mAdapter.setInviteClickListener(new VKFriendsAdapter.InViteClickListener() {
            @Override
            public void onClick(int id) {
                Debug.error("INVITE_FRIENDS_CLICK " + id);
                VKRequest inviteRequest = new VKRequest("apps.sendRequest");
                inviteRequest.addExtraParameter("user_id", id);
                inviteRequest.executeWithListener(mVkInviteListener);
            }
        });
        mFriendsRequest = getVkFriendsRequest(0);
        mFriendsRequest.executeWithListener(mFriendsListener);
    }

    private void loadNewPackData() {
        mFriendsRequest = getVkFriendsRequest(mAdapter.getCount());
        mFriendsRequest.executeWithListener(mFriendsListener);
    }

    private VKRequest.VKRequestListener mVkInviteListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            Debug.error("INVITE_FRIENDS_CLICK mVkInviteListener onComplete");
            Toast.makeText(App.getContext(), R.string.invite_friends_title, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
            Debug.error("INVITE_FRIENDS_CLICK mVkInviteListener attemptFailed");
            Toast.makeText(App.getContext(), R.string.general_error, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(VKError error) {
            Debug.error("INVITE_FRIENDS_CLICK mVkInviteListener onError " + error);
            Toast.makeText(App.getContext(), R.string.general_error, Toast.LENGTH_SHORT).show();
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
        friendsRequest.attempts = 0;
        friendsRequest.addExtraParameters(VKParameters.from(VKApiConst.FIELDS, "photo_200", VKApiConst.COUNT, 30, VKApiConst.EXTENDED, 1));
        if (offset > 0) {
            friendsRequest.addExtraParameter(VKApiConst.OFFSET, mAdapter.getCount());
        }
        return friendsRequest;
    }

    private VKRequest.VKRequestListener mFriendsListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            super.onComplete(response);
            try {
                JSONObject responseJSON = response.json.getJSONObject("response");
                setAdapterData(JsonUtils.fromJson(responseJSON.getJSONArray("items").toString(), new TypeToken<List<VKApiUser>>() {
                }));
                mAvailableFriendsCount = responseJSON.getInt("count");
            } catch (JSONException e) {
                Debug.error(e);
            }
//            mIsGettingExtraFriends = false;
        }

        @Override
        public void onError(VKError error) {
            super.onError(error);
//            mIsGettingExtraFriends = false;
        }
    };

    private static class VKFriendsAdapter extends BaseAdapter {

        List<VKApiUser> mFriendsList;
        private InViteClickListener mInViteClickListener;

        VKFriendsAdapter(List<VKApiUser> friends) {
            mFriendsList = friends;
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
        }

        public List<VKApiUser> getAllData() {
            return mFriendsList;
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
            holder.invite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mInViteClickListener != null) {
                        mInViteClickListener.onClick(friend.id);
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
}
