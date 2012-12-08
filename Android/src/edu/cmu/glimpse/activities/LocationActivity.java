package edu.cmu.glimpse.activities;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import edu.cmu.glimpse.entry.EntryPlace;
import edu.cmu.glimpse.modules.GooglePlaceClient;
import edu.cmu.glimpse.widget.GlimpseMyLocationOverlay;
import edu.cmu.glimpse.widget.GlimpseMyLocationOverlay.OnLocationChangedListener;

public class LocationActivity extends MapActivity {
    private MapView mMapView;
    private ListView mLocationListView;
    private GlimpseMyLocationOverlay mMyLocationOverlay;
    private ProgressDialog mLoadingDialog;
    private GooglePlaceClient mGooglePlaceClient;

    private List<EntryPlace> mPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.setBuiltInZoomControls(true);
        mMyLocationOverlay = new GlimpseMyLocationOverlay(this, mMapView);
        mMyLocationOverlay.setOnLocationChangedListener(new OnLocationChangedListener() {

            public void onLocationChanged(Location location) {
                new GooglePlaceTask(LocationActivity.this).execute(location);
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
        mLocationListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EntryPlace selected = (EntryPlace) mLocationListView.getItemAtPosition(position);

                Bundle bundle = new Bundle();
                bundle.putParcelable("selected", selected);
                Intent intent = new Intent();
                intent.putExtras(bundle);

                setResult(Activity.RESULT_OK, intent);
                finish();
            }

        });

        mLoadingDialog = ProgressDialog.show(this, "Loading", "Loading location data, just one second...", true);

        mGooglePlaceClient = GooglePlaceClient.getInstance();

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

    private class GooglePlaceTask extends AsyncTask<Location, Void, Void> {
        Context mContext;

        GooglePlaceTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Location... params) {
            try {
                mPlaces = mGooglePlaceClient.execute(params[0]);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            if (mPlaces == null) {
                Toast.makeText(mContext, "Get location failed, please try later", Toast.LENGTH_SHORT).show();
                return;
            }

            mLoadingDialog.dismiss();
            mLocationListView
                    .setAdapter(new ArrayAdapter<EntryPlace>(mContext, android.R.layout.simple_list_item_1, mPlaces));
        }
    }

    // private static final String TAG = "LocationActivity";

}
