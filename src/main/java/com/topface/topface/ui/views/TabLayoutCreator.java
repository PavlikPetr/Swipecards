package com.topface.topface.ui.views;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.utils.ListUtils;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Класс для инициализации TabLayout
 * Created by onikitin on 31.07.15.
 */
public class TabLayoutCreator {

    private Context mContext;
    private TabLayout mTabLayout;
    private ArrayList<TabViewsContainer> mTabViews;
    private ArrayList<String> mPagesTitles;
    private ArrayList<Integer> mPagesCounters;

    public TabLayoutCreator(Context context, ViewPager pager, TabLayout tabLayout
            , ArrayList<String> pageTitles, ArrayList<Integer> pageCounters) {
        mContext = context.getApplicationContext();
        mPagesCounters = pageCounters;
        mPagesTitles = pageTitles;
        mTabLayout = tabLayout;
        mTabLayout.setupWithViewPager(pager);
        initTabView();
    }

    public void release() {
        mTabLayout = null;
        mContext = null;
        if (mPagesCounters != null) {
            mPagesCounters.clear();
        }
        if (mPagesTitles != null) {
            mPagesTitles.clear();
        }
        for (TabViewsContainer container : mTabViews) {
            container.clear();
        }
        mTabViews.clear();
    }

    // TODO: 25.05.16 Убираешь батернайф? Сделай это приватом!
    public static class TabViewsContainer {

        @BindView(R.id.tab_title)
        public TextView titleView;
        @BindView(R.id.tab_counter)
        public TextView counterView;
        public View tabView;

        public TabViewsContainer(Context context) {
            tabView = LayoutInflater.from(context).inflate(R.layout.tab_indicator, null);
            ButterKnife.bind(this, tabView);
        }

        public void clear() {
            titleView = null;
            counterView = null;
            tabView = null;
        }

    }

    private void initTabView() {
        mTabViews = new ArrayList<>();
        TabViewsContainer viewsContainer;
        for (int i = 0; i < mPagesTitles.size(); i++) {
            viewsContainer = new TabViewsContainer(mContext);
            mTabViews.add(viewsContainer);
            mTabLayout.getTabAt(i).setCustomView(viewsContainer.tabView);
        }
    }

    public void setTabTitle(int position) {
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            String title = ListUtils.isEntry(i, mPagesTitles) ? mPagesTitles.get(i) : Utils.EMPTY;
            int counter = ListUtils.isEntry(i, mPagesCounters) ? mPagesCounters.get(i) : 0;
            TabViewsContainer container = ListUtils.isEntry(i, mTabViews) ? mTabViews.get(i) : new TabViewsContainer(mContext);
            container.titleView.setEnabled(i == position);
            container.titleView.setText(title);
            if (counter > 0) {
                container.counterView.setEnabled(i == position);
                container.counterView.setText(String.valueOf(counter));
            } else {
                container.counterView.setText(Utils.EMPTY);
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
