package com.example.tsiisware;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
    Button gobackButton, answer1, answer2, answer3, answer4;
    String correctAnswer;
    Integer totalQuestions, questionProgress, correctQuestions, wrongQuestions;
    Float progress;
    Float progressPercentage;
    private final int quizEndDelay = 3000; //3 seconds delay. The delay indicates the time the splashscreen is visible.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        label = getIntent().getStringExtra("label");
        category = getIntent().getStringExtra("category");
        if (Objects.equals(category, "Quiz")) {
            correctQuestions = getIntent().getIntExtra("correctQuestions", 0);
            wrongQuestions = getIntent().getIntExtra("wrongQuestions", 0);
            questionProgress = getIntent().getIntExtra("questionProgress", 0);
        }

        switch (category) {
            case "Quiz":
                setContentView(R.layout.activity_main_informationview_quiz);
                answer1 = findViewById(R.id.answer_1);
                answer2 = findViewById(R.id.answer_2);
                answer3 = findViewById(R.id.answer_3);
                answer4 = findViewById(R.id.answer_4);
                quizQuestion = findViewById(R.id.question);
                pb = findViewById(R.id.progressBar);
                progressNum = findViewById(R.id.progressNumber);
                progressMax = findViewById(R.id.progressMax);

                // Answer OnClickListeners
                answer1.setOnClickListener(v -> {
                    if (correctAnswer.equals(answer1.getText().toString())) {
                        // Correct answer
                        answer1.setBackgroundColor(Color.GREEN);
                        correctQuestions++;
                        questionProgress++;
                        progressBar(questionProgress);
                    } else {
                        // Wrong answer
                        answer1.setBackgroundColor(Color.RED);
                        wrongQuestions++;
                        questionProgress++;
                        progressBar(questionProgress);
                    }
                    goBackToARView();
                });

                answer2.setOnClickListener(v -> {
                    if (correctAnswer.equals(answer2.getText().toString())) {
                        // Correct answer
                        answer2.setBackgroundColor(Color.GREEN);
                        questionProgress++;
                        progressBar(questionProgress);
                    } else {
                        // Wrong answer
                        answer2.setBackgroundColor(Color.RED);
                        questionProgress++;
                        progressBar(questionProgress);
                    }
                    goBackToARView();
                });

                answer3.setOnClickListener(v -> {
                    if (correctAnswer.equals(answer3.getText().toString())) {
                        // Correct answer
                        answer3.setBackgroundColor(Color.GREEN);
                        questionProgress++;
                        progressBar(questionProgress);
                    } else {
                        // Wrong answer
                        answer3.setBackgroundColor(Color.RED);
                        questionProgress++;
                        progressBar(questionProgress);
                    }
                    goBackToARView();
                });

                answer4.setOnClickListener(v -> {
                    if (correctAnswer.equals(answer4.getText().toString())) {
                        // Correct answer
                        answer4.setBackgroundColor(Color.GREEN);
                        questionProgress += 1;
                        progressBar(questionProgress);
                    } else {
                        // Wrong answer
                        answer4.setBackgroundColor(Color.RED);
                        questionProgress++;
                        progressBar(questionProgress);
                    }
                    goBackToARView();
                });
                break;
            case "Text + Video":
                setContentView(R.layout.activity_main_informationview_text_video);
                title = findViewById(R.id.titleTextVideo);
                title.setText("Text + Video: " + label);
                information = findViewById(R.id.informationText);
                information.setText("Loading...");
                break;
            case "Video":
                setContentView(R.layout.activity_main_informationview_video);
                title = findViewById(R.id.textViewVideo);
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
    }

    private void getObjectInformation(String label, String category) {
        db = FirebaseFirestore.getInstance();
        CollectionReference objectItems =  db.collection("objects");
               objectItems.document(label).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
//                    String iframeStructure = String.format("<iframe width=\"100%%\" height=\"100%%\" src=\"%s\" frameborder=\"0\" allowfullscreen></iframe>", document.getString("video_url"));
                    ARObject arobject = new ARObject(
                            document.getString("name"),
                            document.getString("description"),
                            document.getString("video"),
                            document.getString("question"),
                            (List<String>) document.get("answers"),
                            document.getString("correct_answer")
                    );

                        webView.getSettings().setJavaScriptEnabled(true);
                        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                        webView.setWebViewClient(new WebViewClient());
                        webView.setWebChromeClient(new WebChromeClient());
                        webView.loadData(arobject.getVideoURL(), "text/html", "utf-8");

                        // Disable user interaction
                        webView.setOnTouchListener((v, event) -> {
                            int width = webView.getWidth();
                            int height = webView.getHeight();
                            float x = event.getX();
                            float y = event.getY();

                            // Define the middle area (e.g., 20% of the width and height)
                            float middleAreaWidth = width * 0.75f;
                            float middleAreaHeight = height * 0.75f;
                            float middleXStart = (width - middleAreaWidth) / 2;
                            float middleYStart = (height - middleAreaHeight) / 2;

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

                            // Counts how many records are in the object table.
                            AggregateQuery queryCount = objectItems.count();
                            queryCount.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<AggregateQuerySnapshot> task1) {
                                    if(task1.isSuccessful())
                                    {
                                        AggregateQuerySnapshot snapshot = task1.getResult();
                                        if (snapshot != null) {
                                            totalQuestions = (int) snapshot.getCount();
                                            progressMax.setText(String.valueOf(totalQuestions));
                                        }
                                    }
                                    else {
                                        Log.e("Error", "Task failed: ", task1.getException());
                                    }
                                }
                            });
                        }
                        if (category.equals("Text + Video")) {
                            information.setText(arobject.getDescription());
                        }
                    }
                }
            });
        }
    private void progressBar(Integer questionProgress)
    {
        progressNum.setText(String.valueOf(questionProgress));

        // Calculates the percentage of the progress
        progress = (float) questionProgress / (float) totalQuestions;
        progressPercentage = progress * 100;
        Integer roundedPercentage = Math.round(progressPercentage); //Rounds the percentage to a whole number.
        pb.setProgress(roundedPercentage);
        if(questionProgress.equals(totalQuestions))
        {
            // Executes delayed code without affecting the content view
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run(){
                    // Prepare End Quiz Activity
                    setContentView(R.layout.activity_main_quiz_end);
                }
            }, quizEndDelay);
        }
    }

    private void goBackToARView() {
        Intent intent = new Intent(this, AR_Activity.class);
        intent.putExtra("label", label);
        intent.putExtra("category", category);
        if (category.equals("Quiz")) {
            intent.putExtra("correctQuestions", correctQuestions);
            intent.putExtra("wrongQuestions", wrongQuestions);
            intent.putExtra("questionProgress", 1);
        }
        startActivity(intent);
    }
}
