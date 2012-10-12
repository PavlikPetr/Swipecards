package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.apps.analytics.easytracking.TrackedActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.topface.topface.R;
import com.topface.topface.data.Visitor;
import com.topface.topface.data.Visitors;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.VisitorsRequest;
import com.topface.topface.ui.adapters.VisitorsAdapter;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

import java.util.LinkedList;

public class VisitorsActivity extends TrackedActivity {
    private PullToRefreshListView mListView;
    private VisitorsAdapter mListAdapter;
    private LinkedList<Visitor> mVisitorsList;
    private AvatarManager<Visitor> mAvatarManager;
    private ProgressBar mProgressBar;
    private VisitorsRequest mVisitorsRequest;
    private FloatBlock mFloatBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Используем layout от InboxActivity, т.к. он ничем не отличается
        setContentView(R.layout.ac_visitors);

        // Data
        mVisitorsList = new LinkedList<Visitor>();

        // Title Header
        ((TextView) findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.visitors_header_title));

        // Progress
        mProgressBar = (ProgressBar) findViewById(R.id.prsInboxLoading);

        // ListView
        mListView = (PullToRefreshListView) findViewById(R.id.lvInboxList);
        mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase refreshView) {
                update(true);
            }
        });

        mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(VisitorsActivity.this.getApplicationContext(), ProfileActivity.class);
                Visitor visitor = (Visitor) parent.getItemAtPosition(position);
                intent.putExtra(ProfileActivity.INTENT_USER_ID, visitor.id);
                intent.putExtra(ProfileActivity.INTENT_USER_NAME, visitor.name);
                startActivity(intent);
            }
        });

        // Control creating
        mAvatarManager = new AvatarManager<Visitor>(mVisitorsList);
        mListAdapter = new VisitorsAdapter(getApplicationContext(), mAvatarManager);
        mListView.setAdapter(mListAdapter);

        update(false);

        mFloatBlock = new FloatBlock(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFloatBlock.update();
    }

    @Override
    protected void onDestroy() {
        if (mVisitorsRequest != null) mVisitorsRequest.cancel();

        release();
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }

    private void update(boolean isPushUpdating) {
        if (!isPushUpdating)
            mProgressBar.setVisibility(View.VISIBLE);

        mVisitorsRequest = new VisitorsRequest(getApplicationContext());
        mVisitorsRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mVisitorsList.clear();
                        mVisitorsList.addAll(Visitors.parse(response).visitors);
                        mProgressBar.setVisibility(View.GONE);
                        mListView.onRefreshComplete();
                        mListAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showErrorMessage(VisitorsActivity.this);
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(View.GONE);
                        }
                        if (mListView != null) {
                            mListView.onRefreshComplete();
                        }
                    }
                });
            }
        }).exec();
    }

    private void release() {
        mListView = null;

        if (mListAdapter != null) {
            mListAdapter.release();
        }

        mListAdapter = null;
        mAvatarManager = null;

        if (mVisitorsList != null) {
            mVisitorsList.clear();
        }
        mVisitorsList = null;
    }

}