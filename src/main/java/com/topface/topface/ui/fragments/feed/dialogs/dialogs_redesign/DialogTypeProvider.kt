package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import com.topface.topface.data.FeedDialog
import com.topface.topface.requests.response.DialogContactsItem
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

/**
 * Type provider for dating fragment items
 * Created by tiberal on 30.11.16.
 */
class DialogTypeProvider : ITypeProvider {
    override fun getType(java: Class<*>): Int {
        if (java == FeedDialog::class.java) {
            return 1
        }
        if (java == EmptyDialogsStubItem::class.java) {
            return 2
        }
        if (java == DialogContactsStubItem::class.java) {
            return 3
        }
        if (java == DialogContactsItem::class.java) {
            return 4
        }
        if (java == UForeverAloneStubItem::class.java) {
            return 5
        }
        if (java == AppDayStubItem::class.java) {
            return 6
        }
        return 0
    }
}