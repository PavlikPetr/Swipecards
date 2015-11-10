package com.topface.topface.ui;

import android.content.Intent;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Photo;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.data.experiments.FeedScreensIntent;
import com.topface.topface.ui.fragments.ChatFragment;

public class ChatActivity extends CheckAuthActivity<ChatFragment> {

    public static final int REQUEST_CHAT = 3;
    public static final String LAST_MESSAGE = "com.topface.topface.ui.ChatActivity_last_message";
    public static final String LAST_MESSAGE_USER_ID = "com.topface.topface.ui.ChatActivity_last_message_user_id";

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected int getContentLayout() {
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

    //Если itemType соответствует популярному юзеру не показываем клаву в чате
    public static Intent createIntent(int id, String nameAndAge, String city, String feedItemId, Photo photo, boolean fromGcm, int itemType, String from) {
        return createIntent(id, nameAndAge, city, feedItemId, photo, fromGcm, from, null).putExtra(ChatFragment.USER_TYPE, itemType);
    }

    public static Intent createIntent(int id, String nameAndAge, String city, String feedItemId, Photo photo, boolean fromGcm, String from, SendGiftAnswer answer) {
        Intent intent = new Intent(App.getContext(), ChatActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_ID, id);
        intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, nameAndAge);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, city);
        intent.putExtra(ChatFragment.FROM, from);
        intent.putExtra(ChatFragment.GIFT_DATA, answer);
        if (!TextUtils.isEmpty(feedItemId)) {
            intent.putExtra(ChatFragment.INTENT_ITEM_ID, feedItemId);
        }
        if (fromGcm) {
            intent.putExtra(Static.INTENT_REQUEST_KEY, REQUEST_CHAT);
        }
        if (photo != null) {
            intent.putExtra(ChatFragment.INTENT_AVATAR, photo);
        }
        return intent;
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        FeedScreensIntent.equipMessageAllIntent(intent);
        return intent;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
