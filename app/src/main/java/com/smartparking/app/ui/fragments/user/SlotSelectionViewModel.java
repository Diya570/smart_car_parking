package com.smartparking.app.ui.fragments.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.model.Slot;
import com.smartparking.app.data.model.SlotWithStatus;
import com.smartparking.app.data.repository.BookingRepository;
import com.smartparking.app.data.repository.ParkingRepository;
import com.smartparking.app.data.source.Result;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SlotSelectionViewModel extends ViewModel {
    private final ParkingRepository parkingRepository;
    private final BookingRepository bookingRepository;
    private final MediatorLiveData<Result<List<SlotWithStatus>>> slotsResult = new MediatorLiveData<>();

    public SlotSelectionViewModel(ParkingRepository parkingRepository) {
        this.parkingRepository = parkingRepository;
        this.bookingRepository = new BookingRepository();
    }

    public LiveData<Result<List<SlotWithStatus>>> getSlotsResult() {
        return slotsResult;
    }

    public void fetchSlotsForLot(String lotId, long startTime, long endTime) {
        slotsResult.setValue(Result.loading(null));

        final LiveData<Result<List<Slot>>> allSlotsSource = parkingRepository.getSlotsForLot(lotId);
        final LiveData<Result<Set<String>>> conflictingSlotsSource = bookingRepository.getConflictingSlotIds(lotId, startTime, endTime);

        slotsResult.addSource(allSlotsSource, allSlots -> {
            if (allSlots.status == Result.Status.SUCCESS && conflictingSlotsSource.getValue() != null && conflictingSlotsSource.getValue().status == Result.Status.SUCCESS) {
                mergeResults(allSlots.data, conflictingSlotsSource.getValue().data);
            } else if (allSlots.status == Result.Status.ERROR) {
                slotsResult.setValue(Result.error(allSlots.message, null));
            }
        });

        slotsResult.addSource(conflictingSlotsSource, conflictingSlots -> {
            if (conflictingSlots.status == Result.Status.SUCCESS && allSlotsSource.getValue() != null && allSlotsSource.getValue().status == Result.Status.SUCCESS) {
                mergeResults(allSlotsSource.getValue().data, conflictingSlots.data);
            } else if (conflictingSlots.status == Result.Status.ERROR) {
                slotsResult.setValue(Result.error(conflictingSlots.message, null));
            }
        });
    }

    private void mergeResults(List<Slot> allSlots, Set<String> conflictingSlotIds) {
        if (allSlots == null || conflictingSlotIds == null) return;

        List<SlotWithStatus> finalSlotList = allSlots.stream()
                .map(slot -> {
                    if (conflictingSlotIds.contains(slot.getId())) {
                        return new SlotWithStatus(slot, "Booked");
                    } else {
                        return new SlotWithStatus(slot, "Available");
                    }
                })
                .collect(Collectors.toList());

        slotsResult.setValue(Result.success(finalSlotList));
    }
}