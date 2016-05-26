package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.data.experiments.FeedScreensIntent;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.dialogs.TakePhotoPopup;
import com.topface.topface.ui.fragments.ChatFragment;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class ChatActivity extends CheckAuthActivity<ChatFragment> {

    public static final int REQUEST_CHAT = 3;
    public static final String LAST_MESSAGE = "com.topface.topface.ui.ChatActivity_last_message";
    public static final String LAST_MESSAGE_USER_ID = "com.topface.topface.ui.ChatActivity_last_message_user_id";

    @Inject
    TopfaceAppState mAppState;
    private Subscription mTakePhotoSubscription;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        App.get().inject(this);
        mTakePhotoSubscription = mAppState.getObservable(TakePhotoPopup.TakePhotoActionHolder.class)
                .filter(new Func1<TakePhotoPopup.TakePhotoActionHolder, Boolean>() {
                    @Override
                    public Boolean call(TakePhotoPopup.TakePhotoActionHolder takePhotoActionHolder) {
                        return takePhotoActionHolder != null && takePhotoActionHolder.getAction() == TakePhotoPopup.ACTION_CANCEL;
                    }
                })
                .subscribe(new Action1<TakePhotoPopup.TakePhotoActionHolder>() {
                    @Override
                    public void call(TakePhotoPopup.TakePhotoActionHolder takePhotoActionHolder) {
                        finishWithResult(Activity.RESULT_CANCELED);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void finishWithResult(int resultCode) {
        setResult(resultCode);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTakePhotoSubscription != null && !mTakePhotoSubscription.isUnsubscribed()) {
            mTakePhotoSubscription.unsubscribe();
        }
    }

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
    public static Intent createIntent(int id, int sex, String nameAndAge, String city, String feedItemId, Photo photo, boolean fromGcm, int itemType, boolean isBanned) {
        return createIntent(id, sex, nameAndAge, city, feedItemId, photo, fromGcm, null, isBanned).putExtra(ChatFragment.USER_TYPE, itemType);
    }

    public static Intent createIntent(int id, int sex, String nameAndAge, String city, String feedItemId, Photo photo, boolean fromGcm, SendGiftAnswer answer, boolean isBanned) {
        Intent intent = new Intent(App.getContext(), ChatActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_ID, id);
        intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, nameAndAge);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, city);
        intent.putExtra(ChatFragment.GIFT_DATA, answer);
        intent.putExtra(ChatFragment.SEX, sex);
        intent.putExtra(ChatFragment.BANNED_USER, isBanned);
        if (!TextUtils.isEmpty(feedItemId)) {
            intent.putExtra(ChatFragment.INTENT_ITEM_ID, feedItemId);
        }
        if (fromGcm) {
            intent.putExtra(App.INTENT_REQUEST_KEY, REQUEST_CHAT);
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
