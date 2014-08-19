package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import ca.efriesen.lydia.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import zh.wang.android.utils.YahooWeather4a.WeatherInfo;
import zh.wang.android.utils.YahooWeather4a.YahooWeatherInfoListener;
import zh.wang.android.utils.YahooWeather4a.YahooWeatherUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by eric on 2013-08-04.
 */
public class WeatherFragment extends Fragment implements
		YahooWeatherInfoListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private LocationClient locationClient;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.weather_fragment, container, false);
	}

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		locationClient = new LocationClient(getActivity().getApplicationContext(), this, this);
		locationClient.connect();
	}

	@Override
	public void onConnected(Bundle data) {
		Location location = locationClient.getLastLocation();
		YahooWeatherUtils yahooWeatherUtils = YahooWeatherUtils.getInstance();
		if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("weatherUseCurrentLocation", true) && location != null) {
			yahooWeatherUtils.queryYahooWeather(getActivity(), location , this);
		} else {
			String city = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("weatherCity", "Vancouver Canada");
			yahooWeatherUtils.queryYahooWeather(getActivity(), city , this);
		}
	}

	@Override
	public void onDisconnected() {

	}

	@Override
	public void gotWeatherInfo(WeatherInfo weatherInfo) {
		final Activity activity = getActivity();
		if (weatherInfo != null) {

			ImageView iconView = (ImageView) activity.findViewById(R.id.weather_conditions_icon);
			int currentCode = weatherInfo.getCurrentCode();
			if (currentCode == WeatherInfo.NOT_AVAILABLE) {
				currentCode = weatherInfo.getForecast1Code();
			}
			iconView.setImageBitmap(weatherInfo.getConditionsIcon(activity, currentCode));

			// hide the loading spinner
			activity.findViewById(R.id.loading_spinner).setVisibility(View.GONE);
			activity.findViewById(R.id.weather_container).setVisibility(View.VISIBLE);

			// get whether we use c or f from preferences
			boolean celsius = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("weatherUseMetric", true);

			// get the current temp, in either celsius or fahrenheit
			int currentTemp;
			String windSpeed, windchill;
			if (celsius) {
				currentTemp = weatherInfo.getCurrentTempC();
				((TextView)activity.findViewById(R.id.weather_degrees)).setText((char)0x00B0 + "C");
				windSpeed = weatherInfo.getWindSpeedK() + " km/h";
				windchill = weatherInfo.getWindChillC() + (char)0x00B0 + "C";
			} else {
				currentTemp = weatherInfo.getCurrentTempF();
				((TextView)activity.findViewById(R.id.weather_degrees)).setText((char)0x00B0 + "F");
				windSpeed = weatherInfo.getWindSpeedM() + " mph";
				windchill = weatherInfo.getWindChillF() + (char)0x00B0 + "F";
			}

			String currentText = weatherInfo.getCurrentText();
			if (currentText.equalsIgnoreCase("unknown")) {
				currentText = weatherInfo.getForecast1Text();
			}
			// current info
			((TextView)activity.findViewById(R.id.weather_title)).setText(weatherInfo.getLocationCity() + ", " + weatherInfo.getLocationRegion());
			((TextView)activity.findViewById(R.id.weather_current_temp)).setText(String.valueOf(currentTemp));
			((TextView)activity.findViewById(R.id.weather_conditions_desc)).setText(currentText);
			((TextView)activity.findViewById(R.id.weather_wind)).setText(windSpeed + " from "+ weatherInfo.getmWindDirectionText());
			((TextView)activity.findViewById(R.id.weather_windchill)).setText(windchill);
			((TextView)activity.findViewById(R.id.weather_humidity)).setText(weatherInfo.getAtmosphereHumidity() + "%");
			((TextView)activity.findViewById(R.id.weather_sunrise)).setText(weatherInfo.getAstronomySunrise());
			((TextView)activity.findViewById(R.id.weather_sunset)).setText(weatherInfo.getAstronomySunset());

			// 5 day forecast
			for (int i=0; i<5; i++) {
				try {
					// use reflection to get the proper method
					Method getDay = weatherInfo.getClass().getMethod("getForecast" + (i+1) + "Day");
					Method getCode = weatherInfo.getClass().getMethod("getForecast" + (i+1) + "Code");
					Method getText = weatherInfo.getClass().getMethod("getForecast" + (i+1) + "Text");
					Method getTempHigh;
					Method getTempLow;
					String high, low;
					if (celsius) {
						getTempHigh = weatherInfo.getClass().getMethod("getForecast" + (i+1) + "TempHighC");
						getTempLow = weatherInfo.getClass().getMethod("getForecast" + (i+1) + "TempLowC");
						high = "High: " + getTempHigh.invoke(weatherInfo) + (char)0x00B0 + "C";
						low = "Low: " + getTempLow.invoke(weatherInfo) + (char)0x00B0 + "C";
					} else {
						getTempHigh = weatherInfo.getClass().getMethod("getForecast" + (i+1) + "TempHighF");
						getTempLow = weatherInfo.getClass().getMethod("getForecast" + (i+1) + "TempLowF");
						high = "High: " + getTempHigh.invoke(weatherInfo) + (char)0x00B0 + "F";
						low = "Low: " + getTempLow.invoke(weatherInfo) + (char)0x00B0 + "F";
					}

					// get the text/image based on the resource id and method from above
					((TextView)activity.findViewById(getResources().getIdentifier("weather_day" + i + "_title", "id", "ca.efriesen.lydia"))).setText((String)getDay.invoke(weatherInfo));
					((ImageView) activity.findViewById(getResources().getIdentifier("weather_day" + i + "_icon", "id", "ca.efriesen.lydia"))).setImageBitmap(weatherInfo.getConditionsIcon(activity, (Integer) getCode.invoke(weatherInfo)));
					((TextView)activity.findViewById(getResources().getIdentifier("weather_day" + i + "_conditions", "id", "ca.efriesen.lydia"))).setText((String) getText.invoke(weatherInfo));
					((TextView)activity.findViewById(getResources().getIdentifier("weather_day" + i + "_high", "id", "ca.efriesen.lydia"))).setText(high);
					((TextView)activity.findViewById(getResources().getIdentifier("weather_day" + i + "_low", "id", "ca.efriesen.lydia"))).setText(low);
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		}

	}
}
