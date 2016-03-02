package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.topface.topface.R;
import com.topface.topface.databinding.VipLibertyViewBinding;

/**
 * Created by ppetr on 02.03.16.
 * Custom view for vip liberty items
 */
public class VipLibertyView extends LinearLayout {

    private VipLibertyViewHandler mBtnHandler;

    public VipLibertyView(Context context, AttributeSet attrs) {
        super(context.getApplicationContext(), attrs);
        initViews();
        getAttrs(context.getApplicationContext(), attrs, 0);
    }

    public VipLibertyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context.getApplicationContext(), attrs, defStyleAttr);
        initViews();
        getAttrs(context.getApplicationContext(), attrs, defStyleAttr);
    }

    public VipLibertyView(Context context) {
        super(context.getApplicationContext());
        initViews();
    }

    private void getAttrs(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VipLibertyView, defStyle, 0);
        setViewsData(a.getResourceId(R.styleable.VipLibertyView_imageRes, 0), a.getString(R.styleable.VipLibertyView_titleText), a.getString(R.styleable.VipLibertyView_descriptionText));
        a.recycle();
    }

    private void initViews() {
        VipLibertyViewBinding binding = DataBindingUtil.bind(inflateRootView());
        mBtnHandler = new VipLibertyViewHandler();
        binding.setHandler(mBtnHandler);
    }

    private View inflateRootView() {
        View view = inflate(getContext(), R.layout.vip_liberty_view, null);
        view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(view);
        return view;
    }

    public VipLibertyView setViewsData(@DrawableRes int res, String title, String description) {
        mBtnHandler.iconRes.set(res);
        mBtnHandler.titleVisibility.set(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        mBtnHandler.titleText.set(title);
        mBtnHandler.descriptionVisibility.set(TextUtils.isEmpty(description) ? View.GONE : View.VISIBLE);
        mBtnHandler.descriptionText.set(description);
        return this;
    }

    public static class VipLibertyViewHandler {
        public final ObservableField<String> titleText = new ObservableField<>();
        public final ObservableInt titleVisibility = new ObservableInt(View.GONE);
        public final ObservableField<String> descriptionText = new ObservableField<>();
        public final ObservableInt descriptionVisibility = new ObservableInt(View.GONE);
        public final ObservableInt iconRes = new ObservableInt();
    }
}
