package com.topface.topface.ui.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nineoldandroids.view.ViewHelper;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.FeedPhotoBlog;
import com.topface.topface.data.FeedUser;
import com.topface.topface.ui.views.FeedItemViewConstructor;
import com.topface.topface.utils.ad.NativeAd;

import java.util.ArrayList;
import java.util.List;

public class PhotoBlogListAdapter extends FeedAdapter<FeedPhotoBlog> {

    private OnSympathySent mSympathyListener;
    private List<Integer> mSympathySentArray = new ArrayList<>();

    public interface OnSympathySent {
        void onSympathy(FeedItem item);
    }

    public PhotoBlogListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected FeedItemViewConstructor.TypeAndFlag getViewCreationFlag() {
        return new FeedItemViewConstructor.TypeAndFlag(FeedItemViewConstructor.Type.PHOTOBLOG);
    }

    @Override
    public void addDataFirst(FeedListData<FeedPhotoBlog> data) {
        super.addDataFirst(data);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected View getContentView(final int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        FeedViewHolder holder = (FeedViewHolder) convertView.getTag();
        final FeedPhotoBlog leader = getItem(position);
        if (holder != null) {
            holder.heart.setActivated(isSympathySent(leader.user.id));
            holder.text.setText(leader.user.status);
            ViewHelper.setAlpha(holder.heart, (leader.user.deleted || leader.user.banned) ? 0.1f : 1f);
            holder.heart.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mSympathyListener != null && !isSympathySent(leader.user.id)) {
                        if (isNotOwnId(leader.user.id)) {
                            addNewSympathySent(leader.user.id);
                            mSympathyListener.onSympathy(leader);
                        } else {
                            addNewSympathySent(leader.user.id);
                        }
                    }
                }
            });
        }

        return convertView;
    }

    @Override
    protected FeedViewHolder getEmptyHolder(View convertView, FeedPhotoBlog item) {
        FeedViewHolder holder = super.getEmptyHolder(convertView, item);
        holder.heart = (ImageView) convertView.findViewById(R.id.ifp_heart);
        return holder;
    }

    public void setOnSympathyListener(OnSympathySent listener) {
        mSympathyListener = listener;
    }

    @Override
    public ILoaderRetrierCreator<FeedPhotoBlog> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<FeedPhotoBlog>() {
            @Override
            public FeedPhotoBlog getLoader() {
                FeedPhotoBlog result = new FeedPhotoBlog();
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public FeedPhotoBlog getRetrier() {
                FeedPhotoBlog result = new FeedPhotoBlog();
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }

    @Override
    protected INativeAdItemCreator<FeedPhotoBlog> getNativeAdItemCreator() {
        return new INativeAdItemCreator<FeedPhotoBlog>() {
            @Override
            public FeedPhotoBlog getAdItem(NativeAd nativeAd) {
                return new FeedPhotoBlog(nativeAd);
            }
        };
    }

    private void addNewSympathySent(int id) {
        mSympathySentArray.add(id);
        notifyDataSetChanged();
    }

    public void removeSympathySentId(int id) {
        if (mSympathySentArray != null) {
            for (int i = 0; i < mSympathySentArray.size(); i++) {
                if (mSympathySentArray.get(i) == id) {
                    mSympathySentArray.remove(i);
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean isSympathySent(int id) {
        for (int itemId : mSympathySentArray) {
            if (itemId == id) {
                return true;
            }
        }
        return false;
    }

    // remove not incliding in current data id from symphatysent list
    private void removeOldId() {
        FeedList<FeedPhotoBlog> currentData = getData();

        for (int i = 0; i < mSympathySentArray.size(); i++) {
            boolean isNeverUsed = true;
            for (int j = 0; j < currentData.size(); j++) {
                FeedUser user = currentData.get(j).user;
                if (user != null) {
                    if (user.id == mSympathySentArray.get(i)) {
                        isNeverUsed = false;
                        break;
                    }
                }
            }
            if (isNeverUsed) {
                mSympathySentArray.remove(i);
                i--;
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        removeOldId();
        super.notifyDataSetChanged();
    }

    private boolean isNotOwnId(int id) {
        return App.from(getContext()).getProfile().uid != id;
    }

    public List<Integer> getSympathySentArray() {
        return mSympathySentArray;
    }

    public void setSympathySentArray(List<Integer> array) {
        if (mSympathySentArray != null) {
            mSympathySentArray.clear();
            mSympathySentArray.addAll(array);
        }
    }
}
