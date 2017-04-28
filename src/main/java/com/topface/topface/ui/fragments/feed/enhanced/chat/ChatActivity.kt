package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.data.experiments.FeedScreensIntent
import com.topface.topface.databinding.AcFragmentFrameBinding
import com.topface.topface.databinding.ToolbarViewBinding
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.chat.ChatComponent
import com.topface.topface.di.chat.ChatModule
import com.topface.topface.state.EventBus
import com.topface.topface.ui.CheckAuthActivity
import com.topface.topface.ui.dialogs.take_photo.TakePhotoActionHolder
import com.topface.topface.ui.dialogs.take_photo.TakePhotoPopup
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData
import com.topface.topface.ui.views.toolbar.view_models.BaseToolbarViewModel
import com.topface.topface.ui.views.toolbar.view_models.CustomTitleSubTitleToolbarViewModel
import com.topface.topface.utils.AddPhotoHelper
import com.topface.topface.utils.delegated_properties.objectArg
import com.topface.topface.utils.extensions.finishWithResult
import com.topface.topface.utils.extensions.goneIfEmpty
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscription
import javax.inject.Inject

class ChatActivity : CheckAuthActivity<ChatFragment, AcFragmentFrameBinding>() {

    companion object {
        const val REQUEST_CHAT = 3
        const val LAST_MESSAGE = "com.topface.topface.ui.ChatActivity_last_message"
        const val LAST_MESSAGE_USER_ID = "com.topface.topface.ui.ChatActivity_last_message_user_id"
        const val DISPATCHED_GIFTS = "com.topface.topface.ui.ChatActivity_dispatched_gifts"
    }

    @Inject lateinit var eventBus: EventBus
    @Inject lateinit var addPhotoHelper: AddPhotoHelper


    private var mTakePhotoSubscription: Subscription? = null

    override fun getToolbarBinding(binding: AcFragmentFrameBinding): ToolbarViewBinding = binding.toolbarInclude

    override fun getLayout() = R.layout.ac_fragment_frame

    override fun onCreate(savedInstanceState: Bundle?) {
        ComponentManager.obtainComponent(ChatComponent::class.java) {
            App.getAppComponent().add(ChatModule(this))
        }.inject(this)
        addPhotoHelper.setOnResultHandler(object : Handler() {
            override fun handleMessage(msg: Message) {
                AddPhotoHelper.handlePhotoMessage(msg)
            }
        })
        super.onCreate(savedInstanceState)
        mTakePhotoSubscription = eventBus.getObservable(TakePhotoActionHolder::class.java)
                .filter { it != null && it.action == TakePhotoPopup.ACTION_CANCEL }
                .subscribe({ finishWithResult(RESULT_CANCELED) }, { Debug.error("Take photo popup actions subscription catch error", it) })
        val user by objectArg<FeedUser>(ChatIntentCreator.WHOLE_USER)
        user?.let {
            onToolbarSettings(ToolbarSettingsData(it.nameAndAge,
                    it.city.name, isOnline = it.online))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        addPhotoHelper.processActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        ComponentManager.releaseComponent(ChatComponent::class.java)
        addPhotoHelper.releaseHelper()
        mTakePhotoSubscription.safeUnsubscribe()
        super.onDestroy()
    }

    override fun getFragmentTag(): String = ChatFragment::class.java.simpleName

    override fun createFragment() = ChatFragment().apply {
        arguments = intent.extras
    }

    override fun generateToolbarViewModel(toolbar: ToolbarViewBinding): BaseToolbarViewModel {
        return CustomTitleSubTitleToolbarViewModel(toolbar, this)
    }

    override fun getSupportParentActivityIntent() = super.getSupportParentActivityIntent().apply {
        FeedScreensIntent.equipMessageAllIntent(this)
    }

    override fun setToolbarSettings(settings: ToolbarSettingsData): Unit =
            with(getToolbarViewModel() as CustomTitleSubTitleToolbarViewModel) {
                extraViewModel.titleVisibility.set(settings.title.goneIfEmpty())
                extraViewModel.subTitleVisibility.set(settings.subtitle.goneIfEmpty())
                settings.title?.let {
                    extraViewModel.title.set(settings.title)
                }
                settings.subtitle?.let {
                    extraViewModel.subTitle.set(settings.subtitle)
                }
                settings.isOnline?.let {
                    extraViewModel.isOnline.set(settings.isOnline)
                }
                settings.icon?.let {
                    upIcon.set(settings.icon)
                }
            }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}