package edu.cmu.glimpse.modules;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class LocationModule {

    public interface OnLocationUpdatedListener {
        void updateLocation(Location location);
    }

    private static final long MIN_UPDATE_Time = 0l;
    private static final float MIN_UPDATE_DISTANCE = 0f;
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private final Context mContext;
    private final LocationManager mLocationManager;
    private final OnLocationUpdatedListener mOnLocationUpdatedListener;

    private Timer mTimer;
    private boolean mIsUpdating;
    private Location mCurrentBestLocation;

    public LocationModule(Context context, OnLocationUpdatedListener onLocationUpdatedListener) {
        mContext = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mOnLocationUpdatedListener = onLocationUpdatedListener;
    }

    public synchronized boolean startLocationUpdate() {
        if (mIsUpdating) {
            return false;
        }

        mIsUpdating = true;
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        // exceptions will be thrown if provider is not permitted.
        try {
            gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            networkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        // don't start listeners if no provider is enabled
        if (!gpsEnabled && !networkEnabled) {
            // TODO: Show a dialog to ask user turn GPS, Network on
            return false;
        }

        if (gpsEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_Time,
                    MIN_UPDATE_DISTANCE, mLocationListenerGps);
            Location lastKnownGpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (isBetterLocation(lastKnownGpsLocation, mCurrentBestLocation)) {
                mCurrentBestLocation = lastKnownGpsLocation;
                mOnLocationUpdatedListener.updateLocation(lastKnownGpsLocation);
            }
        }
        if (networkEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_UPDATE_Time,
                    MIN_UPDATE_DISTANCE, mLocationListenerNetwork);
            Location lastKnownNetworkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (isBetterLocation(lastKnownNetworkLocation, mCurrentBestLocation)) {
                mCurrentBestLocation = lastKnownNetworkLocation;
                mOnLocationUpdatedListener.updateLocation(lastKnownNetworkLocation);
            }
        }
        mTimer = new Timer();
        mTimer.schedule(new GetLastLocation(gpsEnabled, networkEnabled), 20000);
        return true;
    }

    public synchronized void stopLocationUpdate() {
        if (!mIsUpdating) {
            return;
        }

        mTimer.cancel();
        mLocationManager.removeUpdates(mLocationListenerGps);
        mLocationManager.removeUpdates(mLocationListenerNetwork);
        mIsUpdating = false;
    }

    private final LocationListener mLocationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            mTimer.cancel();
            if (isBetterLocation(location, mCurrentBestLocation)) {
                mCurrentBestLocation = location;
                mOnLocationUpdatedListener.updateLocation(location);
            }
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(mContext, "Disable provider " + provider, Toast.LENGTH_SHORT).show();
        }

        public void onProviderEnabled(String provider) {
            Toast.makeText(mContext, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private final LocationListener mLocationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            mTimer.cancel();
            if (isBetterLocation(location, mCurrentBestLocation)) {
                mCurrentBestLocation = location;
                mOnLocationUpdatedListener.updateLocation(location);
            }
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(mContext, "Disable provider " + provider, Toast.LENGTH_SHORT).show();
        }

        public void onProviderEnabled(String provider) {
            Toast.makeText(mContext, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private class GetLastLocation extends TimerTask {
        boolean mGpsEnabled;
        boolean mNetworkEnabled;

        public GetLastLocation(boolean gpsEnabled, boolean networkEnabled) {
            mGpsEnabled = gpsEnabled;
            mNetworkEnabled = networkEnabled;
        }

        @Override
        public void run() {
            mLocationManager.removeUpdates(mLocationListenerGps);
            mLocationManager.removeUpdates(mLocationListenerNetwork);

            Location networkLocation = null, gpsLocation = null;
            if (mGpsEnabled) {
                gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (mNetworkEnabled) {
                networkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            Location betterLocation = getBetterLocation(gpsLocation, networkLocation);
            mOnLocationUpdatedListener.updateLocation(betterLocation);
        }
    }

    /**
     * Determines which location is better
     * 
     * @param location1
     * @param location2
     * @return The better location, location2 if tie
     */
    private Location getBetterLocation(Location location1, Location location2) {
        return isBetterLocation(location1, location2) ? location1 : location2;
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     * 
     * @param location
     *            The new Location that you want to evaluate
     * @param currentBestLocation
     *            The current Location fix, to which you want to compare the new one
     */
    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (location == null) {
            return false;
        }

        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
