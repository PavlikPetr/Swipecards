package com.topface.topface.ui.fragments.dating.form

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.topface.framework.JsonUtils
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.data.Gift
import com.topface.topface.data.SendGiftAnswer
import com.topface.topface.databinding.GiftsFormItemBinding
import com.topface.topface.ui.ChatActivity
import com.topface.topface.ui.GiftsActivity
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.ExpandableItem
import com.topface.topface.ui.new_adapter.ExpandableItemDelegate
import com.topface.topface.utils.rx.shortSubscription
import java.util.*


/**
 * Делегат для итема с подарочками
 * Created by tiberal on 07.11.16.
 */
class GiftsItemDelegate(private val mApi: FeedApi, private val mNavigator: IFeedNavigator) : ExpandableItemDelegate<GiftsFormItemBinding, GiftsModel>() {

    companion object {
        const val TYPE = 2
    }

    private var mViewModel: GiftsItemViewModel? = null
    private var mAdapter: FormGiftsAdapter? = null
    private var mGiftsModel: GiftsModel? = null
    private var mGiftsList: RecyclerView? = null

    override val itemLayout: Int
        get() = R.layout.gifts_form_item
    override val bindingClass: Class<GiftsFormItemBinding>
        get() = GiftsFormItemBinding::class.java

    override fun bind(binding: GiftsFormItemBinding, data: ExpandableItem<GiftsModel>?, position: Int) {
        val giftsList = binding.giftsList
        giftsList.layoutManager = LinearLayoutManager(binding.root.context.applicationContext
                , LinearLayoutManager.HORIZONTAL, false)
        data?.data?.let { giftsModel ->
            mGiftsModel = giftsModel
            if (giftsModel.gifts != null) {
                Debug.log("GIFTS_BUGS adapter init has gifts ${giftsModel.gifts.items.isNotEmpty()} moar? =  ${giftsModel.gifts.more}")
                giftsList.adapter = FormGiftsAdapter(giftsModel.gifts.items.isNotEmpty() || giftsModel.gifts.more).apply {
                    mAdapter = this
                    mViewModel = GiftsItemViewModel(mApi, mNavigator, giftsModel.gifts, giftsModel.userId) {
                        //добавляем подарочки в search user, чтобы не загружать их заново после поворота
                        giftsModel.gifts.items.addAll(it.items)
                        addData(it.items)
                        notifyDataSetChanged()
                    }
                    Debug.log("GIFTS_BUGS try add items ${giftsModel.gifts.items.count()}")
                    addData(giftsModel.gifts.items)
                    notifyDataSetChanged()
                    updaterObservable
                            .distinct {
                                it?.getInt(BaseFeedFragmentViewModel.TO, -1)
                            }
                            .subscribe(shortSubscription {
                                Debug.log("GIFTS_BUGS loadGifts ${data.data?.userId} count ${giftsModel.gifts.count} moar ${giftsModel.gifts.more}")
                                if (giftsModel.gifts.more) {
                                    mViewModel?.loadGifts(it.getInt(BaseFeedFragmentViewModel.TO, -1))
                                }
                            })
                    binding.model = mViewModel
                }
            }
        }
        mGiftsList = giftsList
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
                val gifts = ArrayList<Gift>().apply {
                    val answer = data?.getParcelableExtra<SendGiftAnswer>(GiftsActivity.INTENT_SEND_GIFT_ANSWER)
                    add(JsonUtils.fromJson(answer?.history?.mJsonForParse, Gift::class.java))
                }
                handleGifts(gifts)
            }
            if (requestCode == ChatActivity.REQUEST_CHAT) {
                data?.getParcelableArrayListExtra<Gift>(ChatActivity.DISPATCHED_GIFTS)?.let {
                    handleGifts(it)
                }
            }
        }
    }

    private fun handleGifts(gifts: ArrayList<Gift>) {
        if (gifts.isEmpty()) return
        mAdapter?.let {
            if (!it.hasGifts) {
                //выпиливаем итем с повеливающий ченить подарить
                it.clearData()
                it.hasGifts = true
            }
            mGiftsModel?.gifts?.let {
                Debug.log("GIFTS_BUGS handle gifts ${gifts.count()} adapter items ${it.items}")
                it.items?.addAll(0, gifts)
                it.count += gifts.count()
                it.more = true
                mViewModel?.gifts = it
            }
            it.addFirst(gifts)
            mGiftsList?.scrollToPosition(0)
        }
    }

    fun onDestroyView() {
        mViewModel?.release()
        mGiftsList = null
        mAdapter = null
    }

}