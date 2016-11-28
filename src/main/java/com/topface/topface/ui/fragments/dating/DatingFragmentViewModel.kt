package com.topface.topface.ui.fragments.dating

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.widget.Toast
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.RetryRequestReceiver
import com.topface.topface.Ssid
import com.topface.topface.data.DatingFilter
import com.topface.topface.data.Profile
import com.topface.topface.data.search.CachableSearchList
import com.topface.topface.data.search.OnUsersListEventsListener
import com.topface.topface.data.search.SearchUser
import com.topface.topface.data.search.UsersList
import com.topface.topface.databinding.FragmentDatingLayoutBinding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.edit.EditContainerActivity
import com.topface.topface.ui.edit.filter.model.FilterData
import com.topface.topface.ui.edit.filter.view.FilterFragment
import com.topface.topface.ui.fragments.dating.form.FormModel
import com.topface.topface.ui.fragments.dating.form.GiftsModel
import com.topface.topface.ui.fragments.dating.form.ParentModel
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.new_adapter.CompositeAdapter
import com.topface.topface.ui.new_adapter.IType
import com.topface.topface.utils.FlurryManager
import com.topface.topface.utils.PreloadManager
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.extensions.safeUnsubscribe
import com.topface.topface.utils.social.AuthToken
import com.topface.topface.viewModels.BaseViewModel
import rx.Observer
import rx.Subscriber
import rx.Subscription
import javax.inject.Inject

/** Бизнеслогика для дейтинга
 * Created by tiberal on 12.10.16.
 */
