package com.smartparking.app.ui.fragments.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.smartparking.app.data.repository.FeedbackRepository;
import com.smartparking.app.data.source.Result;

public class FeedbackViewModel extends ViewModel {
    private final FeedbackRepository feedbackRepository;
    private LiveData<Result<Void>> feedbackResult;

    public FeedbackViewModel(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public void submitFeedback(String lotId, float rating, String comment) {
        feedbackResult = feedbackRepository.submitFeedback(lotId, rating, comment);
    }

    public LiveData<Result<Void>> getFeedbackResult() {
        return feedbackResult;
    }
}