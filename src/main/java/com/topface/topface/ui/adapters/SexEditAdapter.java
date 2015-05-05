package com.topface.topface.ui.adapters;

import android.content.Context;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.FormItem;

/**
 * Adapter for sex.
 */
public class SexEditAdapter extends FormItemEditAdapter {
    public SexEditAdapter(Context context, FormItem formItem) {
        super(context, formItem);
    }

    @Override
    protected String[] createEntries(FormItem formItem) {
        Context context = App.getContext();
        return new String[]{context.getString(R.string.boy), context.getString(R.string.girl)};
    }

    @Override
    protected int[] createIds(FormItem formItem) {
        return new int[]{1, 0};
    }
}
