package com.topface.topface.utils;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.topface.framework.imageloader.IPhoto;
import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.RangeSeekBar;
import com.topface.topface.utils.extensions.ResourceExtensionKt;

/**
 * Сюда складывать все BindingAdapter
 * Created by tiberal on 18.01.16.
 */
public class BindingsAdapters {

    @BindingAdapter("pxTextSize")
    public static void setPxTextSize(TextView view, int size) {
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    @BindingAdapter("textTypeface")
    public static void setTextTypeface(TextView view, int typeface) {
        view.setTypeface(null, typeface);
    }

    @BindingAdapter("online")
    public static void setOnline(TextView view, boolean isOnline) {
        view.setCompoundDrawablesWithIntrinsicBounds(0, 0, isOnline ? R.drawable.im_list_online : 0, 0);
    }

    @BindingAdapter("fabVisibility")
    public static void setFabVisibility(FloatingActionButton fab, int visible) {
        if (visible == View.VISIBLE) {
            try {
                com.topface.topface.utils.AnimationUtils.cancelViewAnivation(fab);
                fab.startAnimation(AnimationUtils.loadAnimation(fab.getContext(), R.anim.fab_show));
                fab.show();
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
//                com.topface.topface.utils.AnimationUtils.cancelViewAnivation(fab);
//                fab.startAnimation(AnimationUtils.loadAnimation(fab.getContext(), R.anim.fab_hide));
                fab.hide();
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @BindingAdapter("setCompoundDrawablesWithIntrinsicBounds")
    public static void setCompoundDrawablesWithIntrinsicBounds(TextView view, int image) {
        view.setCompoundDrawablesWithIntrinsicBounds(image, 0, 0, 0);
    }

    @BindingAdapter("isActivated")
    public static void isActivated(View view, boolean isActivated) {
        view.setActivated(isActivated);
    }

    @BindingAdapter("showChild")
    public static void showChild(ViewFlipper flipper, int childPosition) {
        flipper.setDisplayedChild(childPosition);
    }

    @BindingAdapter("OnSwipeRefreshListener")
    public static void OnSwipeRefreshListener(SwipeRefreshLayout refreshLayout, SwipeRefreshLayout.OnRefreshListener refreshListener) {
        refreshLayout.setOnRefreshListener(refreshListener);
    }

    @BindingAdapter("setRefresh")
    public static void setRefresh(SwipeRefreshLayout refreshLayout, boolean isRefresh) {
        refreshLayout.setRefreshing(isRefresh);
    }

    @BindingAdapter("animate")
    public static void animateDisplaying(ImageViewRemote imageView, int duration) {
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(duration);
        imageView.setViewDisplayAnimate(animation);
    }

    @BindingAdapter("onLongItemClick")
    public static void onLongItemClick(View view, View.OnLongClickListener longClickListener) {
        view.setOnLongClickListener(longClickListener);
    }

    @BindingAdapter("android:layout_marginTop")
    public static void setMarginTop(View view, float padding) {
        if (view.getLayoutParams().getClass().equals(LinearLayout.LayoutParams.class)) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            lp.setMargins(lp.leftMargin, (int) padding, lp.rightMargin, lp.bottomMargin);
            return;
        }
        if (view.getLayoutParams().getClass().equals(RelativeLayout.LayoutParams.class)) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            lp.setMargins(lp.leftMargin, (int) padding, lp.rightMargin, lp.bottomMargin);
        }
    }

    @BindingAdapter("android:layout_marginRight")
    public static void setMarginRight(View view, float padding) {
        if (view.getLayoutParams().getClass().equals(RelativeLayout.LayoutParams.class)) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            lp.setMargins(lp.leftMargin, lp.topMargin, (int) padding, lp.bottomMargin);
            return;
        }
        if (view.getLayoutParams().getClass().equals(LinearLayout.LayoutParams.class)) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            lp.setMargins(lp.leftMargin, lp.topMargin, (int) padding, lp.bottomMargin);
        }
    }

