package com.topface.topface.ui.profile;

import com.topface.topface.ui.profile.edit.EditContainerActivity;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.topface.R;

public class ProfileFormFragment extends Fragment {
    private ProfileFormListAdapter mProfilePhotoListAdapter;
    private TextView mTitle;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProfilePhotoListAdapter = new ProfileFormListAdapter(getActivity().getApplicationContext());
        mProfilePhotoListAdapter.setOnFillListener(mOnFillClickListener);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_form, container, false);
        ListView formListView = (ListView)root.findViewById(R.id.fragmentFormList);
        formListView.setAdapter(mProfilePhotoListAdapter);
        
        mTitle = (TextView)root.findViewById(R.id.fragmentTitle);
        mTitle.setText("You have 15 something");
        
        return root;
    }
    
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.putExtra(EditContainerActivity.INTENT_REQUEST_KEY, requestCode);
        super.startActivityForResult(intent, requestCode);
    }
    
    View.OnClickListener mOnFillClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Object formItem = view.getTag();
            if(formItem instanceof FormItem) {
                FormItem item = (FormItem)formItem;
                Intent intent = new Intent(getActivity().getApplicationContext(), EditContainerActivity.class);
                intent.putExtra(EditContainerActivity.INTENT_FORM_TITLE_ID, item.titleId);
                intent.putExtra(EditContainerActivity.INTENT_FORM_DATA_ID, item.dataId);
                intent.putExtra(EditContainerActivity.INTENT_FORM_DATA, item.value);
                startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_FORM_ITEM);
            }
        }
    };
}
