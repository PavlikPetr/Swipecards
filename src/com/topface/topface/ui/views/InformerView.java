package com.topface.topface.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.Button;
import com.topface.topface.R;
import com.topface.topface.Recycle;
import com.topface.topface.utils.CacheProfile;

public class InformerView extends ViewGroup {


    // class Informer


    class Informer { // информер нажатой звезды
        public float _x; // х
        public float _y; // у
        public int _widht; // ширина
        public int _temp; // temp
        public int _height; // высота
        public int _index; // цифра рейтинга
        public int _bottom; // нижняя граница предка 
        public boolean _visible; // рисовать или нет

        // Ctor
        public Informer(int widht, int height) {
            _widht = widht;
            _height = height;
            _temp = height / 2;
        }

        public void draw(Canvas canvas) {
            if (!_visible)
                return;

            _y -= _temp; // ПЕРЕПИСАТЬ ОТРИСОВКУ 

            if (_y < 0)
                _y = 0;
            else if (_y > _bottom - _height)
                _y = _bottom - _height; // иметь просчитанным !!!

            int z = (Recycle.s_DatingInformer.getHeight() - Recycle.s_RateHigh.getHeight()) / 2;

            // background
            canvas.drawBitmap(Recycle.s_DatingInformer, _x, _y, informerPaint);

            // star
            if (_index == CacheProfile.average_rate)
                canvas.drawBitmap(Recycle.s_RateAverage, _x + z * 2, _y + z, informerPaint);
            else if (_index > CacheProfile.average_rate && _index < 10)
                canvas.drawBitmap(Recycle.s_RateHigh, _x + z * 2, _y + z, informerPaint);
            else if (_index < CacheProfile.average_rate)
                canvas.drawBitmap(Recycle.s_RateLow, _x + z * 2, _y + z, informerPaint);
            else if (_index == 10)
                canvas.drawBitmap(Recycle.s_RateTop, _x + z * 2, _y + z, informerPaint);

            // rating
            if (_index > CacheProfile.average_rate)
                canvas.drawText("" + _index, (float) (_x + Recycle.s_RateHigh.getWidth() / 2) + z * 2, (float) (_y + z + Recycle.s_RateHigh.getHeight() / 1.6), informerTitlePaintHight);
            else
                canvas.drawText("" + _index, (float) (_x + Recycle.s_RateHigh.getWidth() / 2) + z * 2, (float) (_y + z + Recycle.s_RateHigh.getHeight() / 1.6), informerTitlePaintLow);

            // coins
            if (_index == 10) {
                canvas.drawText("1", (float) (_x + Recycle.s_DatingInformer.getWidth() / 1.8), (float) (_y + z + Recycle.s_RateHigh.getHeight() / 1.6), informerTitlePaintHight);
                // иметь просчитанным !!!
                int n = Recycle.s_DatingInformer.getWidth() - Recycle.s_Money.getWidth() - z * 2;
                z = (Recycle.s_DatingInformer.getHeight() - Recycle.s_Money.getHeight()) / 2;
                // money
                float a = _x + n;
                float b = _y + z;
                canvas.drawBitmap(Recycle.s_Money, a, b, informerPaint);
            }
        }
    }


    // Data
    private Button mProfileBtn; // кнопка на профиль
    private Button mChatBtn; // кнопка в чат
    private Informer mInformer; // информер текущей звезды
    // Constants
    private Paint informerTitlePaintHight = new Paint();
    private Paint informerTitlePaintLow = new Paint();
    private Paint informerPaint = new Paint();


    public InformerView(Context context) {
        super(context);

        setBackgroundColor(Color.TRANSPARENT);

        informerTitlePaintHight.setColor(Color.WHITE);
        informerTitlePaintHight.setTextSize(getResources().getDimension(R.dimen.dating_star_number));
        informerTitlePaintHight.setTypeface(Typeface.DEFAULT_BOLD);
        informerTitlePaintHight.setAntiAlias(true);
        informerTitlePaintHight.setTextAlign(Paint.Align.CENTER);

        informerTitlePaintLow.setColor(Color.BLACK);
        informerTitlePaintLow.setTextSize(getResources().getDimension(R.dimen.dating_star_number));
        informerTitlePaintLow.setTypeface(Typeface.DEFAULT_BOLD);
        informerTitlePaintLow.setAntiAlias(true);
        informerTitlePaintLow.setTextAlign(Paint.Align.CENTER);

        // Chat btn
        mChatBtn = new Button(context);
        mChatBtn.setId(R.id.btnDatingChat);
        mChatBtn.setBackgroundResource(R.drawable.dating_chat_selector);
        addView(mChatBtn);

        // Profile btn
        mProfileBtn = new Button(context);
        mProfileBtn.setId(R.id.btnDatingProfile);
        mProfileBtn.setBackgroundResource(R.drawable.dating_man_selector);
        addView(mProfileBtn);

        // Informer popup
        mInformer = new Informer(Recycle.s_DatingInformer.getWidth(), Recycle.s_DatingInformer.getHeight());
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        mInformer.draw(canvas);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (Recycle.s_DatingInformer.getWidth() * 1.1); // вычисление своей ширины
        int height = MeasureSpec.getSize(heightMeasureSpec); // вычисляем предоставленную нам высоту для отрисовки

        // передаем свои размеры предкам
        mChatBtn.measure(width, height);
        mProfileBtn.measure(width, height);

        setMeasuredDimension(width, height);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        float offset_from_stars = 1.3f;

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        mInformer._bottom = height;

        int x = (int) (width - mProfileBtn.getMeasuredWidth() * offset_from_stars);
        int y = (int) (height - mProfileBtn.getMeasuredHeight() * 2.5);
        mChatBtn.layout(x, y, x + mChatBtn.getMeasuredWidth(), y + mChatBtn.getMeasuredHeight());

        y = (int) (y + mChatBtn.getMeasuredHeight() * offset_from_stars);
        mProfileBtn.layout(x, y, x + mProfileBtn.getMeasuredWidth(), y + mProfileBtn.getMeasuredHeight());
    }


    public void setVisible(boolean visible) {
        mInformer._visible = visible;
    }


    public void setData(float y, int index) {
        mInformer._y = y;
        mInformer._index = index;
    }


    public void setBlock(boolean block) {
        mChatBtn.setEnabled(block);
        mProfileBtn.setEnabled(block);
    }


    public void release() {
        mProfileBtn = null;
        mChatBtn = null;
        mInformer = null;
        informerTitlePaintHight = null;
        informerPaint = null;
    }


}
