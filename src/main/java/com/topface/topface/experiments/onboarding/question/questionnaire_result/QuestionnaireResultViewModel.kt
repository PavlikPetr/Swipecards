package com.topface.topface.experiments.onboarding.question.questionnaire_result

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.databinding.ObservableLong
import android.os.Bundle
import com.topface.topface.R
import com.topface.topface.data.Photo
import com.topface.topface.data.User
import com.topface.topface.experiments.onboarding.question.QuestionnaireResult
import com.topface.topface.glide.tranformation.GlideTransformationType
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.json.JSONObject
import rx.Observable
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit

class QuestionnaireResultViewModel(bundle: Bundle, val mApi: FeedApi) {

    companion object{
        const val LOADER = 0
        const val FINAL = 1
        private const val DELAY = 3L
    }

    val mMethodName: String = bundle.getString(QuestionnaireResultFragment.EXTRA_METHOD_NAME)
    val mRequestData = JSONObject(bundle.getString(QuestionnaireResultFragment.EXTRA_REQUEST_DATA))
    val foundTitle = ObservableField<String>()
    val buyMessage = ObservableField<String>()

    val firstAvatar: ObservableField<Photo> = ObservableField()
    val secondAvatar: ObservableField<Photo> = ObservableField()
    val thirdAvatar: ObservableField<Photo> = ObservableField()
    val fourthAvatar: ObservableField<Photo> = ObservableField()
    val fifthAvatar: ObservableField<Photo> = ObservableField()

    val avatarPlaceholderRes = ObservableInt()
    val type = GlideTransformationType.CIRCLE_AVATAR_WITH_STROKE_AROUND

    val showChild = ObservableInt(LOADER)

    val startOffSettMedial = ObservableLong(500)
    val startOffSettLateral = ObservableLong(700)
    val outsideCircle = R.dimen.mutual_popup_stroke_outside.getDimen()

    var productId: String = Utils.EMPTY

    private val mSubscription = CompositeSubscription()

    init {
        mSubscription.add(
                Observable.combineLatest(
                        Observable.timer(DELAY, TimeUnit.SECONDS),
                        mApi.callQuestionnaireSearch(mMethodName, mRequestData)
                ) { item1, item2 ->
                    item2
                }.first().subscribe(shortSubscription {
                    fillData(it)
                })
        )
    }

    fun fillData(data: QuestionnaireResult) {
        with(data.users) {
            firstAvatar.set(get(0).photo)
            secondAvatar.set(get(1).photo)
            thirdAvatar.set(get(2).photo)
            fourthAvatar.set(get(3).photo)
            fifthAvatar.set(get(4).photo)

            avatarPlaceholderRes.set((if (get(0).sex == User.BOY) R.drawable.dialogues_av_man_big
            else R.drawable.dialogues_av_girl_small))
        }
        foundTitle.set(data.foundtitle)
        buyMessage.set(data.buyMessage)
        productId = data.productId
        showChild.set(FINAL)
    }

    fun onBuyButtonClick() {
        //todo start GP purchase activity with productId
    }

    fun release() = mSubscription.safeUnsubscribe()
}