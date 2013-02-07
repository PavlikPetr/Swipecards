package com.topface.topface.ui.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.topface.topface.R;
import com.topface.topface.data.BlackListItem;

import java.util.ArrayList;

public class BlackListAdapter extends FeedAdapter<BlackListItem> {

    public static final int LIMIT = 100;

    public static final int ITEM_LAYOUT = R.layout.item_feed_black_list;
    public static final int ANIMATION_DURATION = 250;
    public static final float TO_X_DELTA = 80f;
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

    @Override
    protected int getVipItemLayout() {
        return ITEM_LAYOUT;
    }

    @Override
    protected int getNewVipItemLayout() {
        return ITEM_LAYOUT;
    }

    public void toggleEditMode() {
        mFirstRun = false;

        mEditMode = !mEditMode;
        notifyDataSetChanged();
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

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.dataLayout.getLayoutParams();
        if (isEditMode()) {
            params.leftMargin = (int) TO_X_DELTA;
        } else {
            params.leftMargin = 0;
        }

        holder.dataLayout.setLayoutParams(params);

        //Показываем анимацию только при переключении, а не при изначальной загрузке
        if (!mFirstRun) {
            //setAnimation(holder);
        }

        return convertView;
    }

    private void setAnimation(final FeedViewHolder holder) {
        Animation animation = isEditMode() ?
                new TranslateAnimation(0f, TO_X_DELTA, 0f, 0f) :
                new TranslateAnimation(TO_X_DELTA, 0f, 0, 0f);
        animation.setInterpolator(new AccelerateInterpolator());
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
        } else {
            mItemsForDelete.remove(id);
        }
        notifyDataSetChanged();
    }

    public ArrayList<Integer> getMarkedForDelete() {
        ArrayList<Integer> array = new ArrayList<Integer>();
        for (int i = 0; i < mItemsForDelete.size(); i++) {
            array.add(getItem(mItemsForDelete.keyAt(i)).user.id);
        }
        return array;
    }

    public void removeDeleted() {
        FeedList<BlackListItem> data = getData();
        for (int i = 0; i < mItemsForDelete.size(); i++) {
            boolean result = false;
            if (data.hasItem(mItemsForDelete.keyAt(i))) {
                data.remove(mItemsForDelete.keyAt(i));
            }
        }
        mItemsForDelete.clear();
        setData(data);
    }

    @Override
    public ILoaderRetrierCreator<BlackListItem> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<BlackListItem>() {
            @Override
            public BlackListItem getLoader() {
                BlackListItem result = new BlackListItem(null);
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public BlackListItem getRetrier() {
                BlackListItem result = new BlackListItem(null);
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }
}
