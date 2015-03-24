package com.topface.topface.ui.adapters;

import com.topface.topface.data.Profile;
import com.topface.topface.utils.FormItem;

/**
 * Factory for edit dialog adapters
 */
public class EditAdapterFactory {

    public AbstractEditAdapter createSelectorFor(Object data) {
        if (data instanceof  Profile.TopfaceNotifications) {
            return new NotificationEditAdapter((Profile.TopfaceNotifications) data);
        } else if (data instanceof FormItem) {
            FormItem formItem = (FormItem) data;
            if (formItem.dataId != FormItem.NO_RESOURCE_ID) {
                return new FormItemEditAdapter(formItem);
            }
        }
        return new TextItemEditAdapter((FormItem) data);
    }
}
