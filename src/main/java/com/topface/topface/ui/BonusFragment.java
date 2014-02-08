package com.topface.topface.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.topface.topface.R;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.offerwalls.Offerwalls;

public class BonusFragment extends BaseFragment implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Offerwalls.init(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bonus, null);
        ViewGroup mainOffersContainer = (ViewGroup) root.findViewById(R.id.loContainerMain);
        mainOffersContainer.addView(createButton(getActivity(), 0, R.attr.blueButtonStyle, "Ololo1", this));
        ViewGroup extraOffersContainer = (ViewGroup) root.findViewById(R.id.loContainerExtra);
        extraOffersContainer.addView(createButton(getActivity(), 0, R.attr.grayButtonStyle, "Ololo2", this));
        extraOffersContainer.addView(createButton(getActivity(), 0, R.attr.grayButtonStyle, "Ololo3", this));
        return root;
    }

    /**
     * Creates styled button
     *
     * @param context  current context
     * @param defStyle style to apply to button
     * @param text     will be set to button
     * @param listener click listener for button
     */
    private static Button createButton(Context context, int id, int defStyle, String text, View.OnClickListener listener) {
        Button btn = new Button(context, null, defStyle);
        LinearLayout.LayoutParams params = new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, Utils.getPxFromDp(12));
        btn.setLayoutParams(params);
        btn.setId(id);
        btn.setText(text);
        btn.setOnClickListener(listener);
        return btn;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                Offerwalls.startOfferwall(getActivity(), Offerwalls.SPONSORPAY);
        }
    }
}
