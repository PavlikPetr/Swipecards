package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.History;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by saharuk on 24.04.14.
 */
public class EditButtonsAdapter extends BaseAdapter {

    private Context context;
    private History item;
    private int count;
    private ArrayList<String> editButtonsNames;
    private LayoutInflater inflater;

    public EditButtonsAdapter(Context context, History item) {
        this.context = context;
        this.item = item;
        editButtonsNames = new ArrayList<>(Arrays.
                asList(context.getString(R.string.general_copy_title),
                        context.getString(R.string.general_delete_title),
                        context.getString(R.string.general_complain)));
        if (item.target == 0) {
            editButtonsNames.remove(context.getString(R.string.general_complain));
        }
        if (item.type == FeedDialog.GIFT || item.type == FeedDialog.MAP ||
                item.type == FeedDialog.ADDRESS) {
            editButtonsNames.remove(context.getString(R.string.general_copy_title));
        }
        count = editButtonsNames.size();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return editButtonsNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView editOption = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, null);

        editOption.setText(getItem(position).toString());
        return editOption;
    }
}
