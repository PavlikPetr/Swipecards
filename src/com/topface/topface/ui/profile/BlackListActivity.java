package com.topface.topface.ui.profile;

import android.os.Bundle;
import android.view.View;
import com.topface.topface.R;
import com.topface.topface.ui.BaseFragmentActivity;


public class BlackListActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.ac_black_list_wrapper);
        findViewById(R.id.btnNavigationBack).setVisibility(View.VISIBLE);
    }
}
