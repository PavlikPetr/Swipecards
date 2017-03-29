package com.topface.topface.ui.auth.fb_invitation

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.R
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString

/**
 * Created by mbulgakov on 28.03.17.
 */
class FBInvitationItemViewModel(image: Int) {

    val imageSrc = ObservableInt(image)
    val popupText =  ObservableField<String>(setText(image))

    fun setText(img:Int):String=
            when(img){
                R.drawable.ic_clocks -> R.string.fb_invitation_find_what_you_are_looking_for.getString()
                R.drawable.ic_messages -> R.string.fb_invitation_amoung_more_than_100_mln_people.getString()
                R.drawable.ic_profiles -> R.string.fb_invitation_intelligent_selection_filter.getString()
                R.drawable.ic_people -> R.string.fb_invitation_personalized_approach.getString()
                else -> Utils.EMPTY
        }

}