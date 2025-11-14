package com.smartparking.app.ui.fragments.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.Slot;
import com.smartparking.app.data.repository.AdminRepository;
import com.smartparking.app.data.repository.ParkingRepository;
import com.smartparking.app.data.source.Result;
import java.util.List;

public class ManageSlotsViewModel extends ViewModel {
    private final ParkingRepository parkingRepository;
    private final AdminRepository adminRepository;
    private LiveData<Result<List<Slot>>> slotsResult;
    private final MutableLiveData<Result<Void>> cudResult = new MutableLiveData<>();

    public ManageSlotsViewModel(ParkingRepository parkingRepository) {
        this.parkingRepository = parkingRepository;
        this.adminRepository = new AdminRepository();
    }

    public LiveData<Result<List<Slot>>> getSlotsResult() {
        return slotsResult;
    }

    public LiveData<Result<Void>> getCudResult() {
        return cudResult;
    }

    public void fetchSlotsForLot(String lotId) {
        slotsResult = parkingRepository.getSlotsForLot(lotId);
    }

    public void addSlot(String lotId, Slot slot) {
        adminRepository.addSlot(lotId, slot).observeForever(cudResult::setValue);
    }

    // CHANGE: lotId parameter is required again
    public void updateSlot(String lotId, Slot slot) {
        adminRepository.updateSlot(lotId, slot).observeForever(cudResult::setValue);
    }

    // CHANGE: lotId parameter is required again
    public void deleteSlot(String lotId, String slotId) {
        adminRepository.deleteSlot(lotId, slotId).observeForever(cudResult::setValue);
    }
}