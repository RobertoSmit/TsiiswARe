package com.example.tsiisware;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Objects;

public class InformationActivity extends AppCompatActivity {
    FirebaseFirestore db;
    String label = null, category = null;
    TextView quizQuestion, title, information;
    WebView webView;
    Button gobackButton, answer1, answer2, answer3, answer4, reloadButton;
    Integer correctQuestions, wrongQuestions;
    Boolean isClicked = false;
    String correctAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        label = getIntent().getStringExtra("label");
        category = getIntent().getStringExtra("category");
        if (Objects.equals(category, "Quiz")) {
            correctQuestions = getIntent().getIntExtra("correctQuestions", 0);
            wrongQuestions = getIntent().getIntExtra("wrongQuestions", 0);
        }

        switch (category) {
            case "Quiz":
                setContentView(R.layout.activity_main_informationview_quiz);
                answer1 = findViewById(R.id.answer_1);
                answer2 = findViewById(R.id.answer_2);
                answer3 = findViewById(R.id.answer_3);
                answer4 = findViewById(R.id.answer_4);
                quizQuestion = findViewById(R.id.question);

                // Answer OnClickListeners
                answer1.setOnClickListener(v -> {
                    if (correctAnswer.equals(answer1.getText().toString())) {
                        // Correct answer
                        answer1.setBackgroundColor(Color.GREEN);
                        correctQuestions++;
                    } else {
                        // Wrong answer
                        answer1.setBackgroundColor(Color.RED);
                        wrongQuestions++;
                    }
                    goBackToARView();
                });

                answer2.setOnClickListener(v -> {
                    if (correctAnswer.equals(answer2.getText().toString())) {
                        // Correct answer
                        answer2.setBackgroundColor(Color.GREEN);
                    } else {
                        // Wrong answer
                        answer2.setBackgroundColor(Color.RED);
                    }
                    goBackToARView();
                });

                answer3.setOnClickListener(v -> {
                    if (correctAnswer.equals(answer3.getText().toString())) {
                        // Correct answer
                        answer3.setBackgroundColor(Color.GREEN);
                    } else {
                        // Wrong answer
                        answer3.setBackgroundColor(Color.RED);
                    }
                    goBackToARView();
                });

                answer4.setOnClickListener(v -> {
                    if (correctAnswer.equals(answer4.getText().toString())) {
                        // Correct answer
                        answer4.setBackgroundColor(Color.GREEN);
                    } else {
                        // Wrong answer
                        answer4.setBackgroundColor(Color.RED);
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

        webView = findViewById(R.id.webView);
        gobackButton = findViewById(R.id.go_back);
        reloadButton = findViewById(R.id.resetVideobtn); // Ensure this is in the correct layout file

        getObjectInformation(label, category);

        gobackButton.setOnClickListener(v -> goBackToARView());
        reloadButton.setOnClickListener(v -> reloadVideo());
    }

    private void reloadVideo() {
        getObjectInformation(label, category);
        isClicked = false;
    }

    private void getObjectInformation(String label, String category) {
        db = FirebaseFirestore.getInstance();
        db.collection("objects").document(label).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String iframeStructure = String.format("<iframe width=\"100%%\" height=\"100%%\" src=\"%s?rel=0&autoplay=1&showinfo=0&controls=0\" frameborder=\"0\" allowfullscreen></iframe>", document.getString("video_url"));
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
                        webView.loadData(iframeStructure, "text/html", "utf-8");

                        // The user can only click once in the middle of the screen then the touch event is ignored

                    webView.setOnTouchListener((v, event) -> {
                        if (isClicked) {
                            return true;
                        }
                        int width = webView.getWidth();
                        int height = webView.getHeight();
                        float x = event.getX();
                        float y = event.getY();

                        // Define the middle area (e.g., 75% of the width and height)
                        float middleAreaWidth = width * 0.20f;
                        float middleAreaHeight = height * 0.25f;
                        float middleXStart = (width - middleAreaWidth) / 2;
                        float middleYStart = (height - middleAreaHeight) / 2;

                        if (x >= middleXStart && x <= (middleXStart + middleAreaWidth) &&
                                y >= middleYStart && y <= (middleYStart + middleAreaHeight)) {
                            return false; // Allow touch event
                        } else {
                            isClicked = true;
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
                        }
                        if (category.equals("Text + Video")) {
                            information.setText(arobject.getDescription());
                        }
                    }
                }
        });
    }

    private void goBackToARView() {
        Intent intent = new Intent(this, AR_Activity.class);
        intent.putExtra("label", label);
        intent.putExtra("category", category);
        if (category.equals("Quiz")) {
            intent.putExtra("correctQuestions", correctQuestions);
            intent.putExtra("wrongQuestions", wrongQuestions);
        }
        startActivity(intent);
    }
}