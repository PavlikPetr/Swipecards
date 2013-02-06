package com.topface.topface.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gcm.GCMRegistrar;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.search.SearchUser;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.LogoutRequest;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.analytics.TrackedFragment;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.LinkedList;

public class SettingsAccountFragment extends TrackedFragment {

    public static final int RESULT_LOGOUT = 666;
    private LockerView lockerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_account, container, false);

        // Navigation bar
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
        lockerView = (LockerView) root.findViewById(R.id.llvLogoutLoading);
        lockerView.setVisibility(View.GONE);
        ((TextView) getActivity().findViewById(R.id.tvNavigationTitle)).setText(R.string.settings_account);

        Drawable icon = null;
        final AuthToken token = new AuthToken(getActivity().getApplicationContext());

        if (token.getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
            icon = getResources().getDrawable(R.drawable.fb_icon);
        } else if (token.getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            icon = getResources().getDrawable(R.drawable.vk_icon);
        }
        TextView textName = (TextView) root.findViewById(R.id.tvName);
        textName.setText(Settings.getInstance().getSocialAccountName());
        textName.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);

        root.findViewById(R.id.btnLogout).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                LogoutRequest logoutRequest = new LogoutRequest(getActivity());
                lockerView.setVisibility(View.VISIBLE);
                logoutRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        GCMRegistrar.unregister(getActivity().getApplicationContext());
                        Data.removeSSID(getActivity().getApplicationContext());
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
                        Data.searchList = new LinkedList<SearchUser>();
                        Data.searchPosition = -1;

                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        lockerView.setVisibility(View.GONE);
                    }
                }).exec();

            }
        });

        return root;
    }

    @SuppressWarnings({"rawtypes", "hiding"})
    class FacebookLogoutTask extends AsyncTask {
        @Override
        protected java.lang.Object doInBackground(java.lang.Object... params) {
            try {
                AuthorizationManager.getFacebook().logout(getActivity().getApplicationContext());

            } catch (Exception e) {
                Debug.error(e);
            }
            return null;
        }

    }
}
