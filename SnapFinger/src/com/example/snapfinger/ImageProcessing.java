package com.example.snapfinger;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

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

public class ImageProcessing extends AsyncTask<Integer, Void, Bitmap> {

	private MainActivity activity;
	// private Uri uri;
	private int flag = 0;
	private int[] _array;
	private int width;
	private int height;
	private GaussianFilter filter_gau;
	private TwirlFilter filter_twi;
	private SwimFilter filter_swi;
	private WaterFilter filter_wat;
	private GlowFilter filter_glo;
	private BumpFilter filter_bum;
	private CrystallizeFilter filter_cry;
	private ContrastFilter filter_con;
	private MotionBlurFilter filter_mot;
	private OilFilter filter_oil;
	private final WeakReference<ImageView> imageViewReference;

	// private ContentResolver cr;

	public ImageProcessing(ImageView imageView, GaussianFilter filter,
			int[] _array, int width, int height) {
		imageViewReference = new WeakReference<ImageView>(imageView);
		this.filter_gau = filter;
		this._array = _array;
		this.width = width;
		this.height = height;
	}

	public ImageProcessing(ImageView imageView, TwirlFilter filter,
			int[] _array, int width, int height) {
		imageViewReference = new WeakReference<ImageView>(imageView);
		this.filter_twi = filter;
		this._array = _array;
		this.width = width;
		this.height = height;
	}

	public ImageProcessing(ImageView imageView, SwimFilter filter,
			int[] _array, int width, int height) {
		imageViewReference = new WeakReference<ImageView>(imageView);
		this.filter_swi = filter;
		this._array = _array;
		this.width = width;
		this.height = height;
	}

	public ImageProcessing(ImageView imageView, WaterFilter filter,
			int[] _array, int width, int height) {
		imageViewReference = new WeakReference<ImageView>(imageView);
		this.filter_wat = filter;
		this._array = _array;
		this.width = width;
		this.height = height;
	}

	public ImageProcessing(ImageView imageView, GlowFilter filter,
			int[] _array, int width, int height) {
		imageViewReference = new WeakReference<ImageView>(imageView);
		this.filter_glo = filter;
		this._array = _array;
		this.width = width;
		this.height = height;
	}

	public ImageProcessing(ImageView imageView, BumpFilter filter,
			int[] _array, int width, int height) {
		imageViewReference = new WeakReference<ImageView>(imageView);
		this.filter_bum = filter;
		this._array = _array;
		this.width = width;
		this.height = height;
	}

	public ImageProcessing(ImageView imageView, CrystallizeFilter filter,
			int[] _array, int width, int height) {
		imageViewReference = new WeakReference<ImageView>(imageView);
		this.filter_cry = filter;
		this._array = _array;
		this.width = width;
		this.height = height;
	}

	public ImageProcessing(ImageView imageView, ContrastFilter filter,
			int[] _array, int width, int height) {
		this.imageViewReference = new WeakReference<ImageView>(imageView);
		this.filter_con = filter;
		this._array = _array;
		this.width = width;
		this.height = height;
	}

	public ImageProcessing(ImageView imageView, MotionBlurFilter filter,
			int[] _array, int width, int height) {
		this.imageViewReference = new WeakReference<ImageView>(imageView);
		this.filter_mot = filter;
		this._array = _array;
		this.width = width;
		this.height = height;
	}

	public ImageProcessing(ImageView imageView, OilFilter filter, int[] _array,
			int width, int height) {
		this.imageViewReference = new WeakReference<ImageView>(imageView);
		this.filter_oil = filter;
		this._array = _array;
		this.width = width;
		this.height = height;
	}

	@Override
	protected Bitmap doInBackground(Integer... flags) {
		System.out.println("started");
		flag = flags[0].intValue();
		switch (flag) {
		case 1:
			_array=filter_gau.filter(_array, width, height);
			break;
		case 2:
			_array=filter_twi.filter(_array, width, height);
			break;
		case 3:
			_array=filter_swi.filter(_array, width, height);
			break;
		case 4:
			_array=filter_wat.filter(_array, width, height);
			break;
		case 5:
			_array=filter_glo.filter(_array, width, height);
			break;
		case 6:
			_array=filter_bum.filter(_array, width, height);
			break;
		case 7:
			_array=filter_cry.filter(_array, width, height);
			break;
		case 8:
			_array=filter_con.filter(_array, width, height);
			break;
		case 9:
			_array=filter_mot.filter(_array, width, height);
			break;
		case 10:
			_array=filter_oil.filter(_array, width, height);
			break;
		}
		System.out.println("finished");
		return Bitmap.createBitmap(_array, width, height,
				Bitmap.Config.ARGB_8888);
	}

	/*
	 * public void setContentResolver(ContentResolver context) { cr = context; }
	 */
	public void setMainActivity(MainActivity activity) {
		this.activity = activity;
	}

	/*
	 * public void setImageUri(Uri uri) { this.uri = uri; }
	 */
	protected void onPostExecute(Bitmap bitmap) {
		if (imageViewReference != null && bitmap != null) {
			final ImageView imageView = imageViewReference.get();
			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
			}
			// tempファイルに書き出し

			try {
				System.out.println("saving");
				File file = activity.getFileStreamPath("temp.jpg");
				if (file.exists())
					file.delete();
				FileOutputStream out = activity.openFileOutput("temp.jpg",
						Context.MODE_PRIVATE);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.close();
				System.out.println("saved");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
}
