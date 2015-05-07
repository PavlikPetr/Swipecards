package com.topface.topface.ui;

import android.content.Intent;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.experiments.FeedScreensIntent;
import com.topface.topface.ui.fragments.ChatFragment;

public class ChatActivity extends CheckAuthActivity<ChatFragment> {

    public static final int INTENT_CHAT = 3;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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

    public static Intent createIntent(int id, String nameAndAge, String city, String feedItemId, boolean fromGcm) {
        Intent intent = new Intent(App.getContext(), ChatActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_ID, id);
        intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, nameAndAge);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, city);
        if (!TextUtils.isEmpty(feedItemId)) {
            intent.putExtra(ChatFragment.INTENT_ITEM_ID, feedItemId);
        }
        if (fromGcm) {
            intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_CHAT);
        }
        return intent;
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        FeedScreensIntent.equipMessageAllIntent(intent);
        return intent;
    }
}
