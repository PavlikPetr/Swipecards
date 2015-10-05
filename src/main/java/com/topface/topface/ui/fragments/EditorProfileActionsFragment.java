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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditorProfileActionsFragment extends BaseFragment {
    public static final String USERID = "USERID";
    public static final String PROFILE_RESPONSE = "PROFILE_RESPONSE";
    private static final String FULL_INFO_VISIBLE = "full_info_visible";
    private static final String SCROLL_VIEW_LIST = "scroll_view_list";
    private int mUserId;
    private String mResponse = null;
    private User mUser = null;
    private boolean mIsFullInfoVisible;

    public enum BAN_ACTION {
        SPAM_MSG("TWO_MONTHS_SPAM", R.id.editor_ban_spam_msg),
        SPAM_PHOTO("TWO_MONTHS_PHOTO_SPAM", R.id.editor_ban_spam_photo),
        FAKE("ONE_WEEK_PHOTO_FAKE", R.id.editor_ban_fake),
        CENSOR("TWO_DAYS_ABUSE", R.id.editor_ban_censor),
        PORN("TWO_MONTHS_PHOTO_PORNO", R.id.editor_ban_porn),
        PORN_ALBUM("TWO_MONTHS_PHOTO_PORNO_ALBUM", R.id.editor_ban_porn_album),
        DEL_PHOTO("REMOVE_PHOTO", R.id.editor_ban_del_photo),
        DEL_PHOTO_ALL("REMOVE_ALL_PHOTO", R.id.editor_ban_del_photo_all),
        DEL_STATUS("REMOVE_SHORT", R.id.editor_ban_del_status),
        DEL_ABOUT("REMOVE_ABOUT", R.id.editor_ban_del_about),
        CHANGE_GENDER("SWITCH_SEX", R.id.editor_ban_change_gender);

        private String mText;
        private int mViewId;

        BAN_ACTION(String text, int viewId) {
            this.mText = text;
            this.mViewId = viewId;
        }

        public int getViewId() {
            return mViewId;
        }

        public String getText() {
            return mText;
        }

    }

    @Bind(R.id.editor_profile_scroll)
    ScrollView mScroll;
    @Bind(R.id.editor_ban_locker)
    View mLocker;
    @Bind(R.id.editor_ban_profile_full_info)
    View mFullInfo;

    @SuppressWarnings("unused")
    @OnClick(R.id.editor_ban_profile_full_info)
    protected void fullInfoButtonClick() {
        if (mFullInfo != null) {
            mFullInfo.setVisibility(mFullInfo.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_editor_profile_actions, container, false);
        ButterKnife.bind(this, root);
        Bundle args = getArguments();
        mIsFullInfoVisible = false;
        int scroll = 0;
        if (savedInstanceState != null) {
            mIsFullInfoVisible = savedInstanceState.getBoolean(FULL_INFO_VISIBLE);
            scroll = savedInstanceState.getInt(SCROLL_VIEW_LIST);
        }
        mUserId = args.getInt(USERID, -1);

        mResponse = args.getString(PROFILE_RESPONSE);

        if (!TextUtils.isEmpty(mResponse)) {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(mResponse);
            } catch (JSONException e) {
                Debug.error(e);
            }
            mUser = new User(mUserId, jsonResponse, getActivity());
        }
        initViews(root);

        if (mUserId == -1) {
            getActivity().finish();
        }
        mScroll.scrollTo(0, scroll);
        return root;
    }

    private void initViews(View root) {
        showView(mLocker, false);
        root.setVisibility(View.VISIBLE);
        if (mUser != null) {
            setInfoText(root, R.id.editor_ban_profile_name, mUser.firstName);
            setInfoText(root, R.id.editor_ban_profile_id, Integer.toString(mUserId));
            setInfoText(root, R.id.editor_ban_profile_banned, String.valueOf(mUser.banned));

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

    @SuppressWarnings("unused")
    @OnClick({R.id.editor_ban_censor,
            R.id.editor_ban_change_gender,
            R.id.editor_ban_del_about,
            R.id.editor_ban_del_photo,
            R.id.editor_ban_del_photo_all,
            R.id.editor_ban_del_status,
            R.id.editor_ban_fake,
            R.id.editor_ban_porn,
            R.id.editor_ban_porn_album,
            R.id.editor_ban_spam_msg,
            R.id.editor_ban_spam_photo})
    protected void banUser(View v) {
        showView(mLocker, true);
        ModerationPunish punish = new ModerationPunish(getActivity(), getTextByViewId(v.getId()), mUserId);
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

    @SuppressWarnings("unused")
    @OnClick(R.id.editor_ban_unban_user)
    protected void unBanUser(final View v) {
        ModerationUnban unban = new ModerationUnban(getActivity(), mUserId);
        registerRequest(unban);
        unban.callback(new DataApiHandler<ModerationResponse>() {
            @Override
            protected void success(ModerationResponse data, IApiResponse response) {
                if (data.completed) {
                    Toast.makeText(getActivity(), R.string.editor_ban_unban_user_result, Toast.LENGTH_SHORT).show();
                    showView(mLocker, false);
                    showView(getView(), v.getId(), false);
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
        if (mFullInfo != null) {
            outState.putBoolean(FULL_INFO_VISIBLE, mFullInfo.getVisibility() == View.VISIBLE);
        }
        if (mScroll != null) {
            outState.putInt(SCROLL_VIEW_LIST, mScroll.getScrollY());
        }
    }

    private String getTextByViewId(int viewId) {
        for (BAN_ACTION action : BAN_ACTION.values()) {
            if (action.getViewId() == viewId) {
                return action.getText();
            }
        }
        return null;
    }
}
