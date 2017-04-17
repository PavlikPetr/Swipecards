package com.topface.topface.ui.fragments.dating

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.GiftsFormItemBinding
import com.topface.topface.state.EventBus
import com.topface.topface.ui.fragments.dating.form.GiftId
import com.topface.topface.ui.fragments.dating.form.GiftsItemViewModel
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription


class GiftListItemComponent(private val mApi: FeedApi, private val mNavigator: IFeedNavigator) : AdapterComponent<GiftsFormItemBinding, GiftsModel>() {

    companion object {
        const val TO = "to"
    }

    private val mEventBus: EventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private var mEventBusSubscription: Subscription? = null
    private var mModel: GiftsItemViewModel? = null
    private lateinit var mAdapter: CompositeAdapter
    private var mGiftsModel: GiftsModel? = null

    var lastGiftsId = -1


    private val mGiftTypeProvider by lazy { GiftTypeProvider() }
    override val itemLayout: Int
        get() = R.layout.gifts_form_item
    override val bindingClass: Class<GiftsFormItemBinding>
        get() = GiftsFormItemBinding::class.java

    init {
        mEventBusSubscription = mEventBus.getObservable(GiftId::class.java).subscribe(shortSubscription {
            Debug.error("------------------lastGiftsId = ${it.value}----------------------")
            lastGiftsId = it.value
        })
    }

    override fun bind(binding: GiftsFormItemBinding, data: GiftsModel?, position: Int) {
        data?.let {
            it.gifts?.let {
                with(binding.giftsList) {
                    data?.let { giftsModel ->
                        mGiftsModel = giftsModel
                        if (giftsModel.gifts != null) {
                            mModel = GiftsItemViewModel(mApi, mNavigator, it, giftsModel.userId)
                            mAdapter = CompositeAdapter(mGiftTypeProvider) { Bundle().apply {
                                    putInt(TO, if (giftsModel.gifts.items.isEmpty()) giftsModel.gifts.items.last().feedId else -1)
                                }
                            }
                                    .addAdapterComponent(GiftItemComponent())
                                    .addAdapterComponent(GiftEmptyComponent())

                            mAdapter.updateObservable
                                    .distinct { it?.getInt(TO, -1) }
                                    .subscribe(shortSubscription {
                                        if (giftsModel.gifts.more) {
                                            mModel?.loadGifts(it.getInt(TO, -1))
                                        } else {
                                            mModel?.loadFakeGift()
                                        }
                                        mAdapter.notifyDataSetChanged()
                                    })
                            adapter = mAdapter
                            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        }
                    }
                }
                binding.setModel(mModel)
            }
        }
    }
}