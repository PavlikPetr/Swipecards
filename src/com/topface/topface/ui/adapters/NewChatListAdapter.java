package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.data.History;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.AddressesCache;

import java.text.SimpleDateFormat;
import java.util.HashMap;


public class NewChatListAdapter extends LoadingListAdapter<History> implements AbsListView.OnScrollListener{

    FeedList<History> mInternalData;

    static class ViewHolder {
        ImageViewRemote avatar;
        TextView message;
        TextView date;
        ImageViewRemote gift;
        ImageView mapBackground;
        ProgressBar prgsAddress;
        Button likeRequest;
        View userInfo;
    }

    private static final int T_USER_PHOTO = 3;
    private static final int T_USER_EXT = 4;
    private static final int T_FRIEND_PHOTO = 5;
    private static final int T_FRIEND_EXT = 6;
    private static final int T_DATE = 7;
    private static final int T_USER_GIFT_PHOTO = 8;
    private static final int T_USER_GIFT_EXT = 9;
    private static final int T_FRIEND_GIFT_PHOTO = 10;
    private static final int T_FRIEND_GIFT_EXT = 11;
    private static final int T_USER_MAP_PHOTO = 12;
    private static final int T_USER_MAP_EXT = 13;
    private static final int T_FRIEND_MAP_PHOTO = 14;
    private static final int T_FRIEND_MAP_EXT = 15;
    private static final int T_USER_REQUEST = 16;
    private static final int T_USER_REQUEST_EXT = 17;
    private static final int T_FRIEND_REQUEST = 18;
    private static final int T_FRIEND_REQUEST_EXT = 19;

    private static final int T_COUNT = 20;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm");

    private HashMap<Integer, String> mItemTimeList;
    private View.OnClickListener mOnClickListener;
    private ChatFragment.OnListViewItemLongClickListener mLongClickListener;
    private AddressesCache mAddressesCache;

    public NewChatListAdapter(Context context,FeedList<History> data, Updater updateCallback) {
        super(context,data, updateCallback);
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        return null;
    }

    public void setOnItemLongClickListener(ChatFragment.OnListViewItemLongClickListener l) {
        mLongClickListener = l;
    }

    public void setOnAvatarListener(View.OnClickListener onAvatarListener) {
        mOnClickListener = onAvatarListener;
    }

    @Override
    public ILoaderRetrierCreator<History> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<History>() {
            @Override
            public History getLoader() {
                History result = new History();
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                result.created = 0;
                return result;
            }

            @Override
            public History getRetrier() {
                History result = new History();
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                result.created = 0;
                return result;
            }
        };
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {
            mUpdateCallback.onUpdate();
        }
    }
}
