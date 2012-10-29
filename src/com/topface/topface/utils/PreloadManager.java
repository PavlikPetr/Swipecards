package com.topface.topface.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import com.topface.topface.RetryRequestReceiver;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.SearchUser;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.receivers.ConnectionChangeReceiver;

import java.util.LinkedList;

public class PreloadManager {

    int width, height;
    boolean canLoad = true;
    Context mContext;

    public PreloadManager(int width, int height, Context mContext) {
        this.width = width;
        this.height = height;
        this.mContext = mContext;

        checkConnectionType(ConnectionChangeReceiver.getConnectionType());

        BroadcastReceiver mReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                checkConnectionType(intent.getIntExtra(ConnectionChangeReceiver.CONNECTION_TYPE,0));
            }
        };

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, new IntentFilter(RetryRequestReceiver.RETRY_INTENT));
    }

    public PreloadManager(Context mContext) {
        this(0,0,mContext);
    }

    public void preloadPhoto (LinkedList<SearchUser> userList, int position) {
        if(position < userList.size()) {
            preloadNextPhoto(userList.get(position).photos.getFirst());
        }
    }

    public void preloadPhoto (SearchUser currentUser,int position) {
        preloadNextPhoto(currentUser.photos.get(position));
    }

    public void preloadPhoto (Photos photos,int position) {
        if(position<photos.size()) {
            preloadNextPhoto(photos.get(position)); //TODO: Этот код какой-то неправильный, надо что-то придумать
        }
    }

    private void preloadNextPhoto(Photo photo) {
        if (photo != null && canLoad) {
            int size = Math.max(height, width);
            if (size > 0) {
                preloadImage(photo.getSuitableLink(size));
            } else {
                preloadImage(photo.getSuitableLink(Photo.SIZE_960));
            }
        }
    }

    private void preloadImage(String url) {
        getImageLoader().preloadImage(url);
    }

    private DefaultImageLoader getImageLoader() {
        return DefaultImageLoader.getInstance();
    }

    private void checkConnectionType(int type) {
        switch(type) {
            case ConnectionChangeReceiver.CONNECTION_WIFI:
                canLoad = true;
                break;
            case ConnectionChangeReceiver.CONNECTION_OFFLINE:
            case ConnectionChangeReceiver.CONNECTION_MOBILE:
                canLoad = false;
                break;
        }
    }
}