package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.ui.LeadersActivity;
import com.topface.topface.ui.views.ImageViewRemote;

public class LeadersPhotoAdapter extends BaseAdapter {
    private LeadersActivity.PhotoSelector mPhotoSelector;
    // class ViewHolder

    static class ViewHolder {
        ImageViewRemote imageView;
        TextView textRating;
        Button checkbox;
    }

    private Photos mAlbumsList;
    private LayoutInflater mInflater;


    public LeadersPhotoAdapter(Context context, Photos albumList, LeadersActivity.PhotoSelector selector) {
        mAlbumsList = albumList;
        mInflater = LayoutInflater.from(context);
        mPhotoSelector = selector;

        sortPhotosByRating();
    }

    private void sortPhotosByRating() {
        int[] likes = new int[mAlbumsList.size()];
        int i = 0;
        for (Photo photo: mAlbumsList) {
            likes[i] = photo.mLiked;
            i++;
        }

        qSort(0, i-1);
    }

    public void qSort(int low, int high) {
        int i = low;
        int j = high;
        int x = mAlbumsList.get((low+high)/2).mLiked;
        do {
            while(mAlbumsList.get(i).mLiked > x) ++i;  // поиск элемента для переноса в старшую часть
            while(mAlbumsList.get(j).mLiked < x) --j;  // поиск элемента для переноса в младшую часть
            if(i <= j){
                // обмен элементов местами:
                Photo temp = mAlbumsList.get(i);
                mAlbumsList.set(i, mAlbumsList.get(j));
                mAlbumsList.set(j, temp);
                // переход к следующим элементам:
                i++; j--;
            }
        } while(i < j);
        if(low < j) qSort(low, j);
        if(i < high) qSort(i, high);
    }

    @Override
    public int getCount() {
        return mAlbumsList.size();
    }

    @Override
    public Photo getItem(int position) {
        return mAlbumsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        Photo item = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.leaders_album_item, parent, false);
            holder.imageView = (ImageViewRemote) convertView.findViewById(R.id.leaderAlbumPhoto);
            holder.textRating = (TextView) convertView.findViewById(R.id.tvRating);
            holder.checkbox = (Button) convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.checkbox.setVisibility(
                mPhotoSelector.getItemId() == position ?
                        View.VISIBLE :
                        View.GONE
        );
        holder.textRating.setText(item.getRate() + "%");
        holder.imageView.setPhoto(item);

        return convertView;
    }

}
