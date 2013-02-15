package com.topface.topface.ui.fragments;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.*;
import com.topface.topface.R;
import com.topface.topface.ui.analytics.TrackedDialogFragment;
import com.topface.topface.utils.Debug;

public class TakePhotoDialog extends TrackedDialogFragment implements SurfaceHolder.Callback, Camera.PictureCallback{

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;

    public static TakePhotoDialog newInstance() {
        return new TakePhotoDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_take_photo, container, false);

        mSurfaceView = (SurfaceView) root.findViewById(R.id.svPreview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCamera = Camera.open();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null)
        {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

    }
}
