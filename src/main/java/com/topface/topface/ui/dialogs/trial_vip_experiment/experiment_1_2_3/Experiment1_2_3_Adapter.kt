package com.topface.topface.ui.dialogs.trial_vip_experiment.experiment_1_2_3

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.Experiment123ItemBinding
import org.jetbrains.anko.layoutInflater

/**
 * Адаптер для слийдера картинок в триал фип попапе
 * Created by tiberal on 16.11.16.
 */
class Experiment1_2_3_Adapter(val context: Context) : PagerAdapter() {

    companion object {
        private val IMAGES = intArrayOf(R.drawable.trial_vip_img_crown, R.drawable.trial_vip_img_crown, R.drawable.trial_vip_img_crown)
        private val DESCRIPTIONS = intArrayOf(R.string.vip_buy_vip, R.string.vip_buy_vip, R.string.vip_buy_vip)
    }

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

    override fun getCount() = IMAGES.size

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    private fun getItem(position: Int) = Pair(IMAGES[position], DESCRIPTIONS[position])

}