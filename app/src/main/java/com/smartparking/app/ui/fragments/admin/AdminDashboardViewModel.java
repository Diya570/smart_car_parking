package com.smartparking.app.ui.fragments.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.repository.AdminRepository;
import com.smartparking.app.data.source.Result;

public class AdminDashboardViewModel extends ViewModel {

    private final AdminRepository adminRepository;

    // THE FIX IS HERE: We initialize the LiveData objects immediately.
    private LiveData<Result<Long>> lotCount;
    private LiveData<Result<Long>> userCount;
    private LiveData<Result<Long>> activeBookingCount;
    private LiveData<Result<Double>> totalRevenue;

    public AdminDashboardViewModel() {
        this.adminRepository = new AdminRepository();
        // Trigger the fetch in the constructor so the data starts loading as soon as the ViewModel is created.
        fetchDashboardStats();
    }

    // Getters for the UI to observe
    public LiveData<Result<Long>> getLotCount() { return lotCount; }
    public LiveData<Result<Long>> getUserCount() { return userCount; }
    public LiveData<Result<Long>> getActiveBookingCount() { return activeBookingCount; }
    public LiveData<Result<Double>> getTotalRevenue() { return totalRevenue; }

    /**
     * This method now assigns the LiveData objects returned by the repository.
     */
    public void fetchDashboardStats() {
        lotCount = adminRepository.getLotCount();
        userCount = adminRepository.getUserCount();
        activeBookingCount = adminRepository.getActiveBookingCount();
        totalRevenue = adminRepository.getTotalRevenue();
    }
}