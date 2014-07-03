package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.Static;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.fragments.ChatFragment;

public class ChatActivity extends UserOnlineActivity {

    public static final int INTENT_CHAT = 3;

    @Override
    protected Fragment createFragment() {
        return new ChatFragment();
    }

    public static Intent createIntent(Context context, FeedUser user) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_ID, user.id);
        intent.putExtra(ChatFragment.INTENT_USER_NAME, user.first_name);
        intent.putExtra(ChatFragment.INTENT_USER_SEX, user.sex);
        intent.putExtra(ChatFragment.INTENT_USER_AGE, user.age);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, user.city.name);
        return intent;
    }

    public static Intent createIntent(Context context, FeedUser user, String feedItemId) {
        Intent intent = createIntent(context, user);
        intent.putExtra(ChatFragment.INTENT_ITEM_ID, feedItemId);
        return intent;
    }

    public static Intent createIntent(Context context, Profile profile) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_ID, profile.uid);
        intent.putExtra(ChatFragment.INTENT_USER_NAME, profile.firstName != null ?
                profile.firstName : Static.EMPTY);
        intent.putExtra(ChatFragment.INTENT_USER_SEX, profile.sex);
        intent.putExtra(ChatFragment.INTENT_USER_AGE, profile.age);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, profile.city == null ? Static.EMPTY : profile.city.name);
        return intent;
    }

    public static Intent createIntent(Context context, GCMUtils.User user) {
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
