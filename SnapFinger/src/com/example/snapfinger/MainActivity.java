package com.example.snapfinger;

//todo:回転したら消える

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private Bitmap photoImg = null;
	private final static String PHOTOIMG_KEY = "photoImg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		} else
			photoImg = savedInstanceState.getParcelable(PHOTOIMG_KEY);
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
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(PHOTOIMG_KEY, photoImg);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}

	private Uri mImageUri;

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

	public void callFilter(View view) {
		GPUImage gpuimage = new GPUImage(this);
		gpuimage.setImage(mImageUri);
		/*
		try {
			Bitmap bmp = MediaStore.Images.Media.getBitmap(
					getContentResolver(), mImageUri);
			gpuimage.setImage(bmp);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		*/
		//いったん古い画像を消す？
		//適当な場所に保存しておいてsaveコマンドの後上書き？
		Bitmap bitmap=gpuimage.getBitmapWithFilterApplied();
		Cursor c = getContentResolver().query(mImageUri, null, null, null,
				null);
		c.moveToFirst();
		String filepath = c.getString(c
				.getColumnIndex(MediaStore.MediaColumns.DATA));
		try{
			FileOutputStream out=new FileOutputStream(filepath);
			bitmap.compress(CompressFormat.JPEG, 100, out);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case MY_REQUEST_FOR_PHOTO:
			if (resultCode == RESULT_OK) {
				// mImageUri=data.getData();
				// data.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
				showPhoto();
			}
			break;
		}
	}

	private void showPhoto() {
		if (mImageUri != null) {

			Cursor c = getContentResolver().query(mImageUri, null, null, null,
					null);
			c.moveToFirst();
			String filepath = c.getString(c
					.getColumnIndex(MediaStore.MediaColumns.DATA));

			File file = new File(filepath);
			long size = file.length();

			if (size == 0) {
				getContentResolver().delete(mImageUri, null, null);
			} else {
				ImageView photoView = (ImageView) findViewById(R.id.photo_view);
				photoView.setImageURI(mImageUri);
			}
		}
	}
}
