package com.topface.topface.ui.adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.topface.framework.imageloader.DefaultImageLoader;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.Photo;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ResourcesUtils;

import java.util.ArrayList;

public class LeftMenuAdapter extends BaseAdapter {
    public static final int TYPE_MENU_BUTTON = 0;
    public static final int TYPE_MENU_BUTTON_WITH_BADGE = 1;
    public static final int TYPE_MENU_BUTTON_WITH_PHOTO = 2;
    private static final int TYPE_COUNT = 3;
    private ArrayList<ILeftMenuItem> mItems;
    private CountersData mCountersData;

    public LeftMenuAdapter(ArrayList<ILeftMenuItem> items) {
        mItems = items;
    }

    public void replaceMenuItems(ArrayList<ILeftMenuItem> items) {
        mItems = items;
    }

    public static ILeftMenuItem newLeftMenuItem(BaseFragment.FragmentSettings menuId, int menuType,
                                                int menuIcon) {
        return newLeftMenuItem(menuId, menuType, menuIcon, null, null);
    }

    public static ILeftMenuItem newLeftMenuItem(BaseFragment.FragmentSettings menuId, int menuType,
                                                int menuIcon, String menuIconUrl) {
        return newLeftMenuItem(menuId, menuType, menuIcon, null, menuIconUrl);
    }

    public static ILeftMenuItem newLeftMenuItem(BaseFragment.FragmentSettings menuId, int menuType,
                                                Photo menuIcon) {
        return newLeftMenuItem(menuId, menuType, -1, menuIcon, null);
    }

    public static ILeftMenuItem newLeftMenuItem(final BaseFragment.FragmentSettings menuId, final int menuType,
                                                final int menuIconResId, final Photo menuIconPhoto, final String menuIconUrl) {
        return new ILeftMenuItem() {

            private Photo menuPhoto = menuIconPhoto;
            private int menuExtraIconId = -1;

            @Override
            public BaseFragment.FragmentSettings getMenuId() {
                return menuId;
            }

            @Override
            public int getMenuType() {
                return menuType;
            }

            @Override
            public String getMenuText() {
                    return ResourcesUtils.getFragmentNameResId(menuId);
            }

            @Override
            public int getMenuIconResId() {
                return menuIconResId;
            }

            @Override
            public Photo getMenuIconPhoto() {
                return menuPhoto;
            }

            @Override
            public String getMenuIconUrl() {
                return menuIconUrl;
            }

            @Override
            public void setMenuIconPhoto(Photo photo) {
                menuPhoto = photo;
            }

            @Override
            public int getExtraIconDrawable() {
                return menuExtraIconId;
            }

            @Override
            public void setExtraIconDrawable(int resId) {
                menuExtraIconId = resId;
            }
        };
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public ILeftMenuItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // initialize holder and convertView
        final ViewHolder holder;
        // get menu item on current position
        final ILeftMenuItem item = getItem(position);
        int type = item.getMenuType();
        //init convertView
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(
                    parent.getContext(),
                    type == TYPE_MENU_BUTTON_WITH_PHOTO
                            ? R.layout.item_left_menu_button_with_photo
                            : R.layout.item_left_menu_button_with_badge,
                    null
            );
            holder.icon = (ImageView) convertView.findViewById(R.id.image_icon);
            holder.extraIcon = (ImageView) convertView.findViewById(R.id.image_badge);
            holder.btnMenu = (TextView) convertView.findViewById(R.id.btnMenu);
            holder.counterBadge = (TextView) convertView.findViewById(R.id.tvCounterBadge);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            // unregister previous non-visible item from updates
            holder.item = item;
        }
        // initiate views' state in holder
        holder.icon.setImageDrawable(null);
        switch (type) {
            case TYPE_MENU_BUTTON:
                holder.btnMenu.setText(item.getMenuText());
                holder.counterBadge.setVisibility(View.GONE);
                holder.icon.setBackgroundResource(item.getMenuIconResId());
                break;
            case TYPE_MENU_BUTTON_WITH_BADGE:
                holder.btnMenu.setText(item.getMenuText());
                holder.icon.setBackgroundResource(item.getMenuIconResId());
                if (mCountersData != null) {
                    updateCountersBadge(holder.counterBadge, mCountersData.getCounterByFragmentId(item.getMenuId()));
                }
                break;
            case TYPE_MENU_BUTTON_WITH_PHOTO:
                holder.btnMenu.setText(item.getMenuText());
                if (holder.icon instanceof ImageViewRemote) {
                    ((ImageViewRemote) holder.icon).setPhoto(CacheProfile.photo);
                }
                if (holder.extraIcon != null) {
                    int extraIconDrawable = item.getExtraIconDrawable();
                    if (extraIconDrawable > 0) {
                        holder.extraIcon.setImageResource(extraIconDrawable);
                        holder.extraIcon.setVisibility(View.VISIBLE);
                    } else {
                        holder.extraIcon.setVisibility(View.GONE);
                    }
                }
            default:
                break;
        }
        holder.item = item;
        if (!TextUtils.isEmpty(item.getMenuIconUrl())) {
            holder.icon.setBackgroundResource(0);
            DefaultImageLoader.getInstance(App.getContext()).preloadImage(item.getMenuIconUrl(), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    holder.icon.setImageDrawable(new BitmapDrawable(App.getContext().getResources(), loadedImage));
                }
            });
        }
        return convertView;
    }

    public void updateCounters(CountersData countersData) {
        mCountersData = countersData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getMenuType();
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    private void updateCountersBadge(TextView view, int value) {
        if (view != null) {
            view.setVisibility(value > 0 ? View.VISIBLE : View.INVISIBLE);
            view.setText(String.valueOf(value));
        }
    }

    public View getViewForActivate(ListView listView, BaseFragment.FragmentSettings fragmentSettings) {
        View view = null;
        for (int j = 0; j <= getCount(); j++) {
            View wantedView = listView.getChildAt(j);
            if (wantedView == null) {
                continue;
            }
            Object viewHolder = wantedView.getTag();
            if (viewHolder == null || !(viewHolder instanceof LeftMenuAdapter.ViewHolder)) {
                continue;
            }
            BaseFragment.FragmentSettings item = ((LeftMenuAdapter.ViewHolder) viewHolder).item.getMenuId();
            if (fragmentSettings == item) {
                wantedView.setActivated(true);
                view = wantedView;
            } else {
                wantedView.setActivated(false);
            }
        }
        return view;
    }

    private class ViewHolder {
        TextView btnMenu;
        TextView counterBadge;
        ILeftMenuItem item;
        ImageView icon;
        ImageView extraIcon;
    }

    public interface ILeftMenuItem {
        BaseFragment.FragmentSettings getMenuId();

        int getMenuType();

        String getMenuText();

        int getMenuIconResId();

        Photo getMenuIconPhoto();

        String getMenuIconUrl();

        void setMenuIconPhoto(Photo photo);

        @DrawableRes
        int getExtraIconDrawable();

        void setExtraIconDrawable(@DrawableRes int resId);
    }
}
