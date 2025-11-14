package com.smartparking.app.ui.fragments.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.data.repository.ParkingRepository;
import com.smartparking.app.data.source.Result;
import java.util.List;

public class BookingDetailsViewModel extends ViewModel {
    private final ParkingRepository parkingRepository;
    private LiveData<Result<List<ParkingLot>>> allLotsResult;

    public BookingDetailsViewModel() {
        this.parkingRepository = new ParkingRepository();
    }

    public LiveData<Result<List<ParkingLot>>> getAllLots() {
        if (allLotsResult == null) {
            allLotsResult = parkingRepository.getAllParkingLots();
        }
        return allLotsResult;
    }
}