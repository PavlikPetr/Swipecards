package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.ModerationResponse;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ModerationPunish;
import com.topface.topface.requests.ModerationUnban;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class EditorProfileActionsFragment extends BaseFragment implements View.OnClickListener {
    public static final String USERID = "USERID";
    public static final String PROFILE_RESPONSE = "PROFILE_RESPONSE";
    private static final String FULL_INFO_VISIBLE = "full_info_visible";
    private static final String SCROLL_VIEW_LIST = "scroll_view_list";
    private int mUserId;
    private String mResponse = null;
    private User mUser = null;
    private View mFullInfo = null;
    private View mLocker = null;
    private boolean mIsFullInfoVisible;
    private ScrollView mScroll;

    public class BanAction {
        public static final String SPAM_MSG = "TWO_MONTHS_SPAM";

        public static final String SPAM_PHOTO = "TWO_MONTHS_PHOTO_SPAM";
        public static final String FAKE = "ONE_WEEK_PHOTO_FAKE";
        public static final String CENSOR = "TWO_DAYS_ABUSE";
        public static final String PORN = "TWO_MONTHS_PHOTO_PORNO";
        public static final String PORN_ALBUM = "TWO_MONTHS_PHOTO_PORNO_ALBUM";
        public static final String DEL_PHOTO = "REMOVE_PHOTO";
        public static final String DEL_PHOTO_ALL = "REMOVE_ALL_PHOTO";
        public static final String DEL_STATUS = "REMOVE_SHORT";
        public static final String DEL_ABOUT = "REMOVE_ABOUT";
        public static final String CHANGE_GENDER = "SWITCH_SEX";

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_editor_profile_actions, container, false);
        Bundle args = getArguments();
        mIsFullInfoVisible = false;
        int scroll = 0;
        if (savedInstanceState != null) {
            mIsFullInfoVisible = savedInstanceState.getBoolean(FULL_INFO_VISIBLE);
            scroll = savedInstanceState.getInt(SCROLL_VIEW_LIST);
        }
        mScroll = (ScrollView) root.findViewById(R.id.editor_profile_scroll);
        mUserId = args.getInt(USERID, -1);

        mResponse = args.getString(PROFILE_RESPONSE);

        if (!TextUtils.isEmpty(mResponse)) {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(mResponse);
            } catch (JSONException e) {
                Debug.error(e);
            }
            mUser = new User(mUserId, jsonResponse);
        }
        initViews(root);

        if (mUserId == -1) {
            getActivity().finish();
        }
        mScroll.scrollTo(0, scroll);
        return root;
    }

    private void initViews(View root) {
        mLocker = root.findViewById(R.id.editor_ban_locker);
        showView(mLocker, false);
        root.setVisibility(View.VISIBLE);
        if (mUser != null) {
            setInfoText(root, R.id.editor_ban_profile_name, mUser.firstName);
            setInfoText(root, R.id.editor_ban_profile_id, Integer.toString(mUserId));
            setInfoText(root, R.id.editor_ban_profile_banned, String.valueOf(mUser.banned));

            initActionButtons(root);

            if (mUser.socialInfo != null) {
                setInfoText(root, R.id.editor_ban_profile_social, mUser.socialInfo.link);
                setInfoText(root, R.id.editor_ban_profile_social_id, String.valueOf(mUser.socialInfo.id));
            } else {
                showView(root, R.id.editor_ban_profile_social, false);
                showView(root, R.id.editor_ban_profile_social_id, false);
            }
            if (!mUser.banned) {
                showView(root, R.id.editor_ban_unban_user, false);
            }
            initFullInfo(root);
        }
    }

    private void initActionButtons(View root) {
        root.findViewById(R.id.editor_ban_spam_photo).setOnClickListener(this);
        root.findViewById(R.id.editor_ban_spam_msg).setOnClickListener(this);
        root.findViewById(R.id.editor_ban_porn_album).setOnClickListener(this);
        root.findViewById(R.id.editor_ban_censor).setOnClickListener(this);
        root.findViewById(R.id.editor_ban_change_gender).setOnClickListener(this);
        root.findViewById(R.id.editor_ban_del_about).setOnClickListener(this);
        root.findViewById(R.id.editor_ban_del_photo).setOnClickListener(this);
        root.findViewById(R.id.editor_ban_del_photo_all).setOnClickListener(this);
        root.findViewById(R.id.editor_ban_del_status).setOnClickListener(this);
        root.findViewById(R.id.editor_ban_fake).setOnClickListener(this);
        root.findViewById(R.id.editor_ban_porn).setOnClickListener(this);
        root.findViewById(R.id.editor_ban_unban_user).setOnClickListener(this);

    }

    private void initFullInfo(View root) {
        boolean ok = false;
        if (mResponse != null) {
            JSONTokener tokener = new JSONTokener(mResponse);
            try {
                JSONObject finalResult;
                finalResult = new JSONObject(tokener);
                setInfoText(root, R.id.editor_ban_profile_full_info, finalResult.toString(4));
                ok = true;
            } catch (JSONException e) {
                Debug.error("Wrong response parsing", e);
            }
        }

        if (ok) {
            mFullInfo = root.findViewById(R.id.editor_ban_profile_full_info);
            root.findViewById(R.id.editor_ban_profile_show_full_info).setOnClickListener(this);
            mFullInfo.setVisibility(mIsFullInfoVisible ? View.VISIBLE : View.GONE);
        } else {
            showView(root, R.id.editor_ban_profile_show_full_info, false);
        }
    }

    private void showView(View root, int viewId, boolean show) {
        root.findViewById(viewId).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showView(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setInfoText(View rootLayout, int fieldId, String text) {
        TextView textView = (TextView) rootLayout.findViewById(fieldId);
        textView.setText(textView.getText() + " " + text);
    }

    @Override
    protected String getTitle() {
        return getResources().getString(R.string.editor_profile_title);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editor_ban_profile_show_full_info:
                if (mFullInfo != null) {
                    mFullInfo.setVisibility(mFullInfo.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                }
                break;
            case R.id.editor_ban_censor:
                banUser(BanAction.CENSOR);
                break;
            case R.id.editor_ban_change_gender:
                banUser(BanAction.CHANGE_GENDER);
                break;
            case R.id.editor_ban_del_about:
                banUser(BanAction.DEL_ABOUT);
                break;
            case R.id.editor_ban_del_photo:
                banUser(BanAction.DEL_PHOTO);
                break;
            case R.id.editor_ban_del_photo_all:
                banUser(BanAction.DEL_PHOTO_ALL);
                break;
            case R.id.editor_ban_del_status:
                banUser(BanAction.DEL_STATUS);
                break;
            case R.id.editor_ban_fake:
                banUser(BanAction.FAKE);
                break;
            case R.id.editor_ban_porn:
                banUser(BanAction.PORN);
                break;
            case R.id.editor_ban_porn_album:
                banUser(BanAction.PORN_ALBUM);
                break;
            case R.id.editor_ban_spam_msg:
                banUser(BanAction.SPAM_MSG);
                break;
            case R.id.editor_ban_spam_photo:
                banUser(BanAction.SPAM_PHOTO);
                break;
            case R.id.editor_ban_unban_user:
                unBanUser();
                break;
        }
    }

    private void banUser(String banAction) {
        showView(mLocker, true);
        ModerationPunish punish = new ModerationPunish(getActivity(), banAction, mUserId);
        registerRequest(punish);
        punish.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                Utils.showToastNotification(R.string.editor_ban_result_ok, Toast.LENGTH_SHORT);
                showView(mLocker, false);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Toast.makeText(getActivity(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                showView(mLocker, false);
            }
        }).exec();
    }

    private void unBanUser() {
        ModerationUnban unban = new ModerationUnban(getActivity(), mUserId);
        registerRequest(unban);
        unban.callback(new DataApiHandler<ModerationResponse>() {
            @Override
            protected void success(ModerationResponse data, IApiResponse response) {
                if (data.completed) {
                    Toast.makeText(getActivity(), R.string.editor_ban_unban_user_result, Toast.LENGTH_SHORT).show();
                    showView(mLocker, false);
                    showView(getView(), R.id.editor_ban_unban_user, false);
                }
            }

            @Override
            protected ModerationResponse parseResponse(ApiResponse response) {
                return JsonUtils.fromJson(response.toString(), ModerationResponse.class);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Utils.showToastNotification(response.getErrorMessage(), Toast.LENGTH_SHORT);
                showView(mLocker, false);
            }
        }).exec();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FULL_INFO_VISIBLE, mFullInfo.getVisibility() == View.VISIBLE);
        if (mScroll != null) {
            outState.putInt(SCROLL_VIEW_LIST, mScroll.getScrollY());
        }
    }
}
