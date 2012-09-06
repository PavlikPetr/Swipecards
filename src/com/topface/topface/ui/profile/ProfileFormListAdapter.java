package com.topface.topface.ui.profile;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.User;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileFormListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private LinkedList<Pair<String, String>> mProfileForm;
    private LinkedList<Integer> mItemLayoutList;
    
    private static final int T_TITLE   = 0;
    private static final int T_DIVIDER = 1;
    private static final int T_DATA    = 2;
    private static final int T_COUNT   = 3;
    
    static class ViewHolder {
        public ImageView mState;
        public TextView mTitle;
        public TextView mHeader;
        public TextView mValue;
    }

    public ProfileFormListAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mProfileForm = new LinkedList<Pair<String, String>>();
        mItemLayoutList = new LinkedList<Integer>();
        
        prepare();
    }

    @Override
    public int getCount() {
        return mProfileForm.size();
    }

    @Override
    public Pair<String, String> getItem(int position) {
        return mProfileForm.get(position);
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

    	Pair<String, String> item = getItem(position);
    	
    	switch (type) {
            case T_TITLE:
                holder.mTitle.setText(item.first);
                holder.mState.setImageResource(R.drawable.user_title);
                break;
            case T_DATA:
                holder.mHeader.setText(item.first);
                holder.mValue.setText(item.second);
                break;
        }
        
        return convertView;
    }
    
    private void prepare() {
        FormInfo formInfo = new FormInfo(mContext, CacheProfile.sex);

        // header
        mProfileForm.add(new Pair<String, String>("Интеллектуально-личностные", null));
        mItemLayoutList.add(T_TITLE);

        // education
        if(CacheProfile.form_education_id > 0) {
            mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_education),formInfo.getEducation(CacheProfile.form_education_id)));        
            mItemLayoutList.add(T_DATA);
        }

        // communication
        if(CacheProfile.form_communication_id > 0) {
            mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_commutability),formInfo.getCommunication(CacheProfile.form_communication_id)));       
            mItemLayoutList.add(T_DATA);
        }

        // character
        if(CacheProfile.form_character_id > 0) {
            mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_character),formInfo.getCharacter(CacheProfile.form_character_id)));       
            mItemLayoutList.add(T_DATA);
        }
        
        mProfileForm.add(null);
        mItemLayoutList.add(T_DIVIDER);
        
        // header
        mProfileForm.add(new Pair<String, String>("Физические", null));
        mItemLayoutList.add(T_TITLE);

        // height
        if(CacheProfile.form_height > 0) {
            mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_height),""+CacheProfile.form_height));       
            mItemLayoutList.add(T_DATA);
        }

        // weight
        if(CacheProfile.form_weight > 0) {
            mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_weight),""+CacheProfile.form_weight));
            mItemLayoutList.add(T_DATA);
        }

        // fitness
        if(CacheProfile.form_fitness_id > 0) {
            mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_fitness),formInfo.getFitness(CacheProfile.form_fitness_id)));       
            mItemLayoutList.add(T_DATA);
        }

        // marriage
        if(CacheProfile.form_marriage_id > 0) {
            int marriage = CacheProfile.sex==Static.GIRL ? R.string.profile_marriage_female : R.string.profile_marriage_male ;
            mProfileForm.add(new Pair<String, String>(mContext.getString(marriage),formInfo.getMarriage(CacheProfile.form_marriage_id)));       
            mItemLayoutList.add(T_DATA);
        }

        // finances
        if(CacheProfile.form_finances_id > 0) {
            mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_finances),formInfo.getFinances(CacheProfile.form_finances_id)));       
            mItemLayoutList.add(T_DATA);
        }

        // smoking
        if(CacheProfile.form_smoking_id > 0) {
            mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_smoking),formInfo.getSmoking(CacheProfile.form_smoking_id)));       
            mItemLayoutList.add(T_DATA);
        }

        // alcohol
        if(CacheProfile.form_alcohol_id > 0) {
            mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_alcohol),formInfo.getAlcohol(CacheProfile.form_alcohol_id)));       
            mItemLayoutList.add(T_DATA);
        }
    }
}
