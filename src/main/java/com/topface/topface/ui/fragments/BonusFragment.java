package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.offerwall.common.TFCredentials;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

@FlurryOpenEvent(name = BonusFragment.PAGE_NAME)
public class BonusFragment extends WebViewFragment {

    public static final String NEED_SHOW_TITLE = "need_show_title";
    public static final String OFFERWALL_OPENED = "com.topface.topface.offerwall.opened";
    public static final String OFFERWALL_NAME = "offerwall_name";

    public static final String PAGE_NAME = "bonus";

    private Button tfOfferwallButton;

    public static BonusFragment newInstance(boolean needShowTitle) {
        BonusFragment fragment = new BonusFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(NEED_SHOW_TITLE, needShowTitle);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public String getIntegrationUrl() {
        return App.get().getOptions().bonus.integrationUrl;
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    public boolean isNeedTitles() {
        return getArguments().getBoolean(NEED_SHOW_TITLE);
    }

    @Override
    protected View getView(LayoutInflater inflater) {
        if (!TextUtils.isEmpty(getIntegrationUrl())) {
            return super.getView(inflater);
        } else {
            return getOfferwallView(inflater);
        }
    }

    @Override
    protected void onLoadProfile() {
        super.onLoadProfile();
        OfferwallsManager.init(getActivity(), App.from(getActivity()).getOptions());
        OfferwallsManager.initTfOfferwall(getActivity(), new TFCredentials.OnInitializeListener() {
            @Override
            public void onInitialized() {
                if (tfOfferwallButton != null) {
                    tfOfferwallButton.setEnabled(true);
                }
            }

            @Override
            public void onError() {
            }
        });
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_bonus);
    }

    private View getOfferwallView(LayoutInflater inflater) {
        View root = inflater.inflate(R.layout.fragment_bonus, null);
        Options.Offerwalls offerwalls = App.from(getActivity()).getOptions().offerwalls;
        // main offerwalls - blue buttons
        ((TextView) root.findViewById(R.id.tvOfferMain)).setText(offerwalls.mainText);
        ViewGroup mainOffersContainer = (ViewGroup) root.findViewById(R.id.loContainerMain);
        for (Options.Offerwalls.Offer offer : offerwalls.mainOffers) {
            mainOffersContainer.addView(createButton(getActivity(), offer));
        }
        // extra offerwalls - gray buttons
        TextView extraTitle = ((TextView) root.findViewById(R.id.tvOfferExtra));
        ViewGroup extraOffersContainer = (ViewGroup) root.findViewById(R.id.loContainerExtra);
        if (offerwalls.extraText != null) {
            extraTitle.setText(offerwalls.extraText);
        } else {
            extraTitle.setVisibility(View.GONE);
            extraOffersContainer.setVisibility(View.GONE);
        }
        for (Options.Offerwalls.Offer offer : offerwalls.extraOffers) {
            Button offerwallButton = createButton(getActivity(), offer);
            extraOffersContainer.addView(offerwallButton);
            if (offer.action.equals(OfferwallsManager.TFOFFERWALL) && TFCredentials.getAdId() == null) {
                tfOfferwallButton = offerwallButton;
                tfOfferwallButton.setEnabled(false);
            }
        }
        return root;
    }

    /**
     * Creates button specified by offer object: styled and with appropriate click listener
     *
     * @param activity current context
     * @param offer    offer from Options
     * @return button obj
     */
    private Button createButton(final Activity activity, final Options.Offerwalls.Offer offer) {
        int style;
        switch (offer.type) {
            case Options.Offerwalls.Offer.TYPE_MAIN:
                style = R.attr.blueButtonStyle;
                break;
            case Options.Offerwalls.Offer.TYPE_EXTRA:
                style = R.attr.grayButtonStyle;
                break;
            default:
                style = R.attr.grayButtonStyle;
                break;
        }
        return createButton(activity, style, offer.text, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OfferwallsManager.startOfferwall(activity, offer.action, App.from(getActivity()).getOptions());
                Intent intent = new Intent(OFFERWALL_OPENED);
                intent.putExtra(OFFERWALL_NAME, offer.action);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            }
        });
    }

    /**
     * Creates styled button with appropriate margins
     *
     * @param context  current context
     * @param defStyle style to apply to button
     * @param text     will be set to button
     * @param listener click listener for button
     * @return button obj
     */
    private static Button createButton(Context context, int defStyle, String text, View.OnClickListener listener) {
        Button btn = new Button(context, null, defStyle);
        LinearLayout.LayoutParams params = new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, Utils.getPxFromDp(12));
        btn.setLayoutParams(params);
        btn.setText(text);
        btn.setOnClickListener(listener);
        return btn;
    }
}
