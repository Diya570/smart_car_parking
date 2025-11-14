package com.smartparking.app.ui.fragments.admin;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.smartparking.app.databinding.DialogEnterOtpBinding;
import com.smartparking.app.databinding.FragmentScanQrBinding;
import com.smartparking.app.ui.base.BaseFragment;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

public class ScanQrFragment extends BaseFragment<FragmentScanQrBinding, ScanQrViewModel> {

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_LONG).show();
                } else {
                    handleQrCodeResult(result.getContents());
                }
            });

    @NonNull
    @Override
    protected FragmentScanQrBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentScanQrBinding.inflate(inflater, container, false);
    }

    @NonNull
    @Override
    protected Class<ScanQrViewModel> getViewModelClass() {
        return ScanQrViewModel.class;
    }

    @Override
    protected void setupViews() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        binding.scanButton.setOnClickListener(v -> launchScanner());
        binding.otpButton.setOnClickListener(v -> showOtpDialog());
    }

    @Override
    protected void observeData() {
        viewModel.getCheckInResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Check-in Successful")
                            .setMessage("Successfully checked in booking for slot " + result.data.getSlotLabel())
                            .setPositiveButton("OK", null)
                            .show();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Check-in Failed")
                            .setMessage("Error: " + result.message)
                            .setPositiveButton("OK", null)
                            .show();
                    break;
            }
        });
    }

    private void launchScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan a booking QR code");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        barcodeLauncher.launch(options);
    }

    private void handleQrCodeResult(String contents) {
        try {
            JSONObject json = new JSONObject(contents);
            String bookingId = json.getString("bookingId");
            String entryToken = json.getString("entryToken");
            viewModel.checkInWithQr(bookingId, entryToken);
        } catch (JSONException e) {
            Timber.e(e, "Invalid QR code format.");
            Toast.makeText(getContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void showOtpDialog() {
        DialogEnterOtpBinding dialogBinding = DialogEnterOtpBinding.inflate(LayoutInflater.from(getContext()));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Check-in with OTP")
                .setView(dialogBinding.getRoot())
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Verify", (dialog, which) -> {

                    String otp = dialogBinding.otpEditText.getText().toString().trim();

                    if (TextUtils.isEmpty(otp)) {
                        Toast.makeText(getContext(), "Booking ID and OTP are required.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.checkInWithOtp(otp);
                })
                .show();
    }
}