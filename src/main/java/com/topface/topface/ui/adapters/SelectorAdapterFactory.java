package com.topface.topface.ui.adapters;

import com.topface.topface.data.Profile;
import com.topface.topface.ui.dialogs.AbstractSelectorDialog;
import com.topface.topface.utils.FormItem;

/**
 * Factory for edit dialog adapters
 */
public class SelectorAdapterFactory {

    public AbstractSelectorAdapter createSelectorFor(Object data) {
        if (data instanceof  Profile.TopfaceNotifications) {
            return new NotificationSelectorAdapter((Profile.TopfaceNotifications) data);
        } else {
            return new FormItemSelectorAdapter((FormItem) data);
        }
    }
}
