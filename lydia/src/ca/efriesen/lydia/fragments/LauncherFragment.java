package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.includes.AppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by eric on 2013-07-15.
 */
public class LauncherFragment extends Fragment implements
		AdapterView.OnItemClickListener {
	private static final String TAG = "lydia launcher fragment";

	private Activity activity;
	private PackageManager packageManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		super.onCreateView(inflater, container, savedInstance);
		// hide ourself on create
//		getFragmentManager().beginTransaction().hide(getFragmentManager().findFragmentById(R.id.launcher_fragment)).commit();
		return inflater.inflate(R.layout.launcher_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		activity = getActivity();
		packageManager = activity.getPackageManager();

		// find the listview and attach the appinfo adapter to it
		ListView listView = (ListView) getActivity().findViewById(R.id.application_list);
		listView.setAdapter(new AppInfoViewAdapter(getInstalledPackages(), getActivity()));
		// send any item clicks back to this class, looking for method onItemClick
		listView.setOnItemClickListener(this);
	}

	// returns an array of appinfos of the installed packages we can launch
	private ArrayList<AppInfo> getInstalledPackages() {
		// create our new arrays
		ArrayList<AppInfo> appInfos = new ArrayList<AppInfo>();
		// get the list of all installed apps
		List<PackageInfo> installedApps = packageManager.getInstalledPackages(0);
		// get a list of activites with the "Action Main" intent
		List<ResolveInfo> activityList = packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null), 0);

		// loop over the installed apps, and get the package info
		for (PackageInfo info : installedApps) {
			// create a new appinfo object
			AppInfo appInfo = new AppInfo();
			// we set package name here because we test on it later
			appInfo.setPackageName(info.packageName);

			// loop over all the activites with the "main intent"
			for (ResolveInfo resolveInfo : activityList) {
				// if the current packages matches one of the activies
				if (info.packageName.equals(resolveInfo.activityInfo.applicationInfo.packageName)) {
					// set the attributes needed
					appInfo.setClassName(resolveInfo.activityInfo.name);
					appInfo.setAppName(info.applicationInfo.loadLabel(packageManager).toString());
					appInfo.setVersionName(info.versionName);
					appInfo.setVersionCode(info.versionCode);
					appInfo.setIcon(info.applicationInfo.loadIcon(packageManager));

					// create a new intent to stuff into the appinfo object
					Intent launchIntent = new Intent();
					ComponentName component = new ComponentName(appInfo.getPackageName(), appInfo.getClassName());
					launchIntent.setComponent(component);
					launchIntent.setAction(Intent.ACTION_MAIN);
					appInfo.setLaunchIntent(launchIntent);

					// add the object to the array that will be returned
					appInfos.add(appInfo);
					break;
				}
			}

		}

		// sort the list by appname ignoring case
		Collections.sort(appInfos, new Comparator<AppInfo>() {
			@Override
			public int compare(AppInfo appInfo, AppInfo appInfo2) {
				return appInfo.getAppName().compareToIgnoreCase(appInfo2.getAppName());
			}
		});

		// return the list
		return appInfos;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		// get the info of what was clicked
		AppInfo appInfo = (AppInfo) adapterView.getAdapter().getItem(position);

		// try to launch it
		try {
			startActivity(appInfo.getLaunchIntent());
		} catch (Exception e) {
			// uh oh, it failed.  show a warning message
			Toast.makeText(activity, getString(R.string.launcher_error), Toast.LENGTH_SHORT).show();
		}
	}

	// listview adapter for appinfos
	class AppInfoViewAdapter extends BaseAdapter implements ListAdapter {
		private final List<AppInfo> content;
		private final Activity activity;

		public AppInfoViewAdapter(List<AppInfo> content, Activity activity) {
			this.content = content;
			this.activity = activity;
		}

		public int getCount() {
			return content.size();
		}

		public AppInfo getItem(int position) {
			return content.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView,	ViewGroup parent) {
			// inflate the view if not already done
			if (convertView == null) {
				LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.app_launcher_row, null);
			}

			// get the specific app we've pressed
			AppInfo appInfo = content.get(position);
			if (appInfo != null) {
				// get the name and icon views
				TextView appName = (TextView) convertView.findViewById(R.id.app_launcher_row_app_name);
				ImageView appIcon = (ImageView) convertView.findViewById(R.id.app_launcherrow_icon);

				// set the name and icon
				appName.setText(appInfo.getAppName());
				appIcon.setImageDrawable(appInfo.getIcon());
			}
			// return the view
			return convertView;
		}
	}
}
