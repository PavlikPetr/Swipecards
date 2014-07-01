package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.topface.topface.R;
import com.topface.topface.utils.social.AuthToken;

/**
 * Activity that checks auth
 */
public abstract class CheckAuthActivity extends SingleFragmentActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        checkAuth();
    }

    private void checkAuth() {
        //Если нужно авторизоваться, то возвращаемся на NavigationActivity
        if (isNeedAuth() && AuthToken.getInstance().isEmpty()) {
            //Если это последняя активити в таске, то создаем NavigationActivity
            if (isTaskRoot()) {
                Intent i = new Intent(this, NavigationActivity.class);
                startActivity(i);
            }
            finish();
        }
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected void initCustomActionBarView(View mCustomView) {

    }

    @Override
    protected int getActionBarCustomViewResId() {
        return R.layout.actionbar_container_title_view;
    }
}
