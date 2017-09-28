package com.cashierapp.camera2_test;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    final String TAG = "MainActivity";

    TextureView mTextureView;

    CameraManager mCameraManager;
    CameraDevice mCameraDevice;
    ImageReader mImageReader;
    CameraCaptureSession mCameraCaptureSession;
    CaptureRequest.Builder mPreviewRequestBuilder;
    Surface mPreviewSurface;

    int captureFlag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextureView = (TextureView) findViewById(R.id.textureView);

        mTextureView.setSurfaceTextureListener(this);

        if(mTextureView.isAvailable()){
            openCamera(2560, 1920);
            Log.d(TAG, "available");
        }
        Log.d(TAG, "not available");
    }


    @Override
    protected void onPause() {
        super.onPause();

        mCameraCaptureSession.close();
        mCameraDevice.close();
        mImageReader.close();
    }

    private void openCamera(int width, int height){

        //カメラ取得
        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        //カメラidのリストを取得
        ArrayList<String> backIds = new ArrayList<>();
        try{
            String[] idList = mCameraManager.getCameraIdList();
            int i = 0;
            for(String id : idList){
                CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(id);
                Integer lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if(lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK){
                    backIds.add(id);
                }
                Log.d(TAG, String.valueOf(i));
                i++;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        Log.d(TAG, String.valueOf(backIds.size()));
        

        try{
            mCameraManager.openCamera(backIds.get(0), stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }catch (SecurityException e) {
            e.printStackTrace();
        }

        mImageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(onImageAvailableListener, null);
    }


    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {

            Log.d(TAG, "camera opened");

            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(1280, 720);
            mPreviewSurface = new Surface(texture);

            try {
                cameraDevice.createCaptureSession(Arrays.asList(mPreviewSurface, mImageReader.getSurface()), sessionStateCallback, null);
                Log.d(TAG, "onOpened stateCallback");
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Log.d(TAG, "onOpened fail stateCallback");
            }
            mCameraDevice = cameraDevice;

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

        }
    };



    ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {

            Log.d(TAG, "onImageAvailable");
            Image image = imageReader.acquireLatestImage();

            Log.d(TAG, "image acquired");

            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

            ImageView imageView = (ImageView) findViewById(R.id.image);
            imageView.setImageBitmap(bitmapImage);

            Log.d(TAG, "image acquired");

            if(captureFlag == 0){
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        captureFlag = 1;
                    }
                }, 1000);
            }

            image.close();
        }
    };



    CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            Log.d(TAG, "onConfigured");

            mCameraCaptureSession = cameraCaptureSession;

            //プレビューの準備
            try{
                mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                //オートフォーカスの追加
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                mPreviewRequestBuilder.addTarget(mPreviewSurface);
                mCameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, null);
                Log.d(TAG, "preview available");

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            Log.d(TAG, "onConfigureFailed");
        }
    };

    
    
    CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            
            
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            Log.d(TAG, "onCaptureCompleted");

            try{
                //ここで撮影を行う
                if(captureFlag == 1){
                    CaptureRequest.Builder capture = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    capture.addTarget(mImageReader.getSurface());
                    capture.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    capture.set(CaptureRequest.JPEG_ORIENTATION, 90);
                    capture.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    mCameraCaptureSession.stopRepeating();
                    mCameraCaptureSession.capture(capture.build(), finalCaptureCallback, null);
                    captureFlag = 0;
                }
                Log.d(TAG, "captured");
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };
    


    CameraCaptureSession.CaptureCallback finalCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            try {
                mCameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };







    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

        Log.d(TAG, "onSurfaceTextureAvailable: ");

        openCamera(2560, 1920);
        Log.d(TAG, "available");
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}



