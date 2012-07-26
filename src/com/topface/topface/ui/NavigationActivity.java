package com.topface.topface.ui;

import com.topface.topface.R;
import com.topface.topface.ui.fragments.FragmentFrameAdapter;
import com.topface.topface.ui.fragments.FragmentSwitchController;
import com.topface.topface.ui.fragments.FragmentMenu;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;


public class NavigationActivity extends FragmentActivity {
    
    private FragmentMenu mFragmentMenu;
    private FragmentFrameAdapter mFragmentAdapter;
    private FragmentSwitchController mSwitchController;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_navigation);
        
        mFragmentAdapter = new FragmentFrameAdapter(getSupportFragmentManager());
        
        mSwitchController = (FragmentSwitchController)findViewById(R.id.frameAnimation);
        mSwitchController.setFragmentFrameAdapter(mFragmentAdapter);
        
        mFragmentMenu = new FragmentMenu();
        mFragmentMenu.setOnClickListener(mSwitchController);
        
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_menu, mFragmentMenu).commit();
        
        //((Button)findViewById(R.id.btnMenu)).setOnClickListener(this);
    }

    //@Override
    public void onClick(View view) {
        mSwitchController.openMenu();
    }
    
}
