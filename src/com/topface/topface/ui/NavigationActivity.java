package com.topface.topface.ui;

import com.topface.topface.R;
import com.topface.topface.ui.fragments.DialogsFragment;
import com.topface.topface.ui.fragments.FragmentContainer;
import com.topface.topface.ui.fragments.FragmentSwitchController;
import com.topface.topface.ui.fragments.FragmentMenu;
import com.topface.topface.utils.Debug;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;


public class NavigationActivity extends FragmentActivity  {
    private int mFragmentId;
    private int mPrevFragmentId;
    private FragmentMenu mFragmentMenu;
    private FragmentContainer mFragmentContainer;       // занимается переключением фрагментов
    private FragmentSwitchController mSwitchController; // занимается анимацией слоя с фрагментами
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_navigation);
        Debug.log(this, "onCreate");
        
        // Menu
        mFragmentMenu = new FragmentMenu();
        mFragmentMenu.setOnMenuListener(mOnMenuListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_menu, mFragmentMenu).commit();
        
        // Fragments
        mFragmentContainer = new FragmentContainer(getSupportFragmentManager());
        
        // Switch Controller
        mSwitchController = (FragmentSwitchController)findViewById(R.id.frameAnimation);
        mSwitchController.setFragmentSwitchListener(mFragmentSwitchListener);
        
        // last opened
        //mFragmentContainer.showFragment(R.id.fragment_dialogs);
        
        ((Button)findViewById(R.id.btnHeaderHome)).setOnClickListener(mOnHomeClickListener);
    }
    
    @Override
    protected void onRestart() {
        Debug.log(this, "onRestart");
        super.onRestart();
    }
    
    @Override
    protected void onStart() {
        Debug.log(this, "onStart");
        super.onStart();
    }
    @Override
    protected void onResume() {
        Debug.log(this, "onResume");
        super.onResume();
    }
    @Override
    protected void onPostResume() {
        Debug.log(this, "onPostResume");
        super.onPostResume();
    }
    @Override
    protected void onPause() {
        Debug.log(this, "onPause");
        super.onPause();
    }
    @Override
    protected void onResumeFragments() {
        Debug.log(this, "onResumeFragments");
        super.onResumeFragments();
    }
    @Override
    protected void onStop() {
        Debug.log(this, "onStop");
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        Debug.log(this, "onDestroy");
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        if(mSwitchController.getAnimationState() == FragmentSwitchController.CLOSED ||
           mSwitchController.getAnimationState() == FragmentSwitchController.COLLAPSE_FULL) {
            mFragmentMenu.setVisibility(View.VISIBLE);
            mSwitchController.openMenu();
        } else {
            super.onBackPressed();
        }
    }
    
    View.OnClickListener mOnHomeClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (mSwitchController.getAnimationState() == FragmentSwitchController.EXPAND) {
              mSwitchController.closeMenu();
            } else {
              mFragmentMenu.setVisibility(View.VISIBLE);
              mSwitchController.openMenu();
            }
        }
    };
    
    FragmentMenu.FragmentMenuListener mOnMenuListener = new FragmentMenu.FragmentMenuListener() {
        @Override
        public void onMenuClick(int fragmentID) {
            if(mPrevFragmentId == fragmentID) {
                mSwitchController.snapToScreen(FragmentSwitchController.COLLAPSE);
            } else {
                mSwitchController.snapToScreen(FragmentSwitchController.EXPAND_FULL);
            }
            mFragmentId = fragmentID;
        }
    };
    
    FragmentSwitchController.FragmentSwitchListener mFragmentSwitchListener = new FragmentSwitchController.FragmentSwitchListener() {
        @Override
        public void endAnimation(int Animation) {
            if(Animation == FragmentSwitchController.EXPAND_FULL) {
                mFragmentContainer.showFragment(mFragmentId);
                mSwitchController.snapToScreen(FragmentSwitchController.COLLAPSE_FULL);
                mPrevFragmentId = mFragmentId;
            } else {
                if(Animation == FragmentSwitchController.COLLAPSE_FULL)
                    mFragmentMenu.setVisibility(View.INVISIBLE);
                
                if(Animation == FragmentSwitchController.COLLAPSE)
                    mFragmentMenu.setVisibility(View.INVISIBLE);
            }
        }
    } ;

}
