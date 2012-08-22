package com.topface.topface.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ToolsBarSwapController extends LinearLayout {

    public ToolsBarSwapController(Context context,AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        int count = getChildCount();
//        
//        for(int i=0; i < count;i++) {
//            getChildAt(i).measure(0, 0);
//        }
        if(count > 1)
          setPadding(getPaddingLeft(), -getChildAt(0).getMeasuredHeight(), getPaddingRight(), getPaddingBottom());
    }
    
    

}
