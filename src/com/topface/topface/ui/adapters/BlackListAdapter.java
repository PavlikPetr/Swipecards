package com.topface.topface.ui.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import com.topface.topface.R;
import com.topface.topface.data.BlackListItem;

import java.util.ArrayList;

public class BlackListAdapter extends FeedAdapter<BlackListItem> {

    public static final int LIMIT = 100;

    public static final int ITEM_LAYOUT = R.layout.item_feed_black_list;
    public static final int ANIMATION_DURATION = 250;
    private boolean mEditMode;
    private boolean mFirstRun = true;
    private SparseArray<Boolean> mItemsForDelete = new SparseArray<Boolean>();

    public BlackListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected int getItemLayout() {
        return ITEM_LAYOUT;
    }

    @Override
    protected int getNewItemLayout() {
        return ITEM_LAYOUT;
    }

    public void toggleEditMode() {
        mFirstRun = false;

        mEditMode = !mEditMode;
        notifyDataSetChanged();

        mItemsForDelete.clear();
    }

    public boolean isEditMode() {
        return mEditMode;
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        final FeedViewHolder holder = (FeedViewHolder) convertView.getTag();

        final BlackListItem like = getItem(position);

        if (mItemsForDelete.get(position, false)) {
            holder.deleteIndicator.setImageResource(R.drawable.ic_delete_cross_pressed);
        } else {
            holder.deleteIndicator.setImageResource(R.drawable.ic_delete_cross);
        }

        //Показываем анимацию только при переключении, а не при изначальной загрузке
        if (!mFirstRun) {
            setAnimation(holder);
        }

        return convertView;
    }

    private void setAnimation(final FeedViewHolder holder) {
        Animation animation = isEditMode() ?
                new TranslateAnimation(0f, 80f, 0f, 0f) :
                new TranslateAnimation(80f, 0f, 0, 0f);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setFillBefore(true);
        animation.setFillAfter(true);
        animation.setDuration(ANIMATION_DURATION);

        holder.dataLayout.setAnimation(animation);
    }

    @Override
    protected FeedViewHolder getEmptyHolder(View convertView, BlackListItem item) {
        FeedViewHolder holder = super.getEmptyHolder(convertView, item);
        holder.dataLayout = convertView.findViewById(R.id.animationLayout);
        holder.deleteIndicator = (ImageView) convertView.findViewById(R.id.deleteCross);

        return holder;
    }

    public void toggleItemDeleteMark(int id) {
        FeedList<BlackListItem> data = getData();
        if (data.hasItem(id) && !mItemsForDelete.get(id, false)) {
            mItemsForDelete.put(id, true);
        }
        else {
            mItemsForDelete.remove(id);
        }
        notifyDataSetChanged();
    }

    public ArrayList<Integer> getMarkedForDelete() {
        ArrayList<Integer> array = new ArrayList<Integer>();
        for (int i = 0; i < mItemsForDelete.size(); i++) {
            array.add(getItem(i).user.id);
        }
        return array;
    }

    public void removeDeleted() {
        for (int i = 0; i < mItemsForDelete.size(); i++) {
            boolean result = false;
            FeedList<BlackListItem> data = getData();
            if (data.hasItem(i)) {
                getData().remove(i);
            }
        }
        notifyDataSetChanged();
    }

}
