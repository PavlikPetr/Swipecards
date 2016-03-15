package com.topface.topface.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.dialogs.AboutAppDialog;
import com.topface.topface.ui.dialogs.PreloadPhotoSelector;
import com.topface.topface.ui.dialogs.PreloadPhotoSelectorTypes;
import com.topface.topface.ui.dialogs.SelectLanguageDialog;
import com.topface.topface.ui.fragments.profile.ProfileInnerFragment;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.MarketApiManager;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class SettingsFragment extends ProfileInnerFragment implements OnClickListener {

    private TextView mSocialNameText;
    private MarketApiManager mMarketApiManager;

    private View mLoNotifications;

    private TextView preloadPhotoName;
    private ViewGroup mNoNotificationViewGroup;
    private CheckBox mAutoReplySettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(R.layout.fragment_settings, null);

        mMarketApiManager = new MarketApiManager();

        // Account
        initAccountViews(view);

        // Auto reply settings
        initAutoReply(view);

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
    protected String getTitle() {
        return getString(R.string.settings_header_title);
    }

    private void initAutoReply(View root) {
        mAutoReplySettings = (CheckBox) root.findViewById(R.id.auto_reply_state);
        mAutoReplySettings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchAutoReplyButton(isChecked);
            }
        });
        setAutoReplySettings(CacheProfile.getOptions().isAutoreplyAllow);
        root.findViewById(R.id.autoReplyItem).setOnClickListener(this);
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    private void setAutoReplySettings(boolean isChecked) {
        if (mAutoReplySettings != null) {
            mAutoReplySettings.setChecked(isChecked);
        }
    }

    private void setInversAutoReplySettings() {
        if (mAutoReplySettings != null) {
            setAutoReplySettings(!mAutoReplySettings.isChecked());
        }
    }

    private void switchAutoReplyButton() {
        if (mAutoReplySettings != null) {
            switchAutoReplyButton(!mAutoReplySettings.isChecked());
        }
    }

    private void switchAutoReplyButton(boolean isChecked) {
        SettingsRequest settingsRequest = new SettingsRequest(getActivity());
        final boolean newValue = isChecked;
        settingsRequest.isAutoReplyAllowed = newValue;
        setAutoReplySettings(newValue);
        settingsRequest.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                CacheProfile.getOptions().isAutoreplyAllow = newValue;
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                setAutoReplySettings(!newValue);
            }
        }).exec();

    }

    private void initViews(View root) {
        View frame;

        // Notifications
        mLoNotifications = root.findViewById(R.id.loNotifications);
        mLoNotifications.setOnClickListener(this);
        mNoNotificationViewGroup = (ViewGroup) root.findViewById(R.id.loNoNotifications);
        mNoNotificationViewGroup.findViewById(R.id.buttonNoNotificationsSetServices)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mMarketApiManager != null) {
                            mMarketApiManager.onProblemResolve(getActivity());
                        }
                    }
                });
        setNotificationsState();

        // Help
        View help = root.findViewById(R.id.loHelp);
        help.setOnClickListener(this);
        if (TextUtils.isEmpty(CacheProfile.getOptions().helpUrl)) {
            help.setVisibility(View.GONE);
        }

        // Feedback
        root.findViewById(R.id.loFeedback).setOnClickListener(this);

        // Language app
        root.findViewById(R.id.loLanguage).setOnClickListener(this);

        // About
        root.findViewById(R.id.loAbout).setOnClickListener(this);

        //Preload photo
        frame = root.findViewById(R.id.loPreloadPhoto);
        ((TextView) frame.findViewWithTag("tvTitle")).setText(R.string.settings_loading_photo);
        preloadPhotoName = (TextView) frame.findViewWithTag("tvText");
        preloadPhotoName.setVisibility(View.VISIBLE);
        preloadPhotoName.setText(App.getUserConfig().getPreloadPhotoType().getName());
        frame.setOnClickListener(this);
    }

    private void initAccountViews(View root) {
        ViewGroup frame = (ViewGroup) root.findViewById(R.id.loAccount);
        ((TextView) frame.findViewWithTag("tvTitle")).setText(R.string.settings_account);
        mSocialNameText = (TextView) frame.findViewWithTag("tvText");
        getSocialAccountName(mSocialNameText);
        getSocialAccountIcon(mSocialNameText);
        mSocialNameText.setVisibility(View.VISIBLE);
        frame.setOnClickListener(this);
    }

    private void setNotificationsState() {
        boolean isMarketApiAvailable = mMarketApiManager.isMarketApiAvailable();
        if ((!isMarketApiAvailable && mMarketApiManager.isMarketApiSupportByUs()) ||
                (!isMarketApiAvailable && !CacheProfile.email)) {
            TextView text = (TextView) mNoNotificationViewGroup.findViewById(R.id.textNoNotificationDescription);
            text.setVisibility(mMarketApiManager.isTitleVisible() ? View.VISIBLE : View.GONE);
            text.setText(mMarketApiManager.getTitleTextId());
            Button button = (Button) mNoNotificationViewGroup.findViewById(R.id.buttonNoNotificationsSetServices);
            button.setVisibility(mMarketApiManager.isButtonVisible() ? View.VISIBLE : View.GONE);
            if (mMarketApiManager.isButtonVisible()) {
                button.setText(mMarketApiManager.getButtonTextId());
            }
            mNoNotificationViewGroup.setVisibility(View.VISIBLE);
            mLoNotifications.setVisibility(View.GONE);
        } else {
            mNoNotificationViewGroup.setVisibility(View.GONE);
            mLoNotifications.setVisibility(View.VISIBLE);
        }
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
                String helpUrl = CacheProfile.getOptions().helpUrl;
                if (!TextUtils.isEmpty(helpUrl)) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(helpUrl));
                    startActivity(intent);
                }
                break;
            case R.id.autoReplyItem:
                setInversAutoReplySettings();
                break;
            case R.id.loFeedback:
                intent = new Intent(applicationContext, SettingsContainerActivity.class);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_FEEDBACK);
                break;
            case R.id.loAbout:
                Options options = CacheProfile.getOptions();
                AboutAppDialog.newInstance(getActivity().getString(R.string.settings_about), options.aboutApp.title, options.aboutApp.url).show(getFragmentManager(),
                        AboutAppDialog.class.getName());
                break;
            case R.id.loLanguage:
                new SelectLanguageDialog().show(getFragmentManager(), SelectLanguageDialog.class.getName());
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
            case R.id.loNotifications:
                intent = new Intent(applicationContext, SettingsContainerActivity.class);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_NOTIFICATIONS);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AuthorizationManager.RESULT_LOGOUT &&
                requestCode == SettingsContainerActivity.INTENT_ACCOUNT) {
            if (isAdded()) {
                getActivity().finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tf_settings, 0, 0, 0);
        } else if (authToken.getSocialNet().equals(AuthToken.SN_ODNOKLASSNIKI)) {
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_ok, 0, 0, 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMarketApiManager != null) {
            mMarketApiManager.onResume();
        }
        setNotificationsState();
        AuthToken authToken = AuthToken.getInstance();
        if (authToken.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            mSocialNameText.setText(authToken.getLogin());
        }
    }

}
