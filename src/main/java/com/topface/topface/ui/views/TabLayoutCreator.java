package com.topface.topface.ui.views;

import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;

import java.util.ArrayList;

/**
 * Класс для инициализации TabLayout
 * Created by onikitin on 31.07.15.
 */
public class TabLayoutCreator {

    private Activity mActivity;
    private TabLayout mTabLayout;
    private ArrayList<TextView> mTabViews;
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

    private void initTabView() {
        mTabViews = new ArrayList<>();
        for (int i = 0; i < mPagesTitles.size(); i++) {
            TextView textView = (TextView) LayoutInflater
                    .from(App.getContext()).inflate(R.layout.tab_indicator, null);
            mTabViews.add(textView);
            mTabLayout.getTabAt(i).setCustomView(textView);
        }
    }

    public void setTabTitle(int position) {
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TextView textView = mTabViews.get(i);
            if (i == position) {
                textView.setText(prepareTabIndicatorTitle(mPagesTitles.get(i)
                        , mPagesCounters == null ? 0 : mPagesCounters.get(i), true));
            } else {
                textView.setText(prepareTabIndicatorTitle(mPagesTitles.get(i)
                        , mPagesCounters == null ? 0 : mPagesCounters.get(i), false));
            }
        }
    }

    private CharSequence prepareTabIndicatorTitle(String title, int counter, boolean isSelectedTab) {
        SpannableString titleSpannable = new SpannableString(title);
        titleSpannable.setSpan(new ForegroundColorSpan(isSelectedTab
                ? mActivity.getResources().getColor(R.color.tab_text_color)
                : mActivity.getResources().getColor(R.color.disable_tab_color))
                , 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (counter > 0) {
            SpannableString counterSpannable = new SpannableString(String.valueOf(counter));
            counterSpannable.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.tab_counter_color))
                    , 0, counterSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return TextUtils.concat(titleSpannable, " ", counterSpannable);
        }
        return titleSpannable;
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

    public void isModeScrollable(boolean isScrollable) {
        if (isScrollable) {
            mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            changeLayoutWidth();
        } else {
            mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        }
    }

}
