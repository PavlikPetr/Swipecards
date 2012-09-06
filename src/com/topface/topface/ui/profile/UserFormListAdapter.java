package com.topface.topface.ui.profile;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.User;
import com.topface.topface.ui.views.TripleButton;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.Triple;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserFormListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private LinkedList<Triple<String, String, Boolean>> mUserQuestionnaire;
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

    public UserFormListAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mUserQuestionnaire = new LinkedList<Triple<String, String, Boolean>>();
        mItemLayoutList = new LinkedList<Integer>();
    }

    @Override
    public int getCount() {
        return mUserQuestionnaire.size();
    }

    @Override
    public Triple<String, String, Boolean> getItem(int position) {
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
                    holder.mValue  = (TextView)convertView.findViewById(R.id.tvData);
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

    	Triple<String, String, Boolean> item = getItem(position);
    	
    	switch (type) {
            case T_TITLE:
                holder.mTitle.setText(item.first);
                holder.mState.setImageResource(R.drawable.user_title);
                break;
            case T_DATA:
                holder.mHeader.setText(item.first);
                holder.mValue.setText(item.second);
                if(item.third)
                    holder.mState.setImageResource(R.drawable.user_cell);  // GREEN POINT
                else
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
        mUserQuestionnaire.add(new Triple<String, String, Boolean>("Интеллектуально-личностные", null ,null));
        mItemLayoutList.add(T_TITLE);

        // education
        if(user.form_education_id > 0) {
            boolean b = CacheProfile.form_education_id == user.form_education_id; 
            mUserQuestionnaire.add(new Triple<String, String, Boolean>(mContext.getString(R.string.profile_education),formInfo.getEducation(user.form_education_id),b));        
            mItemLayoutList.add(T_DATA);
        }

        // communication
        if(user.form_communication_id > 0) {
            boolean b = CacheProfile.form_communication_id == user.form_communication_id;
            mUserQuestionnaire.add(new Triple<String, String, Boolean>(mContext.getString(R.string.profile_commutability),formInfo.getCommunication(user.form_communication_id),b));       
            mItemLayoutList.add(T_DATA);
        }

        // character
        if(user.form_character_id > 0) {
            boolean b = CacheProfile.form_character_id == user.form_character_id;
            mUserQuestionnaire.add(new Triple<String, String, Boolean>(mContext.getString(R.string.profile_character),formInfo.getCharacter(user.form_character_id),b));       
            mItemLayoutList.add(T_DATA);
        }
        
        mUserQuestionnaire.add(null);
        mItemLayoutList.add(T_DIVIDER);
        
        // header
        mUserQuestionnaire.add(new Triple<String, String, Boolean>("Физические", null, null));
        mItemLayoutList.add(T_TITLE);

        // height
        if(user.form_height > 0) {
            boolean b = CacheProfile.form_height == user.form_height;
            mUserQuestionnaire.add(new Triple<String, String, Boolean>(mContext.getString(R.string.profile_height),""+user.form_height,b));       
            mItemLayoutList.add(T_DATA);
        }

        // weight
        if(user.form_weight > 0) {
            boolean b = CacheProfile.form_weight == user.form_weight;
            mUserQuestionnaire.add(new Triple<String, String, Boolean>(mContext.getString(R.string.profile_weight),""+user.form_weight,b));
            mItemLayoutList.add(T_DATA);
        }

        // fitness
        if(user.form_fitness_id > 0) {
            boolean b = CacheProfile.form_fitness_id == user.form_fitness_id;
            mUserQuestionnaire.add(new Triple<String, String, Boolean>(mContext.getString(R.string.profile_fitness),formInfo.getFitness(user.form_fitness_id),b));       
            mItemLayoutList.add(T_DATA);
        }

        // marriage
        if(user.form_marriage_id > 0) {
            boolean b = CacheProfile.form_marriage_id == user.form_marriage_id;
            int marriage = user.sex==Static.GIRL ? R.string.profile_marriage_female : R.string.profile_marriage_male ;
            mUserQuestionnaire.add(new Triple<String, String, Boolean>(mContext.getString(marriage),formInfo.getMarriage(user.form_marriage_id),b));       
            mItemLayoutList.add(T_DATA);
        }

        // finances
        if(user.form_finances_id > 0) {
            boolean b = CacheProfile.form_finances_id == user.form_finances_id;
            mUserQuestionnaire.add(new Triple<String, String, Boolean>(mContext.getString(R.string.profile_finances),formInfo.getFinances(user.form_finances_id),b));       
            mItemLayoutList.add(T_DATA);
        }

        // smoking
        if(user.form_smoking_id > 0) {
            boolean b = CacheProfile.form_smoking_id == user.form_smoking_id;
            mUserQuestionnaire.add(new Triple<String, String, Boolean>(mContext.getString(R.string.profile_smoking),formInfo.getSmoking(user.form_smoking_id),b));       
            mItemLayoutList.add(T_DATA);
        }

        // alcohol
        if(user.form_alcohol_id > 0) {
            boolean b = CacheProfile.form_alcohol_id == user.form_alcohol_id;
            mUserQuestionnaire.add(new Triple<String, String, Boolean>(mContext.getString(R.string.profile_alcohol),formInfo.getAlcohol(user.form_alcohol_id),b));       
            mItemLayoutList.add(T_DATA);
        }
    }
}
