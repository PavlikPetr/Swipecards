package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.topface.topface.R;

/**
 * Created by kirussell on 28/01/15.
 * Fragment that holds ListView
 */
public abstract class ContentListFragment extends BaseFragment {
    private final static int ITEMS_COUNT_BEFORE_END = 5;

    private ListView mContentList;
    private ProgressBar mProgress;
    private BaseAdapter mAdapter;
    private View mFooterView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(getMainLayout(), container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Integer footerLayout = getFooterLayout();
        Integer headerLayout = getHeaderLayout();
        mContentList = (ListView) view.findViewById(R.id.content_list);
        if (null != headerLayout) {
            mContentList.addHeaderView(getActivity().getLayoutInflater().inflate(headerLayout, null));
        }
        if (null != footerLayout) {
            mFooterView = getActivity().getLayoutInflater().inflate(footerLayout, null);
            mContentList.addFooterView(mFooterView);
        }
        mProgress = (ProgressBar) view.findViewById(R.id.content_list_progress);
        if (mAdapter != null) {
            mContentList.setAdapter(mAdapter);
            setListShown(true);
        }
        mContentList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount - (firstVisibleItem + visibleItemCount) <= ITEMS_COUNT_BEFORE_END) {
                    needToLoad();
                }
            }
        });
    }

    protected Integer getFooterLayout() {
        return null;
    }

    protected Integer getHeaderLayout() {
        return null;
    }

    protected abstract int getMainLayout();

    protected void needToLoad() {
    }

    public ListView getListView() {
        return mContentList;
    }

    protected void setListShown(boolean show) {
        if (mContentList == null || mProgress == null) {
            throw new IllegalStateException("Views are not initialized yet. Call after onViewCreated()");
        }
        mContentList.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    protected void setAdapter(BaseAdapter adapter) {
        mAdapter = adapter;
        if (mContentList != null) {
            mContentList.setAdapter(adapter);
            setListShown(true);
        }
    }

    public void showMainProgressBar(boolean visibility) {
        if (null != mProgress) {
            mProgress.setVisibility(visibility ? View.VISIBLE : View.GONE);
        }
    }

    public void showFooterProgressBar(boolean visibility) {
        if (mFooterView != null) {
            mFooterView.setVisibility(visibility ? View.VISIBLE : View.GONE);
        }
    }


    protected BaseAdapter getAdapter() {
        return mAdapter;
    }
}