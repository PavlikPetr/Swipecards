package com.topface.topface.ui.profile;

import java.util.LinkedList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.User;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.utils.FormItem;
import com.topface.topface.utils.Utils;

public class UserFormFragment extends Fragment implements OnClickListener{
    private User mUser;
    private UserFormListAdapter mUserFormListAdapter;
    private View mTitleLayout;
    private TextView mTitle;
    private ImageView mState;
    private Button mAskToFillForm;
    private ViewGroup mEmptyFormLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserFormListAdapter = new UserFormListAdapter(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_form, container, false);
        ListView listQuestionnaire = (ListView) root.findViewById(R.id.fragmentFormList);
        listQuestionnaire.setAdapter(mUserFormListAdapter);

        mEmptyFormLayout = (ViewGroup) root.findViewById(R.id.loEmptyForm);
            
        mAskToFillForm =(Button) mEmptyFormLayout.findViewById(R.id.btnEmptyForm);
        mAskToFillForm.setOnClickListener(this);
        
        mTitleLayout = root.findViewById(R.id.fragmentTitle);
        mTitle = (TextView) root.findViewById(R.id.tvTitle);
        mState = (ImageView) root.findViewById(R.id.ivState);
        if (mUser != null) {
        	initFormHeader();
        } else {
            mTitle.setText(Utils.formatFormMatchesQuantity(0));
            mState.setImageResource(R.drawable.user_cell_center);
        }
        mTitleLayout.setVisibility(View.VISIBLE);        

        return root;
    }

    public void setUserData(User user) {
        mUser = user;
        mUserFormListAdapter.setUserData(mUser);
        mUserFormListAdapter.notifyDataSetChanged();
        
        initFormHeader();
    }
    
    private void initFormHeader() {
    	mTitle.setText(Utils.formatFormMatchesQuantity(mUser.formMatches));    	
    	
    	if (formIsEmpty(mUser.forms)) {
    		mEmptyFormLayout.setVisibility(View.VISIBLE);
    	} else {    		
	        if(mUser.formMatches > 0) {
	        	mState.setImageResource(R.drawable.user_cell_center_on);
	        	mTitleLayout.setOnClickListener(this);        	
	        } else {
	            mState.setImageResource(R.drawable.user_cell_center);
	        }
    	}
    }

    private boolean formIsEmpty(LinkedList<FormItem> forms) {
    	for (FormItem formItem : forms) {
			if(formItem.type == FormItem.DATA) {
				if(formItem.dataId != FormItem.NO_RESOURCE_ID || 
						formItem.value != null)
					return false;
			}
		}
    	return true;
    }
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fragmentTitle:
			if (mUserFormListAdapter.isMatchedDataOnly()) mUserFormListAdapter.setAllData();
			else mUserFormListAdapter.setMatchedDataOnly();
			mUserFormListAdapter.notifyDataSetChanged();
			break;
		case R.id.btnEmptyForm:
			Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra(ChatActivity.INTENT_USER_ID, mUser.uid);
            intent.putExtra(ChatActivity.INTENT_USER_NAME, mUser.first_name);
            intent.putExtra(ChatActivity.INTENT_USER_SEX, mUser.sex);
            intent.putExtra(ChatActivity.INTENT_USER_AGE, mUser.age);
            intent.putExtra(ChatActivity.INTENT_USER_CITY, mUser.city_name);
            intent.putExtra(ChatActivity.INTENT_PROFILE_INVOKE, true);
            intent.putExtra(ChatActivity.INTENT_PREV_ENTITY, UserProfileActivity.class.getSimpleName());
            startActivity(intent);
			break;
		}		
	}

}
