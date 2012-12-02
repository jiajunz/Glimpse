package edu.cmu.glimpse.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.util.Log;
import edu.cmu.glimpse.entry.EntryPlace;

public class GooglePlaceClient {
    private static final String TAG = "GooglePlaceClient";
    private static final String mBaseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=AIzaSyDE9PBsR0OJpB_mUWAoLDCq-a-TfOh_pmc&sensor=true";
    private static GooglePlaceClient mClient;

    private GooglePlaceClient() {
        super();
    }

    public static GooglePlaceClient getInstance() {
        if (mClient == null) {
            mClient = new GooglePlaceClient();
        }

        return mClient;
    }

    public List<EntryPlace> execute(Location location) throws IOException {
        String urlString = buildUrl(location);

        Log.d(TAG, "making https call: " + urlString);

        URL url = new URL(urlString);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        List<EntryPlace> placeNames = new ArrayList<EntryPlace>();
        try {
            JSONObject result = new JSONObject(getContent(con));
            JSONArray places = result.getJSONArray("results");
            for (int i = 0; i < places.length(); i++) {
                JSONObject place = places.getJSONObject(i);
                String name = place.getString("name");
                String reference = place.getString("reference");
                placeNames.add(new EntryPlace(name, reference));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return placeNames;
    }

    private String getContent(HttpsURLConnection con) {
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(con.getInputStream()));

            String input;
            while ((input = br.readLine()) != null) {
                sb.append(input);
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * Build URL based on current location
     * 
     * @param location
     *            Current location
     * @return
     *         Request URL based on current location and accuracy
     */
    public String buildUrl(Location location) {
        StringBuilder sb = new StringBuilder(mBaseUrl);
        sb.append("&location=");
        sb.append(location.getLatitude());
        sb.append(",");
        sb.append(location.getLongitude());
        sb.append("&radius=");
        sb.append(location.getAccuracy());
        return sb.toString();
    }

    /**
     * Build URL based on parameters
     * 
     * @param params
     *            Location and Radius are necessary
     * @return
     *         Complete request URL
     */
    public String buildUrl(Map<String, String> params) {
        StringBuilder sb = new StringBuilder(mBaseUrl);
        for (String key : params.keySet()) {
            sb.append("&");
            sb.append(key);
            sb.append("=");
            sb.append(params.get(key));
        }
        return sb.toString();
    }
}
