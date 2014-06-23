package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.fragments.ChatFragment;

public class ChatActivity extends CustomTitlesBaseFragmentActivity implements IUserOnlineListener {

    private View mOnlineIcon;

    public static final int INTENT_CHAT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_chat);
    }

    @Override
    protected void initCustomActionBarView(View mCustomView) {
        mOnlineIcon = mCustomView.findViewById(R.id.online);
    }

    @Override
    protected int getActionBarCustomViewResId() {
        return R.layout.actionbar_container_title_view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().findFragmentById(R.id.chat_fragment).onOptionsItemSelected(item);
                if (isTaskRoot()) {
                    Intent i = new Intent(this, NavigationActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    onBackPressed();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setUserOnline(boolean online) {
        if (mOnlineIcon != null) {
            mOnlineIcon.setVisibility(online ? View.VISIBLE : View.GONE);
        }
    }

    public static Intent getChatIntent(Context context, FeedUser user) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_ID, user.id);
        intent.putExtra(ChatFragment.INTENT_USER_NAME, user.first_name);
        intent.putExtra(ChatFragment.INTENT_USER_SEX, user.sex);
        intent.putExtra(ChatFragment.INTENT_USER_AGE, user.age);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, user.city.name);
        return intent;
    }

    public static Intent getChatIntent(Context context, FeedUser user, String feedItemId) {
        Intent intent = getChatIntent(context, user);
        intent.putExtra(ChatFragment.INTENT_ITEM_ID, feedItemId);
        return intent;
    }

    public static Intent getChatIntent(Context context, Profile profile) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_ID, profile.uid);
        intent.putExtra(ChatFragment.INTENT_USER_NAME, profile.firstName != null ?
                profile.firstName : Static.EMPTY);
        intent.putExtra(ChatFragment.INTENT_USER_SEX, profile.sex);
        intent.putExtra(ChatFragment.INTENT_USER_AGE, profile.age);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, profile.city == null ? Static.EMPTY : profile.city.name);
        return intent;
    }

    public static Intent getChatIntent(Context context, GCMUtils.User user) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_ID, user.id);
        intent.putExtra(ChatFragment.INTENT_USER_NAME, user.name);
        intent.putExtra(ChatFragment.INTENT_USER_SEX, user.sex);
        intent.putExtra(ChatFragment.INTENT_USER_AGE, user.age);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, user.city);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_CHAT);
        return intent;
    }
}
