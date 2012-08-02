package com.topface.topface.ui.views;

import java.util.Collection;
import java.util.LinkedHashMap;
import com.topface.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class IndicatorView extends View {
    private int mCurrentId;
    private LinkedHashMap<Integer, Integer> mMeasures;
    private Bitmap mIndicator;
    private Bitmap mDivider;
    private Paint mPaint;
    
    private int mDividerY;
    
    private static final int DEFAULT = -1;
    
    public IndicatorView(Context context ,AttributeSet attrs) {
       this(context, attrs, 0);
    }
    
    public IndicatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mCurrentId = DEFAULT;
        mPaint = new Paint();
        mMeasures = new LinkedHashMap<Integer, Integer>(4);
        mIndicator = BitmapFactory.decodeResource(getResources(), R.drawable.user_sign);
        mDivider = BitmapFactory.decodeResource(getResources(), R.drawable.user_divider);
        
        mDividerY =  mIndicator.getHeight() - mDivider.getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mIndicator.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mCurrentId < 0) {
            canvas.drawBitmap(mIndicator, mMeasures.get(mCurrentId), 0, mPaint);
        }
        canvas.drawBitmap(mDivider, 0, mDividerY, mPaint);
    }
    
    public void setButtonMeasure(int id, int measure) {
        int sum = 0;
        
        Object[] measures = mMeasures.values().toArray();
               
        if (measures.length > 0) {
            for (Object width : measures) {
                sum += (Integer)width;
            }
        }
        
        int value = (measure - mIndicator.getWidth())/2;
        mMeasures.put(id, sum + value); 
    }
    
    public void setIndicator(int id) {
        mCurrentId = id;
        invalidate();
    }


}
