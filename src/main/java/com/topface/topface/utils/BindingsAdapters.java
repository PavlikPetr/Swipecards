package com.topface.topface.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.databinding.BindingAdapter;
import android.databinding.ObservableList;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.topface.framework.imageloader.IPhoto;
import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.glide.tranformation.GlideTransformationFactory;
import com.topface.topface.ui.fragments.feed.toolbar.CustomCoordinatorLayout;
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.RangeSeekBar;
import com.topface.topface.utils.databinding.IArrayListChange;
import com.topface.topface.utils.databinding.MultiObservableArrayList;
import com.topface.topface.utils.databinding.SingleObservableArrayList;
import com.topface.topface.utils.extensions.ResourceExtensionKt;
import com.topface.topface.utils.extensions.UiTestsExtensionKt;
import com.topface.topface.utils.extensions.ViewExtensionsKt;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Сюда складывать все BindingAdapter
 * Created by tiberal on 18.01.16.
 */
public class BindingsAdapters {

    @BindingAdapter("bindDataToCompositeAdapter")
    public static void bindDataToCompositeAdapter(final RecyclerView recyclerView, SingleObservableArrayList<?> observableArrayList) {
        if (!observableArrayList.isListenerAdded() && recyclerView.getAdapter() instanceof CompositeAdapter) {
            final CompositeAdapter adapter = ((CompositeAdapter) recyclerView.getAdapter());
            ArrayList<Object> list = new ArrayList<>();
            for (Object item : observableArrayList.getObservableList()) {
                list.add(item);
            }
            adapter.setData(list);
            adapter.notifyDataSetChanged();
            observableArrayList.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<?>>() {
                @Override
                public void onChanged(ObservableList<?> objects) {
                    Debug.log("EPTA onChanged" + objects.size());
                    adapter.getData().add(objects);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onItemRangeInserted(ObservableList<?> objects, int positionStart, int itemCount) {
                    Debug.log("EPTA onItemRangeInserted" + " to pos " + positionStart + " count " + itemCount + " size " + objects.size());
                    if (itemCount == 1) {
                        adapter.getData().add(positionStart, objects.get(positionStart));
                        adapter.notifyItemInserted(positionStart);
                    } else {
                        List<?> data = objects.subList(positionStart, objects.size());
                        Debug.log("EPTA onItemRangeInserted " + " sublist size " + data.size());
                        adapter.getData().addAll(positionStart, data);
                        adapter.notifyItemRangeInserted(positionStart, itemCount);
                        if (positionStart == 0) {
                            recyclerView.getLayoutManager().scrollToPosition(0);
                        }
                    }
                }

                @Override
                public void onItemRangeRemoved(ObservableList<?> objects, int positionStart, int itemCount) {
                    Debug.log("EPTA onItemRangeRemoved " + objects.size());
                    if (itemCount == 1) {
                        adapter.getData().remove(positionStart);
                    } else {
                        adapter.getData().removeAll(new ArrayList<>(adapter.getData().subList(positionStart, itemCount)));
                    }
                    adapter.notifyItemRangeRemoved(positionStart, itemCount);
                }

                @Override
                public void onItemRangeMoved(ObservableList<?> objects, int fromPosition, int toPosition, int itemCount) {
                    Debug.log("EPTA onItemRangeMoved" + objects.size());
                }

                @Override
                public void onItemRangeChanged(ObservableList<?> objects, int positionStart, int itemCount) {
                    Debug.log("EPTA onItemRangeChanged" + objects.size());
                    if (itemCount == 1) {
                        adapter.notifyItemChanged(positionStart);
                    }
                }
            });
        }
    }

