package com.topface.topface.utils;

import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.topface.framework.imageloader.DefaultImageLoader;
import com.topface.topface.App;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.receivers.ConnectionChangeReceiver;

import static com.topface.topface.ui.dialogs.PreloadPhotoSelectorTypes.ALWAYS_ON;
import static com.topface.topface.ui.dialogs.PreloadPhotoSelectorTypes.PRELOAD_OFF;
import static com.topface.topface.ui.dialogs.PreloadPhotoSelectorTypes.WIFI;

public class PreloadManager<T extends FeedUser> {

    int width, height;
    boolean canLoad = true;

    public PreloadManager(int width, int height) {
        this.width = width;
        this.height = height;

        checkConnectionType();
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

    public void checkConnectionType() {
        canLoad = isPreloadAllowed();
    }

    public static boolean isPreloadAllowed() {
        ConnectionChangeReceiver.ConnectionType connectionType = Utils.getConnectionType();
        int userPreloadTypeId;
        if (App.getUserConfig() != null) {
            userPreloadTypeId = App.getUserConfig().getPreloadPhotoType().getId();
        } else {
            return false;
        }
        switch (connectionType) {
            case CONNECTION_OFFLINE:
                return false;
            case CONNECTION_MOBILE_3G:
                if (userPreloadTypeId == WIFI.getId() ||
                        userPreloadTypeId == PRELOAD_OFF.getId()) {
                    return false;
                }
                return true;
            case CONNECTION_MOBILE_EDGE:
                if (userPreloadTypeId == ALWAYS_ON.getId()) {
                    return true;
                }
                return false;
            case CONNECTION_WIFI:
                if (userPreloadTypeId == PRELOAD_OFF.getId()) {
                    return false;
                }
                return true;
            default:
                return true;
        }
    }
}