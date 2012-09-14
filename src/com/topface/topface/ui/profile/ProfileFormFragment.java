package com.topface.topface.ui.profile;

import com.topface.topface.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    
    View.OnClickListener mOnFillClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(getActivity(), "fill me", Toast.LENGTH_SHORT).show();
        }
    };
}
