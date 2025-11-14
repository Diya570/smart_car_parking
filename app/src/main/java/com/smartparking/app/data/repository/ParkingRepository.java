package com.smartparking.app.data.repository;

import android.location.Location;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.smartparking.app.constants.Constants;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.data.model.Slot;
import com.smartparking.app.data.source.Result;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import timber.log.Timber;

public class ParkingRepository {
    private final FirebaseFirestore firestore;

    public ParkingRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public LiveData<Result<List<ParkingLot>>> getAllParkingLots() {
        MutableLiveData<Result<List<ParkingLot>>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_PARKING_LOTS)
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ParkingLot> lots = queryDocumentSnapshots.toObjects(ParkingLot.class);
                    result.setValue(Result.success(lots));
                })
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }

    public LiveData<Result<List<ParkingLot>>> getNearbyParkingLots(Location center) {
        MutableLiveData<Result<List<ParkingLot>>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        final GeoLocation centerLocation = new GeoLocation(center.getLatitude(), center.getLongitude());
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(centerLocation, Constants.GEO_QUERY_RADIUS_METERS);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (GeoQueryBounds b : bounds) {
            Query q = firestore.collection(Constants.COLLECTION_PARKING_LOTS)
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);
            tasks.add(q.get());
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> {
            Set<String> uniqueLotIds = new HashSet<>();
            List<ParkingLot> matchingLots = new ArrayList<>();

            for (Task<QuerySnapshot> task : tasks) {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        ParkingLot lot = doc.toObject(ParkingLot.class);
                        if (lot != null && lot.getLocation() != null && !uniqueLotIds.contains(lot.getId())) {
                            GeoLocation docLocation = new GeoLocation(lot.getLocation().getLatitude(), lot.getLocation().getLongitude());
                            if (GeoFireUtils.getDistanceBetween(docLocation, centerLocation) <= Constants.GEO_QUERY_RADIUS_METERS) {
                                matchingLots.add(lot);
                                uniqueLotIds.add(lot.getId());
                            }
                        }
                    }
                } else {
                    Timber.e(task.getException(), "A geo-query task failed.");
                }
            }
            result.setValue(Result.success(matchingLots));
        }).addOnFailureListener(e -> {
            Timber.e(e, "All geo-query tasks failed.");
            result.setValue(Result.error(e.getMessage(), null));
        });

        return result;
    }

    public LiveData<Result<List<Slot>>> getSlotsForLot(String lotId) {
        MutableLiveData<Result<List<Slot>>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));
        firestore.collection(Constants.COLLECTION_PARKING_LOTS).document(lotId)
                .collection(Constants.COLLECTION_SLOTS)
                .whereEqualTo("active", true)
                .orderBy("label")
                .get()
                .addOnSuccessListener(snaps -> result.setValue(Result.success(snaps.toObjects(Slot.class))))
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));
        return result;
    }

    public Task<DocumentSnapshot> getLotDocument(String lotId) {
        return firestore.collection(Constants.COLLECTION_PARKING_LOTS).document(lotId).get();
    }
}