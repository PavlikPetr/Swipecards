package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.NewChatFragmentBinding
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.chat.ChatComponent
import com.topface.topface.di.chat.ChatModule
import com.topface.topface.di.chat.ChatViewModelComponent
import com.topface.topface.di.chat.DaggerChatViewModelComponent
import com.topface.topface.di.navigation_activity.NavigationActivityComponent
import com.topface.topface.ui.fragments.feed.enhanced.base.IViewModelLifeCycle
import com.topface.topface.ui.fragments.feed.enhanced.utils.DaggerFragment
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

class ChatFragment : DaggerFragment() {

    companion object {
        const val USER_TYPE = "type"
        const val INTENT_AVATAR = "user_avatar"
        const val INTENT_USER_ID = "user_id"
        const val INTENT_USER_CITY = "user_city"
        const val INTENT_USER_NAME_AND_AGE = "user_name_and_age"
        const val INTENT_ITEM_ID = "item_id"
        const val GIFT_DATA = "gift_data"
        const val BANNED_USER = "banned_user"
        const val SEX = "sex"
    }

    @Inject lateinit var adapter: CompositeAdapter

    private val mBinding: NewChatFragmentBinding by lazy {
        DataBindingUtil.inflate<NewChatFragmentBinding>(context.layoutInflater, R.layout.new_chat_fragment, null, false)
    }
    private val mViewModel by lazy {
        ComponentManager.obtainComponent(ChatViewModelComponent::class.java) {
            DaggerChatViewModelComponent.builder().build()
        }.chatViewModel()
    }

    override fun getViewModel(): IViewModelLifeCycle = mViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ComponentManager.obtainComponent(ChatComponent::class.java) {
            ComponentManager.obtainComponent(NavigationActivityComponent::class.java)
                    .add(ChatModule()).apply {
                inject(this@ChatFragment)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return mBinding.apply {
            chat.layoutManager = LinearLayoutManager(context)
            chat.adapter = adapter
            viewModel = mViewModel
        }.root
    }

    override fun terminateImmortalComponent() {
        ComponentManager.releaseComponent(ChatComponent::class.java)
    }

    override fun onDestroyView() {
        ComponentManager.releaseComponent(ChatComponent::class.java)
        super.onDestroyView()
    }
}