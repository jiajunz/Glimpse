package edu.cmu.glimpse.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import android.location.Location;

public class GooglePlaceClient {

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

    public void execute(Location location) throws IOException {
        String urlString = buildUrl(location);
        URL url = new URL(urlString);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

        // dump all the content
        print_content(con);
    }

    private void print_content(HttpsURLConnection con) {
        if (con != null) {

            try {

                System.out.println("****** Content of the URL ********");
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(con.getInputStream()));

                String input;

                while ((input = br.readLine()) != null) {
                    System.out.println(input);
                }
                br.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

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
