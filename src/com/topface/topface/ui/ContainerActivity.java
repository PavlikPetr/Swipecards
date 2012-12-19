package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.VipBuyFragment;

public class ContainerActivity extends BaseFragmentActivity {

    public static final int INTENT_BUY_VIP_FRAGMENT = 1;

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

        Fragment fragment = null;
        Intent intent = getIntent();
        switch (intent.getIntExtra(Static.INTENT_REQUEST_KEY, 0)) {
            case INTENT_BUY_VIP_FRAGMENT:
                ((TextView) findViewById(R.id.tvNavigationTitle)).setText(getString(R.string.profile_vip_status));
                fragment = VipBuyFragment.newInstance();
                Toast.makeText(App.getContext(), R.string.general_premium_access_error, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.loFrame, fragment).commit();
        }
    }

    @Override
    public boolean isTrackable() {
        return false;
    }
}