    @BindingAdapter("android:layout_marginBottom")
    public static void setMarginBottom(View view, float padding) {
        if (view.getLayoutParams().getClass().equals(LinearLayout.LayoutParams.class)) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, (int) padding);
            return;
        }
        if (view.getLayoutParams().getClass().equals(RelativeLayout.LayoutParams.class)) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, (int) padding);
        }
    }

    @BindingAdapter("android:background")
    public static void setBackgroundResource(View view, @DrawableRes int bgResource) {
        view.setBackgroundResource(bgResource);
    }

    @BindingAdapter("android:src")
    public static void setImageResource(ImageView view, @DrawableRes int bgResource) {
        view.setImageResource(bgResource);
    }

    @BindingAdapter("android:drawableTop")
    public static void setDrawableTop(TextView view, @DrawableRes int bgResource) {
        BindingsUtils.replaceDrawable(view, bgResource, BindingsUtils.TOP);
    }

    @BindingAdapter("android:drawableLeft")
    public static void setDrawableLeft(TextView view, @DrawableRes int bgResource) {
        BindingsUtils.replaceDrawable(view, bgResource, BindingsUtils.LEFT);
    }

    @BindingAdapter("android:drawableRight")
    public static void setDrawableRight(TextView view, @DrawableRes int bgResource) {
        BindingsUtils.replaceDrawable(view, bgResource, BindingsUtils.RIGHT);
    }

    @BindingAdapter("android:text")
    public static void setText(TextView view, @StringRes int stringRes) {
        view.setText(stringRes != 0 ? view.getResources().getString(stringRes) : "");
    }

    @BindingAdapter("textColorSelector")
    public static void setTextColorSelector(View view, int colorSelector) {
        try {
            XmlResourceParser xrp = view.getResources().getXml(colorSelector);
            ColorStateList csl = ColorStateList.createFromXml(view.getResources(), xrp);
            ((TextView) view).setTextColor(csl);
        } catch (Exception e) {
            Debug.error(e.toString());
        }
    }

    @BindingAdapter("remoteSrc")
    public static void setremoteSrc(ImageViewRemote view, String res) {
        BindingsUtils.loadLink(view, res, BindingsUtils.EMPTY_RESOURCE);
    }

    @BindingAdapter({"remoteSrc", "defaultSelector"})
    public static void setremoteSrc(ImageViewRemote view, String res, @DrawableRes int drawableRes) {
        BindingsUtils.loadLink(view, res, drawableRes);
    }

    @BindingAdapter("enable")
    public static void setEnable(View view, boolean state) {
        view.setEnabled(state);
    }

    @BindingAdapter("selected")
    public static void setSelected(View view, boolean isSelected) {
        view.setSelected(isSelected);
    }

    @BindingAdapter("setPhoto")
    public static void setPhoto(ImageViewRemote view, IPhoto photo) {
        view.setPhoto((photo));
    }

    @BindingAdapter("currentMinValue")
    public static void setRangeSeekBarCurrentMinimalValue(RangeSeekBar view, int value) {
        view.setCurrentMinimalValue(value);
    }

    @BindingAdapter("currentMaxValue")
    public static void setRangeSeekBarCurrentMaximalValue(RangeSeekBar view, int value) {
        view.setCurrentMaximalValue(value);
    }

    @BindingAdapter("maxValue")
    public static void setRangeSeekBarMaxValue(RangeSeekBar view, int value) {
        view.setMaximalValue(value);
    }

    @BindingAdapter("minValue")
    public static void setRangeSeekBarMinValue(RangeSeekBar view, int value) {
        view.setMinimalValue(value);
    }

    @BindingAdapter("maxValueTitle")
    public static void setRangeSeekBarMaxValueTitle(RangeSeekBar view, String value) {
        view.setMaximalValueTitle(value);
    }

    @BindingAdapter("minValueTitle")
    public static void setRangeSeekBarMinValueTitle(RangeSeekBar view, String value) {
        view.setMinimalValueTitle(value);
    }

    @BindingAdapter("remoteSrcGlide")
    public static void setImgeByGlide(ImageViewRemote view, String res) {
        if (res.contains(Utils.LOCAL_RES)) {
            Glide.with(view.getContext().getApplicationContext()).load(Integer.valueOf(res.replace(Utils.LOCAL_RES, Utils.EMPTY))).into(view);
        } else {
            Glide.with(view.getContext().getApplicationContext()).load(res).into(view);
        }
    }

    @BindingAdapter("navigationIcon")
    public static void setNavigationIcon(Toolbar view, @DrawableRes int resource) {
        Drawable drawable = ResourceExtensionKt.getDrawable(resource);
        if (drawable != null) {
            view.setNavigationIcon(drawable);
        }
    }

    /*
    *Если надо через DB засетить тег для автоматизированного тестирования, то следует использовать этот атрибут
    */
    @BindingAdapter("specialTag")
    public static void setTag(View view, String tag) {
        view.setTag(tag);
    }

    @BindingAdapter("animationSrc")
    public static void setAnimationSrc(View view, Animation resource) {
        view.startAnimation(resource);
    }
}