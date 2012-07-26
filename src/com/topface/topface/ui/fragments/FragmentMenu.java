package com.topface.topface.ui.fragments;

import com.topface.topface.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentMenu extends Fragment {
    View.OnClickListener mOnClickListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.fragment_menu, null);
        
        ((Button)v.findViewById(R.id.btnFragmentProfile)).setOnClickListener(mOnClickListener);
        ((Button)v.findViewById(R.id.btnFragmentDating)).setOnClickListener(mOnClickListener);
        ((Button)v.findViewById(R.id.btnFragmentLikes)).setOnClickListener(mOnClickListener);
        ((Button)v.findViewById(R.id.btnFragmentMutual)).setOnClickListener(mOnClickListener);
        ((Button)v.findViewById(R.id.btnFragmentDialogs)).setOnClickListener(mOnClickListener);
        ((Button)v.findViewById(R.id.btnFragmentTops)).setOnClickListener(mOnClickListener);
        ((Button)v.findViewById(R.id.btnFragmentSettings)).setOnClickListener(mOnClickListener);
        
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }
    
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
         super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {

    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }
}
