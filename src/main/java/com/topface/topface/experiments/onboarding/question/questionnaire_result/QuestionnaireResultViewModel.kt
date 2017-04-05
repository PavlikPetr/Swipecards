package com.topface.topface.experiments.onboarding.question.questionnaire_result

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.databinding.ObservableLong
import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Photo
import com.topface.topface.data.Profile
import com.topface.topface.experiments.onboarding.question.QuestionnaireResult
import com.topface.topface.glide.tranformation.GlideTransformationType
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.json.JSONObject
import rx.Observable
import rx.Subscription
import java.util.concurrent.TimeUnit

class QuestionnaireResultViewModel(bundle: Bundle, api: FeedApi, private val mFeedNavigator: FeedNavigator) {

    companion object {
        const val LOADER = 0
        const val FINAL = 1
        private const val DELAY = 3L
    }

    val mMethodName: String = bundle.getString(QuestionnaireResultFragment.EXTRA_METHOD_NAME)
    val mRequestData = JSONObject(bundle.getString(QuestionnaireResultFragment.EXTRA_REQUEST_DATA))
    val foundTitle = ObservableField<String>()
    val buyMessage = ObservableField<String>()

    val firstAvatar: ObservableField<Photo?> = ObservableField()
    val secondAvatar: ObservableField<Photo?> = ObservableField()
    val thirdAvatar: ObservableField<Photo?> = ObservableField()
    val fourthAvatar: ObservableField<Photo?> = ObservableField()
    val fifthAvatar: ObservableField<Photo?> = ObservableField()

    val avatarPlaceholderRes = ObservableInt(if (App.get().profile.sex == Profile.GIRL) R.drawable.dialogues_av_man_big else R.drawable.dialogues_av_girl_small)
    val type = GlideTransformationType.CIRCLE_AVATAR_WITH_STROKE_AROUND

    val showChild = ObservableInt(LOADER)

    val startOffSettMedial = ObservableLong(500)
    val startOffSettLateral = ObservableLong(700)
    val outsideCircle = R.dimen.mutual_popup_stroke_outside.getDimen()

    var productId: String = Utils.EMPTY

    private var mSubscription: Subscription? = null

    init {
        mSubscription = Observable.combineLatest(
                Observable.timer(DELAY, TimeUnit.SECONDS),
                api.callQuestionnaireSearch(mMethodName, mRequestData)
        ) { item1, item2 ->
            item2
        }.first().subscribe(shortSubscription {
            fillData(it)
        })
    }

    fun fillData(data: QuestionnaireResult) {
        with(data.users) {
            firstAvatar.set(getOrNull(0)?.photo)
            secondAvatar.set(getOrNull(1)?.photo)
            thirdAvatar.set(getOrNull(2)?.photo)
            fourthAvatar.set(getOrNull(3)?.photo)
            fifthAvatar.set(getOrNull(4)?.photo)

            getOrNull(0)?.let {
                avatarPlaceholderRes.set(if (it.sex == Profile.BOY) R.drawable.dialogues_av_man_big else R.drawable.dialogues_av_girl_small)
            }
        }
        foundTitle.set(data.foundTitle)
        buyMessage.set(data.buyMessage)
        productId = data.productId
        showChild.set(FINAL)
    }

    fun onBuyButtonClick() = mFeedNavigator.showPurchaseProduct(productId, "Questionnaire Experiment")

    fun release() = mSubscription.safeUnsubscribe()
}