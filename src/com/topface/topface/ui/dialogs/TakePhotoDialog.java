package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.PhotoAddRequest;
import com.topface.topface.utils.BitmapUtils;
import com.topface.topface.utils.Utils;

import java.io.File;

public class TakePhotoDialog extends DialogFragment implements View.OnClickListener{

    public static final String TAG = "Topface_TakePhotoDialog_Tag";

    public static final String PATH_TO_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp.jpg";

    public static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA = 101;
    public static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY = 100;

    private TextView mText;
    private ImageView mPhoto;
    private View mPhotoLayout;
    private Button mBtnTakePhoto;
    private Button mBtnFromGallery;
    private Button mBtnSendPhoto;
    private ImageButton mBtnClose;

    private Uri mPhotoUri = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_take_photo, container, false);
        ColorDrawable color = new ColorDrawable(Color.BLACK);
        color.setAlpha(175);
        getDialog().getWindow().setBackgroundDrawable(color);
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

        mBtnClose = (ImageButton) root.findViewById(R.id.btnClose);
        mBtnClose.setOnClickListener(this);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mPhotoUri == null) {
            mBtnSendPhoto.setVisibility(View.GONE);
            mPhotoLayout.setVisibility(View.GONE);
        } else {
            mBtnSendPhoto.setVisibility(View.VISIBLE);
            mPhotoLayout.setVisibility(View.VISIBLE);
        }
    }

    public static TakePhotoDialog newInstance() {
        TakePhotoDialog dialog = new TakePhotoDialog();
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Topface);
        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA) {
                //Если фотография сделана, то ищем ее во временном файле
                mPhotoUri = Uri.fromParts("file", PATH_TO_FILE, null);
            } else if (requestCode == GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY) {
                //Если она взята из галереи, то получаем URL из данных интента и преобразуем его в путь до файла
                mPhotoUri = data.getData();
            }

            setPhoto(mPhotoUri, mPhoto);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setPhoto(Uri uri, ImageView photo) {
        Bitmap bitmap = BitmapUtils.getBitmap(getActivity().getApplicationContext(), uri);

        if(bitmap == null) return;

        if (bitmap.getWidth() >= photo.getLayoutParams().width) {
            photo.setImageBitmap(BitmapUtils.getScaledBitmap(bitmap,photo.getLayoutParams().width,0));
        } else {
            photo.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnTakePhoto:
                intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(PATH_TO_FILE)));
                intent = Intent.createChooser(intent, getActivity().getApplicationContext().getResources().getString(R.string.profile_add_title));

                if (Utils.isIntentAvailable(getActivity().getApplicationContext(), intent.getAction())) {
                    startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA);
                }
                break;
            case R.id.btnTakeFormGallery:
                intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent = Intent.createChooser(intent, getActivity().getApplicationContext().getResources().getString(R.string.profile_add_title));
                startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY);
                break;
            case R.id.btnSendPhoto:
                if (mPhotoUri != null)
                    sendRequest(mPhotoUri);
                break;
            case R.id.btnClose:
                getDialog().dismiss();
                break;
            default:
                break;
        }
    }

    private void sendRequest(Uri uri) {
        new PhotoAddRequest(uri, getActivity().getApplicationContext()).callback(new DataApiHandler<Photo>() {
            @Override
            protected void success(Photo photo, ApiResponse response) {
                //TODO result callback with photo
                getDialog().dismiss();
            }

            @Override
            protected Photo parseResponse(ApiResponse response) {
                return new Photo(response);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                //TODO result callback with codeError
            }

            @Override
            public void always(ApiResponse response) {
                super.always(response);
            }
        }).exec();
    }
}
