package com.topface.topface.ui.settings;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.c2dm.C2DMessaging;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.ui.AuthActivity;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.social.AuthToken;

public class SettingsAccountFragment extends Fragment {

    public static final int RESULT_LOGOUT = 666;

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

        ((Button) root.findViewById(R.id.btnLogout)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Data.removeSSID(getActivity().getApplicationContext());
                C2DMessaging.unregister(getActivity().getApplicationContext());
                token.removeToken();
                startActivity(new Intent(getActivity().getApplicationContext(), AuthActivity.class));

                getActivity().setResult(RESULT_LOGOUT);
                getActivity().finish();
            }
        });

        return root;
    }

}
