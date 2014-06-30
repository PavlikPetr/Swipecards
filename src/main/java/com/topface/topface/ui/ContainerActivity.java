package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;

import com.topface.framework.utils.Debug;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.social.AuthToken;

import org.jetbrains.annotations.NotNull;

public class ContainerActivity extends CustomTitlesBaseFragmentActivity {
    public static final String UPDATE_USER_CATEGORY = "com.topface.topface.action.USER_CATEGORY";

    public static final String INTENT_USERID = "INTENT_USERID";
    public static final String FEED_ID = "FEED_ID";
    public static final String FEED_IDS = "FEED_IDS";
    public static final String TYPE = "type";
    public static final String CHANGED = "changed";
    public static final String VALUE = "value";
    private int mCurrentFragmentId = -1;
    private Fragment mCurrentFragment;
    private View mOnlineIcon;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initRequestKey();
        checkAuth();
        setContentView(R.layout.ac_fragment_frame);
        //Сперва пробуем получуить существующий фрагмент из fragmentManager
        mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.loFrame);
        if (mCurrentFragment == null) {
            //Если не находим, то создаем новый
            mCurrentFragment = getNewFragment(mCurrentFragmentId);
        }
        if (mCurrentFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(
                            R.id.loFrame,
                            mCurrentFragment
                    ).commit();
        }
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void initCustomActionBarView(View mCustomView) {
        mOnlineIcon = mCustomView.findViewById(R.id.online);
    }

    @Override
    protected int getActionBarCustomViewResId() {
        return R.layout.actionbar_container_title_view;
    }

    private void initRequestKey() {
        if (mCurrentFragmentId == -1) {
            Intent intent = getIntent();
            try {
                mCurrentFragmentId = intent.getIntExtra(Static.INTENT_REQUEST_KEY, 0);
            } catch (Exception ex) {
                Debug.error(ex);
                finish();
            }

            if (BuildConfig.DEBUG && mCurrentFragmentId <= 0) {
                throw new IllegalArgumentException(
                        "ContainerActivity needs request code, use static ContainerActivity methods to get Intents"
                );
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        FragmentManager manager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            mCurrentFragment = manager.findFragmentById(R.id.loFrame);
        }
    }

    private Fragment getNewFragment(int id) {
        Fragment fragment = null;
        Intent intent = getIntent();
        switch (id) {
        }

        return fragment;
    }

    public enum ActionTypes {BLACK_LIST, BOOKMARK}

    public static Intent getIntentForActionsUpdate(ActionTypes type, boolean value) {
        Intent intent = new Intent(UPDATE_USER_CATEGORY);
        intent.putExtra(TYPE, type);
        intent.putExtra(CHANGED, value);
        return intent;
    }

    public static Intent getValuedActionsUpdateIntent(ActionTypes type, int userId, boolean value) {
        Intent intent = new Intent(UPDATE_USER_CATEGORY);
        intent.putExtra(TYPE, type);
        intent.putExtra(VALUE, value);
        intent.putExtra(FEED_ID, userId);
        intent.putExtra(FEED_IDS, new int[]{userId});
        return intent;
    }

    public static Intent getValuedActionsUpdateIntent(ActionTypes type, int[] userIds, boolean value) {
        Intent intent = new Intent(UPDATE_USER_CATEGORY);
        intent.putExtra(TYPE, type);
        intent.putExtra(VALUE, value);
        intent.putExtra(FEED_IDS, userIds);
        return intent;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mCurrentFragment.onOptionsItemSelected(item);
                if (isTaskRoot()) {
                    Intent i = new Intent(this, NavigationActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    onBackPressed();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
