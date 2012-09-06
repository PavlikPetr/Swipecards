package com.topface.topface.ui.profile;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
        public Button   mFill;
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
                    holder.mFill   = (Button)convertView.findViewById(R.id.btnFill);
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
                holder.mState.setImageResource(R.drawable.user_title);
                holder.mTitle.setText(item.first);
                break;
            case T_DATA:
                holder.mState.setImageResource(R.drawable.user_cell);
                holder.mHeader.setText(item.first);
                if(item.second != null) {
                    holder.mValue.setText(item.second);
                    holder.mValue.setVisibility(View.VISIBLE);
                    holder.mFill.setVisibility(View.INVISIBLE);
                } else {
                    holder.mValue.setVisibility(View.INVISIBLE);
                    holder.mFill.setVisibility(View.VISIBLE);
                }
                break;
        }
        
        return convertView;
    }
    
    private void prepare() {

        // header
        mProfileForm.add(new Pair<String, String>("Интеллектуально-личностные", null));
        mItemLayoutList.add(T_TITLE);

//        // education
//        mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_education), formInfo.getEducation(CacheProfile.form_education_id)));        
//        mItemLayoutList.add(T_DATA);
//
//        // communication
//        mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_commutability), formInfo.getCommunication(CacheProfile.form_communication_id)));       
//        mItemLayoutList.add(T_DATA);
//
//        // character
//        mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_character), formInfo.getCharacter(CacheProfile.form_character_id)));       
//        mItemLayoutList.add(T_DATA);
//        
        mProfileForm.add(null);
        mItemLayoutList.add(T_DIVIDER);
        
        // header
        mProfileForm.add(new Pair<String, String>("Физические", null));
        mItemLayoutList.add(T_TITLE);

//        // height
//        mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_height), CacheProfile.form_height>0 ? ""+CacheProfile.form_height : null));       
//        mItemLayoutList.add(T_DATA);
//
//        // weight
//        mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_weight), CacheProfile.form_weight>0 ? ""+CacheProfile.form_weight : null));
//        mItemLayoutList.add(T_DATA);
//
//        // fitness
//        mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_fitness), formInfo.getFitness(CacheProfile.form_fitness_id)));       
//        mItemLayoutList.add(T_DATA);
//
//        // marriage
//        int marriage = CacheProfile.sex==Static.GIRL ? R.string.profile_marriage_female : R.string.profile_marriage_male ;
//        mProfileForm.add(new Pair<String, String>(mContext.getString(marriage), formInfo.getMarriage(CacheProfile.form_marriage_id)));       
//        mItemLayoutList.add(T_DATA);
//
//        // finances
//        mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_finances), formInfo.getFinances(CacheProfile.form_finances_id)));       
//        mItemLayoutList.add(T_DATA);
//
//        // smoking
//        mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_smoking), formInfo.getSmoking(CacheProfile.form_smoking_id)));       
//        mItemLayoutList.add(T_DATA);
//
//        // alcohol
//        mProfileForm.add(new Pair<String, String>(mContext.getString(R.string.profile_alcohol), formInfo.getAlcohol(CacheProfile.form_alcohol_id)));       
//        mItemLayoutList.add(T_DATA);
    }
}
