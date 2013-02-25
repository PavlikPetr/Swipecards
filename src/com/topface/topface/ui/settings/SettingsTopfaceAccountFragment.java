package com.topface.topface.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.google.android.gcm.GCMRegistrar;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.cache.SearchCacheManager;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class SettingsTopfaceAccountFragment extends BaseFragment implements OnClickListener{

    public static final int RESULT_LOGOUT = 666;
    private LockerView mLockerView;
    private ViewFlipper mViewPlipper;
    private EditText mEditText;
    private TextView mText;
    private Button mBtnChange;
    private Button mBtnLogout;
    private final AuthToken mToken = AuthToken.getInstance();

    private static final int ACTION_RESEND_CONFIRM = 0;
    private static final int ACTION_CHANGE_EMAIL = 1;
    private static final int ACTION_CHANGE_PASSWORD = 2;
    private int mChangeButtonAction = ACTION_CHANGE_PASSWORD;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_topface_account, container, false);

        // Navigation bar
        initNavigationBar();

        mLockerView = (LockerView) root.findViewById(R.id.llvLogoutLoading);
        mLockerView.setVisibility(View.GONE);

        mViewPlipper = (ViewFlipper) root.findViewById(R.id.vfFlipper);


        initTextViews(root);
        initButtons(root);
        setViewsState();
        initEmailConfirmedFlag();

        return root;
    }

    private void initEmailConfirmedFlag() {
        ProfileRequest profileRequest = new ProfileRequest(getActivity());
        profileRequest.part = ProfileRequest.P_EMAIL_CONFIRMED;
        lock();
        profileRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                CacheProfile.emailConfirmed = response.jsonResult.optBoolean("email_confirmed");
                setViewsState();
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                Toast.makeText(getActivity(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void always(ApiResponse response) {
                super.always(response);
                unlock();
            }
        }).exec();
    }

    private void setViewsState() {
        setTextViewsState();
        setButtonsState();
    }

    private void initTextViews(ViewGroup root) {
        mEditText = (EditText) root.findViewById(R.id.edText);
        mEditText.setText(mToken.getLogin());
        mEditText.setSelection(mEditText.getText().length());
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.equals(mToken.getLogin())) {
                    setChangeBtnAction(ACTION_RESEND_CONFIRM);
                } else {
                    setChangeBtnAction(ACTION_CHANGE_EMAIL);
                }
            }
        });
        mText = (TextView) root.findViewById(R.id.tvText);
        mText.setText(mToken.getLogin());
    }

    private void setTextViewsState() {
        if (CacheProfile.emailConfirmed) {
            mEditText.setVisibility(View.GONE);
            mText.setVisibility(View.VISIBLE);
        } else {
            mEditText.setVisibility(View.VISIBLE);
            mText.setVisibility(View.GONE);
        }
    }

    private void initButtons(ViewGroup root) {
        mBtnChange = (Button) root.findViewById(R.id.btnChange);
        mBtnChange.setOnClickListener(this);
        mBtnLogout = (Button) root.findViewById(R.id.btnLogout);
        mBtnLogout.setOnClickListener(this);
    }

    private void setButtonsState() {
        if (CacheProfile.emailConfirmed) {
            mBtnLogout.setVisibility(View.VISIBLE);
            setChangeBtnAction(ACTION_CHANGE_PASSWORD);
        } else {
            mBtnLogout.setVisibility(View.GONE);
            setChangeBtnAction(ACTION_RESEND_CONFIRM);
        }
    }

    private void setChangeBtnAction(int action) {
        switch (action) {
            case ACTION_CHANGE_PASSWORD:
                mBtnChange.setText(R.string.change_password);
                break;
            case ACTION_RESEND_CONFIRM:
                mBtnChange.setText(R.string.send_confirmation_email);
                break;
            case ACTION_CHANGE_EMAIL:
                mBtnChange.setText(R.string.change_email);
                break;
        }
        mChangeButtonAction = action;
    }

    private void initNavigationBar() {
        getActivity().findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
        Button btnBack = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setText(R.string.settings_header_title);
        btnBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        ((TextView) getActivity().findViewById(R.id.tvNavigationTitle)).setText(R.string.settings_account);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogout:
                logout(mToken);
                break;
            case R.id.btnChange:
                onChangeButtonClick();
                break;
            default:
                break;
        }
    }

    private void onChangeButtonClick() {
        switch (mChangeButtonAction) {
            case ACTION_RESEND_CONFIRM:
                RemindRequest remindRequest = new RemindRequest(getActivity());
                remindRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        Toast.makeText(getActivity(), R.string.confirmation_successfully_sent, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        Toast.makeText(getActivity(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                    }
                }).exec();
                break;
            case ACTION_CHANGE_EMAIL:
                final String email = mEditText.getText().toString();
                if(Utils.isValidEmail(email)) {
                    ChangeLoginRequest changeLoginRequest = new ChangeLoginRequest(getActivity(),email);
                    changeLoginRequest.callback(new ApiHandler() {
                        @Override
                        public void success(ApiResponse response) {
                            mToken.saveToken(mToken.getUserId(), email, mToken.getPassword());
                            setChangeBtnAction(ACTION_RESEND_CONFIRM);
                        }

                        @Override
                        public void fail(int codeError, ApiResponse response) {
                            Toast.makeText(getActivity(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                        }
                    }).exec();
                } else {
                    Toast.makeText(getActivity(), R.string.incorrect_email, Toast.LENGTH_SHORT).show();
                }
                break;
            case ACTION_CHANGE_PASSWORD:
                Intent intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_CHANGE_PASSWORD);
                break;
        }
    }

    private void logout(final AuthToken token) {
        LogoutRequest logoutRequest = new LogoutRequest(getActivity());
        mLockerView.setVisibility(View.VISIBLE);
        logoutRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                GCMRegistrar.unregister(getActivity().getApplicationContext());
                Ssid.remove();
                token.removeToken();
                //noinspection unchecked
                new FacebookLogoutTask().execute();
                Settings.getInstance().resetSettings();
                startActivity(new Intent(getActivity().getApplicationContext(), NavigationActivity.class));
                getActivity().setResult(RESULT_LOGOUT);
                CacheProfile.clearProfile();
                getActivity().finish();
                SharedPreferences preferences = getActivity().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                if (preferences != null) {
                    preferences.edit().clear().commit();
                }
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Static.LOGOUT_INTENT));
                //Чистим список тех, кого нужно оценить
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new SearchCacheManager().clearCache();
                    }
                }).start();
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                mLockerView.setVisibility(View.GONE);
            }
        }).exec();
    }

    @SuppressWarnings({"rawtypes", "hiding"})
    class FacebookLogoutTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object... params) {
            try {
                AuthorizationManager.getFacebook().logout(getActivity().getApplicationContext());
            } catch (Exception e) {
                Debug.error(e);
            }
            return null;
        }
    }

    private void lock() {
        mLockerView.setVisibility(View.VISIBLE);
    }

    private void unlock() {
        mLockerView.setVisibility(View.GONE);
    }
}
