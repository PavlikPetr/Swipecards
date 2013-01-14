package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.BuyingFragment;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.fragments.VipBuyFragment;

public class ContainerActivity extends BaseFragmentActivity {

    private int mCurrentFragmentId;
    private Fragment mCurrentFragment;

    private static final String TAG_FRAGMENT = "current_fragment";

    public static final int INTENT_BUY_VIP_FRAGMENT = 1;
    public static final int INTENT_BUYING_FRAGMENT = 2;
    public static final int INTENT_CHAT_FRAGMENT = 3;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.fragment_frame);

        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_left);

        // Title Header
        findViewById(R.id.btnNavigationHome).setVisibility(View.INVISIBLE);
        ImageButton backButton = ((ImageButton) findViewById(R.id.btnNavigationBack));
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        Intent intent = getIntent();
        mCurrentFragmentId = intent.getIntExtra(Static.INTENT_REQUEST_KEY,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCurrentFragment == null) {
            mCurrentFragment = getFragment(mCurrentFragmentId);
        }

        if (mCurrentFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.loFrame, mCurrentFragment,TAG_FRAGMENT).commit();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        FragmentManager manager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            mCurrentFragment = manager.findFragmentByTag(TAG_FRAGMENT);
        }
    }

    private Fragment getFragment(int id) {
        Fragment fragment = null;
        switch (id) {
            case INTENT_BUY_VIP_FRAGMENT:
                ((TextView) findViewById(R.id.tvNavigationTitle)).setText(getString(R.string.profile_vip_status));
                fragment = VipBuyFragment.newInstance();
//                Toast.makeText(App.getContext(), R.string.general_premium_access_error, Toast.LENGTH_SHORT).show();
                break;
            case INTENT_BUYING_FRAGMENT:
                ((TextView) findViewById(R.id.tvNavigationTitle)).setText(getString(R.string.buying_header_title));
                Bundle extras = getIntent().getExtras();
                if (extras.containsKey(BuyingFragment.ARG_ITEM_TYPE) && extras.containsKey(BuyingFragment.ARG_ITEM_PRICE)) {
                    fragment = BuyingFragment.newInstance(extras.getInt(BuyingFragment.ARG_ITEM_TYPE),
                            extras.getInt(BuyingFragment.ARG_ITEM_PRICE));
                } else {
                    fragment = BuyingFragment.newInstance();
                }
                break;
            case INTENT_CHAT_FRAGMENT:
                Intent intent = getIntent();

                fragment = ChatFragment.newInstance(intent.getIntExtra(ChatFragment.INTENT_ITEM_ID, -1),
                        intent.getIntExtra(ChatFragment.INTENT_USER_ID, -1),
                        false,
                        intent.getIntExtra(ChatFragment.INTENT_USER_SEX, Static.BOY),
                        intent.getStringExtra(ChatFragment.INTENT_USER_NAME),
                        intent.getIntExtra(ChatFragment.INTENT_USER_AGE, 0),
                        intent.getStringExtra(ChatFragment.INTENT_USER_CITY),
                        intent.getStringExtra(BaseFragmentActivity.INTENT_PREV_ENTITY));
            default:
                break;
        }
        return fragment;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }
}
