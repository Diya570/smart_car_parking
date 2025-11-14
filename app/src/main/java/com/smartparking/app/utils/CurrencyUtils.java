package com.smartparking.app.utils;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyUtils {
    public static String formatCurrency(double amount, String currencyCode) {
        try {
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
            format.setCurrency(Currency.getInstance(currencyCode));
            return format.format(amount);
        } catch (Exception e) {
            // Fallback if currency code is invalid
            return String.format(Locale.US, "%s %.2f", currencyCode, amount);
        }
    }
}