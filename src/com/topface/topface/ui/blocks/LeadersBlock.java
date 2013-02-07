package com.topface.topface.ui.blocks;

import android.content.Intent;
import android.support.v4.app.Fragment;
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
import com.topface.topface.ui.LeadersActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.LeadersAdapter;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.ProfileFragment;
import com.topface.topface.utils.CacheProfile;

/**
 * Блок с лидерами
 */
public class LeadersBlock {
    private Fragment mFragment;
    private final ViewGroup mLayout;

    public LeadersBlock(Fragment fragment, ViewGroup layout) {
        mFragment  = fragment;
        mLayout = layout;

        bindButtonEvent();
        loadLeaders();

        layout.findViewById(R.id.leadersBlock).setVisibility(View.VISIBLE);
    }

    public void loadLeaders() {
        EasyTracker.getTracker().trackEvent("Leaders", "Load", "", 1L);
        LeadersRequest request = new LeadersRequest(mFragment.getActivity().getApplicationContext());
        if (mFragment instanceof BaseFragment) {
            ((BaseFragment) mFragment).registerRequest(request);
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
            public void fail(int codeError, ApiResponse response) {}

        }).exec();
    }

    private void bindButtonEvent() {
        //При клике на кнопку "Хочу на свидание" открываем экран вставания в лидеры
        mLayout.findViewById(R.id.leadersDateBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFragment.startActivity(
                        new Intent(
                                mFragment.getActivity().getApplicationContext(),
                                LeadersActivity.class
                        )
                );
            }
        });
    }

    private void setAdapter(FeedUserListData<Leader> leaders) {
        HorizontalListView list = (HorizontalListView) mLayout.findViewById(R.id.leadersList);
        if (list != null) {
            list.setAdapter(new LeadersAdapter(mFragment.getActivity().getApplicationContext(), leaders));
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
            int type = (leader.id == CacheProfile.uid) ? ProfileFragment.TYPE_MY_PROFILE : ProfileFragment.TYPE_USER_PROFILE;
            ((NavigationActivity) mFragment.getActivity()).onExtraFragment(
                    ProfileFragment.newInstance(leader.id, type));

        }
    };

}
