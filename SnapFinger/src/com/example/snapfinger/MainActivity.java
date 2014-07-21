package com.example.snapfinger;

//todo:アクションバー動作、フィルタ、ゲーム

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
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

	private final static String PHOTOIMG_KEY = "photoImg";
	private Uri mImageUri;
	private boolean flag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		} else
			mImageUri = savedInstanceState.getParcelable(PHOTOIMG_KEY);
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

		if (flag) {
			menu_filter.setVisible(true);
			menu_game.setVisible(true);
		} else {
			menu_filter.setVisible(false);
			menu_game.setVisible(false);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
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

		case R.id.action_filter:
			AlertDialog.Builder filterDialog = new AlertDialog.Builder(this);
			filterDialog.setTitle("確認");
			filterDialog.setMessage("フィルタモードへ移行します。");

			filterDialog.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent_filter = new Intent(
									MainActivity.this, Filter.class);
							intent_filter.putExtra("image_uri", mImageUri);
							startActivity(intent_filter);
						}
					});
			filterDialog.setPositiveButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			filterDialog.setCancelable(true);
			AlertDialog alert_f = filterDialog.create();
			alert_f.show();
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
		outState.putParcelable(PHOTOIMG_KEY, mImageUri);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		
		mImageUri=(Uri)savedInstanceState.get(PHOTOIMG_KEY);
		showPhoto();
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

	public void callGallery(View view) {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, MY_REQUEST_FOR_CALL);
		// GPUImage gpuimage = new GPUImage(this);
		// gpuimage.setImage(mImageUri);
		/*
		 * try { Bitmap bmp = MediaStore.Images.Media.getBitmap(
		 * getContentResolver(), mImageUri); gpuimage.setImage(bmp); } catch
		 * (FileNotFoundException e) { e.printStackTrace(); } catch (IOException
		 * ie) { ie.printStackTrace(); }
		 */
		// いったん古い画像を消す？
		// 適当な場所に保存しておいてsaveコマンドの後上書き？
		/*
		 * Bitmap bitmap=gpuimage.getBitmapWithFilterApplied(); Cursor c =
		 * getContentResolver().query(mImageUri, null, null, null, null);
		 * c.moveToFirst(); String filepath = c.getString(c
		 * .getColumnIndex(MediaStore.MediaColumns.DATA)); try{ FileOutputStream
		 * out=new FileOutputStream(filepath);
		 * bitmap.compress(CompressFormat.JPEG, 100, out); }catch(IOException
		 * e){ e.printStackTrace(); }
		 */
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
					showPhoto();
				}
			}
			break;
		case MY_REQUEST_FOR_CALL:
			if (data != null) {
				mImageUri = data.getData();
				flag = true;
				showPhoto();
			}
		}
	}

	private void showPhoto() {
		ImageView photoView = (ImageView) findViewById(R.id.photo_view);
		/*
		ContentResolver cr=getContentResolver();
		try{
		photoImg=MediaStore.Images.Media.getBitmap(cr, mImageUri);
		}catch(IOException e){
			e.printStackTrace();
		}
		photoView.setImageBitmap(photoImg);
		*/
		photoView.setImageURI(mImageUri);
	}
}
