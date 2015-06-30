package com.topface.topface.ui.adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

import java.util.HashMap;

public class LeftMenuAdapter extends BaseAdapter {
    public static final int TYPE_MENU_BUTTON = 0;
    public static final int TYPE_MENU_BUTTON_WITH_BADGE = 1;
    public static final int TYPE_MENU_BUTTON_WITH_PHOTO = 2;
    private static final int TYPE_COUNT = 3;
    private final SparseArray<ILeftMenuItem> mItems;
    private HashMap<BaseFragment.FragmentId, TextView> mCountersBadgesMap = new HashMap<>();

    public LeftMenuAdapter(SparseArray<ILeftMenuItem> items) {
        mItems = items;
    }

    public static ILeftMenuItem newLeftMenuItem(BaseFragment.FragmentId menuId, int menuType,
                                                int menuIcon) {
        return newLeftMenuItem(menuId, menuType, menuIcon, null);
    }

    public static ILeftMenuItem newLeftMenuItem(BaseFragment.FragmentId menuId, int menuType,
                                                Photo menuIcon) {
        return newLeftMenuItem(menuId, menuType, -1, menuIcon);
    }

    public static ILeftMenuItem newLeftMenuItem(final BaseFragment.FragmentId menuId, final int menuType,
                                                final int menuIconResId, final Photo menuIconPhoto) {
        return new ILeftMenuItem() {

            private Photo menuPhoto = menuIconPhoto;
            private int menuExtraIconId = -1;

            @Override
            public BaseFragment.FragmentId getMenuId() {
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
        return mItems.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(ILeftMenuItem item) {
        mItems.put(item.getMenuId().getId(), item);
    }

    public boolean hasFragment(BaseFragment.FragmentId id) {
        return mItems.size() > id.getId();
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
            mCountersBadgesMap.remove(item.getMenuId());
            holder.item = item;
        }
        // initiate views' state in holder
        switch (type) {
            case TYPE_MENU_BUTTON:
                holder.btnMenu.setText(item.getMenuText());
                holder.counterBadge.setVisibility(View.GONE);
                holder.icon.setBackgroundResource(item.getMenuIconResId());
                mCountersBadgesMap.remove(item.getMenuId());
                break;
            case TYPE_MENU_BUTTON_WITH_BADGE:
                holder.btnMenu.setText(item.getMenuText());
                holder.icon.setBackgroundResource(item.getMenuIconResId());
                mCountersBadgesMap.put(item.getMenuId(), holder.counterBadge);
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
        // init button state
        if (item.getMenuId() == BaseFragment.FragmentId.BONUS) {
            if (CacheProfile.getOptions().bonus.buttonPicture != null) {
                // set custom button ico from server
                DefaultImageLoader.getInstance(App.getContext()).preloadImage(CacheProfile.getOptions().bonus.buttonPicture, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        holder.icon.setImageDrawable(new BitmapDrawable(App.getContext().getResources(), loadedImage));
                    }
                });
            }
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.valueAt(position).getMenuType();
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    public void updateCountersBadge(CountersData countersData) {
        for (BaseFragment.FragmentId id : mCountersBadgesMap.keySet()) {
            int counter = countersData.getCounterByFragmentId(id);
            updateCountersBadge(mCountersBadgesMap.get(id), counter);
        }
    }

    private void updateCountersBadge(TextView view, int value) {
        if (view != null) {
            view.setVisibility(value > 0 ? View.VISIBLE : View.INVISIBLE);
            view.setText(String.valueOf(value));
        }
    }

    public class ViewHolder {
        TextView btnMenu;
        TextView counterBadge;
        ILeftMenuItem item;
        ImageView icon;
        ImageView extraIcon;
    }

    public interface ILeftMenuItem {
        BaseFragment.FragmentId getMenuId();

        int getMenuType();

        String getMenuText();

        int getMenuIconResId();

        Photo getMenuIconPhoto();

        void setMenuIconPhoto(Photo photo);

        int getExtraIconDrawable();

        void setExtraIconDrawable(int resId);
    }
}
