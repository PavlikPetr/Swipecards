package com.topface.topface.utils.http;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.topface.topface.R;

import java.util.LinkedList;

public class ProfileBackgrounds {
    public static final int DEFAULT_BACKGROUND_ID = 1;
    public static final int DEFAULT_BACKGROUND_RES_ID = R.drawable.profile_background_1;

    private static BackgroundPair[] getPairs(Context context) {
        BackgroundPair[] result = null;

        TypedArray backgrounds = getBackgroundsTypedArray(context);
        int[] backgroundsIds = getBackgroundIds(context);
        TypedArray vipBackgrounds = getVipBackgroundsTypedArray(context);
        int[] vipBackgroundsIds = getVipBackgroundIds(context);

        if (backgroundsIds.length == backgrounds.length() && vipBackgroundsIds.length == vipBackgrounds.length()) {
            result = new BackgroundPair[backgroundsIds.length + vipBackgroundsIds.length];
            for (int i = 0; i < backgroundsIds.length; i++) {
                result[i] = new BackgroundPair();
                result[i].resId = backgrounds.getResourceId(i, DEFAULT_BACKGROUND_RES_ID);
                result[i].id = backgroundsIds[i];
                result[i].vip = false;
            }

            int j = 0;
            for (int i = backgroundsIds.length; i < result.length; i++, j++) {
                result[i] = new BackgroundPair();
                result[i].resId = vipBackgrounds.getResourceId(j, DEFAULT_BACKGROUND_RES_ID);
                result[i].id = vipBackgroundsIds[j];
                result[i].vip = true;
            }
        }
        return result;
    }

    private static TypedArray getBackgroundsTypedArray(Context context) {
        return context.getResources().obtainTypedArray(R.array.profile_background_images);
    }

    public static int[] getBackgroundIds(Context context) {
        return context.getResources().getIntArray(R.array.profile_background_images_ids);
    }

    private static TypedArray getVipBackgroundsTypedArray(Context context) {
        return context.getResources().obtainTypedArray(R.array.profile_vip_background_images);
    }

    public static int[] getVipBackgroundIds(Context context) {
        return context.getResources().getIntArray(R.array.profile_vip_background_images_ids);
    }

    public static LinkedList<BackgroundItem> getBackgroundItems(Context context) {
        LinkedList<BackgroundItem> result = new LinkedList<BackgroundItem>();
        for(BackgroundPair pair : getPairs(context)) {
            result.add(new ResourceBackgroundItem(context.getResources(), pair.resId, pair.id,pair.vip));
        }
        return result;
    }

    public static LinkedList<BackgroundItem> getBackgroundItems(Context context, int selectedId) {
        LinkedList<BackgroundItem> result = new LinkedList<BackgroundItem>();
        for(BackgroundPair pair : getPairs(context)) {
            result.add(new ResourceBackgroundItem(context.getResources(), pair.resId, pair.id,pair.vip,selectedId == pair.id));
        }
        return result;
    }

    public static ResourceBackgroundItem getResourceBackgroundItem(Context context, int id) {
        for (BackgroundPair pair : getPairs(context)) {
            if (pair.id == id) {
                return new ResourceBackgroundItem(context.getResources(), pair.resId, pair.id, pair.vip);
            }
        }
        return new ResourceBackgroundItem(context.getResources(), DEFAULT_BACKGROUND_RES_ID, DEFAULT_BACKGROUND_ID);
    }

    public static int getBackgroundResource(Context context, int id) {
        BackgroundPair[] pairs = getPairs(context);

        for (BackgroundPair pair : pairs) {
            if (pair.id == id) {
                return pair.resId;
            }
        }

        return DEFAULT_BACKGROUND_RES_ID;
    }

    private static class BackgroundPair {
        int resId;
        int id;
        boolean vip = false;
    }

    public static interface BackgroundItem {
        public Bitmap getBitmap();

        public boolean isSelected();

        public BackgroundItem setSelected(boolean selected);

        public boolean isForVip();
    }

    public static class ResourceBackgroundItem implements BackgroundItem {
        private Bitmap mBitmap;
        private boolean mSelected;
        private int mId;
        private boolean mVip;

        public ResourceBackgroundItem(Resources resources, int resId, int id) {
            mBitmap = BitmapFactory.decodeResource(resources, resId);
            mId = id;
            mVip = false;
        }

        public ResourceBackgroundItem(Resources resources, int resId, int id, boolean vip) {
            mBitmap = BitmapFactory.decodeResource(resources, resId);
            mId = id;
            mVip = vip;
        }

        public ResourceBackgroundItem(Resources resources, int resId, int id, boolean vip, boolean selected) {
            mBitmap = BitmapFactory.decodeResource(resources, resId);
            mId = id;
            mVip = vip;
            mSelected = selected;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public boolean isSelected() {
            return mSelected;
        }

        public BackgroundItem setSelected(boolean selected) {
            this.mSelected = selected;
            return this;
        }

        public int getId() {
            return mId;
        }

        public boolean isForVip() {
            return mVip;
        }
    }
}
