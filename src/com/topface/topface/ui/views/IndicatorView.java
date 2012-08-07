package com.topface.topface.ui.views;

import java.util.LinkedHashMap;
import com.topface.topface.R;
import com.topface.topface.utils.Debug;
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
    private LinkedHashMap<Integer, Integer> mPoints;
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
        mPoints = new LinkedHashMap<Integer, Integer>(4);
        mIndicator = BitmapFactory.decodeResource(getResources(), R.drawable.user_sign);
        mDivider = BitmapFactory.decodeResource(getResources(), R.drawable.user_divider);
        mDividerY = mIndicator.getHeight() - mDivider.getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mIndicator.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Debug.log(this, "mCurrentId:"+mCurrentId);

        canvas.drawBitmap(mDivider, 0, mDividerY, mPaint);
        if (mCurrentId > 0 && mPoints.size() > 0) {
            int x = mPoints.get(mCurrentId);
            canvas.drawBitmap(mIndicator, x, 0, mPaint);
        }

    }
    
    public void setButtonMeasure(int id, int measure) {
        mMeasures.put(id, measure);
    }

    public void reCompute() {
        int sum = 0;
        mPoints.clear();
        int counter = 0;
        if (mMeasures.size() > 0) {
            for (Integer width : mMeasures.values()) {
                int point = (width-mIndicator.getWidth())/2;
                mPoints.put(counter++, sum+point);
                sum += width;
            }
        }
        /*
        if (mMeasures.size() > 0) {
            for (Entry<Integer, Integer> measure : mMeasures.entrySet()) {
                int point = (measure.getValue()-mIndicator.getWidth())/2;
                mPoints.put(measure.getKey(), sum+point);
                sum += measure.getValue();
            }
        }
        */
    }
    
    public void setIndicator(int id) {
        mCurrentId = id;
        invalidate();
    }


}
