package com.topface.topface.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.utils.AnimationUtils;
import com.topface.topface.utils.PreloadManager;

public class ImageSwitcher extends ViewPager {

    private GestureDetector mGestureDetector;
    private ImageSwitcherAdapter mImageSwitcherAdapter;
    `
    private Handler mUpdatedHandler;
    private static final String VIEW_TAG = "view_container";
    private PreloadManager mPreloadManager;
    private int mCurrentPhotoPosition;
    private int mPreviousPhotoPosition;
    private int mPrev;
    private int mNext;

    public ImageSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mGestureDetector = new GestureDetector(getContext(), mOnGestureListener);
        mImageSwitcherAdapter = createImageSwitcherAdapter();
        setAdapter(mImageSwitcherAdapter);
        setOnTouchListener(mOnTouchListener);
        setPageMargin(40);
        mPreloadManager = new PreloadManager(getWidth(), getHeight());
    }

    protected ImageSwitcherAdapter createImageSwitcherAdapter() {
        return new ImageSwitcherAdapter();
    }

    public void setData(Photos photoLinks) {
        ImageSwitcherAdapter adapter = mImageSwitcherAdapter;
        adapter.setData(photoLinks);
        adapter.setIsFirstInstantiate(true);
        this.setAdapter(adapter);
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setUpdateHandler(Handler handler) {
        mUpdatedHandler = handler;
    }

    public boolean canScrollHorizontally(int direction) {
        final int offset = computeHorizontalScrollOffset();
        final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 1;
        }
    }

    private GestureDetector.SimpleOnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mOnClickListener != null) mOnClickListener.onClick(ImageSwitcher.this);
            return false;
        }
    };

    @Override
    public void setOnPageChangeListener(final OnPageChangeListener finalListener) {
        super.setOnPageChangeListener(new OnPageChangeListener() {

            int oldPosition = getCurrentItem();

            @Override
            public void onPageScrolled(int i, float v, int i1) {
                //При WiFi подключении это не нужно, т.к. фотографию мы уже прелоадим заранее, но нужно при 3G
                //Если показано больше 10% следующей фотографии, то начинаем ее грузить
                if (v > 0.1 && v < 0.6) {
                    if (i > oldPosition) {
                        int next;
                        next = i + 1;
                        //Проверяем, начали ли мы грузить следующую фотографию
                        if (mNext != next) {
                            mNext = next;
                            oldPosition = mNext;
                            mImageSwitcherAdapter.setPhotoToPosition(mNext, false);
                        }
                    } else if (i < oldPosition) {
                        //Проверяем, не начали ли мы грузить предыдущую фотографию
                        if (mPrev != i) {
                            mPrev = i;
                            oldPosition = mPrev;
                            mImageSwitcherAdapter.setPhotoToPosition(mPrev, false);
                        }
                    }
                }
                finalListener.onPageScrolled(i, v, i1);
            }

            @Override
            public void onPageSelected(int i) {
                setSelectedPosition(i);
                mImageSwitcherAdapter.setPhotoToPosition(i, false);
                finalListener.onPageSelected(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                finalListener.onPageScrollStateChanged(i);
            }
        });
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mGestureDetector.onTouchEvent(event);
            return false;
        }
    };

    /*
    *  class ImageSwitcherAdapter
    */
    public class ImageSwitcherAdapter extends PagerAdapter {
        private boolean isFirstInstantiate = true;
        protected Photos mPhotoLinks;
        private SparseBooleanArray mLoadedPhotos;

        /**
         * Создает слушателя загрузки фотки, через замыкание передавая позицию слушаемой фотографии
         *
         * @param position изображение, загрузку которого мы слушаем
         * @return listener
         */
        private ImageLoadingListener getListener(final int position) {
            return new SimpleImageLoadingListener() {

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);

                    int currentItem = getCurrentItem();
                    if (currentItem + 1 == position || currentItem - 1 == position) {
                        setPhotoToPosition(position, true);
                    }
                }
            };
        }

        public Photos getData() {
            return mPhotoLinks;
        }

        public int getRealPosition(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return mPhotoLinks == null ? 0 : mPhotoLinks.size();
        }

        public Object instantiateItem(ViewGroup pager, int position) {
            int realPosition = getRealPosition(position);
            LayoutInflater inflater = (LayoutInflater) pager.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_album_gallery, null);
            view.setTag(VIEW_TAG + Integer.toString(position));
            ImageViewRemote imageView = (ImageViewRemote) view.findViewById(R.id.ivPreView);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnClickListener != null) mOnClickListener.onClick(ImageSwitcher.this);
                }
            });
            //Первую фотографию грузим сразу, или если фотографию уже загружена, то сразу показываем ее
            Boolean isLoadedPhoto = mLoadedPhotos.get(realPosition, false);
            //Если это первая фото в списке или фотография уже загружена, то устанавливаем фото сразу
            if (isFirstInstantiate || isLoadedPhoto) {
                setPhotoToView(position, view, imageView);
                isFirstInstantiate = false;
            }

            //Если фото еще не загружено, то пытаемся его загрузить через прелоадер
            if (!isLoadedPhoto && mPreloadManager.preloadPhoto(mPhotoLinks, realPosition, getListener(position))) {
                //Добавляем его в список загруженых
                mLoadedPhotos.put(realPosition, true);
            }

            pager.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup view, int position, Object object) {
            view.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        /**
         * Устанавливает фотографию в ImageView
         *
         * @param position страница на которой находится ImageView
         * @param ifLoaded если true, то установить только если фотография уже загружена
         */
        public void setPhotoToPosition(int position, boolean ifLoaded) {
            int realPosition = getRealPosition(position);
            if (!ifLoaded || mLoadedPhotos.get(realPosition, false)) {
                View baseLayout = ImageSwitcher.this.findViewWithTag(VIEW_TAG + Integer.toString(position));
                //Этот метод может вызываться до того, как создана страница для этой фотографии
                if (baseLayout != null) {
                    ImageViewRemote imageView = (ImageViewRemote) baseLayout.findViewById(R.id.ivPreView);
                    setPhotoToView(position, baseLayout, imageView);
                }
            }
        }

        private void setPhotoToView(int position, View baseLayout, ImageViewRemote imageView) {
            int realPosition = getRealPosition(position);
            Object tag = imageView.getTag(R.string.photo_is_set_tag);
            //Проверяем, не установленно ли уже изображение в ImageView
            if (tag == null || !((Boolean) tag)) {
                View progressBar = baseLayout.findViewById(R.id.pgrsAlbum);
                progressBar.setVisibility(View.VISIBLE);
                AnimationUtils.createProgressBarAnimator(progressBar).start();
                Photo photo = mPhotoLinks.get(realPosition);
                imageView.setPhoto(photo, mUpdatedHandler, progressBar);
                imageView.setTag(R.string.photo_is_set_tag, !photo.isFake());
            }
            mLoadedPhotos.put(realPosition, true);
        }


        public void setData(Photos photos) {
            mPhotoLinks = photos;
            mLoadedPhotos = new SparseBooleanArray();
            mPrev = -1;
            mNext = 0;
            notifyDataSetChanged();
        }

        public void setIsFirstInstantiate(boolean value) {
            isFirstInstantiate = value;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            setPhotoToPosition(getSelectedPosition(), true);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            //Мы отлавливаем эту ошибку из-за бага в support library, который кидает такие ошибки:
            //IllegalArgumentException: pointerIndex out of range
            //Debug.error(e);
            return false;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IndexOutOfBoundsException | IllegalStateException | IllegalArgumentException | NullPointerException e) {
            return false;
        }
    }

    public void notifyDataSetChanged() {
        mImageSwitcherAdapter.notifyDataSetChanged();
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
        mPreviousPhotoPosition = item;
        mCurrentPhotoPosition = item;
    }

    private void setSelectedPosition(int i) {
        mPreviousPhotoPosition = mCurrentPhotoPosition;
        mCurrentPhotoPosition = i;
    }

    public int getSelectedPosition() {
        return mCurrentPhotoPosition;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getPreviousSelectedPosition() {
        return mPreviousPhotoPosition;
    }
}
