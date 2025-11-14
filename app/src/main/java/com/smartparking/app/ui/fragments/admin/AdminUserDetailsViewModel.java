package com.smartparking.app.ui.fragments.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.data.repository.AdminRepository;
import com.smartparking.app.data.source.Result;
import java.util.List;

public class AdminUserDetailsViewModel extends ViewModel {
    private final AdminRepository adminRepository;
    private LiveData<Result<List<Booking>>> userBookings;

    public AdminUserDetailsViewModel() {
        this.adminRepository = new AdminRepository();
    }

    public LiveData<Result<List<Booking>>> getUserBookings() {
        return userBookings;
    }

    public void fetchBookingsForUser(String userId) {
        userBookings = adminRepository.getBookingsForUser(userId);
    }
}