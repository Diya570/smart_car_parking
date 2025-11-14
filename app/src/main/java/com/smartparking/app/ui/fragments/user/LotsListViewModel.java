package com.smartparking.app.ui.fragments.user;

import android.location.Location;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.data.model.ParkingLotWithAvailability;
import com.smartparking.app.data.repository.BookingRepository;
import com.smartparking.app.data.repository.ParkingRepository;
import com.smartparking.app.data.source.Result;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LotsListViewModel extends ViewModel {
    private final ParkingRepository parkingRepository;
    private final BookingRepository bookingRepository;
    private final MediatorLiveData<Result<List<ParkingLotWithAvailability>>> lotsResult = new MediatorLiveData<>();

    public LotsListViewModel(ParkingRepository parkingRepository) {
        this.parkingRepository = parkingRepository;
        this.bookingRepository = new BookingRepository();
    }

    public LiveData<Result<List<ParkingLotWithAvailability>>> getLotsResult() {
        return lotsResult;
    }

    public void loadParkingLots(boolean isNearbyOnly, @Nullable Location location) {
        lotsResult.setValue(Result.loading(null));

        final LiveData<Result<List<ParkingLot>>> lotsSource;
        if (isNearbyOnly) {
            if (location == null) {
                lotsResult.setValue(Result.error("Location is required to find nearby lots.", null));
                return;
            }
            lotsSource = parkingRepository.getNearbyParkingLots(location);
        } else {
            lotsSource = parkingRepository.getAllParkingLots();
        }

        lotsResult.addSource(lotsSource, result -> {
            if (result.status == Result.Status.SUCCESS && result.data != null) {
                List<ParkingLot> lots = result.data;
                if (lots.isEmpty()) {
                    lotsResult.setValue(Result.success(new ArrayList<>()));
                    return;
                }

                List<ParkingLotWithAvailability> listWithAvailability = new ArrayList<>();
                AtomicInteger counter = new AtomicInteger(lots.size());

                for (ParkingLot lot : lots) {
                    bookingRepository.getOccupiedSlotsCount(lot.getId(), new Date()).observeForever(countResult -> {
                        if (countResult != null && countResult.status == Result.Status.SUCCESS) {
                            listWithAvailability.add(new ParkingLotWithAvailability(lot, countResult.data));
                        } else {
                            listWithAvailability.add(new ParkingLotWithAvailability(lot, 0));
                        }

                        if (counter.decrementAndGet() == 0) {
                            lotsResult.setValue(Result.success(listWithAvailability));
                            lotsResult.removeSource(lotsSource);
                        }
                    });
                }
            } else if (result.status == Result.Status.ERROR) {
                lotsResult.setValue(Result.error(result.message, null));
                lotsResult.removeSource(lotsSource);
            }
        });
    }
}