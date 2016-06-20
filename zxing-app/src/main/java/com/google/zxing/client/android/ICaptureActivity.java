package com.google.zxing.client.android;

import android.graphics.Bitmap;
import android.os.Handler;

import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;

/**
 * Created by Alex on 2016/6/17.
 */
public interface ICaptureActivity {

	public CameraManager getCameraManager();

	public Handler getHandler();

	public ViewfinderView getViewfinderView();

	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor);

}
