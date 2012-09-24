package com.topface.topface.ui.fragments;

import com.topface.topface.R;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.social.AuthToken;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingsFragment extends BaseFragment implements OnClickListener, OnCheckedChangeListener{

	private Settings mSettings;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View root = inflater.inflate(R.layout.ac_settings, null);  

        mSettings = Settings.getInstance();
        
        // Navigation bar
        (root.findViewById(R.id.btnNavigationHome)).setOnClickListener((NavigationActivity)getActivity());
        ((TextView)root.findViewById(R.id.tvNavigationTitle)).setText(R.string.settings_header_title);
		
        // Init settings views
        initViews(root);
        
        return root;
    }

    private void initViews(View root) {
    	ViewGroup frame;
    	
    	// Notifications header
    	frame = (ViewGroup) root.findViewById(R.id.loNotificationsHeader);
    	setText(R.string.settings_notifications_header, frame);
    	
    	// Likes
    	frame = (ViewGroup) root.findViewById(R.id.loLikes);
    	setBackground(R.drawable.edit_big_btn_top, frame);
    	setText(R.string.settings_likes, frame);    	
    	initEditNotificationFrame(Settings.SETTINGS_C2DM_LIKES_PHONE, 
    			Settings.SETTINGS_C2DM_LIKES_EMAIL, frame);    	
    	
    	// Mutual
    	frame = (ViewGroup) root.findViewById(R.id.loMutual);
    	setBackground(R.drawable.edit_big_btn_middle, frame);
    	setText(R.string.settings_mutual, frame);
    	initEditNotificationFrame(Settings.SETTINGS_C2DM_MUTUAL_PHONE, 
    			Settings.SETTINGS_C2DM_MUTUAL_EMAIL, frame);
    	
    	// Chat
    	frame = (ViewGroup) root.findViewById(R.id.loChat);
    	setBackground(R.drawable.edit_big_btn_middle, frame);
    	setText(R.string.settings_messages, frame);
    	initEditNotificationFrame(Settings.SETTINGS_C2DM_MESSAGES_PHONE, 
    			Settings.SETTINGS_C2DM_MESSAGES_EMAIL, frame);
    	
    	// Guests
    	frame = (ViewGroup) root.findViewById(R.id.loGuests);
    	setBackground(R.drawable.edit_big_btn_bottom, frame);
    	setText(R.string.settings_guests, frame);
    	initEditNotificationFrame(Settings.SETTINGS_C2DM_GUESTS_PHONE, 
    			Settings.SETTINGS_C2DM_GUESTS_EMAIL, frame);
    	
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
			setText(R.string.settings_account, mSettings.getSocialAccountName(), R.drawable.ic_fb, frame);
		} else if (authToken.getSocialNet().equals(AuthToken.SN_VKONTAKTE)){
			setText(R.string.settings_account, mSettings.getSocialAccountName(), R.drawable.ic_vk, frame);
		} else {
			setText(R.string.settings_account, frame);
		}
    	frame.setOnClickListener(this);
    	
    	// Rate app
    	frame = (ViewGroup) root.findViewById(R.id.loFeedback);
    	setBackground(R.drawable.edit_big_btn_middle_selector, frame);
    	setText(R.string.settings_rate_app, frame);
    	frame.setOnClickListener(this);
    	
    	// About
    	frame = (ViewGroup) root.findViewById(R.id.loAbout);
    	setBackground(R.drawable.edit_big_btn_bottom_selector, frame);
    	setText(R.string.settings_about, frame);
    	frame.setOnClickListener(this);
    }
    
    private void setText(int titleId, ViewGroup frame) {
    	((TextView)frame.findViewById(R.id.tvTitle)).setText(titleId);
    }
    
    private void setText(int titleId, String text, int iconRes, ViewGroup frame) {
    	((TextView)frame.findViewById(R.id.tvTitle)).setText(titleId);
    	TextView textView = (TextView)frame.findViewById(R.id.tvText); 
    	textView.setVisibility(View.VISIBLE);
    	textView.setText(text);
    	textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(iconRes), null, null, null);
    }
    
    private void initEditNotificationFrame(String phoneKey, String emailKey, ViewGroup frame) {
    	CheckBox checkBox = (CheckBox)frame.findViewById(R.id.cbPhone); 
    	checkBox.setTag(phoneKey);
    	checkBox.setChecked(mSettings.getSetting(phoneKey));
    	checkBox.setOnCheckedChangeListener(this);
    	
    	checkBox = (CheckBox)frame.findViewById(R.id.cbMail); 
    	checkBox.setTag(emailKey);
    	checkBox.setChecked(mSettings.getSetting(emailKey));
    	checkBox.setOnCheckedChangeListener(this);
    }
    
    private void setBackground(int resId, ViewGroup frame) {
		ImageView background = (ImageView) frame.findViewById(R.id.ivEditBackground);
		background.setImageResource(resId);
	}
    
	@Override
	public void fillLayout() { }

	@Override
	public void clearLayout() { }

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
			startActivityForResult(intent,SettingsContainerActivity.INTENT_ACCOUNT);
			break;
		case R.id.loFeedback:
			intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);			
			startActivityForResult(intent,SettingsContainerActivity.INTENT_FEEDBACK);
			break;
		case R.id.loAbout:
			intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);			
			startActivityForResult(intent,SettingsContainerActivity.INTENT_ABOUT);
			break;
		default:
			break;
		}		
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		String key = (String) buttonView.getTag();
		mSettings.setSetting(key, isChecked);
	}
}
