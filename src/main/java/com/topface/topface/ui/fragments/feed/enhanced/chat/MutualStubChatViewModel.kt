package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Profile
import com.topface.topface.data.User
import com.topface.topface.glide.tranformation.GlideTransformationType
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription


class MutualStubChatViewModel(private val mMutualItem: FeedUser) {

    val userPhoto = ObservableField(App.get().profile.photo)
    val type = GlideTransformationType.CROP_CIRCLE_TYPE
    val userPlaceholderRes = ObservableInt((if (App.get().profile.sex == User.BOY) R.drawable.dialogues_av_man_big
    else R.drawable.dialogues_av_girl_small))

    val mutualUserPhoto = ObservableField(mMutualItem.photo)
    val mutualType = GlideTransformationType.CIRCLE_AVATAR_WITH_STROKE_AROUND
    val mutualPlaceholderRes = ObservableInt(if (mMutualItem.sex == User.BOY) R.drawable.dialogues_av_man_big else R.drawable.dialogues_av_girl_big)
    val outsideCircle = R.dimen.mutual_popup_stroke_outside.getDimen()
    val circleColor = R.color.ask_moar_item_background

    val stubText = ObservableField((if (App.get().profile.sex == User.BOY) R.string.write_her_something_first.getString()
    else R.string.write_him_first.getString()))

    private val mAppState by lazy {
        App.getAppComponent().appState()
    }
    private var mProfileSubscription: Subscription? = null

    init {
        mProfileSubscription = mAppState.getObservable(Profile::class.java)
                .distinctUntilChanged { t1, t2 -> t1.photo == t2.photo }
                .subscribe(shortSubscription {
                    userPhoto.set(it.photo)
                })
    }

    fun release() = mProfileSubscription.safeUnsubscribe()
}