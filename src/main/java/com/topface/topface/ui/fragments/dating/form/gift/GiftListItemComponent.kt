package com.topface.topface.ui.fragments.dating.form.gift

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.topface.topface.R
import com.topface.topface.data.Gift
import com.topface.topface.databinding.GiftsFormItemBinding
import com.topface.topface.ui.fragments.dating.form.GiftsItemViewModel
import com.topface.topface.ui.fragments.dating.form.GiftsModel
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.rx.shortSubscription


class GiftListItemComponent(private val mApi: FeedApi, private val mNavigator: IFeedNavigator) : AdapterComponent<GiftsFormItemBinding, GiftsModel>(), ILifeCycle {

    companion object {
        const val TO = "to"
    }

    private var mModel: GiftsItemViewModel? = null
    private lateinit var mAdapter: CompositeAdapter
    private val mGiftTypeProvider by lazy { GiftTypeProvider() }

    override val itemLayout: Int
        get() = R.layout.gifts_form_item
    override val bindingClass: Class<GiftsFormItemBinding>
        get() = GiftsFormItemBinding::class.java

    override fun bind(binding: GiftsFormItemBinding, data: GiftsModel?, position: Int) {
        data?.let { giftsModel ->
            giftsModel.gifts?.let {
                with(binding.giftsList) {
                    mModel = GiftsItemViewModel(mApi, mNavigator, it, giftsModel.userId) { position, newGifts ->
                        //добавляем подарочки в search user, чтобы не загружать их заново после поворота
                        giftsModel.gifts.items.apply {
                            // если position==null значит вставку делаем в конец
                            if (position == null) {
                                addAll(newGifts.items)
                            } else {
                                // т.к. position!=null, то вставку делаем в указанную позицию и
                                // проскроливаем список к вставленному подарку
                                addAll(position, newGifts.items)
                                scrollToPosition(position)
                            }
                        }
                    }
                    mAdapter = CompositeAdapter(mGiftTypeProvider) {
                        Bundle().apply {
                            putInt(TO, with(mModel?.data?.lastOrNull()) {
                                if (this is Gift) feedId else -1
                            })
                        }
                    }
                            .addAdapterComponent(GiftItemComponent())
                            .addAdapterComponent(GiftEmptyComponent())
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    mAdapter.updateObservable
                            .distinct { it?.getInt(TO, -1) }
                            .subscribe(shortSubscription {
                                if (giftsModel.gifts.more) {
                                    mModel?.loadGifts(it.getInt(TO, -1))
                                } else if (giftsModel.gifts.gifts.isEmpty()) {
                                    mModel?.loadFakeGift()
                                } else {
                                    mModel?.data?.replaceData(arrayListOf<Any>()
                                            .apply { addAll(giftsModel.gifts.gifts) })
                                }
                            })
                    adapter = mAdapter
                }
                binding.model = mModel
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mModel?.onActivityResult(requestCode, resultCode, data)
    }

    override fun release() {
        super.release()
        mModel?.release()
        mAdapter.releaseComponents()
    }
}