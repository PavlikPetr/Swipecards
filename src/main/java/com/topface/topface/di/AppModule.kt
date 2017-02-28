package com.topface.topface.di

import android.content.Context
import android.location.Location
import android.text.TextUtils
import com.topface.framework.JsonUtils
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.data.*
import com.topface.topface.data.leftMenu.NavigationState
import com.topface.topface.mvp.PresenterCache
import com.topface.topface.state.*
import com.topface.topface.ui.external_libs.adjust.AdjustAttributeData
import com.topface.topface.utils.CacheProfile
import com.topface.topface.utils.RunningStateManager
import com.topface.topface.utils.config.WeakStorage
import com.topface.topface.utils.social.OkUserData
import dagger.Module
import dagger.Provides
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Singleton

/**
 * Рутовый модуль даггера
 * Created by tiberal on 02.02.17.
 */

@Module()
class AppModule(private val mContext: Context) {

    @Provides
    fun providesContext() = mContext

    @Provides
    @Singleton
    fun providesTopfaceAppState(): TopfaceAppState {
        return TopfaceAppState(object : CacheDataInterface {
            override fun <T : Any> saveDataToCache(data: T) {
                if (data.javaClass == BalanceData::class.java) {
                    CacheProfile.balanceData = data as BalanceData
                } else if (data.javaClass == CountersData::class.java) {
                    CacheProfile.countersData = data as CountersData
                } else if (data.javaClass == Location::class.java) {
                    val config = App.getUserConfig()
                    config.userGeoLocation = data as Location
                    config.saveConfig()
                } else if (data.javaClass == OkUserData::class.java) {
                    val config = App.getUserConfig()
                    config.okUserData = data as OkUserData
                    config.saveConfig()
                } else if (data.javaClass == AdjustAttributeData::class.java) {
                    val config = App.getAppConfig()
                    config.adjustAttributeData = data as AdjustAttributeData
                    config.saveConfig()
                } else if (data.javaClass == Options::class.java) {
                    CacheProfile.setOptions(JsonUtils.optionsToJson(data as Options))
                } else if (data.javaClass == Profile::class.java) {
                    val profile = data as Profile
                    CacheProfile.setProfile(profile, JsonUtils.profileToJson(profile))
                }
            }

            override fun <T> getDataFromCache(classType: Class<T>): T? {
                if (BalanceData::class.java == classType) {
                    return (if (CacheProfile.balanceData != null) BalanceData(CacheProfile.balanceData) else BalanceData()) as T
                } else if (CountersData::class.java == classType) {
                    return (if (CacheProfile.countersData != null) CountersData(CacheProfile.countersData) else CountersData()) as T
                } else if (Location::class.java == classType) {
                    return App.getUserConfig().userGeoLocation as T
                } else if (Options::class.java == classType) {
                    return options as T
                } else if (Profile::class.java == classType) {
                    return profile as T
                } else if (OkUserData::class.java == classType) {
                    return App.getUserConfig().okUserData as T
                } else if (AdjustAttributeData::class.java == classType) {
                    return App.getAppConfig().adjustAttributeData as T?
                }
                return null
            }

            private //Получаем опции из кэша, причем передаем флаг, что бы эти опции не кешировались повторно
                    //Если произошла ошибка при парсинге кэша, то скидываем опции
                    //Если по каким то причинам кэша нет и опции нам в данный момент взять негде.
                    //то просто используем их по умолчанию
            val options: Options
                get() {
                    val config = App.getSessionConfig()
                    val optionsCache = config.optionsData
                    if (!TextUtils.isEmpty(optionsCache)) {
                        try {
                            return Options(JSONObject(optionsCache), false)
                        } catch (e: JSONException) {
                            config.resetOptionsData()
                            Debug.error(e)
                        }

                    }
                    return Options(null, false)
                }

            private //Получаем опции из кэша
            val profile: Profile
                get() {
                    val config = App.getSessionConfig()
                    val profileCache = config.profileData
                    if (!TextUtils.isEmpty(profileCache)) {
                        try {
                            val profileJson = JSONObject(profileCache)
                            CacheProfile.isLoaded.set(true)
                            return Profile(profileJson, true)
                        } catch (e: JSONException) {
                            config.resetProfileData()
                            Debug.error(e)
                        }

                    }
                    return Profile()
                }

        })
    }

    @Provides
    fun providesRunningStateManager() = RunningStateManager()

    @Provides
    @Singleton
    fun providesNavigationState() = NavigationState()

    @Provides
    @Singleton
    fun providesLifeCycleState() = LifeCycleState()

    @Provides
    @Singleton
    fun providesDrawerLayoutState() = DrawerLayoutState()

    @Provides
    @Singleton
    fun providesEventBus() = EventBus()

    @Provides
    @Singleton
    fun providesAuthState() = AuthState(object : CacheDataInterface {
        override fun <T> saveDataToCache(data: T) {}

        override fun <T> getDataFromCache(classType: Class<T>): T? {
            if (AuthTokenStateData::class.java == classType) {
                return AuthTokenStateData() as T
            }
            return null
        }
    })

    @Provides
    @Singleton
    fun providesWeakStorage() = WeakStorage()

    @Provides
    @Singleton
    fun providesPresenterCache() = PresenterCache()


}