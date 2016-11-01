package com.topface.topface.ui.fragments.feed.dating.view_etc

import android.support.design.widget.CoordinatorLayout
import android.view.View
import com.topface.topface.R

/** Поведение для кнопок в знакомствах. Если сколапсились - скрываем
 * Created by tiberal on 07.10.16.
 */
class DatingButtonsBehavior<V : View>() : CoordinatorLayout.Behavior<V>() {

    var datingLayout: DatingButtonsLayout? = null

    private fun getDatingLayout(child: V): DatingButtonsLayout? {
        if (datingLayout == null) {
            datingLayout = child.findViewById(R.id.dating_buttons_root) as DatingButtonsLayout?
        }
        return datingLayout
    }
    //todo установить для коллапсинга высоту, при которой менять цвет и юзать сдесь(24+ апи)
    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: V, dependency: View?): Boolean {
        getDatingLayout(child)?.let {
            if (child.top <= 240) {
                it.visibility = View.INVISIBLE
            } else {
                it.visibility = View.VISIBLE
            }
        }
        return false
    }


}