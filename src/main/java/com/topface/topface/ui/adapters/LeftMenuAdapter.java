package com.topface.topface.ui.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
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

public class LeftMenuAdapter extends BaseAdapter {
    public static final int TYPE_MENU_BUTTON = 0;
    public static final int TYPE_MENU_BUTTON_WITH_BADGE = 1;
    private static final int TYPE_COUNT = 2;
    private MenuFragment mMenuFragment;
    private final SparseArray<ILeftMenuItem> mItems;

    private HashMap<BaseFragment.FragmentId, TextView> mCountersBadgesMap = new HashMap<BaseFragment.FragmentId, TextView>();
    private boolean mIsEnabled = true;
    private View.OnClickListener mDisabledItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMenuFragment != null) {
                BaseFragment.FragmentId id = (BaseFragment.FragmentId) v.getTag();
                mMenuFragment.showClosingsDialog(id);
            }
        }
    };

    public LeftMenuAdapter(MenuFragment menuFragment, SparseArray<ILeftMenuItem> items) {
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
        return mItems.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return mIsEnabled;
    }

    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
    }

    public void hideItem(BaseFragment.FragmentId id) {
        setItemHidden(id, true);
    }

    public void showItem(BaseFragment.FragmentId id) {
        setItemHidden(id, false);
    }

    public void showAllItems() {
        setAllItemsHidden(false);
    }

    private void setItemHidden(BaseFragment.FragmentId id, boolean hidden) {
        mItems.get(id.getId()).setHidden(hidden);
    }

    private void setAllItemsHidden(boolean hidden) {
        int key;
        for (int i = 0; i < mItems.size(); i++) {
            key = mItems.keyAt(i);
            mItems.get(key).setHidden(hidden);
        }
    }

    public void addItem(ILeftMenuItem item) {
        mItems.put(item.getMenuId().getId(), item);
    }

    public boolean hasFragment(BaseFragment.FragmentId id) {
        return mItems.valueAt(id.getId()) != null;
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
            holder.btnMenu = (Button) convertView.findViewById(R.id.btnMenu);
            holder.counterBadge = (TextView) convertView.findViewById(R.id.tvCounterBadge);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            // unregister previous non-visible item from updates
            unregisterCounterBadge(holder.item);
            holder.item = item;
        }
        // initiate views' state in holder
        switch (item.getMenuType()) {
            case TYPE_MENU_BUTTON:
                holder.btnMenu.setText(item.getMenuText());
                holder.counterBadge.setVisibility(View.GONE);
                unregisterCounterBadge(item);
                break;
            case TYPE_MENU_BUTTON_WITH_BADGE:
                holder.btnMenu.setText(item.getMenuText());
                if (item.isHidden()) {
                    unregisterCounterBadge(item);
                    holder.counterBadge.setVisibility(View.GONE);
                } else {
                    registerCounterBadge(item, holder.counterBadge);
                }
                break;
            default:
                break;
        }
        holder.item = item;
        // init button state
        holder.btnMenu.setCompoundDrawablesWithIntrinsicBounds(item.getMenuIconResId(), 0, 0, 0);
        holder.btnMenu.setTag(item.getMenuId());
        if (enabled || item.isHidden()) {
            holder.btnMenu.setOnClickListener(mMenuFragment);
            setAlphaToTextAndDrawable(holder.btnMenu, 255);
            holder.btnMenu.setSelected(mMenuFragment.getCurrentFragmentId() == item.getMenuId());
        } else {
            holder.btnMenu.setOnClickListener(mDisabledItemClickListener);
            setAlphaToTextAndDrawable(holder.btnMenu, 102);
            holder.btnMenu.setSelected(mMenuFragment.getCurrentFragmentId() == item.getMenuId());
        }
        if (item.isHidden()) {
            holder.btnMenu.setVisibility(View.GONE);
        } else {
            holder.btnMenu.setVisibility(View.VISIBLE);
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
        return mItems.valueAt(position).getMenuType();
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
        for (BaseFragment.FragmentId id : mCountersBadgesMap.keySet()) {
            updateCounterBadge(id, mCountersBadgesMap.get(id));
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
        Button btnMenu;
        TextView counterBadge;
        ILeftMenuItem item;
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
