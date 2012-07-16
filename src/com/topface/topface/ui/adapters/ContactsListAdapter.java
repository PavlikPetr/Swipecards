package com.topface.topface.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.ui.views.RoundedImageView;

import java.util.zip.Inflater;

public class ContactsListAdapter extends CursorAdapter {
    LayoutInflater mInflater;

    class ViewHolder {
        RoundedImageView mAvatar;
        TextView name;
        TextView phone;
    }

    public ContactsListAdapter(Context context, Cursor c) {
        super(context, c);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View v = mInflater.inflate(R.layout.item_invite, viewGroup, false);
        bindView(v, context, cursor);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = new ViewHolder();
        holder.name = (TextView) view.findViewById(R.id.contactName);
        holder.phone = (TextView) view.findViewById(R.id.contactPhone);
        //holder.mAvatar = (ImageView) view.findViewById(R.id.contactAvatar);
    }
}
