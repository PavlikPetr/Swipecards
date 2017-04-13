package com.topface.topface.ui.fragments.dating

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.databinding.GiftsFormItemBinding
import com.topface.topface.ui.fragments.dating.form.GiftsItemViewModel
import com.topface.topface.ui.fragments.dating.form.GiftsModel
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.rx.shortSubscription


class GiftListItemComponent(private val mApi: FeedApi, private val mNavigator: IFeedNavigator, private val mContext: Context) : AdapterComponent<GiftsFormItemBinding, GiftsModel>() {

    private var mModel: GiftsItemViewModel? = null
    private lateinit var mAdapter: CompositeAdapter


    override val itemLayout: Int
        get() = R.layout.gifts_form_item
    override val bindingClass: Class<GiftsFormItemBinding>
        get() = GiftsFormItemBinding::class.java

    override fun bind(binding: GiftsFormItemBinding, data: GiftsModel?, position: Int) {
        data?.let {
            it.gifts?.let {
                with(binding.giftsList) {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    mAdapter = CompositeAdapter(DatingTypeProvider()) { Bundle() }
                            .addAdapterComponent(if (it.items.isNotEmpty()) GiftItemComponent() else GiftEmptyComponent())
                    adapter = mAdapter

                }
            }
            mModel = it.gifts?.let { it1 -> GiftsItemViewModel(mApi, mNavigator, it1, it.userId) }
            mModel?.loadGifts(2)
        }
        binding.model = mModel
    }
}