package com.topface.topface.ui.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.History;

/**
 * Adapter for chat editing options. They can vary depending on sender(own, others' messages),
 * messege type(text, gift, geo).
 */
public class EditButtonsAdapter extends BaseAdapter {

    public static final int ITEM_COPY = 1;
    public static final int ITEM_COMPLAINT = 2;
    public static final int ITEM_DELETE = 3;

    private SparseArray<String> mEditButtonsNames;
    private LayoutInflater mInflater;

    public EditButtonsAdapter(Context context, History item) {
        mEditButtonsNames = new SparseArray<>(3);
        mEditButtonsNames.append(ITEM_DELETE, context.getString(R.string.general_delete_title));
        if (item.type != FeedDialog.GIFT && item.type != FeedDialog.MAP &&
                item.type != FeedDialog.ADDRESS) {
            mEditButtonsNames.append(ITEM_COPY, context.getString(R.string.general_copy_title));
        }
        if (item.target != 0) {
            mEditButtonsNames.append(ITEM_COMPLAINT, context.getString(R.string.general_complain));
        }
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mEditButtonsNames.size();
    }

    @Override
    public Object getItem(int position) {
        return mEditButtonsNames.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return mEditButtonsNames.keyAt(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView editOption = (TextView) mInflater.inflate(android.R.layout.simple_list_item_1, null);

        editOption.setText(getItem(position).toString());
        return editOption;
    }
}
