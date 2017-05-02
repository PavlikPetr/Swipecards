package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.experiments.FeedScreensIntent;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarViewBinding;
import com.topface.topface.state.EventBus;
import com.topface.topface.ui.dialogs.take_photo.TakePhotoActionHolder;
import com.topface.topface.ui.dialogs.take_photo.TakePhotoPopup;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;
import com.topface.topface.ui.views.toolbar.view_models.BaseToolbarViewModel;
import com.topface.topface.ui.views.toolbar.view_models.CustomTitleSubTitleToolbarViewModel;

import org.jetbrains.annotations.NotNull;

import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class ChatActivity extends CheckAuthActivity<ChatFragment, AcFragmentFrameBinding> {

    public static final int REQUEST_CHAT = 3;
    public static final String LAST_MESSAGE = "com.topface.topface.ui.ChatActivity_last_message";
    public static final String LAST_MESSAGE_USER_ID = "com.topface.topface.ui.ChatActivity_last_message_user_id";
    public static final String DISPATCHED_GIFTS = "com.topface.topface.ui.ChatActivity_dispatched_gifts";

    private EventBus mEventBus;
    private Subscription mTakePhotoSubscription;

    @NotNull
    @Override
    public ToolbarViewBinding getToolbarBinding(@NotNull AcFragmentFrameBinding binding) {
        return binding.toolbarInclude;
    }

    @Override
    public int getLayout() {
        return R.layout.ac_fragment_frame;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventBus = App.getAppComponent().eventBus();
        mTakePhotoSubscription = mEventBus.getObservable(TakePhotoActionHolder.class)
                .filter(new Func1<TakePhotoActionHolder, Boolean>() {
                    @Override
                    public Boolean call(TakePhotoActionHolder takePhotoActionHolder) {
                        return takePhotoActionHolder != null && takePhotoActionHolder.getAction() == TakePhotoPopup.ACTION_CANCEL;
                    }
                })
                .subscribe(new Action1<TakePhotoActionHolder>() {
                    @Override
                    public void call(TakePhotoActionHolder takePhotoActionHolder) {
                        finishWithResult(Activity.RESULT_CANCELED);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Debug.error("Take photo popup actions subscription catch error", throwable);
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
    protected String getFragmentTag() {
        return ChatFragment.class.getSimpleName();
    }

    @Override
    protected ChatFragment createFragment() {
        return new ChatFragment();
    }

    @NotNull
    @Override
    protected BaseToolbarViewModel generateToolbarViewModel(@NotNull ToolbarViewBinding toolbar) {
        return new CustomTitleSubTitleToolbarViewModel(toolbar, this);
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        FeedScreensIntent.equipMessageAllIntent(intent);
        return intent;
    }

    @Override
    public void setToolbarSettings(@NotNull ToolbarSettingsData settings) {
        CustomTitleSubTitleToolbarViewModel toolbarViewModel = (CustomTitleSubTitleToolbarViewModel) getToolbarViewModel();
        toolbarViewModel.getExtraViewModel().getTitleVisibility().set(TextUtils.isEmpty(settings.getTitle()) ? View.GONE : View.VISIBLE);
        toolbarViewModel.getExtraViewModel().getSubTitleVisibility().set(TextUtils.isEmpty(settings.getSubtitle()) ? View.GONE : View.VISIBLE);
        if (settings.getTitle() != null) {
            toolbarViewModel.getExtraViewModel().getTitle().set(settings.getTitle());
        }
        if (settings.getSubtitle() != null) {
            toolbarViewModel.getExtraViewModel().getSubTitle().set(settings.getSubtitle());
        }
        if (settings.isOnline() != null) {
            //noinspection ConstantConditions
            toolbarViewModel.getExtraViewModel().isOnline().set(settings.isOnline());
        }
        if (settings.getIcon() != null) {
            toolbarViewModel.getUpIcon().set(settings.getIcon());
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
