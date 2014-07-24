package com.example.snapfinger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToonFilter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
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
import com.jabistudio.androidjhlabs.filter.Kernel;
import com.jabistudio.androidjhlabs.filter.MotionBlurFilter;
import com.jabistudio.androidjhlabs.filter.OilFilter;
import com.jabistudio.androidjhlabs.filter.SwimFilter;
import com.jabistudio.androidjhlabs.filter.TwirlFilter;
import com.jabistudio.androidjhlabs.filter.WaterFilter;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;

public class MainActivity extends Activity implements OnItemClickListener,
		SensorEventListener {

	private final static String PHOTOIMG_KEY = "photoImg";
	private final static String ORIGINAL_IMAGE_KEY = "origin";
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
	private Bitmap bmp_origin = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
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

	/*
	 * adapter.add("カメラ"); adapter.add("画像を開く"); adapter.add("グレースケール");// gpu
	 * adapter.add("反転");// gpu adapter.add("セピア");// gpu adapter.add("シャープ");//
	 * gpu adapter.add("レリーフ"); adapter.add("クリスタル"); adapter.add("コントラスト");
	 * adapter.add("輝度逆補正"); adapter.add("ガウスぼかし"); adapter.add("ひねり");
	 * adapter.add("モーション"); // adapter.add("モーション(平面)"); //
	 * adapter.add("モーション(回転)"); // adapter.add("モーション(ズーム)");
	 * adapter.add("水彩"); adapter.add("油彩"); adapter.add("スケッチ");// gpu
	 * adapter.add("グロー"); adapter.add("陽炎"); adapter.add("波紋");
	 * adapter.add("†神威†"); adapter.add("保存"); adapter.add("リセット");
	 * adapter.add("オリジナル"); adapter.add("Tweet");
	 */

	private void selectItem(int position) {
		setBitmap();
		switch (position) {
		case 0:
			takePhoto();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			break;
		case 1:
			callGallery();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			break;
		case 2:
			GPUImage gpuImage = new GPUImage(this);
			gpuImage.setImage(bmp);
			gpuImage.setFilter(new GPUImageGrayscaleFilter());
			bmp = gpuImage.getBitmapWithFilterApplied();
			showPhoto();
			try {
				File file = getFileStreamPath("temp.jpg");
				if (file.exists())
					file.delete();

				FileOutputStream out = openFileOutput("temp.jpg",
						Context.MODE_PRIVATE);
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			break;
		case 3:
			GPUImage gpuImage_i = new GPUImage(this);
			gpuImage_i.setImage(bmp);
			gpuImage_i.setFilter(new GPUImageColorInvertFilter());
			bmp = gpuImage_i.getBitmapWithFilterApplied();
			showPhoto();
			try {
				File file = getFileStreamPath("temp.jpg");
				if (file.exists())
					file.delete();

				FileOutputStream out = openFileOutput("temp.jpg",
						Context.MODE_PRIVATE);
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			break;
		case 4:
			GPUImage gpuImage_s = new GPUImage(this);
			gpuImage_s.setImage(bmp);
			gpuImage_s.setFilter(new GPUImageSepiaFilter());
			bmp = gpuImage_s.getBitmapWithFilterApplied();

			showPhoto();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			break;
		case 5:
			sharp();
			break;
		case 6:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			relief();
			break;
		case 7:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			accel_max_z = 0;
			sensor = sensorMgr
					.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			sensorMgr.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_FASTEST);
			AlertDialog.Builder alertDialog_cry = new AlertDialog.Builder(this);
			alertDialog_cry.setTitle("クリスタル");
			alertDialog_cry.setMessage("振れ！");
			alertDialog_cry.setCancelable(false);
			alertDialog_cry.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							crystal();
						}
					});
			alertDialog_cry.setCancelable(true);
			AlertDialog alert_cry = alertDialog_cry.create();
			alert_cry.setCancelable(false);
			alert_cry.show();
			break;
		case 8:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			accel_max_z = 0;
			sensor = sensorMgr
					.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			sensorMgr.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_FASTEST);
			AlertDialog.Builder alertDialog_con = new AlertDialog.Builder(this);
			alertDialog_con.setTitle("コントラスト");
			alertDialog_con.setMessage("照らせ！");
			alertDialog_con.setCancelable(false);
			alertDialog_con.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							contrast();
						}
					});
			alertDialog_con.setCancelable(true);
			AlertDialog alert_con = alertDialog_con.create();
			alert_con.setCancelable(false);
			alert_con.show();

			break;
		case 9:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			light_value = SensorManager.LIGHT_SUNLIGHT_MAX;
			sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT);
			sensorMgr.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_FASTEST);
			AlertDialog.Builder alertDialog_gamma = new AlertDialog.Builder(
					this);
			alertDialog_gamma.setTitle("輝度逆調整");
			alertDialog_gamma.setMessage("周囲の光量を利用します。");
			alertDialog_gamma.setCancelable(false);
			alertDialog_gamma.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							gamma();
						}
					});
			alertDialog_gamma.setCancelable(true);
			AlertDialog alert_gamma = alertDialog_gamma.create();
			alert_gamma.setCancelable(false);
			alert_gamma.show();
			break;
		case 10:
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
			alert_g.setCancelable(false);
			alert_g.show();
			break;
		case 11:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			gyro_max = 0;
			sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			sensorMgr.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_FASTEST);
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
			alert_t.setCancelable(false);
			alert_t.show();
			break;
		case 12:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			gyro_max = 0;
			accel_max_x = 0;
			accel_max_y = 0;
			accel_max_z = 0;
			sensorMgr
					.registerListener(this, sensorMgr
							.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
							SensorManager.SENSOR_DELAY_FASTEST);
			sensorMgr.registerListener(this,
					sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
					SensorManager.SENSOR_DELAY_FASTEST);
			AlertDialog.Builder alertDialog_motion = new AlertDialog.Builder(
					this);
			alertDialog_motion.setTitle("モーションブラシ");
			alertDialog_motion.setMessage("振り回せ！");
			alertDialog_motion.setCancelable(false);
			alertDialog_motion.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							motion();
						}
					});
			alertDialog_motion.setCancelable(true);
			AlertDialog alert_motion = alertDialog_motion.create();
			alert_motion.setCancelable(false);
			alert_motion.show();

			break;
		case 13:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			accel_max_z = 0;
			sensor = sensorMgr
					.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			sensorMgr.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_FASTEST);
			AlertDialog.Builder alertDialog_suisai = new AlertDialog.Builder(
					this);
			alertDialog_suisai.setTitle("水彩画");
			alertDialog_suisai.setMessage("振れ！");
			alertDialog_suisai.setCancelable(false);
			alertDialog_suisai.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							suisai();
						}
					});
			alertDialog_suisai.setCancelable(true);
			AlertDialog alert_suisai = alertDialog_suisai.create();
			alert_suisai.setCancelable(false);
			alert_suisai.show();
			break;
		case 14:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			accel_max_z = 0;
			sensor = sensorMgr
					.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			sensorMgr.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_FASTEST);
			AlertDialog.Builder alertDialog_oil = new AlertDialog.Builder(this);
			alertDialog_oil.setTitle("油彩画");
			alertDialog_oil.setMessage("振れ！");
			alertDialog_oil.setCancelable(false);
			alertDialog_oil.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							oil();
						}
					});
			alertDialog_oil.setCancelable(true);
			AlertDialog alert_oil = alertDialog_oil.create();
			alert_oil.setCancelable(false);
			alert_oil.show();
			break;
		case 15:
			// sketch
			GPUImage gpuImage_sketch = new GPUImage(this);
			gpuImage_sketch.setImage(bmp);
			gpuImage_sketch.setFilter(new GPUImageToonFilter());
			bmp = gpuImage_sketch.getBitmapWithFilterApplied();

			showPhoto();
			try {
				File file = getFileStreamPath("temp.jpg");
				if (file.exists())
					file.delete();

				FileOutputStream out = openFileOutput("temp.jpg",
						Context.MODE_PRIVATE);
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			break;
		case 16:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			light_value = SensorManager.LIGHT_SUNLIGHT_MAX;
			sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT);
			sensorMgr.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_FASTEST);
			AlertDialog.Builder alertDialog_glow = new AlertDialog.Builder(this);
			alertDialog_glow.setTitle("グロー");
			alertDialog_glow.setMessage("照らせ！");
			alertDialog_glow.setCancelable(false);
			alertDialog_glow.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							glowFiltering();
						}
					});
			alertDialog_glow.setCancelable(true);
			AlertDialog alert_glow = alertDialog_glow.create();
			alert_glow.setCancelable(false);
			alert_glow.show();
			break;
		case 17:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			light_value = SensorManager.LIGHT_SUNLIGHT_MAX;
			sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT);
			sensorMgr.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_FASTEST);
			AlertDialog.Builder alertDialog_swim = new AlertDialog.Builder(this);
			alertDialog_swim.setTitle("陽炎");
			alertDialog_swim.setMessage("照らせ！");
			alertDialog_swim.setCancelable(false);
			alertDialog_swim.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							swimFiltering();
						}
					});
			alertDialog_swim.setCancelable(true);
			AlertDialog alert_swim = alertDialog_swim.create();
			alert_swim.setCancelable(false);
			alert_swim.show();
			break;
		case 18:
			_array = AndroidUtils.bitmapToIntArray(bmp);
			accel_max_z = 0;
			sensor = sensorMgr
					.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			sensorMgr.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_FASTEST);
			AlertDialog.Builder alertDialog_w = new AlertDialog.Builder(this);
			alertDialog_w.setTitle("波紋");
			alertDialog_w.setMessage("振れ！");
			alertDialog_w.setCancelable(false);
			alertDialog_w.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							waterFiltering();
						}
					});
			alertDialog_w.setCancelable(true);
			AlertDialog alert_w = alertDialog_w.create();
			alert_w.setCancelable(false);
			alert_w.show();
			break;
		case 19:
			kamui();
			break;
		case 20:
			AlertDialog.Builder alertDialog_save = new AlertDialog.Builder(this);
			alertDialog_save.setTitle("確認");
			alertDialog_save.setMessage("画像を保存します。以前の画像は上書きされます。");
			alertDialog_save.setCancelable(false);
			alertDialog_save.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							save();
						}
					});
			alertDialog_save.setPositiveButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			alertDialog_save.setCancelable(true);
			AlertDialog alert_save = alertDialog_save.create();
			alert_save.setCancelable(false);
			alert_save.show();
			break;
		case 21:
			AlertDialog.Builder alertDialog_reset = new AlertDialog.Builder(
					this);
			alertDialog_reset.setTitle("確認");
			alertDialog_reset.setMessage("画像をリセットします。");
			alertDialog_reset.setCancelable(false);
			alertDialog_reset.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							reset();
						}
					});
			alertDialog_reset.setPositiveButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			alertDialog_reset.setCancelable(true);
			AlertDialog alert_reset = alertDialog_reset.create();
			alert_reset.setCancelable(false);
			alert_reset.show();
			break;
		case 22:
			AlertDialog.Builder alertDialog_origin = new AlertDialog.Builder(
					this);
			alertDialog_origin.setTitle("確認");
			alertDialog_origin.setMessage("画像をリセットします。");
			alertDialog_origin.setCancelable(false);
			alertDialog_origin.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							setOrigin();
						}
					});
			alertDialog_origin.setPositiveButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			alertDialog_origin.setCancelable(true);
			AlertDialog alert_origin = alertDialog_origin.create();
			alert_origin.setCancelable(false);
			alert_origin.show();
			break;
		case 23:
			motion();
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
							clean();
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
		File file_temp = getFileStreamPath("temp.jpg");
		if (file_temp.exists()) {
			InputStream in;
			try {
				in = openFileInput("temp.jpg");
				bmp = BitmapFactory.decodeStream(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
			file_temp.delete();
		}
		outState.putParcelable(PHOTOIMG_KEY, bmp);
		outState.putParcelable(ORIGINAL_IMAGE_KEY, bmp_origin);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		bmp = (Bitmap) savedInstanceState.get(PHOTOIMG_KEY);
		mImageUri = (Uri) savedInstanceState.get(PHOTOIMG_KEY_URI);
		bmp_origin = (Bitmap) savedInstanceState.get(ORIGINAL_IMAGE_KEY);
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
					bmp_origin = bmp;
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
				bmp_origin = bmp;
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, android.R.id.text1);
				addition(adapter);
				mDrawerList.setAdapter(adapter);
				showPhoto();
			}
		}
	}

	private void showPhoto() {
		if (bmp != null)
			if (bmp != null) {
				ImageView photoView = (ImageView) findViewById(R.id.photo_view);
				photoView.setImageBitmap(bmp);
			}
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
		adapter.add("オリジナル");
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
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		ImageProcessing task = new ImageProcessing(photoView, filter, _array,
				width, height);
		// task.setContentResolver(getContentResolver());
		task.execute(1);
		task.setMainActivity(this);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void twirlFiltering() {
		sensorMgr.unregisterListener(this);
		System.out.println(gyro_max);
		getHeightAndWidth();
		TwirlFilter filter = new TwirlFilter();
		/*
		 * if(gyro_max>width/2 || gyro_max>height/2)
		 * gyro_max=(height>width)?height/2:width/2;
		 */
		float temp = Math.max(width, height);
		filter.setCentre(0.5f,0.5f);
		filter.setAngle(gyro_max/2);
		filter.setRadius(temp);
		filter.setEdgeAction(TwirlFilter.CLAMP);
		filter.setInterpolation(TwirlFilter.NEAREST_NEIGHBOUR);
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		ImageProcessing task_twi = new ImageProcessing(photoView, filter, _array,
				width, height);
		task_twi.setMainActivity(this);
		// task.setContentResolver(getContentResolver());
		task_twi.execute(2);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void swimFiltering() {
		sensorMgr.unregisterListener(this);
		System.out.println(light_value);
		getHeightAndWidth();
		SwimFilter filter = new SwimFilter();
		float temp = light_value/10;
		filter.setAmount(temp);
		filter.setEdgeAction(SwimFilter.CLAMP);
		filter.setInterpolation(SwimFilter.NEAREST_NEIGHBOUR);
		filter.setScale(temp*3);
		filter.setStretch(temp/2);
		filter.setTurbulence(1.0f);
		filter.setTime(light_value*10);
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		ImageProcessing task_swi = new ImageProcessing(photoView, filter, _array,
				width, height);
		task_swi.setMainActivity(this);
		// task.setContentResolver(getContentResolver());
		task_swi.execute(3);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void waterFiltering() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		WaterFilter filter = new WaterFilter();
		filter.setCentre(0.5f,0.5f);
		float temp = Math.max(width, height);
		filter.setRadius(temp);
		filter.setAmplitude(0.8f);
		filter.setEdgeAction(WaterFilter.CLAMP);
		filter.setPhase(accel_max_z/3);
		filter.setWavelength(accel_max_z*5);
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		ImageProcessing task_wat = new ImageProcessing(photoView, filter, _array,
				width, height);
		task_wat.setMainActivity(this);
		// task.setContentResolver(getContentResolver());
		task_wat.execute(4);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void glowFiltering() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		GlowFilter filter = new GlowFilter();
		filter.setAmount(0.15f);
		System.out.println(light_value);
		filter.setRadius(light_value / 100);
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		ImageProcessing task_glo = new ImageProcessing(photoView, filter, _array,
				width, height);
		task_glo.setMainActivity(this);
		// task.setContentResolver(getContentResolver());
		task_glo.execute(5);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void suisai() {
		sensorMgr.unregisterListener(this);
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		ImageProcessing_mat task = new ImageProcessing_mat(photoView);
		// task.setContentResolver(getContentResolver());
		task.setFlag(2);
		task.setMainActivity(this);
		task.execute(bmp);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void gamma() {
		sensorMgr.unregisterListener(this);
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		ImageProcessing_mat task = new ImageProcessing_mat(photoView);
		// task.setContentResolver(getContentResolver());
		task.setFlag(1);
		task.setMainActivity(this);
		task.execute(bmp);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void sharp() {
		GPUImage gpuImage_sharp = new GPUImage(this);
		gpuImage_sharp.setImage(bmp);
		gpuImage_sharp.setFilter(new GPUImageSharpenFilter(3.0f));
		bmp = gpuImage_sharp.getBitmapWithFilterApplied();

		showPhoto();
		try {
			File file = getFileStreamPath("temp.jpg");
			if (file.exists())
				file.delete();

			FileOutputStream out = openFileOutput("temp.jpg",
					Context.MODE_PRIVATE);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void relief() {
		getHeightAndWidth();
		BumpFilter filter = new BumpFilter();
		filter.setEdgeAction(BumpFilter.CLAMP_EDGES);
		
		float[] embossMatrix = {
				-2.0f, -1.0f,  0.0f,
				-1.0f,  1.0f,  1.0f,
				 0.0f,  1.0f,  2.0f
			};
		Kernel kernel=new Kernel(3, 3, embossMatrix);
		filter.setKernel(kernel);
		System.out.println(kernel.toString());
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		ImageProcessing task_rel = new ImageProcessing(photoView, filter, _array,
				width, height);
		task_rel.setMainActivity(this);
		// task.setContentResolver(getContentResolver());
		task_rel.execute(6);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void crystal() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		CrystallizeFilter filter = new CrystallizeFilter();
		filter.setEdgeColor(Color.BLACK);
		//filter.setAmount(0.5f);
		filter.setAmount(accel_max_z);
		filter.setEdgeThickness(0.4f);
		filter.setRandomness(0.7f);
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		ImageProcessing task_cry = new ImageProcessing(photoView, filter, _array,
				width, height);
		task_cry.setMainActivity(this);
		// task.setContentResolver(getContentResolver());
		task_cry.execute(7);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void contrast() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		ContrastFilter filter = new ContrastFilter();
		filter.setBrightness(light_value/10);
		filter.setContrast(light_value/10);
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		ImageProcessing task_con = new ImageProcessing(photoView, filter, _array,
				width, height);
		task_con.setMainActivity(this);
		// task.setContentResolver(getContentResolver());
		task_con.execute(8);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void motion() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		MotionBlurFilter filter = new MotionBlurFilter();
		double sita=Math.atan2(accel_max_y, accel_max_x);
		sita=sita/Math.PI;
		filter.setAngle((float)sita);
		float max=Math.max(accel_max_x, accel_max_y);
		max=Math.max(max, accel_max_z);
		max=Math.max(max, gyro_max);
		filter.setDistance(max);
		filter.setRotation(gyro_max);
		filter.setZoom(accel_max_z);
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		ImageProcessing task_mot = new ImageProcessing(photoView, filter, _array,
				width, height);
		task_mot.setMainActivity(this);
		// task.setContentResolver(getContentResolver());
		task_mot.execute(9);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void oil() {
		sensorMgr.unregisterListener(this);
		getHeightAndWidth();
		OilFilter filter = new OilFilter();
		filter.setLevels((int)accel_max_z/2);
		filter.setRange((int)accel_max_z/2);
		/*
		 * filter.setLevels(mLevelValue); filter.setRange(mRangeValue);
		 */
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		ImageProcessing task_oil = new ImageProcessing(photoView, filter, _array,
				width, height);
		task_oil.setMainActivity(this);
		// task.setContentResolver(getContentResolver());
		task_oil.execute(10);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void kamui() {

	}

	public void save() {
		File file_temp = getFileStreamPath("temp.jpg");
		if (file_temp.exists()) {
			InputStream in;
			try {
				in = openFileInput("temp.jpg");
				bmp = BitmapFactory.decodeStream(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
			file_temp.delete();
		}

		Cursor c = getContentResolver()
				.query(mImageUri, null, null, null, null);
		c.moveToFirst();
		String filepath = c.getString(c
				.getColumnIndex(MediaStore.MediaColumns.DATA));
		getContentResolver().delete(mImageUri, null, null);
		File file = new File(filepath);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			bmp.compress(CompressFormat.JPEG, 100, fos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, file.getName());
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		values.put(MediaStore.Images.Media.DATA, filepath);
		getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void reset() {
		try {
			bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),
					mImageUri);
		} catch (IOException e) {
			e.printStackTrace();
		}
		File file_temp = getFileStreamPath("temp.jpg");
		if (file_temp.exists())
			file_temp.delete();
		showPhoto();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void setOrigin() {
		bmp = bmp_origin;
		File file_temp = getFileStreamPath("temp.jpg");
		if (file_temp.exists())
			file_temp.delete();
		showPhoto();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public void setBitmap() {
		File file_temp = getFileStreamPath("temp.jpg");
		if (file_temp.exists()) {
			InputStream in;
			try {
				in = openFileInput("temp.jpg");
				bmp = BitmapFactory.decodeStream(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
			file_temp.delete();
		}
	}

	public void clean() {
		File file_temp = getFileStreamPath("temp.jpg");
		if (file_temp.exists())
			file_temp.delete();
	}

	public void getHeightAndWidth() {
		width = bmp.getWidth();
		height = bmp.getHeight();
	}
}
