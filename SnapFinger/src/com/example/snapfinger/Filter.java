package com.example.snapfinger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ListView;

public class Filter extends Activity implements OnItemClickListener {

	private Uri mImageUri;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		setupNavigationDrawer();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1);
		adapter.add("Menu1");
		adapter.add("Menu2");
		adapter.add("Menu3");
		mDrawerList.setAdapter(adapter);
	}

	private void setupNavigationDrawer() {
		//mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
		//		GravityCompat.START);
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
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View parent,
			int position, long id) {
		selectItem(position);
	}

	private void selectItem(int position) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.filter, menu);

		return true;
	}

	/*
	 * @Override protected void onSaveInstanceState(Bundle outState) {
	 * super.onSaveInstanceState(outState); outState.putParcelable(PHOTOIMG_KEY,
	 * mImageUri); }
	 * 
	 * @Override protected void onRestoreInstanceState(Bundle
	 * savedInstanceState){ super.onRestoreInstanceState(savedInstanceState);
	 * 
	 * mImageUri=(Uri)savedInstanceState.get(PHOTOIMG_KEY); showPhoto(); }
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_exit:
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
			alertDialog.setTitle("確認");
			alertDialog.setMessage("カメラモードに戻ります。");

			alertDialog.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finishActivity();
						}
					});
			alertDialog.setPositiveButton("Cancel",
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
			View rootView = inflater.inflate(R.layout.fragment_filter,
					container, false);
			return rootView;
		}
	}
}
