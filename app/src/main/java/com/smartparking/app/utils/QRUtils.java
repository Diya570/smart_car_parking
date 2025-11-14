package com.smartparking.app.utils;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

public class QRUtils {

    /**
     * Generates a QR Code bitmap from a JSON string.
     * @param bookingId The unique ID of the booking.
     * @param userId The user's ID.
     * @param lotId The parking lot's ID.
     * @param entryToken The secret token for verification.
     * @return A Bitmap of the QR code, or null if an error occurs.
     */
    public static Bitmap generateQrCode(String bookingId, String userId, String lotId, String entryToken) {
        JSONObject json = new JSONObject();
        try {
            json.put("bookingId", bookingId);
            json.put("userId", userId);
            json.put("lotId", lotId);
            json.put("entryToken", entryToken);
        } catch (JSONException e) {
            Timber.e(e, "Failed to create JSON for QR code.");
            return null;
        }

        String content = json.toString();
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            // Define the size of the QR code bitmap
            return barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 600, 600);
        } catch (Exception e) {
            Timber.e(e, "Failed to encode QR code bitmap.");
            return null;
        }
    }
}