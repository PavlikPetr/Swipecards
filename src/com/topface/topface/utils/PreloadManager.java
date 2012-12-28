package com.topface.topface.utils;

import android.app.Activity;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.SearchUser;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.receivers.ConnectionChangeReceiver;

import java.util.LinkedList;

public class PreloadManager {

    int width, height;
    boolean canLoad = true;
    Activity mActivity;

    public PreloadManager(int width, int height, Activity activity) {
        this.width = width;
        this.height = height;
        this.mActivity = activity;

        checkConnectionType(ConnectionChangeReceiver.getConnectionType());
    }

    public PreloadManager(Activity activity) {
        this(0, 0, activity);
    }

    public void preloadPhoto(LinkedList<SearchUser> userList, int position) {
        if (position < userList.size()) {
            preloadNextPhoto(userList.get(position).photos.getFirst());
        }
    }

    public void preloadPhoto(SearchUser currentUser, int position) {
        preloadNextPhoto(currentUser.photos.get(position));
    }

    public void preloadPhoto(Photos photos, int position) {
        if (position < photos.size()) {
            preloadNextPhoto(photos.get(position));
        }
    }

    private void preloadNextPhoto(Photo photo) {
        if (photo != null && canLoad) {
            int size = Math.max(height, width);
            if (size > 0) {
                preloadImage(photo.getSuitableLink(width, height));
            } else {
                preloadImage(photo.getSuitableLink(Photo.SIZE_960));
            }
        }
    }

    private void preloadImage(String url) {
        getImageLoader().preloadImage(url);
    }

    private DefaultImageLoader getImageLoader() {
        return DefaultImageLoader.getInstance(mActivity);
    }

    public void checkConnectionType(int type) {
        switch (type) {
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