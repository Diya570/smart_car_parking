package com.smartparking.app.utils;

// This class would typically use a library like Retrofit or Volley.
// For simplicity and to avoid adding another large dependency, this example
// shows a conceptual outline. A full implementation would require robust
// background threading and JSON parsing.

import com.google.android.gms.maps.model.LatLng;
import timber.log.Timber;

public class DirectionsApiClient {

    // IMPORTANT: This is a conceptual example. In a real app, you would use
    // a proper HTTP client (like OkHttp + Retrofit) and run this on a background thread.
    // Making network calls on the main thread will crash your app.

    public static void getDirections(LatLng origin, LatLng destination, String apiKey) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&key=" + apiKey;

        Timber.d("Directions API URL: %s", url);

        // TODO:
        // 1. Use an Executor or a library like Retrofit to make this network call on a background thread.
        // 2. Use a library like Gson or Moshi to parse the JSON response into model classes.
        // 3. Extract the polyline string from the response: response.routes[0].overview_polyline.points
        // 4. Decode the polyline string into a List<LatLng>.
        // 5. Pass this list back to the UI (e.g., via a LiveData or callback) to be drawn on the map.
    }
}