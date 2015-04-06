package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.adapters.GiftsStripAdapter;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class UserFormListAdapter extends AbstractFormListAdapter {

    public UserFormListAdapter(Context context) {
        super(context);
    }

    @Override
    protected LinkedList<FormItem> prepareForm(LinkedList<FormItem> forms) {
        return removeEmptyForms((LinkedList<FormItem>) forms.clone());
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
