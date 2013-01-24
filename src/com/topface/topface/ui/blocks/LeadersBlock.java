package com.topface.topface.ui.blocks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.data.FeedUserListData;
import com.topface.topface.data.Leader;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.LeadersRequest;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.LeadersActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.LeadersAdapter;
import com.topface.topface.ui.fragments.ProfileFragment;
import com.topface.topface.utils.Debug;

/**
 * Блок с лидерами
 */
public class LeadersBlock {
    private Activity mActivity;
    private final Context mContext;
    private final ViewGroup mLayout;

    public LeadersBlock(Activity activity, ViewGroup layout) {
        mActivity = activity;
        mContext = mActivity.getApplicationContext();
        mLayout = layout;

        bindButtonEvent();
        loadLeaders();

        layout.findViewById(R.id.leadersBlock).setVisibility(View.VISIBLE);
    }

    public void loadLeaders() {
        EasyTracker.getTracker().trackEvent("Leaders", "Load", "", 1L);
        LeadersRequest request = new LeadersRequest(mActivity.getApplicationContext());
        if (mActivity instanceof BaseFragmentActivity) {
            ((BaseFragmentActivity) mActivity).registerRequest(request);
        }
        request.callback(new DataApiHandler<FeedUserListData<Leader>>() {

            @Override
            protected void success(FeedUserListData<Leader> data, ApiResponse response) {
                setAdapter(data);
            }

            @Override
            protected FeedUserListData<Leader> parseResponse(ApiResponse response) {
                return new FeedUserListData<Leader>(response.jsonResult, Leader.class);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                Debug.error("Leaders loading error: " + codeError + "-" + response.toString());
            }

        }).exec();
    }

    private void bindButtonEvent() {
        //При клике на кнопку "Хочу на свидание" открываем экран вставания в лидеры
        mLayout.findViewById(R.id.leadersDateBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.startActivity(
                        new Intent(
                                mActivity.getApplicationContext(),
                                LeadersActivity.class
                        )
                );
            }
        });
    }

    private void setAdapter(FeedUserListData<Leader> leaders) {
        HorizontalListView list = (HorizontalListView) mLayout.findViewById(R.id.leadersList);
        if (list != null) {
            list.setAdapter(new LeadersAdapter(mContext, leaders));
            //Обработчик нажатия на лидера
            list.setOnItemClickListener(mItemClickListener);
        }
    }

    //Листенер нажатия на лидера
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //При клике на лидера, открываем его профиль
            Leader leader = (Leader) adapterView.getItemAtPosition(i);
            ((NavigationActivity) mActivity).onExtraFragment(
                    ProfileFragment.newInstance(leader.id, ProfileFragment.TYPE_USER_PROFILE));

        }
    };

}
