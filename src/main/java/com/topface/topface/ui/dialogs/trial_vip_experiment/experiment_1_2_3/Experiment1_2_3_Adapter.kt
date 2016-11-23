package com.topface.topface.ui.dialogs.trial_vip_experiment.experiment_1_2_3

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.Experiment123ItemBinding
import org.jetbrains.anko.layoutInflater

/**
 * Адаптер для слийдера картинок в триал вип попапе
 * Created by tiberal on 16.11.16.
 */
class Experiment1_2_3_Adapter(val context: Context, val type: Int) : PagerAdapter() {

    private lateinit var mImages: IntArray
    private lateinit var mDescriptions: IntArray

    companion object {
        const val MODE = "mode"
        const val GUESTS_FIRST = 1
        const val VIEWS_FIRST = 2
        const val MESSAGE_FIRST = 3
    }

    init {
        createDataArrays(type)
    }

    private fun createDataArrays(type: Int) {
        when (type) {
            GUESTS_FIRST -> {
                mImages = intArrayOf(R.drawable.guest_big, R.drawable.no_advertising,
                        R.drawable.message_big, getPopularPersonIcon(), R.drawable.views)
                mDescriptions = intArrayOf(R.string.i_see_u_face, R.string.no_more_ad,
                        getPopularPersonDescription(), R.string.moar_messages, R.string.buy_vip_more_views)
            }
            VIEWS_FIRST -> {
                mImages = intArrayOf(R.drawable.views, R.drawable.no_advertising, R.drawable.guest_big,
                        R.drawable.message_big, getPopularPersonIcon())
                mDescriptions = intArrayOf(R.string.buy_vip_more_views, R.string.no_more_ad, R.string.i_see_u_face,
                        getPopularPersonDescription(), R.string.moar_messages)
            }
            MESSAGE_FIRST -> {
                mImages = intArrayOf(R.drawable.message_big, R.drawable.no_advertising, R.drawable.guest_big,
                        getPopularPersonIcon(), R.drawable.views)
                mDescriptions = intArrayOf(getPopularPersonDescription(), R.string.no_more_ad, R.string.i_see_u_face,
                        R.string.moar_messages, R.string.buy_vip_more_views)
            }
            else -> {
                mImages = intArrayOf(R.drawable.no_advertising, R.drawable.guest_big,
                        R.drawable.message_big, getPopularPersonIcon(), R.drawable.views)
                mDescriptions = intArrayOf(R.string.no_more_ad, R.string.i_see_u_face,
                        getPopularPersonDescription(), R.string.moar_messages, R.string.buy_vip_more_views)
            }
        }

    }

    private fun getPopularPersonIcon() = if (App.get().profile.sex == Profile.BOY)
        R.drawable.favorite_boy
    else
        R.drawable.popular_girl

    private fun getPopularPersonDescription() = if (App.get().profile.sex == Profile.BOY)
        R.string.buy_vip_write_anyone_boy
    else
        R.string.buy_vip_write_anyone

    override fun instantiateItem(container: ViewGroup, position: Int): Any =
            with(DataBindingUtil.inflate<Experiment123ItemBinding>(context.layoutInflater,
                    R.layout.experiment_1_2_3_item, container, false)) {
                viewModel = getItem(position)
                container.addView(root)
                root
            }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (`object` is View) {
            container.removeView(`object`)
        }
    }

    override fun getCount() = mImages.size

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    private fun getItem(position: Int) = if (mImages.size <= mDescriptions.size)
        Pair(mImages[position], mDescriptions[position])
    else
        Pair(mImages[position], null)


}