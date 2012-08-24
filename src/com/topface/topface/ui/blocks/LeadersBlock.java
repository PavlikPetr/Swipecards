package com.topface.topface.ui.blocks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.HorizontalScrollView;
import com.topface.topface.R;
import com.topface.topface.data.Leaders;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.LeadersRequest;
import com.topface.topface.ui.DashboardActivity;
import com.topface.topface.ui.adapters.LeadersAdapter;
import com.topface.topface.ui.profile.gallery.HorizontalListView;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

/**
 * Блок с лидерами
 */
public class LeadersBlock {
    private Activity mActivity;
    private final Context mApplicationContext;

    public LeadersBlock(Activity activity) {
        mActivity = activity;
        mApplicationContext = mActivity.getApplicationContext();
        bindButtonEvent();
    }

    private void loadLeaders() {
        new LeadersRequest(mActivity.getApplicationContext()).callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) throws NullPointerException {
                setAdapter(Leaders.parse(response));
            }

            @Override
            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                Debug.error("Leaders loading error: " + codeError + "-" + response.toString());
            }
        });
    }

    private void bindButtonEvent() {
        mActivity.findViewById(R.id.leadersDateBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.startActivity(
                            new Intent(
                                    mActivity.getApplicationContext(),
                                    DashboardActivity.class
                            )
                    );
                }
        });
    }

    private void setAdapter(Leaders leaders) {
        HorizontalListView list = (HorizontalListView) mActivity.findViewById(R.id.leadersList);
        list.setAdapter(new LeadersAdapter(mApplicationContext, leaders));
    }


}
