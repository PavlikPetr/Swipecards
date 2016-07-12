package com.topface.topface.ui.fragments.profile.photoswitcher;

import com.topface.topface.data.Photos;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

import static com.topface.topface.ui.fragments.profile.photoswitcher.view.PhotoSwitcherActivity.DEFAULT_PRELOAD_ALBUM_RANGE;

public class PhotosManager {
    private int mLimit;
    private IUploadAlbumPhotos mIUploadAlbumPhotos;

    public PhotosManager(IUploadAlbumPhotos listener) {
        mIUploadAlbumPhotos = listener;
        LoadController loadController = new AlbumLoadController(AlbumLoadController.FOR_PREVIEW);
        mLimit = loadController.getItemsLimitByConnectionType();
    }

    /**
     * sends request for photo data load, if need
     *
     * @param photos   array of photos
     * @param position _real_ index of current photo in this array
     */
    public void check(final Photos photos, final int position) {
        int indexToLeft = calcRightIndex(photos, position - DEFAULT_PRELOAD_ALBUM_RANGE);
        if (photos.get(indexToLeft).isFake()) {
            if (mIUploadAlbumPhotos != null) {
                mIUploadAlbumPhotos.sendRequest(calcRightIndex(photos, position - mLimit));
            }
        }
        int indexToRight = calcRightIndex(photos, position + DEFAULT_PRELOAD_ALBUM_RANGE);
        if (photos.get(indexToRight).isFake()) {
            if (mIUploadAlbumPhotos != null) {
                mIUploadAlbumPhotos.sendRequest(calcRightIndex(photos, position));
            }
        }
    }

    /**
     * converts some index (negative for example) to fit in array size
     *
     * @param photos source array
     * @param index  some index to fit
     * @return correct index, fitted in array size
     */
    private int calcRightIndex(final Photos photos, final int index) {
        if (index < 0) {
            int res = index;
            while (res < 0) {
                res += photos.size();
            }
            return res;
        } else if (index >= photos.size()) {
            int res = index;
            while (res >= photos.size()) {
                res -= 1;
            }
            return res;
        }
        return index;
    }
}