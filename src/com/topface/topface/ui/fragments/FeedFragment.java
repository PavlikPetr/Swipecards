package com.topface.topface.ui.fragments;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.topface.topface.R;
import com.topface.topface.Recycle;
import com.topface.topface.Static;
import com.topface.topface.data.AbstractFeedItem;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.SwapAnimation;

public abstract class FeedFragment<T extends FeedAdapter> extends BaseFragment {
    protected PullToRefreshListView mListView;
    protected T mListAdapter;
    protected boolean mHasUnread;
    private TextView mBackgroundText;
    protected DoubleBigButton mDoubleButton;
    protected boolean mIsUpdating;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(getLayout(), null);

        // Home Button
        view.findViewById(R.id.btnNavigationHome).setOnClickListener((NavigationActivity) getActivity());
        // Set title
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(getTitle());

        mHasUnread = isHasUnread();

        initBackground(view);
        initFilter(view);
        initListView(view);
        if (mListAdapter.isNeedUpdate()) {
            updateData(false);
        }

        return view;
    }

    private void initBackground(View view) {
        // ListView background
        mBackgroundText = (TextView) view.findViewById(R.id.tvBackgroundText);

        mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(
                mBackgroundText.getCompoundDrawables()[0],
                getBackIcon(),
                mBackgroundText.getCompoundDrawables()[2],
                mBackgroundText.getCompoundDrawables()[3]
        );
    }

    protected abstract Drawable getBackIcon();

    abstract protected boolean isHasUnread();

    abstract protected int getTitle();

    protected int getLayout() {
        return R.layout.ac_feed;
    }

    private void initListView(View view) {
        // ListView
        mListView = (PullToRefreshListView) view.findViewById(R.id.lvFeedList);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                updateData(true);
            }
        });

        mListView.getRefreshableView().setOnItemClickListener(getOnItemClickListener());
        mListAdapter = getAdapter();
        mListView.setOnScrollListener(mListAdapter);
        mListView.getRefreshableView().setAdapter(mListAdapter);
    }

    abstract protected T getAdapter();

    protected FeedAdapter.Updater getUpdaterCallback() {
        return new FeedAdapter.Updater() {
            @Override
            public void onFeedUpdate() {
                if (!mIsUpdating) {
                    updateData(false, true);
                }
            }
        };
    }

    protected AdapterView.OnItemClickListener getOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AbstractFeedItem item = (AbstractFeedItem) parent.getItemAtPosition(position);
                if (!mIsUpdating && item.isLoaderRetry()) {
                    updateUI(new Runnable() {
                        public void run() {
                            mListAdapter.showLoaderItem();
                        }
                    });
                    updateData(false, true);
                } else {
                    try {
                        onFeedItemClick(item);
                    } catch (Exception e) {
                        Debug.error("FeedItem click error:", e);
                    }
                }
            }
        };
    }

    private void onFeedItemClick(AbstractFeedItem item) {
        // Open profile activity
        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
        intent.putExtra(ChatActivity.INTENT_USER_URL, item.getSmallLink());
        intent.putExtra(UserProfileActivity.INTENT_USER_ID, item.uid);
        intent.putExtra(UserProfileActivity.INTENT_USER_NAME, item.first_name);
        intent.putExtra(UserProfileActivity.INTENT_MUTUAL_ID, item.id);
        startActivity(intent);
    }

    abstract protected void updateData(boolean isPushUpdating, final boolean isHistoryLoad);

    protected void updateData(boolean isPushUpdating) {
        updateData(isPushUpdating, false);
    }

    @SuppressWarnings("deprecation")
    private void initFilter(View view) {
        final View controlGroup = view.findViewById(R.id.loControlsGroup);
        View showToolsBarButton = view.findViewById(R.id.btnNavigationFilterBar);
        showToolsBarButton.setVisibility(View.VISIBLE);
        showToolsBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlGroup.startAnimation(new SwapAnimation(controlGroup, R.id.loToolsBar));
            }
        });

        final View toolsBar = view.findViewById(R.id.loToolsBar);
        ViewTreeObserver vto = toolsBar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                int y = toolsBar.getMeasuredHeight();
                if (y != 0) {
                    y += Static.HEADER_SHADOW_SHIFT;
                    controlGroup.setPadding(controlGroup.getPaddingLeft(), -y, controlGroup.getPaddingRight(), controlGroup.getPaddingBottom());
                    ViewTreeObserver obs = controlGroup.getViewTreeObserver();
                    obs.removeGlobalOnLayoutListener(this);
                }
            }
        });

        // Double Button
        mDoubleButton = (DoubleBigButton) view.findViewById(R.id.btnDoubleBig);
        mDoubleButton.setLeftText(getString(R.string.inbox_btn_dbl_left));
        mDoubleButton.setRightText(getString(R.string.inbox_btn_dbl_right));
        mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
        mDoubleButton.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHasUnread = false;
                updateData(false);
            }
        });
        mDoubleButton.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHasUnread = true;
                updateData(false);
            }
        });
    }

    @Override
    protected void onUpdateSuccess(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mListView.setVisibility(View.VISIBLE);

            if (mListAdapter.isEmpty()) {
                mBackgroundText.setText(getEmptyFeedText());
            } else {
                mBackgroundText.setText("");
            }

            if (mBackgroundText.getCompoundDrawables()[0] != null) {
                ((AnimationDrawable) mBackgroundText.getCompoundDrawables()[0]).stop();
            }

            mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(Recycle.s_Loader_0,
                    mBackgroundText.getCompoundDrawables()[1],
                    mBackgroundText.getCompoundDrawables()[2],
                    mBackgroundText.getCompoundDrawables()[3]);
            mDoubleButton.setClickable(true);
        }
    }

    abstract protected int getEmptyFeedText();

    @Override
    protected void onUpdateFail(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mListView.setVisibility(View.VISIBLE);
            mBackgroundText.setText("");

            if (mBackgroundText.getCompoundDrawables()[0] != null) {
                ((AnimationDrawable) mBackgroundText.getCompoundDrawables()[0]).stop();
            }

            mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(Recycle.s_Loader_0,
                    mBackgroundText.getCompoundDrawables()[1],
                    mBackgroundText.getCompoundDrawables()[2],
                    mBackgroundText.getCompoundDrawables()[3]);
            mDoubleButton.setClickable(true);
        }
    }

    @Override
    protected void onUpdateStart(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mListView.setVisibility(View.INVISIBLE);
            mBackgroundText.setText(R.string.general_dialog_loading);
            mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(Recycle.s_Loader,
                    mBackgroundText.getCompoundDrawables()[1],
                    mBackgroundText.getCompoundDrawables()[2],
                    mBackgroundText.getCompoundDrawables()[3]);
            ((AnimationDrawable) mBackgroundText.getCompoundDrawables()[0]).start();
            mDoubleButton.setClickable(false);
        }
    }
}
