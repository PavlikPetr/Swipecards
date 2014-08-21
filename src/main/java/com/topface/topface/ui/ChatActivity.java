package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;

import com.topface.topface.Static;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.utils.gcmutils.GCMUtils;

public class ChatActivity extends UserOnlineActivity<ChatFragment> {

    public static final int INTENT_CHAT = 3;

    public static class IntentBuilder {
        private Context context;
        private FeedUser feedUser;
        private Profile profile;
        private GCMUtils.User gcmUser;
        private String feedItemId;
        private String initialMessage;

        public IntentBuilder(Context context) {
            this.context = context;
        }

        public IntentBuilder feedUser(FeedUser feedUser) {
            this.feedUser = feedUser;
            profile = null;
            gcmUser = null;
            return this;
        }

        public IntentBuilder profile(Profile profile) {
            this.profile = profile;
            feedUser = null;
            gcmUser = null;
            return this;
        }

        public IntentBuilder gcmUser(GCMUtils.User gcmUser) {
            this.gcmUser = gcmUser;
            profile = null;
            feedUser = null;
            return this;
        }

        public IntentBuilder feedIdItem(String feedItemId) {
            this.feedItemId = feedItemId;
            return this;
        }

        public IntentBuilder initialMessage(String initialMessage) {
            this.initialMessage = initialMessage;
            return this;
        }

        public Intent build() {
            Intent intent = new Intent(context, ChatActivity.class);
            if (feedUser != null) {
                intent.putExtra(ChatFragment.INTENT_USER_ID, feedUser.id);
                intent.putExtra(ChatFragment.INTENT_USER_NAME, feedUser.first_name);
                intent.putExtra(ChatFragment.INTENT_USER_SEX, feedUser.sex);
                intent.putExtra(ChatFragment.INTENT_USER_AGE, feedUser.age);
                intent.putExtra(ChatFragment.INTENT_USER_CITY, feedUser.city.name);
            } else if (profile != null) {
                intent.putExtra(ChatFragment.INTENT_USER_ID, profile.uid);
                intent.putExtra(ChatFragment.INTENT_USER_NAME, profile.firstName != null ?
                        profile.firstName : Static.EMPTY);
                intent.putExtra(ChatFragment.INTENT_USER_SEX, profile.sex);
                intent.putExtra(ChatFragment.INTENT_USER_AGE, profile.age);
                intent.putExtra(ChatFragment.INTENT_USER_CITY, profile.city == null ? Static.EMPTY : profile.city.name);
            } else if (gcmUser != null) {
                intent.putExtra(ChatFragment.INTENT_USER_ID, gcmUser.id);
                intent.putExtra(ChatFragment.INTENT_USER_NAME, gcmUser.name);
                intent.putExtra(ChatFragment.INTENT_USER_SEX, gcmUser.sex);
                intent.putExtra(ChatFragment.INTENT_USER_AGE, gcmUser.age);
                intent.putExtra(ChatFragment.INTENT_USER_CITY, gcmUser.city);
                intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_CHAT);
            }
            if (feedItemId != null) {
                intent.putExtra(ChatFragment.INTENT_ITEM_ID, feedItemId);
            }
            if (initialMessage != null) {
                intent.putExtra(ChatFragment.INITIAL_MESSAGE, initialMessage);
            }
            return intent;
        }
    }

    @Override
    protected String getFragmentTag() {
        return ChatFragment.class.getSimpleName();
    }

    @Override
    protected ChatFragment createFragment() {
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
