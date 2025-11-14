package com.smartparking.app.ui.fragments.admin;

import android.app.DatePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.smartparking.app.databinding.FragmentReportsBinding;
import com.smartparking.app.ui.base.BaseFragment;
import com.smartparking.app.utils.CsvUtils;
import com.smartparking.app.utils.TimeUtils;
import java.util.Calendar;

public class ReportsFragment extends BaseFragment<FragmentReportsBinding, ReportsViewModel> {

    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();

    @NonNull
    @Override
    protected FragmentReportsBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentReportsBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<ReportsViewModel> getViewModelClass() {
        return ReportsViewModel.class;
    }

    @Override
    protected void setupViews() {
        updateDateDisplays();
        binding.startDateLayout.setOnClickListener(v -> showDatePicker(true));
        binding.endDateLayout.setOnClickListener(v -> showDatePicker(false));
        binding.generateReportButton.setOnClickListener(v -> {
            viewModel.fetchBookingsForReport(startCalendar.getTimeInMillis(), endCalendar.getTimeInMillis());
        });
    }

    @Override
    protected void observeData() {
        viewModel.getReportBookingsResult().observe(getViewLifecycleOwner(), result -> {
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.generateReportButton.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.generateReportButton.setEnabled(true);
                    if (result.data != null && !result.data.isEmpty()) {
                        String fileName = "report-" + TimeUtils.formatDateForFilename(System.currentTimeMillis()) + ".csv";
                        CsvUtils.exportBookingsToCsv(requireContext(), result.data, fileName);
                    } else {
                        Toast.makeText(getContext(), "No bookings found in the selected date range.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.generateReportButton.setEnabled(true);
                    Toast.makeText(getContext(), "Error generating report: " + result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendarToUpdate = isStartDate ? startCalendar : endCalendar;
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendarToUpdate.set(year, month, dayOfMonth);
            updateDateDisplays();
        }, calendarToUpdate.get(Calendar.YEAR), calendarToUpdate.get(Calendar.MONTH), calendarToUpdate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateDisplays() {
        binding.startDateText.setText(TimeUtils.formatDate(startCalendar.getTimeInMillis()));
        binding.endDateText.setText(TimeUtils.formatDate(endCalendar.getTimeInMillis()));
    }
}