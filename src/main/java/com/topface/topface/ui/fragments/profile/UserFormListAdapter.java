package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.User;
import com.topface.topface.utils.FormItem;

import java.util.LinkedList;

public class UserFormListAdapter extends BaseAdapter {

    private static final int T_HEADER = 0;
    private static final int T_DIVIDER = 1;
    private static final int T_DATA = 2;
    private static final int T_COUNT = T_DATA + 1;
    private LayoutInflater mInflater;
    private LinkedList<FormItem> mUserForms;
    private LinkedList<FormItem> mInitialUserForms;
    private LinkedList<FormItem> mMatchedUserForms;
    private boolean isMatchedDataOnly = false;

    public UserFormListAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mUserForms = new LinkedList<>();
    }

    @Override
    public int getCount() {
        return mUserForms != null ? mUserForms.size() : 0;
    }

    @Override
    public FormItem getItem(int position) {
        return mUserForms != null ? mUserForms.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return T_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        switch (getItem(position).type) {
            case FormItem.HEADER:
                return T_HEADER;
            case FormItem.DATA:
                return T_DATA;
            case FormItem.DIVIDER:
                return T_DIVIDER;
            default:
                return T_HEADER;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);
        if (convertView == null) {
            holder = new ViewHolder();

            switch (type) {
                case T_DIVIDER:
                    convertView = mInflater.inflate(R.layout.item_divider, null, false);
                    break;
                case T_HEADER:
                case T_DATA:
                    convertView = mInflater.inflate(R.layout.item_user_list, null, false);
                    holder.mState = (ImageView) convertView.findViewById(R.id.ivState);
                    holder.mTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                    holder.mHeader = (TextView) convertView.findViewById(R.id.tvHeader);
                    holder.mValue = (TextView) convertView.findViewById(R.id.tvValue);
                    break;
            }

            if (convertView != null) {
                convertView.setTag(holder);
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FormItem item = getItem(position);

        switch (type) {
            case T_HEADER:
                holder.mHeader.setText(item.title);
                holder.mState.setImageResource(getHeaderPicture(item));
                holder.mTitle.setVisibility(View.GONE);
                holder.mValue.setVisibility(View.GONE);
                break;
            case T_DATA:
                holder.mTitle.setText(item.title.toUpperCase());
                holder.mValue.setText(item.value.toLowerCase());
                if (item.equal)
                    holder.mState.setImageResource(R.drawable.user_cell_on);  // GREEN POINT
                else
                    holder.mState.setImageResource(R.drawable.user_cell);
                holder.mHeader.setVisibility(View.GONE);
                break;
        }

        return convertView;
    }

    @SuppressWarnings("unchecked")
    public void setUserData(User user) {
        mInitialUserForms = removeEmptyHeaders((LinkedList<FormItem>) user.forms.clone());
        mMatchedUserForms = removeEmptyHeaders(removeNotMatchedItems((LinkedList<FormItem>) mInitialUserForms.clone()));
        setAllData();
    }

    public void setMatchedDataOnly() {
        mUserForms = mMatchedUserForms;
        isMatchedDataOnly = true;
    }

    public void setAllData() {
        mUserForms = mInitialUserForms;
        isMatchedDataOnly = false;
    }

    public boolean isMatchedDataOnly() {
        return isMatchedDataOnly;
    }

    private LinkedList<FormItem> removeNotMatchedItems(LinkedList<FormItem> userForms) {
        int i = 0;
        while (i < userForms.size()) {
            if (userForms.get(i).type == FormItem.DATA) {
                if (!userForms.get(i).equal) {
                    userForms.remove(i);
                } else {
                    i++;
                }
            } else {
                i++;
            }
        }

        return userForms;
    }

    private LinkedList<FormItem> removeEmptyHeaders(LinkedList<FormItem> userForms) {
        int i = 0;
        while (i < userForms.size()) {
            if (userForms.get(i).type == FormItem.HEADER) {
                FormItem headerItem = userForms.get(i);
                if (!hasRelatedFormItem(headerItem, userForms, i)) {
                    userForms.remove(i);
                    if (i - 1 >= 0) {
                        if (userForms.get(i - 1).type == FormItem.DIVIDER) {
                            userForms.remove(i - 1);
                        }
                        i--;
                    }
                } else {
                    i++;
                }

            } else {
                i++;
            }
        }

        if (!userForms.isEmpty() && (userForms.get(0).type == FormItem.DIVIDER)) {
            userForms.remove(0);
        }

        return userForms;
    }

    private boolean hasRelatedFormItem(FormItem header, LinkedList<FormItem> userForms, int startPos) {
        for (int i = startPos; i < userForms.size(); i++) {
            if (userForms.get(i).header == header) return true;
        }
        return false;
    }

    private int getHeaderPicture(FormItem item) {
        switch (item.titleId) {
            case R.string.form_main:
                return R.drawable.user_main;
            case R.string.form_habits:
                return R.drawable.user_habits;
            case R.string.form_physique:
                return R.drawable.user_physical;
            case R.string.form_social:
                return R.drawable.user_social;
            case R.string.form_detail:
                return R.drawable.user_details;
        }
        return 0;
    }

    // class ViewHolder
    private static class ViewHolder {
        public ImageView mState;
        public TextView mTitle;
        public TextView mHeader;
        public TextView mValue;
    }

}
