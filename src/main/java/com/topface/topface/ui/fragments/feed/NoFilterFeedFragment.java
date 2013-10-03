package com.topface.topface.ui.fragments.feed;

import android.view.View;

import com.topface.topface.data.FeedItem;

public abstract class NoFilterFeedFragment<T extends FeedItem> extends FeedFragment<T> {

    @Override
    protected final void initFilter(View view) {
        //Фильтра в данном типе фрагмента нет
    }

    @Override
    protected final boolean isShowUnreadItems() {
        //Данный фрагмент оперирует только прочитаными элементами ленты, здесь нет поддержки фильтра
        return false;
    }

    @Override
    protected final void setFilterSwitcherState(boolean clickable) {
        //Мы не используем фильтр, поэтому и его статус менять не будем
    }
}
