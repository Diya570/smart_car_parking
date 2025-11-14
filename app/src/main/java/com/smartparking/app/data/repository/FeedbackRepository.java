package com.smartparking.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartparking.app.constants.Constants;
import com.smartparking.app.data.model.Feedback;
import com.smartparking.app.data.source.Result;

import java.util.Objects;

public class FeedbackRepository {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public FeedbackRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public LiveData<Result<Void>> submitFeedback(String lotId, float rating, String comment) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.setValue(Result.loading(null));

        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setLotId(lotId);
        feedback.setRating(rating);
        feedback.setComment(comment);

        firestore.collection(Constants.COLLECTION_FEEDBACK)
                .add(feedback)
                .addOnSuccessListener(documentReference -> result.setValue(Result.success(null)))
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage(), null)));

        return result;
    }
}