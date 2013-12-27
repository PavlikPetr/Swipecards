package com.topface.topface.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.BackgroundThread;

/**
 * Created by kirussell on 25.12.13.
 * User can rate an app through this dialog
 */
public class RateAppDialog extends BaseDialogFragment implements View.OnClickListener,
        DialogInterface.OnCancelListener {

    public static final String RATING_POPUP = "RATING_POPUP";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Закрыть диалог можно
        setCancelable(true);
        //По стилю это у нас не диалог, а кастомный дизайн -
        //закрывает весь экран оверлеем и ниже ActionBar показывает контент
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_rate_app, container);
        getDialog().setOnCancelListener(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRate:
                //TODO
                break;
            case R.id.btnAskLater:
                getDialog().cancel();
                //TODO
                break;
            case R.id.btnNoThanx:
                // TODO

                break;
        }
    }

    private void saveRatingPopupStatus(final long value) {
        new BackgroundThread() {
            @Override
            public void execute() {
                final SharedPreferences.Editor editor = getActivity().getSharedPreferences(
                        Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE
                ).edit();
                editor.putLong(RATING_POPUP, value);
                editor.commit();
            }
        };
    }
}
