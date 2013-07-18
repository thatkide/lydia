package ca.efriesen.lydia.includes;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import ca.efriesen.lydia.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by eric on 2013-07-14.
 */
public class MapHelpers {

	private static final String TAG = "lydia map helpers";

	public static Address getAddressFromLatLng(LatLng latLng) {
		try {
			return new GetAddressTask().execute(latLng.latitude, latLng.longitude).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<Address> getDetailsFromReference(Context context, String search) {
		ArrayList<Address> detailsArray = new ArrayList<Address>();
		try {
			URL googlePlaces = new URL(
					"https://maps.googleapis.com/maps/api/place/details/json?" +
							"reference="+ URLEncoder.encode(search, "UTF-8") + "&" +
							"language=en&" +
							"sensor=true&" +
							"key=" + context.getString(R.string.google_api_browser_key));
			URLConnection tc = googlePlaces.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(tc.getInputStream()));

			String line;
			StringBuilder sb = new StringBuilder();
			//take Google's legible JSON and turn it into one big string.
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			//turn that string into a JSON object
			JSONObject json = new JSONObject(sb.toString());
			JSONObject result = json.getJSONObject("result");
			JSONObject geometry = result.getJSONObject("geometry");
			JSONObject location = geometry.getJSONObject("location");
			Address address = new Address(Locale.CANADA);
			address.setLongitude(location.getDouble("lng"));
			address.setLatitude(location.getDouble("lat"));
			address.setFeatureName(result.getString("name"));
			address.setAddressLine(0, (!result.isNull("formatted_address") ? result.getString("formatted_address") : ""));
			address.setPhone((!result.isNull("formatted_phone_number") ? result.getString("formatted_phone_number") : ""));

			detailsArray.add(address);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return detailsArray;
	}

	public static ArrayList<Address> getLocationsFromString(Context context, String search, Location location) {
		ArrayList<Address> predictionsArr = new ArrayList<Address>();

		try {
			URL googlePlaces = new URL(
					"https://maps.googleapis.com/maps/api/place/autocomplete/json?" +
							"input="+ URLEncoder.encode(search, "UTF-8") + "&" +
//								"types=geocode&" + // geocode is address locations only.  the other option is establisghment for businesses only.  or no restrictions
							"language=en&" +
							"sensor=true&" +
							"location=" + location.getLatitude() + "," + location.getLongitude() + "&" +
							"radius=1000&" + // 5 kilometer radius
							"key=" + context.getString(R.string.google_api_browser_key));
			URLConnection tc = googlePlaces.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(tc.getInputStream()));

			String line;
			StringBuilder sb = new StringBuilder();
			//take Google's legible JSON and turn it into one big string.
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			//turn that string into a JSON object
			JSONObject predictions = new JSONObject(sb.toString());
			//now get the JSON array that's inside that object
			JSONArray ja = new JSONArray(predictions.getString("predictions"));

			for (int i = 0; i < ja.length(); i++) {
				JSONObject jo = (JSONObject) ja.get(i);
				//add each entry to our array
				Address address = new Address(Locale.CANADA);
				address.setFeatureName(jo.getString("description"));
				address.setUrl(jo.getString("reference"));
				predictionsArr.add(address);
			}
		} catch (IOException e)	{
			Log.e(TAG, "GetPlaces : doInBackground", e);
		} catch (JSONException e) {
			Log.e(TAG, "GetPlaces : doInBackground", e);
		} catch (NullPointerException e) {
			Log.d(TAG, "null pointer", e);
		}

		return predictionsArr;
	}



	//// ------------------ ASync tasks

	private static class GetAddressTask extends AsyncTask<Double, Void, Address> {
		@Override
		protected Address doInBackground(Double... doubles) {
			Address address = new Address(Locale.CANADA);

			LatLng latLng = new LatLng(doubles[0], doubles[1]);
			try {
				URL googlePlaces = new URL(
						"https://maps.googleapis.com/maps/api/geocode/json?" +
								"latlng=" + latLng.latitude + "," + latLng.longitude + "&" +
								"language=en&" +
								"sensor=true&");
				URLConnection tc = googlePlaces.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(tc.getInputStream()));

				String line;
				StringBuilder sb = new StringBuilder();
				//take Google's legible JSON and turn it into one big string.
				while ((line = in.readLine()) != null) {
					sb.append(line);
				}
				//turn that string into a JSON object
				JSONObject json = new JSONObject(sb.toString());
				JSONArray results = new JSONArray(json.getString("results"));

				address.setLatitude(latLng.latitude);
				address.setLongitude(latLng.longitude);

				JSONObject result = (JSONObject) results.get(0);
				address.setFeatureName(result.getString("formatted_address"));

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return address;
		}
	}

}
