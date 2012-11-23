package com.topface.topface.ui.views;


import com.topface.topface.R;
import com.topface.topface.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NoviceLayout extends RelativeLayout {

	private Context mContext;
	
	private ImageView mMask;
	private ImageView mBackground;	
	
	private OnClickListener mMaskListener;
	private OnClickListener mBackgroundListener;
	
	
	public NoviceLayout(Context context) {
		super(context);
		mContext = context;
	}

	public NoviceLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}		
	
	public void setLayoutRes(int res,OnClickListener maskListener) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		removeAllViews();
		
        inflater.inflate(res, this, true);
        setVisibility(View.VISIBLE);
        
        mMask = (ImageView) findViewById(R.id.ivMask);
        mBackground = (ImageView) findViewById(R.id.ivBackground);        
        TextView bubble = (TextView) findViewById(R.id.ivBubble);
        Typeface tf = Typeface.createFromAsset(mContext.getAssets(), "neucha.otf"); 
        bubble.setTypeface(tf);
                
        ViewTreeObserver vto = mMask.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				int[] point = new int[2];
				mMask.getLocationOnScreen(point);				
				mBackground.setImageBitmap(getMaskedBackgroundBitmap(mMask,point));
				ViewTreeObserver obs = mMask.getViewTreeObserver();
				invalidate();
				obs.removeGlobalOnLayoutListener(this);
			}
		});
        
        mMask.setVisibility(View.INVISIBLE);
        mMaskListener = maskListener;     
        mBackgroundListener = null;
	}	
	
	public void setLayoutRes(int res,OnClickListener maskListener, OnClickListener backgroundListener) {
		setLayoutRes(res,maskListener);
		mBackgroundListener = backgroundListener;
	}
	
	private Bitmap getMaskedBackgroundBitmap(ImageView maskView,int[] point) {		
		Bitmap mask = ((BitmapDrawable)mMask.getDrawable()).getBitmap();		
				
		Point size = Utils.getSrceenSize(mContext);		
		int width = size.x;
		int height = size.y;
		
        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);        

        Canvas canvas = new Canvas(output);
        
        canvas.drawARGB(178, 0, 0, 0);        

        Paint paint = new Paint();        
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        canvas.drawBitmap(mask, point[0], point[1], paint);
        
		return output;
	}	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (mBackgroundListener != null) mBackgroundListener.onClick(this);
			
			if (isPointInsideView(event.getX(),event.getY(),mMask) && mMaskListener != null) {
				mMaskListener.onClick(mMask);			
				this.setVisibility(View.GONE);
				return true;
			} else {
				this.setVisibility(View.GONE);
				return true;
			}
		}
		return true;
	}
	
	private boolean isPointInsideView(float x, float y, View view){
	    int location[] = new int[2];
	    view.getLocationOnScreen(location);
	    int viewX = location[0];
	    int viewY = location[1];

	    //point is inside view bounds
	    if(( x > viewX && x < (viewX + view.getWidth())) &&
	            ( y > viewY && y < (viewY + view.getHeight()))){
	        return true;
	    } else {
	        return false;
	    }
	}
}
