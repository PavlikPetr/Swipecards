package com.topface.topface.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.dialogs.AboutDialog;
import com.topface.topface.ui.dialogs.PreloadPhotoSelector;
import com.topface.topface.ui.dialogs.PreloadPhotoSelectorTypes;
import com.topface.topface.ui.fragments.profile.ProfileInnerFragment;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.MarketApiManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.cache.SearchCacheManager;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.Locale;

public class SettingsFragment extends ProfileInnerFragment implements OnClickListener {

    private TextView mSocialNameText;
    private MarketApiManager mMarketApiManager;

    private View mLoNotifications;

    private TextView preloadPhotoName;
    private ViewGroup mNoNotificationViewGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(R.layout.fragment_settings, null);

        mMarketApiManager = new MarketApiManager();

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
    protected String getTitle() {
        return getString(R.string.settings_header_title);
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
                            mMarketApiManager.onProblemResolve();
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
            case R.id.loFeedback:
                intent = new Intent(applicationContext, SettingsContainerActivity.class);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_FEEDBACK);
                break;
            case R.id.loAbout:
                FragmentManager fm = getChildFragmentManager();
                if (fm != null) {
                    AboutDialog.newInstance(App.getContext().getString(R.string.settings_about)).
                            show(fm, AboutDialog.class.getName());
                }
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
            case R.id.loNotifications:
                intent = new Intent(applicationContext, SettingsContainerActivity.class);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_NOTIFICATIONS);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
