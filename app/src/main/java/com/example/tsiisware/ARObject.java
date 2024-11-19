package com.example.tsiisware;

import java.util.List;

public class ARObject {
    private String name;
    private String description;
    private String video;
    private String question;
    private List<String> answers;
    private String correctAnswer;

    public ARObject() {
        // No-argument constructor required for Firestore deserialization
    }

    public ARObject(String name, String description, String video, String question, List<String> answers, String correctAnswer) {
        this.name = name;
        this.description = description;
        this.video = video;
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

    public String getVideoURL() {
        return video;
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