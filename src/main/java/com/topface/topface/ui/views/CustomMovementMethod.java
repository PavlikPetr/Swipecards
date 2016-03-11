package com.topface.topface.ui.views;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import com.topface.topface.utils.IActivityDelegate;
import com.topface.topface.utils.Utils;

public class CustomMovementMethod extends LinkMovementMethod {
    private static CustomMovementMethod sInstance;
    private IActivityDelegate mIActivityDelegate;

    public static CustomMovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new CustomMovementMethod();

        return sInstance;
    }

    public void setIActivityDelegate(IActivityDelegate iActivityDelegate) {
        mIActivityDelegate = iActivityDelegate;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    if (mIActivityDelegate != null) {
                        Utils.goToUrl(mIActivityDelegate, ((URLSpan) link[0]).getURL());
                    }
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]));
                }

                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }
}