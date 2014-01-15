package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.ui.profile.AddPhotoHelper;
import com.topface.topface.utils.BitmapUtils;

public class TakePhotoDialog extends BaseDialogFragment implements View.OnClickListener {
    public static final String TAG = "Topface_TakePhotoDialog_Tag";

    private TextView mText;
    private ImageView mPhoto;
    private View mPhotoLayout;
    private Button mBtnTakePhoto;
    private Button mBtnFromGallery;
    private Button mBtnSendPhoto;

    private Uri mPhotoUri = null;

    private ITakePhotoListener mTakePhotoListener;
    private AddPhotoHelper mAddPhotoHelper;
    private Bitmap mBitmap;
    private Bitmap mScaledBitmap;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ITakePhotoListener) {
            setOnTakePhotoListener((ITakePhotoListener) activity);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_take_photo, container, false);
        getDialog().setCanceledOnTouchOutside(false);
        mAddPhotoHelper = new AddPhotoHelper(this);
        mAddPhotoHelper.setOnResultHandler(mAddPhotoHandler);

        mText = (TextView) root.findViewById(R.id.tvText);

        mPhotoLayout = root.findViewById(R.id.loPhoto);
        mPhoto = (ImageView) mPhotoLayout.findViewById(R.id.ivPhoto);

        mBtnTakePhoto = (Button) root.findViewById(R.id.btnTakePhoto);
        mBtnTakePhoto.setOnClickListener(this);
        mBtnFromGallery = (Button) root.findViewById(R.id.btnTakeFormGallery);
        mBtnFromGallery.setOnClickListener(this);
        mBtnSendPhoto = (Button) root.findViewById(R.id.btnSendPhoto);
        mBtnSendPhoto.setOnClickListener(this);

        root.findViewById(R.id.btnClose).setOnClickListener(this);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        initButtonsState();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mTakePhotoListener != null) {
            mTakePhotoListener.onTakePhotoDialogDismiss();
        }
    }

    private void initButtonsState() {
        if (mPhotoUri == null) {
            mBtnSendPhoto.setVisibility(View.GONE);
            mBtnFromGallery.setVisibility(View.VISIBLE);
            mPhotoLayout.setVisibility(View.GONE);
            mBtnTakePhoto.setText(R.string.take_photo);
            mText.setText(R.string.no_photo_take_photo);
        } else {
            mBtnSendPhoto.setVisibility(View.VISIBLE);
            mBtnFromGallery.setVisibility(View.INVISIBLE);
            mPhotoLayout.setVisibility(View.VISIBLE);
            mBtnTakePhoto.setText(R.string.take_another_photo);
            mText.setText(R.string.photo_for_avatar);
        }
    }

    public TakePhotoDialog() {
        super();
    }

    public static TakePhotoDialog newInstance() {
        TakePhotoDialog dialog = new TakePhotoDialog();
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Topface);
        return dialog;
    }

    public void setOnTakePhotoListener(ITakePhotoListener listener) {
        mTakePhotoListener = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPhotoUri = mAddPhotoHelper.processActivityResult(requestCode, resultCode, data, false);
        setPhoto(mPhotoUri, mPhoto);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setPhoto(Uri uri, ImageView photo) {
        int maxWidth = getResources().getDimensionPixelSize(R.dimen.take_photo_max_width);
        int maxHeight = getResources().getDimensionPixelSize(R.dimen.take_photo_max_height);
        mBitmap = BitmapUtils.getBitmap(getActivity(), uri, maxWidth, maxHeight);
        if (mBitmap == null) return;
        if (mBitmap.getWidth() >= maxWidth || mBitmap.getHeight() >= maxHeight) {
            if (mBitmap.getWidth() >= mBitmap.getHeight()) {
                mScaledBitmap = BitmapUtils.getScaledBitmap(mBitmap, maxWidth, 0);
            } else {
                mScaledBitmap = BitmapUtils.getScaledBitmap(mBitmap, 0, maxHeight);
            }
            photo.setImageBitmap(mScaledBitmap);
        } else {
            photo.setImageBitmap(mBitmap);
        }
    }

    @Override
    public void onClick(View v) {
        Dialog dialog = getDialog();
        if (mTakePhotoListener != null) {
            switch (v.getId()) {
                case R.id.btnTakePhoto:
                    if (mPhotoUri == null) {
                        mAddPhotoHelper.getAddPhotoClickListener().onClick(v);
                    } else {
                        mPhotoUri = null;
                        initButtonsState();
                    }
                    break;
                case R.id.btnTakeFormGallery:
                    mAddPhotoHelper.getAddPhotoClickListener().onClick(v);
                    break;
                case R.id.btnSendPhoto:
                    if (mPhotoUri != null && dialog != null) {
                        sendRequest(mPhotoUri);
                        dialog.dismiss();
                    }
                    break;
                case R.id.btnClose:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    mTakePhotoListener.onTakePhotoDialogDismiss();
                    break;
                default:
                    break;
            }
        } else if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void sendRequest(Uri uri) {
        if (uri != null) {
            if (mAddPhotoHelper != null) mAddPhotoHelper.sendRequest(uri);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBitmap != null) {
            mBitmap.recycle();
        }
        if (mScaledBitmap != null) {
            mScaledBitmap.recycle();
        }
    }

    public interface ITakePhotoListener {
        void onTakePhotoDialogSentSuccess(Photo photo);

        void onTakePhotoDialogSentFailure();

        void onTakePhotoDialogDismiss();
    }

    private Handler mAddPhotoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_OK) {
                Photo photo = (Photo) msg.obj;
                if (mTakePhotoListener != null) mTakePhotoListener.onTakePhotoDialogSentSuccess(photo);
            } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                if (mTakePhotoListener != null) mTakePhotoListener.onTakePhotoDialogSentFailure();
            }
        }
    };
}
