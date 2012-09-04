package com.topface.topface.ui.blocks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import com.google.android.apps.analytics.easytracking.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.data.Leaders;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.LeadersRequest;
import com.topface.topface.ui.LeadersActivity;
import com.topface.topface.ui.adapters.LeadersAdapter;
import com.topface.topface.ui.profile.ProfileActivity;
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

    public void loadLeaders() {
        new LeadersRequest(mActivity.getApplicationContext()).callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) throws NullPointerException {
                sendStat("Show", null);
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
        //При клике на кнопку "Хочу на свидание" открываем экран вставания в лидеры
        mActivity.findViewById(R.id.leadersDateBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendStat("WantDateButtonClick", null);
                    mActivity.startActivity(
                            new Intent(
                                    mActivity.getApplicationContext(),
                                    LeadersActivity.class
                            )
                    );
                }
        });
    }

    private void setAdapter(Leaders leaders) {
        HorizontalListView list = (HorizontalListView) mActivity.findViewById(R.id.leadersList);
        list.setAdapter(new LeadersAdapter(mContext, leaders));
        //Обработчик нажатия на лидера
        list.setOnItemClickListener(mItemClickListener);
    }

    //Листенер нажатия на лидера
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //При клике на лидера, открываем его профиль
            Leaders.LeaderUser leader = (Leaders.LeaderUser) adapterView.getAdapter().getItem(i);
            Intent intent = new Intent(mContext, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(ProfileActivity.INTENT_USER_ID, leader.user_id);
            intent.putExtra(ProfileActivity.INTENT_USER_NAME, leader.name);
            mContext.startActivity(intent);
        }
    };

    private void sendStat(String action, String label) {
        EasyTracker.getTracker().trackEvent("Leaders", action, label, 0);
    }


}
