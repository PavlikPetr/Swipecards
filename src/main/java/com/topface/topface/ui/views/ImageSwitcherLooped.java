package com.topface.topface.ui.views;

import android.content.Context;
import android.util.AttributeSet;

public class ImageSwitcherLooped extends ImageSwitcher {

    public static final int ITEMS_MAX = Integer.MAX_VALUE;
    public static final int ITEMS_HALF = Integer.MAX_VALUE / 2;

    public ImageSwitcherLooped(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected ImageSwitcherAdapter createImageSwitcherAdapter() {
        return new ImageSwitcherLoopedAdapter();
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
    }

    public class ImageSwitcherLoopedAdapter extends ImageSwitcherAdapter {
        @Override
        public int getCount() {
            if (mPhotoLinks != null) {
                if (mPhotoLinks.size() > 1) {
                    return ITEMS_MAX;
                } else {
                    return 1;
                }
            }
            return 0;
        }

        @Override
        public int getRealPosition(int position) {
            int photosCount = mPhotoLinks.size();
            return mPhotoLinks == null || photosCount == 0 ? position : position % photosCount;
        }
    }
}
