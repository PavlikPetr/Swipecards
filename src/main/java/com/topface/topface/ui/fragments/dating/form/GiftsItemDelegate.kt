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
import com.topface.topface.utils.Utils
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
                giftsList.adapter = FormGiftsAdapter(giftsModel.gifts.more).apply {
                    mAdapter = this
                    mViewModel = GiftsItemViewModel(mApi, mNavigator, giftsModel.gifts, giftsModel.userId) {
                        //добавляем подарочки в search user, чтобы не загружать их заново после поворота
                        giftsModel.gifts.items.addAll(it.items)
                        addData(it.items)
                        notifyDataSetChanged()
                    }
                    updaterObservable.distinct {
                        it?.getString(BaseFeedFragmentViewModel.TO, Utils.EMPTY)
                    }.subscribe {
                        if (giftsModel.gifts.more) {
                            Debug.log("GIFTS_BUGS loadGifts ${data.data?.userId} want ${giftsModel.gifts.count}")
                            var lastGiftId = -1
                            if (this.data.isNotEmpty()) lastGiftId = this.data.last().feedId
                            mViewModel?.loadGifts(lastGiftId)
                        }
                    }
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
                it.items?.addAll(0, gifts)
                it.count++
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