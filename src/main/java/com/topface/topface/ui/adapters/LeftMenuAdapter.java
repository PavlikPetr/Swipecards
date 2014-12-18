package com.topface.topface.ui.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ResourcesUtils;

import java.util.HashMap;

public class LeftMenuAdapter extends BaseAdapter {
    public static final int TYPE_MENU_BUTTON = 0;
    public static final int TYPE_MENU_BUTTON_WITH_BADGE = 1;
    private static final int TYPE_COUNT = 2;
    private MenuFragment mMenuFragment;
    private final SparseArray<ILeftMenuItem> mItems;
    private SparseArray<ILeftMenuItem> mHiddenItems = new SparseArray<>();

    private HashMap<BaseFragment.FragmentId, TextView> mCountersBadgesMap = new HashMap<>();
    private boolean mIsEnabled = true;
    private View.OnClickListener mDisabledItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMenuFragment != null) {
                BaseFragment.FragmentId id = ((ViewHolder) v.getTag()).getFragmentId();
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

    private void setItemHidden(BaseFragment.FragmentId fragmentId, boolean hidden) {
        int id = fragmentId.getId();
        if (hidden) {
            mHiddenItems.put(id, mItems.get(id));
            mItems.remove(id);
        } else {
            mItems.put(id, mHiddenItems.get(id));
            mHiddenItems.remove(id);
        }
        notifyDataSetChanged();
    }

    private void setAllItemsHidden(boolean hidden) {
        int key;

        SparseArray<ILeftMenuItem> goalStateItems = hidden ? mHiddenItems : mItems;
        SparseArray<ILeftMenuItem> anotherStateItems = hidden ? mItems : mHiddenItems;

        for (int i = 0; i < anotherStateItems.size(); i++) {
            key = anotherStateItems.keyAt(i);
            goalStateItems.put(key, anotherStateItems.get(key));
        }
        anotherStateItems.clear();
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
        final boolean enabled = isEnabled(position);
        //init convertView
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mMenuFragment.getActivity(), R.layout.item_left_menu_button_with_badge, null);
            holder.leftMenuCellLayout = convertView.findViewById(R.id.leftMenuCellLayout);
            holder.btnMenu = (TextView) convertView.findViewById(R.id.btnMenu);
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
                if (enabled) {
                    registerCounterBadge(item, holder.counterBadge);
                } else {
                    unregisterCounterBadge(item);
                    holder.counterBadge.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
        holder.item = item;
        // init button state
        holder.btnMenu.setCompoundDrawablesWithIntrinsicBounds(item.getMenuIconResId(), 0, 0, 0);

        if (enabled) {
            holder.leftMenuCellLayout.setOnClickListener(mMenuFragment);
            setAlphaToTextAndDrawable(holder.btnMenu, 255);
            setCheckedBackgroundState(mMenuFragment.getCurrentFragmentId() == item.getMenuId(), holder.leftMenuCellLayout);
        } else {
            holder.leftMenuCellLayout.setOnClickListener(mDisabledItemClickListener);
            setAlphaToTextAndDrawable(holder.btnMenu, 102);
            setCheckedBackgroundState(mMenuFragment.getCurrentFragmentId() == item.getMenuId(), holder.leftMenuCellLayout);
        }

        return convertView;
    }

    private void setCheckedBackgroundState(boolean isChecked, View layout) {
        if (isChecked) {
            layout.setBackgroundResource(R.color.bg_left_menu);
        } else {
            layout.setBackgroundResource(R.drawable.bg_left_menu_item_selector);
        }
    }


    private void setAlphaToTextAndDrawable(TextView btn, int alpha) {
        btn.setTextColor(Color.argb(alpha, 255, 255, 255));
        Drawable[] compoundDrawables = btn.getCompoundDrawables();
        if (compoundDrawables[0] != null) {
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

    public class ViewHolder {
        TextView btnMenu;
        View leftMenuCellLayout;
        TextView counterBadge;
        ILeftMenuItem item;

        public BaseFragment.FragmentId getFragmentId() {
            return item.getMenuId();
        }
    }

    public interface ILeftMenuItem {
        BaseFragment.FragmentId getMenuId();

        int getMenuType();

        String getMenuText();

        int getMenuIconResId();
    }
}
