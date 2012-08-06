package com.topface.topface.ui.profile;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.data.User;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private LinkedList<Integer> mUserQuestionnaire;
    
    static class ViewHolder {
        public ImageView mState;
        public TextView mHeader;
        public TextView mTitle;
        public TextView mData;
    }

    public UserListAdapter(Context context) {
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mUserQuestionnaire = new LinkedList<Integer>();
    }

    @Override
    public int getCount() {
        return mUserQuestionnaire.size();
    }

    @Override
    public Integer getItem(int position) {
        return mUserQuestionnaire.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
    	return super.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
    	return 0;
    }

    @Override
    public View getView(int position,View convertView,ViewGroup parent) {
        ViewHolder holder;
        //int type = getItemViewType(position);        
    	if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_user_list, null, false);
            holder = new ViewHolder();
            holder.mState  = (ImageView)convertView.findViewById(R.id.ivState);
            holder.mHeader = (TextView)convertView.findViewById(R.id.tvHeader);
            holder.mTitle  = (TextView)convertView.findViewById(R.id.tvTitle);
            holder.mData   = (TextView)convertView.findViewById(R.id.tvData);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }


//        if (likes.online)
//            holder.mOnline.setVisibility(View.VISIBLE);
//        else
//            holder.mOnline.setVisibility(View.INVISIBLE);
        
        return convertView;
    }
    
    public void setUserData(User user) {
        for (int i=0;i<10;++i)
            mUserQuestionnaire.add(i);
    }
}
