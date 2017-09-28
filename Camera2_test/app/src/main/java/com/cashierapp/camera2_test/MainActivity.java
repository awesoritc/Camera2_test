package com.cashierapp.camera2_test;


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
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    //TextureView mTextureView;
    String cameraId;
    AutoFitTextureView mTextureView;
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

            //CaptureSessionを生成する
            cameraDevice.createCaptureSession(Arrays.asList(mPreviewSurface, mImageReader.getSurface()), mSessionCallback, null);

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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

    }


    @Override
    protected void onResume() {
        super.onResume();


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
        

    }


    @Override
    protected void onPause() {
        super.onPause();
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
}
