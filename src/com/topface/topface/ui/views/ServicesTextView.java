package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import com.topface.topface.R;

public class ServicesTextView extends View {

    private int mMaxChars;
    private int mCharPadding;
    private String text;
    private int textSize;
    private String textWithoutBackround;
    private int imageId;

    private String color = "#B8B8B8"; //TODO: сделать это как параметр view

    private Bitmap mBackgroundFree;
    private Bitmap mBackgroundFull;
    private Bitmap mImageBitmap;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private OnMeasureListener listener;

    public ServicesTextView(Context context) {
        super(context);
        mMaxChars = 1;
        mCharPadding = 0;
        initBitmaps();
    }

    public ServicesTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray allAttrs = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.ServicesTextView,
                0, 0);

        try {
            mMaxChars = allAttrs.getInteger(R.styleable.ServicesTextView_maxChars, 0);
            setCharPadding(allAttrs.getInteger(R.styleable.ServicesTextView_charPadding, 5));
            setText(allAttrs.getString(R.styleable.ServicesTextView_text));
            setTextSize(allAttrs.getInteger(R.styleable.ServicesTextView_fontSize, 12));
            textWithoutBackround = allAttrs.getString(R.styleable.ServicesTextView_textWithoutBackground);
            imageId = allAttrs.getResourceId(R.styleable.ServicesTextView_image, 0);
            initBitmaps();
        } finally {
            allAttrs.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int additionalChars = 0;
        int addWidth = 0;
        int height = mBackgroundFree.getHeight();
        if (textWithoutBackround != null) {
            additionalChars++;
        }
        if (mImageBitmap != null) {
            addWidth = mImageBitmap.getWidth();
            if (mImageBitmap.getHeight() > height) {
                height = mImageBitmap.getHeight();
            }
        }
        setMeasuredDimension(mBackgroundFree.getWidth() * (mMaxChars + additionalChars) + addWidth + mCharPadding , height);
        if(listener != null) {
            listener.onMeasure(mBackgroundFree.getWidth() * (mMaxChars + additionalChars) + addWidth + mCharPadding, height);
        }
    }

    private void initBitmaps() {
        int textHeight = getTextHeight();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), R.drawable.ic_cell_counter_free, options);

        options.inSampleSize = options.outHeight / textHeight;
        double realScaleCoeff = (double) options.outHeight / (double) textHeight;
        options.inJustDecodeBounds = false;

        int outWidth = options.outWidth;
        int outHeight = options.outHeight;

        mBackgroundFree = BitmapFactory.decodeResource(getResources(), R.drawable.ic_cell_counter_free, options);
        mBackgroundFull = BitmapFactory.decodeResource(getResources(), R.drawable.ic_cell_counter_full, options);


        mBackgroundFree = Bitmap.createScaledBitmap(mBackgroundFree, (int) (outWidth / realScaleCoeff), (int) (outHeight / realScaleCoeff), false);
        mBackgroundFull = Bitmap.createScaledBitmap(mBackgroundFull, (int) (outWidth / realScaleCoeff), (int) (outHeight / realScaleCoeff), false);

        if (imageId != 0) {
            mImageBitmap = BitmapFactory.decodeResource(getResources(), imageId);
//            double imageRes = (double)mImageBitmap.getWidth()/(double)mImageBitmap.getHeight();
//            int newWidth = (int) (imageRes * (int)(outHeight/realScaleCoeff));
            mImageBitmap = Bitmap.createScaledBitmap(mImageBitmap, (int) (outWidth / realScaleCoeff ),  (int)(outHeight/realScaleCoeff/1.5), false);
        }

    }

    private int getTextHeight() {
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor(color));
        paint.setTextSize(textSize);
        paint.setFakeBoldText(true);
        paint.setStyle(Paint.Style.FILL);
        Rect bounds = new Rect();
        paint.getTextBounds("A", 0, 1, bounds);
        return bounds.height() + 2 * mCharPadding;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int currentSymbol = 0;
        int textTop = mBackgroundFull.getHeight() / 2 + (getTextHeight() - 2 * mCharPadding) / 2;
        for (int i = 0; i < mMaxChars; i++) {
            int padding = 0;//(i==0)? 0 : mCharPadding;
            if (i < mMaxChars - text.length()) {
                canvas.drawBitmap(mBackgroundFree, i * mBackgroundFree.getWidth() + padding, 0, paint);
            } else {
                canvas.drawBitmap(mBackgroundFull, i * mBackgroundFull.getWidth() + padding, 0, paint);
                paint.setAntiAlias(true);
                paint.setColor(Color.parseColor(color));
                paint.setTextSize(textSize);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(String.valueOf(text.charAt(currentSymbol)), i * mBackgroundFull.getWidth() + mBackgroundFull.getWidth() / 2, textTop, paint);
                currentSymbol++;
            }


        }
        if (textWithoutBackround != null) {
            paint.setAntiAlias(true);
            paint.setColor(Color.parseColor(color));
            paint.setTextSize(textSize);
            paint.setFakeBoldText(true);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(textWithoutBackround, mMaxChars * mBackgroundFull.getWidth() + mBackgroundFull.getWidth() / 2, textTop, paint);
        }

        if (imageId != 0) {
            int left = textWithoutBackround == null ? mMaxChars * mBackgroundFull.getWidth() + mCharPadding : (mMaxChars + 1) * mBackgroundFull.getWidth() + mCharPadding;
            int top = mBackgroundFull.getHeight() / 2 - mImageBitmap.getHeight() / 2;
            canvas.drawBitmap(mImageBitmap, left, top, paint);
        }
    }

    public void setText(String text) {
        if (text == null) {
            this.text = "0";
        } else if (text.length() <= mMaxChars) {
            this.text = text;
        } else {
            this.text = "";
            for (int i = 0; i < mMaxChars; i++) {
                this.text += "9";
            }
            if (textWithoutBackround == null) {
                textWithoutBackround = "+";
            }
        }
        invalidate();
    }

    public void setTextSize(int textSize) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        this.textSize = (int) (textSize * scale);
    }

    public void setCharPadding(int charPadding) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        mCharPadding = (int) (charPadding * scale);
    }

    public void setOnMeasureListener(OnMeasureListener listener) {
        this.listener = listener;
    }

    public interface OnMeasureListener{
        public void onMeasure(int width, int height);
    }
}
