package com.topface.topface.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;

/**
 * Adapter for text forms edit dialog
 */
public class TextItemEditAdapter extends AbstractEditAdapter<FormItem> {

    private final FormItem mFormItem;

    public TextItemEditAdapter(FormItem formItem) {
        mFormItem = new FormItem(formItem);
    }

    @Override
    public FormItem getData() {
        return mFormItem;
    }

    @Override
    public void saveData() {
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public String getItem(int position) {
        return mFormItem.value;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflate(R.layout.edit_dialog_text, parent);

            Holder holder = new Holder();
            holder.textView = (TextView) convertView.findViewById(R.id.editor_text);
            convertView.setTag(holder);
        }

        Holder holder = (Holder) convertView.getTag();
        holder.textView.setText(getItem(position));

        return convertView;
    }

    private static class Holder {
        TextView textView;
    }
}
