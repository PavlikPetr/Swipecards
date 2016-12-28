package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import com.topface.topface.data.FeedDialog
import com.topface.topface.requests.response.DialogContacts
import com.topface.topface.ui.fragments.feed.app_day.AppDay

/**
 * Модели для диалогов
 * Created by tiberal on 01.12.16.
 */

/**
 * Пустой итем для отображения общей загушки(нет ни контактов, ни диалогов)
 */
class EmptyDialogsFragmentStubItem() : FeedDialog()

/**
 * Пустой итем для отображения заглушки "Нет диалогов"
 */
class EmptyDialogsStubItem() : FeedDialog()

/**
 * Пустой итем для отображения хедера, со списком контактов
 */
class DialogContactsStubItem(val dialogContacts: DialogContacts = DialogContacts()) : FeedDialog()

/**
 * Пустой итем для отображения заглушки "Топай знакомиться" в контактах
 */
class GoDatingContactsStubItem()

/**
 * Пустой итем для отображения заглушки "Взаимных нет, вообще нет. Дуй знакомиться" в контактах
 */
class UForeverAloneStubItem()

/**
 * Пустой итем для отображения рекламы приложения дня
 */
class AppDayStubItem(var appDay: AppDay) : FeedDialog()

/**
 * Ивент о загрузке контактов есть/нет
 */
data class DialogContactsEvent(var hasContacts: Boolean)

/**
 * Ивент о загрузке диалогов есть/нет
 */
data class DialogItemsEvent(var hasDialogItems: Boolean)

/**
 *  события попапМеню, возникающего при длительном нажатии на экране сообщений
 */
data class DialogPopupEvent(val feedForDelete: FeedDialog)

