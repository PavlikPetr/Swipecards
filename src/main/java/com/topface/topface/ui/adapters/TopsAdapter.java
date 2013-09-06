package com.topface.topface.ui.adapters;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.Top;
import com.topface.topface.ui.views.ImageViewRemote;

import java.util.LinkedList;

public class TopsAdapter extends BaseAdapter {
    private final LinkedList<Top> mTopsList;

    public TopsAdapter(FragmentActivity activity, LinkedList<Top> topsList) {
        mInflater = LayoutInflater.from(activity);
        mTopsList = topsList;
    }

    static class ViewHolder {
        public ImageViewRemote photo;
        public TextView name;
        public TextView rating;
        public View online;
    }

    // Data
    private LayoutInflater mInflater;

    @Override
    public int getCount() {
        return mTopsList.size();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_top, null, false);
            holder = new ViewHolder();
            holder.photo = (ImageViewRemote) convertView.findViewById(R.id.ivTG);
            holder.rating = (TextView) convertView.findViewById(R.id.topsRating);
            holder.name = (TextView) convertView.findViewById(R.id.topsName);
            holder.online = convertView.findViewById(R.id.ivOnline);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Top topUser = mTopsList.get(position);
        holder.rating.setText(topUser.liked + "%");
        holder.name.setText(topUser.getNameAndAge());
        holder.photo.setPhoto(topUser.photo);
        holder.online.setVisibility(topUser.online ? View.VISIBLE : View.GONE);
        return convertView;
    }


    @Override
    public Object getItem(int position) {
        return null;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


}
