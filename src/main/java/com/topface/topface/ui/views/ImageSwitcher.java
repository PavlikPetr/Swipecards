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

import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.PreloadManager;

public class ImageSwitcher extends ViewPager {

    private GestureDetector mGestureDetector;
    private ImageSwitcherAdapter mImageSwitcherAdapter;
    private OnClickListener mOnClickListener;
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
        mImageSwitcherAdapter = new ImageSwitcherAdapter();
        setAdapter(mImageSwitcherAdapter);
        setOnTouchListener(mOnTouchListener);
        setPageMargin(40);
        mPreloadManager = new PreloadManager(getWidth(), getHeight());
    }

    public void setData(Photos photoLinks) {
        mImageSwitcherAdapter.setData(photoLinks);
        mImageSwitcherAdapter.setIsFirstInstantiate(true);
        this.setAdapter(mImageSwitcherAdapter);
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

            @Override
            public void onPageScrolled(int i, float v, int i1) {
                //При WiFi подключении это не нужно, т.к. фотографию мы уже прелоадим заранее, но нужно при 3G
                //Если показано больше 10% следующей фотографии, то начинаем ее грузить
                if (v > 0.1 && v < 0.6) {
                    Debug.log("IS: page_scroll " + i);
                    if (getCurrentItem() == i) {
                        int next;
                        next = i + 1;
                        //Проверяем, начали ли мы грузить следующую фотографию
                        if (mNext != next) {
                            mNext = next;
                            Debug.log("IS next: " + mNext + "_" + v);
                            mImageSwitcherAdapter.setPhotoToPosition(mNext, false);
                        }
                    } else {
                        //Проверяем, не начали ли мы грузить предыдущую фотографию
                        if (mPrev != i) {
                            mPrev = i;
                            Debug.log("IS prev: " + mPrev + "_" + v);
                            mImageSwitcherAdapter.setPhotoToPosition(mPrev, false);
                        }
                    }
                }
                finalListener.onPageScrolled(i, v, i1);
            }

            @Override
            public void onPageSelected(int i) {
                setSelectedPosition(i);
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
        private Photos mPhotoLinks;
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
                    Debug.log("IS: complete_but_not_set " + position + " " + currentItem);
                    if (currentItem + 1 == position || currentItem - 1 == position) {
                        Debug.log("IS: onLoadingComplete " + position);
                        setPhotoToPosition(position, true);
                    }
                }

            };
        }

        public Photos getData() {
            return mPhotoLinks;
        }

        @Override
        public int getCount() {
            return mPhotoLinks == null ? 0 : mPhotoLinks.size();
        }

        public Object instantiateItem(ViewGroup pager, int position) {
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
            Boolean isLoadedPhoto = mLoadedPhotos.get(position, false);
            //Если это первая фото в списке или фотография уже загружена, то устанавливаем фото сразу
            if (isFirstInstantiate || isLoadedPhoto) {
                setPhotoToView(position, view, imageView);
                isFirstInstantiate = false;

            }

            //Если фото еще не загружено, то пытаемся его загрузить через прелоадер
            if (!isLoadedPhoto && mPreloadManager.preloadPhoto(mPhotoLinks, position, getListener(position))) {
                //Добавляем его в список загруженых
                Debug.log("IS: preloadPhoto " + position);
                mLoadedPhotos.put(position, true);
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
            if (!ifLoaded || mLoadedPhotos.get(position, false)) {
                View baseLayout = ImageSwitcher.this.findViewWithTag(VIEW_TAG + Integer.toString(position));
                //Этот метод может вызываться до того, как создана страница для этой фотографии
                if (baseLayout != null) {
                    Debug.log("IS: trySetPhoto " + position);
                    ImageViewRemote imageView = (ImageViewRemote) baseLayout.findViewById(R.id.ivPreView);
                    setPhotoToView(position, baseLayout, imageView);
                }
            }
        }

        private void setPhotoToView(int position, View baseLayout, ImageViewRemote imageView) {
            Object tag = imageView.getTag(R.string.photo_is_set_tag);
            //Проверяем, не установленно ли уже изображение в ImageView
            if (tag == null || !((Boolean) tag)) {
                Debug.log("IS: setPhoto " + position);
                View progressBar = baseLayout.findViewById(R.id.pgrsAlbum);
                progressBar.setVisibility(View.VISIBLE);
                Photo photo = mPhotoLinks.get(position);
                imageView.setPhoto(photo, mUpdatedHandler, progressBar);
                imageView.setTag(R.string.photo_is_set_tag, !photo.isFake());
            }
            mLoadedPhotos.put(position, true);
        }


        //Опасная копипаста
        private boolean isPhotoSet(int position) {
            View baseLayout = ImageSwitcher.this.findViewWithTag(VIEW_TAG + Integer.toString(position));
            //Этот метод может вызываться до того, как создана страница для этой фотографии
            if (baseLayout != null) {
                Debug.log("IS: trySetPhoto " + position);
                ImageViewRemote imageView = (ImageViewRemote) baseLayout.findViewById(R.id.ivPreView);
                Object tag = imageView.getTag(R.string.photo_is_set_tag);
                //Проверяем, не установленно ли уже изображение в ImageView
                if (tag != null && ((Boolean) tag)) {
                    return true;
                }
            }
            return false;
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
            setPhotoToPosition(getSelectedPosition(),true);
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
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        } catch (IllegalStateException e) {
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

    public int getPreviousSelectedPosition() {
        return mPreviousPhotoPosition;
    }
}