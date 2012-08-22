package com.topface.topface.ui.profile;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.User;
import com.topface.topface.utils.FormInfo;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private LinkedList<Pair<String, String>> mUserQuestionnaire;
    private LinkedList<Integer> mItemLayoutList;
    
    private static final int T_TITLE = 0;
    private static final int T_DIVIDER = 1;
    private static final int T_DATA   = 2;
    private static final int T_COUNT  = 3;
    
    static class ViewHolder {
        public ImageView mState;
        public TextView mTitle;
        public TextView mHeader;
        public TextView mValue;
    }

    public UserListAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mUserQuestionnaire = new LinkedList<Pair<String, String>>();
        mItemLayoutList = new LinkedList<Integer>();
    }

    @Override
    public int getCount() {
        return mUserQuestionnaire.size();
    }

    @Override
    public Pair<String, String> getItem(int position) {
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
            holder = new ViewHolder();

            switch (type) {
                case T_DIVIDER:
                    convertView = mInflater.inflate(R.layout.item_divider, null, false);
                    break;
                case T_TITLE:
                case T_DATA:
                    convertView = mInflater.inflate(R.layout.item_user_list, null, false);
                    holder.mState  = (ImageView)convertView.findViewById(R.id.ivState);
                    holder.mTitle  = (TextView)convertView.findViewById(R.id.tvTitle);
                    holder.mHeader = (TextView)convertView.findViewById(R.id.tvHeader);
                    holder.mValue   = (TextView)convertView.findViewById(R.id.tvData);
                    break;
            }
            
            switch (type) {
                case T_TITLE:
                    convertView.setBackgroundResource(R.drawable.user_list_title_bg);
                    break;
                case T_DATA:
                    convertView.setBackgroundResource(R.drawable.user_list_cell_bg);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

    	Pair<String, String> item = getItem(position);
    	
    	switch (type) {
            case T_TITLE:
                holder.mTitle.setText(item.first);
                holder.mState.setImageResource(R.drawable.user_title);
                break;
            case T_DATA:
                String d = item.second;
                holder.mHeader.setText(item.first);
                holder.mValue.setText(item.second);
                holder.mState.setImageResource(R.drawable.user_cell);
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
        mUserQuestionnaire.add(new Pair<String, String>("Интеллектуально-личностные", null));
        mItemLayoutList.add(T_TITLE);
        
        // education
        mUserQuestionnaire.add(new Pair<String, String>(mContext.getString(R.string.profile_education),formInfo.getEducation(user.questionary_education_id)));        
        mItemLayoutList.add(T_DATA);
        // communication
        mUserQuestionnaire.add(new Pair<String, String>(mContext.getString(R.string.profile_commutability),formInfo.getCommunication(user.questionary_communication_id)));       
        mItemLayoutList.add(T_DATA);
        // character
        mUserQuestionnaire.add(new Pair<String, String>(mContext.getString(R.string.profile_character),formInfo.getCharacter(user.questionary_character_id)));       
        mItemLayoutList.add(T_DATA);
        
        mUserQuestionnaire.add(null);
        mItemLayoutList.add(T_DIVIDER);
        
        // header
        mUserQuestionnaire.add(new Pair<String, String>("Физические", null));
        mItemLayoutList.add(T_TITLE);
        
        // height
        mUserQuestionnaire.add(new Pair<String, String>(mContext.getString(R.string.profile_height),""+user.questionary_height));       
        mItemLayoutList.add(T_DATA);
        // weight
        mUserQuestionnaire.add(new Pair<String, String>(mContext.getString(R.string.profile_weight),""+user.questionary_weight));
        mItemLayoutList.add(T_DATA);
        // fitness
        mUserQuestionnaire.add(new Pair<String, String>(mContext.getString(R.string.profile_fitness),formInfo.getFitness(user.questionary_fitness_id)));       
        mItemLayoutList.add(T_DATA);
        // marriage
        int marriage = user.sex==Static.GIRL ? R.string.profile_marriage_female : R.string.profile_marriage_male ;
        mUserQuestionnaire.add(new Pair<String, String>(mContext.getString(marriage),formInfo.getMarriage(user.questionary_marriage_id)));       
        mItemLayoutList.add(T_DATA);
        // finances
        mUserQuestionnaire.add(new Pair<String, String>(mContext.getString(R.string.profile_finances),formInfo.getFinances(user.questionary_finances_id)));       
        mItemLayoutList.add(T_DATA);
        // smoking
        mUserQuestionnaire.add(new Pair<String, String>(mContext.getString(R.string.profile_smoking),formInfo.getSmoking(user.questionary_smoking_id)));       
        mItemLayoutList.add(T_DATA);
        // alcohol
        mUserQuestionnaire.add(new Pair<String, String>(mContext.getString(R.string.profile_alcohol),formInfo.getAlcohol(user.questionary_alcohol_id)));       
        mItemLayoutList.add(T_DATA);
        // smoking
        mUserQuestionnaire.add(new Pair<String, String>(mContext.getString(R.string.profile_smoking),formInfo.getSmoking(user.questionary_smoking_id)));       
        mItemLayoutList.add(T_DATA);
    }
}
