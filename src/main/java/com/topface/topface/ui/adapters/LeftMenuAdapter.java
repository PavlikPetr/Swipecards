package com.topface.topface.ui.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ResourcesUtils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

/**
* Created by kirussell on 13.11.13.
*/
public class LeftMenuAdapter extends BaseAdapter {
    public static final int TYPE_MENU_BUTTON = 0;
    public static final int TYPE_MENU_BUTTON_WITH_BADGE = 1;
    private static final int TYPE_COUNT = 2;
    private MenuFragment mMenuFragment;
    private final List<ILeftMenuItem> mItems;

    private HashMap<BaseFragment.FragmentId, TextView> mCountersBadgesMap = new HashMap<BaseFragment.FragmentId, TextView>();
    private boolean mIsEnabled = true;
    private View.OnClickListener mDisabledItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMenuFragment != null) {
                mMenuFragment.showClosingsDialog();
            }
        }
    };

    public LeftMenuAdapter(MenuFragment menuFragment, List<ILeftMenuItem> items) {
        this.mMenuFragment = menuFragment;
        mItems = items;
    }

    public static ILeftMenuItem newLeftMenuItem(final BaseFragment.FragmentId menuId, final int menuType,
                                          final int menuIconResId) {
        return new ILeftMenuItem() {
            boolean isHidden = false;

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
                return App.getContext().getString(ResourcesUtils.getFragmentNameResId(menuId));
            }

            @Override
            public int getMenuIconResId() {
                return menuIconResId;
            }

            @Override
            public boolean isHidden() {
                return isHidden;
            }

            @Override
            public void setHidden(boolean hidden) {
                isHidden = hidden;
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
        return mItems.get(position).getMenuId().ordinal();
    }

    @Override
    public boolean isEnabled(int position) {
        return mIsEnabled;
    }

    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
    }

    public void hideItem(BaseFragment.FragmentId id) {
        setItemHidden(id,true);
    }

    public void showItem(BaseFragment.FragmentId id) {
        setItemHidden(id,false);
    }

    public void showAllItems() {
        setAllItemsHidden(false);
    }

    private void setItemHidden(BaseFragment.FragmentId id, boolean hidden) {
        for (ILeftMenuItem item : mItems) {
            if (item.getMenuId().equals(id)) {
                item.setHidden(hidden);
            }
        }
    }

    private void setAllItemsHidden(boolean hidden) {
        for (ILeftMenuItem item : mItems) {
            item.setHidden(hidden);
        }
    }

    @NotNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // initialize holder and convertView
        final ViewHolder holder;
        // get menu item on current position
        final ILeftMenuItem item = getItem(position);
        final boolean enabled = isEnabled(position);
        //init convertView
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mMenuFragment.getActivity(), R.layout.item_left_menu_button_with_badge, null);
            holder.mBtnMenu = (Button) convertView.findViewById(R.id.btnMenu);
            holder.mCounterBadge = (TextView) convertView.findViewById(R.id.tvCounterBadge);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // initiate views' state in holder
        switch (item.getMenuType()) {
            case TYPE_MENU_BUTTON:
                holder.mBtnMenu.setText(item.getMenuText());
                holder.mCounterBadge.setVisibility(View.GONE);
                unregisterCounterBadge(item);
                break;
            case TYPE_MENU_BUTTON_WITH_BADGE:
                holder.mBtnMenu.setText(item.getMenuText());
                registerCounterBadge(item, holder.mCounterBadge);
                break;
            default:
                break;
        }
        // init button state
        holder.mBtnMenu.setCompoundDrawablesWithIntrinsicBounds(item.getMenuIconResId(), 0, 0, 0);
        holder.mBtnMenu.setTag(item.getMenuId());
        if (enabled || item.isHidden()) {
            holder.mBtnMenu.setOnClickListener(mMenuFragment);
            setAlphaToTextAndDrawable(holder.mBtnMenu, 255);
            holder.mBtnMenu.setSelected(mMenuFragment.getCurrentFragmentId() == item.getMenuId());
        } else {
            holder.mBtnMenu.setOnClickListener(mDisabledItemClickListener);
            setAlphaToTextAndDrawable(holder.mBtnMenu, 102);
            holder.mBtnMenu.setSelected(false);
        }

        if (item.isHidden()) {
            holder.mBtnMenu.setVisibility(View.GONE);
            holder.mCounterBadge.setVisibility(View.GONE);
        } else {
            holder.mBtnMenu.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    private void setAlphaToTextAndDrawable(Button btn, int alpha) {
        btn.setTextColor(Color.argb(alpha, 255, 255, 255));
        Drawable[] compoundDrawables = btn.getCompoundDrawables();
        if (compoundDrawables != null && compoundDrawables[0] != null) {
            compoundDrawables[0].setAlpha(alpha);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getMenuType();
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    private void registerCounterBadge(ILeftMenuItem item, TextView mCounterBadge) {
        BaseFragment.FragmentId id = item.getMenuId();
        mCountersBadgesMap.put(item.getMenuId(), mCounterBadge);
        updateCounterBadge(id, mCounterBadge);
    }

    private void unregisterCounterBadge(ILeftMenuItem item) {
        mCountersBadgesMap.remove(item.getMenuId());
    }

    public void refreshCounterBadges() {
        for (ILeftMenuItem item : mItems) {
            if (item.isHidden()) continue;
            if (item.getMenuType() == TYPE_MENU_BUTTON_WITH_BADGE) {
                BaseFragment.FragmentId menuId = item.getMenuId();
                TextView mCounterBadgeView = mCountersBadgesMap.get(menuId);
                updateCounterBadge(menuId, mCounterBadgeView);
            }
        }
    }

    private void updateCounterBadge(BaseFragment.FragmentId menuId, TextView mCounterBadgeView) {
        if (mCounterBadgeView == null) return;
        int unreadCounter = CacheProfile.getUnreadCounterByFragmentId(menuId);
        if (unreadCounter > 0) {
            mCounterBadgeView.setText(Integer.toString(unreadCounter));
            mCounterBadgeView.setVisibility(View.VISIBLE);
        } else {
            mCounterBadgeView.setVisibility(View.GONE);
        }
    }

    class ViewHolder {
        Button mBtnMenu;
        TextView mCounterBadge;
    }

    public interface ILeftMenuItem {
        BaseFragment.FragmentId getMenuId();
        int getMenuType();
        String getMenuText();
        int getMenuIconResId();
        boolean isHidden();
        void setHidden(boolean hidden);
    }
}
