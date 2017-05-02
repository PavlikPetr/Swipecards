package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.content.res.Configuration
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
import com.topface.topface.ui.fragments.ToolbarActivity
import com.topface.topface.ui.fragments.feed.enhanced.base.IViewModelLifeCycle
import com.topface.topface.ui.fragments.feed.enhanced.base.setViewModel
import com.topface.topface.ui.fragments.feed.enhanced.utils.DaggerFragment
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.views.ImageViewRemote
import com.topface.topface.ui.views.KeyboardListenerLayout
import com.topface.topface.utils.Device
import com.topface.topface.utils.Utils
import com.topface.topface.utils.actionbar.OverflowMenu
import com.topface.topface.utils.actionbar.OverflowMenuUser
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

class ChatFragment : DaggerFragment(), KeyboardListenerLayout.KeyboardListener {

    companion object {
        private const val SOFT_KEYBOARD_LOCK_STATE = "keyboard_state"
    }

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
    private var mKeyboardWasShown = false // по умолчанию клава в чате закрыта

    override fun getViewModel(): IViewModelLifeCycle = mViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mKeyboardWasShown = savedInstanceState?.getBoolean(SOFT_KEYBOARD_LOCK_STATE) ?: false
        mUser = arguments?.getParcelable(ChatIntentCreator.WHOLE_USER)
        ComponentManager.obtainComponent(ChatComponent::class.java).inject(this)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean(SOFT_KEYBOARD_LOCK_STATE, mKeyboardWasShown)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return mBinding.apply {
            chat.layoutManager = LinearLayoutManager(context.applicationContext)
            chat.adapter = adapter
            setViewModel(BR.chatViewModel, mViewModel, arguments)
            root.setKeyboardListener(this@ChatFragment)
        }.root
    }

    override fun terminateImmortalComponent() {
        ComponentManager.releaseComponent(ChatViewModelComponent::class.java)
    }

    override fun onResume() {
        super.onResume()
        //показать клавиатуру, если она была показаны до этого(перешли в другой фрагмент, и вернулись обратно)
        showKeyboard()
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

    //Фокусы с клавиатурой! Это та еще адуха, пусть будет тут. Дабы не захламлять VM
    override fun keyboardOpened() {
        mKeyboardWasShown = true
        if (isAdded && getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            setActionbarVisibility(false)
        }
    }

    override fun keyboardClosed() {
        mKeyboardWasShown = false
        if (isAdded) {
            setActionbarVisibility(true)
        }
    }

    override fun keyboardChangeState() {
        if (isAdded && getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            setActionbarVisibility(true)
        }
    }

    private fun showKeyboard() {
        if (mBinding.input != null && mKeyboardWasShown) {
            mBinding.input.postDelayed({
                //грязный хак, чтоб отработал InputMethodManager. иначе не клава не отрабатывает и не открвается.
                if (isShowKeyboardInChat()) {
                    Utils.showSoftKeyboard(activity.applicationContext, mBinding.input)
                }
            }, 200)
        }
    }

    private fun isShowKeyboardInChat() = if (isAdded) {
        val context = activity.applicationContext
        val displayMetrics = Device.getDisplayMetrics(context)
        val height = if (getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT)
            displayMetrics.heightPixels
        else
            displayMetrics.widthPixels
        val dpHeight = height / displayMetrics.density
        dpHeight >= context.resources.getInteger(R.integer.min_screen_height_chat_fragment)
    } else {
        false
    }

    private fun getScreenOrientation() = App.get().resources.configuration.orientation

    private fun setActionbarVisibility(visible: Boolean) {
        val toolbarActivity = activity
        if (toolbarActivity != null && toolbarActivity is ToolbarActivity<*>
                && (toolbarActivity.isToolBarVisible() != visible)) {
            toolbarActivity.setToolBarVisibility(visible)
        }
    }
}