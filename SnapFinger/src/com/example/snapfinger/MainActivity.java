package com.example.snapfinger;

//todo:フィルタ、ゲーム
//surfaceview
//クラス分割

import java.io.File;
import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.jabistudio.androidjhlabs.filter.BumpFilter;
import com.jabistudio.androidjhlabs.filter.ContrastFilter;
import com.jabistudio.androidjhlabs.filter.CrystallizeFilter;
import com.jabistudio.androidjhlabs.filter.GaussianFilter;
import com.jabistudio.androidjhlabs.filter.GlowFilter;
import com.jabistudio.androidjhlabs.filter.MotionBlurFilter;
import com.jabistudio.androidjhlabs.filter.OilFilter;
import com.jabistudio.androidjhlabs.filter.SwimFilter;
import com.jabistudio.androidjhlabs.filter.TwirlFilter;
import com.jabistudio.androidjhlabs.filter.WaterFilter;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;

public class MainActivity extends Activity implements OnItemClickListener,
		SensorEventListener {

	private final static String PHOTOIMG_KEY = "photoImg";
	private final static String PHOTOIMG_KEY_URI = "photoImgUri";
	private Uri mImageUri;
	private Bitmap bmp = null;
	private boolean flag = false;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private SensorManager sensorMgr;
	private Sensor sensor;
	private float accel_max_x;
	private float accel_max_y;
	private float accel_max_z;
	private float gyro_max;
	private float light_value;
	private int width, height;
	private int[] _array;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		} else {
			mImageUri = savedInstanceState.getParcelable(PHOTOIMG_KEY_URI);
			bmp = savedInstanceState.getParcelable(PHOTOIMG_KEY);
		}

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		setupNavigationDrawer();
		ArrayAdapter<String> adapter_start = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1);
		adapter_start.add("カメラ");
		adapter_start.add("画像を開く");
		mDrawerList.setAdapter(adapter_start);
	}

	@Override
	protected void onResume() {
		super.onResume();
		showPhoto();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem menu_filter = (MenuItem) menu.findItem(R.id.action_filter);
		MenuItem menu_game = (MenuItem) menu.findItem(R.id.action_game);

		menu_filter.setVisible(flag);
		menu_game.setVisible(flag);

		return super.onPrepareOptionsMenu(menu);
	}

	private void setupNavigationDrawer() {
		mDrawerList.setOnItemClickListener(this);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {
			@Override
			public void onDrawerClosed(View view) {
			}

			@Override
			public void onDrawerOpened(View drawerView) {
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View parent,
			int position, long id) {
		mDrawerLayout.closeDrawers();
		Configuration config = getResources().getConfiguration();
		if (config.orientation == Configuration.ORIENTATION_LANDSCAPE)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		else if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		selectItem(position);
	}

	private void selectItem(int position) {

		switch (position) {
		case 0:
			takePhoto();
			break;
		case 1:
			callGallery();
			break;
		case 2:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			light_value = SensorManager.LIGHT_SUNLIGHT_MAX;
			GPUImage gpuImage = new GPUImage(this);
			gpuImage.setImage(bmp);
			gpuImage.setFilter(new GPUImageGrayscaleFilter());
			bmp = gpuImage.getBitmapWithFilterApplied();

			showPhoto();
			break;
		case 3:
			GPUImage gpuImage_i = new GPUImage(this);
			gpuImage_i.setImage(bmp);
			gpuImage_i.setFilter(new GPUImageColorInvertFilter());
			bmp = gpuImage_i.getBitmapWithFilterApplied();

			showPhoto();
			break;
		case 5:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			accel_max_z = 0;
			sensor = sensorMgr
					.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			sensorMgr.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_FASTEST);
			AlertDialog.Builder alertDialog_g = new AlertDialog.Builder(this);
			alertDialog_g.setTitle("ガウスぼかし");
			alertDialog_g.setMessage("振れ！");
			alertDialog_g.setCancelable(false);
			alertDialog_g.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							gaussianFiltering();
						}
					});
			alertDialog_g.setCancelable(true);
			AlertDialog alert_g = alertDialog_g.create();
			alert_g.show();
			break;
		case 6:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			gyro_max = 0;
			sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			AlertDialog.Builder alertDialog_t = new AlertDialog.Builder(this);
			alertDialog_t.setTitle("ひねり");
			alertDialog_t.setMessage("回せ！");
			alertDialog_t.setCancelable(false);
			alertDialog_t.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							twirlFiltering();
						}
					});
			alertDialog_t.setCancelable(true);
			AlertDialog alert_t = alertDialog_t.create();
			alert_t.show();
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.action_exit:
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
			alertDialog.setTitle("確認");
			alertDialog.setMessage("アプリを終了しますか？");

			alertDialog.setNegativeButton("はい",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finishActivity();
						}
					});
			alertDialog.setPositiveButton("いいえ",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			alertDialog.setCancelable(true);
			AlertDialog alert = alertDialog.create();
			alert.show();
			return true;
		case R.id.action_game:
			AlertDialog.Builder gameDialog = new AlertDialog.Builder(this);
			gameDialog.setTitle("確認");
			gameDialog.setMessage("ゲームモードへ移行します。");

			gameDialog.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			gameDialog.setPositiveButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			gameDialog.setCancelable(true);
			AlertDialog alert_g = gameDialog.create();
			alert_g.show();
			return true;
		case R.id.action_filter:
			AlertDialog.Builder alertDialog_f = new AlertDialog.Builder(this);
			alertDialog_f.setTitle("確認");
			alertDialog_f.setMessage("フィルタモード(仮)へ移行します。");

			alertDialog_f.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(getApplicationContext(),
									FilterActivity.class);
							intent.putExtra("data", bmp);
							startActivity(intent);
						}
					});
			alertDialog_f.setPositiveButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			alertDialog_f.setCancelable(true);
			AlertDialog alert_f = alertDialog_f.create();
			alert_f.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void finishActivity() {
		this.finish();
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}

	}

	private final static int MY_REQUEST_FOR_PHOTO = 1234;
	private final static int MY_REQUEST_FOR_CALL = 2000;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(PHOTOIMG_KEY_URI, mImageUri);
		outState.putParcelable(PHOTOIMG_KEY, bmp);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		bmp = (Bitmap) savedInstanceState.get(PHOTOIMG_KEY);
		if (bmp != null) {
			if (bmp.getHeight() > 0) {
				flag = true;
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, android.R.id.text1);

				addition(adapter);

				mDrawerList.setAdapter(adapter);
			}
			showPhoto();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public void takePhoto() {
		String filename = System.currentTimeMillis() + ".jpg";
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, filename);
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		mImageUri = getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		Intent intent = new Intent();
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
		startActivityForResult(intent, MY_REQUEST_FOR_PHOTO);
	}

	public void callGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, MY_REQUEST_FOR_CALL);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case MY_REQUEST_FOR_PHOTO:
			if (resultCode == RESULT_OK) {
				Cursor c = getContentResolver().query(mImageUri, null, null,
						null, null);
				c.moveToFirst();
				String filepath = c.getString(c
						.getColumnIndex(MediaStore.MediaColumns.DATA));

				File file = new File(filepath);
				long size = file.length();

				if (size == 0) {
					getContentResolver().delete(mImageUri, null, null);
				} else {
					flag = true;
					try {
						bmp = MediaStore.Images.Media.getBitmap(
								getContentResolver(), mImageUri);
					} catch (IOException e) {
						e.printStackTrace();
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(
							this, android.R.layout.simple_list_item_1,
							android.R.id.text1);

					addition(adapter);

					mDrawerList.setAdapter(adapter);
					showPhoto();
				}
			} else {
				getContentResolver().delete(mImageUri, null, null);
			}
			break;
		case MY_REQUEST_FOR_CALL:
			if (data != null) {
				mImageUri = data.getData();
				flag = true;
				try {
					bmp = MediaStore.Images.Media.getBitmap(
							getContentResolver(), mImageUri);
				} catch (IOException e) {
					e.printStackTrace();
				}
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, android.R.id.text1);
				addition(adapter);
				mDrawerList.setAdapter(adapter);
				showPhoto();
			}
		}
	}

	private void showPhoto() {
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		photoView.setImageBitmap(bmp);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_LIGHT:
			if (event.values[0] < light_value)
				light_value = event.values[0];
			break;
		case Sensor.TYPE_LINEAR_ACCELERATION:
			if (Math.abs(event.values[0]) > accel_max_x)
				accel_max_x = event.values[0];
			if (Math.abs(event.values[1]) > accel_max_y)
				accel_max_y = Math.abs(event.values[1]);
			if (Math.abs(event.values[2]) > accel_max_z)
				accel_max_z = Math.abs(event.values[2]);
			break;
		case Sensor.TYPE_GYROSCOPE:
			if (Math.abs(event.values[2]) > gyro_max)
				gyro_max = event.values[2];
			break;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void addition(ArrayAdapter<String> adapter) {
		adapter.add("カメラ");
		adapter.add("画像を開く");
		adapter.add("グレースケール");// gpu
		adapter.add("反転");// gpu
		adapter.add("セピア");// gpu
		adapter.add("シャープ");// gpu
		adapter.add("レリーフ");
		adapter.add("クリスタル");
		adapter.add("コントラスト");
		adapter.add("輝度逆補正");
		adapter.add("ガウスぼかし");
		adapter.add("ひねり");
		adapter.add("モーション");
		// adapter.add("モーション(平面)");
		// adapter.add("モーション(回転)");
		// adapter.add("モーション(ズーム)");
		adapter.add("水彩");
		adapter.add("油彩");
		adapter.add("スケッチ");// gpu
		adapter.add("グロー");
		adapter.add("陽炎");
		adapter.add("波紋");
		adapter.add("†神威†");
		adapter.add("保存");
		adapter.add("リセット");
		adapter.add("Tweet");
	}

	public void gaussianFiltering() {
		System.out.println(accel_max_z);
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		GaussianFilter filter = new GaussianFilter();
		if (accel_max_z > width / 2 || accel_max_z > height / 2)
			accel_max_z = (height > width) ? height / 2 : width / 2;
		filter.setRadius(accel_max_z);
		_array = filter.filter(_array, width, height);
		bmp = Bitmap.createBitmap(_array, width, height,
				Bitmap.Config.ARGB_8888);
		showPhoto();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void twirlFiltering() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		TwirlFilter filter = new TwirlFilter();
		filter.setCentre((width - 1) / 2, (height - 1) / 2);
		/*
		 * if(gyro_max>width/2 || gyro_max>height/2)
		 * gyro_max=(height>width)?height/2:width/2;
		 */
		filter.setRadius(gyro_max);
		_array = filter.filter(_array, width, height);
		bmp = Bitmap.createBitmap(_array, width, height,
				Bitmap.Config.ARGB_8888);
		showPhoto();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void swimFiltering() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		SwimFilter filter = new SwimFilter();
		filter.setAmount(10);
		/*
		 * if(gyro_max>width/2 || gyro_max>height/2)
		 * gyro_max=(height>width)?height/2:width/2;
		 */
		_array = filter.filter(_array, width, height);
		bmp = Bitmap.createBitmap(_array, width, height,
				Bitmap.Config.ARGB_8888);
		showPhoto();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void waterFiltering() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		WaterFilter filter = new WaterFilter();
		filter.setCentre((width - 1) / 2, (height - 1) / 2);
		/*
		 * if(gyro_max>width/2 || gyro_max>height/2)
		 * gyro_max=(height>width)?height/2:width/2;
		 */
		filter.setRadius(accel_max_z);
		_array = filter.filter(_array, width, height);
		bmp = Bitmap.createBitmap(_array, width, height,
				Bitmap.Config.ARGB_8888);
		showPhoto();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void glowFiltering() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		GlowFilter filter = new GlowFilter();
		filter.setAmount(light_value);
		_array = filter.filter(_array, width, height);
		bmp = Bitmap.createBitmap(_array, width, height,
				Bitmap.Config.ARGB_8888);
		showPhoto();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void suisai() {
		sensorMgr.unregisterListener(this);
		Mat mat = new Mat();
		Mat mat_result = new Mat();
		Utils.bitmapToMat(bmp, mat);
		Imgproc.erode(mat, mat_result, Imgproc.getStructuringElement(
				Imgproc.MORPH_ELLIPSE, new Size(accel_max_z, accel_max_z)));
		Utils.matToBitmap(mat_result, bmp);
		showPhoto();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void gamma() {
		sensorMgr.unregisterListener(this);
		Mat mat = new Mat();
		Mat mat_result = new Mat();
		Utils.bitmapToMat(bmp, mat);
		double gamma;
		/*
		 * double sum=0; double[] rgb; rgb=new double[mat.channels()]; for(int
		 * i=0;i<mat.height();i++){ for(int j=0;j<mat.width();j++){
		 * rgb=mat.get(i, j);
		 * sum+=0.298912*rgb[0]+0.586611*rgb[1]+0.114478*rgb[2]; } } double
		 * average=sum/((double)mat.width()*(double)mat.height());
		 * gamma=Math.log(average)/Math.log(0.5); Mat lut=new
		 * Mat(1,256,CvType.CV_8UC1); lut.setTo(new Scalar(0));
		 * 
		 * for(int i=0;i<256;i++){ double temp=Math.pow((1.0*(double)i/255.0),
		 * 1/gamma); if(temp>1.0) temp=1.0; lut.put(0, i, temp*255.0); }
		 * Core.LUT(mat, lut, mat_result); Utils.matToBitmap(mat_result, bmp);
		 */
		gamma = Math.log(0.5) / Math.log(light_value / 200.0);
		Mat lut = new Mat(1, 256, CvType.CV_8UC1);
		lut.setTo(new Scalar(0));

		for (int i = 0; i < 256; i++) {
			double temp = Math.pow((1.0 * (double) i / 255.0), 1 / gamma);
			if (temp > 1.0)
				temp = 1.0;
			lut.put(0, i, temp * 255.0);
		}
		Core.LUT(mat, lut, mat_result);
		Utils.matToBitmap(mat_result, bmp);
	}

	public void relief() {
		getHeightAndWidth();
		BumpFilter filter = new BumpFilter();
		filter.filter(_array, width, height);
		bmp = Bitmap.createBitmap(_array, width, height,
				Bitmap.Config.ARGB_8888);
		showPhoto();
	}

	public void crystal() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		CrystallizeFilter filter = new CrystallizeFilter();
		filter.setEdgeColor(Color.BLACK);
		// filter.setAmount(getAmout(mSizeValue));
		// filter.setEdgeThickness(getAmout(mEdgeValue));
		// filter.setRandomness(getAmout(mRandomnessValue));
		filter.filter(_array, width, height);
		bmp = Bitmap.createBitmap(_array, width, height,
				Bitmap.Config.ARGB_8888);
		showPhoto();
	}

	public void contrast() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		ContrastFilter filter = new ContrastFilter();
		// filter.setBrightness(getValue(mBrightnessValue));
		// filter.setContrast(getValue(mContrastValue));
		filter.filter(_array, width, height);
		bmp = Bitmap.createBitmap(_array, width, height,
				Bitmap.Config.ARGB_8888);
		showPhoto();
	}

	public void motion() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		MotionBlurFilter filter = new MotionBlurFilter();
		/*
		 * filter.setCentreX(getCenterAndZoom(mCenterXValue));
		 * filter.setCentreY(getCenterAndZoom(mCenterYValue));
		 * filter.setAngle(getAngle(mAngleValue));
		 * filter.setDistance(mDistanceValue);
		 * filter.setRotation(getRotation(mRotationValue));
		 * filter.setZoom(getCenterAndZoom(mZoomValue));
		 */
		filter.filter(_array, width, height);
		bmp = Bitmap.createBitmap(_array, width, height,
				Bitmap.Config.ARGB_8888);
		showPhoto();
	}

	public void oil() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		OilFilter filter = new OilFilter();
		/*
		 * filter.setLevels(mLevelValue); filter.setRange(mRangeValue);
		 */
		filter.filter(_array, width, height);
		bmp = Bitmap.createBitmap(_array, width, height,
				Bitmap.Config.ARGB_8888);
		showPhoto();
	}

	public void kamui() {

	}

	public void save() {
		getContentResolver().delete(mImageUri, null, null);
		
	}

	public void reset() {
		try {
			bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),
					mImageUri);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getHeightAndWidth() {
		width = bmp.getWidth();
		height = bmp.getHeight();
	}
}
