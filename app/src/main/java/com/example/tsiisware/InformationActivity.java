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

public class InformationActivity extends AppCompatActivity {
    ProgressBar pb;
    FirebaseFirestore db;
    String label = null;
    String category = null;
    TextView quizQuestion, progressNum, progressMax;
    WebView webView;
    Button gobackButton, answer1, answer2, answer3, answer4;

    String correctAnswer;
    Integer totalQuestions;
    Integer questionProgress;
    Float progress;
    Float progressPercentage;

    private final int quizEndDelay = 2500; //2.5 seconds delay. The delay indicates the time the splashscreen is visible.

   /* public final void setQuizProgress()
    {
        int progress = (questionProgress / totalQuestions) * 100;
        pb.setProgress(progress);

    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        label = getIntent().getStringExtra("label");
        category = getIntent().getStringExtra("category");

        switch (category) {
            case "Quiz":
                setContentView(R.layout.activity_main_informationview_quiz);
                break;
            case "Text + Video":
                setContentView(R.layout.activity_main_informationview_text_video);
                break;
            case "Video":
                setContentView(R.layout.activity_main_informationview_video);
                break;
            default:
                setContentView(R.layout.ar_view);
        }

        pb = findViewById(R.id.progressBar);
        progressNum = findViewById(R.id.progressNumber);
        progressMax = findViewById(R.id.progressMax);
        gobackButton = findViewById(R.id.go_back);
        answer1 = findViewById(R.id.answer_1);
        answer2 = findViewById(R.id.answer_2);
        answer3 = findViewById(R.id.answer_3);
        answer4 = findViewById(R.id.answer_4);
        quizQuestion = findViewById(R.id.question);
        webView = findViewById(R.id.webView);
        getObjectInformation(label, category);

        //setQuizProgress();

        gobackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToARView();
            }
        });

        // Answer OnClickListeners
        answer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (correctAnswer.equals(answer1.getText().toString())) {
                    // Correct answer
                    answer1.setBackgroundColor(Color.GREEN);
                    questionProgress += 1;
                    progressBar(questionProgress);

                } else {
                    // Wrong answer
                    answer1.setBackgroundColor(Color.RED);
                }
            }
        });

        answer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (correctAnswer.equals(answer2.getText().toString())) {
                    // Correct answer
                    answer2.setBackgroundColor(Color.GREEN);
                    questionProgress += 1;
                    progressBar(questionProgress);
                } else {
                    // Wrong answer
                    answer2.setBackgroundColor(Color.RED);
                }
            }
        });

        answer3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (correctAnswer.equals(answer3.getText().toString())) {
                    // Correct answer
                    answer3.setBackgroundColor(Color.GREEN);
                    questionProgress += 1;
                    progressBar(questionProgress);
                } else {
                    // Wrong answer
                    answer3.setBackgroundColor(Color.RED);
                }
            }
        });

        answer4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (correctAnswer.equals(answer4.getText().toString())) {
                    // Correct answer
                    answer4.setBackgroundColor(Color.GREEN);
                    questionProgress += 1;
                    progressBar(questionProgress);
                } else {
                    // Wrong answer
                    answer4.setBackgroundColor(Color.RED);
                }
            }
        });
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

                    if (category.equals("Quiz")) {
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
                            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                                if(task.isSuccessful())
                                {
                                    AggregateQuerySnapshot snapshot = task.getResult();
                                    totalQuestions = Integer.parseInt(String.valueOf(snapshot.getCount()));
                                    progressMax.setText(String.valueOf(totalQuestions));
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void progressBar(Integer questionProgress)
    {
        progressNum.setText(String.valueOf(questionProgress));

        // Calculates the percentage of the progress
        progress = (float) questionProgress/ (float) totalQuestions;
        progressPercentage = progress * 100;
        Integer roundedPercentage = Math.round(progressPercentage); //Rounds the percentage to a whole number.
        pb.setProgress(roundedPercentage);
        if(questionProgress == totalQuestions)
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
        startActivity(intent);
    }
}