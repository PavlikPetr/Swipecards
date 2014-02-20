package com.topface.topface.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

public class BonusFragment extends BaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OfferwallsManager.init(getActivity());
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_bonus);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bonus, null);
        Options.Offerwalls offerwalls = CacheProfile.getOptions().offerwalls;
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
            extraOffersContainer.addView(createButton(getActivity(), offer));
        }
        return root;
    }

    /**
     * Creates button specified by offer object: styled and with appropriate click listener
     *
     * @param activity current context
     * @param offer offer from Options
     * @return button obj
     */
    private static Button createButton(final Activity activity, final Options.Offerwalls.Offer offer) {
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
                OfferwallsManager.startOfferwall(activity, offer.action);
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
