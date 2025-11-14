package com.smartparking.app.ui.fragments.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.data.repository.AdminRepository;
import com.smartparking.app.data.source.Result;

public class ScanQrViewModel extends ViewModel {
    private final AdminRepository adminRepository;
    private final MutableLiveData<Result<Booking>> checkInResult = new MutableLiveData<>();

    public ScanQrViewModel(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public LiveData<Result<Booking>> getCheckInResult() {
        return checkInResult;
    }

    public void checkInWithQr(String bookingId, String entryToken) {
        adminRepository.verifyAndCheckInWithQr(bookingId, entryToken).observeForever(checkInResult::setValue);
    }

    public void checkInWithOtp(String otp) {
        adminRepository.verifyAndCheckInWithOtp(otp).observeForever(checkInResult::setValue);
    }
}