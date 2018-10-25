package com.pearnode.app.placero.provider;

/**
 * Created by USER on 10/16/2017.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;

import java.util.List;
import java.util.UUID;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.custom.LocationPositionReceiver;
import com.pearnode.app.placero.position.Position;

public class GPSLocationProvider implements LocationListener {

    private final Activity activity;
    private LocationPositionReceiver receiver;
    private int timeout = 30;
    private final Position pe = new Position();

    public GPSLocationProvider(Activity activity) {
        this.activity = activity;
    }

    public GPSLocationProvider(Activity activity, LocationPositionReceiver receiver) {
        this.activity = activity;
        this.receiver = receiver;
    }

    public GPSLocationProvider(Activity activity, LocationPositionReceiver receiver, int timeoutSecs) {
        this.activity = activity;
        this.receiver = receiver;
        timeout = timeoutSecs;
    }


    @SuppressLint("MissingPermission")
    public void getLocation() {
        try {
            final LocationManager locationManager = (LocationManager) this.activity.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);

            Looper looper = Looper.myLooper();
            Handler handler = new Handler(looper);
            handler.postDelayed(new Runnable() {
                public void run() {
                    locationManager.removeUpdates(GPSLocationProvider.this);
                    String uniqueId = pe.getId();
                    if (uniqueId.equalsIgnoreCase("")) {
                        notifyFailureForLocationFix();
                    }
                }
            }, 1000 * timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Area area = AreaContext.INSTANCE.getAreaElement();
        List<Position> positions = area.getPositions();
        pe.setName("Position_" + (positions.size() + 1));
        pe.setLng(location.getLongitude());
        pe.setLat(location.getLatitude());
        pe.setAreaRef(area.getId());
        pe.setCreatedOnMillis(System.currentTimeMillis() + "");

        if (receiver != null) {
            receiver.receivedLocationPostion(pe);
        } else {
            ((LocationPositionReceiver) activity).receivedLocationPostion(pe);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (receiver != null) {
            receiver.providerDisabled();
        } else {
            ((LocationPositionReceiver) activity).providerDisabled();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void notifyFailureForLocationFix() {
        if (receiver != null) {
            receiver.locationFixTimedOut();
        } else {
            ((LocationPositionReceiver) activity).locationFixTimedOut();
        }
    }

}