package com.example.snapfinger;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class ImageProcessing_mat extends AsyncTask<Bitmap, Void, Bitmap> {

	private int flag;
	private Bitmap bitmap;
	private float parameter;
	private final WeakReference<ImageView> imageViewReference;
	private MainActivity activity;
	
	public ImageProcessing_mat(ImageView imageView) {
		// TODO Auto-generated constructor stub
		imageViewReference = new WeakReference<ImageView>(imageView);
	}
	
	@Override
	protected Bitmap doInBackground(Bitmap... bmp) {
		// TODO Auto-generated method stub
		bitmap=bmp[0];
		switch(flag){
		case 1:
			Mat mat_1 = new Mat();
			Mat mat_1_result = new Mat();
			Utils.bitmapToMat(bitmap, mat_1);
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
			gamma = Math.log(0.5) / Math.log(parameter / 200.0);
			Mat lut_1 = new Mat(1, 256, CvType.CV_8UC1);
			lut_1.setTo(new Scalar(0));

			for (int i = 0; i < 256; i++) {
				double temp = Math.pow((1.0 * (double) i / 255.0), 1 / gamma);
				if (temp > 1.0)
					temp = 1.0;
				lut_1.put(0, i, temp * 255.0);
			}
			Core.LUT(mat_1, lut_1, mat_1_result);
			Utils.matToBitmap(mat_1_result, bitmap);
		case 2:
			Mat mat_2 = new Mat();
			Mat mat_2_result = new Mat();
			Utils.bitmapToMat(bitmap, mat_2);
			Imgproc.erode(mat_2, mat_2_result, Imgproc.getStructuringElement(
					Imgproc.MORPH_ELLIPSE, new Size(parameter, parameter)));
			Utils.matToBitmap(mat_2_result, bitmap);
		}
		
		return bitmap;
	}
	public void setFlag(int flag){
		this.flag=flag;
	}
	
	public void setParameter(float parameter){
		this.parameter=parameter;
	}
	
	public void setMainActivity(MainActivity activity) {
		this.activity = activity;
	}
	
	protected void onPostExecute(Bitmap bitmap) {
		if (imageViewReference != null && bitmap != null) {
			final ImageView imageView = imageViewReference.get();
			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
			}
			// tempファイルに書き出し

			try {
				File file = activity.getFileStreamPath("temp.jpg");
				if (file.exists())
					file.delete();

				FileOutputStream out = activity.openFileOutput("temp.jpg",
						Context.MODE_PRIVATE);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

}
