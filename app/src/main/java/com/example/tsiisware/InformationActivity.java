package com.example.tsiisware;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InformationActivity extends AppCompatActivity {
    ProgressBar pb;
    FirebaseFirestore db;
    Boolean isCurrent = false;
    Switch switchButton;
    String label = null;
    String category = null;
    TextView quizQuestion, progressNum, progressMax, title, information;
    WebView webView;
    Button gobackButton, answer1, answer2, answer3, answer4, resetVideo;
    String correctAnswer;
    Integer totalQuestions, questionProgress, correctQuestions, wrongQuestions;
    ImageView imageObject;
    Float progress;
    Float progressPercentage;
    Boolean isCorrect;
    String explainText;
    ArrayList<String> scannedLabels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        label = getIntent().getStringExtra("label");
        scannedLabels = getIntent().getStringArrayListExtra("scannedLabels");
        category = getIntent().getStringExtra("category");

        // If the category is a quiz, retrieve the progress from the shared preferences.
        if (Objects.equals(category, "Quiz")) {
            SharedPreferences sharedPreferences = getSharedPreferences("quizData", Context.MODE_PRIVATE);
            correctQuestions = sharedPreferences.getInt("correctQuestions", 0);
            wrongQuestions = sharedPreferences.getInt("wrongQuestions", 0);
            questionProgress = sharedPreferences.getInt("questionProgress", 0);
        }

        // Set the layout based on the category.
        switch (category) {
            case "Quiz":
                setContentView(R.layout.activity_main_informationview_quiz);
                answer1 = findViewById(R.id.answer_1);
                answer2 = findViewById(R.id.answer_2);
                answer3 = findViewById(R.id.answer_3);
                resetVideo = findViewById(R.id.resetVideobtn);
                answer4 = findViewById(R.id.answer_4);
                quizQuestion = findViewById(R.id.question);
                pb = findViewById(R.id.progressBar);
                progressNum = findViewById(R.id.progressNumber);
                progressMax = findViewById(R.id.progressMax);

                db = FirebaseFirestore.getInstance();
                CollectionReference objectItems = db.collection("quiz_objects");
                // Counts how many records are in the object table.
                AggregateQuery queryCount = objectItems.count();
                queryCount.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<AggregateQuerySnapshot> task1) {
                        if (task1.isSuccessful()) {
                            AggregateQuerySnapshot snapshot = task1.getResult();
                            if (snapshot != null) {
                                totalQuestions = (int) snapshot.getCount();
                                progressMax.setText(String.valueOf(totalQuestions));

                                progressBar(questionProgress);
                            }
                        } else {
                            Log.e("Error", "Task failed: ", task1.getException());
                        }
                    }
                });

                // Answer OnClickListeners
                answer1.setOnClickListener(v -> {
                    if (correctAnswer.equals(answer1.getText().toString())) {
                        // Correct answer
                        answer1.setBackgroundColor(Color.GREEN);
                        correctQuestions++;
                        questionProgress++;
                        isCorrect = true;
                        progressBar(questionProgress);
                    } else {
                        // Wrong answer
                        answer1.setBackgroundColor(Color.RED);
                        wrongQuestions++;
                        questionProgress++;
                        isCorrect = false;
                        progressBar(questionProgress);
                    }
                    showPopup(isCorrect);
                });

                answer2.setOnClickListener(v -> {
                    if (correctAnswer.equals(answer2.getText().toString())) {
                        // Correct answer
                        answer2.setBackgroundColor(Color.GREEN);
                        correctQuestions++;
                        questionProgress++;
                        isCorrect = true;
                        progressBar(questionProgress);
                    } else {
                        // Wrong answer
                        answer2.setBackgroundColor(Color.RED);
                        wrongQuestions++;
                        questionProgress++;
                        isCorrect = false;
                        progressBar(questionProgress);
                    }
                    showPopup(isCorrect);
                });

                answer3.setOnClickListener(v -> {
                    if (correctAnswer.equals(answer3.getText().toString())) {
                        // Correct answer
                        answer3.setBackgroundColor(Color.GREEN);
                        correctQuestions++;
                        questionProgress++;
                        isCorrect = true;
                        progressBar(questionProgress);
                    } else {
                        // Wrong answer
                        answer3.setBackgroundColor(Color.RED);
                        wrongQuestions++;
                        questionProgress++;
                        isCorrect = false;
                        progressBar(questionProgress);
                    }
                    showPopup(isCorrect);
                });

                answer4.setOnClickListener(v -> {
                    if (correctAnswer.equals(answer4.getText().toString())) {
                        // Correct answer
                        answer4.setBackgroundColor(Color.GREEN);
                        correctQuestions++;
                        questionProgress++;
                        isCorrect = true;
                        progressBar(questionProgress);
                    } else {
                        // Wrong answer
                        answer4.setBackgroundColor(Color.RED);
                        wrongQuestions++;
                        questionProgress++;
                        isCorrect = false;
                        progressBar(questionProgress);
                    }
                    showPopup(isCorrect);
                });
                break;
            case "Text + Video":
                setContentView(R.layout.activity_main_informationview_text_video);
                switchButton = findViewById(R.id.switch_past);
                title = findViewById(R.id.titleTextVideo);
                title.setText(label);
                resetVideo = findViewById(R.id.resetVideobtn);
                information = findViewById(R.id.informationText);
                information.setText("Loading...");
                imageObject = findViewById(R.id.imageObject);
                switchButton.setOnClickListener(v -> {
                            isCurrent = switchButton.isChecked();
                            getObjectInformation(label, category);
                        }
                );
                break;
            default:
                setContentView(R.layout.qr_view);
        }

        // Set the layout for the QR view.
        gobackButton = findViewById(R.id.go_back);
        webView = findViewById(R.id.webView);
        gobackButton = findViewById(R.id.go_back);
        getObjectInformation(label, category);

        // OnClickListeners
        gobackButton.setOnClickListener(v -> goBackToQRView());
        resetVideo.setOnClickListener(v -> webView.reload());
    }

    // Retrieves the information of the object from the database.
    private void getObjectInformation(String label, String category) {
        db = FirebaseFirestore.getInstance();
        CollectionReference objectItems = category.equals("Text + Video") ? db.collection("video_objects") : db.collection("quiz_objects");
        objectItems.document(label.toLowerCase()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (category.equals("Text + Video")) {
                    switchButton.setVisibility(Boolean.TRUE.equals(document.getBoolean("isPastPresent")) ? View.VISIBLE : View.GONE);
                }
                if (document.exists()) {
                    String description;
                    String videoUrl;
                    String imageUrl;

                    if (category.equals("Text & Video")) {
                        description = isCurrent ? document.getString("description_past") : document.getString("description_present");
                        videoUrl = isCurrent ? document.getString("video_url_past") : document.getString("video_url_present");
                        imageUrl = isCurrent ? document.getString("image_url_past") : document.getString("image_url_present");
                    } else {
                        description = document.getString("description");
                        videoUrl = document.getString("video_url_past");
                        imageUrl = document.getString("image_url_past");
                    }

                    QRObject qrobject = new QRObject(
                            document.getString("name"),
                            description,
                            videoUrl,
                            document.getString("question"),
                            (List<String>) document.get("answers"),
                            document.getString("correct_answer"),
                            document.getString("explanation"),
                            imageUrl

                    );
                    // Load the video in the WebView
                    String iframeStructure = String.format("<iframe width=\"100%%\" height=\"100%%\" src=\"https://www.youtube.com/embed/%s\" frameborder=\"0\" allowfullscreen></iframe>", qrobject.getVideoURL().split("v=")[1]);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                    webView.setWebViewClient(new WebViewClient());
                    webView.setWebChromeClient(new WebChromeClient());
                    webView.loadData(iframeStructure, "text/html", "utf-8");

                    // Disable user interaction
                    webView.setOnTouchListener((v, event) -> {
                        int width = webView.getWidth();
                        int height = webView.getHeight();
                        float x = event.getX();
                        float y = event.getY();

                        // Define the middle area (e.g., 5% of the width and height)
                        int middleAreaWidth = (int) (width * 0.10);
                        int middleAreaHeight = (int) (height * 0.10);
                        int middleXStart = (width - middleAreaWidth) / 2;
                        int middleYStart = (height - middleAreaHeight) / 2;

                        if (x >= middleXStart && x <= (middleXStart + middleAreaWidth) &&
                                y >= middleYStart && y <= (middleYStart + middleAreaHeight)) {
                            return false; // Allow touch event
                        } else {
                            return true; // Ignore touch event
                        }
                    });

                    // Set the information based on the category.
                    if (category.equals("Quiz")) {
                        quizQuestion.setText(qrobject.getQuestion());
                        List<String> answers = qrobject.getAnswers();
                        if (answers != null && answers.size() >= 4) {
                            answer1.setText(answers.get(0));
                            answer2.setText(answers.get(1));
                            answer3.setText(answers.get(2));
                            answer4.setText(answers.get(3));
                        } else {
                            Log.e("Error", "Answers list is null or does not contain enough elements.");
                        }

                        correctAnswer = qrobject.getCorrectAnswer();
                        explainText = qrobject.getExplanation();
                    } else if (category.equals("Text + Video")) {
                        information.setText(qrobject.getDescription());
                        String imagePath = qrobject.getImageURL();
                        try {
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            Glide.with(this)
                                    .asBitmap()
                                    .load(bitmap)
                                    .into(imageObject);
                        } catch (Exception e) {
                            Log.e("ImageError", "Failed to decode the image from content URI.", e);
                        }
                    }
                }
            }
        });
    }

    private void showPopup(boolean isCorrect) {
        //Set up a new .xml file for the Pop Up
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.popup_answer, null);

        //Create Pop Up
        Dialog popupWindow = new Dialog(this);
        popupWindow.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //Set background transparent
        popupWindow.setContentView(popupView); //Pop Up gets the design of the popup_answer.xml layout.

        //Prepare Pop Up Attributes
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(popupWindow.getWindow().getAttributes());
        layoutParams.width = 800;
        layoutParams.height = 500;
        layoutParams.gravity = Gravity.TOP;
        layoutParams.x = 0;
        layoutParams.y = 350;

        // Prevent disabling the pop up when you click outside it.
        popupWindow.setCancelable(false);
        popupWindow.show();
        popupWindow.getWindow().setAttributes(layoutParams); //Sets the attributes
        TextView explanationTxt = popupView.findViewById(R.id.questionExplanation);
        TextView popupTitle = popupView.findViewById(R.id.popupTitle);
        Button continueButton = popupView.findViewById(R.id.btnContinue);
        explanationTxt.setText(explainText);
        explanationTxt.setPadding(5, 5, 5,5);

        //Checks whether the answer of the question is correct. Changes the layout depending on the result.
        if (isCorrect) {
            popupView.setBackgroundResource(R.drawable.question_correct_background);
            popupTitle.setText(R.string.goedAntw);
        }
        else {
            popupView.setBackgroundResource(R.drawable.question_wrong_background);
            popupTitle.setText(R.string.foutAntw);
            continueButton.setBackgroundColor(getColor(R.color.wrong));
        }
        continueButton.setOnClickListener(v -> { goBackToQRView(); });
    }

    // Updates the progress bar.
    private void progressBar(Integer questionProgress) {
        progressNum.setText(String.valueOf(questionProgress));

        // Calculates the percentage of the progress
        progress = (float) questionProgress / (float) totalQuestions;
        progressPercentage = progress * 100;
        int roundedPercentage = Math.round(progressPercentage); //Rounds the percentage to a whole number.
        pb.setProgress(roundedPercentage);
    }

    // Returns to the AR view.
    private void goBackToQRView() {
        if (category.equals("Quiz")) {
            Log.d("QuestionProgress", String.valueOf(questionProgress));
            Log.d("TotalQuestions", String.valueOf(totalQuestions));
            if (totalQuestions > questionProgress) {
                // Go back to the QR view
                Intent intent = new Intent(InformationActivity.this, QR_Activity.class);
                intent.putExtra("label", label);
                intent.putStringArrayListExtra("scannedLabels", scannedLabels);
                intent.putExtra("category", category);
                SharedPreferences sharedPreferences = getSharedPreferences("quizData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("correctQuestions", correctQuestions);
                editor.putInt("wrongQuestions", wrongQuestions);
                editor.putInt("questionProgress", questionProgress);
                editor.apply();
                startActivity(intent);
            } else {
                // Go to the end quiz view
                Intent endQuizView = new Intent(InformationActivity.this, EndQuizActivity.class);
                SharedPreferences sharedPreferences = getSharedPreferences("quizData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("correctQuestions", correctQuestions);
                editor.putInt("wrongQuestions", wrongQuestions);
                editor.putInt("questionProgress", questionProgress);
                editor.apply();
                startActivity(endQuizView);
            }
        }
        else {
            // Go back to the QR view
            Intent intent = new Intent(InformationActivity.this, QR_Activity.class);
            intent.putExtra("label", label);
            intent.putStringArrayListExtra("scannedLabels", scannedLabels);
            intent.putExtra("category", category);
            startActivity(intent);
        }
    }
}
