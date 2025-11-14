package com.smartparking.app.ui.fragments.user;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.data.repository.BookingRepository;
import com.smartparking.app.data.source.Result;

public class BookingReviewViewModel extends ViewModel {
    private final BookingRepository bookingRepository;
    // THE FIX IS HERE: Change to MutableLiveData and initialize it immediately.
    private final MutableLiveData<Result<Booking>> createBookingResult = new MutableLiveData<>();

    public BookingReviewViewModel(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public LiveData<Result<Booking>> getCreateBookingResult() {
        return createBookingResult;
    }

    public void createBooking(Booking booking, String lotName, Context context) {
        // Post the loading state to our initialized LiveData
        createBookingResult.setValue(Result.loading(null));
        // Observe the result from the repository
        bookingRepository.createBooking(booking, lotName, context).observeForever(result -> {
            createBookingResult.setValue(result);
        });
    }
}