package com.topface.topface.ui.views;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.*;
import com.topface.topface.R;
import com.topface.topface.data.Photos;
import com.topface.topface.utils.Debug;

public class ImageSwitcher extends ViewPager {

    private GestureDetector mGestureDetector;
    private ImageSwitcherAdapter mImageSwitcherAdapter;
    private Photos mPhotoLinks;
    private OnClickListener mOnClickListener;

    private Handler mUpdatedHandler;

    public ImageSwitcher(Context context) {
        this(context, null);
    }

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

        @Override
        public int getCount() {
            return mPhotoLinks == null ? 0 : mPhotoLinks.size();
        }

        public Object instantiateItem(ViewGroup pager, int position) {
            Debug.log("Page has been created");
            LayoutInflater inflater = (LayoutInflater) pager.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_album_gallery, null);
            ImageViewRemote imageView = (ImageViewRemote) view.findViewById(R.id.ivPreView);
            imageView.setPhoto(mPhotoLinks.get(position), mUpdatedHandler);
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
    }
}