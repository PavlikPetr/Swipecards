package com.topface.topface.ui.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Rect;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.topface.framework.utils.Debug;
import com.topface.topface.BR;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.databinding.ItemUserGalleryAddBtnBinding;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;


/**
 * Базовый адаптер для фоточек. Может header/footer(лодер). Может Add button.
 * Created by tiberal on 12.01.16.
 */
public abstract class BasePhotoRecyclerViewAdapter<T extends ViewDataBinding> extends RecyclerView.Adapter<BasePhotoRecyclerViewAdapter.ItemViewHolder> {

    private static final int TYPE_ADD = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_ITEM = 2;
    private static final int TYPE_FOOTER = 3;
    private int mItemWidth;
    private View mHeaderView;
    private View mFooterView;
    protected Photos mPhotoLinks;
    private int mColumnCount;
    private boolean mHeaderIsBindingView;
    private boolean mFooterIsBindingView;
    private OnRecyclerViewItemClickListener mRecyclerViewItemClickListener;
    private OnRecyclerViewItemLongClickListener mRecyclerViewItemLongClickListener;
    private RecyclerView mRecyclerView;
    private LoadingListAdapter.Updater mUpdater;
    private AlbumLoadController mLoadController;
    private boolean mNeedLoadNewItems = false;
    private int mFirstVisibleItemPos = 0;
    private int mLastVisibleItemPos = 0;
    private int mTotalPhotos;

    public BasePhotoRecyclerViewAdapter(Photos photoLinks, int totalPhotos, LoadingListAdapter.Updater callback) {
        mPhotoLinks = new Photos();
        mTotalPhotos = totalPhotos;
        mUpdater = callback;
        if (photoLinks != null) {
            setData(photoLinks, totalPhotos > photoLinks.size(), false);
        }
        mLoadController = new AlbumLoadController(AlbumLoadController.FOR_GALLERY);
    }

    public BasePhotoRecyclerViewAdapter setHeader(View header, boolean isBindingView) {
        if (isAddPhotoButtonEnabled()) {
            Debug.log("U must use only header or add button");
            return this;
        }
        mHeaderIsBindingView = isBindingView;
        mHeaderView = header;
        addFakePhoto(0);
        return this;
    }

    public BasePhotoRecyclerViewAdapter setFooter(View footer, boolean isBindingView) {
        mFooterIsBindingView = isBindingView;
        mFooterView = footer;
        return this;
    }

    public int getFirstVisibleItemPos() {
        return mFirstVisibleItemPos;
    }

    public int getLastVisibleItemPos() {
        return mLastVisibleItemPos;
    }

    public boolean hasHeader() {
        return mHeaderView != null;
    }

    public boolean hasFooter() {
        return mFooterView != null;
    }

