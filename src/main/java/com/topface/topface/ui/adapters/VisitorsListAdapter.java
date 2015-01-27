package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.Visitor;
import com.topface.topface.ui.views.FeedItemViewConstructor;
import com.topface.topface.utils.ad.NativeAd;

public class VisitorsListAdapter extends FeedAdapter<Visitor> {

    public VisitorsListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected FeedAdapter.FeedViewHolder getEmptyHolder(View convertView, Visitor item) {
        FeedViewHolder holder = super.getEmptyHolder(convertView, item);
        holder.time = (TextView) convertView.findViewById(R.id.ifp_time);
        return holder;
    }

    @Override
    protected FeedItemViewConstructor.TypeAndFlag getViewCreationFlag() {
        return new FeedItemViewConstructor.TypeAndFlag(FeedItemViewConstructor.Type.TIME);
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        FeedViewHolder holder = (FeedViewHolder) convertView.getTag();

        Visitor visitor = getItem(position);
        holder.time.setText(visitor.createdRelative);
        holder.time.setVisibility(View.VISIBLE);
        return convertView;
    }

    @Override
    public ILoaderRetrierCreator<Visitor> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<Visitor>() {
            @Override
            public Visitor getLoader() {
                Visitor result = new Visitor();
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public Visitor getRetrier() {
                Visitor result = new Visitor();
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }

    @Override
    protected INativeAdItemCreator<Visitor> getNativeAdItemCreator() {
        return new INativeAdItemCreator<Visitor>() {
            @Override
            public Visitor getAdItem(NativeAd nativeAd) {
                return new Visitor(nativeAd);
            }
        };
    }
}
