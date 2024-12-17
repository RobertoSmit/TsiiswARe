package com.example.tsiisware;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InformationActivity extends AppCompatActivity {
    ProgressBar pb;
    FirebaseFirestore db;
    String label = null;
    String category = null;
    TextView quizQuestion, progressNum, progressMax, title, information;
    WebView webView;
    Button gobackButton, answer1, answer2, answer3, answer4, resetVideo;
    String correctAnswer;
    Integer totalQuestions, questionProgress, correctQuestions, wrongQuestions;
    Float progress;
    Float progressPercentage;
    Boolean isCorrect;
    String explainText;
    ArrayList<String> selectedLabels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        label = getIntent().getStringExtra("label");
        selectedLabels = getIntent().getStringArrayListExtra("selectedLabels");
        category = getIntent().getStringExtra("category");
        if (Objects.equals(category, "Quiz")) {
            SharedPreferences sharedPreferences = getSharedPreferences("quizData", Context.MODE_PRIVATE);
            correctQuestions = sharedPreferences.getInt("correctQuestions", 0);
            wrongQuestions = sharedPreferences.getInt("wrongQuestions", 0);
            questionProgress = sharedPreferences.getInt("questionProgress", 0);
        }

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
                CollectionReference objectItems = db.collection("objects");
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
                title = findViewById(R.id.titleTextVideo);
                title.setText("Text + Video: " + label);
                resetVideo = findViewById(R.id.resetVideobtn);
                information = findViewById(R.id.informationText);
                information.setText("Loading...");
                break;
            case "Video":
                setContentView(R.layout.activity_main_informationview_video);
                title = findViewById(R.id.textViewVideo);
                resetVideo = findViewById(R.id.resetVideobtn);
                title.setText("Video: " + label);
                break;
            default:
                setContentView(R.layout.ar_view);
        }

        gobackButton = findViewById(R.id.go_back);
        webView = findViewById(R.id.webView);
        gobackButton = findViewById(R.id.go_back);
        getObjectInformation(label, category);

        gobackButton.setOnClickListener(v -> goBackToARView());
        resetVideo.setOnClickListener(v -> {
            webView.reload();
        });
    }

    private void getObjectInformation(String label, String category) {
        db = FirebaseFirestore.getInstance();
        CollectionReference objectItems = db.collection("objects");
        objectItems.document(label.toLowerCase()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    ARObject arobject = new ARObject(
                            document.getString("name"),
                            document.getString("description"),
                            document.getString("video_url"),
                            document.getString("question"),
                            (List<String>) document.get("answers"),
                            document.getString("correct_answer"),
                            document.getString("explanation")
                    );
                    Log.d("Video_url", arobject.getVideoURL().split("v=")[1]);
                    String iframeStructure = String.format("<iframe width=\"100%%\" height=\"100%%\" src=\"https://www.youtube.com/embed/%s\" frameborder=\"0\" allowfullscreen></iframe>", arobject.getVideoURL().split("v=")[1]);
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
                        int middleAreaWidth = (int) (width * 0.05);
                        int middleAreaHeight = (int) (height * 0.05);
                        int middleXStart = (width - middleAreaWidth) / 2;
                        int middleYStart = (height - middleAreaHeight) / 2;

                        if (x >= middleXStart && x <= (middleXStart + middleAreaWidth) &&
                                y >= middleYStart && y <= (middleYStart + middleAreaHeight)) {
                            return false; // Allow touch event
                        } else {
                            return true; // Ignore touch event
                        }
                    });

                    if (category.equals("Quiz")) {
                        quizQuestion.setText(arobject.getQuestion());
                        answer1.setText(arobject.getAnswers().get(0));
                        answer2.setText(arobject.getAnswers().get(1));
                        answer3.setText(arobject.getAnswers().get(2));
                        answer4.setText(arobject.getAnswers().get(3));

                        correctAnswer = arobject.getCorrectAnswer();
                        explainText = arobject.getExplanation();
                    }
                    else if (category.equals("Text + Video")) {
                        information.setText(arobject.getDescription());
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
        continueButton.setOnClickListener(v -> { goBackToARView(); });
    }

    private void progressBar(Integer questionProgress) {
        progressNum.setText(String.valueOf(questionProgress));

        // Calculates the percentage of the progress
        progress = (float) questionProgress / (float) totalQuestions;
        progressPercentage = progress * 100;
        int roundedPercentage = Math.round(progressPercentage); //Rounds the percentage to a whole number.
        pb.setProgress(roundedPercentage);
    }

    private void goBackToARView() {
        if (category.equals("Quiz")) {
            if (questionProgress < totalQuestions) {
                Intent intent = new Intent(InformationActivity.this, QR_Activity.class);
                intent.putExtra("label", label);
                intent.putStringArrayListExtra("selectedLabels", selectedLabels);
                intent.putExtra("category", category);
                if (category.equals("Quiz")) {
                    SharedPreferences sharedPreferences = getSharedPreferences("quizData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("correctQuestions", correctQuestions);
                    editor.putInt("wrongQuestions", wrongQuestions);
                    editor.putInt("questionProgress", questionProgress);
                    editor.apply();
                }
                startActivity(intent);
            } else {
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
            Intent intent = new Intent(InformationActivity.this, QR_Activity.class);
            intent.putExtra("label", label);
            intent.putStringArrayListExtra("selectedLabels", selectedLabels);
            intent.putExtra("category", category);
            startActivity(intent);
        }
    }
}
