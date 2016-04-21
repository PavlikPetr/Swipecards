package com.topface.topface.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.InviteVkFriendsActivity;
import com.topface.topface.ui.fragments.profile.ProfileInnerFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VkProfileFragment extends ProfileInnerFragment {
    private final static String IS_MEMER_OF_TOPFACE_TEAM = "is_memer_of_topface_team";
    private final static String IS_MEMER_OF_VK_GAMES_TEAM = "is_memer_of_vk_games_team";
    private final static String VK_USER_DATA = "vk_user_data";
    private final static String VK_COMMUNITIES_DATA = "vk_communities_data";

    private final static String VK_TOPFACE_TEAM_ID = "topface_club";
    private final static String VK_GAMES_TEAM_ID = "vkgames";
    private final static int MAX_RE_REQUEST_COUNT = 3;
    private static final String PAGE_NAME = "profile.vk";

    private VKApiUser mVkUser;
    private VKList<VKApiCommunity> mVkCommunities;
    private Boolean isTopfaceMember;
    private Boolean isVkGamesMember;

    private VKRequest mVkUserRequest;
    private VKRequest mTopfaceMemberRequest;
    private VKRequest mVkGamesMemberRequest;
    private VKRequest mCommunityRequest;

    @Bind(R.id.vkProfileAvatar)
    ImageViewRemote mAvatar;
    @Bind(R.id.vkProfileName)
    TextView mUserName;
    @Bind(R.id.vkProfileButtonEnterTopfaceTeam)
    Button mEnterTopfaceTeamButton;
    @Bind(R.id.vkProfileButtonEnterVkGamesTeam)
    Button mEnterVkGamesTeamButton;
    @Bind(R.id.vkProfileButtonTopfaceTeam)
    Button mShowTopfaceTeamButton;
    @Bind(R.id.rootControlView)
    ScrollView mRootControlView;
    @Bind(R.id.mainProgressBar)
    ProgressBar mMainProgress;

    @OnClick(R.id.vkProfileButtonTopfaceTeam)
    public void btnTopfaceTeamClick() {
        for (VKApiCommunity comunity : mVkCommunities) {
            if (comunity.screen_name.equals(VK_TOPFACE_TEAM_ID)) {
                openCommunity(comunity);
            }
        }
    }

    @OnClick(R.id.vkProfileButtonEnterTopfaceTeam)
    public void btnEnterTopfaceTeamClick() {
        joinCommunity(mEnterTopfaceTeamButton, getCommunityById(VK_TOPFACE_TEAM_ID));
    }

    @OnClick(R.id.vkProfileButtonInviteFriends)
    public void btnInviteFriendsClick() {
        startActivity(new Intent(getActivity(), InviteVkFriendsActivity.class));
    }

    @OnClick(R.id.vkProfileButtonEnterVkGamesTeam)
    public void btnEnterVkGamesTeamClick() {
        joinCommunity(mEnterVkGamesTeamButton, getCommunityById(VK_GAMES_TEAM_ID));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onLoadProfile() {
        super.onLoadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.vk_profile_fragment, null);
        ButterKnife.bind(this, root);
        if (savedInstanceState != null) {
            mVkUser = savedInstanceState.getParcelable(VK_USER_DATA);
            mVkCommunities = savedInstanceState.getParcelable(VK_COMMUNITIES_DATA);
            isTopfaceMember = savedInstanceState.getBoolean(IS_MEMER_OF_TOPFACE_TEAM);
            isVkGamesMember = savedInstanceState.getBoolean(IS_MEMER_OF_VK_GAMES_TEAM);
            setAvatar(true);
            setUserName(true);
            showButton(mShowTopfaceTeamButton, true);
            showButton(mEnterTopfaceTeamButton, !isTopfaceMember);
            showButton(mEnterVkGamesTeamButton, !isVkGamesMember);
            setProgressVisible(false);
        } else {
            initVkRequests();
            checkCurrentRequests();
            sendVkUserRequest();
            sendTopfaceMemberRequest();
            sendVkGamesMemberRequest();
            sendCommunityRequest();
        }
        return root;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(VK_USER_DATA, mVkUser);
        outState.putParcelable(VK_COMMUNITIES_DATA, mVkCommunities);
        if (isTopfaceMember != null) {
            outState.putBoolean(IS_MEMER_OF_TOPFACE_TEAM, isTopfaceMember);
        }
        if (isVkGamesMember != null) {
            outState.putBoolean(IS_MEMER_OF_VK_GAMES_TEAM, isVkGamesMember);
        }
    }

    private void setAvatar(boolean isVisible) {
        if (mAvatar != null) {
            mAvatar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            if (isVisible) {
                if (mVkUser != null) {
                    mAvatar.setRemoteSrc(mVkUser.photo_200);
                }
            }
        }
    }

    private void hideButton(Button btn) {
        initButton(btn, false, false);
    }

    private void showButton(Button btn, boolean isEnabled) {
        initButton(btn, true, isEnabled);
    }

    private void initButton(Button btn, boolean isVisible, boolean isEnabled) {
        if (btn != null) {
            btn.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            if (isVisible) {
                btn.setEnabled(isEnabled);
            }
        }
    }

    private void setUserName(boolean isVisible) {
        if (mUserName != null) {
            mUserName.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            if (isVisible) {
                if (mVkUser != null) {
                    mUserName.setText(mVkUser.first_name.concat(" ").concat(mVkUser.last_name));
                }
            }
        }
    }

    private void openCommunity(VKApiCommunity community) {
        Utils.goToUrl(getActivity(), "https://vk.com/" + community.screen_name);
    }

    private VKRequest getVkUserRequest() {
        VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "photo_200"));
        request.attempts = MAX_RE_REQUEST_COUNT;
        return request;
    }

    private VKRequest getTopfaceMemberRequest() {
        VKRequest request = VKApi.groups().isMember(
                VKParameters.from(VKApiConst.USER_ID, getUserId(), VKApiConst.GROUP_ID, VK_TOPFACE_TEAM_ID)
        );
        request.attempts = MAX_RE_REQUEST_COUNT;
        return request;
    }

    private VKRequest getVkGamesMemberRequest() {
        VKRequest request = VKApi.groups().isMember(
                VKParameters.from(VKApiConst.USER_ID, getUserId(), VKApiConst.GROUP_ID, VK_GAMES_TEAM_ID)
        );
        request.attempts = MAX_RE_REQUEST_COUNT;
        return request;
    }

    private VKRequest getCommunityRequest() {
        VKRequest request = VKApi.groups().getById(VKParameters.from("group_ids", VK_GAMES_TEAM_ID + "," + VK_TOPFACE_TEAM_ID));
        request.attempts = MAX_RE_REQUEST_COUNT;
        return request;
    }

    private void sendVkUserRequest() {
        mVkUserRequest = getVkUserRequest();
        mVkUserRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                mVkUserRequest = null;
                mVkUser = ((VKList<VKApiUser>) response.parsedModel).get(0);
                setAvatar(true);
                setUserName(true);
                checkCurrentRequests();
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                mVkUserRequest = null;
                setAvatar(false);
                setUserName(false);
                checkCurrentRequests();
            }

        });
    }

    private void sendTopfaceMemberRequest() {
        mTopfaceMemberRequest = getTopfaceMemberRequest();
        mTopfaceMemberRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                mTopfaceMemberRequest = null;
                isTopfaceMember = isMember(response);
                showButton(mEnterTopfaceTeamButton, !isTopfaceMember);
                checkCurrentRequests();
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                mTopfaceMemberRequest = null;
                isTopfaceMember = null;
                hideButton(mEnterTopfaceTeamButton);
                checkCurrentRequests();
            }

        });
    }

    private void sendVkGamesMemberRequest() {
        mVkGamesMemberRequest = getVkGamesMemberRequest();
        mVkGamesMemberRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                mVkGamesMemberRequest = null;
                isVkGamesMember = isMember(response);
                showButton(mEnterVkGamesTeamButton, !isVkGamesMember);
                checkCurrentRequests();
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                mVkGamesMemberRequest = null;
                isVkGamesMember = null;
                hideButton(mEnterVkGamesTeamButton);
                checkCurrentRequests();
            }

        });
    }

    private void sendCommunityRequest() {
        mCommunityRequest = getCommunityRequest();
        mCommunityRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                mCommunityRequest = null;
                mVkCommunities = (VKList<VKApiCommunity>) response.parsedModel;
                showButton(mShowTopfaceTeamButton, true);
                checkCurrentRequests();
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                mCommunityRequest = null;
                hideButton(mShowTopfaceTeamButton);
                checkCurrentRequests();
            }

        });
    }

    private void checkCurrentRequests() {
        setProgressVisible(mVkUserRequest != null || mTopfaceMemberRequest != null || mVkGamesMemberRequest != null || mCommunityRequest != null);
    }

    private void setProgressVisible(boolean isVisible) {
        if (mRootControlView != null) {
            mRootControlView.setVisibility(!isVisible ? View.VISIBLE : View.GONE);
        }
        if (mMainProgress != null) {
            mMainProgress.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    private void initVkRequests() {
        mVkUserRequest = getVkUserRequest();
        mTopfaceMemberRequest = getTopfaceMemberRequest();
        mVkGamesMemberRequest = getVkGamesMemberRequest();
        mCommunityRequest = getCommunityRequest();
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelRequest(mVkUserRequest);
        mVkUserRequest = null;
        cancelRequest(mTopfaceMemberRequest);
        mTopfaceMemberRequest = null;
        cancelRequest(mVkGamesMemberRequest);
        mVkGamesMemberRequest = null;
        cancelRequest(mCommunityRequest);
        mCommunityRequest = null;
    }

    private void cancelRequest(VKRequest request) {
        if (request != null) {
            request.cancel();
        }
    }

    private String getUserId() {
        return VKAccessToken.currentToken() != null ? VKAccessToken.currentToken().userId : AuthToken.getInstance().getUserSocialId();
    }

    private boolean isMember(VKResponse response) {
        return response.json.optInt("response", 0) == 1;
    }

    private String getGroupNameFromResponse(VKResponse response) {
        return (String) response.request.getMethodParameters().get("group_id");
    }

    private void joinCommunity(final Button btn, final VKApiCommunity community) {
        if (community == null) {
            return;
        }
        showButton(btn, false);
        VKApi.groups().join(VKParameters.from(VKApiConst.GROUP_ID, community.id))
                .executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onError(VKError error) {
                        super.onError(error);
                        Toast.makeText(App.getContext(), R.string.general_data_error, Toast.LENGTH_SHORT).show();
                        showButton(btn, true);
                    }

                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        Toast.makeText(App.getContext(), R.string.join_group_success, Toast.LENGTH_SHORT).show();
                        showButton(btn, false);
                        if (community.screen_name.equals(VK_GAMES_TEAM_ID)) {
                            isVkGamesMember = true;
                        } else if (community.screen_name.equals(VK_TOPFACE_TEAM_ID)) {
                            isTopfaceMember = true;
                        }
                    }
                });
    }

    private VKApiCommunity getCommunityById(String id) {
        if (mVkCommunities != null && mVkCommunities.size() > 0) {
            for (VKApiCommunity community : mVkCommunities) {
                if (community.screen_name.equals(id)) {
                    return community;
                }
            }
        }
        return null;
    }
}
