package alex.liyzay.zxing;

import android.Manifest.permission;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.client.android.CaptureHandler;
import com.google.zxing.client.android.ICaptureActivity;
import com.google.zxing.client.android.ViewfinderView;
import com.google.zxing.client.android.camera.CameraManager;

import java.util.Collection;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Callback,ICaptureActivity {
	private final String TAG = "MainActivity" ;
	private final int CODE_CAMEARA_PERMISSION = 10 ;

	private CameraManager cameraManager;
	private ViewfinderView viewfinderView;
	private CaptureHandler handler;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet ="UTF-8" ;
	private BeepManager beepManager;

	private SurfaceHolder mSurfaceHolder ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);

		beepManager = new BeepManager(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if(!hasPermission(this,permission.CAMERA)){
			ActivityCompat.requestPermissions(this,new String[]{permission.CAMERA},CODE_CAMEARA_PERMISSION);
			return;
		}

		cameraManager = new CameraManager(getApplication());
		viewfinderView.setCameraManager(cameraManager);
		if(mSurfaceHolder!=null){
			initCamera(mSurfaceHolder);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		if(cameraManager!=null){
			cameraManager.closeDriver();
			cameraManager = null ;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if(requestCode==CODE_CAMEARA_PERMISSION){
			if(!hasPermission(this,permission.CAMERA)){
				Toast.makeText(getApplicationContext(),"Permission denied",Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mSurfaceHolder = holder ;
		if(hasPermission(this,permission.CAMERA)){
			initCamera(holder);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mSurfaceHolder = null ;
	}

	@Override
	public CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	public Handler getHandler() {
		return handler ;
	}

	@Override
	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	@Override
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		boolean fromLiveScan = barcode != null;
		if (fromLiveScan) {
			beepManager.playBeepSoundAndVibrate();
		}
//		restartPreviewAfterDelay(1000L);
		Log.d(TAG,"result:"+rawResult.getText());
		displayResult(rawResult.getText());
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a RuntimeException.
			if (handler == null) {
				handler = new CaptureHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
			}
		} catch (Exception e) {
			displayFrameworkBugMessageAndExit();
		}
	}

	void displayResult(String text){
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(R.string.msg_sbc_results);
		builder.setMessage(text);
		builder.setPositiveButton(R.string.button_ok, null);
		builder.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				restartPreviewAfterDelay(500L);
			}
		});
		builder.create().show();
	}

	void displayFrameworkBugMessageAndExit(){
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(R.string.msg_error);
		builder.setMessage(R.string.msg_camera_framework_bug);
		builder.setPositiveButton(R.string.button_ok, null);
		builder.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				finish();
			}
		});
		builder.create().show();
	}

	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(CaptureHandler.MESSAGE_PREVIEW, delayMS);
		}
	}

	public static boolean hasPermission(Context context,String permission){
		return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED ;
	}
}
