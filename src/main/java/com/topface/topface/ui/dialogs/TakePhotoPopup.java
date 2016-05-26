package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.databinding.TakePhotoDialogBinding;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.utils.debug.FuckingVoodooMagic;

import javax.inject.Inject;

/**
 * Выбираем фоточку, показывается если нет ни одной
 * Created by tiberal on 15.03.16.
 */
public class TakePhotoPopup extends AbstractDialogFragment implements View.OnClickListener {

    public static final String TAG = "take_photo_popup";
    public static final String EXTRA_PLC = "TakePhotoActivity.Extra.Plc";

    public static final int ACTION_UNDEFINED = 0;
    public static final int ACTION_CAMERA_CHOSEN = 1;
    public static final int ACTION_GALLERY_CHOSEN = 2;
    public static final int ACTION_CANCEL = 3;

    @IntDef({ACTION_UNDEFINED, ACTION_CAMERA_CHOSEN, ACTION_GALLERY_CHOSEN, ACTION_CANCEL})
    public @interface TakePhotoPopupAction {

    }

    @Inject
    TopfaceAppState mAppState;
    private Bundle mArgs;

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(),getTheme()){
            @Override
            public void onBackPressed() {
                super.onBackPressed();
                mAppState.setData(new TakePhotoActionHolder(ACTION_CANCEL, mArgs.getString(EXTRA_PLC)));
            }
        };
    }

    @Override
    protected void initViews(View root) {
        TakePhotoDialogBinding binding = TakePhotoDialogBinding.bind(root);
        binding.setHandlers(new Handlers(getDialog(), mArgs, mAppState));
        binding.uploadPhotoPlaceholder.setBackgroundResource(App.get().getProfile().sex == Profile.GIRL ? R.drawable.upload_photo_female : R.drawable.upload_photo_male);
        ((TextView) binding.getRoot().findViewById(R.id.title)).setText(R.string.take_photo);
        binding.getRoot().findViewById(R.id.title_clickable).setOnClickListener(this);
    }

    @FuckingVoodooMagic(description = "рассылка ивентов о действиях с попапом добавления фото")
    @Override
    public void onClick(View v) {
        mAppState.setData(new TakePhotoActionHolder(ACTION_CANCEL, mArgs.getString(EXTRA_PLC)));
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
        @TakePhotoPopupAction
        private int mAction;
        private String mPlc;

        public TakePhotoActionHolder(String plc) {
            this(ACTION_UNDEFINED, plc);
        }

        public TakePhotoActionHolder(@TakePhotoPopupAction int action, String plc) {
            mAction = action;
            mPlc = plc;
        }

        @TakePhotoPopupAction
        public int getAction() {
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

        @FuckingVoodooMagic(description = "рассылка ивентов о действиях с попапом добавления фото")
        public void onTakeClick(View view) {
            mAppState.setData(new TakePhotoActionHolder(ACTION_CAMERA_CHOSEN, mArgs.getString(EXTRA_PLC)));
            view.setEnabled(false);
            mDialog.cancel();
        }

        @FuckingVoodooMagic(description = "рассылка ивентов о действиях с попапом добавления фото")
        public void onChooseClick(View view) {
            mAppState.setData(new TakePhotoActionHolder(ACTION_GALLERY_CHOSEN, mArgs.getString(EXTRA_PLC)));
            view.setEnabled(false);
            mDialog.cancel();
        }

    }
}
