package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.databinding.TakePhotoDialogBinding;
import com.topface.topface.state.TopfaceAppState;

import javax.inject.Inject;

/**
 * Выбираем фоточку, показывается если нет ни одной
 * Created by tiberal on 15.03.16.
 */
public class TakePhotoPopup extends AbstractDialogFragment implements View.OnClickListener {

    public static final String TAG = "take_photo_popup";
    public static final String EXTRA_PLC = "TakePhotoActivity.Extra.Plc";
    @Inject
    TopfaceAppState mAppState;
    private Bundle mArgs;

    public enum TakePhotoPopupAction {ACTION_CAMERA_CHOSEN, ACTION_GALLERY_CHOSEN, ACTION_CANCEL}

    public static TakePhotoPopup newInstance(String plc) {
        TakePhotoPopup popup = new TakePhotoPopup();
        Bundle arg = new Bundle();
        arg.putString(EXTRA_PLC, plc);
        popup.setArguments(arg);
        return popup;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.get().inject(this);
        mArgs = getArguments();
        mArgs = mArgs == null ? savedInstanceState : mArgs;
    }

    @Override
    protected void initViews(View root) {
        TakePhotoDialogBinding binding = TakePhotoDialogBinding.bind(root);
        binding.setHandlers(new Handlers(getDialog(), mArgs, mAppState));
        binding.uploadPhotoPlaceholder.setBackgroundResource(App.get().getProfile().sex == Profile.GIRL ? R.drawable.upload_photo_female : R.drawable.upload_photo_male);
        ((TextView) binding.getRoot().findViewById(R.id.title)).setText(R.string.take_photo);
        binding.getRoot().findViewById(R.id.title_clickable).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mAppState.setData(new TakePhotoActionHolder(TakePhotoPopupAction.ACTION_CANCEL, mArgs.getString(EXTRA_PLC)));
        getDialog().cancel();
    }

    @Override
    protected int getDialogLayoutRes() {
        return R.layout.take_photo_dialog;
    }

    @Override
    protected boolean isModalDialog() {
        return false;
    }

    @Override
    public boolean isUnderActionBar() {
        return false;
    }

    public static class TakePhotoActionHolder {
        private TakePhotoPopupAction mAction;
        private String mPlc;

        public TakePhotoActionHolder(TakePhotoPopupAction action, String plc) {
            mAction = action;
            mPlc = plc;
        }

        public TakePhotoPopupAction getAction() {
            return mAction;
        }

        public String getPlc() {
            return mPlc;
        }
    }

    public static class Handlers {

        private Bundle mArgs;
        private TopfaceAppState mAppState;
        private Dialog mDialog;


        public Handlers(Dialog dialog, Bundle args, TopfaceAppState appState) {
            mArgs = args;
            mAppState = appState;
            mDialog = dialog;
        }

        public void onTakeClick(View view) {
            mAppState.setData(new TakePhotoActionHolder(TakePhotoPopupAction.ACTION_CAMERA_CHOSEN, mArgs.getString(EXTRA_PLC)));
            view.setEnabled(false);
            mDialog.cancel();
        }

        public void onChooseClick(View view) {
            mAppState.setData(new TakePhotoActionHolder(TakePhotoPopupAction.ACTION_GALLERY_CHOSEN, mArgs.getString(EXTRA_PLC)));
            view.setEnabled(false);
            mDialog.cancel();
        }

    }
}
