package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import com.topface.topface.data.FeedDialog
import com.topface.topface.requests.response.DialogContacts

/**
 * Модели для диалогов
 * Created by tiberal on 01.12.16.
 */

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
