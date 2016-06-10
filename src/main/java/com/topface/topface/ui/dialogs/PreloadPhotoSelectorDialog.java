package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.analytics.TrackedDialogFragment;
import com.topface.topface.utils.config.UserConfig;

public class PreloadPhotoSelectorDialog extends TrackedDialogFragment {

    private PreloadPhotoTypeListener mPreloadPhotoTypeListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final UserConfig userSettings = App.getUserConfig();

        final String[] preloadPhotoTypesArray = new String[PreloadPhotoSelectorTypes.values().length];
        int selectedTypeIndex = userSettings.getPreloadPhotoType().getId();
        for (int i = 0; i < preloadPhotoTypesArray.length; i++) {
            preloadPhotoTypesArray[i] = getActivity().getString(PreloadPhotoSelectorTypes.values()[i].getName());
        }
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.settings_select_preload_photo_type)
                .setSingleChoiceItems(preloadPhotoTypesArray, selectedTypeIndex, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int whichButton) {
                        PreloadPhotoSelectorTypes selectedType = PreloadPhotoSelectorTypes.values()[((AlertDialog) dialog).getListView().getCheckedItemPosition()];
                        if (mPreloadPhotoTypeListener != null
                                && userSettings.setPreloadPhotoType(selectedType.getId())) {
                            mPreloadPhotoTypeListener.onSelected(selectedType);
                        }
                        dialog.dismiss();
                    }
                }).create();
    }

    public interface PreloadPhotoTypeListener {
        void onSelected(PreloadPhotoSelectorTypes type);
    }

    public void setPreloadPhotoTypeListener(PreloadPhotoTypeListener type) {
        mPreloadPhotoTypeListener = type;
    }
}
