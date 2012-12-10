package com.topface.topface.ui.views;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.*;
import com.topface.topface.R;
import com.topface.topface.data.Photos;

public class ImageSwitcher extends ViewPager {

    private GestureDetector mGestureDetector;
    private ImageSwitcherAdapter mImageSwitcherAdapter;
    private Photos mPhotoLinks;
    private OnClickListener mOnClickListener;
    private Handler mUpdatedHandler;
    private static final String VIEW_TAG = "view_container";

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
    }

    public void setData(Photos photoLinks) {
        mPhotoLinks = photoLinks;
        mImageSwitcherAdapter.setIsFirstInstantiate(true);
        this.setAdapter(mImageSwitcherAdapter);
    }

    public void setPhoto(int position) {
        mImageSwitcherAdapter.setPhotoToPosition(position);
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
            public int mCurrentSelected;
            public int mNext;
            public int mPrev;

            @Override
            public void onPageScrolled(int i, float v, int i1) {
                //Если показано больше 10% следующей фотографии, то начинаем ее грузить
                if (v > 0.1) {
                    if (mCurrentSelected == i) {
                        int next;
                        next = i + 1;
                        //Проверяем, начали ли мы грузить следующую фотографию
                        if (mNext != next) {
                            mNext = next;
                            setPhoto(next);
                        }
                    } else {
                        //Проверяем, не начали ли мы грузить предыдущую фотографию
                        if (mPrev != i) {
                            mPrev = i;
                            setPhoto(i);
                        }
                    }
                }
                finalListener.onPageScrolled(i,v,i1);
            }

            @Override
            public void onPageSelected(int i) {
                //setPhoto(i);
                mCurrentSelected = i;
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

        @Override
        public int getCount() {
            return mPhotoLinks == null ? 0 : mPhotoLinks.size();
        }

        public Object instantiateItem(ViewGroup pager, int position) {
            LayoutInflater inflater = (LayoutInflater) pager.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_album_gallery, null);
            view.setTag(VIEW_TAG+Integer.toString(position));
            ImageViewRemote imageView = (ImageViewRemote) view.findViewById(R.id.ivPreView);
            if(isFirstInstantiate) {
                imageView.setPhoto(mPhotoLinks.get(position), mUpdatedHandler);
                isFirstInstantiate = false;
            } //else
//                mPreloadManager.preloadImage(mPhotoLinks.get(position).getSuitableLink(Photo.SIZE_960));
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

        public void setPhotoToPosition(int position) {
            if(!isFirstInstantiate) {
                View baseLayout = ImageSwitcher.this.findViewWithTag(VIEW_TAG+Integer.toString(position));
                ImageViewRemote imageView = (ImageViewRemote)baseLayout.findViewById(R.id.ivPreView);
                if(imageView.getBackground() == null) {
                    imageView.setPhoto(mPhotoLinks.get(position), mUpdatedHandler);
                }
                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
//                TopfaceNotificationManager.getInstance(ImageSwitcher.this.getContext()).showNotification("test","test", NavigationActivity.mThis,imageView.getDrawingCache());
            }
        }

        public void setIsFirstInstantiate(boolean value) {
            isFirstInstantiate = value;
        }
    }
}