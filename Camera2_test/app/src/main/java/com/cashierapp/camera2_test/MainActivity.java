package com.cashierapp.camera2_test;


import android.Manifest;
import android.content.pm.PackageManager;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";
/*
    //TextureView mTextureView;
    String cameraId;
    //AutoFitTextureView mTextureView;
    ImageReader mImageReader;
    CameraDevice mCameraDevice;
    CameraCaptureSession mCaptureSession;
    Surface mPreviewSurface;

    ImageReader.OnImageAvailableListener mTakePictureAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {

            //Surfaceから画像が利用できるようになった時に呼び出される

        }
    };

    CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {

            //SurfaceTextureにプレビューサイズを設定し、プレビュー用のSurfaceを生成する。
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(1280, 720);
            mPreviewSurface = new Surface(texture);


            try {

                //CaptureSessionを生成する
                cameraDevice.createCaptureSession(Arrays.asList(mPreviewSurface, mImageReader.getSurface()), mSessionCallback, null);


            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            //パラメーターのcameraDeviceを保持しておく
            mCameraDevice = cameraDevice;

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

        }
    };


    CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

            //パラメーターのCameraCaptureSessionを保持しておきます
            mCaptureSession = cameraCaptureSession;
        }
    };


    CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);

            //撮影開始のタイミングで呼ばれる
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            //撮影完了のタイミングで呼ばれる
        }
    };


*/


    private CameraDevice backCameraDevice;
    private CameraCaptureSession backCameraSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*

        //準備
        //カメラのオープン

        mTextureView = (AutoFitTextureView) findViewById(R.id.texture);


        //CameraManagerのインスタンスを取得
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        //オープンするカメラのIDを決める
        cameraId = getBackCameraIds(cameraManager).get(0);

        //ImageReaderを生成
        mImageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 3);
        mImageReader.setOnImageAvailableListener(mTakePictureAvailableListener, null);

        //CameraManagerにopen要求を出す
        try {
            cameraManager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e){
            e.printStackTrace();
        }


*/


        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {

            String backCameraId ="";
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics chars = manager.getCameraCharacteristics(cameraId);
                Integer facing = chars.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    backCameraId = cameraId;
                }
            }

            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface);
            surfaceView.getHolder().setFixedSize(640, 320);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(backCameraId, openCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
/*

        //プレビューの開始
        try {

            //プレビュー用のCaptureRequsest.Builderを生成します
            CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            //CaptureRequest.Builder用にプレビュー用のSurfaceを設定します
            captureBuilder.addTarget(mPreviewSurface);

            //必要なパラメーターを設定します(サンプル)
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            //プレビューを開始します
            mCaptureSession.setRepeatingRequest(captureBuilder.build(), null, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }



        //撮影
        try{

            //撮影用のCaptureRequsest.Builderを生成します
            CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            //CaptureRequest.Builder用に撮影用のSurfaceを設定します
            captureBuilder.addTarget(mPreviewSurface);

            //必要なパラメーターを設定します(サンプル)
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);

            //プレビューを開始します
            mCaptureSession.stopRepeating();

            //撮影を開始します
            final CaptureRequest.Builder cap = captureBuilder;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //撮影を開始します
                    try {
                        mCaptureSession.capture(cap.build(), mCaptureCallback, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }, 5000);

            //撮影を開始します
            //mCaptureSession.capture(captureBuilder.build(), mCaptureCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
*/


    }


    @Override
    protected void onPause() {
        super.onPause();
/*
        mCaptureSession.close();
        mCameraDevice.close();
        mImageReader.close();

*/

        //カメラセッションの終了
        if(backCameraSession != null){
            try{
                backCameraSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            backCameraSession.close();
        }

        //カメラデバイスとの切断
        if(backCameraDevice != null){
            backCameraDevice.close();
        }
    }


    public List<String> getBackCameraIds(CameraManager cameraManager){

        ArrayList<String> backIds = new ArrayList<>();

        try{
            String[] idList = cameraManager.getCameraIdList();
            for(String id : idList){
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK){
                    //背面カメラならListに追加
                    backIds.add(id);
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return backIds;
    }



    CameraDevice.StateCallback openCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            backCameraDevice = cameraDevice;

            //プレビュー用のSurfaceViewをリストに登録
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface);
            ArrayList<Surface> surfaceList = new ArrayList<>();
            surfaceList.add(surfaceView.getHolder().getSurface());

            try{
                //プレビューリクエストの設定(SurfaceViewをターゲットに)
                mPreviewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(surfaceView.getHolder().getSurface());

                //キャプチャーセッションの開始
                cameraDevice.createCaptureSession(surfaceList, captureSessionStateCallback, null );
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

        }
    };


    CameraCaptureSession.StateCallback captureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

            backCameraSession = cameraCaptureSession;

            try{
                //オートフォーカスの設定
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

                //プレビューの開始
                backCameraSession.setRepeatingRequest(mPreviewRequestBuilder.build(), captureCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

        }
    };


    CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
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
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);

            Log.d(TAG, "onCaptureFailed: ");
        }
    };
}



