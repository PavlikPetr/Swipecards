package com.topface.topface.ui.profile;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.data.User;
import com.topface.topface.utils.FormInfo;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private LinkedList<String> mUserQuestionnaire;
    private LinkedList<Integer> mItemLayoutList;
    
    private static final int T_HEADER = 0;
    private static final int T_DATA   = 1;
    private static final int T_COUNT  = 2;
    
    static class ViewHolder {
        public ImageView mState;
        public TextView mHeader;
        public TextView mTitle;
        public TextView mData;
    }

    public UserListAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mUserQuestionnaire = new LinkedList<String>();
        mItemLayoutList = new LinkedList<Integer>();
    }

    @Override
    public int getCount() {
        return mUserQuestionnaire.size();
    }

    @Override
    public String getItem(int position) {
        return mUserQuestionnaire.get(position);
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
    	return mItemLayoutList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);        
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

        String str = getItem(position);
    	switch (type) {
            case T_HEADER:
                holder.mHeader.setText("Header:" + str);
                break;
            case T_DATA:
                holder.mTitle.setText("Header:" + str);
                holder.mData.setText("Data:" + str);
                break;
        }

//        if (likes.online)
//            holder.mOnline.setVisibility(View.VISIBLE);
//        else
//            holder.mOnline.setVisibility(View.INVISIBLE);
        
        return convertView;
    }
    
    public void setUserData(User user) {
        prepare(user);
    }
    
    private void prepare(User user) {
        FormInfo formInfo = new FormInfo(mContext, user.sex);
//        for (int i=0;i<10;++i)
//            mUserQuestionnaire.add(i);
        
        // header
        mUserQuestionnaire.add("one");
        mItemLayoutList.add(T_HEADER);
        // education
        mUserQuestionnaire.add(formInfo.getEducation(user.questionary_education_id));        
        mItemLayoutList.add(T_DATA);
        // communication
        mUserQuestionnaire.add(formInfo.getCommunication(user.questionary_communication_id));       
        mItemLayoutList.add(T_DATA);
        // character
        mUserQuestionnaire.add(formInfo.getCharacter(user.questionary_character_id));       
        mItemLayoutList.add(T_DATA);
        
        // header
        mUserQuestionnaire.add("two");
        mItemLayoutList.add(T_HEADER);
        // hight
        mUserQuestionnaire.add(" " + user.questionary_height);       
        mItemLayoutList.add(T_DATA);
        // weight
        mUserQuestionnaire.add("" + user.questionary_weight);
        mItemLayoutList.add(T_DATA);
        // fitness
        mUserQuestionnaire.add(formInfo.getFitness(user.questionary_fitness_id));       
        mItemLayoutList.add(T_DATA);
        // marriage
        mUserQuestionnaire.add(formInfo.getMarriage(user.questionary_marriage_id));       
        mItemLayoutList.add(T_DATA);
        // finances
        mUserQuestionnaire.add(formInfo.getFinances(user.questionary_finances_id));       
        mItemLayoutList.add(T_DATA);
        // smoking
        mUserQuestionnaire.add(formInfo.getSmoking(user.questionary_smoking_id));       
        mItemLayoutList.add(T_DATA);
        // alcohol
        mUserQuestionnaire.add(formInfo.getAlcohol(user.questionary_alcohol_id));       
        mItemLayoutList.add(T_DATA);
        // smoking
        mUserQuestionnaire.add(formInfo.getSmoking(user.questionary_smoking_id));       
        mItemLayoutList.add(T_DATA);
    }
}
