package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.topface.topface.App
import com.topface.topface.BR
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Profile
import com.topface.topface.databinding.NewChatFragmentBinding
import com.topface.topface.databinding.NewChatToolbarAvatarBinding
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.chat.ChatComponent
import com.topface.topface.di.chat.ChatViewModelComponent
import com.topface.topface.di.chat.DaggerChatViewModelComponent
import com.topface.topface.ui.fragments.feed.enhanced.base.IViewModelLifeCycle
import com.topface.topface.ui.fragments.feed.enhanced.base.setViewModel
import com.topface.topface.ui.fragments.feed.enhanced.utils.DaggerFragment
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.views.ImageViewRemote
import com.topface.topface.utils.actionbar.OverflowMenu
import com.topface.topface.utils.actionbar.OverflowMenuUser
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

class ChatFragment : DaggerFragment() {

    @Inject lateinit var adapter: CompositeAdapter
    @Inject lateinit var navigator: FeedNavigator
    @Inject lateinit var chatToolbarAvatarModel: ChatToolbarAvatarModel

    private val mBinding: NewChatFragmentBinding by lazy {
        DataBindingUtil.inflate<NewChatFragmentBinding>(context.applicationContext.layoutInflater, R.layout.new_chat_fragment, null, false)
    }
    private val mViewModel by lazy {
        ComponentManager.obtainComponent(ChatViewModelComponent::class.java) {
            DaggerChatViewModelComponent.builder().appComponent(App.getAppComponent()).build()
        }.chatViewModel().apply {
            navigator = this@ChatFragment.navigator
        }
    }
    private var mOverflowMenu: OverflowMenu? = null
    private var mBarAvatar: MenuItem? = null
    private var mUser: FeedUser? = null

    override fun getViewModel(): IViewModelLifeCycle = mViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUser = arguments?.getParcelable(ChatIntentCreator.WHOLE_USER)
        ComponentManager.obtainComponent(ChatComponent::class.java).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return mBinding.apply {
            chat.layoutManager = LinearLayoutManager(context.applicationContext)
            chat.adapter = adapter
            setViewModel(BR.chatViewModel, mViewModel, arguments)
        }.root
    }

    override fun terminateImmortalComponent() {
        ComponentManager.releaseComponent(ChatViewModelComponent::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.releaseComponents()
    }

    override fun onDestroy() {
        super.onDestroy()
        mOverflowMenu?.onReleaseOverflowMenu()
    }

    override fun getOptionsMenuRes() = R.menu.toolbar_avatar_and_menu

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        val item = menu?.findItem(R.id.action_profile)
        if (item != null && mBarAvatar != null) {
            item.isChecked = mBarAvatar!!.isChecked
        }
        mBarAvatar = item
        mOverflowMenu = OverflowMenu(this, menu).apply {
            initOverflowMenuActions(this)
        }
        val user = mUser
        if (user != null && !user.banned) {
            setActionBarAvatar(user)
        }
    }


    fun setActionBarAvatar(user: FeedUser) = mBarAvatar?.let {
        if (user.isEmpty || user.banned || user.deleted || user.photo.isEmpty) {
            showStubAvatar(it)
        } else {
            val view = MenuItemCompat.getActionView(it)
                    .findViewById(R.id.toolbar_avatar_root)
            DataBindingUtil.bind<NewChatToolbarAvatarBinding>(view).viewModel = chatToolbarAvatarModel
        }
    }

    fun showStubAvatar(menuItem: MenuItem) {
        (MenuItemCompat.getActionView(menuItem)
                .findViewById(R.id.ivBarAvatar) as ImageViewRemote)
                .setImageResource(if (mUser?.sex == Profile.GIRL)
                    R.drawable.rounded_avatar_female
                else
                    R.drawable.rounded_avatar_male)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        mOverflowMenu?.onMenuClicked(item)
        return super.onOptionsItemSelected(item)
    }

    fun initOverflowMenuActions(overflowMenu: OverflowMenu) {
        if (overflowMenu.overflowMenuFieldsListener == null) {
            overflowMenu.overflowMenuFieldsListener = object : OverflowMenuUser {
                override fun setBlackListValue(value: Boolean?) {
                    mUser?.let {
                        it.inBlacklist = value ?: !it.inBlacklist
                    }
                }

                override fun setBookmarkValue(value: Boolean?) {
                    mUser?.let {
                        it.bookmarked = value ?: !it.bookmarked
                    }
                }

                override fun getBlackListValue() = mUser?.inBlacklist
                override fun getBookmarkValue() = mUser?.bookmarked
                override fun isOpenChatAvailable() = true
                override fun isAddToFavoritsAvailable() = true
                override fun getUserId() = mUser?.id
                override fun getProfileId() = mUser?.id
                override fun getOpenChatIntent() = null
                override fun isMutual() = null
                override fun getSympathySentValue() = null
                override fun isBanned() = null
                override fun clickSendGift() {}
                override fun setSympathySentValue(value: Boolean?) {}
            }
        }
        overflowMenu.initOverfowMenu()
    }

}