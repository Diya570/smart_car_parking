package com.smartparking.app.utils;

import android.location.Location;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.firestore.GeoPoint;
public final class GeoUtils {
    private GeoUtils() {}
    public static String getGeoHashForLocation(GeoPoint location) {
        if (location == null) {
            return null;
        }
        return GeoFireUtils.getGeoHashForLocation(new GeoLocation(location.getLatitude(), location.getLongitude()));
    }
    public static double getDistanceInMeters(GeoLocation loc1, GeoLocation loc2) {
        return GeoFireUtils.getDistanceBetween(loc1, loc2);
    }
    public static GeoLocation toGeoLocation(Location location) {
        return new GeoLocation(location.getLatitude(), location.getLongitude());
    }
    public static GeoLocation toGeoLocation(GeoPoint geoPoint) {
        return new GeoLocation(geoPoint.getLatitude(), geoPoint.getLongitude());
    }
}