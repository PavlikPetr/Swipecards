package com.topface.topface.ui.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.*;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.topface.topface.R;
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
        mPreloadManager = new PreloadManager(getWidth(), getHeight(), (Activity) getContext());
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
            mOnClickListener.onClick(ImageSwitcher.this);
            return false;
        }
    };

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        final OnPageChangeListener finalListener = listener;
        super.setOnPageChangeListener(new OnPageChangeListener() {
            private int mLastSelected = 0;

            @Override
            public void onPageScrolled(int i, float v, int i1) {
                //При WiFi подключении это не нужно, т.к. фотографию мы уже прелоадим заранее, но нужно при 3G
                //Если показано больше 10% следующей фотографии, то начинаем ее грузить
                if (v > 0.1 && v < 0.6) {
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
    class ImageSwitcherAdapter extends PagerAdapter {
        private boolean isFirstInstantiate = true;
        private Photos mPhotoLinks;
        private SparseArray<Boolean> mLoadedPhotos;

        /**
         * Создает слушателя загрузки фотки, через замыкание передавая позицию слушаемой фотографии
         *
         * @param position изображение, загрузку которого мы слушаем
         * @return listener
         */
        private ImageLoadingListener getListener(final int position) {
            return new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(Bitmap loadedImage) {
                    int currentItem = getCurrentItem();
                    if (currentItem + 1 == position || currentItem - 1 == position) {
                        Debug.log("IS: onLoadingComplete " + position);
                        setPhotoToPosition(position, true);
                    }
                }
            };
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
                imageView.setPhoto(mPhotoLinks.get(position), mUpdatedHandler, progressBar);
                imageView.setTag(R.string.photo_is_set_tag, true);
            }
            mLoadedPhotos.put(position, true);
        }

        public void setData(Photos photos) {
            mPhotoLinks = photos;
            mLoadedPhotos = new SparseArray<Boolean>();
            mPrev = -1;
            mNext = 0;
            notifyDataSetChanged();
        }

        public void setIsFirstInstantiate(boolean value) {
            isFirstInstantiate = value;
        }
    }
}