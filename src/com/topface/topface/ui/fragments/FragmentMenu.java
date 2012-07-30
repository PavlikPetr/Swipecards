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
    
    View mRootLayout;
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
        if(mRootLayout != null)
            return mRootLayout;
        
        mRootLayout = inflater.inflate(R.layout.fragment_menu, null);
        
        ((Button)mRootLayout.findViewById(R.id.btnFragmentProfile)).setOnClickListener(mOnClickListener);
        ((Button)mRootLayout.findViewById(R.id.btnFragmentDating)).setOnClickListener(mOnClickListener);
        ((Button)mRootLayout.findViewById(R.id.btnFragmentLikes)).setOnClickListener(mOnClickListener);
        ((Button)mRootLayout.findViewById(R.id.btnFragmentMutual)).setOnClickListener(mOnClickListener);
        ((Button)mRootLayout.findViewById(R.id.btnFragmentDialogs)).setOnClickListener(mOnClickListener);
        ((Button)mRootLayout.findViewById(R.id.btnFragmentTops)).setOnClickListener(mOnClickListener);
        ((Button)mRootLayout.findViewById(R.id.btnFragmentSettings)).setOnClickListener(mOnClickListener);
        
        mRootLayout.setVisibility(View.INVISIBLE);
        
        return mRootLayout;
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
    
    public void setVisibility(int visibility) {
        mRootLayout.setVisibility(visibility);        
    }
}