class DatingFragmentViewModel(binding: FragmentDatingLayoutBinding, private val mApi: FeedApi,
                              private val mUserSearchList: CachableSearchList<SearchUser>,
                              private val mDatingViewModelEvents: IDatingViewModelEvents,
                              private val mDatingButtonsView: IDatingButtonsView,
                              private val mEmptySearchVisibility: IEmptySearchVisibility) :
        BaseViewModel<FragmentDatingLayoutBinding>(binding), OnUsersListEventsListener<SearchUser> {

    @Inject lateinit var state: TopfaceAppState

    private var mProfileSubscription: Subscription
    private var mUpdateSubscription: Subscription? = null
    private val mPreloadManager by lazy {
        PreloadManager<SearchUser>()
    }
    var currentUser: SearchUser? = null
    private var mUpdateInProcess = false
    private var mNewFilter = false
    private lateinit var mReceiver: BroadcastReceiver

    companion object {
        const val CURRENT_USER = "current_user_dating_fragment_view_model"
        const val UPDATE_IN_PROCESS = "update_in_process"
        const val NEW_FILTER = "new_filter"
    }

    init {
        App.get().inject(this)
        //todo работаем по рофилю из кэша?
        mProfileSubscription = state.getObservable(Profile::class.java).subscribe {
            if (Ssid.isLoaded() && !AuthToken.getInstance().isEmpty) {
                if (currentUser == null) {
                    currentUser = mUserSearchList.currentUser
                    mUserSearchList.setOnEmptyListListener(this)
                } else {
                    //Сделано для того, чтобы не показывалось сообщение о том, что пользователи не найдены.
                    //Иначе при старте приложения, пока список пользователей не запросился показывается сообщение об ошибки
                    mUserSearchList.setOnEmptyListListener(this)
                }
                mUserSearchList.updateSignatureAndUpdate()
            }
        }
        createAndRegisterBroadcasts()
    }

    private fun createAndRegisterBroadcasts() {
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = mPreloadManager.checkConnectionType()
        }
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(mReceiver, IntentFilter(RetryRequestReceiver.RETRY_INTENT))
    }

    fun update(isNeedRefresh: Boolean, isAddition: Boolean, onlyOnline: Boolean = DatingFilter.getOnlyOnlineField()) {
        if (!mUpdateInProcess && mUserSearchList.isEnded) {
            mDatingButtonsView.lockControls()
            mEmptySearchVisibility.hideEmptySearchDialog()
            if (isNeedRefresh) {
                mUserSearchList.clear()
                currentUser = null
            }

            mUpdateInProcess = true
            mUpdateSubscription = mApi.callDatingUpdate(onlyOnline, isNeedRefresh).subscribe(object : Observer<UsersList<SearchUser>> {
                override fun onCompleted() {
                    mUpdateInProcess = false
                    mUpdateSubscription.safeUnsubscribe()
                }

                override fun onError(e: Throwable?) {
                    mDatingButtonsView.unlockControls()
                    e?.printStackTrace()
                }

                override fun onNext(usersList: UsersList<SearchUser>?) {
                    if (usersList != null && usersList.size != 0) {
                        UsersList.log("load success. Loaded " + usersList.size + " users")
                        //Добавляем новых пользователей
                        mUserSearchList.addAndUpdateSignature(usersList)
                        mPreloadManager.preloadPhoto(mUserSearchList)
                        //если список был пуст, то просто показываем нового пользователя
                        val user = mUserSearchList.currentUser
                        //NOTE: Если в поиске никого нет, то мы показываем следующего юзера
                        //Но нужно учитывать, что такое происходит при смене фильтра не через приложение,
                        //Когда чистится поиск, если фильтр поменялся удаленно,
                        //из-за чего происходит автоматический переход на следующего юзера
                        //От этого эффекта можно избавиться, если заменить на такое условие:
                        //<code>if (!isAddition && mCurrentUser != currentUser || mCurrentUser == null)</code>
                        //Но возникает странный эффект, когда в поиске написано одно, а у юзера другое,
                        //В связи с чем, все работает так как работает
                        if (user != null && currentUser !== user) {
                            currentUser = user
                            mDatingViewModelEvents.onDataReceived(user)
                            prepareFormsData(user)
                        } else if (mUserSearchList.isEmpty() || mUserSearchList.isEnded) {
                            mEmptySearchVisibility.showEmptySearchDialog()
                        }
                        mDatingButtonsView.unlockControls()
                    } else {
                        if (!isAddition || mUserSearchList.isEmpty()) {
                            mEmptySearchVisibility.showEmptySearchDialog()
                        }
                    }
                }

            })
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun prepareFormsData(user: SearchUser) = with((binding.formsList
            .adapter as CompositeAdapter<IType>).data) {
        clear()
        if (!user.city.name.isNullOrEmpty()) addExpandableItem(ParentModel(user.city.name, false, R.drawable.pin))
        if (!user.status.isNullOrEmpty()) addExpandableItem(ParentModel(user.status, false, R.drawable.status))
        Debug.log("GIFTS_BUGS prepareFormsData gifts items ${user.gifts.items.count()} gifts ${user.gifts.count}")
        addExpandableItem(GiftsModel(user.gifts, user.id))
        val forms: MutableList<IType>
        if (App.get().profile.hasEmptyFields) {
            //показываем заглушку, чтоб юзер заполнил свою анкету
            forms = mutableListOf<IType>(FormModel(
                    Pair(String.format(if (user.sex == Profile.BOY) R.string.fill_own_profile_boy.getString()
                    else R.string.fill_own_profile.getString(), user.firstName), Utils.EMPTY),
                    isEmptyItem = false))
        } else {
            forms = mutableListOf <IType>().apply {
                var hasEmptyItem = false
                user.forms.forEach {
                    if (it.isEmpty && !hasEmptyItem) {
                        hasEmptyItem = true
                        //если у юзера есть пустые поля в анкете, то добавляем строку с просьбой отправить запрос на добавление инфы
                        add(0, FormModel(Pair(R.string.ask_moar_info.getString(), Utils.EMPTY), currentUser?.id,
                                it.dataType.type, true, R.drawable.arrow_bottom_large, R.color.ask_moar_item_background))
                    }
                    add(FormModel(Pair(it.title, it.value), user.id, it.dataType.type, isEmptyItem = it.isEmpty))
                }
            }
        }
        addExpandableItem(ParentModel(R.string.about.getString(), true, R.drawable.about), forms)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == EditContainerActivity.INTENT_EDIT_FILTER) {
            mDatingButtonsView.lockControls()
            mEmptySearchVisibility.hideEmptySearchDialog()
            if (data != null && data.extras != null) {
                sendFilterRequest(data.getParcelableExtra<FilterData>(FilterFragment.INTENT_DATING_FILTER))
                mNewFilter = true
                FlurryManager.getInstance().sendFilterChangedEvent()
            }
        }
    }

    private fun sendFilterRequest(filter: FilterData) {
        mApi.callFilterRequest(filter).subscribe(object : Subscriber<DatingFilter>() {
            override fun onNext(filter: DatingFilter?) {
                val profile = App.get().profile
                profile.dating = filter
                state.setData(profile)
                mUserSearchList.updateSignatureAndUpdate()
                update(false, false)
                mNewFilter = false
            }

            override fun onCompleted() {
                mNewFilter = false
                mDatingButtonsView.lockControls()
            }

            override fun onError(e: Throwable?) {
                mDatingButtonsView.lockControls()
                mNewFilter = false
                mEmptySearchVisibility.showEmptySearchDialog()
                Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_LONG)
            }
        })
    }

    override fun onSavedInstanceState(state: Bundle): Unit = with(state) {
        putParcelable(CURRENT_USER, currentUser)
        putBoolean(UPDATE_IN_PROCESS, mUpdateInProcess)
        putBoolean(NEW_FILTER, mNewFilter)
        Debug.log("GIFTS_BUGS saved dating v model gifts items ${currentUser?.gifts?.items?.count()} gifts ${currentUser?.gifts?.count}")
    }

    override fun onRestoreInstanceState(state: Bundle) = with(state) {
        mUpdateInProcess = getBoolean(UPDATE_IN_PROCESS)
        mNewFilter = getBoolean(NEW_FILTER)
        currentUser = getParcelable<SearchUser>(CURRENT_USER)?.apply {
            Debug.log("GIFTS_BUGS restore dating v model gifts items ${gifts.items.count()} gifts ${gifts.count}")
            prepareFormsData(this)
        }
    }

    override fun release() {
        super.release()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mReceiver)
        arrayOf(mProfileSubscription, mUpdateSubscription).safeUnsubscribe()
    }

    override fun onEmptyList(usersList: UsersList<SearchUser>?) {
        update(mNewFilter, false)
    }

    override fun onPreload(usersList: UsersList<SearchUser>?) {
        if (!mNewFilter) {
            update(false, true)
        }
    }
}