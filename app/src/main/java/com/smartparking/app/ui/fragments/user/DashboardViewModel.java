package com.smartparking.app.ui.fragments.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.Booking;
import com.smartparking.app.data.model.ParkingLot;
import com.smartparking.app.data.model.ParkingLotWithAvailability;
import com.smartparking.app.data.repository.BookingRepository;
import com.smartparking.app.data.repository.ParkingRepository;
import com.smartparking.app.data.source.Result;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DashboardViewModel extends ViewModel {
    private final BookingRepository bookingRepository;
    private final ParkingRepository parkingRepository;

    private final MediatorLiveData<Result<List<Booking>>> userBookingsResult = new MediatorLiveData<>();
    private LiveData<Result<List<ParkingLot>>> allLotsResult;

    // LiveData for the dashboard features
    private final MutableLiveData<Boolean> isRushHour = new MutableLiveData<>();
    private final MutableLiveData<ParkingLot> favoriteLot = new MutableLiveData<>();
    private final MediatorLiveData<List<ParkingLotWithAvailability>> availabilityTicker = new MediatorLiveData<>();
    private final MutableLiveData<Booking> nextBooking = new MutableLiveData<>();

    public DashboardViewModel() {
        this.bookingRepository = new BookingRepository();
        this.parkingRepository = new ParkingRepository();
        checkIfRushHour();
        loadInitialData();
    }

    // --- Getters for the Fragment to observe ---

    public LiveData<Result<List<Booking>>> getUserBookings() { return userBookingsResult; }
    public LiveData<Boolean> getIsRushHour() { return isRushHour; }
    public LiveData<ParkingLot> getFavoriteLot() { return favoriteLot; }
    public LiveData<List<ParkingLotWithAvailability>> getAvailabilityTicker() { return availabilityTicker; }
    public LiveData<Booking> getNextBooking() { return nextBooking; }
    public LiveData<Result<List<ParkingLot>>> getAllLots() {
        if (allLotsResult == null) {
            allLotsResult = parkingRepository.getAllParkingLots();
        }
        return allLotsResult;
    }

    // --- Data Loading and Processing Logic ---

    private void loadInitialData() {
        // We must have the list of all lots available before processing bookings
        allLotsResult = parkingRepository.getAllParkingLots();

        // When all lots are loaded, then fetch the user's bookings
        allLotsResult.observeForever(lotsResult -> {
            if (lotsResult.status == Result.Status.SUCCESS) {
                userBookingsResult.addSource(bookingRepository.getUserBookings(), result -> {
                    userBookingsResult.setValue(result);
                    if (result.status == Result.Status.SUCCESS && result.data != null) {
                        processBookingHistory(result.data, lotsResult.data);
                    }
                });
            }
        });
    }

    private void processBookingHistory(List<Booking> bookings, List<ParkingLot> allLots) {
        if (allLots == null) return;

        // Find the next upcoming booking
        Booking next = bookings.stream()
                .filter(b -> "confirmed".equalsIgnoreCase(b.getStatus()) && b.getStartTime() > System.currentTimeMillis())
                .min(Comparator.comparingLong(Booking::getStartTime))
                .orElse(null);
        nextBooking.postValue(next);

        // Process the rest of the history
        findFavoriteLot(bookings, allLots);
        findRecentLotsForTicker(bookings, allLots);
    }

    private void checkIfRushHour() {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);

        boolean isWeekday = dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY;
        boolean isPeakTime = (hourOfDay >= 8 && hourOfDay < 11) || (hourOfDay >= 17 && hourOfDay < 20); // 8-11 AM or 5-8 PM

        isRushHour.setValue(isWeekday && isPeakTime);
    }

    private void findFavoriteLot(List<Booking> bookings, List<ParkingLot> allLots) {
        if (bookings == null || bookings.isEmpty()) return;

        bookings.stream()
                .collect(Collectors.groupingBy(Booking::getLotId, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() >= 3) // Favorite threshold is 3+ bookings
                .max(Map.Entry.comparingByValue())
                .ifPresent(favoriteEntry -> {
                    String favoriteLotId = favoriteEntry.getKey();
                    allLots.stream()
                            .filter(p -> favoriteLotId.equals(p.getId()))
                            .findFirst()
                            .ifPresent(favoriteLot::postValue);
                });
    }

    private void findRecentLotsForTicker(List<Booking> bookings, List<ParkingLot> allLots) {
        if (bookings == null || allLots == null || bookings.isEmpty()) return;

        List<String> recentLotIds = bookings.stream()
                .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
                .map(Booking::getLotId)
                .distinct()
                .limit(3)
                .collect(Collectors.toList());

        if (recentLotIds.isEmpty()) return;

        Map<String, ParkingLot> allLotsMap = allLots.stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(ParkingLot::getId, Function.identity()));

        for (String lotId : recentLotIds) {
            ParkingLot lot = allLotsMap.get(lotId);
            if (lot == null) continue;

            LiveData<Result<Integer>> countSource = bookingRepository.getOccupiedSlotsCount(lotId, new Date());
            availabilityTicker.addSource(countSource, countResult -> {
                if (countResult.status == Result.Status.SUCCESS) {
                    List<ParkingLotWithAvailability> currentList = availabilityTicker.getValue();
                    if (currentList == null) currentList = new ArrayList<>();

                    currentList.removeIf(item -> item.getParkingLot().getId().equals(lot.getId()));
                    currentList.add(new ParkingLotWithAvailability(lot, countResult.data));

                    availabilityTicker.setValue(currentList);
                }
                availabilityTicker.removeSource(countSource);
            });
        }
    }
}