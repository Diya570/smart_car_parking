package com.smartparking.app.data.source;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A generic wrapper class that contains a value with its loading status.
 * This is used to communicate the state of a data request from the repository to the UI.
 * @param <T> The type of the data.
 */
public class Result<T> {

    @NonNull
    public final Status status;

    @Nullable
    public final T data;

    @Nullable
    public final String message;


    private Result(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Result<T> success(@NonNull T data) {
        return new Result<>(Status.SUCCESS, data, null);
    }

    public static <T> Result<T> error(String msg, @Nullable T data) {
        return new Result<>(Status.ERROR, data, msg);
    }

    public static <T> Result<T> loading(@Nullable T data) {
        return new Result<>(Status.LOADING, data, null);
    }

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }
}