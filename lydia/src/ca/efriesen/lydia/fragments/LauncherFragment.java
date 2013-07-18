package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.*;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
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
	private List<PackageInfo> installedApps;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		super.onCreateView(inflater, container, savedInstance);
		// hide ourself on create
		getFragmentManager().beginTransaction().hide(getFragmentManager().findFragmentById(R.id.launcher_fragment)).commit();
		return inflater.inflate(R.layout.launcher_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		activity = getActivity();
		packageManager = activity.getPackageManager();

		ListView listView = (ListView) getActivity().findViewById(R.id.application_list);
		listView.setAdapter(new AppInfoViewAdapter(getInstalledPackages(), getActivity()));
		listView.setOnItemClickListener(this);

	}

	// if the back button is pressed while we're visible, go back to home screen one
	public boolean onBackPressed() {
		FragmentManager manager = getFragmentManager();
		manager.beginTransaction()
				.hide(manager.findFragmentById(R.id.launcher_fragment))
				.show(manager.findFragmentById(R.id.home_screen_fragment))
				.addToBackStack(null)
				.commit();
		return true;
	}

	private ArrayList getInstalledPackages() {
		ArrayList<AppInfo> appInfos = new ArrayList<AppInfo>();
		List<ResolveInfo> activityList = getActivityList();

		installedApps = packageManager.getInstalledPackages(0);

		for (PackageInfo info : installedApps) {
			AppInfo appInfo = new AppInfo();
			appInfo.setAppName(info.applicationInfo.loadLabel(packageManager).toString());
			appInfo.setPackageName(info.packageName);
			appInfo.setVersionName(info.versionName);
			appInfo.setVersionCode(info.versionCode);
			appInfo.setIcon(info.applicationInfo.loadIcon(packageManager));

			for (ResolveInfo resolveInfo : activityList) {
				if (info.packageName.equals(resolveInfo.activityInfo.applicationInfo.packageName)) {
					appInfo.setClassName(resolveInfo.activityInfo.name);

					Intent launchIntent = new Intent();
					ComponentName component = new ComponentName(appInfo.getPackageName(), appInfo.getClassName());
					launchIntent.setComponent(component);
					launchIntent.setAction(Intent.ACTION_MAIN);
					appInfo.setLaunchIntent(launchIntent);

					appInfos.add(appInfo);
					break;
				}
			}

		}

		Collections.sort(appInfos, new Comparator<AppInfo>() {
			@Override
			public int compare(AppInfo appInfo, AppInfo appInfo2) {
				return appInfo.getAppName().compareToIgnoreCase(appInfo2.getAppName());
			}
		});

		return appInfos;
	}

	private List<ResolveInfo> getActivityList() {
		List<ResolveInfo> aList = packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null), 0);

		Collections.sort(aList, new ResolveInfo.DisplayNameComparator(packageManager));

		return aList;
	}


	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		AppInfo appInfo = (AppInfo) adapterView.getAdapter().getItem(position);

		try {
			startActivity(appInfo.getLaunchIntent());
		} catch (Exception e) {
			Toast.makeText(activity, "Launcher error", Toast.LENGTH_SHORT).show();
		}
	}

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
			if (convertView == null) {
				LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.app_launcher_row, null);
			}

			AppInfo appInfo = content.get(position);
			if (appInfo != null) {
				TextView appName = (TextView) convertView.findViewById(R.id.app_launcher_row_app_name);
				ImageView appIcon = (ImageView) convertView.findViewById(R.id.app_launcherrow_icon);

				appName.setText(appInfo.getAppName());
				appIcon.setImageDrawable(appInfo.getIcon());
			}
			return convertView;
		}
	}
}
