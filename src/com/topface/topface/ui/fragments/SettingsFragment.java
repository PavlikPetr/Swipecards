package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.SendMailNotificationsRequest;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.ui.settings.SettingsAccountFragment;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.NavigationBarController;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.social.AuthToken;

public class SettingsFragment extends BaseFragment implements OnClickListener, OnCheckedChangeListener {

    private Settings mSettings;
    private EditSwitcher mSwitchVibration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(R.layout.ac_settings, null);

        mSettings = Settings.getInstance();

        // Navigation bar
        mNavBarController = new NavigationBarController((ViewGroup) view.findViewById(R.id.loNavigationBar));
        view.findViewById(R.id.btnNavigationHome).setOnClickListener((NavigationActivity) getActivity());
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(R.string.settings_header_title);

        // Init settings views
        initViews(view);

        return view;
    }

    private void initViews(View root) {
        ViewGroup frame;

        Options options = CacheProfile.getOptions();

        // Notifications header
        frame = (ViewGroup) root.findViewById(R.id.loNotificationsHeader);
        setText(R.string.settings_notifications_header, frame);

        // Likes
        frame = (ViewGroup) root.findViewById(R.id.loLikes);
        setBackground(R.drawable.edit_big_btn_top, frame);
        setText(R.string.settings_likes, frame);
        initEditNotificationFrame(Options.NOTIFICATIONS_LIKES, frame, options.hasMail, options.notifications.get(Options.NOTIFICATIONS_LIKES).mail,options.notifications.get(Options.NOTIFICATIONS_LIKES).apns);

        // Mutual
        frame = (ViewGroup) root.findViewById(R.id.loMutual);
        setBackground(R.drawable.edit_big_btn_middle, frame);
        setText(R.string.settings_mutual, frame);
        initEditNotificationFrame(Options.NOTIFICATIONS_SYMPATHY, frame, options.hasMail, options.notifications.get(Options.NOTIFICATIONS_SYMPATHY).mail,options.notifications.get(Options.NOTIFICATIONS_SYMPATHY).apns);

        // Chat
        frame = (ViewGroup) root.findViewById(R.id.loChat);
        setBackground(R.drawable.edit_big_btn_middle, frame);
        setText(R.string.settings_messages, frame);
        initEditNotificationFrame(Options.NOTIFICATIONS_MESSAGE, frame, options.hasMail, options.notifications.get(Options.NOTIFICATIONS_MESSAGE).mail,options.notifications.get(Options.NOTIFICATIONS_MESSAGE).apns);

        // Guests
        frame = (ViewGroup) root.findViewById(R.id.loGuests);
        setBackground(R.drawable.edit_big_btn_bottom, frame);
        setText(R.string.settings_guests, frame);
        initEditNotificationFrame(Options.NOTIFICATIONS_VISITOR, frame, options.hasMail, options.notifications.get(Options.NOTIFICATIONS_VISITOR).mail,options.notifications.get(Options.NOTIFICATIONS_VISITOR).apns);

        // Vibration
        frame = (ViewGroup) root.findViewById(R.id.loVibration);
        setBackground(R.drawable.edit_big_btn_top, frame);
        setText(R.string.settings_vibration, frame);
        mSwitchVibration = new EditSwitcher(frame);
        mSwitchVibration.setChecked(mSettings.isVibrationEnabled());
        frame.setOnClickListener(this);

        //Melody
        frame = (ViewGroup) root.findViewById(R.id.loMelody);
        setBackground(R.drawable.edit_big_btn_bottom_selector, frame);
        setText(R.string.settings_melody, frame);
        frame.setOnClickListener(this);

        // Help
        frame = (ViewGroup) root.findViewById(R.id.loHelp);
        setBackground(R.drawable.edit_big_btn_top_selector, frame);
        setText(R.string.settings_help, frame);
        frame.setOnClickListener(this);

        // Account
        frame = (ViewGroup) root.findViewById(R.id.loAccount);
        setBackground(R.drawable.edit_big_btn_middle_selector, frame);
        AuthToken authToken = new AuthToken(getActivity().getApplicationContext());
        if (authToken.getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
            setAccountNameText(R.string.settings_account, mSettings.getSocialAccountName(), R.drawable.ic_fb, frame);
        } else if (authToken.getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            setAccountNameText(R.string.settings_account, mSettings.getSocialAccountName(), R.drawable.ic_vk, frame);
        } else {
            setText(R.string.settings_account, frame);
        }
        frame.setOnClickListener(this);

        // Rate app
        frame = (ViewGroup) root.findViewById(R.id.loFeedback);
        setBackground(R.drawable.edit_big_btn_middle_selector, frame);
        setText(R.string.settings_feedback, frame);
        frame.setOnClickListener(this);

        // About
        frame = (ViewGroup) root.findViewById(R.id.loAbout);
        setBackground(R.drawable.edit_big_btn_bottom_selector, frame);
        setText(R.string.settings_about, frame);
        frame.setOnClickListener(this);


    }

    private void setText(int titleId, ViewGroup frame) {
        ((TextView) frame.findViewById(R.id.tvTitle)).setText(titleId);
    }

    private void setAccountNameText(int titleId, String text, int iconRes, ViewGroup frame) {
        ((TextView) frame.findViewById(R.id.tvTitle)).setText(titleId);
        final TextView textView = (TextView) frame.findViewById(R.id.tvText);
        textView.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(text)) {
            mSettings.getSocialAccountNameAsync(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    final String name = (String) msg.obj;
                    textView.post(new Runnable() {

                        @Override
                        public void run() {
                            textView.setText(name);
                        }
                    });
                    mSettings.setSocialAccountName(name);
                }
            });
        } else {
            textView.setText(text);
        }

        textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(iconRes), null, null, null);
    }

    private void initEditNotificationFrame(int key, ViewGroup frame, boolean enabled, boolean mailChecked, boolean phoneChecked) {
        CheckBox checkBox = (CheckBox) frame.findViewById(R.id.cbPhone);
        checkBox.setTag(Options.generateKey(key,false));
        checkBox.setChecked(phoneChecked);
        checkBox.setOnCheckedChangeListener(this);

        final CheckBox checkBoxEmail = (CheckBox) frame.findViewById(R.id.cbMail);
        checkBoxEmail.setTag(Options.generateKey(key,true));
        checkBoxEmail.setChecked(mailChecked);
        checkBoxEmail.setEnabled(enabled);
        checkBoxEmail.setOnCheckedChangeListener(this);
    }

    private void setBackground(int resId, ViewGroup frame) {
        ImageView background = (ImageView) frame.findViewById(R.id.ivEditBackground);
        background.setImageResource(resId);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.loHelp:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getResources().getString(R.string.settings_help_url)));
                startActivity(intent);
                break;
            case R.id.loAccount:
                intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_ACCOUNT);
                break;
            case R.id.loFeedback:
                intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_FEEDBACK);
                break;
            case R.id.loAbout:
                intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_ABOUT);
                break;
            case R.id.loVibration:
                mSwitchVibration.doSwitch();
                mSettings.setSetting(Settings.SETTINGS_C2DM_VIBRATION,mSwitchVibration.isChecked());
                break;
            case R.id.loMelody:
                intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.settings_melody));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mSettings.getRingtone());
                startActivityForResult(intent,Settings.REQUEST_CODE_RINGTONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
        final String key = (String) buttonView.getTag();

        String[] buttonInfo = key.split(Options.GENERAL_SEPARATOR);
        final Integer type = Integer.parseInt(buttonInfo[0]);
        final boolean isMail = Boolean.parseBoolean(buttonInfo[1]);

        SendMailNotificationsRequest request = mSettings.getMailNotificationRequest(type, isMail, isChecked, getActivity());
        if (request != null) {
            buttonView.setEnabled(false);
            request.callback(new ApiHandler() {

                @Override
                public void success(ApiResponse response) throws NullPointerException {
                   buttonView.post(new Runnable() {

                        @Override
                        public void run() {
                            buttonView.setEnabled(true);
                        }
                    });
                    if(isMail) CacheProfile.getOptions().notifications.get(type).mail = isChecked;
                    else CacheProfile.getOptions().notifications.get(type).apns = isChecked;
                }

                @Override
                public void fail(int codeError, ApiResponse response) throws NullPointerException {
                    //TODO: Здесь нужно что-то делать, чтобы пользователь понял, что у него не получилось отменить нотификации.
                    buttonView.post(new Runnable() {

                        @Override
                        public void run() {
                            buttonView.setEnabled(true);
                        }
                    });
                    buttonView.setChecked(!isChecked);
                    if(isMail) CacheProfile.getOptions().notifications.get(type).mail = !isChecked;
                    else CacheProfile.getOptions().notifications.get(type).apns = !isChecked;
                }
            }).exec();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Settings.REQUEST_CODE_RINGTONE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri != null) {
                    mSettings.setSetting(Settings.SETTINGS_C2DM_RINGTONE, uri.toString());
                }
            }
        } else if (resultCode == SettingsAccountFragment.RESULT_LOGOUT &&
                requestCode == SettingsContainerActivity.INTENT_ACCOUNT) {
            getActivity().finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
