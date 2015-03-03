package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendMailNotificationsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.dialogs.PreloadPhotoSelector;
import com.topface.topface.ui.dialogs.PreloadPhotoSelectorTypes;
import com.topface.topface.ui.edit.EditProfileActivity;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.MarketApiManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.cache.SearchCacheManager;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.notifications.UserNotificationManager;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.HashMap;
import java.util.Locale;

public class SettingsFragment extends BaseFragment implements OnClickListener, OnCheckedChangeListener {
    public static final int REQUEST_CODE_RINGTONE = 333;
    private EditSwitcher mSwitchVibration;
    private EditSwitcher mSwitchLED;
    private HashMap<String, ProgressBar> hashNotifiersProgressBars = new HashMap<>();
    private TextView mSocialNameText;
    private MarketApiManager mMarketApiManager;

    private ViewGroup mLoNotificationsHeader;
    private ViewGroup mLoLikes;
    private ViewGroup mLoMutual;
    private ViewGroup mLoChat;
    private ViewGroup mLoGuests;
    private ViewGroup mTvNoNotification;
    private ViewGroup mLoVibration;
    private ViewGroup mLoLED;
    private ViewGroup mLoMelody;

    private CountDownTimer mSendTimer = new CountDownTimer(3000, 3000) {
        @Override
        public void onTick(long l) {
        }

        @Override
        public void onFinish() {
            Activity activity = getActivity();
            if (activity != null) {
                SendMailNotificationsRequest request = getMailNotificationRequest(activity);
                if (request != null) {
                    request.callback(new ApiHandler() {
                        @Override
                        public void success(IApiResponse response) throws NullPointerException {
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) throws NullPointerException {
                        }
                    }).exec();
                }
            }
        }
    };
    private TextView melodyName;
    private TextView preloadPhotoName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(R.layout.fragment_settings, null);

        mMarketApiManager = new MarketApiManager();

        mLoNotificationsHeader = (ViewGroup) view.findViewById(R.id.loNotificationsHeader);
        mLoLikes = (ViewGroup) view.findViewById(R.id.loLikes);
        mLoMutual = (ViewGroup) view.findViewById(R.id.loMutual);
        mLoChat = (ViewGroup) view.findViewById(R.id.loChat);
        mLoGuests = (ViewGroup) view.findViewById(R.id.loGuests);
        mTvNoNotification = (ViewGroup) view.findViewById(R.id.tvNoNotification);
        mLoVibration = (ViewGroup) view.findViewById(R.id.loVibration);
        mLoLED = (ViewGroup) view.findViewById(R.id.loLED);
        mLoMelody = (ViewGroup) view.findViewById(R.id.loMelody);

