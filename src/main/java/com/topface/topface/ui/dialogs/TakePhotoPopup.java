package com.topface.topface.ui.dialogs;

import android.Manifest;
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
import com.topface.topface.state.EventBus;
import com.topface.topface.utils.debug.FuckingVoodooMagic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * Выбираем фоточку, показывается если нет ни одной
 * Created by tiberal on 15.03.16.
 */
@RuntimePermissions
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
    EventBus mEventBus;
    @Nullable
    private Bundle mArgs;
    private Handlers mHandlers;

    @NotNull
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
        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                super.onBackPressed();
                if (mArgs != null) {
                    mEventBus.setData(new TakePhotoActionHolder(ACTION_CANCEL, mArgs.getString(EXTRA_PLC)));
                }
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        TakePhotoPopupPermissionsDispatcher.onRequestPermissionsResult(TakePhotoPopup.this, requestCode, grantResults);
    }

    @Override
    protected void initViews(View root) {
        TakePhotoDialogBinding binding = TakePhotoDialogBinding.bind(root);
        mHandlers = new Handlers(getDialog(), mArgs, mEventBus, new Handlers.TakePhotoClickListener() {
            @Override
            public void onTakeCameraPhoto() {
                TakePhotoPopupPermissionsDispatcher.tekePhotoWithCheck(TakePhotoPopup.this);
            }
        });
        binding.setHandlers(mHandlers);
        binding.uploadPhotoPlaceholder.setBackgroundResource(App.get().getProfile().sex == Profile.GIRL ? R.drawable.upload_photo_female : R.drawable.upload_photo_male);
        ((TextView) binding.getRoot().findViewById(R.id.title)).setText(R.string.take_photo);
        binding.getRoot().findViewById(R.id.title_clickable).setOnClickListener(this);
    }

    @FuckingVoodooMagic(description = "рассылка ивентов о действиях с попапом добавления фото")
    @Override
    public void onClick(View v) {
        if (mArgs != null) {
            mEventBus.setData(new TakePhotoActionHolder(ACTION_CANCEL, mArgs.getString(EXTRA_PLC)));
        }
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

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void tekePhoto() {
        if (mHandlers != null) {
            mHandlers.takePhoto();
        }
    }

    public static class Handlers {

        private Bundle mArgs;
        private EventBus mEventBus;
        private Dialog mDialog;
        private TakePhotoClickListener mListener;


        public Handlers(Dialog dialog, Bundle args, EventBus eventBus, TakePhotoClickListener listener) {
            mArgs = args;
            mEventBus = eventBus;
            mDialog = dialog;
            mListener = listener;
        }

        public void takePhoto() {
            mEventBus.setData(new TakePhotoActionHolder(ACTION_CAMERA_CHOSEN, mArgs.getString(EXTRA_PLC)));
            mDialog.cancel();
        }

        @FuckingVoodooMagic(description = "рассылка ивентов о действиях с попапом добавления фото")
        public void onTakeClick(View view) {
            mListener.onTakeCameraPhoto();
        }

        @FuckingVoodooMagic(description = "рассылка ивентов о действиях с попапом добавления фото")
        public void onChooseClick(View view) {
            mEventBus.setData(new TakePhotoActionHolder(ACTION_GALLERY_CHOSEN, mArgs.getString(EXTRA_PLC)));
            view.setEnabled(false);
            mDialog.cancel();
        }

        public interface TakePhotoClickListener {
            void onTakeCameraPhoto();
        }

    }
}
