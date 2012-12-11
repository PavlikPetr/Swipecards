package com.topface.topface.utils.http;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.topface.topface.R;

public class ProfileBackgrounds {
    public static final int DEFAULT_BACKGROUND_ID = 1;
    public static final int DEFAULT_BACKGROUND_RES_ID = R.drawable.profile_background_1;

    private static BackgroundPair[] getPairs(Context context) {
        TypedArray backgrounds = context.getResources().obtainTypedArray(R.array.profile_background_images);
        int[] backgroundsIds = context.getResources().getIntArray(R.array.profile_background_images_ids);

        if (backgroundsIds.length == backgrounds.length()) {
            BackgroundPair[] result = new BackgroundPair[backgroundsIds.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = new BackgroundPair();
                result[i].resId = backgrounds.getResourceId(i, DEFAULT_BACKGROUND_RES_ID);
                result[i].id = backgroundsIds[i];
            }
            return result;
        }

        return null;
    }

    public static int getBackgroundResource(Context context, int id) {
        BackgroundPair[] backgroundPairs = getPairs(context);

        for (BackgroundPair backgroundPair : backgroundPairs) {
            if (backgroundPair.id == id) {
                return backgroundPair.resId;
            }
        }

        return DEFAULT_BACKGROUND_RES_ID;
    }

    public static int[] getAllBackgroundResources(Context context) {
        BackgroundPair[] backgroundPairs = getPairs(context);

        int[] result = new int[backgroundPairs.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = backgroundPairs[i].resId;
        }
        return result;
    }

    public static int[] getAllBackgroundIds(Context context) {
        return context.getResources().getIntArray(R.array.profile_background_images_ids);
    }

    public static ResourceBackgroundItem getResourceBackgroundItem(Context context, int id) {
        for (BackgroundPair pair : getPairs(context)) {
            if (pair.id == id) {
                return new ResourceBackgroundItem(context.getResources(), pair.resId, pair.id);
            }
        }
        return new ResourceBackgroundItem(context.getResources(), DEFAULT_BACKGROUND_RES_ID, DEFAULT_BACKGROUND_ID);
    }

    private static class BackgroundPair {
        int resId;
        int id;
    }

    public static interface BackgroundItem {
        public Bitmap getBitmap();

        public boolean isSelected();

        public BackgroundItem setSelected(boolean selected);
    }

    public static class ResourceBackgroundItem implements BackgroundItem {
        private Bitmap mBitmap;
        private boolean selected;
        private int mId;

        public ResourceBackgroundItem(Resources resources, int resId, int id) {
            mBitmap = BitmapFactory.decodeResource(resources, resId);
            mId = id;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public boolean isSelected() {
            return selected;
        }

        public BackgroundItem setSelected(boolean selected) {
            this.selected = selected;
            return this;
        }

        public int getId() {
            return mId;
        }
    }
}
