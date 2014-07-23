package com.example.snapfinger;

//todo:フィルタ、ゲーム
//回転時、ドロワーとアクションバーの情報維持

import java.io.File;
import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
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

import com.jabistudio.androidjhlabs.filter.GaussianFilter;
import com.jabistudio.androidjhlabs.filter.TwirlFilter;
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
	private float accel_max;
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

		MenuItem menu_game = (MenuItem) menu.findItem(R.id.action_game);

		if (flag) {
			menu_game.setVisible(true);
		} else {
			menu_game.setVisible(false);
		}

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
			_array = AndroidUtils.bitmapToIntArray(bmp);
			accel_max = 0;
			sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
		case 4:
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

	public void gaussianFiltering() {
		sensorMgr.unregisterListener(this);
		width = bmp.getWidth();
		height = bmp.getHeight();
		GaussianFilter filter = new GaussianFilter();
		if (accel_max > width / 2 || accel_max > height / 2)
			accel_max = (height > width) ? height / 2 : width / 2;
		filter.setRadius(accel_max);
		_array = filter.filter(_array, width, height);
		bmp = Bitmap.createBitmap(_array, width, height,
				Bitmap.Config.ARGB_8888);
		showPhoto();
	}

	public void twirlFiltering() {
		sensorMgr.unregisterListener(this);
		width = bmp.getWidth();
		height = bmp.getHeight();
		TwirlFilter tfilter = new TwirlFilter();
		tfilter.setCentre((width - 1) / 2, (height - 1) / 2);
		/*
		 * if(gyro_max>width/2 || gyro_max>height/2)
		 * gyro_max=(height>width)?height/2:width/2;
		 */
		tfilter.setRadius(gyro_max);
		_array = tfilter.filter(_array, width, height);
		bmp = Bitmap.createBitmap(_array, width, height,
				Bitmap.Config.ARGB_8888);
		showPhoto();
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

				adapter.add("カメラ");
				adapter.add("画像を開く");
				adapter.add("グレースケール");
				adapter.add("ガウスぼかし");
				adapter.add("ひねり");
				adapter.add("†神威†");
				adapter.add("保存");

				mDrawerList.setAdapter(adapter);
			}
			showPhoto();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public void takePhoto(View view) {
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

	public void callGallery(View view) {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, MY_REQUEST_FOR_CALL);
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
				// mImageUri=data.getData();
				// data.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
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

					adapter.add("カメラ");
					adapter.add("画像を開く");
					adapter.add("グレースケール");
					adapter.add("ガウスぼかし");
					adapter.add("ひねり");
					adapter.add("†神威†");
					adapter.add("保存");

					mDrawerList.setAdapter(adapter);
					showPhoto();
				}
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

				adapter.add("カメラ");
				adapter.add("画像を開く");
				adapter.add("グレースケール");
				adapter.add("ガウスぼかし");
				adapter.add("ひねり");
				adapter.add("†神威†");
				adapter.add("保存");

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
		case Sensor.TYPE_ACCELEROMETER:
			if (event.values[0] > accel_max)
				accel_max = event.values[0];
			if (event.values[1] > accel_max)
				accel_max = event.values[1];
			if (event.values[2] > accel_max)
				accel_max = event.values[2];
			break;
		case Sensor.TYPE_GYROSCOPE:
			if (event.values[0] > gyro_max)
				gyro_max = event.values[0];
			if (event.values[1] > gyro_max)
				gyro_max = event.values[1];
			if (event.values[2] > gyro_max)
				gyro_max = event.values[2];
			break;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
