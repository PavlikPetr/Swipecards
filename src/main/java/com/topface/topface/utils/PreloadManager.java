package com.topface.topface.utils;

import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.topface.framework.imageloader.DefaultImageLoader;
import com.topface.topface.App;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.receivers.ConnectionChangeReceiver;

public class PreloadManager<T extends FeedUser> {

    int width, height;
    boolean canLoad = true;

    public PreloadManager(int width, int height) {
        this.width = width;
        this.height = height;

        checkConnectionType(ConnectionChangeReceiver.getConnectionType());
    }

    public PreloadManager() {
        this(0, 0);
    }

    @SuppressWarnings("unchecked")
    public void preloadPhoto(UsersList userList) {
        if (!userList.isEnded()) {
            preloadNextPhoto(((T) userList.get(userList.getSearchPosition() + 1)).photos.getFirst());
        }
    }

    public boolean preloadPhoto(Photos photos, int position) {
        return preloadPhoto(photos, position, null);
    }

    public boolean preloadPhoto(Photos photos, int position, ImageLoadingListener listener) {
        boolean result = false;
        if (position < photos.size()) {
            result = preloadNextPhoto(photos.get(position), listener);
        }
        return result;
    }

    private boolean preloadNextPhoto(Photo photo) {
        return preloadNextPhoto(photo, null);
    }

    private boolean preloadNextPhoto(Photo photo, ImageLoadingListener listener) {
        boolean result = false;
        if (photo != null && canLoad) {
            if (!photo.isFake()) {
                int size = Math.max(height, width);
                if (size > 0) {
                    preloadImage(photo.getSuitableLink(width, height), listener);
                } else {
                    preloadImage(photo.getSuitableLink(Photo.SIZE_960), listener);
                }
                result = true;
            }
        }

        return result;
    }

    private void preloadImage(String url, ImageLoadingListener listener) {
        getImageLoader().preloadImage(url, listener);
    }

    private DefaultImageLoader getImageLoader() {
        return DefaultImageLoader.getInstance(App.getContext());
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