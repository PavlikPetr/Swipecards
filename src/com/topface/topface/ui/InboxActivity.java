package com.topface.topface.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
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
import com.topface.topface.data.FeedInbox;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedInboxRequest;
import com.topface.topface.ui.adapters.InboxListAdapter;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

import java.util.LinkedList;

public class InboxActivity extends TrackedActivity {
    // Data
    private boolean mNewUpdating;
    private TextView mFooterView;
    private PullToRefreshListView mListView;
    private InboxListAdapter mListAdapter;
    private LinkedList<FeedInbox> mInboxDataList;
    private AvatarManager<FeedInbox> mAvatarManager;
    private DoubleBigButton mDoubleButton;
    private ProgressBar mProgressBar;
    private FeedInboxRequest inboxRequest;
    // Constants
    private static final int LIMIT = 40;
    private FloatBlock mFloatBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_inbox);
        Debug.log(this, "+onCreate");

        // Data
        mInboxDataList = new LinkedList<FeedInbox>();

        // Title Header
        ((TextView) findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.inbox_header_title));

        // Progress
        mProgressBar = (ProgressBar) findViewById(R.id.prsInboxLoading);

        // Double Button
        mDoubleButton = (DoubleBigButton) findViewById(R.id.btnDoubleBig);
        mDoubleButton.setLeftText(getString(R.string.inbox_btn_dbl_left));
        mDoubleButton.setRightText(getString(R.string.inbox_btn_dbl_right));
        mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
        mDoubleButton.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewUpdating = false;
                update(false);
            }
        });
        mDoubleButton.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewUpdating = true;
                update(false);
            }
        });

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
                Intent intent = new Intent(InboxActivity.this.getApplicationContext(), ChatActivity.class);
                FeedInbox item = (FeedInbox) parent.getItemAtPosition(position);
                intent.putExtra(ChatActivity.INTENT_USER_ID, item.uid);
                intent.putExtra(ChatActivity.INTENT_USER_NAME, item.first_name);
                intent.putExtra(ChatActivity.INTENT_USER_AVATAR, item.avatars_small);
                startActivity(intent);
            }
        });

        // Footer
        mFooterView = new TextView(getApplicationContext());
        mFooterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                history();
            }
        });
        mFooterView.setBackgroundResource(R.drawable.item_all_selector);
        mFooterView.setText(getString(R.string.general_footer_previous));
        mFooterView.setTextColor(Color.DKGRAY);
        mFooterView.setGravity(Gravity.CENTER);
        mFooterView.setTypeface(Typeface.DEFAULT_BOLD);
        mFooterView.setVisibility(View.GONE);
        mListView.getRefreshableView().addFooterView(mFooterView);

        // Control creating
        mAvatarManager = new AvatarManager<FeedInbox>(mInboxDataList);
        mListAdapter = new InboxListAdapter(getApplicationContext(), mAvatarManager);
        mListView.setAdapter(mListAdapter);

        mNewUpdating = CacheProfile.unread_messages > 0;
        CacheProfile.unread_messages = 0;

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
        if (inboxRequest != null) inboxRequest.cancel();

        release();
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }

    private void update(boolean isPushUpdating) {
        if (!isPushUpdating)
            mProgressBar.setVisibility(View.VISIBLE);
        mDoubleButton.setChecked(mNewUpdating ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);
        inboxRequest = new FeedInboxRequest(getApplicationContext());
        inboxRequest.limit = LIMIT;
        inboxRequest.only_new = mNewUpdating;
        inboxRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final LinkedList<FeedInbox> feedInboxList = FeedInbox.parse(response);
                if (mInboxDataList == null) {
                    return;
                }
                post(new Runnable() {
                    @Override
                    public void run() {
                        //Если это делать не в UI треде, то возникает вероятность падения из-за конфликта с PullToRefresh
                        mInboxDataList.clear();
                        mInboxDataList.addAll(feedInboxList);

                        if (mNewUpdating)
                            mFooterView.setVisibility(View.GONE);
                        else
                            mFooterView.setVisibility(View.VISIBLE);

                        if (feedInboxList.size() == 0 || feedInboxList.size() < LIMIT / 2)
                            mFooterView.setVisibility(View.GONE);

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
                        Utils.showErrorMessage(InboxActivity.this);
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

    private void history() {
        mProgressBar.setVisibility(View.VISIBLE);
        inboxRequest = new FeedInboxRequest(getApplicationContext());
        inboxRequest.limit = LIMIT;
        inboxRequest.only_new = false;
        inboxRequest.from = mInboxDataList.get(mInboxDataList.size() - 1).id;
        inboxRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final LinkedList<FeedInbox> feedInboxList = FeedInbox.parse(response);
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (feedInboxList.size() > 0)
                            mInboxDataList.addAll(feedInboxList);
                        if (feedInboxList.size() == 0 || feedInboxList.size() < LIMIT / 2)
                            mFooterView.setVisibility(View.GONE);
                        else
                            mFooterView.setVisibility(View.VISIBLE);
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
                        Utils.showErrorMessage(InboxActivity.this);
                        mFooterView.setVisibility(View.GONE);
                        mListView.onRefreshComplete();
                    }
                });
            }
        }).exec();
    }

    private void release() {
        mListView = null;

        if (mListAdapter != null)
            mListAdapter.release();
        mListAdapter = null;

        mAvatarManager = null;

        if (mInboxDataList != null)
            mInboxDataList.clear();
        mInboxDataList = null;

    }

}