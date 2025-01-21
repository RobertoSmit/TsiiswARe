package com.example.tsiisware;

import java.util.List;

public class QRObject {
    private String name;
    private String description;
    private String video;
    private String question;
    private List<String> answers;
    private String correctAnswer;
    private String explanation;
    private String imageURL;

    public QRObject() {
        // No-argument constructor required for Firestore deserialization
    }

    public QRObject(String name, String description, String video, String question, List<String> answers, String correctAnswer, String explanation, String imageURl) {
        this.name = name;
        this.description = description;
        this.video = video;
        this.question = question;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.imageURL = imageURl;
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

    public String getCorrectAnswer() { return correctAnswer;}

    public String getExplanation() { return explanation; }

    public String getImageURL() { return imageURL; }
}