package com.smartparking.app.ui.fragments.user;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.data.repository.BookingRepository;
import com.smartparking.app.data.source.Result;
import java.util.List;

public class MyBookingsViewModel extends ViewModel {
    private final BookingRepository bookingRepository;
    private LiveData<Result<List<Booking>>> userBookingsResult;

    // THE FIX IS HERE: Change to MutableLiveData and initialize it immediately.
    private final MutableLiveData<Result<Void>> cancelBookingResult = new MutableLiveData<>();

    public MyBookingsViewModel(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public LiveData<Result<List<Booking>>> getUserBookings() {
        if (userBookingsResult == null) {
            userBookingsResult = bookingRepository.getUserBookings();
        }
        return userBookingsResult;
    }

    public void fetchUserBookings() {
        userBookingsResult = bookingRepository.getUserBookings();
    }

    public LiveData<Result<Void>> getCancelBookingResult() {
        return cancelBookingResult;
    }

    public void cancelBooking(Booking booking, Context context) {
        // Now, we observe the result from the repository and post it to our ViewModel's LiveData.
        cancelBookingResult.setValue(Result.loading(null));
        bookingRepository.cancelBooking(booking, context).observeForever(result -> {
            cancelBookingResult.setValue(result);
        });
    }
}