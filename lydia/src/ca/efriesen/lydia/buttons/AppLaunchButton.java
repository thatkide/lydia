package ca.efriesen.lydia.buttons;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.widget.ArrayAdapter;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.includes.AppInfo;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by eric on 2014-06-22.
 */
public class AppLaunchButton extends BaseButton {

	private static final String TAG = "App Launcher button";

	private ArrayAdapter<AppInfo> adapter;

	private Activity activity;
	public static final String ACTION = "AppLaunchButton";

	public AppLaunchButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(Button button) {
		Log.d(TAG, "button clicked");
		Log.d(TAG, button.getAction());
		Log.d(TAG, button.getExtraData());

		try {
			Intent launch = Intent.parseUri(button.getExtraData(), Intent.URI_INTENT_SCHEME);
			activity.startActivity(launch);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasExtraData() {
		return true;
	}

	@Override
	public String getAction() {
		return ACTION;
	}

	public ArrayAdapter<AppInfo> getAdapterData() {
		List<AppInfo> apps = getInstalledPackages(activity);
		adapter = new ArrayAdapter<AppInfo>(activity, android.R.layout.simple_spinner_dropdown_item, apps);
		return adapter;
	}

	@Override
	public String getDescription() {
		return "Open App Specified";
	}

	@Override
	public String getExtraData(int positioin) {
		AppInfo appInfo = adapter.getItem(positioin);
		return appInfo.getLaunchIntent().toUri(Intent.URI_INTENT_SCHEME);
	}

	@Override
	public String toString() {
		return getDescription();
	}

	// returns an array of appinfos of the installed packages we can launch
	private static List<AppInfo> getInstalledPackages(Context context) {
		// rare time when the package manager dies the app will crash.  this should fix it, at least stop the crash.
		try {
			PackageManager packageManager = context.getPackageManager();
			// create our new arrays
			List<AppInfo> appInfos = new ArrayList<AppInfo>();
			// get the list of all installed apps
			List<PackageInfo> installedApps = packageManager.getInstalledPackages(0);
			// get a list of activities with the "Action Main" intent
			List<ResolveInfo> activityList = packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null), 0);

			// loop over the installed apps, and get the package info
			for (PackageInfo info : installedApps) {
				// create a new appinfo object
				AppInfo appInfo = new AppInfo();
				// we set package name here because we test on it later
				appInfo.setPackageName(info.packageName);

				// loop over all the activites with the "main intent"
				for (ResolveInfo resolveInfo : activityList) {
					// if the current packages matches one of the activities
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
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
