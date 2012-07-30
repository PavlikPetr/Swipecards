package com.topface.topface.ui;

import com.topface.topface.R;
import com.topface.topface.ui.fragments.FragmentContainer;
import com.topface.topface.ui.fragments.FragmentSwitchController;
import com.topface.topface.ui.fragments.FragmentMenu;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;


public class NavigationActivity extends FragmentActivity implements View.OnClickListener {
    private int mFragmentId;
    private int mPrevFragmentId;
    private FragmentMenu mFragmentMenu;
    private FragmentContainer mFragmentContainer;       // занимается переключением фрагментов
    private FragmentSwitchController mSwitchController; // занимается анимацией слоя с фрагментами
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_navigation);
        
        
        // last opened
        // R.id.fragment_tops
        
        mFragmentContainer = new FragmentContainer(R.id.fragment_tops, getSupportFragmentManager());
        
        mSwitchController = (FragmentSwitchController)findViewById(R.id.frameAnimation);
        mSwitchController.setFragmentSwitchListener(f);

        mFragmentMenu = new FragmentMenu();
        mFragmentMenu.setOnClickListener(this);
        
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_menu, mFragmentMenu).commit();

        ((Button)findViewById(R.id.btnTFHome)).setOnClickListener(v);
    }
    
    @Override
    public void onBackPressed() {
        if(mSwitchController.getAnimationState() == FragmentSwitchController.CLOSED)
            mSwitchController.openMenu();
        else
            super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnFragmentProfile:
                mFragmentId = R.id.fragment_profile;
                break;
            case R.id.btnFragmentDating:
                mFragmentId = R.id.fragment_dating;
                break;
            case R.id.btnFragmentLikes:
                mFragmentId = R.id.fragment_likes;
                break;
            case R.id.btnFragmentMutual:
                mFragmentId = R.id.fragment_mutual;
                break;
            case R.id.btnFragmentDialogs:
                mFragmentId = R.id.fragment_dialogs;
                break;
            case R.id.btnFragmentTops:
                mFragmentId = R.id.fragment_tops;
                break;
            case R.id.btnFragmentSettings:
                mFragmentId = R.id.fragment_settings;
                break; 
            default:
                break;
        }
        if(mFragmentId == mPrevFragmentId)
            mSwitchController.snapToScreen(FragmentSwitchController.COLLAPSE);
        else
            mSwitchController.snapToScreen(FragmentSwitchController.EXPAND_FULL);
    }
    
    View.OnClickListener v = new View.OnClickListener() {
        public void onClick(View view) {
            if(mSwitchController.getAnimationState() == FragmentSwitchController.EXPAND)
              mSwitchController.closeMenu();
            else {
              mFragmentMenu.setVisibility(View.VISIBLE);
              mSwitchController.openMenu();
            }
        }
    };
    
    FragmentSwitchController.FragmentSwitchListener f = new FragmentSwitchController.FragmentSwitchListener() {
        public void endAnimation(int Animation) {
            // switch
            if(Animation == FragmentSwitchController.EXPAND_FULL) {
                mFragmentContainer.showFragment(mFragmentId);
                mSwitchController.snapToScreen(FragmentSwitchController.COLLAPSE_FULL);
                mPrevFragmentId = mFragmentId;
            }
            if(Animation == FragmentSwitchController.COLLAPSE_FULL)
                mFragmentMenu.setVisibility(View.INVISIBLE);
            
            if(Animation == FragmentSwitchController.COLLAPSE)
                mFragmentMenu.setVisibility(View.INVISIBLE);
        }
    } ;

}
