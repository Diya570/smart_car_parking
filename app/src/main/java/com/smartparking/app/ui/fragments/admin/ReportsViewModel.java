package com.smartparking.app.ui.fragments.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.data.repository.AdminRepository;
import com.smartparking.app.data.source.Result;
import java.util.List;

public class ReportsViewModel extends ViewModel {
    private final AdminRepository adminRepository;
    private LiveData<Result<List<Booking>>> reportBookingsResult;

    public ReportsViewModel(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public LiveData<Result<List<Booking>>> getReportBookingsResult() {
        return reportBookingsResult;
    }

    public void fetchBookingsForReport(long startTime, long endTime) {
        reportBookingsResult = adminRepository.getBookingsByDateRange(startTime, endTime);
    }
}