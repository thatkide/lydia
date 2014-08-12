package ca.efriesen.lydia.includes;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import ca.efriesen.lydia.R;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by eric on 2013-07-14.
 */
public class MapHelpers {

	private static final String TAG = "lydia map helpers";

	public static class GetLocationsFromStringTask extends AsyncTask<String, Void, ArrayList<Address>> {

		private Context context;
		private Location location;
		public GetLocationsFromStringTask(Context context, Location location) {
			this.context = context;
			this.location = location;
		}

		@Override
		protected ArrayList<Address> doInBackground(String... strings) {
			ArrayList<Address> predictionsArr = new ArrayList<Address>();

			try {
				URL googlePlaces = new URL(
						"https://maps.googleapis.com/maps/api/place/autocomplete/json?" +
								"input="+ URLEncoder.encode(strings[0], "UTF-8") + "&" +
								"language=en&" +
								"sensor=true&" +
								(location != null ? "location=" + location.getLatitude() + "," + location.getLongitude() + "&" : "") +
								"radius=1000&" + // 5 kilometer radius
								"key=" + context.getString(R.string.googleApiBrowserKey));
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
	}

	public static class GetDetailsFromReferenceTask extends AsyncTask<String, Void, Address> {

		private Context context;
		public GetDetailsFromReferenceTask(Context context) {
			this.context = context;
		}

		@Override
		protected Address doInBackground(String... strings) {
			Address address = new Address(Locale.CANADA);

			try {
				URL googlePlaces = new URL(
						"https://maps.googleapis.com/maps/api/place/details/json?" +
								"reference="+ URLEncoder.encode(strings[0], "UTF-8") + "&" +
								"language=en&" +
								"sensor=true&" +
								"key=" + context.getString(R.string.googleApiBrowserKey));
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

				address.setLongitude(location.getDouble("lng"));
				address.setLatitude(location.getDouble("lat"));
				address.setFeatureName(result.getString("name"));
				address.setAddressLine(0, (!result.isNull("formatted_address") ? result.getString("formatted_address") : ""));
				address.setPhone((!result.isNull("formatted_phone_number") ? result.getString("formatted_phone_number") : ""));

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return address;
		}
	}

	public static class GetAddressFromLatLngTask extends AsyncTask<LatLng, Void, Address> {
		@Override
		protected Address doInBackground(LatLng... latLngs) {
			Address address = new Address(Locale.CANADA);

			try {
				URL googlePlaces = new URL(
						"https://maps.googleapis.com/maps/api/geocode/json?" +
								"latlng=" + latLngs[0].latitude + "," + latLngs[0].longitude + "&" +
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

				address.setLatitude(latLngs[0].latitude);
				address.setLongitude(latLngs[0].longitude);

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
