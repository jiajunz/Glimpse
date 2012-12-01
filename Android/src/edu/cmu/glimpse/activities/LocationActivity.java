package edu.cmu.glimpse.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import edu.cmu.glimpse.widget.GlimpseMyLocationOverlay;
import edu.cmu.glimpse.widget.GlimpseMyLocationOverlay.OnLocationChangedListener;

public class LocationActivity extends MapActivity {
    private MapView mMapView;
    private ListView mLocationListView;
    private GlimpseMyLocationOverlay mMyLocationOverlay;
    // private LocationModule mLocationModule;
    private static final int MAX_LOCATION_RESULTS = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.setBuiltInZoomControls(true);
        mMyLocationOverlay = new GlimpseMyLocationOverlay(this, mMapView);
        mMyLocationOverlay.setOnLocationChangedListener(new OnLocationChangedListener() {

            public void locationChanged(Location location) {
                new ReverseGeocodingTask(LocationActivity.this).execute(location);
            }

        });
        mMyLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mMapView.getController().animateTo(mMyLocationOverlay.getMyLocation());
            }
        });
        mMapView.getOverlays().add(mMyLocationOverlay);
        mMapView.getController().setZoom(15);
        mMapView.setClickable(true);
        mMapView.setEnabled(true);
        mMapView.setSatellite(false);

        mLocationListView = (ListView) findViewById(R.id.locationListView);

        // mLocationModule = new LocationModule(this, new OnLocationUpdatedListener() {
        //
        // public void updateLocation(Location location) {
        // new ReverseGeocodingTask(LocationActivity.this).execute(location);
        // }
        //
        // });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // mLocationModule.startLocationUpdate();
        mMyLocationOverlay.enableMyLocation();
    }

    @Override
    protected void onPause() {
        // mLocationModule.stopLocationUpdate();
        mMyLocationOverlay.disableMyLocation();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_location, menu);
        return true;
    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }

    // AsyncTask encapsulating the reverse-geocoding API. Since the geocoder API is blocked,
    // we do not want to invoke it from the UI thread.
    private class ReverseGeocodingTask extends AsyncTask<Location, Void, List<Address>> {
        Context mContext;

        ReverseGeocodingTask(Context context) {
            mContext = context;
        }

        @Override
        protected List<Address> doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

            Location loc = params[0];
            List<Address> addresses = null;
            try {
                // Call the synchronous getFromLocation() method by passing in the lat/long values.
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), MAX_LOCATION_RESULTS);
            } catch (IOException e) {
                e.printStackTrace();
                // Update UI field with the exception.
                Log.e(TAG, e.toString());
            }
            if (addresses == null || addresses.size() == 0) {
                return null;
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> result) {
            if (result == null) {
                Toast.makeText(mContext, "Get location failed, please try later", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> addressNames = new ArrayList<String>();
            for (Address address : result) {
                addressNames.add(address.getFeatureName());
            }

            mLocationListView.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1,
                    addressNames));
        }
    }

    private static final String TAG = "LocationActivity";

}
