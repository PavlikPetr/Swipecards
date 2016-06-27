package com.topface.topface.ui.adapters.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

/**
 * Created by tiberal on 23.06.16.
 */
@SuppressWarnings("unused")
public class ViewInjectManager {

    private LinkedList<InjectViewBucket> mViewBuckets = new LinkedList<>();
    private LinkedList<Integer> mFakePositions = new LinkedList<>();
    private Context mContext;

    public ViewInjectManager(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * @param position - позиция для которой нужно попытаться получить вьюху
     * @return вьюха, которую нужно заинжектить в лист
     */
    @Nullable
    public View getView(int position) {
        View itemView = null;
        InjectViewBucket bucket = getInjectableViewForPos(position);
        if (bucket != null) {
            switch (bucket.getType()) {
                case InjectViewBucket.RES:
                    throwExceptionIfNeed(InjectViewBucket.RES);
                    itemView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                            .inflate(bucket.getInjectLayoutRes(), null, true);
                    break;
                case InjectViewBucket.VIEW:
                    throwExceptionIfNeed(InjectViewBucket.VIEW);
                    itemView = bucket.getInjectLayout();
                    break;
                case InjectViewBucket.FACTORY:
                    IInjectViewFactory factory = bucket.getInjectLayoutFactory();
                    if (factory != null) {
                        itemView = factory.construct();
                    }
            }
            if (itemView != null) {
                //хак для биндингов. там к вьюхе прилеплен объект биндинга.
                itemView.setTag(null);
            }
            handleFakePosition(position);
        }
        return itemView;
    }

    /**
     * Бросаем exception если тип не FACTORY и фейков больше одного. Только с FACTORY можно
     * вставлять несколько одинаковых вьюх.
     *
     * @param type - тип контейнера с вьюхой.
     */
    private void throwExceptionIfNeed(@InjectViewBucket.MetamorphoseType int type) {
        if (mFakePositions.size() == 1 || type != InjectViewBucket.FACTORY) {
            try {
                throw new Exception("U can can use once. Use IInjectViewFactory");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Обрабатываем позицию вставленной вьюхи.
     */
    private void handleFakePosition(int position) {
        if (mFakePositions.isEmpty()) {
            mFakePositions.add(position);
            return;
        }
        if (!mFakePositions.contains(position)) {
            if (position > mFakePositions.getFirst()) {
                mFakePositions.addLast(position);
            } else {
                mFakePositions.addFirst(position);
            }
        }
    }

    public void clearFakePositionList() {
        mFakePositions.clear();
    }

    /**
     * Получаем настоящую позицию в данных списка. Без учета вставленных итемов.
     */
    public int getTruePosition(int position) {
        return mFakePositions.isEmpty() || mFakePositions.getLast() > position
                ? position - mFakePositions.indexOf(position - 1) - 1
                : position - mFakePositions.size();
    }

    @Nullable
    private InjectViewBucket getInjectableViewForPos(int pos) {
        for (InjectViewBucket metamorphose : mViewBuckets) {
            IViewInjectRule filter = metamorphose.getFilter();
            if (filter != null && filter.isNeedInject(pos)) {
                return metamorphose;
            }
        }
        return null;
    }

    public boolean isFakePosition(int position) {
        return mFakePositions.contains(position);
    }

    public boolean hasInjectableView() {
        return mViewBuckets != null && !mViewBuckets.isEmpty();
    }

    public void registerInjectViewBucket(InjectViewBucket bucket) {
        mViewBuckets.add(bucket);
    }

    public void removeInjectViewBucket(InjectViewBucket bucket) {
        mViewBuckets.remove(bucket);
    }

    public void removeAllBuckets() {
        mViewBuckets.clear();
    }
}
