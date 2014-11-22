package com.autosenseapp.includes;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.autosenseapp.interfaces.ExtraDataSpinner;

/**
* Created by eric on 2013-07-16.
*/
public class AppInfo implements ExtraDataSpinner {
	private String appName = "";
	private String packageName = "";
	private String className = "";
	private String versionName = "";
	private Integer versionCode = 0;
	private Drawable icon = null;
	private Intent launchIntent;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String  versionName) {
		this.versionName = versionName;
	}

	public Integer getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(Integer versionCode) {
		this.versionCode = versionCode;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public Intent getLaunchIntent() {
		return launchIntent;
	}

	public String getLaunchIntentUri() {
		return launchIntent.toUri(Intent.URI_INTENT_SCHEME);
	}

	public void setLaunchIntent(Intent launchIntent) {
		this.launchIntent = launchIntent;
	}

	@Override
	public String toString() {
		return appName;
	}

	@Override
	public String getDataInfo() {
		return getLaunchIntentUri();
	}
}
