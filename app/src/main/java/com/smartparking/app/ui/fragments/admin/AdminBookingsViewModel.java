package com.smartparking.app.ui.fragments.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.data.repository.AdminRepository;
import com.smartparking.app.data.source.Result;
import java.util.List;

public class AdminBookingsViewModel extends ViewModel {
    private final AdminRepository adminRepository;
    private LiveData<Result<List<Booking>>> allBookingsResult;

    public AdminBookingsViewModel(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public LiveData<Result<List<Booking>>> getAllBookingsResult() {
        if (allBookingsResult == null) {
            allBookingsResult = adminRepository.getAllBookings();
        }
        return allBookingsResult;
    }

    public void fetchAllBookings() {
        allBookingsResult = adminRepository.getAllBookings();
    }
}