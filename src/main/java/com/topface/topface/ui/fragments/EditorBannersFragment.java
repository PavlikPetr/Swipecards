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
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;
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
        final Profile profile = App.from(getActivity()).getProfile();
        showAsCheckBox.setChecked(profile.showAd);
        showAsCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.showAd = showAsCheckBox.isChecked();
            }
        });
    }

    private void initOnStartLoadingControls(View root) {
        final BannersConfig config = App.getBannerConfig(App.from(getActivity()).getOptions());
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
            for (PageInfo.PageName pageName : PageInfo.PageName.values()) {
                PageInfo pageInfo = App.from(getActivity()).getOptions().getPagesInfo().get(pageName.getName());
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
        ToolbarManager.INSTANCE.setToolbarSettings(new ToolbarSettingsData(getString(R.string.editor_configure_banners)));
        if (!Editor.isEditor()) {
            getActivity().finish();
        }
    }

    private void initHeader() {
    }

    private void showCompleteMessage() {
        Utils.showToastNotification(getActivity().getString(R.string.editor_fragment_complete), Toast.LENGTH_SHORT);
    }

    @Override
    public void onClick(View v) {
        Options options = App.from(getActivity()).getOptions();
        switch (v.getId()) {
            case R.id.btnSaveSettings:
                App.getBannerConfig(options).saveBannersSettings(options.getPagesInfo());
                showCompleteMessage();
                break;
            case R.id.btnResetSettings:
                App.getBannerConfig(options).resetBannersSettings();
                clearConfigContainer();
                initConfigContainer();
                showCompleteMessage();
                break;
            default:
                break;
        }
    }

    private class PageConfigurator extends LinearLayout {
        private PageInfo mPageInfo;

        private TextView mTitleText;
        private Spinner mBannerTypeSpinner;

        public PageConfigurator(Context context) {
            this(context, null);
        }

        public PageConfigurator(Context context, AttributeSet attrs) {
            super(context, attrs);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View root = inflater.inflate(R.layout.editor_banner_configurator, this, true);
            mTitleText = (TextView) root.findViewById(R.id.tvTitle);
            initBannerTypeSpinner(root);
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

        private PageInfo getPage() {
            return mPageInfo;
        }

        public void setPage(PageInfo pageInfo) {
            mPageInfo = pageInfo;
            mTitleText.setText(pageInfo.name);
            mPageInfo = pageInfo;
            for (int i = 0; i < PageInfo.FLOAT_TYPES.length; i++) {
                if (PageInfo.FLOAT_TYPES[i].equals(mPageInfo.floatType)) {
                    mBannerTypeSpinner.setVisibility(PageInfo.FLOAT_TYPES[i].equals(PageInfo.FLOAT_TYPE_BANNER) ? View.VISIBLE : View.GONE);
                }
            }

            for (int i = 0; i < AdProvidersFactory.BANNERS.length; i++) {
                if (AdProvidersFactory.BANNERS[i].equals(mPageInfo.getBanner())) {
                    mBannerTypeSpinner.setSelection(i);
                }
            }

        }
    }
}
