package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.framework.imageloader.BitmapUtils;
import com.topface.topface.R;
import com.topface.topface.utils.IPhotoTakerWithDialog;

public class TakePhotoDialog extends AbstractModalDialog implements View.OnClickListener {

    public static final String TAG = "Topface_TakePhotoDialog_Tag";

    private TextView mText;
    private ImageView mPhoto;
    private View mPhotoLayout;
    private Button mBtnTakePhoto;
    private Button mBtnFromGallery;
    private Button mBtnSendPhoto;

    private Uri mPhotoUri = null;

    private IPhotoTakerWithDialog mPhotoTaker;
    private Bitmap mBitmap;
    private Bitmap mScaledBitmap;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof IPhotoTakerWithDialog) {
            setPhotoTaker((IPhotoTakerWithDialog) activity);
        }
    }

    @Override
    protected void initContentViews(View root) {
        getDialog().setCanceledOnTouchOutside(false);
        mText = (TextView) root.findViewById(R.id.tvText);
        mPhotoLayout = root.findViewById(R.id.loPhoto);
        mPhoto = (ImageView) mPhotoLayout.findViewById(R.id.ivPhoto);
        mBtnTakePhoto = (Button) root.findViewById(R.id.btnTakePhoto);
        mBtnTakePhoto.setOnClickListener(this);
        mBtnFromGallery = (Button) root.findViewById(R.id.btnTakeFormGallery);
        mBtnFromGallery.setOnClickListener(this);
        mBtnSendPhoto = (Button) root.findViewById(R.id.btnSendPhoto);
        mBtnSendPhoto.setOnClickListener(this);
    }

    @Override
    public int getContentLayoutResId() {
        return R.layout.dialog_take_photo;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshViewsState();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mPhotoTaker != null) {
            mPhotoTaker.onTakePhotoDialogDismiss();
        }
    }

    private void refreshViewsState() {
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
            setPhoto(mPhotoUri);
        }
    }

    public TakePhotoDialog() {
        super();
    }

    public static TakePhotoDialog newInstance(Uri uri) {
        TakePhotoDialog dialog = new TakePhotoDialog();
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Topface);
        dialog.setUri(uri);
        return dialog;
    }

    public void setPhotoTaker(IPhotoTakerWithDialog listener) {
        mPhotoTaker = listener;
    }

    private void setPhoto(Uri uri) {
        if (uri == null) return;
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
            mPhoto.setImageBitmap(mScaledBitmap);
        } else {
            mPhoto.setImageBitmap(mBitmap);
        }
    }

    @Override
    public void onClick(View v) {
        if (mPhotoTaker != null) {
            switch (v.getId()) {
                case R.id.btnTakePhoto:
                    if (mPhotoUri == null) {
                        mPhotoTaker.takePhoto();
                    } else {
                        setUri(null);
                    }
                    break;
                case R.id.btnTakeFormGallery:
                    mPhotoTaker.choosePhotoFromGallery();
                    break;
                case R.id.btnSendPhoto:
                    if (mPhotoUri != null) {
                        sendRequest(mPhotoUri);
                        dismiss();
                    }
                    break;
                default:
                    break;
            }
        } else {
            dismiss();
        }
    }

    private void sendRequest(Uri uri) {
        if (uri != null && mPhotoTaker != null) {
            mPhotoTaker.sendPhotoRequest(uri);
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

    @Override
    protected void onCloseButtonClick(View v) {
        if (mPhotoTaker != null) {
            mPhotoTaker.onTakePhotoDialogDismiss();
        }
        dismiss();
    }

    /**
     * Sets photo uri and changes state of TakePhotoDialog to represent one
     * Can be called when fragment is visible to change photo on fly
     *
     * @param uri photo to represent, null will change dialog to state in which you can take a photo
     */
    public TakePhotoDialog setUri(Uri uri) {
        mPhotoUri = uri;
        if (getView() != null) {
            refreshViewsState();
        }
        return this;
    }
}
