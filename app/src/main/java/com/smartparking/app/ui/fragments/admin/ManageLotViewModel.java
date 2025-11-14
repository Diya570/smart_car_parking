package com.smartparking.app.ui.fragments.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.data.repository.AdminRepository;
import com.smartparking.app.data.source.Result;
import java.util.List;

public class ManageLotViewModel extends ViewModel {
    private final AdminRepository adminRepository;
    private LiveData<Result<List<ParkingLot>>> allLotsResult;
    private final MutableLiveData<Result<Void>> cudResult = new MutableLiveData<>(); // For Create, Update, Delete

    public ManageLotViewModel(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public LiveData<Result<List<ParkingLot>>> getAllLotsResult() {
        return allLotsResult;
    }

    public LiveData<Result<Void>> getCudResult() {
        return cudResult;
    }

    public void fetchAllLots() {
        allLotsResult = adminRepository.getAllLots();
    }

    public void addLot(ParkingLot lot) {
        adminRepository.addParkingLot(lot).observeForever(cudResult::setValue);
    }

    public void updateLot(ParkingLot lot) {
        adminRepository.updateParkingLot(lot).observeForever(cudResult::setValue);
    }
    public void deleteLot(ParkingLot lot) {
        adminRepository.deleteParkingLot(lot).observeForever(cudResult::setValue);
    }
}