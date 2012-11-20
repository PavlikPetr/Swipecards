package com.topface.topface.ui.settings;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gcm.GCMRegistrar;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.LogoutRequest;
import com.topface.topface.ui.AuthActivity;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.social.AuthToken;

public class SettingsAccountFragment extends Fragment {

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
                    public void success(ApiResponse response) throws NullPointerException {
                        GCMRegistrar.unregister(getActivity().getApplicationContext());
                        Data.removeSSID(getActivity().getApplicationContext());
                        token.removeToken();
                        getActivity().runOnUiThread(new Runnable() {
                            @SuppressWarnings({"rawtypes", "unchecked"})
                            @Override
                            public void run() {
                                new FacebookLogoutTask().execute();
                            }
                        });
                        Settings.getInstance().resetSettings();
                        startActivity(new Intent(getActivity().getApplicationContext(), AuthActivity.class));
                        getActivity().setResult(RESULT_LOGOUT);
                        getActivity().finish();
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) throws NullPointerException {
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
                Data.facebook.logout(getActivity().getApplicationContext());

            } catch (Exception e) {
                Debug.error(e);
            }
            return null;
        }

    }
}
