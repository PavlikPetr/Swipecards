package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.gcmutils.GCMUtils;

public class ChatActivity extends CheckAuthActivity<ChatFragment> {

    public static final int INTENT_CHAT = 3;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

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

        @SuppressWarnings("UnusedDeclaration")
        public IntentBuilder gcmUser(GCMUtils.User gcmUser) {
            this.gcmUser = gcmUser;
            profile = null;
            feedUser = null;
            return this;
        }

        @SuppressWarnings("UnusedDeclaration")
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
                intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, feedUser.getNameAndAge());
                intent.putExtra(ChatFragment.INTENT_USER_SEX, feedUser.sex);
                intent.putExtra(ChatFragment.INTENT_USER_CITY, feedUser.city.name);
            } else if (profile != null) {
                intent.putExtra(ChatFragment.INTENT_USER_ID, profile.uid);
                intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, profile.getNameAndAge());
                intent.putExtra(ChatFragment.INTENT_USER_SEX, profile.sex);
                intent.putExtra(ChatFragment.INTENT_USER_CITY, profile.city == null ? Static.EMPTY : profile.city.name);
            } else if (gcmUser != null) {
                intent.putExtra(ChatFragment.INTENT_USER_ID, gcmUser.id);
                intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, gcmUser.getNameAndAge());
                intent.putExtra(ChatFragment.INTENT_USER_SEX, gcmUser.sex);
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
    protected int getContentViewId() {
        // the fragment frame layout _without_ background definition
        return R.layout.ac_fragment_frame_no_background;
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
        intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, user.getNameAndAge());
        intent.putExtra(ChatFragment.INTENT_USER_SEX, user.sex);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, user.city.name);
        intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, user.getNameAndAge());
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
        intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, profile.getNameAndAge());
        intent.putExtra(ChatFragment.INTENT_USER_SEX, profile.sex);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, profile.city == null ? Static.EMPTY : profile.city.name);
        return intent;
    }

    public static Intent createIntent(Context context, GCMUtils.User user) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_ID, user.id);
        intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, user.getNameAndAge());
        intent.putExtra(ChatFragment.INTENT_USER_SEX, user.sex);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, user.city);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_CHAT);
        return intent;
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        CacheProfile.getOptions().messagesWithTabs.equipNavigationActivityIntent(intent);
        return intent;
    }
}
