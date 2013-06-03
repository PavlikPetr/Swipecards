package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.data.Auth;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.SendMailNotificationsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.edit.EditProfileActivity;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.ui.settings.SettingsAccountFragment;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.utils.*;
import com.topface.topface.utils.social.AuthToken;

import java.util.HashMap;
import java.util.Locale;

public class SettingsFragment extends BaseFragment implements OnClickListener, OnCheckedChangeListener {

    private Settings mSettings;
    private EditSwitcher mSwitchVibration;
    private HashMap<String, ProgressBar> hashNotifiersProgressBars = new HashMap<String, ProgressBar>();
    private CountDownTimer mSendTimer = new CountDownTimer(3000, 3000) {
        @Override
        public void onTick(long l) {
        }

        @Override
        public void onFinish() {
            Activity activity = getActivity();
            if (activity != null) {
                SendMailNotificationsRequest request = mSettings.getMailNotificationRequest(activity);
                if (request != null) {
                    request.callback(new ApiHandler() {
                        @Override
                        public void success(ApiResponse response) throws NullPointerException {
                        }

                        @Override
                        public void fail(int codeError, ApiResponse response) throws NullPointerException {
                        }
                    }).exec();
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(R.layout.ac_settings, null);

        mSettings = Settings.getInstance();

        // Navigation bar
        ActionBar actionBar = getActionBar(view);
        actionBar.showBackButton(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
        actionBar.setTitleText(getString(R.string.settings_header_title));

        // Init settings views
        initViews(view);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSendTimer != null) {
            mSendTimer.cancel();
            mSendTimer.onFinish();
        }
    }

    private void initViews(View root) {
        ViewGroup frame;

        // Edit Profile Button
        TextView btnEditProfile = (TextView) root.findViewById(R.id.btnProfileEdit);
        btnEditProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity().getApplicationContext(), EditProfileActivity.class));
            }
        });

        // Notifications header
        frame = (ViewGroup) root.findViewById(R.id.loNotificationsHeader);
        setText(R.string.settings_notifications_header, frame);

        // Likes
        frame = (ViewGroup) root.findViewById(R.id.loLikes);
        setBackground(R.drawable.edit_big_btn_top_selector, frame);
        setText(R.string.settings_likes, frame);

        boolean mail;
        boolean apns;
        if (!CacheProfile.hasMail && !GCMUtils.GCM_SUPPORTED) {
            root.findViewById(R.id.tvNoNotification).setVisibility(View.VISIBLE);
            root.findViewById(R.id.loNotificationsHeader).setVisibility(View.GONE);
            root.findViewById(R.id.loLikes).setVisibility(View.GONE);
            root.findViewById(R.id.loMutual).setVisibility(View.GONE);
            root.findViewById(R.id.loChat).setVisibility(View.GONE);
            root.findViewById(R.id.loGuests).setVisibility(View.GONE);
            root.findViewById(R.id.loVibration).setVisibility(View.GONE);
            root.findViewById(R.id.loMelody).setVisibility(View.GONE);
        } else {
            if (CacheProfile.notifications != null && CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_LIKES) != null) {
                mail = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_LIKES).mail;
                apns = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_LIKES).apns;
            } else {
                mail = false;
                apns = false;
            }
            initEditNotificationFrame(CacheProfile.NOTIFICATIONS_LIKES, frame, CacheProfile.hasMail, mail, apns);

            // Mutual
            frame = (ViewGroup) root.findViewById(R.id.loMutual);
            setBackground(R.drawable.edit_big_btn_middle, frame);
            setText(R.string.settings_mutual, frame);
            if (CacheProfile.notifications != null && CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_SYMPATHY) != null) {
                mail = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_SYMPATHY).mail;
                apns = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_SYMPATHY).apns;
            } else {
                mail = false;
                apns = false;
            }
            initEditNotificationFrame(CacheProfile.NOTIFICATIONS_SYMPATHY, frame, CacheProfile.hasMail, mail, apns);

            // Chat
            frame = (ViewGroup) root.findViewById(R.id.loChat);
            setBackground(R.drawable.edit_big_btn_middle, frame);
            setText(R.string.settings_messages, frame);
            if (CacheProfile.notifications != null && CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_MESSAGE) != null) {
                mail = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_MESSAGE).mail;
                apns = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_MESSAGE).apns;
            } else {
                mail = false;
                apns = false;
            }
            initEditNotificationFrame(CacheProfile.NOTIFICATIONS_MESSAGE, frame, CacheProfile.hasMail, mail, apns);

            // Guests
            frame = (ViewGroup) root.findViewById(R.id.loGuests);
            setBackground(R.drawable.edit_big_btn_bottom, frame);
            setText(R.string.settings_guests, frame);
            if (CacheProfile.notifications != null && CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_VISITOR) != null) {
                mail = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_VISITOR).mail;
                apns = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_VISITOR).apns;
            } else {
                mail = false;
                apns = false;
            }
            initEditNotificationFrame(CacheProfile.NOTIFICATIONS_VISITOR, frame, CacheProfile.hasMail, mail, apns);

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

            if (!GCMUtils.GCM_SUPPORTED) {
                root.findViewById(R.id.loVibration).setVisibility(View.GONE);
                root.findViewById(R.id.loMelody).setVisibility(View.GONE);
            }
        }
        // Help
        frame = (ViewGroup) root.findViewById(R.id.loHelp);
        setBackground(R.drawable.edit_big_btn_top_selector, frame);
        setText(R.string.settings_help, frame);
        frame.setOnClickListener(this);

        // Account
        initAccountViews(root);

        // Rate app
        frame = (ViewGroup) root.findViewById(R.id.loFeedback);
        setBackground(R.drawable.edit_big_btn_middle_selector, frame);
        setText(R.string.settings_feedback, frame);
        frame.setOnClickListener(this);

        // Language app
        frame = (ViewGroup) root.findViewById(R.id.loLanguage);
        setBackground(R.drawable.edit_big_btn_middle_selector, frame);
        setText(R.string.settings_select_language, frame);
        frame.setOnClickListener(this);

        // About
        frame = (ViewGroup) root.findViewById(R.id.loAbout);
        setBackground(R.drawable.edit_big_btn_bottom_selector, frame);
        setText(R.string.settings_about, frame);
        frame.setOnClickListener(this);


    }

    private void initAccountViews(View root) {
        ViewGroup frame;
        frame = (ViewGroup) root.findViewById(R.id.loAccount);
        setBackground(R.drawable.edit_big_btn_middle_selector, frame);
        AuthToken authToken = AuthToken.getInstance();
        String name = mSettings.getSocialAccountName();
        if (authToken.getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
            setAccountNameText(R.string.settings_account, name, R.drawable.ic_fb, frame);
        } else if (authToken.getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            setAccountNameText(R.string.settings_account, name, R.drawable.ic_vk, frame);
        } else if (authToken.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            name = authToken.getLogin();
            setAccountNameText(R.string.settings_account, name, R.drawable.ic_tf, frame);
        } else {
            setText(R.string.settings_account, frame);
        }
        frame.setOnClickListener(this);
    }

    private void setText(int titleId, ViewGroup frame) {
        ((TextView) frame.findViewWithTag("tvTitle")).setText(titleId);
    }

    private void setAccountNameText(int titleId, String text, int iconRes, ViewGroup frame) {
        ((TextView) frame.findViewWithTag("tvTitle")).setText(titleId);
        final TextView textView = (TextView) frame.findViewWithTag("tvText");
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

    private void initEditNotificationFrame(int key, ViewGroup frame, boolean hasMail, boolean mailChecked, boolean phoneChecked) {
        CheckBox checkBox = (CheckBox) frame.findViewWithTag("cbPhone");
        ProgressBar prsPhone = (ProgressBar) frame.findViewWithTag("prsPhone");
        String phoneNotifierKey = Options.generateKey(key, false);
        hashNotifiersProgressBars.put(phoneNotifierKey, prsPhone);
        if (GCMUtils.GCM_SUPPORTED) {
            checkBox.setTag(phoneNotifierKey);
            checkBox.setChecked(phoneChecked);
            checkBox.setOnCheckedChangeListener(this);
            checkBox.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // магия
                }
            });
        } else {
            checkBox.setVisibility(View.GONE);
            prsPhone.setVisibility(View.GONE);
        }

        final CheckBox checkBoxEmail = (CheckBox) frame.findViewWithTag("cbMail");
        ProgressBar prsMail = (ProgressBar) frame.findViewWithTag("prsMail");
        String mailNotifierKey = Options.generateKey(key, true);
        hashNotifiersProgressBars.put(mailNotifierKey, prsMail);
        if (hasMail) {
            checkBoxEmail.setTag(mailNotifierKey);
            checkBoxEmail.setChecked(mailChecked);
            checkBoxEmail.setEnabled(hasMail);
            checkBoxEmail.setOnCheckedChangeListener(this);
        } else {
            checkBoxEmail.setVisibility(View.GONE);
            prsMail.setVisibility(View.GONE);
        }
        checkBoxEmail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // магия
            }
        });
    }

    private void setBackground(int resId, ViewGroup frame) {
        ImageView background = (ImageView) frame.findViewWithTag("ivEditBackground");
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
                mSettings.setSetting(Settings.SETTINGS_C2DM_VIBRATION, mSwitchVibration.isChecked());
                break;
            case R.id.loMelody:
                intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.settings_melody));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mSettings.getRingtone());
                startActivityForResult(intent, Settings.REQUEST_CODE_RINGTONE);
                break;
            case R.id.loLanguage:
                startLanguageSelection();
                break;
            default:
                break;
        }
    }

    private void startLanguageSelection() {
        //TODO init selected locale
        final String[] locales = getResources().getStringArray(R.array.application_locales);
        final String[] languages = new String[locales.length];
        int selectedLocaleIndex = 0;
        Locale appLocale = new Locale(App.getConfig().getLocaleConfig().getApplicationLocale());
        for (int i=0;i<locales.length;i++) {
            Locale locale = new Locale(locales[i]);
            languages[i] = locale.getDisplayName(locale);
            if(locale.equals(appLocale)) {
                selectedLocaleIndex = i;
            }
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.settings_select_language)
                .setSingleChoiceItems(languages, selectedLocaleIndex, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogLocales, int which) {
                        dialogLocales.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialogLocales, int whichButton) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.settings_select_language)
                                .setMessage(R.string.restart_to_change_locale)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogConfirm, int which) {
                                        String selectedLocale = locales[((AlertDialog) dialogLocales).getListView().getCheckedItemPosition()];
                                        LocaleConfig.changeLocale(getActivity(), selectedLocale,MenuFragment.F_PROFILE);
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogConfirm, int which) {
                                        dialogLocales.dismiss();
                                    }
                                }).show();
                    }
                }).show();
    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
        final String key = (String) buttonView.getTag();

        String[] buttonInfo = key.split(Options.GENERAL_SEPARATOR);
        final Integer type = Integer.parseInt(buttonInfo[0]);
        final boolean isMail = buttonInfo[1].equals(Options.GENERAL_MAIL_CONST);
        final ProgressBar prs = hashNotifiersProgressBars.get(key);

        if (isMail) {
            SendMailNotificationsRequest request = mSettings.getMailNotificationRequest(type, isMail, isChecked, getActivity().getApplicationContext());
            if (request != null) {
                buttonView.post(new Runnable() {

                    @Override
                    public void run() {
                        buttonView.setEnabled(false);
                        prs.setVisibility(View.VISIBLE);
                    }
                });
                request.callback(new ApiHandler() {

                    @Override
                    public void success(ApiResponse response) {
                        buttonView.post(new Runnable() {

                            @Override
                            public void run() {
                                buttonView.setEnabled(true);
                                prs.setVisibility(View.GONE);
                            }
                        });
                        CacheProfile.notifications.get(type).mail = isChecked;
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        //TODO: Здесь нужно что-то делать, чтобы пользователь понял, что у него не получилось отменить нотификации.
                        buttonView.post(new Runnable() {

                            @Override
                            public void run() {
                                buttonView.setEnabled(true);
                                prs.setVisibility(View.GONE);
                            }
                        });
                        buttonView.setChecked(!isChecked);
                        CacheProfile.notifications.get(type).mail = !isChecked;
                    }
                }).exec();
            }
        } else {
            if (CacheProfile.notifications != null) {
                Profile.TopfaceNotifications notifications = CacheProfile.notifications.get(type);
                if (notifications != null) {
                    notifications.apns = isChecked;
                }
            }
            mSendTimer.cancel();
            mSendTimer.start();
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
