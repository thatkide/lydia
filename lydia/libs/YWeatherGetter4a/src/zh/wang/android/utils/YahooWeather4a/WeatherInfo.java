/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2012 Zhenghong Wang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zh.wang.android.utils.YahooWeather4a;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import zh.wang.android.yweathergetter.R;

public class WeatherInfo {
	
	String mTitle;
	String mDescription;
	String mLanguage;
	String mLastBuildDate;
	String mLocationCity;
	String mLocationRegion; // region may be null
	String mLocationCountry;
	
	String mWindChill;
	String mWindDirection;
	String mWindSpeed;
	
	String mAtmosphereHumidity;
	String mAtmosphereVisibility;
	String mAtmospherePressure;
	String mAtmosphereRising;
	
	String mAstronomySunrise;
	String mAstronomySunset;
	
	String mConditionTitle;
	String mConditionLat;
	String mConditionLon;

	/*
	 * information in tag "yweather:condition"
	 */
	int mCurrentCode;
	String mCurrentText;
	int mCurrentTempC;
	int mCurrentTempF;
	String mCurrentConditionIconURL;
	String mCurrentConditionDate;

	/*
	 * information in the first tag "yweather:forecast"
	 */
	String mForecast1Day;
	String mForecast1Date;
	int mForecast1Code;
	String mForecast1Text;
	int mForecast1TempHighC;
	int mForecast1TempLowC;
	int mForecast1TempHighF;
	int mForecast1TempLowF;
	String mForecast1ConditionIconURL;

	/*
	 * information in the second tag "yweather:forecast"
	 */
	String mForecast2Day;
	String mForecast2Date;
	int mForecast2Code;
	String mForecast2Text;
	int mForecast2TempHighC;
	int mForecast2TempLowC;
	int mForecast2TempHighF;
	int mForecast2TempLowF;
	String mForecast2ConditionIconURL;

	/*
	 * information in the second tag "yweather:forecast"
	 */
	String mForecast3Day;
	String mForecast3Date;
	int mForecast3Code;
	String mForecast3Text;
	int mForecast3TempHighC;
	int mForecast3TempLowC;
	int mForecast3TempHighF;
	int mForecast3TempLowF;
	String mForecast3ConditionIconURL;

	/*
	 * information in the second tag "yweather:forecast"
	 */
	String mForecast4Day;
	String mForecast4Date;
	int mForecast4Code;
	String mForecast4Text;
	int mForecast4TempHighC;
	int mForecast4TempLowC;
	int mForecast4TempHighF;
	int mForecast4TempLowF;
	String mForecast4ConditionIconURL;

	/*
	 * information in the second tag "yweather:forecast"
	 */
	String mForecast5Day;
	String mForecast5Date;
	int mForecast5Code;
	String mForecast5Text;
	int mForecast5TempHighC;
	int mForecast5TempLowC;
	int mForecast5TempHighF;
	int mForecast5TempLowF;
	String mForecast5ConditionIconURL;


	final public static int TORNADO = 0;
	final public static int TROPICAL_STORM = 1;
	final public static int HURRICANE = 2;
	final public static int SEVERE_THUNDERSTORMS = 3;
	final public static int THUNDERSTORMS = 4;
	final public static int MIXED_RAIN_AND_SNOW = 5;
	final public static int MIXED_RAIN_AND_SLEET = 6;
	final public static int MIXED_SNOW_AND_SLEET = 7;
	final public static int FREEZING_DRIZZLE = 8;
	final public static int DRIZZLE = 9;
	final public static int FREEZING_RAIN = 10;
	final public static int SHOWWERS = 11;
	final public static int SHOWWERS2 = 12;
	final public static int SNOW_FLURRIES = 13;
	final public static int LIGHT_SNOW_FLURRIES = 14;
	final public static int BLOWING_SNOW = 15;
	final public static int SNOW = 16;
	final public static int HAIL = 17;
	final public static int SLEET = 18;
	final public static int DUST = 19;
	final public static int FOGGY = 20;
	final public static int HAZE = 21;
	final public static int SMOKY = 22;
	final public static int BLUSTERY = 23;
	final public static int WINDY = 24;
	final public static int COLD = 25;
	final public static int CLOUDY = 26;
	final public static int MOSTLY_CLOUDY_NIGHT = 27;
	final public static int MOSTLY_CLOUDY_DAY = 28;
	final public static int PARTLY_CLOUDY_NIGHT = 29;
	final public static int PARTLY_CLOUDY_DAY = 30;
	final public static int CLEAR_NIGHT = 31;
	final public static int SUNNY = 32;
	final public static int FAIR_NIGHT = 33;
	final public static int FAIR_DAY = 34;
	final public static int MIXED_RAIN_AND_HAIL = 35;
	final public static int HOT = 36;
	final public static int ISOLATED_THUNDERSTORMS = 37;
	final public static int SCATTERED_THUNDERSTORMS = 38;
	final public static int SCATTERED_THUNDERSTORMS2 = 39;
	final public static int SCATTERED_SHOWERS = 40;
	final public static int HEAVY_SNOW = 41;
	final public static int SCATTERED_SNOW_SHOWERS = 42;
	final public static int HEAVY_SNOW2 = 43;
	final public static int PARTLY_CLOUDY = 44;
	final public static int THUNDERSTORMS2 = 45;
	final public static int SNOW_SHOWERS = 46;
	final public static int ISOLATED_THUNDERSHOWERS = 47;
	final public static int NOT_AVAILABLE = 3200;

