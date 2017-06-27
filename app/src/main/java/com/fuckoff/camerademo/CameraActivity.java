package com.fuckoff.camerademo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import pub.devrel.easypermissions.EasyPermissions;

public class CameraActivity extends Activity implements View.OnClickListener {

    private Camera mCamera;
    private CameraPreview mPreview;
    private ImageView imageView;
    private FrameLayout imageViewContainer;
    private Button saveButton, cancelButton;
    private ImageView captureImg;
    private byte[] mData;
    private MyOrientation myOrientation;

    private String TAG = "CameraActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        Camera.Parameters prams = mCamera.getParameters();
        prams.setJpegQuality(100);
        prams.setPictureSize(1920, 1080);
        mCamera.setParameters(prams);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        LinearLayout preview = (LinearLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        preview.setKeepScreenOn(true);
        preview.setFocusable(true);

        // Add a listener to the Capture button
        captureImg = findViewById(R.id.image_capture);
        imageView = findViewById(R.id.image_preview);
        imageViewContainer = findViewById(R.id.image_preview_container);
        saveButton = findViewById(R.id.button_save);
        cancelButton = findViewById(R.id.button_cancel);

        captureImg.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        myOrientation = new MyOrientation(this);

    }

    @Override
    protected void onResume() {
        myOrientation.enable();
        super.onResume();
    }

    @Override
    protected void onPause() {
        myOrientation.disable();
        super.onPause();
        //releaseCamera();
    }

    @Override
    public void startActivityForResult(@RequiresPermission Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mData = data;

            Bitmap picBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            imageView.setImageBitmap(picBitmap);
            imageViewContainer.setVisibility(View.VISIBLE);
        }
    };

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


    /** 检查设备是否存在照相机 */
    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** 一种安全的方式获取Cameer对象的实例. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private void savePicture(byte[] data){
        File mediaStorageDir =
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CameraDemo");

            // Create the storage directory if it does not exist
            if (! mediaStorageDir.exists()){
                if (! mediaStorageDir.mkdirs()){
                    Log.d("MyCameraApp", "failed to create directory");
                    return;
                }
            }

            File pictureFile = new File(mediaStorageDir.getPath()+File.separator+"IMG_"+System.currentTimeMillis()+".jpeg");
            Log.i("img", pictureFile.getAbsolutePath());

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.image_capture:
                mCamera.takePicture(null, null, mPicture);
                break;
            case R.id.button_save:
                if (null != this.mData){
                    this.savePicture(this.mData);
                    this.mData = null;
                    this.imageViewContainer.setVisibility(View.GONE);
                    return;
                }
                Log.i(TAG, "保存图片出错了！");
                break;
            case R.id.button_cancel:
                this.mData = null;
                this.imageViewContainer.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                if (this.imageViewContainer.getVisibility()!=View.GONE) {
                    this.imageViewContainer.setVisibility(View.GONE);
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class MyOrientation extends OrientationEventListener {
        public MyOrientation(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int i) {
            Log.i("orientation", i+"");

            if (mCamera == null){
                Log.i(TAG, "mCamera == null");
                return;
            }
            // 设置照骗的rotation，保持与人眼一致。可能会有bug。
            Camera.Parameters prams = mCamera.getParameters();
            prams.setRotation(0);
            if (i>315 || i<=45){
                prams.setRotation(90);
            } else if (i>=45 && i<=135){

            } else if (i>135 && i<=225){
                prams.setRotation(90);
            } else {

            }
            mCamera.setParameters(prams);
        }
    }

}
