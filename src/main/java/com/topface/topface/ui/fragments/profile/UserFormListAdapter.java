package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormItem;

import java.util.Iterator;
import java.util.LinkedList;

public class UserFormListAdapter extends AbstractFormListAdapter {
    private Context mContext;
    private boolean isItemsEnabled;

    @SuppressWarnings("unused")
    public UserFormListAdapter(Context context) {
        this(context, true);
    }

    public UserFormListAdapter(Context context, boolean isItemsEnabled) {
        super(context);
        this.isItemsEnabled = isItemsEnabled;
        mContext = context;
    }

    // control items selectable
    @Override
    public boolean isEnabled(int position) {
        return isItemsEnabled;
    }

    @Override
    protected LinkedList<FormItem> prepareForm(String status, LinkedList<FormItem> forms) {
        LinkedList<FormItem> result = new LinkedList<>();
        if (!TextUtils.isEmpty(status)) {
            FormItem statusItem = new FormItem(R.string.edit_status, status, FormItem.STATUS) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.setStatus(mContext, formItem.value);
                }
            };
            statusItem.setTextLimitInterface(new FormItem.DefaultTextLimiter(App.getAppOptions().getUserStatusMaxLength()));
            result.add(statusItem);
        }
        result.addAll(removeEmptyForms((LinkedList<FormItem>) forms.clone()));
        return result;
    }

    @Override
    protected void configureHolder(ViewHolder holder, FormItem item) {

    }

    private LinkedList<FormItem> removeEmptyForms(LinkedList<FormItem> userForms) {
        Iterator<FormItem> itemsIterator = userForms.iterator();
        while (itemsIterator.hasNext()) {
            FormItem formItem = itemsIterator.next();
            if (TextUtils.isEmpty(formItem.value) || formItem.dataId == 0) {
                itemsIterator.remove();
            }
        }
        return userForms;
    }

}