    @BindingAdapter("bindDataToCompositeAdapterWithSmoothUpdate")
    public static void bindDataToCompositeAdapterWithSmoothUpdate(final RecyclerView recyclerView, MultiObservableArrayList<Object> observableArrayList) {
        final CompositeAdapter adapter = ((CompositeAdapter) recyclerView.getAdapter());
        adapter.setData(observableArrayList.getList());
        adapter.notifyDataSetChanged();
        observableArrayList.addOnListChangeListener(new IArrayListChange<Object>() {
            @Override
            public void onChange(@NotNull final ArrayList<Object> newList) {
                final List<Object> oldList = adapter.getData();
                DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return oldList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return newList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
                    }
                });
                adapter.setData(newList);
                diff.dispatchUpdatesTo(adapter);
            }
        });
    }

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
    public static void setFabVisibility(View view, int visible) {
        com.topface.topface.utils.AnimationUtils.cancelViewAnivation(view);
        try {
            if (visible == View.VISIBLE) {
                view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.fab_show));
                view.setVisibility(View.VISIBLE);
            } else {
                view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.fab_hide));
                view.setVisibility(View.INVISIBLE);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
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

    @BindingAdapter("android:layout_paddingTop")
    public static void setPaddingTop(View view, float padding) {
        ViewExtensionsKt.setPadding(view, null, (int) padding, null, null);
    }

    @BindingAdapter("android:layout_paddingStart")
    public static void setPaddingStart(View view, float padding) {
        setPaddingLeft(view, padding);
    }

    @BindingAdapter("android:layout_paddingEnd")
    public static void setPaddingEnd(View view, float padding) {
        setPaddingRight(view, padding);
    }

    @BindingAdapter("android:layout_paddingLeft")
    public static void setPaddingLeft(View view, float padding) {
        ViewExtensionsKt.setPadding(view, (int) padding, null, null, null);
    }

    @BindingAdapter("android:layout_paddingRight")
    public static void setPaddingRight(View view, float padding) {
        ViewExtensionsKt.setPadding(view, null, null, (int) padding, null);
    }

    @BindingAdapter("android:layout_paddingBottom")
    public static void setPaddingBottom(View view, float padding) {
        ViewExtensionsKt.setPadding(view, null, null, null, (int) padding);
    }

    @BindingAdapter("android:layout_marginTop")
    public static void setMarginTop(View view, float padding) {
        ViewExtensionsKt.setMargins(view, null, (int) padding, null, null);
    }

    @BindingAdapter("android:layout_marginStart")
    public static void setMarginStart(View view, float padding) {
        setMarginLeft(view, padding);
    }

    @BindingAdapter("android:layout_marginEnd")
    public static void setMarginEnd(View view, float padding) {
        setMarginRight(view, padding);
    }

    @BindingAdapter("android:layout_marginLeft")
    public static void setMarginLeft(View view, float padding) {
        ViewExtensionsKt.setMargins(view, (int) padding, null, null, null);
    }

    @BindingAdapter("android:layout_marginRight")
    public static void setMarginRight(View view, float padding) {
        ViewExtensionsKt.setMargins(view, null, null, (int) padding, null);
    }

    @BindingAdapter("android:layout_marginBottom")
    public static void setMarginBottom(View view, float padding) {
        ViewExtensionsKt.setMargins(view, null, null, null, (int) padding);
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
    public static void setImgeByGlide(ImageView view, String res) {
        setImgeByGlideWithPlaceholder(view, res, 0);
    }

    @BindingAdapter({"setImgeByGlideWithPlaceholder", "placeholderRes"})
    public static void setImgeByGlideWithPlaceholder(ImageView view, String res, Integer placeholderRes) {
        if (res.contains(Utils.LOCAL_RES)) {
            Glide.with(view.getContext().getApplicationContext()).load(Integer.valueOf(res.replace(Utils.LOCAL_RES, Utils.EMPTY))).into(view);
        } else {
            Glide.with(view.getContext().getApplicationContext()).load(res).centerCrop().placeholder(placeholderRes).into(view);
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
    @BindingAdapter("uiTestTag")
    public static void setTag(View view, String tag) {
        UiTestsExtensionKt.setUiTestTag(view, tag);
    }

    @BindingAdapter("animationSrc")
    public static void setAnimationSrc(View view, Animation resource) {
        setAnimationSrc(view, resource, 0L);
    }

    @BindingAdapter({"animationSrc", "bindStartOffSet"})
    public static void setAnimationSrc(View view, Animation resource, Long bindStartOffSet) {
        resource.setStartOffset(bindStartOffSet);
        view.startAnimation(resource);
    }


    @SuppressWarnings("unchecked")
    @BindingAdapter(value = {"glideTransformationPhoto", "typeTransformation", "placeholderRes", "radiusOnline", "outSideCircle"}, requireAll = false)
    public static <T extends IPhoto> void setPhotoWithTransformation(final ImageView imageView, T photo, Long type, Integer placeholderRes, Float radiusOnline, Float outSideLine) {
        Context context = imageView.getContext().getApplicationContext();
        imageView.setImageResource(placeholderRes); /// Наговнякано, но работает
        if (photo == null) {
            Glide.with(context).load(placeholderRes).into(imageView);
            return;
        }
        int size = Math.max(imageView.getLayoutParams().width, imageView.getLayoutParams().height);
        int width = imageView.getLayoutParams().width;
        int height = imageView.getLayoutParams().height;
        String suitableLink = photo.getSuitableLink(height, width);
        String defaultLink = photo.getDefaultLink();
        SimpleTarget target = new SimpleTarget<GlideBitmapDrawable>(width, height) {
            @Override
            public void onResourceReady(GlideBitmapDrawable resource, GlideAnimation<? super GlideBitmapDrawable> glideAnimation) {
                imageView.setImageBitmap(resource.getBitmap());
            }
        };
        if (suitableLink != null && size > 0) {
            Glide.with(context)
                    .load(suitableLink)
                    .placeholder(placeholderRes)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .bitmapTransform(new GlideTransformationFactory(context).construct(type, radiusOnline, outSideLine))
                    .into(target);
        } else if (defaultLink != null) {
            Glide.with(context)
                    .load(defaultLink)
                    .placeholder(placeholderRes)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .bitmapTransform(new GlideTransformationFactory(context).construct(type, radiusOnline, outSideLine))
                    .into(target);
        } else {
            Glide.with(context).load(placeholderRes).into(target);
        }
    }

    @BindingAdapter({"glideBlurUrl", "blurRadius"})
    public static void setBlurredImageByUrlWithRadius(ImageView imageView, String imgUrl, int blurRadius) {
        Context context = imageView.getContext().getApplicationContext();
        Glide.with(context)
                .load(imgUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .bitmapTransform(new BlurTransformation(context, blurRadius))
                .into(imageView);
    }

    @BindingAdapter("android:layout_height")
    public static void setImageViewHeight(ImageView view, float height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) height;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("viewConfigList")
    public static void setViewConfigList(CustomCoordinatorLayout view, List<CustomCoordinatorLayout.ViewConfig> list) {
        view.setViewConfigList(list);
    }

    // вынужден идти на такой шаг, т.к. в случае с AutoSetters от dataBinding получается лажа на preLollipop
    @BindingAdapter("foreground")
    public static void setForeground(FrameLayout view, Drawable drawable) {
        view.setForeground(drawable);
    }

    @BindingAdapter("restoreScrollState")
    public static void restoreStateForLayoutManager(RecyclerView view, Parcelable state) {
        if (state != null) {
            view.getLayoutManager().onRestoreInstanceState(state);
        }
    }

    @BindingAdapter("nextSlide")
    public static void nextSlide(ViewFlipper vFlipper, Boolean doNextSlide) {
        if (doNextSlide) {
            vFlipper.showNext();
        }
    }
}
