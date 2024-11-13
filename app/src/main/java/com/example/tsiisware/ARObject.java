package com.example.tsiisware;

import android.net.Uri;
import java.util.List;

public class ARObject {
    private String name;
    private String description;
    private Uri videoURL;
    private String question;
    private List<String> answers;
    private String correctAnswer;

    public ARObject() {
        // No-argument constructor required for Firestore deserialization
    }

    public ARObject(String name, String description, Uri videoURL, String question, List<String> answers, String correctAnswer) {
        this.name = name;
        this.description = description;
        this.videoURL = videoURL;
        this.question = question;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Uri getVideoURL() {
        return videoURL;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}