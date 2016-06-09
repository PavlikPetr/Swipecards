package com.topface.topface.ui.fragments;

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
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.ui.dialogs.AboutAppDialog;
import com.topface.topface.ui.dialogs.PreloadPhotoSelectorDialog;
import com.topface.topface.ui.dialogs.PreloadPhotoSelectorTypes;
import com.topface.topface.ui.dialogs.SelectLanguageDialog;
import com.topface.topface.ui.fragments.profile.ProfileInnerFragment;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.utils.MarketApiManager;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

@FlurryOpenEvent(name = SettingsFragment.PAGE_NAME)
public class SettingsFragment extends ProfileInnerFragment {

    public static final String PAGE_NAME = "profile.settings";

    private TextView mSocialNameText;
    private MarketApiManager mMarketApiManager;

    private TextView preloadPhotoName;
    private CheckBox mAutoReplySettings;

    @Bind(R.id.loNotifications)
    View mLoNotifications;
    @Bind(R.id.loNoNotifications)
    ViewGroup mNoNotificationViewGroup;
    @Bind(R.id.loHelp)
    View mHelp;

    @SuppressWarnings("unused")
    @OnClick(R.id.loNotifications)
    protected void notificationClick() {
        Intent intent = new Intent(App.getContext(), SettingsContainerActivity.class);
        startActivityForResult(intent, SettingsContainerActivity.INTENT_NOTIFICATIONS);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.loAccount)
    protected void accountClick() {
        Intent intent = new Intent(App.getContext(), SettingsContainerActivity.class);
        startActivityForResult(intent, SettingsContainerActivity.INTENT_ACCOUNT);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.loFeedback)
    protected void feedbackClick() {
        Intent intent = new Intent(App.getContext(), SettingsContainerActivity.class);
        startActivityForResult(intent, SettingsContainerActivity.INTENT_FEEDBACK);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.loAbout)
    protected void aboutClick() {
        Options options = App.from(getActivity()).getOptions();
        AboutAppDialog.newInstance(getActivity().getString(R.string.settings_about), options.aboutApp.title, options.aboutApp.url).show(getActivity().getSupportFragmentManager(),
                AboutAppDialog.class.getName());
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.loLanguage)
    protected void languageClick() {
        new SelectLanguageDialog().show(getActivity().getSupportFragmentManager(), SelectLanguageDialog.class.getName());
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.loHelp)
    protected void helpClick() {
        String helpUrl = App.from(getActivity()).getOptions().helpUrl;
        if (!TextUtils.isEmpty(helpUrl)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(helpUrl));
            startActivity(intent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(R.layout.fragment_settings, null);
        ButterKnife.bind(this, view);
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
    protected String getScreenName() {
        return PAGE_NAME;
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
        setAutoReplySettings(App.get().getOptions().isAutoreplyAllow);
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

    @OnClick(R.id.autoReplyItem)
    public void setInversAutoReplySettings() {
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
                App.get().getOptions().isAutoreplyAllow = newValue;
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

        if (TextUtils.isEmpty(App.from(getActivity()).getOptions().helpUrl)) {
            mHelp.setVisibility(View.GONE);
        }

        //Preload photo
        frame = root.findViewById(R.id.loPreloadPhoto);
        ((TextView) frame.findViewWithTag("tvTitle")).setText(R.string.settings_loading_photo);
        preloadPhotoName = (TextView) frame.findViewWithTag("tvText");
        preloadPhotoName.setVisibility(View.VISIBLE);
        preloadPhotoName.setText(App.getUserConfig().getPreloadPhotoType().getName());
    }

    private void initAccountViews(View root) {
        ViewGroup frame = (ViewGroup) root.findViewById(R.id.loAccount);
        ((TextView) frame.findViewWithTag("tvTitle")).setText(R.string.settings_account);
        mSocialNameText = (TextView) frame.findViewWithTag("tvText");
        getSocialAccountName(mSocialNameText);
        getSocialAccountIcon(mSocialNameText);
        mSocialNameText.setVisibility(View.VISIBLE);
    }

    private void setNotificationsState() {
        boolean isMarketApiAvailable = mMarketApiManager.isMarketApiAvailable();
        if ((!isMarketApiAvailable && mMarketApiManager.isMarketApiSupportByUs()) ||
                (!isMarketApiAvailable && !App.from(getActivity()).getProfile().email)) {
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

    @SuppressWarnings("unused")
    @OnClick(R.id.loPreloadPhoto)
    protected void showPreloadPhotoSelectorDialog() {
        PreloadPhotoSelectorDialog preloadPhotoSelectorDialog = new PreloadPhotoSelectorDialog();
        preloadPhotoSelectorDialog.setPreloadPhotoTypeListener(new PreloadPhotoSelectorDialog.PreloadPhotoTypeListener() {
            @Override
            public void onSelected(PreloadPhotoSelectorTypes type) {
                preloadPhotoName.setText(type.getName());
            }
        });
        preloadPhotoSelectorDialog.show(getActivity().getSupportFragmentManager(), PreloadPhotoSelectorDialog.class.getName());

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

    private void getSocialAccountName(final TextView textView) {
        if (textView == null) {
            return;
        }
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
        } else {
            textView.setText(authToken.getLogin());
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
    private void getSocialAccountIcon(final TextView textView) {
        int iconId = 0;
        switch (AuthToken.getInstance().getSocialNet()) {
            case AuthToken.SN_FACEBOOK:
                iconId = R.drawable.ic_fb;
                break;
            case AuthToken.SN_VKONTAKTE:
                iconId = R.drawable.ic_vk;
                break;
            case AuthToken.SN_TOPFACE:
                iconId = R.drawable.ic_tf_settings;
                break;
            case AuthToken.SN_ODNOKLASSNIKI:
                iconId = R.drawable.ic_ok;
                break;
        }
        if (textView != null) {
            textView.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMarketApiManager != null) {
            mMarketApiManager.onResume();
        }
        setNotificationsState();
        getSocialAccountName(mSocialNameText);
        getSocialAccountIcon(mSocialNameText);
    }

}
