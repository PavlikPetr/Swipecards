package com.topface.topface.ui.adapters;

import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ItemEventListener<D> implements View.OnClickListener, View.OnLongClickListener {
    private boolean mIsLongClick;
    @Nullable
    private OnRecyclerViewItemClickListener<D> mRecyclerViewItemClickListener;
    @Nullable
    private OnRecyclerViewItemLongClickListener<D> mRecyclerViewItemLongClickListener;

    @Override
    public void onClick(View v) {
        if (mRecyclerViewItemClickListener != null && !mIsLongClick) {
            int pos = getPosition(v);
            mRecyclerViewItemClickListener.itemClick(v, pos, getDataItem(pos));
        } else {
            mIsLongClick = false;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        mIsLongClick = true;
        if (mRecyclerViewItemLongClickListener != null) {
            int pos = getPosition(v);
            mRecyclerViewItemLongClickListener.itemLongClick(v, pos, getDataItem(pos));
            return false;
        }
        return true;
    }

    public void registerClickListener(@NotNull OnRecyclerViewItemClickListener<D> clickListener) {
        mRecyclerViewItemClickListener = clickListener;
    }

    public void registerLongClickListener(@NotNull OnRecyclerViewItemLongClickListener<D> longClickListener) {
        mRecyclerViewItemLongClickListener = longClickListener;
    }

    abstract public D getDataItem(int pos);

    abstract public int getPosition(View v);

    /**
     * Листенер для оработки нажатий на элемент списка.
     * Для перехвата кликов следует использовать именно его, а не вешать листенеры в
     * onBindViewHolder или в ViewHolder
     * Для добавления нужно передать в сеттер адаптера объект листенера,
     * и в расметке итема в тег <data> вставать следующею конструкцию:
     * <p/>
     * <variable
     * name="clickListener"
     * type="android.view.View.OnClickListener" />
     * <p/>
     * затем нужным вьюхам назначить атрибут onClick в XML
     */
    public interface OnRecyclerViewItemClickListener<D> {

        /**
         * @param view         - вьюха на которую был произведен клик
         * @param itemPosition - позиция
         * @param data         - итем данных
         */
        void itemClick(View view, int itemPosition, D data);
    }

    /**
     * Листенер для оработки долгих нажатий на элемент списка.
     * Для добавления нужно передать в сеттер адаптера объект листенера,
     * и в расметке итема в тег <data> вставать следующею конструкцию:
     * <p/>
     * <variable
     * name="longClickListener"
     * type="android.view.View.OnClickListener" />
     * <p/>
     * Затем, чтобы прийти к успеху, в нужной вьюхе прописать
     * app:onLongItemClick="@{longClickListener}"
     */
    public interface OnRecyclerViewItemLongClickListener<D> {
        /**
         * @param view         - вьюха на которую был произведен клик
         * @param itemPosition - позиция
         * @param data         - итем данных
         */
        void itemLongClick(View view, int itemPosition, D data);
    }
}