package com.topface.topface.ui.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.topface.topface.App;

/**
 * Адовый костыль! Если в чате один итем или ни одного, нужно увелисить количество сообщения на 1,
 * для того, чтобы при перехде на новую строку в футере чата(input поле) анимация не отражалась снова.
 * Это происходит из-за перерисовки листа, и  неверного расчета условий анимации в методе
 * animateViewIfNecessary класса AnimationAdapter в библиотеке ListViewAnimation
 * Created by onikitin on 11.03.15.
 */
public class HackBaseAdapterDecorator extends BaseAdapter {

    private BaseAdapter mBaseAdapter;
    private static int T_HACK_ITEM = 3;
    private static final String TRANSLATION_Y = "translationY";
    private boolean mNeedAnimate = true;
    private final View mTempView = new View(App.getContext());
    private boolean mAnimateFirstMessage = true;


    public HackBaseAdapterDecorator(BaseAdapter baseAdapter) {
        mBaseAdapter = baseAdapter;
        if (mBaseAdapter.getCount() < 2) {
            mBaseAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    notifyDataSetChanged();
                    switch (mBaseAdapter.getCount()) {
                        case 0:
                            mAnimateFirstMessage = true;
                            break;
                        case 1:
                            mNeedAnimate = true;
                    }
                }
            });
        }
    }

    /**
     * хакаем, только если в чате 1 итем
     *
     * @return нужно хакнуть или нет
     */
    private boolean needHack() {
        return mBaseAdapter.getCount() == 1;
    }

    /**
     * Накручиваем позицию для исправления бага
     *
     * @return если нужно хакнуть, то увеличиваем количество на 1, если нет то сохраняем количество
     */
    @Override
    public int getCount() {
        return needHack() ? mBaseAdapter.getCount() + 1 : mBaseAdapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        return needHack() ? mBaseAdapter.getItem(0) : mBaseAdapter.getItem(position);
    }


    @Override
    public long getItemId(int position) {
        return needHack() ? mBaseAdapter.getItemId(0) : mBaseAdapter.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (needHack() && position == 1) {
            return T_HACK_ITEM;
        } else {
            return mBaseAdapter.getItemViewType(position);
        }
    }

    @Override
    public int getViewTypeCount() {
        return mBaseAdapter.getViewTypeCount() + 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        View view;
        if (type == T_HACK_ITEM) {
            return mTempView;
        } else {
            view = mBaseAdapter.getView(position, mTempView == convertView ? null : convertView, parent);
        }

        if (position == 1 && mBaseAdapter.getCount() == 2 && mNeedAnimate || mAnimateFirstMessage) {
            if (!mAnimateFirstMessage) {
                mNeedAnimate = false;
            }
            mAnimateFirstMessage = false;
            Animator animator = ObjectAnimator.ofFloat(view, TRANSLATION_Y, parent.getMeasuredHeight() >> 1, 0)
                    .setDuration(100);
            animator.start();
        }
        return view;
    }

}