    private void addFakePhoto(int fakePos) {
        Photos photos = getAdapterData();
        if (photos == null) {
            photos = new Photos();
        }
        photos.add(fakePos, Photo.createFakePhoto());
    }

    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {

            private int mPadding = 0;

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if (mPadding == 0) {
                    mPadding = (int) view.getContext().getResources().getDimension(R.dimen.add_to_leader_spacing_value);
                }
                outRect.bottom = mPadding;
                outRect.right = mPadding;
            }
        };
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addItemDecoration(getItemDecoration());
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        final StaggeredGridLayoutManager layoutManager = ((StaggeredGridLayoutManager) mRecyclerView.getLayoutManager());
        mColumnCount = layoutManager.getSpanCount();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                int[] first = layoutManager.findFirstVisibleItemPositions(null);
                int[] last = layoutManager.findLastVisibleItemPositions(null);
                mFirstVisibleItemPos = first[0];
                mLastVisibleItemPos = last[last.length - 1];
                int visibleItemCount = last[last.length - 1] - first[0];
                if (visibleItemCount != 0 && first[0] + visibleItemCount >= getPhotos().size() - 1 - mLoadController.getItemsOffsetByConnectionType() && mNeedLoadNewItems) {
                    if (mUpdater != null && !getAdapterData().isEmpty()) {
                        mNeedLoadNewItems = false;
                        mUpdater.onUpdate();
                    }
                }
            }
        });
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView.clearOnScrollListeners();
    }


    @Override
    public BasePhotoRecyclerViewAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        boolean isBindingView = true;
        mItemWidth = parent.getWidth() / mColumnCount;
        switch (viewType) {
            case TYPE_ADD:
                itemView = inflater.inflate(R.layout.item_user_gallery_add_btn, null, false);
                break;
            case TYPE_HEADER:
                itemView = createLayoutParams(mHeaderView);
                isBindingView = mHeaderIsBindingView;
                break;
            case TYPE_FOOTER:
                itemView = createLayoutParams(mFooterView);
                isBindingView = mFooterIsBindingView;
                break;
            default:
                itemView = inflater.inflate(getItemLayoutId(), null);
        }
        return new ItemViewHolder(itemView, isBindingView);
    }

    public int getItemWidth() {
        return mItemWidth;
    }

    private View createLayoutParams(View targetView) {
        StaggeredGridLayoutManager.LayoutParams params;
        params = new StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT, StaggeredGridLayoutManager.LayoutParams.WRAP_CONTENT);
        params.setFullSpan(true);
        targetView.setLayoutParams(params);
        return targetView;
    }


    @Override
    public void onBindViewHolder(BasePhotoRecyclerViewAdapter.ItemViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == TYPE_ADD && holder.getBinding() != null && holder.getBinding() instanceof ItemUserGalleryAddBtnBinding) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(getItemWidth(), getItemWidth());
            holder.getBinding().getRoot().setLayoutParams(lp);
            ((ItemUserGalleryAddBtnBinding) holder.getBinding()).ivPhoto.setLayoutParams(lp);
        }
        if (type == TYPE_ITEM && holder.getBinding() != null) {
            setHandlers(getItemBinding(holder), position);
            return;
        }
        if (type == TYPE_HEADER || type == TYPE_FOOTER) {
            setHeaderOrFooterHandlers(holder.getFooterOrHeaderBinding(), position);
        }
    }

    @Override
    public int getItemCount() {
        return getAdapterData().size();
    }

    public Photo getItem(int pos) {
        return getAdapterData().get(pos);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            //если есть кнопка add photo, то header не доступен
            if (isAddPhotoButtonEnabled()) {
                return TYPE_ADD;
            }
            if (mHeaderView != null) {
                return TYPE_HEADER;
            }
        }
        if (position == getAdapterData().size() - 1 && getAdapterData().get(position).isFake() && mFooterView != null) {
            return getPhotos().size() != mTotalPhotos ? TYPE_FOOTER : TYPE_ITEM;
        }
        return TYPE_ITEM;
    }

    public Photos getAdapterData() {
        if (mPhotoLinks == null) {
            mPhotoLinks = new Photos();
        }
        return mPhotoLinks;
    }

    private T getItemBinding(BasePhotoRecyclerViewAdapter.ItemViewHolder holder) {
        return getItemBindingClass().cast(holder.getBinding());
    }

    public void setHeaderOrFooterHandlers(ViewDataBinding binding, int position) {
    }

    protected abstract Class<T> getItemBindingClass();

    protected abstract void setHandlers(T binding, int position);

    @LayoutRes
    protected abstract int getItemLayoutId();

    public void setOnItemClickListener(OnRecyclerViewItemClickListener itemClick) {
        mRecyclerViewItemClickListener = itemClick;
    }

    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener itemLongClick) {
        mRecyclerViewItemLongClickListener = itemLongClick;
    }

    public void setData(Photos photoLinks, boolean needMore, boolean notifyAll) {
        setData(photoLinks, needMore, isAddPhotoButtonEnabled(), notifyAll);
    }

    public void setData(Photos photoLinks, boolean needMore, boolean isAddPhotoButtonEnabled, boolean notifyAll) {
        getAdapterData().clear();
        if (isAddPhotoButtonEnabled && !(!photoLinks.isEmpty() && photoLinks.get(0).isFake())) {
            getAdapterData().add(0, Photo.createFakePhoto());
        }
        addPhotos(photoLinks, needMore, false, notifyAll);
    }


    public void addPhotos(Photos photoLinks, boolean needMore, boolean isReversed, boolean notifyAll) {
        Photos photos = getAdapterData();
        int pos = photos.size();
        if (pos > 1 && photos.get(pos - 1).isFake()) {
            photos.remove(pos - 1);
        }
        for (Photo photo : photoLinks) {
            if (isReversed) {
                addFirst(photo);
            } else {
                photos.add(photo);
            }
        }
        if (mFooterView != null && needMore) {
            photos.add(Photo.createFakePhoto());
        }
        mNeedLoadNewItems = needMore;
        if (notifyAll) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(pos, photos.size());
        }
    }

    public void removePhoto(Photo photo, int position) {
        if (null != getAdapterData()) {
            getAdapterData().remove(photo);
            notifyItemRemoved(position);
        }
    }

    public Photos getPhotos() {
        Photos photoLinks = (Photos) getAdapterData().clone();
        //Убираем первую фейковую фотографию
        if (photoLinks != null && photoLinks.size() > 0 && photoLinks.get(0).isFake()) {
            photoLinks.remove(0);
        }
        //Убираем последнюю фейковую вьюху для футера
        if (mFooterView != null && photoLinks != null && photoLinks.size() > 0
                && photoLinks.get(photoLinks.size() - 1).isFake()) {
            photoLinks.remove(photoLinks.size() - 1);
        }
        return photoLinks;
    }

    public void addFirst(Photo photo) {
        Photos photos = getAdapterData();
        if (photos.size() > 1 && photos.get(photos.size() - 1).getId() == 0) {
            photos.add(2, photo);
        } else {
            photos.add(1, photo);
        }
        notifyDataSetChanged();
    }

    public void updateData(Photos photos, int photosCount, boolean notifyAll) {
        if (isNeedUpdate(photos) && photos != null) {
            setData((Photos) photos.clone(), photos.size() != photosCount, true, notifyAll);
        }
    }

    private boolean isNeedUpdate(Photos photos) {
        return !(getPhotos().equals(photos));
    }


    protected boolean isAddPhotoButtonEnabled() {
        return false;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private T mBinding;
        @SuppressWarnings("unused")
        private ViewDataBinding mFooterOrHeaderBinding;
        private View mHeaderOrFooterView;
        private boolean mIsBindingView;
        private boolean isLongClick;

        public ItemViewHolder(View view, boolean isBindingView) {
            super(view);
            mIsBindingView = isBindingView;
            if (mIsBindingView) {
                mBinding = DataBindingUtil.bind(view);
            }
            mHeaderOrFooterView = view;
            if (mRecyclerViewItemClickListener != null && mBinding != null) {
                mBinding.setVariable(BR.clickListener, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isLongClick) {
                            isLongClick = false;
                            return;
                        }
                        if (mRecyclerViewItemClickListener == null) {
                            return;
                        }
                        int pos = mRecyclerView.getLayoutManager().getPosition(v);
                        mRecyclerViewItemClickListener.itemClick(v, pos, getAdapterData().get(pos));
                    }
                });
                mBinding.setVariable(BR.longClickListener, new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        isLongClick = true;
                        if (mRecyclerViewItemLongClickListener == null) {
                            return true;
                        }
                        int pos = mRecyclerView.getLayoutManager().getPosition(v);
                        mRecyclerViewItemLongClickListener.itemLongClick(v, pos, getAdapterData().get(pos));
                        return false;
                    }
                });
            }
        }

        @Nullable
        public T getBinding() {
            return mBinding;
        }

        @SuppressWarnings("unused")
        @NonNull
        public View getHolderView() {
            return mHeaderOrFooterView;
        }

        @Nullable
        public ViewDataBinding getFooterOrHeaderBinding() {
            return mFooterOrHeaderBinding;
        }
    }

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
    public interface OnRecyclerViewItemClickListener {

        /**
         * @param view         - вьюха на которую был произведен клик
         * @param itemPosition - позиция
         * @param photo        - фоточка
         */
        void itemClick(View view, int itemPosition, Photo photo);

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
    public interface OnRecyclerViewItemLongClickListener {

        /**
         * @param view         - вьюха на которую был произведен клик
         * @param itemPosition - позиция
         * @param photo        - фоточка
         */
        void itemLongClick(View view, int itemPosition, Photo photo);

    }
}
