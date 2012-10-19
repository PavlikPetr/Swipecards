package com.topface.topface.utils;


import android.content.Context;
import android.util.DisplayMetrics;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ListView;

public class SwipeGestureListener extends SimpleOnGestureListener {	
	private int REL_SWIPE_MIN_DISTANCE = 80; 
    private int REL_SWIPE_MAX_OFF_PATH = 200;
    private int REL_SWIPE_THRESHOLD_VELOCITY = 150;    
    
    private SwipeListener mSwipeListener;
    private ListView mListView;
    
    public interface SwipeListener {
    	void onSwipeR2L(int position);
    	void onSwipeL2R(int position);
    	void onTap(int position);
    }
    
    public SwipeGestureListener(Context context, ListView listView, SwipeListener listener) {
    	DisplayMetrics dm = context.getResources().getDisplayMetrics();
        REL_SWIPE_MIN_DISTANCE = (int)(80.0f * dm.densityDpi / 160.0f + 0.5); 
        REL_SWIPE_MAX_OFF_PATH = (int)(250.0f * dm.densityDpi / 160.0f + 0.5);
        REL_SWIPE_THRESHOLD_VELOCITY = (int)(200.0f * dm.densityDpi / 160.0f + 0.5);
        mSwipeListener = listener;
        mListView = listView;
	}
    
    @Override 
    public boolean onSingleTapUp(MotionEvent e) {    	
        int position = mListView.pointToPosition((int)e.getX(), (int)e.getY());
    	mSwipeListener.onTap(position);
        return false;
    }

    @Override 
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { 
        if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH) 
            return false; 
        if(e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE && 
            Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) { 
            if(mSwipeListener != null) {
            	int position = mListView.pointToPosition((int)e1.getX(), (int)e1.getY());
            	if (position > 0) 
            		mSwipeListener.onSwipeR2L(position-1); //TODO магия
            }
        }  else if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE && 
            Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) { 
        	if(mSwipeListener != null) {
        		int position = mListView.pointToPosition((int)e1.getX(), (int)e1.getY());
        		if (position > 0)  
        			mSwipeListener.onSwipeL2R(position-1); //TODO магия
        	}
        } 
        return false; 
    }
}
