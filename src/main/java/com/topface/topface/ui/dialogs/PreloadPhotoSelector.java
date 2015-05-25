package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.config.UserConfig;

public class PreloadPhotoSelector {

    private Context mContext;
    private UserConfig mUserSettings;
    private PreloadPhotoTypeListener mPreloadPhotoTypeListener;

    public PreloadPhotoSelector(Context context) {
        mContext = context;
        startPreloadPhotoTypesSelection();
    }

    private void startPreloadPhotoTypesSelection() {
        mUserSettings = App.getUserConfig();
        final String[] preloadPhotoTypesArray = new String[PreloadPhotoSelectorTypes.values().length];
        int selectedTypeIndex = mUserSettings.getPreloadPhotoType().getId();
        for (int i = 0; i < preloadPhotoTypesArray.length; i++) {
            preloadPhotoTypesArray[i] = mContext.getString(PreloadPhotoSelectorTypes.values()[i].getName());
        }
        setDialogBtnBackground(new AlertDialog.Builder(mContext)
                .setTitle(R.string.settings_select_preload_photo_type)
                .setSingleChoiceItems(preloadPhotoTypesArray, selectedTypeIndex, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int whichButton) {
                        PreloadPhotoSelectorTypes selectedType = PreloadPhotoSelectorTypes.values()[((AlertDialog) dialog).getListView().getCheckedItemPosition()];
                        if (mPreloadPhotoTypeListener != null
                                && mUserSettings.setPreloadPhotoType(selectedType.getId())) {
                            mPreloadPhotoTypeListener.onSelected(selectedType);
                        }
                        dialog.dismiss();
                    }
                }).show());
    }

    private void setDialogBtnBackground(AlertDialog dialog) {
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(R.drawable.btn_profile_dialog_selector);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(R.drawable.btn_profile_dialog_selector);
    }

    public interface PreloadPhotoTypeListener {
        void onSelected(PreloadPhotoSelectorTypes type);
    }

    public void setPreloadPhotoTypeListener(PreloadPhotoTypeListener type) {
        mPreloadPhotoTypeListener = type;
    }
}
