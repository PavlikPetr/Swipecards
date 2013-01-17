package com.topface.topface.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import com.topface.topface.R;

import java.util.concurrent.TimeUnit;

public class AnimatedView extends View {

    SpriteTile st;

    GameLoop gameloop;
    private class GameLoop extends Thread
    {
        private volatile boolean running=true;
        public void run()
        {
            while(running)
            {
                try{

                    TimeUnit.MILLISECONDS.sleep(1);
                    postInvalidate();
                    pause();

                }
                catch(InterruptedException ex)
                {
                    running=false;
                }

            }

        }
        public void pause()
        {
            running=false;
        }
        public void start()
        {
            running=true;
            run();
        }
        public void safeStop()
        {
            running=false;
            interrupt();
        }

    }
    public void unload()
    {
        gameloop.safeStop();

    }

    public AnimatedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
// TODO Auto-generated constructor stub
        init(context);

    }
    public AnimatedView(Context context, AttributeSet attrs) {
        super(context, attrs);
// TODO Auto-generated constructor stub
        init(context);
    }
    public AnimatedView(Context context) {
        super(context);
// TODO Auto-generated constructor stub
        init(context);
    }

    private void init(Context context)
    {
        st = new SpriteTile(R.drawable.sprite8, R.anim.image_anim, context);
        gameloop = new GameLoop();
        gameloop.run();

    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
// TODO Auto-generated method stub
//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        System.out.println("Width " + widthMeasureSpec);
        setMeasuredDimension(150, 150);
    }

    @Override
    protected void onDraw(Canvas canvas) {
// TODO Auto-generated method stub
//super.onDraw(canvas);

        st.setXpos(0);
        st.setYpos(0);
        st.draw(canvas);
        gameloop.start();

    }
}