        // Edit Profile Button
        view.findViewById(R.id.btnProfileEdit).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity().getApplicationContext(), EditProfileActivity.class));
            }
        });

        mLoVibration.setOnClickListener(this);
        mLoLED.setOnClickListener(this);
        mLoMelody.setOnClickListener(this);


        mSwitchVibration = new EditSwitcher(mLoVibration);
        mSwitchLED = new EditSwitcher(mLoLED);

        melodyName = (TextView) mLoMelody.findViewWithTag("tvText");

        // Account
        initAccountViews(view);

        // Init settings views
        /*
         Hack for xiaomi. Maybe not only xiaomi. After finishing activity it keeps initializing it's fragments.
         In case of logout we haven't user settings to properly init some views. Because of this we need
         to abort views initialization if activity is finishing.
          */
        if (!getActivity().isFinishing()) {
            initViews(view);
        }
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

    @Override
    protected String getTitle() {
        return getString(R.string.settings_header_title);
    }

    private void setNotificationVisibility(int visibility) {
        mLoNotificationsHeader.setVisibility(visibility);
        mLoLikes.setVisibility(visibility);
        mLoMutual.setVisibility(visibility);
        mLoChat.setVisibility(visibility);
        mLoGuests.setVisibility(visibility);
    }

    private void setNotificationSettingsVisibility(int visibility) {
        mLoVibration.setVisibility(visibility);
        mLoLED.setVisibility(visibility);
        mLoMelody.setVisibility(visibility);
    }

    private void setNotificationState() {
        boolean isMarketApiAvailable = mMarketApiManager.isMarketApiAvailable();
        if ((!isMarketApiAvailable && mMarketApiManager.isMarketApiSupportByUs()) ||
                (!isMarketApiAvailable && !CacheProfile.email)) {
            TextView title = (TextView) mTvNoNotification.findViewById(R.id.loTitle);
            Button button = (Button) mTvNoNotification.findViewById(R.id.loButton);
            title.setVisibility(mMarketApiManager.isTitleVisible() ? View.VISIBLE : View.GONE);
            button.setVisibility(mMarketApiManager.isButtonVisible() ? View.VISIBLE : View.GONE);
            title.setText(mMarketApiManager.getTitleTextId());
            if (mMarketApiManager.isButtonVisible()) {
                button.setText(mMarketApiManager.getButtonTextId());
            }
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMarketApiManager != null) {
                        mMarketApiManager.onProblemResolve();
                    }
                }
            });
            mTvNoNotification.setBackgroundResource(R.drawable.edit_big_btn_selector);
            mTvNoNotification.setVisibility(View.VISIBLE);
        } else {
            mTvNoNotification.setVisibility(View.GONE);
        }
        if (!CacheProfile.email && !isMarketApiAvailable) {
            melodyName.setVisibility(View.GONE);
            setNotificationVisibility(View.GONE);
        } else {
            setNotificationVisibility(View.VISIBLE);
            melodyName.setVisibility(View.VISIBLE);
        }
        if (isMarketApiAvailable) {
            setNotificationSettingsVisibility(View.VISIBLE);
        } else {
            setNotificationSettingsVisibility(View.GONE);
        }
        initEditNotificationFrame(CacheProfile.NOTIFICATIONS_LIKES,
                mLoLikes,
                CacheProfile.email,
                isMailAvailable(CacheProfile.NOTIFICATIONS_LIKES),
                isGcmAvailable(CacheProfile.NOTIFICATIONS_LIKES));
        initEditNotificationFrame(CacheProfile.NOTIFICATIONS_SYMPATHY,
                mLoMutual,
                CacheProfile.email,
                isMailAvailable(CacheProfile.NOTIFICATIONS_SYMPATHY),
                isGcmAvailable(CacheProfile.NOTIFICATIONS_SYMPATHY));
        initEditNotificationFrame(CacheProfile.NOTIFICATIONS_MESSAGE,
                mLoChat,
                CacheProfile.email,
                isMailAvailable(CacheProfile.NOTIFICATIONS_MESSAGE),
                isGcmAvailable(CacheProfile.NOTIFICATIONS_MESSAGE));
        initEditNotificationFrame(CacheProfile.NOTIFICATIONS_VISITOR,
                mLoGuests,
                CacheProfile.email,
                isMailAvailable(CacheProfile.NOTIFICATIONS_VISITOR),
                isGcmAvailable(CacheProfile.NOTIFICATIONS_VISITOR));
    }

    private boolean isMailAvailable(int key) {
        if (CacheProfile.notifications != null && CacheProfile.notifications.get(key) != null) {
            return CacheProfile.notifications.get(key).mail;
        } else {
            return false;
        }
    }

    private boolean isGcmAvailable(int key) {
        if (CacheProfile.notifications != null && CacheProfile.notifications.get(key) != null) {
            return CacheProfile.notifications.get(key).apns;
        } else {
            return false;
        }
    }

    private void initViews(View root) {
        ViewGroup frame;
        // Notifications header
        setText(R.string.settings_notifications_header, mLoNotificationsHeader);

        // Likes
        setBackground(R.drawable.edit_big_btn_top_selector, mLoLikes);
        setText(R.string.settings_likes, mLoLikes);

        // Mutual
        setBackground(R.drawable.edit_big_btn_middle, mLoMutual);
        setText(R.string.settings_mutual, mLoMutual);

        // Chat
        setBackground(R.drawable.edit_big_btn_middle, mLoChat);
        setText(R.string.settings_messages, mLoChat);

        // Guests
        setBackground(R.drawable.edit_big_btn_bottom, mLoGuests);
        setText(R.string.settings_guests, mLoGuests);

        // Vibration
        setBackground(R.drawable.edit_big_btn_top, mLoVibration);
        setText(R.string.settings_vibration, mLoVibration);
        mSwitchVibration.setChecked(App.getUserConfig().isVibrationEnabled());

        //LED
        setBackground(R.drawable.edit_big_btn_middle, mLoLED);
        setText(R.string.settings_led, mLoLED);
        mSwitchLED.setChecked(App.getUserConfig().isLEDEnabled());

        //Melody
        setBackground(R.drawable.edit_big_btn_bottom_selector, mLoMelody);
        ((TextView) mLoMelody.findViewWithTag("tvTitle")).setText(R.string.settings_melody);
        setRingtonNameByUri(App.getUserConfig().getRingtone());

        // Help
        frame = (ViewGroup) root.findViewById(R.id.loHelp);
        setBackground(R.drawable.edit_big_btn_middle_selector, frame);
        setText(R.string.settings_help, frame);
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

        //Preload photo
        frame = (ViewGroup) root.findViewById(R.id.loPreloadPhoto);
        setBackground(R.drawable.edit_big_btn_selector, frame);
        ((TextView) frame.findViewWithTag("tvTitle")).setText(R.string.settings_loading_photo);
        preloadPhotoName = (TextView) frame.findViewWithTag("tvText");
        preloadPhotoName.setVisibility(View.VISIBLE);
        preloadPhotoName.setText(App.getUserConfig().getPreloadPhotoType().getName());
        frame.setOnClickListener(this);
    }

    private void initAccountViews(View root) {
        ViewGroup frame = (ViewGroup) root.findViewById(R.id.loAccount);
        setBackground(R.drawable.edit_big_btn_top_selector, frame);
        ((TextView) frame.findViewWithTag("tvTitle")).setText(R.string.settings_account);
        mSocialNameText = (TextView) frame.findViewWithTag("tvText");
        getSocialAccountName(mSocialNameText);
        getSocialAccountIcon(mSocialNameText);
        mSocialNameText.setVisibility(View.VISIBLE);
        frame.setOnClickListener(this);
    }

    private void setText(int titleId, ViewGroup frame) {
        ((TextView) frame.findViewWithTag("tvTitle")).setText(titleId);
    }

    private void initEditNotificationFrame(int key, ViewGroup frame, boolean hasMail, boolean mailChecked, boolean phoneChecked) {
        CheckBox checkBox = (CheckBox) frame.findViewWithTag("cbPhone");
        ProgressBar prsPhone = (ProgressBar) frame.findViewWithTag("prsPhone");
        String phoneNotifierKey = Options.generateKey(key, false);
        if (checkBox == null) {
            checkBox = (CheckBox) frame.findViewWithTag(phoneNotifierKey);
        }
        hashNotifiersProgressBars.put(phoneNotifierKey, prsPhone);
        if (mMarketApiManager.isMarketApiAvailable()) {
            checkBox.setTag(phoneNotifierKey);
            checkBox.setChecked(phoneChecked);
            checkBox.setOnCheckedChangeListener(this);
            checkBox.setVisibility(View.VISIBLE);
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
        CheckBox checkBoxEmail = (CheckBox) frame.findViewWithTag("cbMail");
        ProgressBar prsMail = (ProgressBar) frame.findViewWithTag("prsMail");
        String mailNotifierKey = Options.generateKey(key, true);
        if (checkBoxEmail == null) {
            checkBoxEmail = (CheckBox) frame.findViewWithTag(mailNotifierKey);
        }
        hashNotifiersProgressBars.put(mailNotifierKey, prsMail);
        if (hasMail) {
            checkBoxEmail.setTag(mailNotifierKey);
            checkBoxEmail.setChecked(mailChecked);
            checkBoxEmail.setEnabled(true);
            checkBoxEmail.setVisibility(View.VISIBLE);
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
        Context applicationContext = App.getContext();
        switch (v.getId()) {
            case R.id.loAccount:
                intent = new Intent(applicationContext, SettingsContainerActivity.class);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_ACCOUNT);
                break;
            case R.id.loHelp:
                intent = new Intent(applicationContext, SettingsContainerActivity.class);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_FEEDBACK);
                break;
            case R.id.loAbout:
                intent = new Intent(applicationContext, SettingsContainerActivity.class);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_ABOUT);
                break;
            case R.id.loVibration:
                mSwitchVibration.doSwitch();
                App.getUserConfig().setGCMVibrationEnabled(mSwitchVibration.isChecked());
                App.getUserConfig().saveConfig();
                Debug.log(App.getUserConfig(), "UserConfig changed");

                // Send empty vibro notification to demonstrate
                if (mSwitchVibration.isChecked()) {
                    UserNotificationManager.getInstance().showSimpleNotification(
                            new NotificationCompat.Builder(getActivity()).setDefaults(Notification.
                                    DEFAULT_VIBRATE).build()
                    );
                }
                break;
            case R.id.loLED:
                mSwitchLED.doSwitch();
                App.getUserConfig().setLEDEnabled(mSwitchLED.isChecked());
                App.getUserConfig().saveConfig();
                Debug.log(App.getUserConfig(), "UserConfig changed");
                break;
            case R.id.loMelody:
                intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.settings_melody));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, App.getUserConfig().getRingtone());
                startActivityForResult(intent, REQUEST_CODE_RINGTONE);
                break;
            case R.id.loLanguage:
                startLanguageSelection();
                break;
            case R.id.loPreloadPhoto:
                PreloadPhotoSelector preloadPhotoSelector = new PreloadPhotoSelector(getActivity());
                preloadPhotoSelector.setPreloadPhotoTypeListener(new PreloadPhotoSelector.PreloadPhotoTypeListener() {
                    @Override
                    public void onSelected(PreloadPhotoSelectorTypes type) {
                        preloadPhotoName.setText(type.getName());
                    }
                });
                break;
            default:
                break;
        }
    }

    private void startLanguageSelection() {
        final String[] locales = getResources().getStringArray(R.array.application_locales);
        final String[] languages = new String[locales.length];
        int selectedLocaleIndex = 0;
        Locale appLocale = new Locale(App.getLocaleConfig().getApplicationLocale());
        for (int i = 0; i < locales.length; i++) {
            Locale locale = new Locale(locales[i]);
            languages[i] = Utils.capitalize(locale.getDisplayName(locale));
            if (locale.equals(appLocale)) {
                selectedLocaleIndex = i;
            }
        }
        final int selectedLocaleIndexFinal = selectedLocaleIndex;
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
                        final int selectedPosition = ((AlertDialog) dialogLocales).getListView().getCheckedItemPosition();
                        if (selectedLocaleIndexFinal == selectedPosition) {
                            dialogLocales.dismiss();
                            return;
                        }
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.settings_select_language)
                                .setMessage(R.string.restart_to_change_locale)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogConfirm, int which) {
                                        String selectedLocale = locales[selectedPosition];
                                        (new SearchCacheManager()).clearCache();

                                        LocaleConfig.changeLocale(getActivity(), selectedLocale);
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

        String[] buttonInfo = key.split(Options.INNER_SEPARATOR);
        final Integer type = Integer.parseInt(buttonInfo[0]);
        final boolean isMail = buttonInfo[1].equals(Options.INNER_MAIL_CONST);
        final ProgressBar prs = hashNotifiersProgressBars.get(key);

        if (isMail) {
            SendMailNotificationsRequest request = getMailNotificationRequest(type, true, isChecked, getActivity().getApplicationContext());
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
                    public void success(IApiResponse response) {
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
                    public void fail(int codeError, IApiResponse response) {
                        //NOTE: Здесь нужно что-то делать, чтобы пользователь понял, что у него не получилось отменить нотификации.
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
        if (requestCode == REQUEST_CODE_RINGTONE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                setRingtonNameByUri(uri);
            }
        } else if (resultCode == AuthorizationManager.RESULT_LOGOUT &&
                requestCode == SettingsContainerActivity.INTENT_ACCOUNT) {
            if (isAdded()) {
                getActivity().finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * @return SendMailNotificationRequest depending on a key
     */
    public SendMailNotificationsRequest getMailNotificationRequest(int key, boolean isMail, boolean value, Context context) {
        SendMailNotificationsRequest request = getMailNotificationRequest(context);

        if (request != null) {
            switch (key) {
                case CacheProfile.NOTIFICATIONS_LIKES:
                    if (isMail) request.mailSympathy = value;
                    else request.apnsSympathy = value;
                    break;
                case CacheProfile.NOTIFICATIONS_MESSAGE:
                    if (isMail) request.mailChat = value;
                    else request.apnsChat = value;
                    break;
                case CacheProfile.NOTIFICATIONS_SYMPATHY:
                    if (isMail) request.mailMutual = value;
                    else request.apnsMutual = value;
                    break;
                case CacheProfile.NOTIFICATIONS_VISITOR:
                    if (isMail) request.mailGuests = value;
                    else request.apnsVisitors = value;
                    break;
                default:
                    return null;
            }
        }

        return request;
    }

    private void setRingtonNameByUri(Uri uri) {
        String ringtoneName = getActivity().getString(R.string.silent_ringtone);
        if (uri != null) {
            Cursor mCursor = null;
            try {
                mCursor = getActivity().getContentResolver().query(uri, null, null, null, null);

                if (mCursor != null && mCursor.moveToFirst()) {
                    if (mCursor.getColumnIndex("title") >= 0) {
                        ringtoneName = mCursor.getString(mCursor.getColumnIndex("title"));
                    } else {
                        uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        ringtoneName = getString(R.string.default_ringtone);
                    }
                }
            } catch (Exception ex) {
                Debug.error(ex);
            } finally {
                if (mCursor != null) mCursor.close();
            }
        }
        melodyName.setText(ringtoneName);
        App.getUserConfig().setGCMRingtone(uri == null ? UserConfig.SILENT : uri.toString());
        App.getUserConfig().saveConfig();
        Debug.log(App.getUserConfig(), "UserConfig changed");
    }

    /**
     * Creates SendMailNotificationRequest if possible
     * may return null, when profile information still not cached
     *
     * @return new SendMailNotificationRequest
     */
    public SendMailNotificationsRequest getMailNotificationRequest(Context context) {
        SendMailNotificationsRequest request = new SendMailNotificationsRequest(context);
        if (CacheProfile.notifications != null) {
            try {
                request.mailSympathy = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_LIKES).mail;
                request.mailMutual = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_SYMPATHY).mail;
                request.mailChat = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_MESSAGE).mail;
                request.mailGuests = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_VISITOR).mail;
            } catch (Exception e) {
                Debug.error(e);
            }

            try {
                request.apnsSympathy = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_LIKES).apns;
                request.apnsMutual = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_SYMPATHY).apns;
                request.apnsChat = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_MESSAGE).apns;
                request.apnsVisitors = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_VISITOR).apns;
            } catch (Exception e) {
                Debug.error(e);
            }
            return request;
        } else {
            return null;
        }

    }

    public void getSocialAccountName(final TextView textView) {
        AuthToken authToken = AuthToken.getInstance();
        if (!authToken.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            String name = App.getSessionConfig().getSocialAccountName();
            if (TextUtils.isEmpty(name)) {
                getSocialAccountNameAsync(new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        final String socialName = (String) msg.obj;
                        textView.post(new Runnable() {

                            @Override
                            public void run() {
                                textView.setText(socialName);
                            }
                        });
                        App.getSessionConfig().setSocialAccountName(socialName);
                        App.getSessionConfig().saveConfig();
                    }
                });
            } else {
                textView.setText(name);
            }
        }
    }

    public void getSocialAccountNameAsync(final Handler handler) {
        new BackgroundThread() {
            @Override
            public void execute() {
                AuthToken.getAccountName(handler);
            }
        };
    }

    /**
     * Sets drawable with social network icon to textView
     */
    public void getSocialAccountIcon(final TextView textView) {
        AuthToken authToken = AuthToken.getInstance();
        if (authToken.getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fb, 0, 0, 0);
        } else if (authToken.getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_vk, 0, 0, 0);
        } else if (authToken.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tf, 0, 0, 0);
        } else if (authToken.getSocialNet().equals(AuthToken.SN_ODNOKLASSNIKI)) {
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_ok_settings, 0, 0, 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMarketApiManager != null) {
            mMarketApiManager.onResume();
        }
        setNotificationState();
        AuthToken authToken = AuthToken.getInstance();
        if (authToken.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            mSocialNameText.setText(authToken.getLogin());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
