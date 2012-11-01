package com.topface.topface.ui.fragments;

import java.util.LinkedList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.topface.topface.requests.OptionsRequest;
import com.topface.topface.requests.SendMailNotificationsRequest;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.settings.SettingsAccountFragment;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.utils.NavigationBarController;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.social.AuthToken;

public class SettingsFragment extends BaseFragment implements OnClickListener, OnCheckedChangeListener {

    private Settings mSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(R.layout.ac_settings, null);

        mSettings = Settings.getInstance();

        // Navigation bar
        mNavBarController = new NavigationBarController((ViewGroup)view.findViewById(R.id.loNavigationBar));
        (view.findViewById(R.id.btnNavigationHome)).setOnClickListener((NavigationActivity) getActivity());
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(R.string.settings_header_title);

        // Init settings views
        initViews(view);

        return view;
    }

    interface Method {
    	void onExecute(String key, boolean value);
    }
    
    private void initViews(View root) {
        ViewGroup frame;

        OptionsRequest request = new OptionsRequest(getActivity().getApplicationContext());
        final LinkedList<Method> methodsList = new LinkedList<SettingsFragment.Method>();
        ApiHandler handler = new ApiHandler() {
			
			@Override
			public void success(ApiResponse response) throws NullPointerException {
				final Options options = Options.parse(response);
				if (options.hasMail) {
					getActivity().runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							for (Method method : methodsList) {
								if(options.pages.containsKey(Options.PAGE_LIKES))
									method.onExecute(Settings.SETTINGS_C2DM_LIKES_EMAIL, options.pages.get(Options.PAGE_LIKES).mail);
								if(options.pages.containsKey(Options.PAGE_MUTUAL))
									method.onExecute(Settings.SETTINGS_C2DM_MUTUAL_EMAIL, options.pages.get(Options.PAGE_MUTUAL).mail);
								if(options.pages.containsKey(Options.PAGE_DIALOGS))
									method.onExecute(Settings.SETTINGS_C2DM_DIALOGS_EMAIL, options.pages.get(Options.PAGE_DIALOGS).mail);
								if(options.pages.containsKey(Options.PAGE_VISITORS))
									method.onExecute(Settings.SETTINGS_C2DM_VISITORS_EMAIL, options.pages.get(Options.PAGE_VISITORS).mail);
							}
						}
					});					
				}				
			}
			
			@Override
			public void fail(int codeError, ApiResponse response) throws NullPointerException {
				
			}
		};
        request.callback(handler);
        
        // Notifications header
        frame = (ViewGroup) root.findViewById(R.id.loNotificationsHeader);
        setText(R.string.settings_notifications_header, frame);

        // Likes
        frame = (ViewGroup) root.findViewById(R.id.loLikes);
        setBackground(R.drawable.edit_big_btn_top, frame);
        setText(R.string.settings_likes, frame);
        methodsList.add(initEditNotificationFrame(Settings.SETTINGS_C2DM_LIKES_PHONE,
                Settings.SETTINGS_C2DM_LIKES_EMAIL, frame));

        // Mutual
        frame = (ViewGroup) root.findViewById(R.id.loMutual);
        setBackground(R.drawable.edit_big_btn_middle, frame);
        setText(R.string.settings_mutual, frame);
        methodsList.add(initEditNotificationFrame(Settings.SETTINGS_C2DM_MUTUAL_PHONE,
                Settings.SETTINGS_C2DM_MUTUAL_EMAIL, frame));

        // Chat
        frame = (ViewGroup) root.findViewById(R.id.loChat);
        setBackground(R.drawable.edit_big_btn_middle, frame);
        setText(R.string.settings_messages, frame);
        methodsList.add(initEditNotificationFrame(Settings.SETTINGS_C2DM_DIALOGS_PHONE,
                Settings.SETTINGS_C2DM_DIALOGS_EMAIL, frame));

        // Guests
        frame = (ViewGroup) root.findViewById(R.id.loGuests);
        setBackground(R.drawable.edit_big_btn_bottom, frame);
        setText(R.string.settings_guests, frame);
        methodsList.add(initEditNotificationFrame(Settings.SETTINGS_C2DM_VISITORS_PHONE,
                Settings.SETTINGS_C2DM_VISITORS_EMAIL, frame));

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
        
        request.exec();
    }
    
    private void setText(int titleId, ViewGroup frame) {
        ((TextView) frame.findViewById(R.id.tvTitle)).setText(titleId);
    }

    private void setAccountNameText(int titleId, String text, int iconRes, ViewGroup frame) {
        ((TextView) frame.findViewById(R.id.tvTitle)).setText(titleId);
        final TextView textView = (TextView) frame.findViewById(R.id.tvText);
        textView.setVisibility(View.VISIBLE);
        if (text.isEmpty()) {
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

    private Method initEditNotificationFrame(String phoneKey,final String emailKey, ViewGroup frame) {
        CheckBox checkBox = (CheckBox) frame.findViewById(R.id.cbPhone);
        checkBox.setTag(phoneKey);
        checkBox.setChecked(mSettings.getSetting(phoneKey));
        checkBox.setOnCheckedChangeListener(this);

        final CheckBox checkBoxEmail = (CheckBox) frame.findViewById(R.id.cbMail);
        checkBoxEmail.setTag(emailKey);
        checkBoxEmail.setChecked(false);
        checkBoxEmail.setEnabled(false);
        checkBoxEmail.setOnCheckedChangeListener(this);     
        
        return new Method() {
			
			@Override
			public void onExecute(String key, boolean value) {
				if (emailKey.equals(key)) {
					checkBoxEmail.setEnabled(true);
					checkBoxEmail.setChecked(value);
				}
			}
		};
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
                intent.setData(Uri.parse(getResources().getString(R.string.app_help_url)));
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
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView,final boolean isChecked) {
        final String key = (String) buttonView.getTag();        
        
        SendMailNotificationsRequest request = mSettings.getMailNotificationRequest(key,isChecked,getActivity().getApplicationContext());
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
					
					mSettings.setSetting(key, isChecked);
				}
				
				@Override
				public void fail(int codeError, ApiResponse response) throws NullPointerException {
					buttonView.post(new Runnable() {
						
						@Override
						public void run() {
							buttonView.setEnabled(true);
						}
					});
					mSettings.setSetting(key, !isChecked);
				}
			}).exec();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == SettingsAccountFragment.RESULT_LOGOUT &&
                requestCode == SettingsContainerActivity.INTENT_ACCOUNT) {
            getActivity().finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
