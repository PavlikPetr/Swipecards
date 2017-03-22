package com.topface.topface.ui;

import android.content.Intent;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.topface.topface.utils.social.AuthToken;

/**
 * Activity that checks auth
 */
public abstract class CheckAuthActivity<T extends Fragment, V extends ViewDataBinding> extends SingleFragmentActivity<T, V> {

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
}
