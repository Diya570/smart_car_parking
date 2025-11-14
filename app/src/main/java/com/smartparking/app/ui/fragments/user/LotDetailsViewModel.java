package com.smartparking.app.ui.fragments.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.repository.BookingRepository;
import com.smartparking.app.data.source.Result;

import java.util.Date;

public class LotDetailsViewModel extends ViewModel {
    private final BookingRepository bookingRepository;
    // THE FIX IS HERE: Change to MutableLiveData and initialize it.
    private final MutableLiveData<Result<Integer>> occupiedSlotsCount = new MutableLiveData<>();

    public LotDetailsViewModel(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public LiveData<Result<Integer>> getOccupiedSlotsCount() {
        return occupiedSlotsCount;
    }

    public void fetchOccupiedSlotsCount(String lotId) {
        // Post loading state, then observe the result from the repository
        occupiedSlotsCount.setValue(Result.loading(null));
        bookingRepository.getOccupiedSlotsCount(lotId, new Date()).observeForever(result -> {
            occupiedSlotsCount.setValue(result);
        });
    }
}