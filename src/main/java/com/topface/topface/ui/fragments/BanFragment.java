package com.topface.topface.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.BanActivity;
import com.topface.topface.ui.settings.FeedbackMessageFragment;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.social.AuthorizationManager;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class BanFragment extends BaseFragment implements View.OnClickListener {

    public static final String USER_MESSAGE = "userMessage";
    public static final String BAN_EXPIRE = "banExpire";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root;
        root = initView(inflater);
        return root;
    }

    private View initView(LayoutInflater inflater) {
        View root = inflater.inflate(R.layout.ban, null);
        Bundle arg = getArguments();
        Long banExpire = arg.getLong(BAN_EXPIRE);
        String message = arg.getString(USER_MESSAGE);
        TextView mMessage = (TextView) root.findViewById(R.id.banned_message);
        mMessage.setText(message);
        TextView mLogoutText = (TextView) root.findViewById(R.id.logout_text);
        mLogoutText.setOnClickListener(this);
        TextView mTitle = (TextView) root.findViewById(R.id.banned_title);
        Button mFeedback = (Button) root.findViewById(R.id.btnFeedback);
        if (arg.getBoolean(BanActivity.FLOOD)) {
            mFeedback.setVisibility(View.GONE);
            getTimer(mTitle, banExpire).start();
        } else {
            mTitle.setText(prepareTitle(banExpire));
            mFeedback.setOnClickListener(this);
        }
        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logout_text:
                new AuthorizationManager(getActivity()).logout(getActivity());
                break;
            case R.id.btnFeedback:
                getSupportActionBar().show();
                ConnectionManager.getInstance().onBanActivityFinish();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(
                                R.id.ban_content,
                                FeedbackMessageFragment.newInstance(FeedbackMessageFragment.FeedbackType.BAN)
                        )
                        .addToBackStack("e")
                        .commit();
                break;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String prepareTitle(long banExpire) {
        if (banExpire == -1) {
            return App.getContext().getResources().getString(R.string.ban_title_forever);
        }
        SimpleDateFormat df = new SimpleDateFormat("HH:mm dd MMMM yyyy");
        String s = df.format(banExpire * 1000);
        return String.format(App.getContext().getResources().getString(R.string.ban_title_until), s);
    }

    private CountDownTimer getTimer(final TextView textView, long time) {
        return new CountDownTimer(time, 1000) {
            public void onTick(long millis) {
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                textView.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
            }

            public void onFinish() {
                textView.setText("0:00");
                ConnectionManager.getInstance().onBanActivityFinish();
            }
        };
    }

}
