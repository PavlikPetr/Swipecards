package com.topface.topface.ui.views;

import java.util.HashMap;
import com.topface.topface.R;
import com.topface.topface.utils.PhotoLinksResolver;
import com.topface.topface.utils.http.Http;
import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class ImageSwitcher extends ViewPager  {

    private GestureDetector mGestureDetector;
    private ImageSwitcherAdapter mImageSwitcherAdapter;
    private SparseArray<HashMap<String, String>> mPhotoLinks;
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
        mGestureDetector = new GestureDetector(mOnGestureListener);
        mImageSwitcherAdapter = new ImageSwitcherAdapter();        
        setAdapter(mImageSwitcherAdapter);
        setOnTouchListener(mOnTouchListener);
        setPageMargin(20);
    }
    
    public void setData(SparseArray<HashMap<String, String>> photoLinks) {
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
            return offset < range - 0;
        }
    }
    
    private GestureDetector.SimpleOnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mOnClickListener.onClick(ImageSwitcher.this);
            return false;
        }
    };
    
    private View.OnTouchListener mOnTouchListener =  new View.OnTouchListener() {
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
            if (mPhotoLinks == null)
                return 0;
            return mPhotoLinks.size();
        }
        
        public Object instantiateItem(View pager, int position) {
            LayoutInflater inflater = (LayoutInflater)pager.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_album_gallery, null);
            ImageView imageView = (ImageView)view.findViewById(R.id.ivPreView);
            String url = mPhotoLinks.get(mPhotoLinks.keyAt(position)).get(PhotoLinksResolver.SIZE_ORIGIN);
            if (mUpdatedHandler != null) {
            	Http.imageLoader(url, imageView, mUpdatedHandler);
            } else {
            	Http.imageLoader(url, imageView);
            }
            ((ViewPager)pager).addView(view);   
            return view;
        }
        
        @Override
        public void destroyItem(View view, int position, Object object) {
            ((ViewPager)view).removeView((View)object);
        }
        
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (View)object;
        }
    }
}