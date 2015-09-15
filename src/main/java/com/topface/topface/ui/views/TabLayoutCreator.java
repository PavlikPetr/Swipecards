package com.topface.topface.ui.views;

import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.topface.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Класс для инициализации TabLayout
 * Created by onikitin on 31.07.15.
 */
public class TabLayoutCreator {

    private Activity mActivity;
    private TabLayout mTabLayout;
    private ArrayList<TabViewsContainer> mTabViews;
    private ArrayList<String> mPagesTitles;
    private ArrayList<Integer> mPagesCounters;

    public TabLayoutCreator(Activity activity, ViewPager pager, TabLayout tabLayout
            , ArrayList<String> pageTitles, ArrayList<Integer> pageCounters) {
        mActivity = activity;
        mPagesCounters = pageCounters;
        mPagesTitles = pageTitles;
        mTabLayout = tabLayout;
        mTabLayout.setupWithViewPager(pager);
        initTabView();
    }

    public class TabViewsContainer {

        @Bind(R.id.tab_title)
        public TextView titleView;
        @Bind(R.id.tab_counter)
        public TextView counterView;
        public View tabView;

        public TabViewsContainer(Activity activity) {
            tabView = LayoutInflater.from(activity).inflate(R.layout.tab_indicator, null);
            ButterKnife.bind(this, tabView);
        }
    }

    private void initTabView() {
        mTabViews = new ArrayList<>();
        TabViewsContainer viewsContainer;
        for (int i = 0; i < mPagesTitles.size(); i++) {
            viewsContainer = new TabViewsContainer(mActivity);
            mTabViews.add(viewsContainer);
            mTabLayout.getTabAt(i).setCustomView(viewsContainer.tabView);
        }
    }

    public void setTabTitle(int position) {
        TabViewsContainer container;
        String title;
        int counter;
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            title = mPagesTitles.get(i);
            counter = mPagesCounters == null ? 0 : mPagesCounters.get(i);
            container = mTabViews.get(i);
            container.titleView.setEnabled(i == position);
            container.titleView.setText(title);
            if (counter > 0) {
                container.counterView.setEnabled(i == position);
                container.counterView.setText(String.valueOf(counter));
            } else {
                container.counterView.setText("");
            }
        }
    }

    /**
     * Для вкладки симпатий нужно выставить wrap_content, через XML это не сделать, так как
     * перестанет работать свойтво fixed для остальных вкладок с табами
     */
    private void changeLayoutWidth() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mTabLayout.getLayoutParams();
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        mTabLayout.setLayoutParams(layoutParams);
    }

    /**
     * Если табы дляжны скролится ныжно вызвать этот метод. При этом TabLayout нужно обернуть в
     * LinearLayout
     *
     * @param isScrollable скролящиеся табы или нет
     */
    @SuppressWarnings("unused")
    public void isModeScrollable(boolean isScrollable) {
        if (isScrollable) {
            mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            changeLayoutWidth();
        } else {
            mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        }
    }

}
