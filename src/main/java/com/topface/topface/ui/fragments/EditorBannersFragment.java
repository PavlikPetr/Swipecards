package com.topface.topface.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.banners.ad_providers.AdProvidersFactory;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.ads.BannersConfig;

public class EditorBannersFragment extends BaseFragment implements View.OnClickListener {

    private ViewGroup mConfigContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_editor_banners, null);
        initHeader();
        mConfigContainer = (ViewGroup) root.findViewById(R.id.loBannersConfigurationsContainer);
        initConfigContainer();
        root.findViewById(R.id.btnSaveSettings).setOnClickListener(this);
        root.findViewById(R.id.btnResetSettings).setOnClickListener(this);
        initShowAdsCheckbox(root);
        initOnStartLoadingControls(root);
        return root;
    }

    private void initShowAdsCheckbox(View root) {
        final CheckBox showAsCheckBox = ((CheckBox) root.findViewById(R.id.show_ad_checkbox));
        showAsCheckBox.setChecked(CacheProfile.show_ad);
        showAsCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CacheProfile.show_ad =showAsCheckBox.isChecked();
            }
        });
    }

    private void initOnStartLoadingControls(View root) {
        final BannersConfig config = App.getBannerConfig();
        final CheckBox checkBoxOnStart = ((CheckBox) root.findViewById(R.id.cbOnStart));
        checkBoxOnStart.setChecked(config.needLoadOnStart());
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                config.setLoadOnStart(checkBoxOnStart.isChecked());
            }
        };
        checkBoxOnStart.setOnClickListener(listener);
    }

    private void initConfigContainer() {
        if (mConfigContainer != null) {
            for (String pageName : CacheProfile.getOptions().getPagesInfo().keySet()) {
                PageInfo pageInfo = CacheProfile.getOptions().getPagesInfo().get(pageName);
                if (pageInfo != null) {
                    PageConfigurator configurator = new PageConfigurator(getActivity());
                    configurator.setPage(pageInfo);
                    mConfigContainer.addView(configurator);
                }
            }
        }
    }

    private void clearConfigContainer() {
        if (mConfigContainer != null) {
            mConfigContainer.removeAllViews();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Editor.isEditor()) {
            getActivity().finish();
        }
    }

    private void initHeader() {
    }

    private void showCompleteMessage() {
        Toast.makeText(getActivity(), getActivity().getString(R.string.editor_fragment_complete), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSaveSettings:
                App.getBannerConfig().saveBannersSettings();
                showCompleteMessage();
                break;
            case R.id.btnResetSettings:
                App.getBannerConfig().resetBannersSettings();
                clearConfigContainer();
                initConfigContainer();
                showCompleteMessage();
                break;
            default:
                break;
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.editor_configure_banners);
    }

    private class PageConfigurator extends LinearLayout {
        private PageInfo mPageInfo;

        private TextView mTitleText;
        private ViewGroup mSpinnersContainer;
        private Spinner mFloatTypeSpinner;
        private Spinner mBannerTypeSpinner;

        private LayoutParams mCompressedParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0);

        private LayoutParams mExpandedParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 150);
        private boolean mExpanded = false;

        public PageConfigurator(Context context) {
            this(context, null);
        }

        public PageConfigurator(Context context, AttributeSet attrs) {
            super(context, attrs);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View root = inflater.inflate(R.layout.editor_banner_configurator, this, true);
            initTitleText(root);
            initSpinnersContainer(root);
            initFloatTypeSpinner(root);
            initBannerTypeSpinner(root);
            Utils.enableLayoutChangingTransition(this);
        }

        private void initTitleText(View root) {
            mTitleText = (TextView) root.findViewById(R.id.tvTitle);
            mTitleText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSpinnersContainer != null) {
                        mSpinnersContainer.setLayoutParams(mExpanded ? mCompressedParams : mExpandedParams);
                        mExpanded = !mExpanded;
                        requestLayout();
                    }
                }
            });
        }

        private void initSpinnersContainer(View root) {
            mSpinnersContainer = (ViewGroup) root.findViewById(R.id.loSpinners);
            mSpinnersContainer.setLayoutParams(mCompressedParams);
        }

        private void initBannerTypeSpinner(View root) {
            mBannerTypeSpinner = (Spinner) root.findViewById(R.id.spEditBannerType);
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    getActivity(),
                    android.R.layout.simple_spinner_item,
                    AdProvidersFactory.BANNERS
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mBannerTypeSpinner.setAdapter(adapter);
            mBannerTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    getPage().setBanner(AdProvidersFactory.BANNERS[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        private void initFloatTypeSpinner(View root) {
            mFloatTypeSpinner = (Spinner) root.findViewById(R.id.spEditFloatType);
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    getActivity(),
                    android.R.layout.simple_spinner_item,
                    PageInfo.FLOAT_TYPES
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mFloatTypeSpinner.setAdapter(adapter);
            mFloatTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    getPage().floatType = PageInfo.FLOAT_TYPES[position];
                    mBannerTypeSpinner.setVisibility(PageInfo.FLOAT_TYPES[position].equals(PageInfo.FLOAT_TYPE_BANNER) ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        private PageInfo getPage() {
            return mPageInfo;
        }

        public void setPage(PageInfo pageInfo) {
            mPageInfo = pageInfo;
            mTitleText.setText(pageInfo.name);
            mPageInfo = pageInfo;
            for (int i = 0; i < PageInfo.FLOAT_TYPES.length; i++) {
                if (PageInfo.FLOAT_TYPES[i].equals(mPageInfo.floatType)) {
                    mFloatTypeSpinner.setSelection(i);
                    mBannerTypeSpinner.setVisibility(PageInfo.FLOAT_TYPES[i].equals(PageInfo.FLOAT_TYPE_BANNER) ? View.VISIBLE : View.GONE);
                }
            }
            if (mPageInfo.name.equals(PageInfo.PageName.GAG.getName())
                    || mPageInfo.name.equals(PageInfo.PageName.START.getName())) {
                mFloatTypeSpinner.setVisibility(View.GONE);
            }

            for (int i = 0; i < AdProvidersFactory.BANNERS.length; i++) {
                if (AdProvidersFactory.BANNERS[i].equals(mPageInfo.getBanner())) {
                    mBannerTypeSpinner.setSelection(i);
                }
            }

        }
    }
}
