package com.topface.topface.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class LeadersAlbumAdapter extends BaseAdapter {
   /* private LeadersActivity.PhotoSelector mPhotoSelector;
    // class ViewHolder

    static class ViewHolder {
        ImageView imageView;
        ImageView checkbox;
    }

    private LinkedList<Album> mAlbumsList;
    private LayoutInflater mInflater;


    public LeadersAlbumAdapter(Context context, LinkedList<Album> albumList, LeadersActivity.PhotoSelector selector) {
        mAlbumsList = albumList;
        mInflater = LayoutInflater.from(context);
        mAlbumsList.addFirst(null);
        mPhotoSelector = selector;
    }

    @Override
    public int getCount() {
        return mAlbumsList.size();
    }


    @Override
    public Album getItem(int position) {
        return mAlbumsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.leaders_album_item, parent, false);
            holder.imageView = (ImageView) convertView.findViewById(R.id.leaderAlbumPhoto);
            holder.checkbox = (ImageView) convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == 0 && getItem(position) == null) {
            holder.imageView.setImageResource(R.drawable.btn_leaders_add_photo);
            holder.checkbox.setVisibility(View.GONE);
        } else {
            holder.checkbox.setVisibility(
                    mPhotoSelector.getItemId() == position ?
                            View.VISIBLE :
                            View.GONE
            );
            loadingImage(position, holder.imageView);
        }

        return convertView;
    }

    public void loadingImage(final int position, ImageView view) {
        DefaultImageLoader.getInstance().displayImage(
                getItem(position).getBigLink(),
                view
        );
    }*/

    @Override
    public int getCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getItem(int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getItemId(int i) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
