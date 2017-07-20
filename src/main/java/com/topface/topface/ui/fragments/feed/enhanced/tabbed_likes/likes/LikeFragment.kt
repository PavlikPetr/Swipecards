package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes

import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.databinding.LikesCardsFragmentBinding
import com.topface.topface.databinding.LikesCardsItemBinding
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.feed.likes.*
import com.topface.topface.di.navigation_activity.NavigationActivityComponent
import com.topface.topface.di.navigation_activity.NavigationActivityModule
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragment
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragmentModel
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.BaseAdapter
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription


/**
 * Created by ppavlik on 17.07.17.
 * Фрагмент симпатий в виде карточек, по аналогии с tinder
 */
@FlurryOpenEvent(name = LikeFragment.SCREEN_TYPE)
class LikeFragment : BaseFeedFragment<FeedBookmark, BaseAdapter<LikesCardsItemBinding, FeedBookmark>, LikesCardsFragmentBinding>() {

    companion object {
        const val SCREEN_TYPE = "NewLikes"
    }

    private var mUserСhoiceSubscription: Subscription? = null

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }


    override fun attachAdapterComponents(adapter: BaseAdapter<LikesCardsItemBinding, FeedBookmark>) {
    }

    override val res: Int
        get() = R.layout.likes_cards_fragment

    override fun initScreenView(binding: LikesCardsFragmentBinding) {
        with(binding.frame) {
            adapter = mAdapter
            setFlingListener(mViewModel)
            setOnItemClickListener(mViewModel)
        }
    }

    override val mViewModel by lazy {
        ComponentManager.obtainComponent(LikesViewModelsComponent::class.java) {
            DaggerLikesViewModelsComponent.builder().appComponent(App.getAppComponent()).build()
        }.likesViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        ComponentManager.releaseComponent(LikesComponent::class.java)
//        ComponentManager.obtainComponent(LikesComponent::class.java) {
//            ComponentManager.obtainComponent(NavigationActivityComponent::class.java) {
//                App.getAppComponent().add(NavigationActivityModule(activity as NavigationActivity))
//            }
//                    .add(LikesModule(this@LikeFragment), LikesFeedModule(this@LikeFragment))
//        }.inject(this@LikeFragment)
        super.onCreate(savedInstanceState)
        mUserСhoiceSubscription = mEventBus
                .getObservable(LikesCardUserChoose::class.java)
                .subscribe(shortSubscription {
                    it?.let {
                        try {
                            if (it.isLike) {
                                mBinding.frame.topCardListener.selectRight()
                            } else {
                                mBinding.frame.topCardListener.selectLeft()
                            }
                        } catch (e: NullPointerException) {

                        }
                    }

                })
    }

    override fun onDestroy() {
        super.onDestroy()
        mUserСhoiceSubscription.safeUnsubscribe()
    }

    override fun terminateImmortalComponent() {
        ComponentManager.releaseComponent(LikesViewModelsComponent::class.java)
    }

    override fun onDestroyView() {
        ComponentManager.releaseComponent(LikesComponent::class.java)
        super.onDestroyView()
    }
}