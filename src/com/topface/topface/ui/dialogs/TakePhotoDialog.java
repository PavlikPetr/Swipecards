package com.topface.topface.ui.dialogs;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.ui.profile.AddPhotoHelper;
import com.topface.topface.utils.BitmapUtils;
import com.topface.topface.utils.Debug;

public class TakePhotoDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "Topface_TakePhotoDialog_Tag";

    private TextView mText;
    private ImageView mPhoto;
    private View mPhotoLayout;
    private Button mBtnTakePhoto;
    private Button mBtnFromGallery;
    private Button mBtnSendPhoto;
    private ImageButton mBtnClose;

    private Uri mPhotoUri = null;

    private TakePhotoListener mTakePhotoListener;
    private AddPhotoHelper mAddPhotoHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_take_photo, container, false);
        ColorDrawable color = new ColorDrawable(Color.BLACK);
        color.setAlpha(175);
        getDialog().getWindow().setBackgroundDrawable(color);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mTakePhotoListener.onDialogClose();
            }
        });
        getDialog().setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog) {
                mTakePhotoListener.onDialogClose();
            }
        });

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

        mBtnClose = (ImageButton) root.findViewById(R.id.btnClose);
        mBtnClose.setOnClickListener(this);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        initButtonsState();
    }



    private void initButtonsState() {
        if(mPhotoUri == null) {
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

    public  TakePhotoDialog() {
        super();
    }


    public static TakePhotoDialog newInstance() {
        TakePhotoDialog dialog = new TakePhotoDialog();
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Topface);
        return dialog;
    }

    public void setOnTakePhotoListener(TakePhotoListener listener) {
        mTakePhotoListener = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPhotoUri = mAddPhotoHelper.processActivityResult(requestCode,resultCode,data,false);
        setPhoto(mPhotoUri, mPhoto);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setPhoto(Uri uri, ImageView photo) {
        int maxWidth = getResources().getDimensionPixelSize(R.dimen.take_photo_max_width);
        int maxHeight = getResources().getDimensionPixelSize(R.dimen.take_photo_max_height);

        Bitmap bitmap = BitmapUtils.getBitmap(getActivity().getApplicationContext(), uri, maxWidth, maxHeight);
        if(bitmap == null) return;

        if (bitmap.getWidth() >= maxWidth || bitmap.getHeight() >= maxHeight) {
            Bitmap scaledBitmap = null;
            if (bitmap.getWidth() >= bitmap.getHeight()) {
                scaledBitmap = BitmapUtils.getScaledBitmap(bitmap, maxWidth, 0);
            } else {
                scaledBitmap = BitmapUtils.getScaledBitmap(bitmap, 0, maxHeight);
            }
            photo.setImageBitmap(scaledBitmap);
        } else {
            photo.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onClick(View v) {
        if(mTakePhotoListener != null) {
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
                    if (mPhotoUri != null) {
                        sendRequest(mPhotoUri);
                        getDialog().dismiss();
                    }
                    break;
                case R.id.btnClose:
                    mTakePhotoListener.onDialogClose();
                    getDialog().dismiss();
                    break;
                default:
                    break;
            }
        } else {
            getDialog().dismiss();
        }
    }

    private void sendRequest(Uri uri) {
        if (uri != null) {
            mAddPhotoHelper.sendRequest(uri);
        }
    }

    public interface TakePhotoListener {
        void onPhotoSentSuccess(Photo photo);
        void onPhotoSentFailure();
        void onDialogClose();
    }

    private Handler mAddPhotoHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_OK) {
                Photo photo = (Photo) msg.obj;
                mTakePhotoListener.onPhotoSentSuccess(photo);
            } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                mTakePhotoListener.onPhotoSentFailure();
            }
        }
    };
}
