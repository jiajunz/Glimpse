package edu.cmu.glimpse.widget;

import android.content.Context;
import android.location.Location;

import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class GlimpseMyLocationOverlay extends MyLocationOverlay {

    public interface OnLocationChangedListener {
        void onLocationChanged(Location location);
    }

    private OnLocationChangedListener mOnLocationChangedListener;

    public GlimpseMyLocationOverlay(Context context, MapView mapView) {
        super(context, mapView);
    }

    public void setOnLocationChangedListener(OnLocationChangedListener onLocationChangedListener) {
        mOnLocationChangedListener = onLocationChangedListener;
    }

    @Override
    public synchronized void onLocationChanged(Location location) {
        if (mOnLocationChangedListener != null) {
            mOnLocationChangedListener.onLocationChanged(location);
        }

        super.onLocationChanged(location);
    }
}
