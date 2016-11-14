package com.topface.topface.ui.fragments.dating.view_etc

import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.view.View
import com.topface.topface.R
import com.topface.topface.utils.extensions.getDimen

/** Поведение для кнопок в знакомствах. Если сколапсились - скрываем
 * Created by tiberal on 07.10.16.
 */
class DatingButtonsBehavior<V : View>() : CoordinatorLayout.Behavior<V>() {

    var datingLayout: DatingButtonsLayout? = null
    var collapsing: CollapsingToolbarLayout? = null

    private fun getDatingLayout(child: V): DatingButtonsLayout? {
        if (datingLayout == null) {
            datingLayout = child.findViewById(R.id.dating_buttons_root) as DatingButtonsLayout?
        }
        return datingLayout
    }

    private fun getCollapsing(parent: CoordinatorLayout?): CollapsingToolbarLayout? {
        if (collapsing == null && parent != null) {
            collapsing = parent.findViewById(R.id.collapsing_layout) as?CollapsingToolbarLayout
        }
        return collapsing
    }

    //todo установить для коллапсинга высоту, при которой менять цвет и юзать сдесь(24+ апи)
    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: V, dependency: View?): Boolean {
        getDatingLayout(child)?.let {
            if (child.top + (child.bottom - child.top) / 2 <=
                    getCollapsing(parent)?.scrimVisibleHeightTrigger ?:
                            R.dimen.collapsing_scrim_size.getDimen().toInt()) {
                    it.visibility = View.INVISIBLE
            } else {
                    it.visibility = View.VISIBLE
            }
        }
        return false
    }
}