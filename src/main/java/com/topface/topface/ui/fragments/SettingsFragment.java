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
import android.widget.TextView;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.dialogs.AboutAppDialog;
import com.topface.topface.ui.dialogs.PreloadPhotoSelectorDialog;
import com.topface.topface.ui.dialogs.PreloadPhotoSelectorTypes;
import com.topface.topface.ui.dialogs.SelectLanguageDialog;
import com.topface.topface.ui.fragments.profile.ProfileInnerFragment;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.ui.settings.payment_ninja.PaymentInfo;
import com.topface.topface.utils.MarketApiManager;
import com.topface.topface.utils.extensions.SomeExtensionsKt;
import com.topface.topface.utils.rx.RxUtils;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.Subscription;
import rx.functions.Func1;

@FlurryOpenEvent(name = SettingsFragment.PAGE_NAME)
public class SettingsFragment extends ProfileInnerFragment {

    public static final String PAGE_NAME = "profile.settings";

    private TextView mSocialNameText;
    private MarketApiManager mMarketApiManager;

    private TextView preloadPhotoName;
    private boolean mIsAllowedAutoReply;
    private View mRootView;
    private Subscription mPaymentInfoSubscription = null;

    @BindView(R.id.loNotifications)
    View mLoNotifications;
    @BindView(R.id.loNoNotifications)
    ViewGroup mNoNotificationViewGroup;
    @BindView(R.id.loHelp)
    View mHelp;
    @BindView(R.id.auto_reply_state)
    CheckBox mAutoReplySettings;

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

    @SuppressWarnings("unused")
    @OnCheckedChanged(R.id.auto_reply_state)
    protected void autoreplyCheckedChanged(boolean isChecked) {
        autoReplySwitched(isChecked);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        mRootView = inflater.inflate(R.layout.fragment_settings, null);
        ButterKnife.bind(this, mRootView);
        mMarketApiManager = new MarketApiManager();

        // Account
        initAccountViews();

        // Auto reply settings
        boolean isAutoreplyAllow = App.get().getOptions().isAutoreplyAllow;
        mIsAllowedAutoReply = isAutoreplyAllow;
        setAutoReplySettings(isAutoreplyAllow);
        subscibePaymentInfoUpdate();

        // Init settings views
        /*
         Hack for xiaomi. Maybe not only xiaomi. After finishing activity it keeps initializing it's fragments.
         In case of logout we haven't user settings to properly init some views. Because of this we need
         to abort views initialization if activity is finishing.
          */
        if (!getActivity().isFinishing()) {
            initViews();
        }
        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        RxUtils.safeUnsubscribe(mPaymentInfoSubscription);
    }

    private void subscibePaymentInfoUpdate() {
        mPaymentInfoSubscription = App.getAppComponent().appState()
                .getObservable(Options.class)
                .map(new Func1<Options, PaymentInfo>() {
                    @Override
                    public PaymentInfo call(Options options) {
                        return options.paymentNinjaInfo;
                    }
                })
                .distinctUntilChanged()
                .compose(RxUtils.<PaymentInfo>applySchedulers())
                .subscribe(new RxUtils.ShortSubscription<PaymentInfo>() {
                    @Override
                    public void onNext(PaymentInfo type) {
                        super.onNext(type);
                        if (type != null) {
                            initPurchases(type);
                        }
                    }
                });
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
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

    @SuppressWarnings("unused")
    @OnClick(R.id.autoReplyItem)
    public void setInversAutoReplySettings() {
        if (mAutoReplySettings != null) {
            setAutoReplySettings(!mAutoReplySettings.isChecked());
        }
    }

    private void autoReplySwitched(boolean state) {
        if (state != mIsAllowedAutoReply) {
            switchAutoReplyButton(state);
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
                mIsAllowedAutoReply = newValue;
                App.get().getOptions().isAutoreplyAllow = newValue;
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                setAutoReplySettings(!newValue);
            }
        }).exec();

    }

    private void initViews() {
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
        frame = mRootView.findViewById(R.id.loPreloadPhoto);
        ((TextView) frame.findViewWithTag("tvTitle")).setText(R.string.settings_loading_photo);
        preloadPhotoName = (TextView) frame.findViewWithTag("tvText");
        preloadPhotoName.setVisibility(View.VISIBLE);
        preloadPhotoName.setText(App.getUserConfig().getPreloadPhotoType().getName());
    }

    private void initPurchases(PaymentInfo info) {
        View frame = mRootView.findViewById(R.id.loPurchases);
        if (SomeExtensionsKt.isCradAvailable(info)) {
            frame.setVisibility(View.VISIBLE);
            ((TextView) frame.findViewWithTag("tvTitle")).setText(R.string.ninja_settings_toolbar);
            TextView text = (TextView) frame.findViewWithTag("tvText");
            text.setText(String.format(App.getCurrentLocale(), getString(R.string.payment_ninja_card_number), info.getLastDigits()));
            text.setVisibility(View.VISIBLE);
        } else {
            frame.setVisibility(View.GONE);
        }
    }

    private void initAccountViews() {
        ViewGroup frame = (ViewGroup) mRootView.findViewById(R.id.loAccount);
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

    @SuppressWarnings("unused")
    @OnClick(R.id.loPurchases)
    protected void showPurchasesScreen() {
        Intent intent = new Intent(App.getContext(), SettingsContainerActivity.class);
        startActivityForResult(intent, SettingsContainerActivity.INTENT_PURCHASES);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AuthorizationManager.RESULT_LOGOUT &&
                requestCode == SettingsContainerActivity.INTENT_ACCOUNT) {
            if (isAdded() && !(getActivity() instanceof NavigationActivity)) {
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
