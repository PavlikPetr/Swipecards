package com.topface.topface.ui.blocks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import com.topface.topface.R;
import com.topface.topface.data.Leaders;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.LeadersRequest;
import com.topface.topface.ui.DashboardActivity;
import com.topface.topface.ui.adapters.LeadersAdapter;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.profile.gallery.HorizontalListView;
import com.topface.topface.utils.Debug;

/**
 * Блок с лидерами
 */
public class LeadersBlock {
    private Activity mActivity;
    private final Context mContext;

    public LeadersBlock(Activity activity) {
        mActivity = activity;
        mContext = mActivity.getApplicationContext();

        bindButtonEvent();
        loadLeaders();

        mActivity.findViewById(R.id.leadersBlock).setVisibility(View.VISIBLE);
    }

    private void loadLeaders() {
        new LeadersRequest(mActivity.getApplicationContext()).callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) throws NullPointerException {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAdapter(Leaders.parse(response));
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                Debug.error("Leaders loading error: " + codeError + "-" + response.toString());
            }
        }).exec();
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
        list.setAdapter(new LeadersAdapter(mContext, leaders));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Leaders.LeaderUser leader = (Leaders.LeaderUser) adapterView.getAdapter().getItem(i);
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(ProfileActivity.INTENT_USER_ID, leader.user_id);
                        intent.putExtra(ProfileActivity.INTENT_USER_NAME, leader.name);
                        mContext.startActivity(intent);
            }
        });
    }


}