	public String getForecast1Date() {
		return mForecast1Date;
	}

	void setForecast1Date(String forecast1Date) {
		mForecast1Date = forecast1Date;
	}

	public String getForecast2Date() {
		return mForecast2Date;
	}

	void setForecast2Date(String forecast2Date) {
		mForecast2Date = forecast2Date;
	}

	public String getCurrentConditionDate() {
		return mCurrentConditionDate;
	}
	
	void setCurrentConditionDate(String currentConditionDate) {
		mCurrentConditionDate = currentConditionDate;
	}
	
	private int turnFtoC(int tempF) {
		return (tempF - 32) * 5 / 9;
	}

	public Bitmap getConditionsIcon(Context context, int code) {
		switch (code) {
			case PARTLY_CLOUDY:
			case PARTLY_CLOUDY_DAY:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_cloudy);

			case CLOUDY:
			case MOSTLY_CLOUDY_DAY:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_cloudy);

			case PARTLY_CLOUDY_NIGHT:
			case MOSTLY_CLOUDY_NIGHT:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_cloudy_night);

			case MIXED_RAIN_AND_SNOW:
			case MIXED_SNOW_AND_SLEET:
			case FREEZING_DRIZZLE:
			case FREEZING_RAIN:
			case HAIL:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_drizzle_snow);

			case MIXED_RAIN_AND_HAIL:
			case MIXED_RAIN_AND_SLEET:
			case SHOWWERS:
			case SHOWWERS2:
			case SLEET:
			case SCATTERED_SHOWERS:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_drizzle);

			case HAZE:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_haze);

			case CLEAR_NIGHT:
			case FAIR_NIGHT:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_moon);

			case DRIZZLE:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_slight_drizzle);

			case SNOW:
			case SNOW_FLURRIES:
			case SNOW_SHOWERS:
			case BLOWING_SNOW:
			case HEAVY_SNOW:
			case HEAVY_SNOW2:
			case LIGHT_SNOW_FLURRIES:
			case SCATTERED_SNOW_SHOWERS:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_snow);

			case SUNNY:
			case HOT:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_sunny);

			case ISOLATED_THUNDERSHOWERS:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_thunderstorms_snow);

			case THUNDERSTORMS:
			case THUNDERSTORMS2:
			case ISOLATED_THUNDERSTORMS:
			case SCATTERED_THUNDERSTORMS:
			case SCATTERED_THUNDERSTORMS2:
			case SEVERE_THUNDERSTORMS:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_thunderstorms);

			case NOT_AVAILABLE:
			Default:
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_unknown);
		}
		return null;
	}

	public int getCurrentCode() {
		return mCurrentCode;
	}

	void setCurrentCode(int currentCode) {
		mCurrentCode = currentCode;
		mCurrentConditionIconURL = "http://l.yimg.com/a/i/us/we/52/" + currentCode + ".gif";
	}

	public int getCurrentTempF() {
		return mCurrentTempF;
	}

	void setCurrentTempF(int currentTempF) {
		mCurrentTempF = currentTempF;
		mCurrentTempC = this.turnFtoC(currentTempF);
	}

	public String getForecast1Day() {
		return mForecast1Day;
	}

	void setForecast1Day(String forecast1Day) {
		mForecast1Day = forecast1Day;
	}

	public int getForecast1Code() {
		return mForecast1Code;
	}

	void setForecast1Code(int forecast1Code) {
		mForecast1Code = forecast1Code;
		mForecast1ConditionIconURL = "http://l.yimg.com/a/i/us/we/52/" + forecast1Code + ".gif";
	}

	public String getForecast1Text() {
		return mForecast1Text;
	}

	void setForecast1Text(String forecast1Text) {
		mForecast1Text = forecast1Text;
	}

	public int getForecast1TempHighF() {
		return mForecast1TempHighF;
	}

	void setForecast1TempHighF(int forecast1TempHighF) {
		mForecast1TempHighF = forecast1TempHighF;
		mForecast1TempHighC = this.turnFtoC(forecast1TempHighF);
	}

	public int getForecast1TempLowF() {
		return mForecast1TempLowF;
	}

	void setForecast1TempLowF(int forecast1TempLowF) {
		mForecast1TempLowF = forecast1TempLowF;
		mForecast1TempLowC = this.turnFtoC(forecast1TempLowF);
	}

	public String getForecast2Day() {
		return mForecast2Day;
	}

	void setForecast2Day(String forecast2Day) {
		mForecast2Day = forecast2Day;
	}

	public int getForecast2Code() {
		return mForecast2Code;
	}

	void setForecast2Code(int forecast2Code) {
		mForecast2Code = forecast2Code;
		mForecast2ConditionIconURL = "http://l.yimg.com/a/i/us/we/52/" + forecast2Code + ".gif";
	}

	public String getCurrentConditionIconURL() {
		return mCurrentConditionIconURL;
	}

	public String getForecast1ConditionIconURL() {
		return mForecast1ConditionIconURL;
	}

	public String getForecast2ConditionIconURL() {
		return mForecast2ConditionIconURL;
	}

	public String getForecast2Text() {
		return mForecast2Text;
	}

	void setForecast2Text(String forecast2Text) {
		mForecast2Text = forecast2Text;
	}

	public int getForecast2TempHighF() {
		return mForecast2TempHighF;
	}

	void setForecast2TempHighF(int forecast2TempHighF) {
		mForecast2TempHighF = forecast2TempHighF;
		mForecast2TempHighC = this.turnFtoC(forecast2TempHighF);
	}

	public int getForecast2TempLowF() {
		return mForecast2TempLowF;
	}

	void setForecast2TempLowF(int forecast2TempLowF) {
		mForecast2TempLowF = forecast2TempLowF;
		mForecast2TempLowC = this.turnFtoC(forecast2TempLowF);
	}

	public String getForecast3Date() {
		return mForecast3Date;
	}

	void setForecast3Date(String forecast3Date) {
		mForecast3Date = forecast3Date;
	}

	public String getForecast3Day() {
		return mForecast3Day;
	}

	void setForecast3Day(String forecast3Day) {
		mForecast3Day = forecast3Day;
	}

	public int getForecast3Code() {
		return mForecast3Code;
	}

	void setForecast3Code(int forecast3Code) {
		mForecast3Code = forecast3Code;
		mForecast3ConditionIconURL = "http://l.yimg.com/a/i/us/we/52/" + forecast3Code + ".gif";
	}

	public String getForecast3Text() {
		return mForecast3Text;
	}

	void setForecast3Text(String forecast3Text) {
		mForecast3Text = forecast3Text;
	}

	public int getForecast3TempHighF() {
		return mForecast3TempHighF;
	}

	void setForecast3TempHighF(int forecast3TempHighF) {
		mForecast3TempHighF = forecast3TempHighF;
		mForecast3TempHighC = this.turnFtoC(forecast3TempHighF);
	}

	public int getForecast3TempLowF() {
		return mForecast3TempLowF;
	}

	void setForecast3TempLowF(int forecast3TempLowF) {
		mForecast3TempLowF = forecast3TempLowF;
		mForecast3TempLowC = this.turnFtoC(forecast3TempLowF);
	}

	public String getForecast4Date() {
		return mForecast4Date;
	}

	void setForecast4Date(String forecast4Date) {
		mForecast4Date = forecast4Date;
	}

	public String getForecast4Day() {
		return mForecast4Day;
	}

	void setForecast4Day(String forecast4Day) {
		mForecast4Day = forecast4Day;
	}

	public int getForecast4Code() {
		return mForecast4Code;
	}

	void setForecast4Code(int forecast4Code) {
		mForecast4Code = forecast4Code;
		mForecast4ConditionIconURL = "http://l.yimg.com/a/i/us/we/52/" + forecast4Code + ".gif";
	}

	public String getForecast4Text() {
		return mForecast4Text;
	}

	void setForecast4Text(String forecast4Text) {
		mForecast4Text = forecast4Text;
	}

	public int getForecast4TempHighF() {
		return mForecast4TempHighF;
	}

	void setForecast4TempHighF(int forecast4TempHighF) {
		mForecast4TempHighF = forecast4TempHighF;
		mForecast4TempHighC = this.turnFtoC(forecast4TempHighF);
	}

	public int getForecast4TempLowF() {
		return mForecast4TempLowF;
	}

	void setForecast4TempLowF(int forecast4TempLowF) {
		mForecast4TempLowF = forecast4TempLowF;
		mForecast4TempLowC = this.turnFtoC(forecast4TempLowF);
	}

	public String getForecast5Date() {
		return mForecast5Date;
	}

	void setForecast5Date(String forecast5Date) {
		mForecast5Date = forecast5Date;
	}

	public String getForecast5Day() {
		return mForecast5Day;
	}

	void setForecast5Day(String forecast5Day) {
		mForecast5Day = forecast5Day;
	}

	public int getForecast5Code() {
		return mForecast5Code;
	}

	void setForecast5Code(int forecast5Code) {
		mForecast5Code = forecast5Code;
		mForecast5ConditionIconURL = "http://l.yimg.com/a/i/us/we/52/" + forecast5Code + ".gif";
	}

	public String getForecast5Text() {
		return mForecast5Text;
	}

	void setForecast5Text(String forecast5Text) {
		mForecast5Text = forecast5Text;
	}

	public int getForecast5TempHighF() {
		return mForecast5TempHighF;
	}

	void setForecast5TempHighF(int forecast5TempHighF) {
		mForecast5TempHighF = forecast5TempHighF;
		mForecast5TempHighC = this.turnFtoC(forecast5TempHighF);
	}

	public int getForecast5TempLowF() {
		return mForecast5TempLowF;
	}

	void setForecast5TempLowF(int forecast5TempLowF) {
		mForecast5TempLowF = forecast5TempLowF;
		mForecast5TempLowC = this.turnFtoC(forecast5TempLowF);
	}

	public int getCurrentTempC() {
		return mCurrentTempC;
	}

	public int getForecast1TempHighC() {
		return mForecast1TempHighC;
	}

	public int getForecast1TempLowC() {
		return mForecast1TempLowC;
	}

	public int getForecast2TempHighC() {
		return mForecast2TempHighC;
	}

	public int getForecast2TempLowC() {
		return mForecast2TempLowC;
	}

	public int getForecast3TempHighC() {
		return mForecast3TempHighC;
	}

	public int getForecast3TempLowC() {
		return mForecast3TempLowC;
	}

	public int getForecast4TempHighC() {
		return mForecast4TempHighC;
	}

	public int getForecast4TempLowC() {
		return mForecast4TempLowC;
	}

	public int getForecast5TempHighC() {
		return mForecast5TempHighC;
	}

	public int getForecast5TempLowC() {
		return mForecast5TempLowC;
	}

	public String getTitle() {
		return mTitle;
	}

	void setTitle(String title) {
		mTitle = title;
	}

	public String getDescription() {
		return mDescription;
	}

	void setDescription(String description) {
		mDescription = description;
	}

	public String getLanguage() {
		return mLanguage;
	}

	void setLanguage(String language) {
		mLanguage = language;
	}

	public String getLastBuildDate() {
		return mLastBuildDate;
	}

	void setLastBuildDate(String lastBuildDate) {
		mLastBuildDate = lastBuildDate;
	}

	public String getLocationCity() {
		return mLocationCity;
	}

	void setLocationCity(String locationCity) {
		mLocationCity = locationCity;
	}

	public String getLocationRegion() {
		return mLocationRegion;
	}

	void setLocationRegion(String locationRegion) {
		mLocationRegion = locationRegion;
	}

	public String getLocationCountry() {
		return mLocationCountry;
	}

	void setLocationCountry(String locationCountry) {
		mLocationCountry = locationCountry;
	}

	public String getWindChillF() {
		return mWindChill;
	}

	public String getWindChillC() {
		int chill = turnFtoC(Integer.parseInt(mWindChill));
		return String.valueOf(chill);
	}

	public String getWindChill() {
		return mWindChill;
	}

	void setWindChill(String windChill) {
		mWindChill = windChill;
	}

	public String getmWindDirectionText() {
		String dir = "";
		int direction = Integer.parseInt(getWindDirection());
		// N 0/360
		// E 90
		// S 180
		// W 270

		if (direction >= 337.5 || direction < 22.5) {
			dir = "N";
		} else if (direction >= 22.5 && direction < 67.5) {
			dir = "NE";
		} else if (direction >= 67.5 && direction < 112.5) {
			dir = "E";
		} else if (direction >=112.5 && direction < 157.5) {
			dir = "SE";
		} else if (direction >= 157.5 && direction < 202.5) {
			dir = "S";
		} else if (direction >= 202.5 && direction < 247.5) {
			dir = "SW";
		} else if (direction >= 247.5 && direction < 292.5) {
			dir = "W";
		} else if (direction >= 292.5 && direction < 337.5) {
			dir = "NW";
		}

		return dir;
	}

	public String getWindDirection() {
		return mWindDirection;
	}

	void setWindDirection(String windDirection) {
		mWindDirection = windDirection;
	}

	public String getWindSpeedK() {
		double speed = Integer.parseInt(mWindSpeed) * 1.60934;
		return String.valueOf((int)speed);
	}

	public String getWindSpeedM() {
		return mWindSpeed;
	}

	void setWindSpeed(String windSpeed) {
		mWindSpeed = windSpeed;
	}

	public String getAtmosphereHumidity() {
		return mAtmosphereHumidity;
	}

	void setAtmosphereHumidity(String atmosphereHumidity) {
		mAtmosphereHumidity = atmosphereHumidity;
	}

	public String getAtmosphereVisibility() {
		return mAtmosphereVisibility;
	}

	void setAtmosphereVisibility(String atmosphereVisibility) {
		mAtmosphereVisibility = atmosphereVisibility;
	}

	public String getAtmospherePressure() {
		return mAtmospherePressure;
	}

	void setAtmospherePressure(String atmospherePressure) {
		mAtmospherePressure = atmospherePressure;
	}

	public String getAtmosphereRising() {
		return mAtmosphereRising;
	}

	void setAtmosphereRising(String atmosphereRising) {
		mAtmosphereRising = atmosphereRising;
	}

	public String getAstronomySunrise() {
		return mAstronomySunrise;
	}

	void setAstronomySunrise(String astronomySunrise) {
		mAstronomySunrise = astronomySunrise;
	}

	public String getAstronomySunset() {
		return mAstronomySunset;
	}

	void setAstronomySunset(String astronomySunset) {
		mAstronomySunset = astronomySunset;
	}

	public String getConditionTitle() {
		return mConditionTitle;
	}

	void setConditionTitle(String conditionTitle) {
		mConditionTitle = conditionTitle;
	}

	public String getConditionLat() {
		return mConditionLat;
	}

	void setConditionLat(String conditionLat) {
		mConditionLat = conditionLat;
	}

	public String getConditionLon() {
		return mConditionLon;
	}

	void setConditionLon(String conditionLon) {
		mConditionLon = conditionLon;
	}

	public String getCurrentText() {
		return mCurrentText;
	}

	void setCurrentText(String currentText) {
		mCurrentText = currentText;
	}

	void setCurrentTempC(int currentTempC) {
		mCurrentTempC = currentTempC;
	}

	void setCurrentConditionIconURL(String currentConditionIconURL) {
		mCurrentConditionIconURL = currentConditionIconURL;
	}


}
